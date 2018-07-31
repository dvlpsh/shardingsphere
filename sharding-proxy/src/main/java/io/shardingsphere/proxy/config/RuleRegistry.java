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

package io.shardingsphere.proxy.config;

import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.MoreExecutors;
import io.shardingsphere.core.api.config.ShardingRuleConfiguration;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.ShardingProperties;
import io.shardingsphere.core.constant.ShardingPropertiesConstant;
import io.shardingsphere.core.constant.TransactionType;
import io.shardingsphere.core.metadata.datasource.ShardingDataSourceMetaData;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.core.rule.MasterSlaveRule;
import io.shardingsphere.core.rule.ProxyAuthority;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.jdbc.orchestration.internal.OrchestrationProxyConfiguration;
import io.shardingsphere.jdbc.orchestration.internal.eventbus.ProxyEventBusEvent;
import io.shardingsphere.proxy.backend.constant.ProxyMode;
import io.shardingsphere.proxy.backend.jdbc.datasource.JDBCBackendDataSource;
import io.shardingsphere.proxy.metadata.ProxyShardingTableMetaData;
import io.shardingsphere.transaction.api.TransactionManager;
import io.shardingsphere.transaction.common.config.ProxyTransactionConfiguration;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

/**
 * Sharding rule registry.
 *
 * @author zhangliang
 * @author zhangyonglun
 * @author panjuan
 * @author zhaojun
 * @author wangkai
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class RuleRegistry {
    
    private static final RuleRegistry INSTANCE = new RuleRegistry();
    
    private ShardingRule shardingRule;
    
    private MasterSlaveRule masterSlaveRule;
    
    private JDBCBackendDataSource backendDataSource;
    
    private Map<String, DataSourceParameter> dataSourceConfigurationMap;
    
    private boolean showSQL;
    
    private ProxyMode proxyMode;
    
    private TransactionType transactionType;
    
    private TransactionManager transactionManager;
    
    private int maxWorkingThreads;
    
    private boolean proxyBackendUseNio;
    
    private int proxyBackendSimpleDbConnections;
    
    private int proxyBackendConnectionTimeout;
    
    private ProxyAuthority proxyAuthority;
    
    private ShardingDataSourceMetaData shardingDataSourceMetaData;
    
    private ShardingTableMetaData shardingTableMetaData;
    
    /**
     * Get instance of sharding rule registry.
     *
     * @return instance of sharding rule registry
     */
    public static RuleRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Initialize rule registry.
     *
     * @param config yaml proxy configuration
     */
    public synchronized void init(final OrchestrationProxyConfiguration config) {
        Properties properties = null == config.getShardingRule() ? config.getMasterSlaveRule().getProps() : config.getShardingRule().getProps();
        ShardingProperties shardingProperties = new ShardingProperties(null == properties ? new Properties() : properties);
        showSQL = shardingProperties.getValue(ShardingPropertiesConstant.SQL_SHOW);
        proxyMode = ProxyMode.valueOf(shardingProperties.<String>getValue(ShardingPropertiesConstant.PROXY_MODE));
        transactionType = TransactionType.valueOf(shardingProperties.<String>getValue(ShardingPropertiesConstant.PROXY_TRANSACTION_MODE));
        transactionManager = ProxyTransactionConfiguration.getInstance().configTransactionContext(transactionType);
        ProxyTransactionConfiguration.getInstance().registerListener();
        maxWorkingThreads = shardingProperties.getValue(ShardingPropertiesConstant.PROXY_MAX_WORKING_THREADS);
        proxyBackendUseNio = shardingProperties.getValue(ShardingPropertiesConstant.PROXY_BACKEND_USE_NIO);
        proxyBackendSimpleDbConnections = shardingProperties.getValue(ShardingPropertiesConstant.PROXY_BACKEND_SIMPLE_DB_CONNECTIONS);
        proxyBackendConnectionTimeout = shardingProperties.getValue(ShardingPropertiesConstant.PROXY_BACKEND_CONNECTION_TIMEOUT);
        shardingRule = new ShardingRule(
                null == config.getShardingRule() ? new ShardingRuleConfiguration() : config.getShardingRule().getShardingRuleConfiguration(), config.getDataSources().keySet());
        if (null != config.getMasterSlaveRule()) {
            masterSlaveRule = new MasterSlaveRule(config.getMasterSlaveRule().getMasterSlaveRuleConfiguration());
        }
        // TODO :jiaqi only use JDBC need connect db via JDBC, netty style should use SQL packet to get metadata
        backendDataSource = new JDBCBackendDataSource(transactionType, config.getDataSources());
        dataSourceConfigurationMap = new LinkedHashMap<>(config.getDataSources().size(), 1);
        if (proxyBackendUseNio) {
            for (Entry<String, DataSourceParameter> entry : config.getDataSources().entrySet()) {
                dataSourceConfigurationMap.put(entry.getKey(), entry.getValue());
            }
        }
        proxyAuthority = config.getProxyAuthority();
        shardingDataSourceMetaData = new ShardingDataSourceMetaData(getDataSourceURLs(config.getDataSources()), shardingRule, DatabaseType.MySQL);
    }
    
    private static Map<String, String> getDataSourceURLs(final Map<String, DataSourceParameter> dataSourceParameters) {
        Map<String, String> result = new LinkedHashMap<>(dataSourceParameters.size(), 1);
        for (Entry<String, DataSourceParameter> entry : dataSourceParameters.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getUrl());
        }
        return result;
    }
    
    /**
     * Initialize rule registry.
     *
     * @param executorService executor service
     */
    public void initShardingMetaData(final ExecutorService executorService) {
        shardingTableMetaData = new ProxyShardingTableMetaData(MoreExecutors.listeningDecorator(executorService), backendDataSource);
        if (!isMasterSlaveOnly()) {
            shardingTableMetaData.init(shardingRule);
        }
    }
    
    /**
     * Judge is master slave only.
     * 
     * @return is master slave only
     */
    public boolean isMasterSlaveOnly() {
        return shardingRule.getTableRules().isEmpty() && null != masterSlaveRule;
    }
    
    /**
     * Renew rule registry.
     *
     * @param proxyEventBusEvent proxy event bus event.
     */
    @Subscribe
    public void renew(final ProxyEventBusEvent proxyEventBusEvent) {
        init(new OrchestrationProxyConfiguration(proxyEventBusEvent.getDataSources(), proxyEventBusEvent.getOrchestrationConfig()));
    }
}
