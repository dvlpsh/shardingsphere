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

package org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.execute;

import lombok.Getter;
import org.apache.shardingsphere.db.protocol.binary.BinaryResultSetRow;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLBinaryColumnType;
import org.apache.shardingsphere.db.protocol.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.execute.protocol.MySQLBinaryProtocolValueFactory;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Binary result set row packet for MySQL.
 * 
 * @see <a href="https://dev.mysql.com/doc/internals/en/binary-protocol-resultset-row.html">Binary Protocol Resultset Row</a>
 */
public final class MySQLBinaryResultSetRowPacket implements MySQLPacket {
    
    private static final int PACKET_HEADER = 0x00;
    
    private static final int NULL_BITMAP_OFFSET = 2;
    
    @Getter
    private final int sequenceId;
    
    private final Collection<BinaryResultSetRow> binaryRows;
    
    public MySQLBinaryResultSetRowPacket(final int sequenceId, final List<Object> data, final List<Integer> columnTypes) {
        this.sequenceId = sequenceId;
        binaryRows = getBinaryResultSetRows(columnTypes, data);
    }
    
    private Collection<BinaryResultSetRow> getBinaryResultSetRows(final List<Integer> columnTypes, final List<Object> data) {
        Collection<BinaryResultSetRow> result = new LinkedList<>();
        for (int i = 0; i < columnTypes.size(); i++) {
            result.add(new BinaryResultSetRow(MySQLBinaryColumnType.valueOfJDBCType(columnTypes.get(i)), data.get(i)));
        }
        return result;
    }
    
    @Override
    public void write(final MySQLPacketPayload payload) {
        payload.writeInt1(PACKET_HEADER);
        writeNullBitmap(payload);
        writeValues(payload);
    }
    
    private void writeNullBitmap(final MySQLPacketPayload payload) {
        for (int each : getNullBitmap().getNullBitmap()) {
            payload.writeInt1(each);
        }
    }
    
    private MySQLNullBitmap getNullBitmap() {
        MySQLNullBitmap result = new MySQLNullBitmap(binaryRows.size(), NULL_BITMAP_OFFSET);
        int index = 0;
        for (BinaryResultSetRow each : binaryRows) {
            if (null == each.getData()) {
                result.setNullBit(index);
            }
            index++;
        }
        return result;
    }
    
    private void writeValues(final MySQLPacketPayload payload) {
        for (BinaryResultSetRow each : binaryRows) {
            if (null != each.getData()) {
                MySQLBinaryProtocolValueFactory.getBinaryProtocolValue(each.getColumnType()).write(payload, each.getData());
            }
        }
    }
}
