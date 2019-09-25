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

package info.avalon566.shardingscaling.sync.mysql.binlog.packet.response;

import info.avalon566.shardingscaling.sync.mysql.binlog.codec.DataTypesCodec;
import info.avalon566.shardingscaling.sync.mysql.binlog.packet.AbstractPacket;
import io.netty.buffer.ByteBuf;
import lombok.Data;

/**
 * MySQL field packet.
 *
 * <p>
 *     MySQL Internals Manual  /  MySQL Client/Server Protocol  /  Text Protocol  /  COM_QUERY  /  COM_QUERY Response
 *     https://dev.mysql.com/doc/internals/en/com-query-response.html#packet-Protocol::ColumnDefinition41
 * </p>
 *
 * @author avalon566
 * @author yangyi
 */
@Data
public final class FieldPacket extends AbstractPacket {
    
    private String catalog;
    
    private String db;
    
    private String table;
    
    private String originalTable;
    
    private String name;
    
    private String originalName;
    
    private int character;
    
    private long length;
    
    private byte type;
    
    private int flags;
    
    private byte decimals;
    
    private String definition;

    @Override
    public void fromByteBuf(final ByteBuf data) {
        catalog = DataTypesCodec.readLengthCodedString(data);
        db = DataTypesCodec.readLengthCodedString(data);
        table = DataTypesCodec.readLengthCodedString(data);
        originalTable = DataTypesCodec.readLengthCodedString(data);
        name = DataTypesCodec.readLengthCodedString(data);
        originalName = DataTypesCodec.readLengthCodedString(data);
        character = DataTypesCodec.readShort(data);
        length = DataTypesCodec.readInt(data);
        type = DataTypesCodec.readByte(data);
        flags = DataTypesCodec.readShort(data);
        decimals = DataTypesCodec.readByte(data);
        // fill
        data.readerIndex(data.readerIndex() + 2);
        definition = DataTypesCodec.readLengthCodedString(data);
    }
}
