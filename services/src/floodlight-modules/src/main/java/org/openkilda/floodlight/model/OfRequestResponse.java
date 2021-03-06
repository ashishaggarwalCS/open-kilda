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

package org.openkilda.floodlight.model;

import org.openkilda.floodlight.error.OfErrorResponseException;
import org.openkilda.floodlight.error.OfWriteException;

import org.projectfloodlight.openflow.protocol.OFErrorMsg;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.types.DatapathId;

public class OfRequestResponse {
    private final DatapathId dpId;
    private final OFMessage request;
    private final long xid;
    private OFMessage response = null;
    private OfWriteException error = null;

    public OfRequestResponse(DatapathId dpId, OFMessage request) {
        this.dpId = dpId;
        this.request = request;
        this.xid = request.getXid();
    }

    public long getXid() {
        return xid;
    }

    public DatapathId getDpId() {
        return dpId;
    }

    public OFMessage getRequest() {
        return request;
    }

    public OFMessage getResponse() {
        return response;
    }

    public OfWriteException getError() {
        return error;
    }

    /**
     * Set response field, also set error field, if response is an error.
     */
    public void setResponse(OFMessage response) {
        this.response = response;
        if (OFType.ERROR == response.getType()) {
            error = new OfErrorResponseException((OFErrorMsg) response);
        }
    }

    public void setError(OfWriteException error) {
        this.error = error;
    }

    /**
     * Make string representation.
     */
    public String toString() {
        String result = String.format("%s ===> %s ===> ", formatOfMessage(request), dpId);
        if (error != null) {
            result += error.getMessage();
        } else if (response != null) {
            result += formatOfMessage(response);
        } else {
            result += "no response";
        }

        return result;
    }

    private static String formatOfMessage(OFMessage message) {
        return String.format("%s:%s xid: %d", message.getType(), message.getVersion(), message.getXid());
    }
}
