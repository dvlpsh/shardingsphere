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

package io.shardingsphere.shardingproxy.backend.jdbc.wrapper;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.parsing.SQLJudgeEngine;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.routing.PreparedStatementRoutingEngine;
import io.shardingsphere.core.routing.RouteUnit;
import io.shardingsphere.core.routing.SQLRouteResult;
import io.shardingsphere.core.routing.SQLUnit;
import io.shardingsphere.core.routing.router.masterslave.MasterSlaveRouter;
import io.shardingsphere.shardingproxy.config.GlobalRegistry;
import io.shardingsphere.shardingproxy.config.RuleInstance;
import io.shardingsphere.shardingproxy.rewrite.MasterSlaveSQLRewriteEngine;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;

/**
 * Executor wrapper for prepared statement.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class PreparedStatementExecutorWrapper implements JDBCExecutorWrapper {
    
    private static final GlobalRegistry GLOBAL_REGISTRY = GlobalRegistry.getInstance();
    
    private final RuleInstance ruleInstance;
    
    private final List<Object> parameters;
    
    @Override
    public SQLRouteResult route(final String sql, final DatabaseType databaseType) {
        return ruleInstance.isMasterSlaveOnly() ? doMasterSlaveRoute(sql) : doShardingRoute(sql, databaseType);
    }
    
    private SQLRouteResult doMasterSlaveRoute(final String sql) {
        SQLStatement sqlStatement = new SQLJudgeEngine(sql).judge();
        String rewriteSQL = new MasterSlaveSQLRewriteEngine(ruleInstance.getMasterSlaveRule(), sql, sqlStatement, ruleInstance.getMetaData()).rewrite();
        SQLRouteResult result = new SQLRouteResult(sqlStatement);
        for (String each : new MasterSlaveRouter(ruleInstance.getMasterSlaveRule(), GLOBAL_REGISTRY.isShowSQL()).route(rewriteSQL)) {
            result.getRouteUnits().add(new RouteUnit(each, new SQLUnit(rewriteSQL, Collections.<List<Object>>emptyList())));
        }
        return result;
    }
    
    private SQLRouteResult doShardingRoute(final String sql, final DatabaseType databaseType) {
        return new PreparedStatementRoutingEngine(
                sql, ruleInstance.getShardingRule(), ruleInstance.getMetaData().getTable(), databaseType, GLOBAL_REGISTRY.isShowSQL(), ruleInstance.getMetaData().getDataSource()).route(parameters);
    }
    
    @Override
    public Statement createStatement(final Connection connection, final String sql, final boolean isReturnGeneratedKeys) throws SQLException {
        PreparedStatement result = isReturnGeneratedKeys ? connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS) : connection.prepareStatement(sql);
        for (int i = 0; i < parameters.size(); i++) {
            result.setObject(i + 1, parameters.get(i));
        }
        return result;
    }
    
    @Override
    public boolean executeSQL(final Statement statement, final String sql, final boolean isReturnGeneratedKeys) throws SQLException {
        return ((PreparedStatement) statement).execute();
    }
}
