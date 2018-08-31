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

package io.shardingsphere.transaction.manager.base.executor;

import com.google.common.eventbus.EventBus;
import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.constant.transaction.TransactionType;
import io.shardingsphere.core.event.ShardingEventBusInstance;
import io.shardingsphere.core.executor.sql.SQLExecuteCallback;
import io.shardingsphere.core.executor.sql.StatementExecuteUnit;
import io.shardingsphere.transaction.event.base.SagaSQLExecutionEvent;
import io.shardingsphere.transaction.manager.ShardingTransactionManagerRegistry;
import io.shardingsphere.transaction.manager.base.BASETransactionManager;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Saga transaction sql exeucte callback.
 *
 * @author yangyi
 */
public abstract class SagaSQLExeucteCallback<T> extends SQLExecuteCallback<T> {
    
    private final String transactionId;
    
    private final EventBus shardingEventBus = ShardingEventBusInstance.getInstance();
    
    public SagaSQLExeucteCallback(final SQLType sqlType, final boolean isExceptionThrown, final Map<String, Object> dataMap) {
        super(sqlType, isExceptionThrown, dataMap);
        this.transactionId = ((BASETransactionManager) ShardingTransactionManagerRegistry.getInstance().getShardingTransactionManager(TransactionType.BASE)).getTransactionId();
        shardingEventBus.post(new SagaSQLExecutionEvent(null, null, null, transactionId));
    }
    
    /**
     * Saga transaction don't execute sql immediately, but send event to listener.
     *
     * @param executeUnit exeucte unit
     * @return return true if T is Boolean, 0 if T is Integer
     * @throws SQLException sql exception
     */
    @Override
    protected T executeSQL(final StatementExecuteUnit executeUnit) throws SQLException {
        List<List<Object>> params = executeUnit.getSqlExecutionUnit().getSqlUnit().getParameterSets();
        for (List<Object> each : params) {
            SagaSQLExecutionEvent event = new SagaSQLExecutionEvent(executeUnit.getSqlExecutionUnit().getDataSource(), executeUnit.getSqlExecutionUnit().getSqlUnit(), each, transactionId);
            event.setExecuteSuccess();
            shardingEventBus.post(event);
        }
        return exeucteResult();
    }
    
    protected abstract T exeucteResult();
}
