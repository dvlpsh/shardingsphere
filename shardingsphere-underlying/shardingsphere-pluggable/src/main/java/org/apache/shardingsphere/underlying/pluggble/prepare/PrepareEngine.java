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

package org.apache.shardingsphere.underlying.pluggble.prepare;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.underlying.common.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.underlying.common.rule.BaseRule;
import org.apache.shardingsphere.underlying.executor.context.ExecutionContext;
import org.apache.shardingsphere.underlying.executor.context.ExecutionUnit;
import org.apache.shardingsphere.underlying.executor.context.SQLUnit;
import org.apache.shardingsphere.underlying.executor.log.SQLLogger;
import org.apache.shardingsphere.underlying.rewrite.SQLRewriteEntry;
import org.apache.shardingsphere.underlying.rewrite.engine.SQLRewriteResult;
import org.apache.shardingsphere.underlying.route.context.RouteContext;
import org.apache.shardingsphere.underlying.route.context.RouteUnit;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Prepare engine.
 */
@RequiredArgsConstructor
public final class PrepareEngine {
    
    private final Collection<BaseRule> rules;
    
    private final ConfigurationProperties properties;
    
    private final ShardingSphereMetaData metaData;
    
    /**
     * Prepare to execute.
     *
     * @param sql SQL
     * @param parameters SQL parameters
     * @param routeContext route context
     * @return execution context
     */
    public ExecutionContext prepare(final String sql, final List<Object> parameters, final RouteContext routeContext) {
        ExecutionContext result = new ExecutionContext(routeContext.getSqlStatementContext());
        result.getExecutionUnits().addAll(rewrite(sql, parameters, routeContext));
        if (properties.<Boolean>getValue(ConfigurationPropertyKey.SQL_SHOW)) {
            SQLLogger.logSQL(sql, properties.<Boolean>getValue(ConfigurationPropertyKey.SQL_SIMPLE), result.getSqlStatementContext(), result.getExecutionUnits());
        }
        return result;
    }
    
    private Collection<ExecutionUnit> rewrite(final String sql, final List<Object> parameters, final RouteContext routeContext) {
        Map<RouteUnit, SQLRewriteResult> sqlRewriteResults = new SQLRewriteEntry(metaData.getSchema().getConfiguredSchemaMetaData(), properties, rules).rewrite(sql, parameters, routeContext);
        return routeContext.getRouteResult().getRouteUnits().isEmpty() ? getExecutionUnit(sqlRewriteResults.values().iterator().next()) : getExecutionUnit(sqlRewriteResults);
    }
    
    private Collection<ExecutionUnit> getExecutionUnit(final SQLRewriteResult sqlRewriteResult) {
        String dataSourceName = metaData.getDataSources().getAllInstanceDataSourceNames().iterator().next();
        return Collections.singletonList(new ExecutionUnit(dataSourceName, new SQLUnit(sqlRewriteResult.getSql(), sqlRewriteResult.getParameters())));
    }
    
    private Collection<ExecutionUnit> getExecutionUnit(final Map<RouteUnit, SQLRewriteResult> sqlRewriteResults) {
        Collection<ExecutionUnit> result = new LinkedHashSet<>();
        for (Entry<RouteUnit, SQLRewriteResult> entry : sqlRewriteResults.entrySet()) {
            result.add(new ExecutionUnit(entry.getKey().getDataSourceMapper().getActualName(), new SQLUnit(entry.getValue().getSql(), entry.getValue().getParameters())));
        }
        return result;
    }
}
