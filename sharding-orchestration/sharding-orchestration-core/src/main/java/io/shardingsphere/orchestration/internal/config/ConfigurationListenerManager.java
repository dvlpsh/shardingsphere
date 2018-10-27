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

import io.shardingsphere.core.constant.ShardingConstant;
import io.shardingsphere.core.event.ShardingEventBusInstance;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.orchestration.internal.event.config.MasterSlaveConfigurationEventBusEvent;
import io.shardingsphere.orchestration.internal.event.config.ProxyConfigurationEventBusEvent;
import io.shardingsphere.orchestration.internal.event.config.ShardingConfigurationEventBusEvent;
import io.shardingsphere.orchestration.internal.listener.ListenerManager;
import io.shardingsphere.orchestration.internal.state.datasource.DataSourceService;
import io.shardingsphere.orchestration.reg.api.RegistryCenter;
import io.shardingsphere.orchestration.reg.listener.DataChangedEvent;
import io.shardingsphere.orchestration.reg.listener.EventListener;

import javax.sql.DataSource;
import java.util.Map;

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
    public void watchSharding() {
        watchSharding(configNode.getDataSourcePath(shardingSchemaName));
        watchSharding(configNode.getRulePath(shardingSchemaName));
        watchSharding(configNode.getPropsPath());
        // TODO watch config map
    }
    
    private void watchSharding(final String path) {
        regCenter.watch(path, new EventListener() {
            
            @Override
            public void onChange(final DataChangedEvent event) {
                if (DataChangedEvent.Type.UPDATED == event.getEventType()) {
                    Map<String, DataSource> dataSourceMap = dataSourceService.getAvailableDataSources(shardingSchemaName);
                    ShardingConfigurationEventBusEvent shardingEvent = new ShardingConfigurationEventBusEvent(dataSourceMap,
                            new ShardingRule(dataSourceService.getAvailableShardingRuleConfiguration(shardingSchemaName), dataSourceMap.keySet()), configService.loadProperties());
                    ShardingEventBusInstance.getInstance().post(shardingEvent);
                }
            }
        });
    }
    
    @Override
    public void watchMasterSlave() {
        watchMasterSlave(configNode.getDataSourcePath(ShardingConstant.LOGIC_SCHEMA_NAME));
        watchMasterSlave(configNode.getRulePath(ShardingConstant.LOGIC_SCHEMA_NAME));
        watchMasterSlave(configNode.getPropsPath());
        // TODO watch config map
    }
    
    private void watchMasterSlave(final String path) {
        regCenter.watch(path, new EventListener() {
            
            @Override
            public void onChange(final DataChangedEvent event) {
                if (DataChangedEvent.Type.UPDATED == event.getEventType()) {
                    MasterSlaveConfigurationEventBusEvent masterSlaveEvent = new MasterSlaveConfigurationEventBusEvent(dataSourceService.getAvailableDataSources(shardingSchemaName),
                            dataSourceService.getAvailableMasterSlaveRuleConfiguration(shardingSchemaName), configService.loadProperties());
                    ShardingEventBusInstance.getInstance().post(masterSlaveEvent);
                }
            }
        });
    }
    
    @Override
    public void watchProxy() {
        watchProxy(configNode.getDataSourcePath(shardingSchemaName));
        watchProxy(configNode.getRulePath(shardingSchemaName));
        watchProxy(configNode.getPropsPath());
    }
    
    private void watchProxy(final String path) {
        regCenter.watch(path, new EventListener() {
            
            @Override
            public void onChange(final DataChangedEvent event) {
                if (DataChangedEvent.Type.UPDATED == event.getEventType()) {
                    ShardingEventBusInstance.getInstance().post(new ProxyConfigurationEventBusEvent(dataSourceService.getProxyAvailableDataSourceParameters(), 
                            dataSourceService.getAvailableYamlProxyConfiguration(), configService.loadAuthentication(), configService.loadProperties()));
                }
            }
        });
    }
}
