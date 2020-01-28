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

package org.apache.shardingsphere.sql.parser.integrate.asserts.statement.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.integrate.asserts.SQLCaseAssertContext;
import org.apache.shardingsphere.sql.parser.integrate.asserts.segment.insert.InsertColumnsAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.segment.insert.InsertValuesAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.segment.set.SetClauseAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.segment.table.TableAssert;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.statement.impl.InsertStatementTestCase;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;

import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Insert statement assert.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InsertStatementAssert {
    
    /**
     * Assert insert statement is correct with expected parser result.
     *
     * @param assertContext assert context
     * @param actual actual insert statement
     * @param expected expected insert statement test case
     */
    public static void assertIs(final SQLCaseAssertContext assertContext, final InsertStatement actual, final InsertStatementTestCase expected) {
        assertTable(assertContext, actual, expected);
        assertInsertColumns(assertContext, actual, expected);
        assertInsertValues(assertContext, actual, expected);
        assertSetClause(assertContext, actual, expected);
    }
    
    private static void assertTable(final SQLCaseAssertContext assertContext, final InsertStatement actual, final InsertStatementTestCase expected) {
        TableAssert.assertIs(assertContext, Collections.singletonList(actual.getTable()), expected.getTables());
    }
    
    private static void assertInsertColumns(final SQLCaseAssertContext assertContext, final InsertStatement actual, final InsertStatementTestCase expected) {
        if (null != expected.getColumns()) {
            assertTrue(assertContext.getText("Actual insert columns segment should exist."), actual.getInsertColumns().isPresent());
            InsertColumnsAssert.assertIs(assertContext, actual.getInsertColumns().get(), expected.getColumns());    
        } else {
            assertFalse(assertContext.getText("Actual insert columns segment should not exist."), actual.getInsertColumns().isPresent());
        }
    }
    
    private static void assertInsertValues(final SQLCaseAssertContext assertContext, final InsertStatement actual, final InsertStatementTestCase expected) {
        if (null != expected.getValues()) {
            assertFalse(assertContext.getText("Actual insert values segment should exist."), actual.getValues().isEmpty());
            InsertValuesAssert.assertIs(assertContext, actual.getValues(), expected.getValues());
        } else {
            assertTrue(assertContext.getText("Actual insert values segment should not exist."), actual.getValues().isEmpty());
        }
    }
    
    private static void assertSetClause(final SQLCaseAssertContext assertContext, final InsertStatement actual, final InsertStatementTestCase expected) {
        if (null != expected.getSetClause()) {
            assertTrue(assertContext.getText("Actual set assignment segment should exist."), actual.getSetAssignment().isPresent());
            SetClauseAssert.assertIs(assertContext, actual.getSetAssignment().get(), expected.getSetClause());
        } else {
            assertFalse(assertContext.getText("Actual set assignment segment should not exist."), actual.getSetAssignment().isPresent());
        }
    }
}
