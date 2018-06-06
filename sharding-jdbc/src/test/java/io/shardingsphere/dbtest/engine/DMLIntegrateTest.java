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
import io.shardingsphere.core.rule.DataNode;
import io.shardingsphere.core.util.InlineExpressionParser;
import io.shardingsphere.dbtest.cases.assertion.IntegrateTestCasesLoader;
import io.shardingsphere.dbtest.cases.assertion.dml.DMLIntegrateTestCase;
import io.shardingsphere.dbtest.cases.assertion.dml.DMLIntegrateTestCaseAssertion;
import io.shardingsphere.dbtest.cases.dataset.init.DataSetColumnMetadata;
import io.shardingsphere.dbtest.cases.dataset.init.DataSetMetadata;
import io.shardingsphere.dbtest.cases.dataset.init.DataSetRow;
import io.shardingsphere.dbtest.cases.dataset.init.DataSetsRoot;
import io.shardingsphere.dbtest.common.DatabaseUtil;
import io.shardingsphere.dbtest.env.DatabaseTypeEnvironment;
import io.shardingsphere.test.sql.SQLCaseType;
import io.shardingsphere.test.sql.SQLCasesLoader;
import org.junit.After;
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
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public final class DMLIntegrateTest extends BaseIntegrateTest {
    
    private static SQLCasesLoader sqlCasesLoader = SQLCasesLoader.getInstance();
    
    private static IntegrateTestCasesLoader integrateTestCasesLoader = IntegrateTestCasesLoader.getInstance();
    
    private final DMLIntegrateTestCaseAssertion assertion;
    
    public DMLIntegrateTest(final String sqlCaseId, final String path, final DMLIntegrateTestCaseAssertion assertion, 
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
            DMLIntegrateTestCase integrateTestCase = integrateTestCasesLoader.getDMLIntegrateTestCase(sqlCaseId);
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
            getDataSetEnvironmentManager().initialize(true);
        }
    }
    
    @After
    public void clearData() throws SQLException {
        if (getDatabaseTypeEnvironment().isEnabled()) {
            getDataSetEnvironmentManager().clear();
        }
    }
    
    @Test
    public void assertExecuteUpdate() throws JAXBException, IOException, SQLException, ParseException {
        if (!getDatabaseTypeEnvironment().isEnabled()) {
            return;
        }
        try (Connection connection = getDataSource().getConnection()) {
            if (SQLCaseType.Literal == getCaseType()) {
                assertThat(DatabaseUtil.executeUpdateForStatement(connection, getSql(), assertion.getSQLValues()), is(assertion.getExpectedUpdate()));
            } else {
                assertThat(DatabaseUtil.executeUpdateForPreparedStatement(connection, getSql(), assertion.getSQLValues()), is(assertion.getExpectedUpdate()));
            } 
        }
        assertDataSet();
    }
    
    @Test
    public void assertExecute() throws JAXBException, IOException, SQLException, ParseException {
        if (!getDatabaseTypeEnvironment().isEnabled()) {
            return;
        }
        try (Connection connection = getDataSource().getConnection()) {
            if (SQLCaseType.Literal == getCaseType()) {
                assertThat(DatabaseUtil.executeDMLForStatement(connection, getSql(), assertion.getSQLValues()), is(assertion.getExpectedUpdate()));
            } else {
                assertThat(DatabaseUtil.executeDMLForPreparedStatement(connection, getSql(), assertion.getSQLValues()), is(assertion.getExpectedUpdate()));
            }
        }
        assertDataSet();
    }
    
    private void assertDataSet() throws IOException, JAXBException, SQLException {
        DataSetsRoot expected;
        try (FileReader reader = new FileReader(getExpectedDataFile())) {
            expected = (DataSetsRoot) JAXBContext.newInstance(DataSetsRoot.class).createUnmarshaller().unmarshal(reader);
        }
        assertDataSet(expected);
    }
    
    private void assertDataSet(final DataSetsRoot expected) throws SQLException {
        assertThat("Only support single table for DML.", expected.getMetadataList().size(), is(1));
        DataSetMetadata dataSetMetadata = expected.getMetadataList().get(0);
        for (String each : new InlineExpressionParser(dataSetMetadata.getDataNodes()).evaluate()) {
            DataNode dataNode = new DataNode(each);
            try (Connection connection = getDataSourceMap().get(dataNode.getDataSourceName()).getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(String.format("SELECT * FROM %s", dataNode.getTableName()))) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    int count = 0;
                    while (resultSet.next()) {
                        List<String> actualResultSetData = getResultSetData(dataSetMetadata, resultSet);
                        assertTrue(String.format("Cannot find actual record '%s' from data node '%s'", actualResultSetData, each), isMatch(each, actualResultSetData, expected.getDataSetRows()));
                        count++;
                    }
                    assertThat(String.format("Count of records are different for data node '%s'", each), count, is(countExpectedDataSetRows(each, expected.getDataSetRows())));
                }
            }
        }
    }
    
    private List<String> getResultSetData(final DataSetMetadata dataSetMetadata, final ResultSet resultSet) throws SQLException {
        List<String> result = new ArrayList<>(dataSetMetadata.getColumnMetadataList().size());
        for (DataSetColumnMetadata each : dataSetMetadata.getColumnMetadataList()) {
            Object resultSetValue = resultSet.getObject(each.getName());
            result.add(resultSetValue instanceof Date ? new SimpleDateFormat("yyyy-MM-dd").format(resultSetValue) : resultSetValue.toString());
        }
        return result;
    }
    
    private boolean isMatch(final String actualDataNode, final List<String> actualResultSetData, final List<DataSetRow> expectedDataSetRows) {
        for (DataSetRow each : expectedDataSetRows) {
            if (each.getDataNode().equals(actualDataNode) && isMatch(actualResultSetData, each)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isMatch(final List<String> actualResultSetData, final DataSetRow expectedDataSetRow) {
        int count = 0;
        for (String each : Splitter.on(",").trimResults().splitToList(expectedDataSetRow.getValues())) {
            if (!each.equals(actualResultSetData.get(count))) {
                return false;
            }
            count++;
        }
        return true;
    }
    
    private int countExpectedDataSetRows(final String actualDataNode, final List<DataSetRow> expectedDataSetRows) {
        int result = 0;
        for (DataSetRow each : expectedDataSetRows) {
            if (each.getDataNode().equals(actualDataNode)) {
                result++;
            }
            
        }
        return result;
    }
}
