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

package org.apache.shardingsphere.infra.executor.sql.execute.result.query.raw.metadata;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;

import java.util.List;

/**
 * Raw query result meta data.
 */
@RequiredArgsConstructor
public final class RawQueryResultMetaData implements QueryResultMetaData {
    
    private final List<QueryResultColumnMetaData> columns;
    
    @Override
    public int getColumnCount() {
        return columns.size();
    }
    
    @Override
    public String getTableName(final int columnIndex) {
        return columns.get(columnIndex).getTableName();
    }
    
    @Override
    public String getColumnName(final int columnIndex) {
        return columns.get(columnIndex).getName();
    }
    
    @Override
    public String getColumnLabel(final int columnIndex) {
        return columns.get(columnIndex).getLabel();
    }
    
    @Override
    public int getColumnType(final int columnIndex) {
        return columns.get(columnIndex).getType();
    }
    
    @Override
    public String getColumnTypeName(final int columnIndex) {
        return columns.get(columnIndex).getTypeName();
    }
    
    @Override
    public int getColumnLength(final int columnIndex) {
        return columns.get(columnIndex).getLength();
    }
    
    @Override
    public int getDecimals(final int columnIndex) {
        return columns.get(columnIndex).getDecimals();
    }
    
    @Override
    public boolean isSigned(final int columnIndex) {
        return columns.get(columnIndex).isSigned();
    }
    
    @Override
    public boolean isNotNull(final int columnIndex) {
        return columns.get(columnIndex).isNotNull();
    }
    
    @Override
    public boolean isAutoIncrement(final int columnIndex) {
        return columns.get(columnIndex).isAutoIncrement();
    }
}
