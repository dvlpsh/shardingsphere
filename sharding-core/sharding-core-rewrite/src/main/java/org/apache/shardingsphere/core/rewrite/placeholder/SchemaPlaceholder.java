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

package org.apache.shardingsphere.core.rewrite.placeholder;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.metadata.datasource.ShardingDataSourceMetaData;
import org.apache.shardingsphere.core.parse.constant.QuoteCharacter;
import org.apache.shardingsphere.core.route.type.RoutingUnit;
import org.apache.shardingsphere.core.rule.BaseRule;

import java.util.Map;

/**
 * Schema placeholder for rewrite.
 *
 * @author zhangliang
 * @author panjuan
 */
@RequiredArgsConstructor
@Getter
public final class SchemaPlaceholder implements ShardingPlaceholder, Alterable {
    
    private final String logicSchemaName;
    
    private final String logicTableName;
    
    private final QuoteCharacter quoteCharacter;
    
    private final BaseRule baseRule;
    
    private final ShardingDataSourceMetaData dataSourceMetaData;
    
    @Override
    public String toString(final RoutingUnit routingUnit, final Map<String, String> logicAndActualTables) {
        String actualSchemaName = Strings.isNullOrEmpty(logicTableName) 
                ? logicSchemaName : dataSourceMetaData.getActualDataSourceMetaData(baseRule.getActualDataSourceName(logicAndActualTables.get(logicTableName))).getSchemaName();
        return quoteCharacter.getStartDelimiter() + actualSchemaName + quoteCharacter.getEndDelimiter();
    }
}
