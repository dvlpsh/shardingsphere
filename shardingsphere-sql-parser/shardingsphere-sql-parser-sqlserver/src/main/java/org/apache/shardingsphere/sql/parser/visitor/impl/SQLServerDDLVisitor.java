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

package org.apache.shardingsphere.sql.parser.visitor.impl;

import org.apache.shardingsphere.sql.parser.visitor.SQLServerVisitor;

/**
 * SQLServer DDL visitor.
 *
 * @author zhangliang
 */
public final class SQLServerDDLVisitor extends SQLServerVisitor {
    
//    @Override
//    public ASTNode visitCreateTable(final CreateTableContext ctx) {
//        CreateTableStatement result = new CreateTableStatement();
//        TableSegment table = (TableSegment) visit(ctx.tableName());
//        result.getTables().add(table);
//        result.getAllSQLSegments().add(table);
//        if (null != ctx.createDefinitionClause()) {
//            CreateTableStatement createDefinition = (CreateTableStatement) visit(ctx.createDefinitionClause());
//            result.getColumnDefinitions().addAll(createDefinition.getColumnDefinitions());
//            for (SQLSegment each : createDefinition.getAllSQLSegments()) {
//                result.getAllSQLSegments().add(each);
//                if (each instanceof TableSegment) {
//                    result.getTables().add((TableSegment) each);
//                }
//            }
//        }
//        return result;
//    }
//    
//    @Override
//    public ASTNode visitCreateDefinitionClause(final CreateDefinitionClauseContext ctx) {
//        CreateTableStatement result = new CreateTableStatement();
//        for (RelationalPropertyContext each : ctx.relationalProperties().relationalProperty()) {
//            ColumnDefinitionContext columnDefinition = each.columnDefinition();
//            if (null != columnDefinition) {
//                result.getColumnDefinitions().add((ColumnDefinitionSegment) visit(columnDefinition));
//                Collection<TableSegment> tableSegments = getTableSegments(columnDefinition);
//                result.getTables().addAll(tableSegments);
//                result.getAllSQLSegments().addAll(tableSegments);
//            }
//            if (null != each.outOfLineConstraint() && null != each.outOfLineConstraint().referencesClause() && null != each.outOfLineConstraint().referencesClause().tableName()) {
//                TableSegment tableSegment = (TableSegment) visit(each.outOfLineConstraint().referencesClause().tableName());
//                result.getTables().add(tableSegment);
//                result.getAllSQLSegments().add(tableSegment);
//            }
//            if (null != each.outOfLineRefConstraint() && null != each.outOfLineRefConstraint().referencesClause() && null != each.outOfLineRefConstraint().referencesClause().tableName()) {
//                TableSegment tableSegment = (TableSegment) visit(each.outOfLineRefConstraint().referencesClause().tableName());
//                result.getTables().add(tableSegment);
//                result.getAllSQLSegments().add(tableSegment);
//            }
//        }
//        if (result.getColumnDefinitions().isEmpty()) {
//            result.getAllSQLSegments().addAll(result.getColumnDefinitions());
//        }
//        return result;
//    }
//    
//    @Override
//    public ASTNode visitColumnDefinition(final ColumnDefinitionContext ctx) {
//        ColumnSegment column = (ColumnSegment) visit(ctx.columnName());
//        IdentifierValue dataType = (IdentifierValue) visit(ctx.dataType().dataTypeName());
//        boolean isPrimaryKey = containsPrimaryKey(ctx);
//        return new ColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), column.getIdentifier().getValue(), dataType.getValue(), isPrimaryKey);
//    }
//    
//    private boolean containsPrimaryKey(final ColumnDefinitionContext ctx) {
//        for (InlineConstraintContext each : ctx.inlineConstraint()) {
//            if (null != each.primaryKey()) {
//                return true;
//            }
//        }
//        return false;
//    }
//    
//    private Collection<TableSegment> getTableSegments(final ColumnDefinitionContext columnDefinition) {
//        Collection<TableSegment> result = new LinkedList<>();
//        for (InlineConstraintContext each : columnDefinition.inlineConstraint()) {
//            if (null != each.referencesClause()) {
//                result.add((TableSegment) visit(each.referencesClause().tableName()));
//            }
//        }
//        if (null != columnDefinition.inlineRefConstraint()) {
//            result.add((TableSegment) visit(columnDefinition.inlineRefConstraint().tableName()));
//        }
//        return result;
//    }
//    
//    private Collection<TableSegment> getTableSegments(final VirtualColumnDefinitionContext virtualColumnDefinition) {
//        Collection<TableSegment> result = new LinkedList<>();
//        for (InlineConstraintContext each : virtualColumnDefinition.inlineConstraint()) {
//            if (null != each.referencesClause()) {
//                result.add((TableSegment) visit(each.referencesClause().tableName()));
//            }
//        }
//        return result;
//    }
//    
//    @Override
//    public ASTNode visitAlterTable(final AlterTableContext ctx) {
//        AlterTableStatement result = new AlterTableStatement();
//        TableSegment table = (TableSegment) visit(ctx.tableName());
//        result.getTables().add(table);
//        result.getAllSQLSegments().add(table);
//        if (null != ctx.alterDefinitionClause()) {
//            AlterTableStatement alterDefinition = (AlterTableStatement) visit(ctx.alterDefinitionClause());
//            result.getAddedColumnDefinitions().addAll(alterDefinition.getAddedColumnDefinitions());
//            result.getChangedPositionColumns().addAll(alterDefinition.getChangedPositionColumns());
//            result.getDroppedColumnNames().addAll(alterDefinition.getDroppedColumnNames());
//            for (SQLSegment each : alterDefinition.getAllSQLSegments()) {
//                result.getAllSQLSegments().add(each);
//                if (each instanceof TableSegment) {
//                    result.getTables().add((TableSegment) each);
//                }
//            }
//        }
//        return result;
//    }
//
//    @SuppressWarnings("unchecked")
//    @Override
//    public ASTNode visitAlterDefinitionClause(final AlterDefinitionClauseContext ctx) {
//        final AlterTableStatement result = new AlterTableStatement();
//        if (null != ctx.columnClauses()) {
//            for (OperateColumnClauseContext each : ctx.columnClauses().operateColumnClause()) {
//                AddColumnSpecificationContext addColumnSpecification = each.addColumnSpecification();
//                if (null != addColumnSpecification) {
//                    CollectionValue<AddColumnDefinitionSegment> addColumnDefinitions = (CollectionValue<AddColumnDefinitionSegment>) visit(addColumnSpecification);
//                    for (AddColumnDefinitionSegment addColumnDefinition : addColumnDefinitions.getValue()) {
//                        result.getAddedColumnDefinitions().add(addColumnDefinition.getColumnDefinition());
//                        Optional<ColumnPositionSegment> columnPositionSegment = addColumnDefinition.getColumnPosition();
//                        // TODO refactor SQLStatement
//                        // CHECKSTYLE:OFF
//                        if (columnPositionSegment.isPresent()) {
//                            result.getChangedPositionColumns().add(columnPositionSegment.get());
//                        }
//                        // CHECKSTYLE:ON
//                    }
//                    for (ColumnOrVirtualDefinitionContext columnOrVirtualDefinition : addColumnSpecification.columnOrVirtualDefinitions().columnOrVirtualDefinition()) {
//                        // TODO refactor SQLStatement
//                        // CHECKSTYLE:OFF
//                        if (null != columnOrVirtualDefinition.columnDefinition()) {
//                            result.getAllSQLSegments().addAll(getTableSegments(columnOrVirtualDefinition.columnDefinition()));
//                        }
//                        if (null != columnOrVirtualDefinition.virtualColumnDefinition()) {
//                            result.getAllSQLSegments().addAll(getTableSegments(columnOrVirtualDefinition.virtualColumnDefinition()));
//                        }
//                        // CHECKSTYLE:ON
//                    }
//                }
//                ModifyColumnSpecificationContext modifyColumnSpecification = each.modifyColumnSpecification();
//                if (null != modifyColumnSpecification) {
//                    Optional<ColumnPositionSegment> columnPositionSegment = ((ModifyColumnDefinitionSegment) visit(modifyColumnSpecification)).getColumnPosition();
//                    // TODO refactor SQLStatement
//                    // CHECKSTYLE:OFF
//                    if (columnPositionSegment.isPresent()) {
//                        result.getChangedPositionColumns().add(columnPositionSegment.get());
//                    }
//                    // CHECKSTYLE:ON
//                }
//                DropColumnClauseContext dropColumnClause = each.dropColumnClause();
//                if (null != dropColumnClause) {
//                    result.getDroppedColumnNames().add(((DropColumnDefinitionSegment) visit(dropColumnClause)).getColumnName());
//                }
//            }
//        }
//        if (result.getAddedColumnDefinitions().isEmpty()) {
//            result.getAllSQLSegments().addAll(result.getAddedColumnDefinitions());
//        }
//        if (result.getChangedPositionColumns().isEmpty()) {
//            result.getAllSQLSegments().addAll(result.getChangedPositionColumns());
//        }
//        return result;
//    }
//
//    @Override
//    public ASTNode visitAddColumnSpecification(final AddColumnSpecificationContext ctx) {
//        CollectionValue<AddColumnDefinitionSegment> result = new CollectionValue<>();
//        for (ColumnOrVirtualDefinitionContext each : ctx.columnOrVirtualDefinitions().columnOrVirtualDefinition()) {
//            if (null != each.columnDefinition()) {
//                AddColumnDefinitionSegment addColumnDefinition = new AddColumnDefinitionSegment(
//                        each.columnDefinition().getStart().getStartIndex(), each.columnDefinition().getStop().getStopIndex(), (ColumnDefinitionSegment) visit(each.columnDefinition()));
//                result.getValue().add(addColumnDefinition);
//            }
//        }
//        return result;
//    }
//
//    @Override
//    public ASTNode visitModifyColumnSpecification(final ModifyColumnSpecificationContext ctx) {
//        // TODO visit column definition, need to change g4 for modifyColumn
//        return new ModifyColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), null);
//    }
//
//    @Override
//    public ASTNode visitDropColumnSpecification(final DropColumnSpecificationContext ctx) {
//        // TODO can drop multiple columns
//        return new DropColumnDefinitionSegment(
//                ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), ((ColumnSegment) visit(ctx.columnOrColumnList().columnName(0))).getIdentifier().getValue());
//    }
//    
//    @SuppressWarnings("unchecked")
//    @Override
//    public ASTNode visitDropTable(final DropTableContext ctx) {
//        DropTableStatement result = new DropTableStatement();
//        TableSegment table = (TableSegment) visit(ctx.tableName());
//        result.getTables().add(table);
//        result.getAllSQLSegments().add(table);
//        return result;
//    }
//    
//    @SuppressWarnings("unchecked")
//    @Override
//    public ASTNode visitTruncateTable(final TruncateTableContext ctx) {
//        TruncateStatement result = new TruncateStatement();
//        TableSegment table = (TableSegment) visit(ctx.tableName());
//        result.getTables().add(table);
//        result.getAllSQLSegments().add(table);
//        return result;
//    }
//    
//    @Override
//    public ASTNode visitCreateIndex(final CreateIndexContext ctx) {
//        CreateIndexStatement result = new CreateIndexStatement();
//        if (null != ctx.createIndexDefinitionClause_().tableIndexClause_()) {
//            TableSegment table = (TableSegment) visit(ctx.createIndexDefinitionClause_().tableIndexClause_().tableName());
//            result.setTable(table);
//            result.getAllSQLSegments().add(table);
//        }
//        return result;
//    }
//    
//    @Override
//    public ASTNode visitAlterIndex(final AlterIndexContext ctx) {
//        return new AlterIndexStatement();
//    }
//    
//    @Override
//    public ASTNode visitDropIndex(final DropIndexContext ctx) {
//        return new DropIndexStatement();
//    }
}
