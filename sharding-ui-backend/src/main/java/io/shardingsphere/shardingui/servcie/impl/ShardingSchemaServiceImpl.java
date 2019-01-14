/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingui.servcie.impl;

import com.google.common.base.Preconditions;
import io.shardingsphere.api.config.rule.RuleConfiguration;
import io.shardingsphere.core.config.DataSourceConfiguration;
import io.shardingsphere.orchestration.yaml.ConfigurationYamlConverter;
import io.shardingsphere.shardingui.servcie.RegistryCenterService;
import io.shardingsphere.shardingui.servcie.ShardingSchemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

/**
 * Implementation of sharding schema service.
 *
 * @author chenqingyang
 */
@Service
public final class ShardingSchemaServiceImpl implements ShardingSchemaService {
    
    @Autowired
    private RegistryCenterService registryCenterService;
    
    @Override
    public Collection<String> getAllSchemaNames() {
        return registryCenterService.getActivatedRegistryCenter().getChildrenKeys(registryCenterService.getActivateConfigurationNode().getSchemaPath());
    }
    
    @Override
    public String getRuleConfiguration(final String schemaName) {
        return registryCenterService.getActivatedRegistryCenter().get(registryCenterService.getActivateConfigurationNode().getRulePath(schemaName));
    }
    
    @Override
    public String getDataSourceConfiguration(final String schemaName) {
        return registryCenterService.getActivatedRegistryCenter().get(registryCenterService.getActivateConfigurationNode().getDataSourcePath(schemaName));
    }
    
    @Override
    public void updateRuleConfiguration(final String schemaName, final String configData) {
        checkRuleConfiguration(configData);
        registryCenterService.getActivatedRegistryCenter().persist(registryCenterService.getActivateConfigurationNode().getRulePath(schemaName), configData);
    }
    
    @Override
    public void updateDataSourceConfiguration(final String schemaName, final String configData) {
        checkDataSourceConfiguration(configData);
        registryCenterService.getActivatedRegistryCenter().persist(registryCenterService.getActivateConfigurationNode().getDataSourcePath(schemaName), configData);
    }
    
    private void checkRuleConfiguration(final String configData) {
        try {
            RuleConfiguration ruleConfig = configData.contains("tables:\n")
                    ? ConfigurationYamlConverter.loadShardingRuleConfiguration(configData) : ConfigurationYamlConverter.loadMasterSlaveRuleConfiguration(configData);
            Preconditions.checkState(ruleConfig != null, "rule configuration is invalid.");
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            throw new IllegalArgumentException("rule configuration is invalid.");
        }
    }
    
    private void checkDataSourceConfiguration(final String configData) {
        try {
            Map<String, DataSourceConfiguration> dataSourceConfigs = ConfigurationYamlConverter.loadDataSourceConfigurations(configData);
            Preconditions.checkState(!dataSourceConfigs.isEmpty(), "data source configuration is invalid.");
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            throw new IllegalArgumentException("data source configuration is invalid.");
        }
    }
}
