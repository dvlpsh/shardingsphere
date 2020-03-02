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

package org.apache.shardingsphere.sql.parser.relation.statement.ddl;

import lombok.Getter;
import org.apache.shardingsphere.sql.parser.relation.segment.table.TablesContext;
import org.apache.shardingsphere.sql.parser.relation.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.constraint.ConstraintDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.relation.segment.table.TableAvailable;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Create table statement context.
 */
@Getter
public final class CreateTableStatementContext extends CommonSQLStatementContext<CreateTableStatement> implements TableAvailable {
    
    private final TablesContext tablesContext;
    
    public CreateTableStatementContext(final CreateTableStatement sqlStatement) {
        super(sqlStatement);
        tablesContext = new TablesContext(sqlStatement.getTable());
    }
    
    @Override
    public Collection<TableSegment> getAllTables() {
        Collection<TableSegment> result = new LinkedList<>();
        result.add(getSqlStatement().getTable());
        for (ColumnDefinitionSegment each : getSqlStatement().getColumnDefinitions()) {
            result.addAll(each.getReferencedTables());
        }
        for (ConstraintDefinitionSegment each : getSqlStatement().getConstraintDefinitions()) {
            if (each.getReferencedTable().isPresent()) {
                result.add(each.getReferencedTable().get());
            }
        }
        return result;
    }
}
