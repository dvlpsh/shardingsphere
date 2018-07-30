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

package io.shardingsphere.proxy.metadata;

import io.shardingsphere.core.metadata.table.ColumnMetaData;
import io.shardingsphere.core.metadata.table.TableMetaData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Sharding table meta data handler.
 *
 * @author panjuan
 */
@RequiredArgsConstructor
@Getter
public final class ShardingTableMetaDataHandler {
    
    private final DataSource dataSource;
    
    private final String actualTableName;
    
    /**
     * Get table meta data.
     *
     * @return table meta data
     * @throws SQLException SQL exception
     */
    public TableMetaData getTableMetaData() throws SQLException {
        try (Connection connection = getDataSource().getConnection();
             Statement statement = connection.createStatement()) {
            TableMetaData result = new TableMetaData();
            if (isTableExist(statement)) {
                result.getColumnMetaData().addAll(getExistColumnMeta(statement));
            }
            return result;
        }
    }
    
    private boolean isTableExist(final Statement statement) throws SQLException {
        statement.executeQuery(String.format("SHOW TABLES LIKE '%s'", getActualTableName()));
        try (ResultSet resultSet = statement.getResultSet()) {
            return resultSet.next();
        }
    }
    
    private List<ColumnMetaData> getExistColumnMeta(final Statement statement) throws SQLException {
        List<ColumnMetaData> result = new LinkedList<>();
        statement.executeQuery(String.format("DESC `%s`;", getActualTableName()));
        try (ResultSet resultSet = statement.getResultSet()) {
            while (resultSet.next()) {
                result.add(new ColumnMetaData(resultSet.getString("Field"), resultSet.getString("Type"), resultSet.getString("Key")));
            }
        }
        return result;
    }
    
    /**
     * Get table names from default data source.
     *
     * @return Table names from default data source
     * @throws SQLException SQL exception
     */
    public Collection<String> getTableNamesFromDefaultDataSource() throws SQLException {
        Collection<String> result = new LinkedList<>();
        try (Connection connection = getDataSource().getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeQuery("SHOW TABLES;");
            try (ResultSet resultSet = statement.getResultSet()) {
                while (resultSet.next()) {
                    result.add(resultSet.getString(1));
                }
            }
            return result;
        }
    }
}
