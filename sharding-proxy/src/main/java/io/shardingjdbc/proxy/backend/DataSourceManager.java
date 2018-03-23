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

package io.shardingjdbc.proxy.backend;

import io.shardingjdbc.core.api.ShardingDataSourceFactory;
import io.shardingjdbc.core.exception.ShardingJdbcException;
import lombok.Getter;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Data source manager.
 *
 * @author zhangliang
 */
@Getter
public final class DataSourceManager {
    
    @Getter
    private static DataSourceManager instance = new DataSourceManager();
    
    private final DataSource dataSource;
    
    private DataSourceManager() {
        try {
            dataSource = ShardingDataSourceFactory.createDataSource(new File(DataSourceManager.class.getResource("/conf/sharding-config.yaml").getFile()));
        } catch (final IOException | SQLException ex) {
            throw new ShardingJdbcException(ex);
        }
    }
}
