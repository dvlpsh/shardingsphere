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

package io.shardingsphere.dbtest.engine;

import com.google.common.base.Splitter;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.dbtest.cases.assertion.IntegrateTestCasesLoader;
import io.shardingsphere.dbtest.cases.assertion.dql.DQLIntegrateTestCase;
import io.shardingsphere.dbtest.cases.assertion.dql.DQLIntegrateTestCaseAssertion;
import io.shardingsphere.dbtest.cases.dataset.expected.dataset.ExpectedDataSetsRoot;
import io.shardingsphere.dbtest.common.DatabaseUtil;
import io.shardingsphere.dbtest.env.DatabaseTypeEnvironment;
import io.shardingsphere.test.sql.SQLCaseType;
import io.shardingsphere.test.sql.SQLCasesLoader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public final class DQLIntegrateTest extends BaseIntegrateTest {
    
    private static SQLCasesLoader sqlCasesLoader = SQLCasesLoader.getInstance();
    
    private static IntegrateTestCasesLoader integrateTestCasesLoader = IntegrateTestCasesLoader.getInstance();
    
    private final DQLIntegrateTestCaseAssertion assertion;
    
    public DQLIntegrateTest(final String sqlCaseId, final String path, final DQLIntegrateTestCaseAssertion assertion,
                            final DatabaseTypeEnvironment databaseTypeEnvironment, final SQLCaseType caseType) throws IOException, JAXBException, SQLException {
        super(sqlCaseId, path, assertion, databaseTypeEnvironment, caseType);
        this.assertion = assertion;
    }
    
    @Parameters(name = "{0} -> {2} -> {3} -> {4}")
    public static Collection<Object[]> getParameters() {
        // TODO sqlCasesLoader size should eq integrateTestCasesLoader size
        // assertThat(sqlCasesLoader.countAllSupportedSQLCases(), is(integrateTestCasesLoader.countAllDataSetTestCases()));
        Collection<Object[]> result = new LinkedList<>();
        for (Object[] each : sqlCasesLoader.getSupportedSQLTestParameters(Arrays.<Enum>asList(DatabaseType.values()), DatabaseType.class)) {
            String sqlCaseId = each[0].toString();
            DatabaseType databaseType = (DatabaseType) each[1];
            SQLCaseType caseType = (SQLCaseType) each[2];
            DQLIntegrateTestCase integrateTestCase = integrateTestCasesLoader.getDQLIntegrateTestCase(sqlCaseId);
            // TODO remove when transfer finished
            if (null == integrateTestCase) {
                continue;
            }
            if (getDatabaseTypes(integrateTestCase.getDatabaseTypes()).contains(databaseType)) {
                result.addAll(getParameters(databaseType, caseType, integrateTestCase));
            }
        }
        return result;
    }
    
    @Before
    public void insertData() throws SQLException, ParseException {
        if (getDatabaseTypeEnvironment().isEnabled()) {
            getDataSetEnvironmentManager().initialize(false);
        }
    }
    
    @Test
    public void assertExecuteQuery() throws JAXBException, IOException, SQLException, ParseException {
        if (!getDatabaseTypeEnvironment().isEnabled()) {
            return;
        }
        try (Connection connection = getDataSource().getConnection()) {
            if (SQLCaseType.Literal == getCaseType()) {
                assertDataSet(DatabaseUtil.executeQueryForStatement(connection, getSql(), assertion.getSQLValues()));
            } else {
                assertDataSet(DatabaseUtil.executeQueryForPreparedStatement(connection, getSql(), assertion.getSQLValues()));
            }
        }
    }
    
    @Test
    public void assertExecute() throws JAXBException, IOException, SQLException, ParseException {
        if (!getDatabaseTypeEnvironment().isEnabled()) {
            return;
        }
        try (Connection connection = getDataSource().getConnection()) {
            if (SQLCaseType.Literal == getCaseType()) {
                assertDataSet(DatabaseUtil.executeDQLForStatement(connection, getSql(), assertion.getSQLValues()));
            } else {
                assertDataSet(DatabaseUtil.executeDQLForPreparedStatement(connection, getSql(), assertion.getSQLValues()));
            }
        }
    }
    
    private void assertDataSet(final List<Map<String, String>> actual) throws IOException, JAXBException {
        ExpectedDataSetsRoot expected;
        try (FileReader reader = new FileReader(getExpectedDataFile())) {
            expected = (ExpectedDataSetsRoot) JAXBContext.newInstance(ExpectedDataSetsRoot.class).createUnmarshaller().unmarshal(reader);
        }
        assertThat(actual.size(), is(expected.getDataSetRows().size()));
        List<String> expectedColumnNames = Splitter.on(",").trimResults().splitToList(expected.getColumns().getValues());
        int count = 0;
        for (Map<String, String> each : actual) {
            List<String> expectedValues = Splitter.on(",").trimResults().splitToList(expected.getDataSetRows().get(count++).getValues());
            assertData(each, expectedValues, expectedColumnNames);
        }
    }
    
    private void assertData(final Map<String, String> actual, final List<String> expectedValues, final List<String> expectedColumnNames) {
        assertThat(actual.size(), is(expectedValues.size()));
        assertThat(actual.size(), is(expectedColumnNames.size()));
        int count = 0;
        for (String each : expectedValues) {
            assertThat(actual.get(expectedColumnNames.get(count++)), is(each));
        }
    }
}
