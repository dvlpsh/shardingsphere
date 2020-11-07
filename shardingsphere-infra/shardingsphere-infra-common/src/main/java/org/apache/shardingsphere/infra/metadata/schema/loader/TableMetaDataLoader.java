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

package org.apache.shardingsphere.infra.metadata.schema.loader;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNodes;
import org.apache.shardingsphere.infra.metadata.schema.loader.spi.ShardingSphereMetaDataDecorator;
import org.apache.shardingsphere.infra.metadata.schema.loader.spi.ShardingSphereMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.model.physical.PhysicalTableMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPIRegistry;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Table meta data loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TableMetaDataLoader {
    
    static {
        ShardingSphereServiceLoader.register(ShardingSphereMetaDataLoader.class);
        ShardingSphereServiceLoader.register(ShardingSphereMetaDataDecorator.class);
    }
    
    /**
     * Load table meta data.
     *
     * @param tableName table name
     * @param databaseType database type
     * @param dataSourceMap data source map
     * @param rules ShardingSphere rules
     * @param props configuration properties
     * @return table meta data
     * @throws SQLException SQL exception
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Optional<PhysicalTableMetaData> load(final String tableName, final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap, 
                                                       final Collection<ShardingSphereRule> rules, final ConfigurationProperties props) throws SQLException {
        for (Entry<ShardingSphereRule, ShardingSphereMetaDataLoader> entry : OrderedSPIRegistry.getRegisteredServices(rules, ShardingSphereMetaDataLoader.class).entrySet()) {
            Optional<PhysicalTableMetaData> result = entry.getValue().load(tableName, databaseType, dataSourceMap, new DataNodes(rules), entry.getKey(), props);
            if (result.isPresent()) {
                return Optional.of(decorate(tableName, result.get(), rules));
            }
        }
        return Optional.empty();
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static PhysicalTableMetaData decorate(final String tableName, final PhysicalTableMetaData tableMetaData, final Collection<ShardingSphereRule> rules) {
        Map<ShardingSphereRule, ShardingSphereMetaDataDecorator> decorators = OrderedSPIRegistry.getRegisteredServices(rules, ShardingSphereMetaDataDecorator.class);
        PhysicalTableMetaData result = null;
        for (Entry<ShardingSphereRule, ShardingSphereMetaDataDecorator> entry : decorators.entrySet()) {
            result = entry.getValue().decorate(tableName, null == result ? tableMetaData : result, entry.getKey());
        }
        return Optional.ofNullable(result).orElse(tableMetaData);
    }
}
