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

package org.apache.shardingsphere.shardingproxy.backend;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import org.apache.shardingsphere.shardingproxy.backend.jdbc.JDBCBackendHandler;
import org.apache.shardingsphere.shardingproxy.backend.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.jdbc.execute.JDBCExecuteEngine;
import org.apache.shardingsphere.shardingproxy.backend.jdbc.wrapper.PreparedStatementExecutorWrapper;
import org.apache.shardingsphere.shardingproxy.backend.jdbc.wrapper.StatementExecutorWrapper;
import org.apache.shardingsphere.shardingproxy.backend.netty.NettyBackendHandler;
import org.apache.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import org.apache.shardingsphere.shardingproxy.runtime.schema.LogicSchema;

import java.util.List;

/**
 * Backend handler factory.
 *
 * @author zhangliang
 * @author panjuan
 * @author zhaojun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BackendHandlerFactory {
    
    private static final BackendHandlerFactory INSTANCE = new BackendHandlerFactory();
    
    private static final GlobalRegistry GLOBAL_REGISTRY = GlobalRegistry.getInstance();
    
    /**
     * Get backend handler factory instance.
     *
     * @return backend handler factory
     */
    public static BackendHandlerFactory getInstance() {
        return INSTANCE;
    }
    
    /**
     * Create new instance of text protocol backend handler.
     *
     * @param sequenceId sequence ID of SQL packet
     * @param sql SQL to be executed
     * @param backendConnection backend connection
     * @param databaseType database type
     * @return instance of text protocol backend handler
     */
    public BackendHandler newTextProtocolInstance(final int sequenceId, final String sql, final BackendConnection backendConnection, final DatabaseType databaseType) {
        LogicSchema logicSchema = backendConnection.getLogicSchema();
        return GLOBAL_REGISTRY.getShardingProperties().<Boolean>getValue(ShardingPropertiesConstant.PROXY_BACKEND_USE_NIO)
                ? new NettyBackendHandler(logicSchema, backendConnection.getConnectionId(), sequenceId, sql, databaseType)
                : new JDBCBackendHandler(logicSchema, sql, new JDBCExecuteEngine(backendConnection, new StatementExecutorWrapper(logicSchema)));
    }
    
    /**
     * Create new instance of text protocol backend handler.
     *
     * @param sequenceId sequence ID of SQL packet
     * @param sql SQL to be executed
     * @param parameters SQL parameters
     * @param backendConnection backend connection
     * @param databaseType database type
     * @return instance of text protocol backend handler
     */
    public BackendHandler newBinaryProtocolInstance(final int sequenceId, final String sql, final List<Object> parameters, 
                                                    final BackendConnection backendConnection, final DatabaseType databaseType) {
        LogicSchema logicSchema = backendConnection.getLogicSchema();
        return GLOBAL_REGISTRY.getShardingProperties().<Boolean>getValue(ShardingPropertiesConstant.PROXY_BACKEND_USE_NIO)
                ? new NettyBackendHandler(logicSchema, backendConnection.getConnectionId(), sequenceId, sql, databaseType)
                : new JDBCBackendHandler(logicSchema, sql, new JDBCExecuteEngine(backendConnection, new PreparedStatementExecutorWrapper(logicSchema, parameters)));
    }
}
