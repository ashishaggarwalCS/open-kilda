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

package org.openkilda.floodlight.command;

import org.openkilda.floodlight.config.provider.ConfigurationProvider;
import org.openkilda.floodlight.kafka.KafkaMessageCollector;
import org.openkilda.floodlight.service.ConfigService;
import org.openkilda.floodlight.utils.CommandContextFactory;

import net.floodlightcontroller.core.module.FloodlightModuleContext;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;

public abstract class AbstractCommandTest extends EasyMockSupport {
    protected final CommandContextFactory commandContextFactory = new CommandContextFactory();
    protected final FloodlightModuleContext moduleContext = new FloodlightModuleContext();
    protected final ConfigService configService = new ConfigService();

    @Before
    public void setUp() throws Exception {
        injectMocks(this);

        moduleContext.addService(ConfigService.class, configService);

        commandContextFactory.init(moduleContext);
        configService.init(ConfigurationProvider.of(moduleContext, new KafkaMessageCollector()));
    }

    @After
    public void tearDown() throws Exception {
        verifyAll();
    }
}
