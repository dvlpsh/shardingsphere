/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.transaction.common.listener;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import io.shardingsphere.core.util.EventBusInstance;
import io.shardingsphere.transaction.api.ShardingTransactionManager;
import io.shardingsphere.transaction.common.TransactionContextHolder;
import io.shardingsphere.transaction.common.event.TransactionEvent;

import java.sql.SQLException;

/**
 * Transaction Listener.
 *
 * @author zhaojun
 */
public final class TransactionListener {
    
    /**
     * Register transaction listener into event bus.
     */
    public void register() {
        EventBusInstance.getInstance().register(this);
    }
    
    /**
     * Listen event.
     *
     * @param transactionEvent transaction event
     * @throws SQLException SQL exception
     */
    @Subscribe
    @AllowConcurrentEvents
    public void listen(final TransactionEvent transactionEvent) throws SQLException {
        ShardingTransactionManager transactionManager = TransactionContextHolder.get().getTransactionManager();
        switch (transactionEvent.getTclType()) {
            case BEGIN:
                transactionManager.begin(transactionEvent);
                break;
            case COMMIT:
                transactionManager.commit(transactionEvent);
                break;
            case ROLLBACK:
                transactionManager.rollback(transactionEvent);
                break;
            default:
        }
    }
}
