/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.proxy.transport.mysql.packet.command.query;

import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.proxy.backend.common.SQLExecuteBackendHandler;
import io.shardingjdbc.proxy.transport.common.packet.DatabaseProtocolPacket;
import io.shardingjdbc.proxy.transport.mysql.packet.MySQLPacketPayload;
import io.shardingjdbc.proxy.transport.mysql.packet.command.CommandPacket;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * COM_QUERY command packet.
 * @see <a href="https://dev.mysql.com/doc/internals/en/com-query.html">COM_QUERY</a>
 *
 * @author zhangliang
 */
@Slf4j
public final class ComQueryPacket extends CommandPacket {
    
    private String sql;
    
    @Override
    public ComQueryPacket read(final MySQLPacketPayload mysqlPacketPayload) {
        sql = mysqlPacketPayload.readStringEOF();
        log.debug("SQL received for Sharding-Proxy: {}", sql);
        return this;
    }
    
    @Override
    public List<DatabaseProtocolPacket> execute() {
        return new SQLExecuteBackendHandler(sql, DatabaseType.MySQL, true).execute();
    }
}
