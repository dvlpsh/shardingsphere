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

package io.shardingsphere.spi.xa;


import io.shardingsphere.core.constant.DatabaseType;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class XABackendDataSourceFactoryTest {
    
    private XABackendDataSourceFactory xaBackendDataSourceFactory = XABackendDataSourceFactory.getInstance();
    
    private Map<String, DataSource> dataSourceMap = new HashMap<>();
    
    @Test
    public void createBackendDatasourceSuccess() {
        Map<String, DataSource> backendDatasourceMap = xaBackendDataSourceFactory.build(dataSourceMap, DatabaseType.MySQL);
        assertThat(backendDatasourceMap.size(), is(2));
    }
    
    @Test
    public void createBackendDatasourceFailed() {
    
    }
}
