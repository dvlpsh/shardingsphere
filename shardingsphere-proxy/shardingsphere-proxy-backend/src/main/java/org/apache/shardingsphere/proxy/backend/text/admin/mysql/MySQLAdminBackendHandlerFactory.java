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

package org.apache.shardingsphere.proxy.backend.text.admin.mysql;

import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.text.admin.DatabaseAdminBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.admin.DatabaseAdminBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.text.admin.mysql.handler.ShowCurrentDatabaseBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.admin.mysql.handler.ShowDatabasesBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.admin.mysql.handler.ShowTablesBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.admin.mysql.handler.UseDatabaseBackendHandler;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowTablesStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLUseStatement;

import java.util.Optional;

/**
 * MySQL admin backend handler factory.
 */
public final class MySQLAdminBackendHandlerFactory implements DatabaseAdminBackendHandlerFactory {
    
    @Override
    public Optional<DatabaseAdminBackendHandler> newInstance(final SQLStatement sqlStatement, final BackendConnection backendConnection) {
        if (sqlStatement instanceof MySQLUseStatement) {
            return Optional.of(new UseDatabaseBackendHandler((MySQLUseStatement) sqlStatement, backendConnection));
        }
        if (sqlStatement instanceof MySQLShowDatabasesStatement) {
            return Optional.of(new ShowDatabasesBackendHandler(backendConnection));
        }
        if (sqlStatement instanceof MySQLShowTablesStatement) {
            return Optional.of(new ShowTablesBackendHandler(backendConnection));
        }
        if (sqlStatement instanceof SelectStatement) {
            ProjectionSegment firstProjection = ((SelectStatement) sqlStatement).getProjections().getProjections().iterator().next();
            if (firstProjection instanceof ExpressionProjectionSegment
                    && ShowCurrentDatabaseBackendHandler.FUNCTION_NAME.equalsIgnoreCase(((ExpressionProjectionSegment) firstProjection).getText())) {
                return Optional.of(new ShowCurrentDatabaseBackendHandler(backendConnection));
            }
        }
        return Optional.empty();
    }
    
    @Override
    public String getType() {
        return "MySQL";
    }
}
