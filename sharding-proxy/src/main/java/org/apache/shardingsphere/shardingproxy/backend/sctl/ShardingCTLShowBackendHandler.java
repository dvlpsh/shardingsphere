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

package org.apache.shardingsphere.shardingproxy.backend.sctl;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.merger.MergedResult;
import org.apache.shardingsphere.core.merger.dal.show.ShowShardingCTLMergedResult;
import org.apache.shardingsphere.shardingproxy.backend.AbstractBackendHandler;
import org.apache.shardingsphere.shardingproxy.backend.ResultPacket;
import org.apache.shardingsphere.shardingproxy.backend.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.transport.mysql.constant.ColumnType;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandResponsePackets;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.ColumnDefinition41Packet;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.FieldCountPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.QueryResponsePackets;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.EofPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.ErrPacket;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Sharding CTL show backend handler.
 *
 * @author zhaojun
 */
public final class ShardingCTLShowBackendHandler extends AbstractBackendHandler {
    
    private final String sql;
    
    private final BackendConnection backendConnection;
    
    private MergedResult mergedResult;
    
    private int currentSequenceId;
    
    private int columnCount;
    
    private final List<ColumnType> columnTypes = new LinkedList<>();
    
    public ShardingCTLShowBackendHandler(final String sql, final BackendConnection backendConnection) {
        this.sql = sql.toUpperCase().trim();
        this.backendConnection = backendConnection;
    }
    
    @Override
    protected CommandResponsePackets execute0() {
        Optional<ShardingCTLShowStatement> showStatement = new ShardingCTLShowParser(sql).doParse();
        if (!showStatement.isPresent()) {
            return new CommandResponsePackets(new ErrPacket(" please review your sctl format, should be sctl:show xxxx."));
        }
        switch (showStatement.get().getValue()) {
            case "TRANSACTION_TYPE":
                return createResponsePackets("TRANSACTION_TYPE", backendConnection.getTransactionType().name());
            case "CACHED_CONNECTIONS":
                return createResponsePackets("CACHED_CONNECTIONS", backendConnection.getConnectionSize());
            default:
                return new CommandResponsePackets(new ErrPacket(String.format(" could not support this sctl grammar [%s].", sql)));
        }
    }
    
    private CommandResponsePackets createResponsePackets(final String columnName, final Object... values) {
        mergedResult = new ShowShardingCTLMergedResult(Arrays.asList(values));
        int sequenceId = 0;
        FieldCountPacket fieldCountPacket = new FieldCountPacket(++sequenceId, 1);
        Collection<ColumnDefinition41Packet> columnDefinition41Packets = new ArrayList<>(1);
        columnDefinition41Packets.add(new ColumnDefinition41Packet(++sequenceId, "", "", "", columnName, "", 100, ColumnType.MYSQL_TYPE_VARCHAR, 0));
        QueryResponsePackets queryResponsePackets = new QueryResponsePackets(fieldCountPacket, columnDefinition41Packets, new EofPacket(++sequenceId));
        currentSequenceId = queryResponsePackets.getPackets().size();
        columnCount = queryResponsePackets.getColumnCount();
        columnTypes.addAll(queryResponsePackets.getColumnTypes());
        return queryResponsePackets;
    }
    
    @Override
    public boolean next() throws SQLException {
        return null != mergedResult && mergedResult.next();
    }
    
    @Override
    public ResultPacket getResultValue() throws SQLException {
        List<Object> data = new ArrayList<>(columnCount);
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            data.add(mergedResult.getValue(columnIndex, Object.class));
        }
        return new ResultPacket(++currentSequenceId, data, columnCount, columnTypes);
    }
}
