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

package org.apache.shardingsphere.shardingscaling.core.synctask;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.shardingscaling.core.config.SyncConfiguration;
import org.apache.shardingsphere.shardingscaling.core.controller.ReportCallback;
import org.apache.shardingsphere.shardingscaling.core.controller.SyncProgress;
import org.apache.shardingsphere.shardingscaling.core.execute.Event;
import org.apache.shardingsphere.shardingscaling.core.execute.EventType;
import org.apache.shardingsphere.shardingscaling.core.execute.engine.ExecuteCallback;
import org.apache.shardingsphere.shardingscaling.core.execute.engine.ExecuteUtil;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.channel.AckCallback;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.channel.RealtimeSyncChannel;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.log.LogManager;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.log.LogManagerFactory;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.reader.LogPosition;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.reader.Reader;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.reader.ReaderFactory;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.record.Record;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.writer.Writer;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.writer.WriterFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Realtime data execute task.
 *
 * @author avalon566
 */
@Slf4j
public class RealtimeDataSyncTask implements SyncTask {

    private final SyncConfiguration syncConfiguration;

    private LogManager logManager;

    private Reader reader;

    private LogPosition currentLogPosition;

    private RealtimeSyncChannel channel;

    public RealtimeDataSyncTask(final SyncConfiguration syncConfiguration) {
        this.syncConfiguration = syncConfiguration;
    }

    @Override
    public final void prepare() {
        this.logManager = LogManagerFactory.newInstanceLogManager(syncConfiguration.getReaderConfiguration());
        currentLogPosition = logManager.getCurrentPosition();
        this.reader = ReaderFactory.newInstanceLogReader(syncConfiguration.getReaderConfiguration(), currentLogPosition);
    }

    @Override
    public final void start(final ReportCallback callback) {
        final List<Writer> writers = new ArrayList<>(syncConfiguration.getConcurrency());
        for (int i = 0; i < syncConfiguration.getConcurrency(); i++) {
            writers.add(WriterFactory.newInstance(syncConfiguration.getWriterConfiguration()));
        }
        channel = new RealtimeSyncChannel(writers.size(), Collections.singletonList((AckCallback) new AckCallback() {
            @Override
            public void onAck(final List<Record> records) {
                Record record = records.get(records.size() - 1);
                currentLogPosition = record.getLogPosition();
            }
        }));
        ExecuteUtil.execute(channel, reader, writers, new ExecuteCallback() {

            @Override
            public void onSuccess() {
                log.info("realtime data execute finish");
                callback.onProcess(new Event(syncConfiguration.getTaskId(), EventType.FINISHED));
            }

            @Override
            public void onFailure(final Throwable throwable) {
                log.error("realtime data execute exception exit", throwable);
                callback.onProcess(new Event(syncConfiguration.getTaskId(), EventType.EXCEPTION_EXIT));
            }
        });
    }

    @Override
    public final void stop() {
        reader.stop();
    }

    @Override
    public final SyncProgress getProgress() {
        return new SyncProgress() {
        };
    }
}
