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

package org.openkilda.messaging.payload.flow;

import org.openkilda.messaging.Utils;
import org.openkilda.messaging.command.flow.FlowDirection;
import org.openkilda.messaging.command.flow.FlowVerificationRequest;
import org.openkilda.messaging.command.flow.UniFlowVerificationRequest;
import org.openkilda.messaging.info.InfoMessage;
import org.openkilda.messaging.info.flow.FlowVerificationErrorCode;
import org.openkilda.messaging.info.flow.FlowVerificationResponse;
import org.openkilda.messaging.info.flow.UniFlowVerificationResponse;
import org.openkilda.messaging.info.flow.VerificationMeasures;
import org.openkilda.messaging.model.Flow;
import org.openkilda.northbound.dto.flows.VerificationOutput;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.UUID;

public class FlowPayloadToFlowConverterTest {
    @Test
    public void serialize() throws IOException {
        // prepare data
        String flowId = "flow-ping-test";
        FlowEndpointPayload endpointA = new FlowEndpointPayload("ff:fe:00:00:00:00:00:01", 8, 64);
        FlowEndpointPayload endpointB = new FlowEndpointPayload("ff:fe:00:00:00:00:00:02", 9, 65);

        Flow forward = new Flow(
                flowId, 1000, false, "flow ping test",
                endpointA.getSwitchDpId(), endpointA.getPortId(), endpointA.getVlanId(),
                endpointB.getSwitchDpId(), endpointB.getPortId(), endpointB.getPortId());
        Flow reverse = new Flow(
                flowId, 1000, false, "flow ping test",
                endpointB.getSwitchDpId(), endpointB.getPortId(), endpointB.getPortId(),
                endpointA.getSwitchDpId(), endpointA.getPortId(), endpointA.getVlanId());

        FlowVerificationRequest request = new FlowVerificationRequest(flowId, 5);
        FlowVerificationResponse payload = new FlowVerificationResponse(
                flowId,
                new UniFlowVerificationResponse(
                        new UniFlowVerificationRequest(request, forward, FlowDirection.FORWARD),
                        new VerificationMeasures(10L, 3L, 4L)),
                new UniFlowVerificationResponse(
                        new UniFlowVerificationRequest(request, reverse, FlowDirection.REVERSE),
                        FlowVerificationErrorCode.TIMEOUT));
        InfoMessage message = new InfoMessage(payload, System.currentTimeMillis(), UUID.randomUUID().toString());

        // test
        VerificationOutput output = FlowPayloadToFlowConverter.buildVerificationOutput(
                (FlowVerificationResponse) message.getData());

        Assert.assertEquals(flowId, output.getFlowId());

        Assert.assertTrue(output.getForward().isPingSuccess());
        Assert.assertNull(output.getForward().getError());
        Assert.assertEquals(payload.getForward().getMeasures().getNetworkLatency(), output.getForward().getLatency());

        Assert.assertFalse(output.getReverse().isPingSuccess());
        Assert.assertNotNull(output.getReverse().getError());
        Assert.assertEquals(0, output.getReverse().getLatency());

        // check is it JSON serializable
        String json = Utils.MAPPER.writeValueAsString(output);
        Assert.assertNotNull(json);
    }
}
