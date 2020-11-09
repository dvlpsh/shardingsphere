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

package org.apache.shardingsphere.infra.metadata.schema.loader.addressing;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.schema.loader.physical.PhysicalSchemaMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.loader.spi.TableAddressingMapperDecorator;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPIRegistry;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Table addressing mapper loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TableAddressingMapperDataLoader {
    
    static {
        ShardingSphereServiceLoader.register(TableAddressingMapperDecorator.class);
    }
    
    /**
     * Load table addressing mapper with related data sources.
     *
     * @param databaseType database type
     * @param dataSourceMap data source map
     * @param rules ShardingSphere rules
     * @return table addressing mapper
     * @throws SQLException SQL exception
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Map<String, Collection<String>> load(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap, final Collection<ShardingSphereRule> rules) throws SQLException {
        Map<String, Collection<String>> result = initialize(databaseType, dataSourceMap);
        for (Entry<ShardingSphereRule, TableAddressingMapperDecorator> entry : OrderedSPIRegistry.getRegisteredServices(rules, TableAddressingMapperDecorator.class).entrySet()) {
            entry.getValue().decorate(entry.getKey(), result);
        }
        return result;
    }
    
    private static Map<String, Collection<String>> initialize(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap) throws SQLException {
        Map<String, Collection<String>> result = new HashMap<>();
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            append(result, databaseType, entry.getKey(), entry.getValue());
        }
        return result;
    }
    
    private static void append(final Map<String, Collection<String>> tableAddressingMapper, 
                               final DatabaseType databaseType, final String dataSourceName, final DataSource dataSource) throws SQLException {
        for (String each : PhysicalSchemaMetaDataLoader.loadAllTableNames(dataSource, databaseType)) {
            if (!tableAddressingMapper.containsKey(each)) {
                tableAddressingMapper.put(each, new LinkedHashSet<>());
            }
            tableAddressingMapper.get(each).add(dataSourceName);
        }
    }
}
