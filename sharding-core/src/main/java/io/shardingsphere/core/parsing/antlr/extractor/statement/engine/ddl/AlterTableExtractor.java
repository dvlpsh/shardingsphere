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

package io.shardingsphere.core.parsing.antlr.extractor.statement.engine.ddl;

import io.shardingsphere.core.metadata.table.ColumnMetaData;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.metadata.table.TableMetaData;
import io.shardingsphere.core.parsing.antlr.extractor.segment.engine.DropColumnExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.engine.RenameTableExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.engine.TableNamesExtractor;
import io.shardingsphere.core.parsing.antlr.sql.statement.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.antlr.sql.statement.ddl.ColumnDefinition;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Abstract statement extractor, get information by each phrase extractor.
 * 
 * @author duhongjun
 */
public abstract class AlterTableExtractor extends DDLStatementExtractor {
    
    protected AlterTableExtractor() {
        addSQLSegmentExtractor(new TableNamesExtractor());
        addSQLSegmentExtractor(new RenameTableExtractor());
        addSQLSegmentExtractor(new DropColumnExtractor());
    }
    
    @Override
    protected final SQLStatement createStatement() {
        return new AlterTableStatement();
    }
    
    @Override
    protected final void postExtract(final SQLStatement sqlStatement, final ShardingTableMetaData shardingTableMetaData) {
        AlterTableStatement alterTableStatement = (AlterTableStatement) sqlStatement;
        TableMetaData oldTableMetaData = shardingTableMetaData.get(alterTableStatement.getTables().getSingleTableName());
        if (null == oldTableMetaData) {
            return;
        }
        List<ColumnMetaData> newColumnMetaData = getUpdatedColumnMetaDataList(alterTableStatement, oldTableMetaData);
        fillColumnDefinition(alterTableStatement, newColumnMetaData);
        adjustColumnDefinition(alterTableStatement, newColumnMetaData);
        dropColumnDefinition(alterTableStatement, newColumnMetaData);
        alterTableStatement.setTableMetaData(new TableMetaData(newColumnMetaData));
    }
    
    private List<ColumnMetaData> getUpdatedColumnMetaDataList(final AlterTableStatement alterTableStatement, final TableMetaData oldTableMetaData) {
        List<ColumnMetaData> result = new LinkedList<>();
        for (ColumnMetaData each : oldTableMetaData.getColumnMetaData()) {
            ColumnDefinition updatedColumnDefinition = alterTableStatement.getUpdateColumns().get(each.getColumnName());
            String columnName;
            String columnType;
            boolean primaryKey;
            if (null == updatedColumnDefinition) {
                columnName = each.getColumnName();
                columnType = each.getColumnType();
                primaryKey = !alterTableStatement.isDropPrimaryKey() && each.isPrimaryKey();
            } else {
                columnName = updatedColumnDefinition.getName();
                columnType = updatedColumnDefinition.getType();
                primaryKey = !alterTableStatement.isDropPrimaryKey() && updatedColumnDefinition.isPrimaryKey();
            }
            result.add(new ColumnMetaData(columnName, columnType, primaryKey));
        }
        return result;
    }
    
    private void fillColumnDefinition(final AlterTableStatement alterTableStatement, final List<ColumnMetaData> newColumnMetaData) {
        for (ColumnDefinition each : alterTableStatement.getAddColumns()) {
            newColumnMetaData.add(new ColumnMetaData(each.getName(), each.getType(), each.isPrimaryKey()));
        }
    }
    
    protected void adjustColumnDefinition(final AlterTableStatement alterTableStatement, final List<ColumnMetaData> newColumnMetaData) {
    }
    
    private void dropColumnDefinition(final AlterTableStatement alterTableStatement, final List<ColumnMetaData> newColumnMetaData) {
        Iterator<ColumnMetaData> iterator = newColumnMetaData.iterator();
        while (iterator.hasNext()) {
            ColumnMetaData each = iterator.next();
            if (alterTableStatement.getDropColumns().contains(each.getColumnName())) {
                iterator.remove();
            }
        }
    }
}
