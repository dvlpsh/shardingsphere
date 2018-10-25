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

package io.shardingsphere.shardingjdbc.executor;

import io.shardingsphere.core.constant.ConnectionMode;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.constant.transaction.TransactionType;
import io.shardingsphere.core.executor.StatementExecuteUnit;
import io.shardingsphere.core.executor.sql.execute.SQLExecuteCallback;
import io.shardingsphere.core.routing.RouteUnit;
import io.shardingsphere.core.routing.SQLUnit;
import io.shardingsphere.shardingjdbc.transaction.TransactionTypeHolder;
import io.shardingsphere.transaction.manager.base.executor.SagaSQLExecuteCallback;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SQLExeucteCallbackFactoryTest {
    
    @Mock
    private PreparedStatement preparedStatement;
    
    @Mock
    private Connection connection;
    
    @Mock
    private DatabaseMetaData metaData;
    
    private StatementExecuteUnit unit;
    @Before
    public void setUp() throws SQLException {
        String dsName = "ds";
        String sql = "SELECT now()";
        when(preparedStatement.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getURL()).thenReturn("jdbc:mysql://localhost:3306/test");
        unit = new StatementExecuteUnit(new RouteUnit(dsName, new SQLUnit(sql, Collections.<List<Object>>emptyList())), preparedStatement, ConnectionMode.CONNECTION_STRICTLY);
    }
    
    @Test
    public void assertGetSagaSQLExecuteCallback() {
        TransactionTypeHolder.set(TransactionType.BASE);
        SQLExecuteCallback sqlExecuteCallback = SQLExecuteCallbackFactory.getPreparedUpdateSQLExecuteCallback(DatabaseType.MySQL ,SQLType.DML, false);
        assertThat(sqlExecuteCallback instanceof SagaSQLExecuteCallback, is(true));
        sqlExecuteCallback = SQLExecuteCallbackFactory.getPreparedSQLExecuteCallback(DatabaseType.MySQL, SQLType.DML, false);
        assertThat(sqlExecuteCallback instanceof SagaSQLExecuteCallback, is(true));
        TransactionTypeHolder.set(TransactionType.LOCAL);
    }
    
    @Test
    public void assertGetPreparedUpdateSQLExecuteCallback() throws SQLException {
        SQLExecuteCallback sqlExecuteCallback = SQLExecuteCallbackFactory.getPreparedUpdateSQLExecuteCallback(DatabaseType.MySQL, SQLType.DML, true);
        sqlExecuteCallback.execute(unit, true);
        verify(preparedStatement).executeUpdate();
    }
    
    @Test
    public void assertGetPreparedSQLExecuteCallback() throws SQLException {
        SQLExecuteCallback sqlExecuteCallback = SQLExecuteCallbackFactory.getPreparedSQLExecuteCallback(DatabaseType.MySQL, SQLType.DQL, true);
        sqlExecuteCallback.execute(unit, true);
        verify(preparedStatement).execute();
    }
}
