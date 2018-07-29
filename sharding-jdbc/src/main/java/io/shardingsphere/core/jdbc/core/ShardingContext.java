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

package io.shardingsphere.core.jdbc.core;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.executor.ExecutorEngine;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.metadata.datasource.ShardingDataSourceMetaData;
import io.shardingsphere.core.rule.ShardingRule;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Sharding runtime context.
 * 
 * @author gaohongtao
 * @author panjuan
 */
@RequiredArgsConstructor
@Getter
public final class ShardingContext {
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final ShardingRule shardingRule;
    
    private final DatabaseType databaseType;
    
    private final ExecutorEngine executorEngine;
    
    private final ShardingTableMetaData shardingTableMetaData;
    
    private final boolean showSQL;
    
    private final ShardingDataSourceMetaData shardingDataSourceMetaData;
    
    public ShardingContext(final Map<String, DataSource> dataSourceMap, final ShardingRule shardingRule,
                           final DatabaseType databaseType, final ExecutorEngine executorEngine, final ShardingTableMetaData shardingTableMetaData, final boolean showSQL) {
        this.dataSourceMap = dataSourceMap;
        this.shardingRule = shardingRule;
        this.databaseType = databaseType;
        this.executorEngine = executorEngine;
        this.shardingTableMetaData = shardingTableMetaData;
        this.showSQL = showSQL;
        shardingDataSourceMetaData = new ShardingDataSourceMetaData(getDataSourceURLs(dataSourceMap), shardingRule, databaseType);
    }
    
    private static Map<String, String> getDataSourceURLs(final Map<String, DataSource> dataSourceMap) {
        Map<String, String> result = new LinkedHashMap<>(dataSourceMap.size(), 1);
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            result.put(entry.getKey(), getDataSourceURL(entry.getValue()));
        }
        return result;
    }
    
    private static String getDataSourceURL(final DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            return connection.getMetaData().getURL();
        } catch (final SQLException ex) {
            throw new ShardingException(ex);
        }
    }
}
