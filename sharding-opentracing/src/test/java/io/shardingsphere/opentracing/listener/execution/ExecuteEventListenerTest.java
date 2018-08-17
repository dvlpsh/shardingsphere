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

package io.shardingsphere.opentracing.listener.execution;

import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.executor.BaseStatementUnit;
import io.shardingsphere.core.executor.ExecuteCallback;
import io.shardingsphere.core.executor.ExecutorEngine;
import io.shardingsphere.core.executor.JDBCExecuteCallback;
import io.shardingsphere.core.executor.threadlocal.ExecutorDataMap;
import io.shardingsphere.core.executor.threadlocal.ExecutorExceptionHandler;
import io.shardingsphere.core.executor.type.batch.BatchPreparedStatementUnit;
import io.shardingsphere.core.executor.type.memory.MemoryStrictlyExecutorEngine;
import io.shardingsphere.core.executor.type.statement.StatementUnit;
import io.shardingsphere.core.routing.SQLExecutionUnit;
import io.shardingsphere.core.routing.SQLUnit;
import io.shardingsphere.opentracing.listener.BaseEventListenerTest;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ExecuteEventListenerTest extends BaseEventListenerTest {
    
    private final ExecutorEngine executorEngine = new MemoryStrictlyExecutorEngine(5);
    
    @Test
    public void assertSingleStatement() throws SQLException {
        Statement statement = mock(Statement.class);
        when(statement.getConnection()).thenReturn(mock(Connection.class));
        final boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        final Map<String, Object> dataMap = ExecutorDataMap.getDataMap();
        ExecuteCallback<Integer> executeCallback = new ExecuteCallback<>(SQLType.DML, isExceptionThrown, dataMap, new JDBCExecuteCallback<Integer>() {
        
            @Override
            public Integer execute(final BaseStatementUnit baseStatementUnit) {
                return 0;
            }
        });
        executorEngine.execute(Collections.singleton(
                new StatementUnit(new SQLExecutionUnit("ds_0", new SQLUnit("insert into ...", Collections.singletonList(Collections.<Object>singletonList(1)))), statement)), executeCallback);
        assertThat(getTracer().finishedSpans().size(), is(2));
    }
    
    @Test
    public void assertMultiStatement() throws SQLException {
        List<StatementUnit> statementUnitList = new ArrayList<>(2);
        Statement stm1 = mock(Statement.class);
        when(stm1.getConnection()).thenReturn(mock(Connection.class));
        statementUnitList.add(new StatementUnit(new SQLExecutionUnit("ds_0", new SQLUnit("insert into ...", Collections.singletonList(Collections.<Object>singletonList(1)))), stm1));
        Statement stm2 = mock(Statement.class);
        when(stm2.getConnection()).thenReturn(mock(Connection.class));
        statementUnitList.add(new StatementUnit(new SQLExecutionUnit("ds_0", new SQLUnit("insert into ...", Collections.singletonList(Collections.<Object>singletonList(1)))), stm2));
        final boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        final Map<String, Object> dataMap = ExecutorDataMap.getDataMap();
        ExecuteCallback<Integer> executeCallback = new ExecuteCallback<>(SQLType.DML, isExceptionThrown, dataMap, new JDBCExecuteCallback<Integer>() {
        
            @Override
            public Integer execute(final BaseStatementUnit baseStatementUnit) {
                return 0;
            }
        });
        executorEngine.execute(statementUnitList, executeCallback);
        assertThat(getTracer().finishedSpans().size(), is(3));
    }

    @Test
    public void assertBatchPreparedStatement() throws SQLException {
        List<BatchPreparedStatementUnit> statementUnitList = new ArrayList<>(2);
        List<List<Object>> parameterSets = Arrays.asList(Arrays.<Object>asList(1, 2), Arrays.<Object>asList(3, 4));
        PreparedStatement preparedStatement1 = mock(PreparedStatement.class);
        when(preparedStatement1.getConnection()).thenReturn(mock(Connection.class));
        statementUnitList.add(new BatchPreparedStatementUnit(new SQLExecutionUnit("ds_0", new SQLUnit("insert into ...", parameterSets)), preparedStatement1));
        PreparedStatement preparedStatement2 = mock(PreparedStatement.class);
        when(preparedStatement2.getConnection()).thenReturn(mock(Connection.class));
        statementUnitList.add(new BatchPreparedStatementUnit(new SQLExecutionUnit("ds_1", new SQLUnit("insert into ...", parameterSets)), preparedStatement2));
        final boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        final Map<String, Object> dataMap = ExecutorDataMap.getDataMap();
        ExecuteCallback<Integer> executeCallback = new ExecuteCallback<>(SQLType.DML, isExceptionThrown, dataMap, new JDBCExecuteCallback<Integer>() {
        
            @Override
            public Integer execute(final BaseStatementUnit baseStatementUnit) {
                return 0;
            }
        });
        executorEngine.execute(statementUnitList, executeCallback);
        assertThat(getTracer().finishedSpans().size(), is(3));
    }
    
    @Test(expected = SQLException.class)
    public void assertSQLException() throws SQLException {
        Statement statement = mock(Statement.class);
        when(statement.getConnection()).thenReturn(mock(Connection.class));
        final boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        final Map<String, Object> dataMap = ExecutorDataMap.getDataMap();
        ExecuteCallback<Integer> executeCallback = new ExecuteCallback<>(SQLType.DQL, isExceptionThrown, dataMap, new JDBCExecuteCallback<Integer>() {
        
            @Override
            public Integer execute(final BaseStatementUnit baseStatementUnit) throws SQLException {
                throw new SQLException();
            }
        });
        executorEngine.execute(Collections.singleton(
                new StatementUnit(new SQLExecutionUnit("ds_0", new SQLUnit("select ...", Collections.singletonList(Collections.<Object>singletonList(1)))), statement)), executeCallback);
    }
}
