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

package org.apache.shardingsphere.shardingproxypg.transport.postgresql.packet.command.query.binary.bind;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.shardingproxypg.backend.ResultPacket;
import org.apache.shardingsphere.shardingproxypg.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.shardingproxypg.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.shardingproxypg.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxypg.runtime.GlobalRegistry;
import org.apache.shardingsphere.shardingproxypg.transport.common.packet.DatabasePacket;
import org.apache.shardingsphere.shardingproxypg.transport.postgresql.packet.PostgreSQLPacketPayload;
import org.apache.shardingsphere.shardingproxypg.transport.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.shardingproxypg.transport.postgresql.packet.command.PostgreSQLCommandResponsePackets;
import org.apache.shardingsphere.shardingproxypg.transport.postgresql.packet.command.query.PostgreSQLQueryCommandPacket;
import org.apache.shardingsphere.shardingproxypg.transport.postgresql.packet.command.query.binary.PostgreSQLBinaryStatement;
import org.apache.shardingsphere.shardingproxypg.transport.postgresql.packet.command.query.binary.bind.protocol.PostgreSQLBinaryProtocolValue;
import org.apache.shardingsphere.shardingproxypg.transport.postgresql.packet.command.query.binary.bind.protocol.PostgreSQLBinaryProtocolValueFactory;
import org.apache.shardingsphere.shardingproxypg.transport.postgresql.packet.generic.PostgreSQLErrorResponsePacket;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * PostgreSQL command bind packet.
 *
 * @author zhangyonglun
 */
@Slf4j
public final class PostgreSQLComBindPacket implements PostgreSQLQueryCommandPacket {
    
    @Getter
    private final char messageType = PostgreSQLCommandPacketType.BIND.getValue();
    
    private final String statementId;
    
    private final PostgreSQLBinaryStatement binaryStatement;
    
    private final DatabaseCommunicationEngine databaseCommunicationEngine;
    
    public PostgreSQLComBindPacket(final PostgreSQLPacketPayload payload, final BackendConnection backendConnection) throws SQLException {
        payload.readInt4();
        payload.readStringNul();
        statementId = payload.readStringNul();
        for (int i = 0; i < payload.readInt2(); i++) {
            payload.readInt2();
        }
        binaryStatement = backendConnection.getPostgreSQLBinaryStatementRegistry().getBinaryStatement(statementId);
        databaseCommunicationEngine = DatabaseCommunicationEngineFactory.getInstance().newBinaryProtocolInstance(
            backendConnection.getLogicSchema(), 0, binaryStatement.getSql(), getParameters(payload), backendConnection, DatabaseType.PostgreSQL);
    }
    
    private List<Object> getParameters(final PostgreSQLPacketPayload payload) throws SQLException {
        int parametersCount = payload.readInt2();
        List<Object> result = new ArrayList<>(parametersCount);
        for (int parameterIndex = 0; parameterIndex < parametersCount; parameterIndex++) {
            payload.readInt4();
            PostgreSQLBinaryProtocolValue binaryProtocolValue = PostgreSQLBinaryProtocolValueFactory.getBinaryProtocolValue(binaryStatement.getParameterTypes().get(parameterIndex).getColumnType());
            result.add(binaryProtocolValue.read(payload));
        }
        return result;
    }
    
    @Override
    public void write(final PostgreSQLPacketPayload payload) {
    }
    
    @Override
    public Optional<PostgreSQLCommandResponsePackets> execute() {
        log.debug("PostgreSQLComBindPacket received for Sharding-Proxy: {}", statementId);
        if (GlobalRegistry.getInstance().isCircuitBreak()) {
            return Optional.of(new PostgreSQLCommandResponsePackets(new PostgreSQLErrorResponsePacket()));
        }
        return Optional.of(databaseCommunicationEngine.execute());
    }
    
    @Override
    public boolean next() throws SQLException {
        return databaseCommunicationEngine.next();
    }
    
    @Override
    public DatabasePacket getResultValue() throws SQLException {
        ResultPacket resultPacket = databaseCommunicationEngine.getResultValue();
        return new PostgreSQLBinaryResultSetRowPacket(resultPacket.getColumnCount(), resultPacket.getData(), resultPacket.getColumnTypes());
    }
    
    @Override
    public int getSequenceId() {
        return 0;
    }
}
