/*
 * Copyright 2018 Telstra Open Source
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

package org.openkilda.messaging.te.response;

import org.openkilda.messaging.info.InfoMessage;
import org.openkilda.messaging.te.request.LinkPropsSync;
import org.openkilda.messaging.te.request.LinkPropsSyncTest;

import org.junit.Assert;
import org.junit.Test;

public class LinkPropsSyncResponseTest extends LinkPropsSyncTest {
    @Test
    public void serializeLoop() throws Exception {
        LinkPropsSync request = makeRequest();
        LinkPropsSyncResponse origin = new LinkPropsSyncResponse(request, null);
        InfoMessage wrapper = new InfoMessage(origin, System.currentTimeMillis(), getClass().getCanonicalName());

        serialize(wrapper);

        InfoMessage result = (InfoMessage) deserialize();
        LinkPropsSyncResponse reconstructed = (LinkPropsSyncResponse) result.getData();

        Assert.assertEquals(origin, reconstructed);
    }

}
