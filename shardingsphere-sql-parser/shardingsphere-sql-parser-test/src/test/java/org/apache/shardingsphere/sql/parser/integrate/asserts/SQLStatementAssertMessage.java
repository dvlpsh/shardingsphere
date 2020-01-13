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

package org.apache.shardingsphere.sql.parser.integrate.asserts;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.ParserResultSetRegistry;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.ParserResultSetRegistryFactory;
import org.apache.shardingsphere.test.sql.SQLCaseType;
import org.apache.shardingsphere.test.sql.loader.SQLCasesLoader;
import org.apache.shardingsphere.test.sql.loader.SQLCasesRegistry;

import java.util.Collections;

/**
 * SQL statement assert message.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class SQLStatementAssertMessage {
    
    private final String sqlCaseId;
    
    private final SQLCaseType sqlCaseType;
    
    private final SQLCasesLoader sqlCasesLoader = SQLCasesRegistry.getInstance().getSqlCasesLoader();
    
    private final ParserResultSetRegistry parserResultSetRegistry = ParserResultSetRegistryFactory.getInstance().getRegistry();
    
    /**
     * Get message text.
     * 
     * @param failureMessage failure message
     * @return message text
     */
    public String getText(final String failureMessage) {
        StringBuilder result = new StringBuilder(System.getProperty("line.separator"));
        appendSQLCaseId(result);
        appendSQL(result);
        appendFailureMessage(failureMessage, result);
        return result.toString();
    }
    
    private void appendSQLCaseId(final StringBuilder builder) {
        builder.append("SQL Case ID : ");
        builder.append(sqlCaseId);
        builder.append(System.getProperty("line.separator"));
    }
    
    private void appendSQL(final StringBuilder builder) {
        builder.append("SQL         : ");
        if (SQLCaseType.Placeholder == sqlCaseType) {
            builder.append(sqlCasesLoader.getSQL(sqlCaseId, sqlCaseType, Collections.emptyList()));
            builder.append(System.getProperty("line.separator"));
            builder.append("SQL Params  : ");
            builder.append(parserResultSetRegistry.get(sqlCaseId).getParameters());
            builder.append(System.getProperty("line.separator"));
        } else {
            builder.append(sqlCasesLoader.getSQL(sqlCaseId, sqlCaseType, parserResultSetRegistry.get(sqlCaseId).getParameters()));
        }
    }
    
    private void appendFailureMessage(final String failureMessage, final StringBuilder builder) {
        builder.append(System.getProperty("line.separator"));
        builder.append(failureMessage);
        builder.append(System.getProperty("line.separator"));
    }
}
