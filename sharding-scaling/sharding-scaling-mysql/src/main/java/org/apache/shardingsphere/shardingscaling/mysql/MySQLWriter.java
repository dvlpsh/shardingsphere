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

package org.apache.shardingsphere.shardingscaling.mysql;

import org.apache.shardingsphere.shardingscaling.core.config.RdbmsConfiguration;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.writer.AbstractJdbcWriter;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.writer.AbstractSqlBuilder;
import org.apache.shardingsphere.shardingscaling.core.util.DataSourceFactory;

import javax.sql.DataSource;

/**
 * MySQL writer.
 *
 * @author avalon566
 * @author yangyi
 */
public final class MySQLWriter extends AbstractJdbcWriter {
    
    public MySQLWriter(final RdbmsConfiguration rdbmsConfiguration, final DataSourceFactory dataSourceFactory) {
        super(rdbmsConfiguration, dataSourceFactory);
    }
    
    @Override
    public AbstractSqlBuilder createSqlBuilder(final DataSource dataSource) {
        return new AbstractSqlBuilder(dataSource) {
            
            @Override
            public String getLeftIdentifierQuoteString() {
                return "`";
            }
            
            @Override
            public String getRightIdentifierQuoteString() {
                return "`";
            }
        };
    }
}
