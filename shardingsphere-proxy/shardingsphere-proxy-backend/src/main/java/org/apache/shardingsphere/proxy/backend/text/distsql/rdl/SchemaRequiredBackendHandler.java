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

package org.apache.shardingsphere.proxy.backend.text.distsql.rdl;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.NoDatabaseSelectedException;
import org.apache.shardingsphere.proxy.backend.exception.UnknownDatabaseException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.distsql.rdl.detail.RDLBackendDetailHandler;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.SchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.available.FromSchemaAvailable;

import java.util.Optional;

/**
 * Schema required backend handler.
 * 
 * @param <T> type of SQL statement context
 */
public abstract class SchemaRequiredBackendHandler<T extends SQLStatementContext<?>> implements RDLBackendDetailHandler<T> {
    
    @Override
    public final ResponseHeader execute(final BackendConnection backendConnection, final T sqlStatementContext) {
        String schemaName = getSchemaName(backendConnection, sqlStatementContext);
        checkSchema(schemaName);
        return execute(schemaName, sqlStatementContext);
    }
    
    protected abstract ResponseHeader execute(String schemaName, T sqlStatementContext);
    
    private String getSchemaName(final BackendConnection backendConnection, final T sqlStatementContext) {
        Optional<SchemaSegment> schemaFromSQL = sqlStatementContext.getSqlStatement() instanceof FromSchemaAvailable
                ? ((FromSchemaAvailable) sqlStatementContext.getSqlStatement()).getSchema() : Optional.empty();
        return schemaFromSQL.isPresent() ? schemaFromSQL.get().getIdentifier().getValue() : backendConnection.getSchemaName();
    }
    
    private void checkSchema(final String schemaName) {
        if (null == schemaName) {
            throw new NoDatabaseSelectedException();
        }
        if (!ProxyContext.getInstance().schemaExists(schemaName)) {
            throw new UnknownDatabaseException(schemaName);
        }
    }
}
