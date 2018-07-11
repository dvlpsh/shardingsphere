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

package io.shardingsphere.core.property;

import io.shardingsphere.core.constant.DatabaseType;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Data source property manager.
 *
 * @author panjuan
 */
public class DataSourcePropertyManager {
    
    private final Map<String, DataSourceProperty> dataSourcePropertyMap;
    
    public DataSourcePropertyManager(final Map<String, DataSource> dataSourceMap, final DatabaseType databaseType) {
        dataSourcePropertyMap = initDataSourcePropertyMap(dataSourceMap, databaseType);
    }
    
    private Map<String, DataSourceProperty> initDataSourcePropertyMap(final Map<String, DataSource> dataSourceMap,
            final DatabaseType databaseType) {
        Map<String, DataSourceProperty> result = new LinkedHashMap<>();
        for (Map.Entry<String, DataSource> each : dataSourceMap.entrySet()) {
            result.put(each.getKey(), DataSourcePropertyFactory.createDataSourcePropertyParser(databaseType).parseDataSource(each.getValue()));
        }
        return result;
    }
    
    /**
     * Get all instance data source names.
     *
     * @return instance data source name list
     */
    public List<String> getAllInstanceDataSourceNames() {
        List<String> result = new LinkedList<>();
        for (Map.Entry<String, DataSourceProperty> each : dataSourcePropertyMap.entrySet()) {
            if (!isExisted(each.getKey(), result)) {
                result.add(each.getKey());
            }
        }
        return result;
    }
    
    private boolean isExisted(final String dataSourceName, final List<String> existedDataSourceNames) {
        for (String each : existedDataSourceNames) {
            if (dataSourcePropertyMap.get(each).isPointAtSameInstance(dataSourcePropertyMap.get(dataSourceName))) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get actual schema name.
     *
     * @param actualDataSourceName actual data source name
     * @return actual schema name
     */
    public String getActualSchemaName(final String actualDataSourceName) {
        return dataSourcePropertyMap.get(actualDataSourceName).getSchemeName();
    }
}
