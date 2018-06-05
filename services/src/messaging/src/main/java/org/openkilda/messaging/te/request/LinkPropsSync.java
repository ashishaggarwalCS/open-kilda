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

import org.openkilda.messaging.command.CommandData;
import org.openkilda.messaging.model.LinkProps;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class LinkPropsSync extends CommandData {
    public enum Commands {
        UPDATE,
        DELETE
    }

    @JsonProperty("command")
    Commands command;

    @JsonProperty("link_props")
    LinkProps linkProps;

    @JsonCreator
    public LinkPropsSync(
            @JsonProperty("command") Commands command,
            @JsonProperty("link_props") LinkProps linkProps) {
        this.command = command;
        this.linkProps = linkProps;
    }
}
