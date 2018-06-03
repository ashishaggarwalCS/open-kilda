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

package org.openkilda.floodlight.config.provider;

import static org.easymock.EasyMock.niceMock;
import static org.junit.Assert.assertEquals;

import org.openkilda.config.KafkaTopicsConfig;
import org.openkilda.floodlight.config.KafkaFloodlightConfig;

import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.IFloodlightModule;
import org.junit.Test;

public class ConfigurationProviderTest {
    private static final String TEST_BOOTSTRAP_SERVERS = "test_server";

    @Test
    public void shouldCreateConfigFromContextParameters() {
        FloodlightModuleContext context = new FloodlightModuleContext();
        IFloodlightModule module = niceMock(IFloodlightModule.class);

        context.addConfigParam(module, "bootstrap-servers", TEST_BOOTSTRAP_SERVERS);

        ConfigurationProvider provider = new ConfigurationProvider(context, module);
        KafkaFloodlightConfig kafkaConfig = provider.getConfiguration(KafkaFloodlightConfig.class);
        KafkaTopicsConfig topicsConfig = provider.getConfiguration(KafkaTopicsConfig.class);

        assertEquals(TEST_BOOTSTRAP_SERVERS, kafkaConfig.getBootstrapServers());
    }
}
