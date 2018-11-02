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

package io.shardingsphere.orchestration.internal.config;

import io.shardingsphere.core.config.DataSourceConfiguration;
import io.shardingsphere.core.event.ShardingEventBusInstance;
import io.shardingsphere.core.rule.Authentication;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.orchestration.internal.event.config.ConfigurationChangedEvent;
import io.shardingsphere.orchestration.internal.event.config.MasterSlaveConfigurationChangedEvent;
import io.shardingsphere.orchestration.internal.event.config.ShardingConfigurationChangedEvent;
import io.shardingsphere.orchestration.internal.listener.ListenerManager;
import io.shardingsphere.orchestration.internal.state.datasource.DataSourceService;
import io.shardingsphere.orchestration.reg.api.RegistryCenter;
import io.shardingsphere.orchestration.reg.listener.DataChangedEvent;
import io.shardingsphere.orchestration.reg.listener.EventListener;

import java.util.Map;
import java.util.Properties;

/**
 * Configuration listener manager.
 *
 * @author caohao
 * @author panjuan
 */
public final class ConfigurationListenerManager implements ListenerManager {
    
    private final ConfigurationNode configNode;
    
    private final RegistryCenter regCenter;
    
    private final String shardingSchemaName;
    
    private final ConfigurationService configService;
    
    private final DataSourceService dataSourceService;
    
    public ConfigurationListenerManager(final String name, final RegistryCenter regCenter, final String shardingSchemaName) {
        configNode = new ConfigurationNode(name);
        this.regCenter = regCenter;
        this.shardingSchemaName = shardingSchemaName;
        configService = new ConfigurationService(name, regCenter);
        dataSourceService = new DataSourceService(name, regCenter);
    }
    
    @Override
    public void watch() {
        watch(configNode.getDataSourcePath(shardingSchemaName));
        watch(configNode.getRulePath(shardingSchemaName));
        watch(configNode.getPropsPath());
        watch(configNode.getAuthenticationPath());
    }
    
    private void watch(final String path) {
        regCenter.watch(path, new EventListener() {
            
            @Override
            public void onChange(final DataChangedEvent event) {
                if (DataChangedEvent.Type.UPDATED == event.getEventType()) {
                    Map<String, DataSourceConfiguration> dataSourceConfigurations = dataSourceService.getAvailableDataSourceConfigurations(shardingSchemaName);
                    Authentication authentication = configService.loadAuthentication();
                    Properties props = configService.loadProperties();
                    ShardingEventBusInstance.getInstance().post(configService.isShardingRule(shardingSchemaName)
                            ? getShardingEvent(dataSourceConfigurations, authentication, props) : getMasterSlaveEvent(dataSourceConfigurations, authentication, props));
                }
            }
            
            private ConfigurationChangedEvent getShardingEvent(final Map<String, DataSourceConfiguration> dataSourceConfigurations, final Authentication authentication, final Properties props) {
                return new ShardingConfigurationChangedEvent(shardingSchemaName, dataSourceConfigurations,
                        new ShardingRule(dataSourceService.getAvailableShardingRuleConfiguration(shardingSchemaName), dataSourceConfigurations.keySet()), authentication, props);
            }
            
            private ConfigurationChangedEvent getMasterSlaveEvent(final Map<String, DataSourceConfiguration> dataSourceConfigurations, final Authentication authentication, final Properties props) {
                return new MasterSlaveConfigurationChangedEvent(shardingSchemaName, dataSourceConfigurations,
                        dataSourceService.getAvailableMasterSlaveRuleConfiguration(shardingSchemaName), authentication, props);
            }
        });
    }
}
