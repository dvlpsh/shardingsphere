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

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.sharding.algorithm.sharding.inline.InlineExpressionParser;
import org.apache.shardingsphere.test.integration.cases.assertion.dml.DMLIntegrateTestCaseAssertion;
import org.apache.shardingsphere.test.integration.cases.assertion.root.SQLCaseType;
import org.apache.shardingsphere.test.integration.cases.dataset.metadata.DataSetColumn;
import org.apache.shardingsphere.test.integration.cases.dataset.metadata.DataSetMetadata;
import org.apache.shardingsphere.test.integration.cases.dataset.row.DataSetRow;
import org.apache.shardingsphere.test.integration.engine.SingleIT;
import org.apache.shardingsphere.test.integration.env.EnvironmentPath;
import org.apache.shardingsphere.test.integration.env.dataset.DataSetEnvironmentManager;
import org.apache.shardingsphere.test.integration.env.schema.SchemaEnvironmentManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@Slf4j
public abstract class BaseDMLIT extends SingleIT {
    
    private final DataSetEnvironmentManager dataSetEnvironmentManager;
    
    protected BaseDMLIT(final String parentPath, final DMLIntegrateTestCaseAssertion assertion, final String ruleType,
                        final DatabaseType databaseType, final SQLCaseType caseType, final String sql) throws IOException, JAXBException, SQLException, ParseException {
        super(parentPath, assertion, ruleType, databaseType, caseType, sql);
        dataSetEnvironmentManager = new DataSetEnvironmentManager(EnvironmentPath.getDataSetFile(ruleType), getActualDataSources());
    }
    
    @BeforeClass
    public static void initDatabasesAndTables() throws IOException, JAXBException {
        SchemaEnvironmentManager.createDatabases();
        SchemaEnvironmentManager.dropTables();
        SchemaEnvironmentManager.createTables();
    }
    
    @AfterClass
    public static void destroyDatabasesAndTables() throws IOException, JAXBException {
        SchemaEnvironmentManager.dropDatabases();
    }
    
    @Before
    public void insertData() throws SQLException, ParseException {
        dataSetEnvironmentManager.load();
    }
    
    @After
    public void clearData() {
        dataSetEnvironmentManager.clear();
    }
    
    protected final void assertDataSet(final int actualUpdateCount) throws SQLException {
        try {
            assertThat("Only support single table for DML.", getDataSet().getMetadataList().size(), is(1));
            assertThat(actualUpdateCount, is(getDataSet().getUpdateCount()));
            DataSetMetadata expectedDataSetMetadata = getDataSet().getMetadataList().get(0);
            for (String each : new InlineExpressionParser(expectedDataSetMetadata.getDataNodes()).splitAndEvaluate()) {
                DataNode dataNode = new DataNode(each);
                String sql;
                if (getDatabaseType() instanceof PostgreSQLDatabaseType) {
                    try (Connection connection = getActualDataSources().get(dataNode.getDataSourceName()).getConnection()) {
                        String primaryKeyColumnName = getPostgreSQLTablePrimaryKeyColumnName(connection, dataNode.getTableName());
                        sql = String.format("SELECT * FROM %s ORDER BY %s ASC", dataNode.getTableName(), primaryKeyColumnName);
                    }
                } else {
                    sql = String.format("SELECT * FROM %s", dataNode.getTableName());
                }
                try (Connection connection = getActualDataSources().get(dataNode.getDataSourceName()).getConnection();
                     PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    assertDataSet(preparedStatement, getDataSet().findRows(dataNode), expectedDataSetMetadata);
                }
            }
        } catch (final AssertionError ex) {
            log.error("[ERROR] SQL::{}, Parameter::{}, Expect::{}", getCaseIdentifier(), getAssertion().getParameters(), getAssertion().getExpectedDataFile());
            throw ex;
        }
    }
    
    private void assertDataSet(final PreparedStatement actualPreparedStatement, final List<DataSetRow> expectedDataSetRows, final DataSetMetadata expectedDataSetMetadata) throws SQLException {
        try (ResultSet actualResultSet = actualPreparedStatement.executeQuery()) {
            assertMetaData(actualResultSet.getMetaData(), expectedDataSetMetadata.getColumns());
            assertRows(actualResultSet, expectedDataSetRows);
        }
    }
    
    private String getPostgreSQLTablePrimaryKeyColumnName(final Connection connection, final String tableName) throws SQLException {
        String sql = "SELECT a.attname, format_type(a.atttypid, a.atttypmod) AS data_type FROM pg_index i JOIN pg_attribute a "
            + "ON a.attrelid = i.indrelid AND a.attnum = ANY(i.indkey) WHERE i.indrelid = '"
            + tableName + "'::regclass AND i.indisprimary";
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
            if (!resultSet.next()) {
                throw new SQLException("can not get primary key of " + tableName);
            }
            return resultSet.getString("attname");
        }
    }
    
    private void assertMetaData(final ResultSetMetaData actualMetaData, final List<DataSetColumn> columnMetadataList) throws SQLException {
        assertThat(actualMetaData.getColumnCount(), is(columnMetadataList.size()));
        int index = 1;
        for (DataSetColumn each : columnMetadataList) {
            assertThat(actualMetaData.getColumnLabel(index++), is(each.getName()));
        }
    }
    
    private void assertRows(final ResultSet actualResultSet, final List<DataSetRow> expectedDatSetRows) throws SQLException {
        int count = 0;
        while (actualResultSet.next()) {
            int index = 1;
            for (String each : expectedDatSetRows.get(count).getValues()) {
                if (Types.DATE == actualResultSet.getMetaData().getColumnType(index)) {
                    if (!NOT_VERIFY_FLAG.equals(each)) {
                        assertThat(new SimpleDateFormat("yyyy-MM-dd").format(actualResultSet.getDate(index)), is(each));
                    }
                } else {
                    assertThat(String.valueOf(actualResultSet.getObject(index)), is(each));
                }
                index++;
            }
            count++;
        }
        assertThat("Size of actual result set is different with size of expected dat set rows.", count, is(expectedDatSetRows.size()));
    }
}
