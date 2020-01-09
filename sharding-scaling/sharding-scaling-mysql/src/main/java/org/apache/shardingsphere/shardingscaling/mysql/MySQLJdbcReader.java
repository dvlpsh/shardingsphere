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

import org.apache.shardingsphere.shardingscaling.core.config.JdbcDataSourceConfiguration;
import org.apache.shardingsphere.shardingscaling.core.config.RdbmsConfiguration;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.reader.AbstractJdbcReader;
import org.apache.shardingsphere.shardingscaling.core.metadata.JdbcUri;
import org.apache.shardingsphere.shardingscaling.core.util.DataSourceFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

/**
 * MySQL JDBC Reader.
 *
 * @author avalon566
 */
public final class MySQLJdbcReader extends AbstractJdbcReader {
    
    public MySQLJdbcReader(final RdbmsConfiguration rdbmsConfiguration, final DataSourceFactory dataSourceFactory) {
        super(rdbmsConfiguration, dataSourceFactory);
        JdbcDataSourceConfiguration jdbcDataSourceConfiguration = (JdbcDataSourceConfiguration) getRdbmsConfiguration().getDataSourceConfiguration();
        jdbcDataSourceConfiguration.setJdbcUrl(fixMysqlUrl(jdbcDataSourceConfiguration.getJdbcUrl()));
    }
    
    private String formatMysqlParams(final Map<String, String> params) {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append(entry.getKey());
            if (null != entry.getValue()) {
                result.append("=").append(entry.getValue());
            }
            result.append("&");
        }
        result.deleteCharAt(result.length() - 1);
        return result.toString();
    }
    
    private String fixMysqlUrl(final String url) {
        JdbcUri uri = new JdbcUri(url);
        return String.format("jdbc:%s://%s/%s?%s", uri.getScheme(), uri.getHost(), uri.getDatabase(), fixMysqlParams(uri.getParameters()));
    }
    
    private String fixMysqlParams(final Map<String, String> parameters) {
        if (!parameters.containsKey("yearIsDateType")) {
            parameters.put("yearIsDateType", "false");
        }
        return formatMysqlParams(parameters);
    }
    
    @Override
    public Object readValue(final ResultSet resultSet, final int index) throws SQLException {
        if (isDateTimeValue(resultSet.getMetaData().getColumnType(index))) {
            // fix: jdbc Time objects represent a wall-clock time and not a duration as MySQL treats them
            return resultSet.getString(index);
        } else {
            return resultSet.getObject(index);
        }
    }
    
    private boolean isDateTimeValue(final int columnType) {
        return Types.TIME == columnType
                || Types.DATE == columnType
                || Types.TIMESTAMP == columnType;
    }
}
