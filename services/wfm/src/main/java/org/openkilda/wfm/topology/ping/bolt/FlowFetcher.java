/* Copyright 2018 Telstra Open Source
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.openkilda.wfm.topology.ping.bolt;

import org.openkilda.messaging.Utils;
import org.openkilda.messaging.command.flow.FlowPingRequest;
import org.openkilda.messaging.info.flow.FlowPingResponse;
import org.openkilda.messaging.model.BidirectionalFlow;
import org.openkilda.messaging.model.Flow;
import org.openkilda.pce.provider.PathComputer;
import org.openkilda.pce.provider.PathComputerAuth;
import org.openkilda.wfm.CommandContext;
import org.openkilda.wfm.error.AbstractException;
import org.openkilda.wfm.error.PipelineException;
import org.openkilda.wfm.share.utils.FlowCollector;
import org.openkilda.wfm.share.utils.PathComputerFlowFetcher;
import org.openkilda.wfm.topology.ping.model.FlowRef;
import org.openkilda.wfm.topology.ping.model.FlowsHeap;
import org.openkilda.wfm.topology.ping.model.PingContext;
import org.openkilda.wfm.topology.ping.model.PingContext.Kinds;

import lombok.extern.slf4j.Slf4j;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

@Slf4j
public class FlowFetcher extends Abstract {
    public static final String BOLT_ID = ComponentId.FLOW_FETCHER.toString();

    public static final String FIELD_ID_FLOW_ID = Utils.FLOW_ID;
    public static final String FIELD_FLOW_REF = "flow_ref";
    public static final String FIELD_ID_ON_DEMAND_RESPONSE = NorthboundEncoder.FIELD_ID_PAYLOAD;

    public static final Fields STREAM_FIELDS = new Fields(FIELD_ID_FLOW_ID, FIELD_ID_PING, FIELD_ID_CONTEXT);

    public static final Fields STREAM_EXPIRE_CACHE_FIELDS = new Fields(FIELD_FLOW_REF, FIELD_ID_CONTEXT);
    public static final String STREAM_EXPIRE_CACHE_ID = "expire_cache";

    public static final Fields STREAM_ON_DEMAND_RESPONSE_FIELDS = new Fields(
            FIELD_ID_ON_DEMAND_RESPONSE, FIELD_ID_CONTEXT);
    public static final String STREAM_ON_DEMAND_RESPONSE_ID = "on_demand_response";

    private final PathComputerAuth pathComputerAuth;
    private PathComputer pathComputer = null;
    private FlowsHeap flowsHeap;

    public FlowFetcher(PathComputerAuth pathComputerAuth) {
        this.pathComputerAuth = pathComputerAuth;
    }

    @Override
    protected void handleInput(Tuple input) throws AbstractException {
        String component = input.getSourceComponent();

        if (TickDeduplicator.BOLT_ID.equals(component)) {
            handlePeriodicRequest(input);
        } else if (InputRouter.BOLT_ID.equals(component)) {
            handleOnDemandRequest(input);
        } else {
            unhandledInput(input);
        }
    }

    private void handlePeriodicRequest(Tuple input) throws PipelineException {
        log.debug("Handle periodic ping request");
        PathComputerFlowFetcher fetcher = new PathComputerFlowFetcher(pathComputer);

        final CommandContext commandContext = pullContext(input);
        final FlowsHeap heap = new FlowsHeap();
        for (BidirectionalFlow flow : fetcher.getFlows()) {
            if (!flow.isPeriodicPings()) {
                log.debug("Skip flow {} due to isPeriodicPings == false", flow.getFlowId());
                continue;
            }

            PingContext pingContext = new PingContext(Kinds.PERIODIC, flow);
            emit(input, pingContext, commandContext);

            heap.add(flow);
        }

        emitCacheExpire(input, commandContext, heap);
        flowsHeap = heap;
    }

    private void handleOnDemandRequest(Tuple input) throws PipelineException {
        log.debug("Handle on demand ping request");
        FlowPingRequest request = pullOnDemandRequest(input);
        BidirectionalFlow flow;
        try {
            FlowCollector collector = new FlowCollector();
            for (Flow halfFlow : pathComputer.getFlow(request.getFlowId())) {
                collector.add(halfFlow);
            }
            flow = collector.make();
        } catch (IllegalArgumentException e) {
            emitOnDemandResponse(input, request, String.format(
                    "Can't read flow %s: %s", request.getFlowId(), e.getMessage()));
            return;
        }

        PingContext pingContext = new PingContext(Kinds.ON_DEMAND, flow).toBuilder()
                .timeout(request.getTimeout())
                .build();
        emit(input, pingContext, pullContext(input));
    }

    private void emit(Tuple input, PingContext pingContext, CommandContext commandContext) {
        Values output = new Values(pingContext.getFlowId(), pingContext, commandContext);
        getOutput().emit(input, output);
    }

    private void emitOnDemandResponse(Tuple input, FlowPingRequest request, String errorMessage)
            throws PipelineException {
        FlowPingResponse response = new FlowPingResponse(request.getFlowId(), errorMessage);
        Values output = new Values(response, pullContext(input));
        getOutput().emit(STREAM_ON_DEMAND_RESPONSE_ID, input, output);
    }

    private void emitCacheExpire(Tuple input, CommandContext commandContext, FlowsHeap heap) {
        OutputCollector collector = getOutput();
        for (FlowRef ref : flowsHeap.extra(heap)) {
            Values output = new Values(ref, commandContext);
            collector.emit(STREAM_EXPIRE_CACHE_ID, input, output);
        }
    }

    private FlowPingRequest pullOnDemandRequest(Tuple input) throws PipelineException {
        return pullValue(input, InputRouter.FIELD_ID_PING_REQUEST, FlowPingRequest.class);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputManager) {
        outputManager.declare(STREAM_FIELDS);
        outputManager.declareStream(STREAM_EXPIRE_CACHE_ID, STREAM_EXPIRE_CACHE_FIELDS);
        outputManager.declareStream(STREAM_ON_DEMAND_RESPONSE_ID, STREAM_ON_DEMAND_RESPONSE_FIELDS);
    }

    @Override
    public void init() {
        pathComputer = pathComputerAuth.getPathComputer();
        flowsHeap = new FlowsHeap();
    }
}
