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

package info.avalon566.shardingscaling.sync.mysql.binlog.packet.auth;

import info.avalon566.shardingscaling.sync.mysql.binlog.codec.DataTypesCodec;
import info.avalon566.shardingscaling.sync.mysql.binlog.packet.AbstractPacket;
import io.netty.buffer.ByteBuf;
import lombok.Data;

/**
 * MySQL handshake initializetion packet.
 *
 * <p>
 *     https://github.com/mysql/mysql-server/blob/5.7/sql/auth/sql_authentication.cc
 *     Bytes       Content
 *     -----       ----
 *     1           protocol version (always 10)
 *     n           server version string, \0-terminated
 *     4           thread id
 *     8           first 8 bytes of the plugin provided data (scramble)
 *     1           \0 byte, terminating the first part of a scramble
 *     2           server capabilities (two lower bytes)
 *     1           server character set
 *     2           server status
 *     2           server capabilities (two upper bytes)
 *     1           length of the scramble
 *     10          reserved, always 0
 *     n           rest of the plugin provided data (at least 12 bytes)
 *     1           \0 byte, terminating the second part of a scramble
 * </p>
 *
 * @author avalon566
 * @author yangyi
 */
@Data
public final class HandshakeInitializationPacket extends AbstractPacket {
    
    private short protocolVersion = 0x0a;
    
    private String serverVersion;
    
    private long threadId;
    
    private byte[] scramble;
    
    private int serverCapabilities;
    
    private short serverCharsetSet;
    
    private int serverStatus;
    
    private int serverCapabilities2;
    
    private byte[] restOfScramble;
    
    private String authPluginName;

    @Override
    public void fromByteBuf(final ByteBuf data) {
        protocolVersion = DataTypesCodec.readUnsignedInt1(data);
        serverVersion = DataTypesCodec.readNulTerminatedString(data);
        threadId = DataTypesCodec.readUnsignedInt4LE(data);
        scramble = DataTypesCodec.readBytes(8, data);
        DataTypesCodec.readNul(data);
        serverCapabilities = DataTypesCodec.readUnsignedInt2LE(data);
        if (data.isReadable()) {
            serverCharsetSet = DataTypesCodec.readUnsignedInt1(data);
            serverStatus = DataTypesCodec.readUnsignedInt2LE(data);
            serverCapabilities2 = DataTypesCodec.readUnsignedInt2LE(data);
            int capabilities = (serverCapabilities2 << 16) | serverCapabilities;
            DataTypesCodec.readUnsignedInt1(data);
            DataTypesCodec.readBytes(10, data);
            if ((capabilities & 0x00008000) != 0) {
                restOfScramble = DataTypesCodec.readBytes(12, data);
            }
            DataTypesCodec.readNul(data);
            if ((capabilities & 0x00080000) != 0) {
                authPluginName = DataTypesCodec.readNulTerminatedString(data);
            }
        }
    }
}
