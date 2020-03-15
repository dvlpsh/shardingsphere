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

package org.apache.shardingsphere.core.metadata;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.rule.DataNode;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.rule.TableRule;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetas;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetasLoader;
import org.apache.shardingsphere.underlying.common.exception.ShardingSphereException;
import org.apache.shardingsphere.underlying.common.log.MetaDataLogger;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Table metas loader for sharding.
 */
@RequiredArgsConstructor
public final class ShardingTableMetasLoader {
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final ShardingRule shardingRule;
    
    private final int maxConnectionsSizePerQuery;
    
    private final boolean isCheckingMetaData;
    
    /**
     * Load table meta data.
     * 
     * @param logicTableName logic table name
     * @return table meta data
     * @throws SQLException SQL exception
     */
    public TableMetaData load(final String logicTableName) throws SQLException {
        TableRule tableRule = shardingRule.getTableRule(logicTableName);
        if (!isCheckingMetaData) {
            DataNode dataNode = tableRule.getActualDataNodes().iterator().next();
            return TableMetasLoader.load(dataSourceMap.get(shardingRule.getShardingDataSourceNames().getRawMasterDataSourceName(dataNode.getDataSourceName())), dataNode.getTableName());
        }
        Map<String, List<DataNode>> dataNodeGroups = tableRule.getDataNodeGroups();
        Map<String, TableMetaData> actualTableMetaDataMap = new HashMap<>(dataNodeGroups.size(), 1);
        // TODO use multiple thread for diff data source
        for (Entry<String, List<DataNode>> entry : dataNodeGroups.entrySet()) {
            for (DataNode each : entry.getValue()) {
                actualTableMetaDataMap.put(each.getTableName(), TableMetasLoader.load(dataSourceMap.get(each.getDataSourceName()), each.getTableName()));
            }
        }
        checkUniformed(logicTableName, actualTableMetaDataMap);
        return actualTableMetaDataMap.values().iterator().next();
    }
    
    /**
     * Load table metas.
     *
     * @return table metas
     * @throws SQLException SQL exception
     */
    public TableMetas load() throws SQLException {
        Map<String, TableMetaData> result = new HashMap<>();
        result.putAll(loadShardingTables());
        result.putAll(loadDefaultTables());
        return new TableMetas(result);
    }
    
    private Map<String, TableMetaData> loadShardingTables() throws SQLException {
        Map<String, TableMetaData> result = new HashMap<>(shardingRule.getTableRules().size(), 1);
        long start = System.currentTimeMillis();
        MetaDataLogger.log("There are {} sharding table(s) will be loaded.", shardingRule.getTableRules().size());
        for (TableRule each : shardingRule.getTableRules()) {
            result.put(each.getLogicTable(), load(each.getLogicTable()));
        }
        MetaDataLogger.log("Sharding table(s) have been loaded in {} milliseconds.", System.currentTimeMillis() - start);
        return result;
    }
    
    private Map<String, TableMetaData> loadDefaultTables() throws SQLException {
        Optional<String> actualDefaultDataSourceName = shardingRule.findActualDefaultDataSourceName();
        if (!actualDefaultDataSourceName.isPresent()) {
            return Collections.emptyMap();
        }
        long start = System.currentTimeMillis();
        TableMetas result = TableMetasLoader.load(dataSourceMap.get(actualDefaultDataSourceName.get()), maxConnectionsSizePerQuery);
        MetaDataLogger.log("Default table(s) have been loaded in {} milliseconds.", System.currentTimeMillis() - start);
        return result.getTables();
    }
    
    // TODO check all meta data for one time
    private void checkUniformed(final String logicTableName, final Map<String, TableMetaData> actualTableMetaDataMap) {
        ShardingTableMetaDataDecorator decorator = new ShardingTableMetaDataDecorator();
        TableMetaData sample = decorator.decorate(actualTableMetaDataMap.values().iterator().next(), logicTableName, shardingRule);
        for (Entry<String, TableMetaData> entry : actualTableMetaDataMap.entrySet()) {
            if (!sample.equals(decorator.decorate(entry.getValue(), logicTableName, shardingRule))) {
                throw new ShardingSphereException(
                        "Cannot get uniformed table structure for logic table `%s` and actual table `%s`. The different meta data of actual tables are as follows:\n%s\n%s.",
                        logicTableName, entry.getKey(), sample, entry.getValue());
            }
        }
    }
}
