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

package io.shardingsphere.proxy.backend.jdbc.transaction;

import com.google.common.base.Optional;
import io.shardingsphere.core.constant.TCLType;
import io.shardingsphere.core.constant.TransactionType;
import io.shardingsphere.core.util.EventBusInstance;
import io.shardingsphere.transaction.manager.ShardingTransactionManagerRegistry;
import io.shardingsphere.transaction.common.TransactionTypeHolder;
import io.shardingsphere.transaction.common.event.XaTransactionEvent;

import javax.transaction.Status;
import java.sql.SQLException;

/**
 * Execute XA transaction intercept.
 *
 * @author zhaojun
 */
public final class XaTransactionEngine extends TransactionEngine {
    
    public XaTransactionEngine(final String sql) {
        super(sql);
    }
    
    @Override
    public boolean execute() throws SQLException {
        Optional<TCLType> tclType = parseSQL();
        if (tclType.isPresent() && isInTransaction(tclType.get())) {
            TransactionTypeHolder.set(TransactionType.XA);
            EventBusInstance.getInstance().post(new XaTransactionEvent(tclType.get(), getSql()));
            return true;
        }
        return false;
    }
    
    private boolean isInTransaction(final TCLType tclType) throws SQLException {
        return TCLType.ROLLBACK != tclType || Status.STATUS_NO_TRANSACTION != ShardingTransactionManagerRegistry.getInstance().getShardingTransactionManager(TransactionType.XA).getStatus();
    }
}
