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

package io.shardingsphere.proxy.backend.common.jdbc.statement;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.merger.QueryResult;
import io.shardingsphere.core.routing.PreparedStatementRoutingEngine;
import io.shardingsphere.core.routing.SQLRouteResult;
import io.shardingsphere.proxy.backend.common.ProxyMode;
import io.shardingsphere.proxy.backend.common.jdbc.JDBCBackendHandler;
import io.shardingsphere.proxy.backend.mysql.MySQLPacketStatementExecuteQueryResult;
import io.shardingsphere.proxy.config.RuleRegistry;
import io.shardingsphere.proxy.transport.common.packet.DatabaseProtocolPacket;
import io.shardingsphere.proxy.transport.mysql.constant.ColumnType;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.proxy.transport.mysql.packet.command.statement.PreparedStatementRegistry;
import io.shardingsphere.proxy.transport.mysql.packet.command.statement.execute.BinaryResultSetRowPacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.statement.execute.PreparedStatementParameter;
import lombok.Getter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Statement protocol backend handler via JDBC to connect databases.
 *
 * @author zhangyonglun
 * @author zhaojun
 */
public final class JDBCStatementBackendHandler extends JDBCBackendHandler {
    
    private final List<PreparedStatementParameter> preparedStatementParameters;
    
    private final DatabaseType databaseType;
    
    @Getter
    private final List<ColumnType> columnTypes;
    
    private final RuleRegistry ruleRegistry;
    
    public JDBCStatementBackendHandler(final List<PreparedStatementParameter> preparedStatementParameters, final int statementId, final DatabaseType databaseType) {
        super(PreparedStatementRegistry.getInstance().getSQL(statementId));
        this.preparedStatementParameters = preparedStatementParameters;
        this.databaseType = databaseType;
        columnTypes = new CopyOnWriteArrayList<>();
        ruleRegistry = RuleRegistry.getInstance();
    }
    
    @Override
    protected SQLRouteResult doShardingRoute() {
        PreparedStatementRoutingEngine routingEngine = new PreparedStatementRoutingEngine(
                getSql(), ruleRegistry.getShardingRule(), ruleRegistry.getShardingMetaData(), databaseType, ruleRegistry.isShowSQL(), ruleRegistry.getShardingDataSourceMetaData());
        return routingEngine.route(getComStmtExecuteParameters());
    }
    
    private List<Object> getComStmtExecuteParameters() {
        List<Object> result = new ArrayList<>(32);
        for (PreparedStatementParameter each : preparedStatementParameters) {
            result.add(each.getValue());
        }
        return result;
    }
    
    @Override
    protected Callable<CommandResponsePackets> createExecuteWorker(final Statement statement, final boolean isReturnGeneratedKeys, final String actualSQL) {
        return new JDBCStatementExecuteWorker((PreparedStatement) statement, isReturnGeneratedKeys, getJdbcResourceManager(), this);
    }
    
    @Override
    protected PreparedStatement createStatement(final Connection connection, final String actualSQL, final boolean isReturnGeneratedKeys) throws SQLException {
        PreparedStatement result = isReturnGeneratedKeys ? connection.prepareStatement(actualSQL, Statement.RETURN_GENERATED_KEYS) : connection.prepareStatement(actualSQL);
        for (int i = 0; i < preparedStatementParameters.size(); i++) {
            result.setObject(i + 1, preparedStatementParameters.get(i).getValue());
        }
        return result;
    }
    
    @Override
    protected QueryResult newQueryResult(final CommandResponsePackets packet, final int index) {
        MySQLPacketStatementExecuteQueryResult result = new MySQLPacketStatementExecuteQueryResult(packet, columnTypes);
        if (ProxyMode.MEMORY_STRICTLY == ruleRegistry.getProxyMode()) {
            result.setResultSet(getJdbcResourceManager().getResultSets().get(index));
        } else {
            result.setResultList(getResultLists().get(index));
        }
        return result;
    }
    
    @Override
    protected DatabaseProtocolPacket newDatabaseProtocolPacket(final int sequenceId, final List<Object> data) {
        return new BinaryResultSetRowPacket(sequenceId, getColumnCount(), data, columnTypes);
    }
}
