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

package org.apache.shardingsphere.core.merge.fixture;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.execute.sql.execute.result.QueryResult;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Iterator;

@RequiredArgsConstructor
public final class QueryResultBasedQueryResultFixture implements QueryResult {
    
    private final Iterator<QueryResult> queryResults;
    
    private QueryResult currQueryResult;
    
    @Override
    public boolean next() {
        boolean hasNext = queryResults.hasNext();
        if (hasNext) {
            currQueryResult = queryResults.next();
        }
        return hasNext;
    }
    
    @Override
    public Object getValue(final int columnIndex, final Class<?> type) throws SQLException {
        return currQueryResult.getValue(columnIndex, type);
    }
    
    @Override
    public Object getCalendarValue(final int columnIndex, final Class<?> type, final Calendar calendar) {
        return null;
    }
    
    @Override
    public InputStream getInputStream(final int columnIndex, final String type) {
        return null;
    }
    
    @Override
    public boolean wasNull() {
        return false;
    }
    
    @Override
    public boolean isCaseSensitive(final int columnIndex) {
        return false;
    }

    @Override
    public int getColumnCount() throws SQLException {
        return currQueryResult.getColumnCount();
    }
    
    @Override
    public String getColumnLabel(final int columnIndex) {
        return null;
    }
}
