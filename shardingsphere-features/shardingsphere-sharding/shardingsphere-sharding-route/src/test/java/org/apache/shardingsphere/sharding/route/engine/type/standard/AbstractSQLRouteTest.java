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

package org.apache.shardingsphere.sharding.route.engine.type.standard;

import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.DatabaseAccessConfiguration;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.parser.sql.SQLStatementParserEngine;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.engine.SQLRouteEngine;
import org.apache.shardingsphere.infra.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.schema.model.addressing.TableAddressingMetaData;
import org.apache.shardingsphere.infra.schema.model.datasource.DataSourcesMetaData;
import org.apache.shardingsphere.infra.schema.model.schema.physical.model.column.PhysicalColumnMetaData;
import org.apache.shardingsphere.infra.schema.model.schema.physical.model.schema.PhysicalSchemaMetaData;
import org.apache.shardingsphere.infra.schema.model.schema.physical.model.table.PhysicalTableMetaData;
import org.apache.shardingsphere.sharding.route.engine.fixture.AbstractRoutingEngineTest;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public abstract class AbstractSQLRouteTest extends AbstractRoutingEngineTest {
    
    protected final RouteContext assertRoute(final String sql, final List<Object> parameters) {
        ShardingRule shardingRule = createAllShardingRule();
        TableAddressingMetaData tableAddressingMetaData = new TableAddressingMetaData();
        tableAddressingMetaData.getTableDataSourceNamesMapper().put("t_category", Collections.singletonList("single_db"));
        ShardingSphereSchema schema = new ShardingSphereSchema(buildDataSourceMetas(), tableAddressingMetaData, buildPhysicalSchemaMetaData());
        ConfigurationProperties props = new ConfigurationProperties(new Properties());
        SQLStatementParserEngine sqlStatementParserEngine = new SQLStatementParserEngine("MySQL");
        SQLStatementContext<?> sqlStatementContext = SQLStatementContextFactory.newInstance(schema.getSchemaMetaData(), parameters, sqlStatementParserEngine.parse(sql, false));
        LogicSQL logicSQL = new LogicSQL(sqlStatementContext, sql, parameters);
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(
                "sharding_db", Collections.emptyList(), Collections.singleton(shardingRule), mock(ShardingSphereResource.class, RETURNS_DEEP_STUBS), schema);
        RouteContext result = new SQLRouteEngine(Collections.singletonList(shardingRule), props).route(logicSQL, metaData);
        assertThat(result.getRouteUnits().size(), is(1));
        return result;
    }
    
    private DataSourcesMetaData buildDataSourceMetas() {
        Map<String, DatabaseAccessConfiguration> dataSourceInfoMap = new HashMap<>(3, 1);
        DatabaseAccessConfiguration mainDatabaseAccessConfig = new DatabaseAccessConfiguration("jdbc:mysql://127.0.0.1:3306/actual_db", "test");
        DatabaseAccessConfiguration databaseAccessConfiguration0 = new DatabaseAccessConfiguration("jdbc:mysql://127.0.0.1:3306/actual_db", "test");
        DatabaseAccessConfiguration databaseAccessConfiguration1 = new DatabaseAccessConfiguration("jdbc:mysql://127.0.0.1:3306/actual_db", "test");
        dataSourceInfoMap.put("main", mainDatabaseAccessConfig);
        dataSourceInfoMap.put("ds_0", databaseAccessConfiguration0);
        dataSourceInfoMap.put("ds_1", databaseAccessConfiguration1);
        return new DataSourcesMetaData(DatabaseTypeRegistry.getActualDatabaseType("MySQL"), dataSourceInfoMap);
    }
    
    private PhysicalSchemaMetaData buildPhysicalSchemaMetaData() {
        Map<String, PhysicalTableMetaData> tableMetaDataMap = new HashMap<>(3, 1);
        tableMetaDataMap.put("t_order", new PhysicalTableMetaData(Arrays.asList(new PhysicalColumnMetaData("order_id", Types.INTEGER, "int", true, false, false),
                new PhysicalColumnMetaData("user_id", Types.INTEGER, "int", false, false, false),
                new PhysicalColumnMetaData("status", Types.INTEGER, "int", false, false, false)), Collections.emptySet()));
        tableMetaDataMap.put("t_order_item", new PhysicalTableMetaData(Arrays.asList(new PhysicalColumnMetaData("item_id", Types.INTEGER, "int", true, false, false),
                new PhysicalColumnMetaData("order_id", Types.INTEGER, "int", false, false, false),
                new PhysicalColumnMetaData("user_id", Types.INTEGER, "int", false, false, false),
                new PhysicalColumnMetaData("status", Types.VARCHAR, "varchar", false, false, false),
                new PhysicalColumnMetaData("c_date", Types.TIMESTAMP, "timestamp", false, false, false)), Collections.emptySet()));
        tableMetaDataMap.put("t_other", new PhysicalTableMetaData(Collections.singletonList(new PhysicalColumnMetaData("order_id", Types.INTEGER, "int", true, false, false)), Collections.emptySet()));
        return new PhysicalSchemaMetaData(tableMetaDataMap);
    }
}
