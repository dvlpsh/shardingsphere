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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.connection;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Connection status handler.
 */
@RequiredArgsConstructor
public final class ConnectionStatusHandler {
    
    private final AtomicReference<ConnectionStatus> status = new AtomicReference<>(ConnectionStatus.RELEASED);
    
    private final ResourceLock resourceLock;
    
    /**
     * Switch connection status to using.
     */
    public void switchUsingStatus() {
        status.set(ConnectionStatus.USING);
    }
    
    /**
     * Switch connection status to in transaction.
     */
    public void switchInTransactionStatus() {
        status.set(ConnectionStatus.IN_TRANSACTION);
    }
    
    /**
     * Judge whether connection is in transaction.
     *
     * @return whether connection is in transaction
     */
    public boolean isInTransaction() {
        return ConnectionStatus.IN_TRANSACTION == status.get();
    }
    
    /**
     * Notify connection to finish wait if necessary.
     */
    void doNotifyIfNecessary() {
        if (status.compareAndSet(ConnectionStatus.USING, ConnectionStatus.RELEASED)) {
            resourceLock.doNotify();
        }
    }
    
    /**
     * Wait until connection is released if necessary.
     */
    public void waitUntilConnectionReleasedIfNecessary() {
        if (ConnectionStatus.USING == status.get()) {
            while (!status.compareAndSet(ConnectionStatus.RELEASED, ConnectionStatus.USING)) {
                resourceLock.doAwait();
            }
        }
    }
}
