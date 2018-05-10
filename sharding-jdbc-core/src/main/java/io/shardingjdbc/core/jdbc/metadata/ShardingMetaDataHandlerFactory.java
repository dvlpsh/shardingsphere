/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.core.jdbc.metadata;

import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.jdbc.metadata.dialect.DefaultShardingMetaDataHandler;
import io.shardingjdbc.core.jdbc.metadata.dialect.H2ShardingMetaDataHandler;
import io.shardingjdbc.core.jdbc.metadata.dialect.MySQLShardingMetaDataHandler;
import io.shardingjdbc.core.jdbc.metadata.dialect.ShardingMetaDataHandler;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;

/**
 * Table metadata handler factory.
 *
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingMetaDataHandlerFactory {
    
    /**
     * To generate table metadata handler by data type.
     *
     * @param dataSource data source
     * @param actualTableName actual table name
     * @param databaseType database type
     * @return sharding metadata handler
     */
    public static ShardingMetaDataHandler newInstance(final DataSource dataSource, final String actualTableName, final DatabaseType databaseType) {
        switch (databaseType) {
            case MySQL:
                return new MySQLShardingMetaDataHandler(dataSource, actualTableName);
            case H2:
                return new H2ShardingMetaDataHandler(dataSource, actualTableName);
            default:
                return new DefaultShardingMetaDataHandler(dataSource, actualTableName);
        }
    }

    /**
     * To generate table metadata handler by existing sharding connection.
     *
     * @param actualTableName actual table name
     * @param databaseType database type
     * @return sharding metadata handler
     */
    public static ShardingMetaDataHandler newInstance(final String actualTableName, final DatabaseType databaseType) {
        return newInstance(null, actualTableName, databaseType);
    }
}
