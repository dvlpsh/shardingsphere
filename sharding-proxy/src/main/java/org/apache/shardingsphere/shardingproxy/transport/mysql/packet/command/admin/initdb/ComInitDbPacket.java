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

package org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.admin.initdb;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.shardingproxy.backend.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import org.apache.shardingsphere.shardingproxy.transport.mysql.constant.ServerErrorCode;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacketPayload;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandPacketType;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandResponsePackets;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.ErrPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.OKPacket;

/**
 * COM_INIT_DB command packet.
 *
 * @author zhangliang
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-init-db.html#packet-COM_INIT_DB">COM_INIT_DB</a>
 */
@Slf4j
public final class ComInitDbPacket implements CommandPacket {
    
    @Getter
    private final int sequenceId;
    
    private final String schema;
    
    private final BackendConnection backendConnection;
    
    public ComInitDbPacket(final int sequenceId, final MySQLPacketPayload payload, final BackendConnection backendConnection) {
        this.sequenceId = sequenceId;
        schema = payload.readStringEOF();
        this.backendConnection = backendConnection;
    }
    
    @Override
    public void write(final MySQLPacketPayload payload) {
        payload.writeInt1(CommandPacketType.COM_INIT_DB.getValue());
        payload.writeStringEOF(schema);
    }
    
    @Override
    public Optional<CommandResponsePackets> execute() {
        log.debug("Schema name received for Sharding-Proxy: {}", schema);
        if (GlobalRegistry.getInstance().schemaExists(schema)) {
            backendConnection.setCurrentSchema(schema);
            return Optional.of(new CommandResponsePackets(new OKPacket(getSequenceId() + 1)));
        }
        return Optional.of(new CommandResponsePackets(new ErrPacket(getSequenceId() + 1, ServerErrorCode.ER_BAD_DB_ERROR, schema)));
    }
}
