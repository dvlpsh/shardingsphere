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

import org.apache.shardingsphere.infra.yaml.swapper.YamlSwapper;
import org.apache.shardingsphere.orchestration.repository.api.config.OrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.repository.common.configuration.config.YamlOrchestrationConfiguration;

/**
 * Orchestration instance configuration YAML swapper.
 */
public final class OrchestrationConfigurationYamlSwapper implements YamlSwapper<YamlOrchestrationConfiguration, OrchestrationConfiguration> {
    
    private final OrchestrationCenterConfigurationYamlSwapper swapper = new OrchestrationCenterConfigurationYamlSwapper();
    
    @Override
    public YamlOrchestrationConfiguration swapToYamlConfiguration(final OrchestrationConfiguration configuration) {
        YamlOrchestrationConfiguration result = new YamlOrchestrationConfiguration();
        result.setNamespace(configuration.getNamespace());
        result.setRegistryCenter(swapper.swapToYamlConfiguration(configuration.getRegistryCenterConfiguration()));
        if (configuration.getAdditionalConfigCenterConfiguration().isPresent()) {
            result.setAdditionalConfigCenter(swapper.swapToYamlConfiguration(configuration.getAdditionalConfigCenterConfiguration().get()));
        }
        return result;
    }
    
    @Override
    public OrchestrationConfiguration swapToObject(final YamlOrchestrationConfiguration configuration) {
        if (null != configuration.getAdditionalConfigCenter()) {
            return new OrchestrationConfiguration(configuration.getNamespace(), swapper.swapToObject(configuration.getRegistryCenter()),
                    swapper.swapToObject(configuration.getAdditionalConfigCenter()), configuration.isOverwrite());
        }
        return new OrchestrationConfiguration(configuration.getNamespace(), swapper.swapToObject(configuration.getRegistryCenter()), configuration.isOverwrite());
    }
}
