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

package org.apache.shardingsphere.shardingscaling.core.execute.executor.reader;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.shardingscaling.core.config.JdbcDataSourceConfiguration;
import org.apache.shardingsphere.shardingscaling.core.config.RdbmsConfiguration;
import org.apache.shardingsphere.shardingscaling.core.exception.SyncRunException;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.AbstractSyncRunner;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.channel.Channel;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.record.Column;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.record.DataRecord;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.record.FinishedRecord;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.record.Record;
import org.apache.shardingsphere.shardingscaling.core.util.DataSourceFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import javax.sql.DataSource;

/**
 * generic jdbc reader implement.
 *
 * @author avalon566
 * @author yangyi
 */
@Slf4j
public abstract class AbstractJdbcReader extends AbstractSyncRunner implements JdbcReader {

    @Getter(AccessLevel.PROTECTED)
    private final RdbmsConfiguration rdbmsConfiguration;

    @Setter
    private Channel channel;

    public AbstractJdbcReader(final RdbmsConfiguration rdbmsConfiguration) {
        if (!JdbcDataSourceConfiguration.class.equals(rdbmsConfiguration.getDataSourceConfiguration().getClass())) {
            throw new UnsupportedOperationException("AbstractJdbcReader only support JdbcDataSourceConfiguration");
        }
        this.rdbmsConfiguration = rdbmsConfiguration;
    }

    @Override
    public final void run() {
        start();
        read(channel);
    }

    @Override
    public final void read(final Channel channel) {
        DataSource dataSource = DataSourceFactory.getDataSource(rdbmsConfiguration.getDataSourceConfiguration());
        try {
            Connection conn = dataSource.getConnection();
            String sql = String.format("select * from %s %s", rdbmsConfiguration.getTableName(), rdbmsConfiguration.getWhereCondition());
            PreparedStatement ps = conn.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ps.setFetchSize(Integer.MIN_VALUE);
            ps.setFetchDirection(ResultSet.FETCH_REVERSE);
            ResultSet rs = ps.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            while (isRunning() && rs.next()) {
                DataRecord record = new DataRecord(new NopLogPosition(), metaData.getColumnCount());
                record.setType("bootstrap-insert");
                record.setFullTableName(String.format("%s.%s", conn.getCatalog(), rdbmsConfiguration.getTableName()));
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    if (Types.TIME == rs.getMetaData().getColumnType(i)
                            || Types.DATE == rs.getMetaData().getColumnType(i)
                            || Types.TIMESTAMP == rs.getMetaData().getColumnType(i)) {
                        // fix: jdbc Time objects represent a wall-clock time and not a duration as MySQL treats them
                        record.addColumn(new Column(rs.getString(i), true));
                    } else {
                        record.addColumn(new Column(rs.getObject(i), true));
                    }
                }
                pushRecord(record);
            }
        } catch (SQLException e) {
            throw new SyncRunException(e);
        } finally {
            pushRecord(new FinishedRecord(new NopLogPosition()));
        }
    }

    private void pushRecord(final Record record) {
        try {
            channel.pushRecord(record);
        } catch (InterruptedException ignored) {
        }
    }
}
