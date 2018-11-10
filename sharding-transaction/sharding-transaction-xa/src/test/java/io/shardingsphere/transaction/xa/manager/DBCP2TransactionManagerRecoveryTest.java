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

package io.shardingsphere.transaction.xa.manager;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.PoolType;
import io.shardingsphere.transaction.xa.convert.dialect.XADataSourceFactory;
import io.shardingsphere.transaction.xa.convert.extractor.DataSourceParameterFactory;
import io.shardingsphere.transaction.xa.fixture.DataSourceUtils;
import io.shardingsphere.transaction.xa.fixture.ReflectiveUtil;
import lombok.SneakyThrows;
import org.apache.tomcat.dbcp.dbcp2.managed.ManagedConnection;
import org.apache.tomcat.dbcp.dbcp2.managed.PoolableManagedConnection;
import org.h2.engine.Session;
import org.h2.jdbc.JdbcConnection;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public final class DBCP2TransactionManagerRecoveryTest extends TransactionManagerRecoveryTest {
    
    @Override
    @SneakyThrows
    protected Session getH2Session(final String dsName) {
        ManagedConnection managedConnection = (ManagedConnection) getXaDataSourceMap().get(dsName).getConnection();
        PoolableManagedConnection poolableManagedConnection = (PoolableManagedConnection) ReflectiveUtil.getProperty(managedConnection, "connection");
        JdbcConnection jdbcConnection = (JdbcConnection) ReflectiveUtil.getProperty(poolableManagedConnection, "connection");
        return (Session) jdbcConnection.getSession();
    }
    
    @Override
    protected DataSource createXADataSource(final String dsName) {
        DataSource dataSource = DataSourceUtils.build(PoolType.DBCP2, DatabaseType.H2, dsName);
        return getAtomikosTransactionManager().wrapDataSource(XADataSourceFactory.build(DatabaseType.H2), dsName, DataSourceParameterFactory.build(dataSource));
    }
    
    @Test(expected = SQLException.class)
    public void assertAccessFailedAfterPrepared() {
        getAtomikosTransactionManager().begin(getBeginEvent());
        insertOrder("ds1");
        coordinateOnlyExecutePrepare();
        try {
            assertOrderCount("ds1", 1L);
            // CHECKSTYLE:OFF
        } catch (Exception ex) {
            // CHECKSTYLE:ON
            assertTrue(ex.getMessage().contains("Unable to register transaction context listener"));
            throw ex;
        }
    }
    
    @Test
    @SneakyThrows
    public void assertFailedInXAResourceUnReleased() {
        // TODO result is different from AtomikosDataSourceBean
        Map<String, DataSource> xaDataSourceMap = createXADataSourceMap();
        xaDataSourceMap.get("ds1").getConnection();
    }
}
