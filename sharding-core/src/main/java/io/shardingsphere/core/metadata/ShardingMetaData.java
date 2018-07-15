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

package io.shardingsphere.core.metadata;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.rule.DataNode;
import io.shardingsphere.core.rule.ShardingDataSourceNames;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.core.rule.TableRule;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * Sharding metadata.
 *
 * @author panjuan
 * @author zhaojun
 */
@RequiredArgsConstructor
@Getter
@Slf4j
public abstract class ShardingMetaData {
    
    private final ListeningExecutorService executorService;
    
    private final Map<String, TableMetaData> tableMetaDataMap = new HashMap<>();
    
    /**
     * Initialize sharding metadata.
     *
     * @param shardingRule sharding rule
     */
    public void init(final ShardingRule shardingRule) {
        try {
            for (TableRule each : getTableRules(shardingRule)) {
                refresh(each, shardingRule);
            }
        } catch (final SQLException ex) {
            throw new ShardingException(ex);
        }
    }
    
    private Collection<TableRule> getTableRules(final ShardingRule shardingRule) throws SQLException {
        Collection<TableRule> result = new LinkedList<>(shardingRule.getTableRules());
        String defaultDataSourceName = shardingRule.getShardingDataSourceNames().getDefaultDataSourceName();
        if (!Strings.isNullOrEmpty(defaultDataSourceName)) {
            Collection<String> defaultTableNames = getTableNamesFromDefaultDataSource(shardingRule.getMasterDataSourceName(defaultDataSourceName));
            for (String each : defaultTableNames) {
                result.add(shardingRule.getTableRule(each));
            }
        }
        return result;
    }
    
    /**
     * Get table names from default data source.
     *
     * @param defaultDataSourceName default data source name.
     * @return table names from default data source
     * @throws SQLException SQL exception.
     */
    public abstract Collection<String> getTableNamesFromDefaultDataSource(String defaultDataSourceName) throws SQLException;
    
    /**
     * Refresh each tableMetaData by TableRule.
     *
     * @param tableRule table rule
     * @param shardingRule sharding rule
     */
    public void refresh(final TableRule tableRule, final ShardingRule shardingRule) {
        refresh(tableRule, shardingRule, Collections.<String, Connection>emptyMap());
    }
    
    /**
     * Refresh each tableMetaData by TableRule.
     *
     * @param tableRule table rule
     * @param shardingRule sharding rule
     * @param connectionMap connection map passing from sharding connection
     */
    public void refresh(final TableRule tableRule, final ShardingRule shardingRule, final Map<String, Connection> connectionMap) {
        tableMetaDataMap.put(tableRule.getLogicTable(), getFinalTableMetaData(tableRule.getLogicTable(), tableRule.getActualDataNodes(), shardingRule.getShardingDataSourceNames(), connectionMap));
    }
    
    private TableMetaData getFinalTableMetaData(
            final String logicTableName, final List<DataNode> actualDataNodes, final ShardingDataSourceNames shardingDataSourceNames, final Map<String, Connection> connectionMap) {
        List<TableMetaData> actualTableMetaDataList = getAllActualTableMetaData(actualDataNodes, shardingDataSourceNames, connectionMap);
        for (int i = 0; i < actualTableMetaDataList.size(); i++) {
            if (actualTableMetaDataList.size() - 1 == i) {
                return actualTableMetaDataList.get(i);
            }
            if (!actualTableMetaDataList.get(i).equals(actualTableMetaDataList.get(i + 1))) {
                throw new ShardingException(getErrorMsgOfTableMetaData(logicTableName, actualTableMetaDataList.get(i), actualTableMetaDataList.get(i + 1)));
            }
        }
        return new TableMetaData();
    }
    
    private List<TableMetaData> getAllActualTableMetaData(final List<DataNode> actualDataNodes, final ShardingDataSourceNames shardingDataSourceNames, final Map<String, Connection> connectionMap) {
        List<ListenableFuture<TableMetaData>> result = new ArrayList<>();
        for (final DataNode each : actualDataNodes) {
            result.add(executorService.submit(new Callable<TableMetaData>() {
                
                @Override
                public TableMetaData call() throws Exception {
                    return getTableMetaData(each, shardingDataSourceNames, connectionMap);
                }
            }));
        }
        try {
            return Futures.allAsList(result).get();
        } catch (final InterruptedException | ExecutionException ex) {
            throw new ShardingException(ex);
        }
    }
    
    /**d
     * Get column metadata implementing by concrete handler.
     *
     * @param dataNode DataNode
     * @param shardingDataSourceNames ShardingDataSourceNames
     * @param connectionMap connection map from sharding connection
     * @return ColumnMetaData
     * @throws SQLException SQL exception
     */
    public abstract TableMetaData getTableMetaData(DataNode dataNode, ShardingDataSourceNames shardingDataSourceNames, Map<String, Connection> connectionMap) throws SQLException;
    
    private String getErrorMsgOfTableMetaData(final String logicTableName, final TableMetaData oldTableMetaData, final TableMetaData newTableMetaData) {
        return String.format("Cannot get uniformed table structure for %s. The different metadata of actual tables is as follows:\n%s\n%s.",
                logicTableName, oldTableMetaData.toString(), newTableMetaData.toString());
    }
    
    /**
     * Judge whether this database type is supported.
     *
     * @return supported or not
     */
    public boolean isSupportedDatabaseType() {
        for (TableMetaData each : tableMetaDataMap.values()) {
            if (each.getColumnMetaData().isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Judge has column from table meta data or not.
     * 
     * @param tableName table name
     * @param column column
     * @return has column from table meta data or not
     */
    public boolean hasColumn(final String tableName, final String column) {
        return tableMetaDataMap.containsKey(tableName) && tableMetaDataMap.get(tableName).getAllColumnNames().contains(column.toLowerCase());
    }
}
