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

package io.shardingsphere.core.executor.type.prepared;

import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.executor.BaseStatementUnit;
import io.shardingsphere.core.executor.ExecuteCallback;
import io.shardingsphere.core.executor.ExecutorEngine;
import io.shardingsphere.core.executor.JDBCExecuteCallback;
import io.shardingsphere.core.executor.threadlocal.ExecutorDataMap;
import io.shardingsphere.core.executor.threadlocal.ExecutorExceptionHandler;
import lombok.RequiredArgsConstructor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * PreparedStatement Executor for multiple threads.
 * 
 * @author zhangliang
 * @author caohao
 * @author maxiaoguang
 */
@RequiredArgsConstructor
public final class PreparedStatementExecutor {
    
    private final ExecutorEngine executorEngine;
    
    private final SQLType sqlType;
    
    private final Collection<PreparedStatementUnit> preparedStatementUnits;
    
    /**
     * Execute query.
     * 
     * @return result set list
     * @throws SQLException SQL exception
     */
    public List<ResultSet> executeQuery() throws SQLException {
        final boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        final Map<String, Object> dataMap = ExecutorDataMap.getDataMap();
        ExecuteCallback<ResultSet> executeCallback = new ExecuteCallback<>(sqlType, isExceptionThrown, dataMap, new JDBCExecuteCallback<ResultSet>() {
        
            @Override
            public ResultSet execute(final BaseStatementUnit baseStatementUnit) throws SQLException {
                return ((PreparedStatement) baseStatementUnit.getStatement()).executeQuery();
            }
        });
        return executorEngine.execute(preparedStatementUnits, executeCallback);
    }
    
    /**
     * Execute update.
     * 
     * @return effected records count
     * @throws SQLException SQL exception
     */
    public int executeUpdate() throws SQLException {
        final boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        final Map<String, Object> dataMap = ExecutorDataMap.getDataMap();
        ExecuteCallback<Integer> executeCallback = new ExecuteCallback<>(sqlType, isExceptionThrown, dataMap, new JDBCExecuteCallback<Integer>() {
        
            @Override
            public Integer execute(final BaseStatementUnit baseStatementUnit) throws SQLException {
                return ((PreparedStatement) baseStatementUnit.getStatement()).executeUpdate();
            }
        });
        List<Integer> results = executorEngine.execute(preparedStatementUnits, executeCallback);
        return accumulate(results);
    }
    
    private int accumulate(final List<Integer> results) {
        int result = 0;
        for (Integer each : results) {
            result += null == each ? 0 : each;
        }
        return result;
    }
    
    /**
     * Execute SQL.
     *
     * @return return true if is DQL, false if is DML
     * @throws SQLException SQL exception
     */
    public boolean execute() throws SQLException {
        boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        Map<String, Object> dataMap = ExecutorDataMap.getDataMap();
        ExecuteCallback<Boolean> executeCallback = new ExecuteCallback<>(sqlType, isExceptionThrown, dataMap, new JDBCExecuteCallback<Boolean>() {
            
            @Override
            public Boolean execute(final BaseStatementUnit baseStatementUnit) throws SQLException {
                return ((PreparedStatement) baseStatementUnit.getStatement()).execute();
            }
        });
        List<Boolean> result = executorEngine.execute(preparedStatementUnits, executeCallback);
        if (null == result || result.isEmpty() || null == result.get(0)) {
            return false;
        }
        return result.get(0);
    }
}
