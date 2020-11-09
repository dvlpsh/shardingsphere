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

package org.apache.shardingsphere.infra.metadata.schema.refresh;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import org.apache.shardingsphere.infra.metadata.schema.model.physical.PhysicalColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.physical.PhysicalIndexMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.physical.PhysicalSchemaMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.physical.PhysicalTableMetaData;
import org.junit.Before;

import java.util.Collections;

@Getter
public abstract class AbstractMetaDataRefreshStrategyTest {
    
    private PhysicalSchemaMetaData schema;
    
    @Before
    public void setUp() {
        schema = buildSchema();
    }
    
    private PhysicalSchemaMetaData buildSchema() {
        return new PhysicalSchemaMetaData(ImmutableMap.of("t_order", new PhysicalTableMetaData(
                Collections.singletonList(new PhysicalColumnMetaData("order_id", 1, "String", false, false, false)), Collections.singletonList(new PhysicalIndexMetaData("index")))));
    }
}

