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

package org.apache.shardingsphere.sql.parser.oracle.visitor.statement;

import org.apache.shardingsphere.sql.parser.api.visitor.statement.StatementSQLVisitorFacade;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.impl.DALStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.impl.DCLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.impl.DDLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.impl.DMLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.impl.RLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.impl.TCLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.oracle.visitor.statement.impl.OracleDALStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.oracle.visitor.statement.impl.OracleDCLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.oracle.visitor.statement.impl.OracleDDLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.oracle.visitor.statement.impl.OracleDMLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.oracle.visitor.statement.impl.OracleTCLStatementSQLVisitor;

/**
 * Visitor facade for Oracle.
 */
public final class OracleStatementSQLVisitorFacade implements StatementSQLVisitorFacade {
    
    @Override
    public Class<? extends DMLStatementSQLVisitor> getDMLVisitorClass() {
        return OracleDMLStatementSQLVisitor.class;
    }
    
    @Override
    public Class<? extends DDLStatementSQLVisitor> getDDLVisitorClass() {
        return OracleDDLStatementSQLVisitor.class;
    }
    
    @Override
    public Class<? extends TCLStatementSQLVisitor> getTCLVisitorClass() {
        return OracleTCLStatementSQLVisitor.class;
    }
    
    @Override
    public Class<? extends DCLStatementSQLVisitor> getDCLVisitorClass() {
        return OracleDCLStatementSQLVisitor.class;
    }
    
    @Override
    public Class<? extends DALStatementSQLVisitor> getDALVisitorClass() {
        return OracleDALStatementSQLVisitor.class;
    }
    
    @Override
    public Class<? extends RLStatementSQLVisitor> getRLVisitorClass() {
        return null;
    }
}
