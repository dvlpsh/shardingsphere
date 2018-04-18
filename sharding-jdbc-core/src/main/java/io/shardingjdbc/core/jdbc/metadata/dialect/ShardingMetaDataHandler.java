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

import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.core.metadata.ColumnMetaData;
import lombok.AccessLevel;
import lombok.Getter;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Abstract table meta handler.
 *
 * @author panjuan
 */
@Getter(AccessLevel.PROTECTED)
public abstract class ShardingMetaDataHandler {
    
    private final DataSource dataSource;
    
    private final String actualTableName;
    
    public ShardingMetaDataHandler(final DataSource dataSource, final String actualTableName) {
        this.dataSource = dataSource instanceof MasterSlaveDataSource
            ? ((MasterSlaveDataSource) dataSource).getMasterDataSource().values().iterator().next() : dataSource;
        this.actualTableName = actualTableName;
    }
    
    /**
     * Get column meta data list.
     *
     * @return column meta data list
     * @throws SQLException SQL exception
     */
    public abstract Collection<ColumnMetaData> getColumnMetaDataList() throws SQLException;
}
