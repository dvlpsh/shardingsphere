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

package org.apache.shardingsphere.underlying.common.metadata.schema;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.order.OrderedSPIRegistry;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetaData;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.underlying.common.database.type.DatabaseType;
import org.apache.shardingsphere.underlying.common.metadata.schema.spi.RuleTableMetaDataLoader;
import org.apache.shardingsphere.underlying.common.metadata.schema.spi.RuleTableMetaDataDecorator;
import org.apache.shardingsphere.underlying.common.rule.BaseRule;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Rule schema meta data loader.
 */
@RequiredArgsConstructor
public final class RuleSchemaMetaDataLoader {
    
    static {
        ShardingSphereServiceLoader.register(RuleTableMetaDataLoader.class);
        ShardingSphereServiceLoader.register(RuleTableMetaDataDecorator.class);
    }
    
    private final Collection<BaseRule> rules;
    
    /**
     * Load schema meta data.
     * 
     * @param databaseType database type
     * @param dataSourceMap data source map
     * @param properties configuration properties
     * @return schema meta data
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("unchecked")
    public SchemaMetaData load(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap, final ConfigurationProperties properties) throws SQLException {
        Map<String, TableMetaData> result = new HashMap<>();
        for (Entry<BaseRule, RuleTableMetaDataLoader> entry : getLoaders().entrySet()) {
            result.putAll(entry.getValue().load(databaseType, dataSourceMap, entry.getKey(), properties));
        }
        // TODO load remain tables
        return decorate(new SchemaMetaData(result));
    }
    
    /**
     * Load schema meta data.
     *
     * @param databaseType database type
     * @param dataSource data source
     * @param properties configuration properties
     * @return schema meta data
     * @throws SQLException SQL exception
     */
    public SchemaMetaData load(final DatabaseType databaseType, final DataSource dataSource, final ConfigurationProperties properties) throws SQLException {
        Map<String, DataSource> dataSourceMap = new HashMap<>(1, 1);
        dataSourceMap.put("ds", dataSource);
        return load(databaseType, dataSourceMap, properties);
    }
    
    /**
     * Load schema meta data.
     *
     * @param databaseType database type
     * @param dataSourceMap data source map
     * @param tableName table name
     * @param properties configuration properties
     * @return schema meta data
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("unchecked")
    public Optional<TableMetaData> load(final DatabaseType databaseType, 
                                        final Map<String, DataSource> dataSourceMap, final String tableName, final ConfigurationProperties properties) throws SQLException {
        for (Entry<BaseRule, RuleTableMetaDataLoader> entry : getLoaders().entrySet()) {
            Optional<TableMetaData> result = entry.getValue().load(databaseType, dataSourceMap, tableName, entry.getKey(), properties);
            if (result.isPresent()) {
                return Optional.of(decorate(tableName, result.get()));
            }
        }
        return Optional.empty();
    }
    
    /**
     * Load schema meta data.
     *
     * @param databaseType database type
     * @param dataSource data source
     * @param tableName table name
     * @param properties configuration properties
     * @return schema meta data
     * @throws SQLException SQL exception
     */
    public Optional<TableMetaData> load(final DatabaseType databaseType,
                                        final DataSource dataSource, final String tableName, final ConfigurationProperties properties) throws SQLException {
        Map<String, DataSource> dataSourceMap = new HashMap<>(1, 1);
        dataSourceMap.put("ds", dataSource);
        return load(databaseType, dataSourceMap, tableName, properties);
    }
    
    private Map<BaseRule, RuleTableMetaDataLoader> getLoaders() {
        Map<BaseRule, RuleTableMetaDataLoader> result = new LinkedHashMap<>();
        for (RuleTableMetaDataLoader each : OrderedSPIRegistry.getRegisteredServices(RuleTableMetaDataLoader.class)) {
            Class<?> ruleClass = (Class<?>) each.getType();
            // FIXME rule.getClass().getSuperclass() == ruleClass for orchestration, should decouple extend between orchestration rule and sharding rule
            rules.stream().filter(rule -> rule.getClass() == ruleClass || rule.getClass().getSuperclass() == ruleClass).collect(Collectors.toList())
                    .forEach(rule -> result.put(rule, each));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private SchemaMetaData decorate(final SchemaMetaData schemaMetaData) {
        Map<String, TableMetaData> result = new HashMap<>(schemaMetaData.getAllTableNames().size(), 1);
        Map<BaseRule, RuleTableMetaDataDecorator> decorators = getDecorators();
        for (String each : schemaMetaData.getAllTableNames()) {
            for (Entry<BaseRule, RuleTableMetaDataDecorator> entry : decorators.entrySet()) {
                result.put(each, entry.getValue().decorate(each, schemaMetaData.get(each), entry.getKey()));
            }
        }
        return new SchemaMetaData(result);
    }
    
    @SuppressWarnings("unchecked")
    private TableMetaData decorate(final String tableName, final TableMetaData tableMetaData) {
        TableMetaData result = tableMetaData;
        for (Entry<BaseRule, RuleTableMetaDataDecorator> entry : getDecorators().entrySet()) {
            result = entry.getValue().decorate(tableName, tableMetaData, entry.getKey());
        }
        return result;
    }
    
    private Map<BaseRule, RuleTableMetaDataDecorator> getDecorators() {
        Map<BaseRule, RuleTableMetaDataDecorator> result = new LinkedHashMap<>();
        for (RuleTableMetaDataDecorator each : OrderedSPIRegistry.getRegisteredServices(RuleTableMetaDataDecorator.class)) {
            Class<?> ruleClass = (Class<?>) each.getType();
            // FIXME rule.getClass().getSuperclass() == ruleClass for orchestration, should decouple extend between orchestration rule and sharding rule
            rules.stream().filter(rule -> rule.getClass() == ruleClass || rule.getClass().getSuperclass() == ruleClass).collect(Collectors.toList())
                    .forEach(rule -> result.put(rule, each));
        }
        return result;
    }
}
