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

package org.openkilda.messaging.te.request;

import org.openkilda.messaging.StringSerializer;
import org.openkilda.messaging.command.CommandMessage;
import org.openkilda.messaging.model.LinkProps;
import org.openkilda.messaging.model.NetworkEndpoint;
import org.openkilda.messaging.te.request.LinkPropsSync.Commands;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

public class LinkPropsSyncTest implements StringSerializer {
    @Test
    public void serializeLoop() throws Exception {
        LinkPropsSync origin = makeRequest();
        CommandMessage wrapper = new CommandMessage(origin, System.currentTimeMillis(), getClass().getCanonicalName());

        serialize(wrapper);

        CommandMessage result = (CommandMessage) deserialize();
        LinkPropsSync reconstructed = (LinkPropsSync) result.getData();

        Assert.assertEquals(origin, reconstructed);
    }

    protected LinkPropsSync makeRequest() {
        HashMap<String, String> keyValuePairs = new HashMap<>();
        keyValuePairs.put("cost", "10000");

        LinkProps props = new LinkProps(
                new NetworkEndpoint("ff:fe:00:00:00:00:00:01", 8),
                new NetworkEndpoint("ff:fe:00:00:00:00:00:02", 8),
                keyValuePairs);
        return new LinkPropsSync(Commands.UPDATE, props);
    }
}
