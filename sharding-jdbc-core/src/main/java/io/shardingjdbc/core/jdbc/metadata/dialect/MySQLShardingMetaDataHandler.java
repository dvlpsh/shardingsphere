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

package io.shardingjdbc.core.jdbc.metadata.dialect;

import io.shardingjdbc.core.jdbc.metadata.ColumnMetaData;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * MySQL table metadata handler.
 *
 * @author panjuan
 */
public final class MySQLShardingMetaDataHandler extends ShardingMetaDataHandler {
    
    public MySQLShardingMetaDataHandler(final DataSource dataSource, final String actualTableName) {
        super(dataSource, actualTableName);
    }
    
    @Override
    public Collection<ColumnMetaData> getColumnMetaDataList() throws SQLException {
        List<ColumnMetaData> result = new LinkedList<>();
        try (Statement statement = getDataSource().getConnection().createStatement()) {
            statement.executeQuery(String.format("desc %s;", getActualTableName()));
            ResultSet resultSet = statement.getResultSet();
            while (resultSet.next()) {
                result.add(new ColumnMetaData(resultSet.getString("Field"), resultSet.getString("Type"), resultSet.getString("Key")));
            }
        }
        return result;
    }
}
