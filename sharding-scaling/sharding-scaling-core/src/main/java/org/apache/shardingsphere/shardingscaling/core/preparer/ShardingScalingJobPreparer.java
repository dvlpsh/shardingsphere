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

package org.apache.shardingsphere.shardingscaling.core.preparer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.shardingscaling.core.ShardingScalingJob;
import org.apache.shardingsphere.shardingscaling.core.config.SyncConfiguration;
import org.apache.shardingsphere.shardingscaling.core.controller.task.SyncTaskControlStatus;
import org.apache.shardingsphere.shardingscaling.core.datasource.DataSourceManager;
import org.apache.shardingsphere.shardingscaling.core.exception.PrepareFailedException;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.position.LogPositionManager;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.position.LogPositionManagerFactory;
import org.apache.shardingsphere.shardingscaling.core.preparer.checker.DataSourceChecker;
import org.apache.shardingsphere.shardingscaling.core.preparer.checker.DataSourceCheckerCheckerFactory;
import org.apache.shardingsphere.shardingscaling.core.preparer.splitter.InventoryDataTaskSplitter;
import org.apache.shardingsphere.shardingscaling.core.synctask.DefaultSyncTaskFactory;
import org.apache.shardingsphere.shardingscaling.core.synctask.SyncTask;
import org.apache.shardingsphere.shardingscaling.core.synctask.SyncTaskFactory;

import javax.sql.DataSource;

/**
 * Sharding scaling job preparer.
 */
@Slf4j
public final class ShardingScalingJobPreparer {
    
    private final SyncTaskFactory syncTaskFactory = new DefaultSyncTaskFactory();
    
    private final InventoryDataTaskSplitter inventoryDataTaskSplitter = new InventoryDataTaskSplitter();
    
    /**
     * Do prepare work for sharding scaling job.
     *
     * @param shardingScalingJob sharding scaling job
     */
    public void prepare(final ShardingScalingJob shardingScalingJob) {
        String databaseType = shardingScalingJob.getSyncConfigurations().get(0).getDumperConfiguration().getDataSourceConfiguration().getDatabaseType().getName();
        try (DataSourceManager dataSourceManager = new DataSourceManager(shardingScalingJob.getSyncConfigurations())) {
            checkDatasources(databaseType, dataSourceManager);
            splitInventoryDataTasks(shardingScalingJob, dataSourceManager);
            initIncrementalDataTasks(databaseType, shardingScalingJob, dataSourceManager);
        } catch (PrepareFailedException ex) {
            log.warn("Preparing sharding scaling job {} : {} failed", shardingScalingJob.getJobId(), shardingScalingJob.getJobName(), ex);
            shardingScalingJob.setStatus(SyncTaskControlStatus.PREPARING_FAILURE.name());
        }
    }
    
    private void checkDatasources(final String databaseType, final DataSourceManager dataSourceManager) {
        DataSourceChecker dataSourceChecker = DataSourceCheckerCheckerFactory.newInstanceDataSourceChecker(databaseType);
        dataSourceChecker.checkConnection(dataSourceManager.getCachedDataSources().values());
        dataSourceChecker.checkPrivilege(dataSourceManager.getSourceDatasources().values());
    }
    
    private void splitInventoryDataTasks(final ShardingScalingJob shardingScalingJob, final DataSourceManager dataSourceManager) {
        List<SyncTask> allInventoryDataTasks = new LinkedList<>();
        for (SyncConfiguration each : shardingScalingJob.getSyncConfigurations()) {
            allInventoryDataTasks.addAll(inventoryDataTaskSplitter.splitInventoryData(each, dataSourceManager));
        }
        for (Collection<SyncTask> each : groupInventoryDataTasks(shardingScalingJob.getSyncConfigurations().get(0).getConcurrency(), allInventoryDataTasks)) {
            shardingScalingJob.getInventoryDataTasks().add(syncTaskFactory.createInventoryDataSyncTaskGroup(each));
        }
    }
    
    private List<Collection<SyncTask>> groupInventoryDataTasks(final int taskNumber, final List<SyncTask> allInventoryDataTasks) {
        List<Collection<SyncTask>> result = new ArrayList<>(taskNumber);
        for (int i = 0; i < taskNumber; i++) {
            result.add(new LinkedList<>());
        }
        for (int i = 0; i < allInventoryDataTasks.size(); i++) {
            result.get(i % taskNumber).add(allInventoryDataTasks.get(i));
        }
        return result;
    }
    
    private void initIncrementalDataTasks(final String databaseType, final ShardingScalingJob shardingScalingJob, final DataSourceManager dataSourceManager) {
        for (SyncConfiguration each : shardingScalingJob.getSyncConfigurations()) {
            LogPositionManager logPositionManager = instanceLogPositionManager(databaseType, dataSourceManager.getDataSource(each.getDumperConfiguration().getDataSourceConfiguration()));
            shardingScalingJob.getIncrementalDataTasks().add(syncTaskFactory.createIncrementalDataSyncTask(each, logPositionManager.getCurrentPosition()));
        }
    }
    
    private LogPositionManager instanceLogPositionManager(final String databaseType, final DataSource dataSource) {
        return LogPositionManagerFactory.newInstanceLogManager(databaseType, dataSource);
    }
}
