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

import io.shardingjdbc.core.constant.DatabaseType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Table metadata handler factory.
 *
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TableMetaHandlerFactory {
    
    /**
     * To generate table metadata handler by data type.
     *
     * @param dataSource data source.
     * @param actualTableName actual table name.
     * @return abstract table metadata handler.
     * @throws SQLException SQL exception.
     */
    public static AbstractTableMetaHandler newInstance(final DataSource dataSource, final String actualTableName) throws SQLException {
        DatabaseType databaseType = DatabaseType.valueFrom(dataSource.getConnection().getMetaData().getDatabaseProductName());
        switch (databaseType) {
            case MySQL:
                return new MySQLTableMetaHandler(dataSource, actualTableName);
            default:
                throw new UnsupportedOperationException(String.format("Cannot support database [%s].", databaseType));
        }
    }
}
