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

package io.shardingsphere.proxy.backend.common.jdbc.statement;

import io.shardingsphere.proxy.backend.common.jdbc.JDBCExecuteWorker;
import io.shardingsphere.proxy.backend.common.jdbc.JDBCResourceManager;
import io.shardingsphere.proxy.transport.mysql.constant.ColumnType;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Statement execute worker.
 *
 * @author zhangyonglun
 * @author zhaojun
 * @author zhangliang
 */
public final class JDBCStatementExecuteWorker extends JDBCExecuteWorker {
    
    private final PreparedStatement preparedStatement;
    
    public JDBCStatementExecuteWorker(final PreparedStatement preparedStatement, final boolean isReturnGeneratedKeys,
                                      final JDBCResourceManager jdbcResourceManager, final JDBCStatementBackendHandler jdbcStatementBackendHandler) {
        super(preparedStatement, isReturnGeneratedKeys, jdbcResourceManager, jdbcStatementBackendHandler);
        this.preparedStatement = preparedStatement;
    }
    
    @Override
    protected boolean executeSQL() throws SQLException {
        return preparedStatement.execute();
    }
    
    @Override
    protected void setColumnType(final ColumnType columnType) {
        ((JDBCStatementBackendHandler) getJdbcBackendHandler()).getColumnTypes().add(columnType);
    }
}
