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

package io.shardingsphere.transaction.xa.jta.connection;

import javax.sql.XADataSource;
import java.sql.Connection;

/**
 * Sharding XA connection wrapper.
 *
 * @author zhaojun
 */
public interface ShardingXAConnectionWrapper {
    
    /**
     * Wrap a normal connection to sharding XA connection.
     *
     * @param resourceName resource name
     * @param xaDataSource XA data source
     * @param connection connection
     * @return sharding XA connection
     */
    ShardingXAConnection wrap(String resourceName, XADataSource xaDataSource, Connection connection);
}
