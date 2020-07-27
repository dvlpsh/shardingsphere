/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.orchestration.repository.common.configuration.swapper;

import org.apache.shardingsphere.orchestration.repository.api.config.OrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.repository.api.config.OrchestrationCenterConfiguration;
import org.apache.shardingsphere.orchestration.repository.common.configuration.config.YamlOrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.repository.common.configuration.config.YamlOrchestrationRepositoryConfiguration;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class OrchestrationConfigurationYamlSwapperTest {
    
    private static final String LOGIC_SCHEMA = "logic_schema";
    
    @Test
    public void assertSwapToYamlOrchestrationConfiguration() {
        OrchestrationConfiguration data = getOrchestrationConfiguration();
        YamlOrchestrationConfiguration result = new OrchestrationConfigurationYamlSwapper().swapToYamlConfiguration(data);
        assertThat(result.getRegistryCenter().getType(), is(data.getRegistryCenterConfiguration().getType()));
        assertThat(result.getRegistryCenter().getNamespace(), is(data.getRegistryCenterConfiguration().getNamespace()));
        assertThat(result.getRegistryCenter().getServerLists(), is(data.getRegistryCenterConfiguration().getServerLists()));
        assertThat(result.getRegistryCenter().getProps(), is(data.getRegistryCenterConfiguration().getProps()));
    }
    
    private OrchestrationConfiguration getOrchestrationConfiguration() {
        return new OrchestrationConfiguration(LOGIC_SCHEMA, new OrchestrationCenterConfiguration("zookeeper", "127.0.0.1:2181,127.0.0.1:2182", "orchestration", new Properties()));
    }
    
    @Test
    public void assertSwapToOrchestrationConfiguration() {
        YamlOrchestrationConfiguration data = getYamlOrchestrationConfiguration();
        OrchestrationConfiguration result = new OrchestrationConfigurationYamlSwapper().swapToObject(data);
        assertThat(result.getRegistryCenterConfiguration().getType(), is(data.getRegistryCenter().getType()));
        assertThat(result.getRegistryCenterConfiguration().getNamespace(), is(data.getRegistryCenter().getNamespace()));
        assertThat(result.getRegistryCenterConfiguration().getServerLists(), is(data.getRegistryCenter().getServerLists()));
        assertThat(result.getRegistryCenterConfiguration().getProps(), is(data.getRegistryCenter().getProps()));
    }
    
    private YamlOrchestrationConfiguration getYamlOrchestrationConfiguration() {
        YamlOrchestrationRepositoryConfiguration registryCenterConfig = new YamlOrchestrationRepositoryConfiguration();
        registryCenterConfig.setType("zookeeper");
        registryCenterConfig.setProps(new Properties());
        registryCenterConfig.setServerLists("127.0.0.1:2181,127.0.0.1:2182");
        registryCenterConfig.setNamespace("orchestration");
        YamlOrchestrationConfiguration result = new YamlOrchestrationConfiguration();
        result.setName(LOGIC_SCHEMA);
        result.setRegistryCenter(registryCenterConfig);
        return result;
    }
}
