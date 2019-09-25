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

package info.avalon566.shardingscaling.job.schedule.standalone;

import info.avalon566.shardingscaling.job.DatabaseSyncJob;
import info.avalon566.shardingscaling.job.TableSliceSyncJob;
import info.avalon566.shardingscaling.job.config.SyncConfiguration;
import info.avalon566.shardingscaling.job.config.SyncType;
import info.avalon566.shardingscaling.job.schedule.Reporter;
import info.avalon566.shardingscaling.job.schedule.Scheduler;
import lombok.var;

import java.util.List;

/**
 * @author avalon566
 */
public class InProcessScheduler implements Scheduler {

    @Override
    public Reporter schedule(List<SyncConfiguration> syncConfigurations) {
        var reporter = new InProcessReporter();
        for (SyncConfiguration syncConfiguration : syncConfigurations) {
            if (SyncType.Database.equals(syncConfiguration.getSyncType())) {
                new DatabaseSyncJob(syncConfiguration).run();
            } else if (SyncType.TableSlice.equals(syncConfiguration.getSyncType())) {
                new TableSliceSyncJob(syncConfiguration, reporter).run();
            }
        }
        return reporter;
    }
}
