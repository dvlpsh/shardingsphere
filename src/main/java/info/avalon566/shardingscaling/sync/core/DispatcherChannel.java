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

package info.avalon566.shardingscaling.sync.core;

import info.avalon566.shardingscaling.sync.jdbc.DataRecord;
import lombok.var;

import java.util.HashMap;
import java.util.Map;

/**
 * @author avalon566
 */
public class DispatcherChannel implements Channel {

    private final int channelNumber;

    /**
     * 通道Id : 通道
     */
    private final HashMap<String, MemoryChannel> channels = new HashMap<>();

    /**
     * 线程Id : 通道Id
     */
    private final HashMap<String, String> channelAssignment = new HashMap<>();

    public DispatcherChannel(int channelNumber) {
        this.channelNumber = channelNumber;
        for (int i = 0; i < channelNumber; i++) {
            channels.put(Integer.toString(i), new MemoryChannel());
        }
    }

    @Override
    public void pushRecord(Record record) {
        if (FinishedRecord.class.equals(record.getClass())) {
            // 广播事件
            for (Map.Entry<String, MemoryChannel> entry : channels.entrySet()) {
                entry.getValue().pushRecord(record);
            }
        } else if (DataRecord.class.equals(record.getClass())) {
            // 表名哈希
            var dataRecord = (DataRecord) record;
            var index = Integer.toString(dataRecord.getTableName().hashCode() % channelNumber);
            channels.get(index).pushRecord(dataRecord);
        } else {
            throw new RuntimeException("Not Support Record Type");
        }
    }

    @Override
    public Record popRecord() {
        var threadId = Long.toString(Thread.currentThread().getId());
        checkAssignment(threadId);
        return channels.get(channelAssignment.get(threadId)).popRecord();
    }

    private void checkAssignment(String threadId) {
        if (!channelAssignment.containsKey(threadId)) {
            synchronized (this) {
                for (Map.Entry<String, MemoryChannel> entry : channels.entrySet()) {
                    if (!channelAssignment.containsValue(entry.getKey())) {
                        channelAssignment.put(threadId, entry.getKey());
                    }
                }
            }
        }
    }
}
