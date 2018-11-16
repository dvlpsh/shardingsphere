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

package io.shardingsphere.core.parsing.antlr.extractor.statement.handler.result;

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.antlr.sql.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.antlr.sql.ddl.ColumnDefinition;
import io.shardingsphere.core.parsing.antlr.sql.ddl.mysql.MySQLAlterTableStatement;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.ddl.create.table.CreateTableStatement;
import io.shardingsphere.core.util.SQLUtil;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

/**
 * Add column result.
 *
 * @author duhongjun
 */
@Getter
public final class ColumnDefinitionExtractResult implements ExtractResult {
    
    private final List<ColumnDefinition> columnDefintions = new LinkedList<>();
    
    /**
     * Inject column definition to SQLStatement.
     * 
     * @param statement SQL statement
     */
    @Override
    public void fill(final SQLStatement statement) {
        if (statement instanceof AlterTableStatement) {
            fillAlter((AlterTableStatement) statement);
        } else if (statement instanceof CreateTableStatement) {
            fillCreate((CreateTableStatement) statement);
        }
    }
    
    private void fillAlter(final AlterTableStatement alterTableStatement) {
        for (ColumnDefinition each : columnDefintions) {
            String oldName = each.getOldName();
            if (null != oldName) {
                Optional<ColumnDefinition> oldDefinition = alterTableStatement.getColumnDefinitionByName(oldName);
                if (!oldDefinition.isPresent()) {
                    return;
                }
                oldDefinition.get().setName(each.getName());
                if (null == each.getType()) {
                    alterTableStatement.getUpdateColumns().put(oldName, oldDefinition.get());
                } else {
                    alterTableStatement.getUpdateColumns().put(oldName, each);
                }
            } else {
                if (!each.isAdd()) {
                    alterTableStatement.getUpdateColumns().put(each.getName(), each);
                } else if (!alterTableStatement.findColumnDefinition(each.getName()).isPresent()) {
                    alterTableStatement.getAddColumns().add(each);
                }
            }
            if (null != each.getPosition()) {
                MySQLAlterTableStatement mysqlAlterTable = (MySQLAlterTableStatement) alterTableStatement;
                mysqlAlterTable.getPositionChangedColumns().add(each.getPosition());
            }
        }
    }
    
    private void fillCreate(final CreateTableStatement createTableStatement) {
        for (ColumnDefinition each : columnDefintions) {
            createTableStatement.getColumnNames().add(SQLUtil.getExactlyValue(each.getName()));
            createTableStatement.getColumnTypes().add(each.getType());
            if (each.isPrimaryKey()) {
                createTableStatement.getPrimaryKeyColumns().add(each.getName());
            }
        }
    }
}
