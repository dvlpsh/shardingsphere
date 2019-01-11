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

package io.shardingsphere.transaction.xa.jta.datasource;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.transaction.xa.jta.connection.ShardingXAConnection;
import io.shardingsphere.transaction.xa.jta.connection.ShardingXAConnectionFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Sharding XA data source.
 *
 * @author zhaojun
 */
@Getter
@Slf4j
public final class ShardingXADataSource extends AbstractUnsupportedShardingXADataSource {
    
    private final DatabaseType databaseType;
    
    private final String resourceName;
    
    private final DataSource originalDataSource;
    
    private final XADataSource xaDataSource;
    
    private boolean isOriginalXADataSource;
    
    public ShardingXADataSource(final DatabaseType databaseType, final String resourceName, final DataSource dataSource) {
        this.databaseType = databaseType;
        this.resourceName = resourceName;
        this.originalDataSource = dataSource;
        if (dataSource instanceof XADataSource) {
            this.xaDataSource = (XADataSource) dataSource;
            this.isOriginalXADataSource = true;
        } else {
            this.xaDataSource = XADataSourceFactory.build(databaseType, dataSource);
        }
    }
    
    @Override
    public ShardingXAConnection getXAConnection() throws SQLException {
        return isOriginalXADataSource ? new ShardingXAConnection(resourceName, xaDataSource.getXAConnection())
            : ShardingXAConnectionFactory.createShardingXAConnection(databaseType, resourceName, xaDataSource, originalDataSource.getConnection());
    }
    
    /**
     * Get connection from original data source.
     *
     * @return connection
     * @throws SQLException SQL exception
     */
    public Connection getConnectionFromOriginalDataSource() throws SQLException {
        return originalDataSource.getConnection();
    }
}
