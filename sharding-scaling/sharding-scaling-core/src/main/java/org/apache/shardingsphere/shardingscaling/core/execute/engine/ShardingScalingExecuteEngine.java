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

package org.apache.shardingsphere.shardingscaling.core.execute.engine;

import org.apache.shardingsphere.shardingscaling.core.execute.executor.ShardingScalingExecutor;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Sharding scaling executor engine.
 */
public class ShardingScalingExecuteEngine {
    
    private final ListeningExecutorService executorService;
    
    public ShardingScalingExecuteEngine(final int maxWorkerNumber) {
        this.executorService = MoreExecutors.listeningDecorator(
            new ThreadPoolExecutor(maxWorkerNumber, maxWorkerNumber, 0, TimeUnit.SECONDS, new SynchronousQueue<>(), new ThreadPoolExecutor.AbortPolicy()));
    }
    
    /**
     * Submit a {@code ShardingScalingExecutor} without callback to execute.
     *
     * @param shardingScalingExecutor sharding scaling executor
     */
    public void submit(final ShardingScalingExecutor shardingScalingExecutor) {
        executorService.submit(shardingScalingExecutor);
    }
    
    /**
     * Submit a {@code ShardingScalingExecutor} with callback {@code ExecuteCallback} to execute.
     *
     * @param shardingScalingExecutor sharding scaling executor
     * @param executeCallback execute callback
     */
    public void submit(final ShardingScalingExecutor shardingScalingExecutor, final ExecuteCallback executeCallback) {
        ListenableFuture future = executorService.submit(shardingScalingExecutor);
        Futures.addCallback(future, new FutureCallback<Object>() {
            
            @Override
            public void onSuccess(final Object result) {
                executeCallback.onSuccess();
            }
    
            @Override
            public void onFailure(final Throwable t) {
                executeCallback.onFailure(t);
            }
        });
    }
}
