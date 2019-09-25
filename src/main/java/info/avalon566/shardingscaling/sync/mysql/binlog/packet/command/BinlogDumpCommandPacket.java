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

package info.avalon566.shardingscaling.sync.mysql.binlog.packet.command;

import com.google.common.base.Strings;
import info.avalon566.shardingscaling.sync.mysql.binlog.codec.DataTypesCodec;
import info.avalon566.shardingscaling.sync.mysql.binlog.packet.AbstractCommandPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.Data;
import lombok.var;

/**
 * MySQL binlog dump command packet.
 *
 * @author avalon566
 * @author yangyi
 */
@Data
public final class BinlogDumpCommandPacket extends AbstractCommandPacket {
    
    private static final int BINLOG_SEND_ANNOTATE_ROWS_EVENT = 2;
    
    private long binlogPosition;
    
    private int slaveServerId;
    
    private String binlogFileName;

    public BinlogDumpCommandPacket() {
        setCommand((byte) 0x12);
    }

    @Override
    public ByteBuf toByteBuf() {
        var result = ByteBufAllocator.DEFAULT.heapBuffer();
        DataTypesCodec.writeByte(getCommand(), result);
        DataTypesCodec.writeInt((int) binlogPosition, result);
        byte binlogFlags = 0;
        binlogFlags |= BINLOG_SEND_ANNOTATE_ROWS_EVENT;
        DataTypesCodec.writeByte(binlogFlags, result);
        DataTypesCodec.writeByte((byte) 0x00, result);
        DataTypesCodec.writeInt(this.slaveServerId, result);
        if (!Strings.isNullOrEmpty(this.binlogFileName)) {
            DataTypesCodec.writeBytes(this.binlogFileName.getBytes(), result);
        }
        return result;
    }
}
