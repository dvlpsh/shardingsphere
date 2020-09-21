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

package org.apache.shardingsphere.proxy.backend.kernel;

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.sql.LogicSQLContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContextBuilder;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.rewrite.SQLRewriteEntry;
import org.apache.shardingsphere.infra.rewrite.engine.result.SQLRewriteResult;
import org.apache.shardingsphere.infra.route.DataNodeRouter;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteResult;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.sql.parser.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;

import java.util.Collection;

/**
 * Proxy kernel processor.
 */
public final class ProxyKernelProcessor {
    
    /**
     * Generate execution context.
     *
     * @param logicSQLContext logic SQL context
     * @param props configuration properties
     * @return execution context
     */
    public ExecutionContext generateExecutionContext(final LogicSQLContext logicSQLContext, final ConfigurationProperties props) {
        Collection<ShardingSphereRule> rules = logicSQLContext.getSchemaContext().getSchema().getRules();
        if (rules.isEmpty()) {
            return createDefaultExecutionContext(logicSQLContext);
        }
        DataNodeRouter router = new DataNodeRouter(logicSQLContext.getSchemaContext().getSchema().getMetaData(), props, rules);
        RouteContext routeContext = router.route(logicSQLContext.getSqlStatement(), logicSQLContext.getSql(), logicSQLContext.getParameters());
        SQLRewriteEntry rewriteEntry = new SQLRewriteEntry(logicSQLContext.getSchemaContext().getSchema().getMetaData().getRuleSchemaMetaData().getConfiguredSchemaMetaData(), props, rules);
        SQLRewriteResult sqlRewriteResult = rewriteEntry.rewrite(logicSQLContext.getSql(), logicSQLContext.getParameters(), routeContext);
        SQLStatementContext<?> sqlStatementContext = routeContext.getSqlStatementContext();
        Collection<ExecutionUnit> executionUnits = ExecutionContextBuilder.build(logicSQLContext.getSchemaContext().getSchema().getMetaData(), sqlRewriteResult, sqlStatementContext);
        return new ExecutionContext(sqlStatementContext, executionUnits, routeContext);
    }
    
    @SuppressWarnings("unchecked")
    private ExecutionContext createDefaultExecutionContext(final LogicSQLContext logicSQLContext) {
        String dataSourceName = logicSQLContext.getSchemaContext().getSchema().getDataSources().isEmpty()
                ? "" : logicSQLContext.getSchemaContext().getSchema().getDataSources().keySet().iterator().next();
        SQLStatementContext<?> sqlStatementContext = new CommonSQLStatementContext(logicSQLContext.getSqlStatement());
        ExecutionUnit executionUnit = new ExecutionUnit(dataSourceName, new SQLUnit(logicSQLContext.getSql(), logicSQLContext.getParameters()));
        RouteContext routeContext = new RouteContext(sqlStatementContext, logicSQLContext.getParameters(), new RouteResult());
        return new ExecutionContext(sqlStatementContext, executionUnit, routeContext);
    }
}
