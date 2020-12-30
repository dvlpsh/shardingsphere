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

package org.apache.shardingsphere.test.integration.engine.dml;

import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.test.integration.cases.IntegrateTestCaseType;
import org.apache.shardingsphere.test.integration.cases.assertion.IntegrateTestCaseContext;
import org.apache.shardingsphere.test.integration.cases.assertion.root.IntegrateTestCaseAssertion;
import org.apache.shardingsphere.test.integration.cases.assertion.root.SQLValue;
import org.apache.shardingsphere.test.integration.engine.BatchIT;
import org.apache.shardingsphere.test.integration.engine.util.IntegrateTestParameters;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class BatchDMLIT extends BatchIT {
    
    private final IntegrateTestCaseContext testCaseContext;
    
    public BatchDMLIT(final IntegrateTestCaseContext testCaseContext,
                      final String ruleType, final String databaseType, final String sql) throws IOException, JAXBException, SQLException {
        super(testCaseContext, ruleType, DatabaseTypeRegistry.getActualDatabaseType(databaseType), sql);
        this.testCaseContext = testCaseContext;
    }
    
    @Parameters(name = "Rule:{1} -> {2} -> {3}")
    public static Collection<Object[]> getParameters() {
        return IntegrateTestParameters.getParametersWithCase(IntegrateTestCaseType.DML);
    }
    
    @Test
    public void assertExecuteBatch() throws SQLException, ParseException {
        // TODO fix replica_query
        if ("replica_query".equals(getRuleType())) {
            return;
        }
        // TODO fix shadow
        if ("shadow".equals(getRuleType())) {
            return;
        }
        int[] actualUpdateCounts;
        try (Connection connection = getTargetDataSource().getConnection()) {
            actualUpdateCounts = executeBatchForPreparedStatement(connection);
        }
        assertDataSet(actualUpdateCounts);
    }
    
    private int[] executeBatchForPreparedStatement(final Connection connection) throws SQLException, ParseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getSql())) {
            for (IntegrateTestCaseAssertion each : testCaseContext.getTestCase().getIntegrateTestCaseAssertions()) {
                addBatch(preparedStatement, each);
            }
            return preparedStatement.executeBatch();
        }
    }
    
    private void addBatch(final PreparedStatement preparedStatement, final IntegrateTestCaseAssertion assertion) throws ParseException, SQLException {
        for (SQLValue each : assertion.getSQLValues()) {
            preparedStatement.setObject(each.getIndex(), each.getValue());
        }
        preparedStatement.addBatch();
    }
    
    @Test
    public void assertClearBatch() throws SQLException, ParseException {
        // TODO fix replica_query
        if ("replica_query".equals(getRuleType())) {
            return;
        }
        // TODO fix shadow
        if ("shadow".equals(getRuleType())) {
            return;
        }
        try (Connection connection = getTargetDataSource().getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(getSql())) {
                for (IntegrateTestCaseAssertion each : testCaseContext.getTestCase().getIntegrateTestCaseAssertions()) {
                    addBatch(preparedStatement, each);
                }
                preparedStatement.clearBatch();
                assertThat(preparedStatement.executeBatch().length, is(0));
            }
        }
    }
}
