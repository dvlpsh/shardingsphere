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

package org.apache.shardingsphere.sql.parser.visitor;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.antlr.v4.runtime.Token;
import org.apache.shardingsphere.sql.parser.MySQLVisitor;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AddColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AddConstraintSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterDefinitionClause_Context;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterSpecification_Context;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ChangeColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ColumnDefinitionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ConstraintDefinition_Context;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateDefinitionClause_Context;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateDefinition_Context;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateLikeClause_Context;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropPrimaryKeySpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DropTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.FirstOrAfterColumnContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ForeignKeyOption_Context;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.GeneratedDataType_Context;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.InlineDataType_Context;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ModifyColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ReferenceDefinition_Context;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RenameColumnSpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TruncateTableContext;
import org.apache.shardingsphere.sql.parser.sql.ASTNode;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.ColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.alter.AddColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.alter.DropColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.alter.ModifyColumnDefinitionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.alter.RenameColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.position.ColumnAfterPositionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.position.ColumnFirstPositionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.column.position.ColumnPositionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.constraint.DropPrimaryKeySegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.DropTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.TruncateStatement;
import org.apache.shardingsphere.sql.parser.sql.value.CollectionValue;
import org.apache.shardingsphere.sql.parser.sql.value.LiteralValue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * MySQL DDL visitor.
 *
 * @author panjuan
 */
public final class MySQLDDLVisitor extends MySQLVisitor {
    
    @Override
    public ASTNode visitCreateTable(final CreateTableContext ctx) {
        CreateTableStatement result = new CreateTableStatement();
        TableSegment table = (TableSegment) visit(ctx.tableName());
        result.setTable(table);
        result.getAllSQLSegments().add(table);
        CreateDefinitionClause_Context createDefinitionClause = ctx.createDefinitionClause_();
        if (null != createDefinitionClause) {
            CreateTableStatement createDefinition = (CreateTableStatement) visit(createDefinitionClause);
            result.getColumnDefinitions().addAll(createDefinition.getColumnDefinitions());
            result.getAllSQLSegments().addAll(createDefinition.getAllSQLSegments());
        }
        CreateLikeClause_Context createLikeClause = ctx.createLikeClause_();
        if (null != createLikeClause) {
            result.getAllSQLSegments().add((TableSegment) visit(createLikeClause));
        }
        return result;
    }
    
    @Override
    public ASTNode visitAlterTable(final AlterTableContext ctx) {
        AlterTableStatement result = new AlterTableStatement();
        TableSegment table = (TableSegment) visit(ctx.tableName());
        result.setTable(table);
        result.getAllSQLSegments().add(table);
        AlterDefinitionClause_Context alterDefinitionClause = ctx.alterDefinitionClause_();
        if (null != alterDefinitionClause) {
            AlterTableStatement alterDefinition = (AlterTableStatement) visit(alterDefinitionClause);
            result.getAddedColumnDefinitions().addAll(alterDefinition.getAddedColumnDefinitions());
            result.getChangedPositionColumns().addAll(alterDefinition.getChangedPositionColumns());
            result.getDroppedColumnNames().addAll(alterDefinition.getDroppedColumnNames());
            result.getAllSQLSegments().addAll(alterDefinition.getAllSQLSegments());
        }
        return result;
    }
    
    @Override
    public ASTNode visitDropTable(final DropTableContext ctx) {
        DropTableStatement result = new DropTableStatement();
        CollectionValue<TableSegment> tables = (CollectionValue<TableSegment>) visit(ctx.tableNames());
        result.getTables().addAll(tables.getValues());
        result.getAllSQLSegments().addAll(tables.getValues());
        return result;
    }
    
    @Override
    public ASTNode visitTruncateTable(final TruncateTableContext ctx) {
        TruncateStatement result = new TruncateStatement();
        TableSegment table = (TableSegment) visit(ctx.tableName());
        result.getAllSQLSegments().add(table);
        result.getTables().add(table);
        return result;
    }
    
    @Override
    public ASTNode visitCreateIndex(final CreateIndexContext ctx) {
        CreateIndexStatement result = new CreateIndexStatement();
        TableSegment table = (TableSegment) visit(ctx.tableName());
        result.setTable(table);
        result.getAllSQLSegments().add(table);
        return result;
    }
    
    @Override
    public ASTNode visitDropIndex(final DropIndexContext ctx) {
        DropIndexStatement result = new DropIndexStatement();
        TableSegment table = (TableSegment) visit(ctx.tableName());
        result.setTable(table);
        result.getAllSQLSegments().add(table);
        return result;
    }
    
    @Override
    public ASTNode visitColumnDefinition(final ColumnDefinitionContext ctx) {
        ColumnSegment column = (ColumnSegment) visit(ctx.columnName());
        LiteralValue dataType = (LiteralValue) visit(ctx.dataType().dataTypeName_());
        Collection<InlineDataType_Context> inlineDataTypes = Collections2.filter(ctx.inlineDataType_(), new Predicate<InlineDataType_Context>() {
            @Override
            public boolean apply(final InlineDataType_Context inlineDataType) {
                return null != inlineDataType.commonDataTypeOption_() && null != inlineDataType.commonDataTypeOption_().primaryKey();
            }
        });
        Collection<GeneratedDataType_Context> generatedDataTypes = Collections2.filter(ctx.generatedDataType_(), new Predicate<GeneratedDataType_Context>() {
            @Override
            public boolean apply(final GeneratedDataType_Context generatedDataType) {
                return null != generatedDataType.commonDataTypeOption_()
                        && null != generatedDataType.commonDataTypeOption_().primaryKey();
            }
        });
        boolean isPrimaryKey = inlineDataTypes.size() > 0 || generatedDataTypes.size() > 0;
        return new ColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(),
                column.getName(), dataType.getLiteral(), isPrimaryKey);
    }
    
    @Override
    public ASTNode visitFirstOrAfterColumn(final FirstOrAfterColumnContext ctx) {
        return null == ctx.columnName() ? new ColumnFirstPositionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), null)
                : new ColumnAfterPositionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), null,
                ((ColumnSegment) visit(ctx.columnName())).getName());
    }
    
    @Override
    public ASTNode visitCreateDefinitionClause_(final CreateDefinitionClause_Context ctx) {
        CreateTableStatement result = new CreateTableStatement();
        for (CreateDefinition_Context createDefinition : ctx.createDefinitions_().createDefinition_()) {
            ColumnDefinitionContext columnDefinition = createDefinition.columnDefinition();
            if (null != columnDefinition) {
                result.getColumnDefinitions().add((ColumnDefinitionSegment) visit(columnDefinition));
                result.getAllSQLSegments().addAll(extractColumnDefinition(columnDefinition));
            }
            ConstraintDefinition_Context constraintDefinition = createDefinition.constraintDefinition_();
            ForeignKeyOption_Context foreignKeyOption = null == constraintDefinition ? null : constraintDefinition.foreignKeyOption_();
            if (null != foreignKeyOption) {
                result.getAllSQLSegments().add((TableSegment) visit(foreignKeyOption));
            }
        }
        if (result.getColumnDefinitions().isEmpty()) {
            result.getAllSQLSegments().addAll(result.getColumnDefinitions());
        }
        return result;
    }
    
    @Override
    public ASTNode visitCreateLikeClause_(final CreateLikeClause_Context ctx) {
        return visit(ctx.tableName());
    }
    
    @Override
    public ASTNode visitAlterDefinitionClause_(final AlterDefinitionClause_Context ctx) {
        final AlterTableStatement result = new AlterTableStatement();
        for (AlterSpecification_Context alterSpecification : ctx.alterSpecification_()) {
            AddColumnSpecificationContext addColumnSpecification = alterSpecification.addColumnSpecification();
            if (null != addColumnSpecification) {
                CollectionValue<AddColumnDefinitionSegment> addColumnDefinitions = (CollectionValue<AddColumnDefinitionSegment>) visit(addColumnSpecification);
                for (AddColumnDefinitionSegment addColumnDefinition : addColumnDefinitions.getValues()) {
                    result.getAddedColumnDefinitions().add(addColumnDefinition.getColumnDefinition());
                    Optional<ColumnPositionSegment> columnPositionSegment = addColumnDefinition.getColumnPosition();
                    if (columnPositionSegment.isPresent()) {
                        result.getChangedPositionColumns().add(columnPositionSegment.get());
                    }
                }
                result.getAllSQLSegments().addAll(extractColumnDefinitions(addColumnSpecification.columnDefinition()));
            }
            AddConstraintSpecificationContext addConstraintSpecification = alterSpecification.addConstraintSpecification();
            ForeignKeyOption_Context foreignKeyOption = null == addConstraintSpecification
                    ? null : addConstraintSpecification.constraintDefinition_().foreignKeyOption_();
            if (null != foreignKeyOption) {
                result.getAllSQLSegments().add((TableSegment) visit(foreignKeyOption));
            }
            ChangeColumnSpecificationContext changeColumnSpecification = alterSpecification.changeColumnSpecification();
            if (null != changeColumnSpecification) {
                Optional<ColumnPositionSegment> columnPositionSegment = ((ModifyColumnDefinitionSegment) visit(changeColumnSpecification)).getColumnPosition();
                if (columnPositionSegment.isPresent()) {
                    result.getChangedPositionColumns().add(columnPositionSegment.get());
                }
                result.getAllSQLSegments().addAll(extractColumnDefinition(changeColumnSpecification.columnDefinition()));
            }
            DropColumnSpecificationContext dropColumnSpecification = alterSpecification.dropColumnSpecification();
            if (null != dropColumnSpecification) {
                result.getDroppedColumnNames().add(((DropColumnDefinitionSegment) visit(dropColumnSpecification)).getColumnName());
            }
            ModifyColumnSpecificationContext modifyColumnSpecification = alterSpecification.modifyColumnSpecification();
            if (null != modifyColumnSpecification) {
                Optional<ColumnPositionSegment> columnPositionSegment = ((ModifyColumnDefinitionSegment) visit(modifyColumnSpecification)).getColumnPosition();
                if (columnPositionSegment.isPresent()) {
                    result.getChangedPositionColumns().add(columnPositionSegment.get());
                }
                result.getAllSQLSegments().addAll(extractColumnDefinition(modifyColumnSpecification.columnDefinition()));
            }
        }
        if (result.getAddedColumnDefinitions().isEmpty()) {
            result.getAllSQLSegments().addAll(result.getAddedColumnDefinitions());
        }
        if (result.getChangedPositionColumns().isEmpty()) {
            result.getAllSQLSegments().addAll(result.getChangedPositionColumns());
        }
        return result;
    }
    
    @Override
    public ASTNode visitAddColumnSpecification(final AddColumnSpecificationContext ctx) {
        CollectionValue<AddColumnDefinitionSegment> result = new CollectionValue<>();
        List<AddColumnDefinitionSegment> addColumnDefinitions = Lists.transform(ctx.columnDefinition(), new Function<ColumnDefinitionContext, AddColumnDefinitionSegment>() {
            @Override
            public AddColumnDefinitionSegment apply(final ColumnDefinitionContext columnDefinition) {
                return new AddColumnDefinitionSegment(columnDefinition.getStart().getStartIndex(),
                        columnDefinition.getStop().getStopIndex(), (ColumnDefinitionSegment) visit(columnDefinition));
            }
        });
        if (null == ctx.firstOrAfterColumn()) {
            result.getValues().addAll(addColumnDefinitions);
        } else {
            AddColumnDefinitionSegment addColumnDefinition = addColumnDefinitions.get(0);
            addColumnDefinition.setColumnPosition(extractColumnDefinition(addColumnDefinition.getColumnDefinition(),
                    (ColumnPositionSegment) visit(ctx.firstOrAfterColumn())));
            result.getValues().add(addColumnDefinition);
        }
        return result;
    }
    
    @Override
    public ASTNode visitChangeColumnSpecification(final ChangeColumnSpecificationContext ctx) {
        return extractModifyColumnDefinition(ctx.getStart(), ctx.getStop(), ctx.columnDefinition(), ctx.firstOrAfterColumn());
    }
    
    @Override
    public ASTNode visitDropColumnSpecification(final DropColumnSpecificationContext ctx) {
        return new DropColumnDefinitionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(),
                ((ColumnSegment) visit(ctx.columnName())).getName());
    }
    
    @Override
    public ASTNode visitDropPrimaryKeySpecification(final DropPrimaryKeySpecificationContext ctx) {
        return new DropPrimaryKeySegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
    }
    
    @Override
    public ASTNode visitModifyColumnSpecification(final ModifyColumnSpecificationContext ctx) {
        return extractModifyColumnDefinition(ctx.getStart(), ctx.getStop(), ctx.columnDefinition(), ctx.firstOrAfterColumn());
    }
    
    @Override
    public ASTNode visitRenameColumnSpecification(final RenameColumnSpecificationContext ctx) {
        return new RenameColumnSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(),
                ((ColumnSegment) visit(ctx.columnName(0))).getName(), ((ColumnSegment) visit(ctx.columnName(1))).getName());
    }
    
    @Override
    public ASTNode visitReferenceDefinition_(final ReferenceDefinition_Context ctx) {
        return visit(ctx.tableName());
    }
    
    @Override
    public ASTNode visitForeignKeyOption_(final ForeignKeyOption_Context ctx) {
        return visit(ctx.referenceDefinition_());
    }
    
    private ModifyColumnDefinitionSegment extractModifyColumnDefinition(final Token start, final Token stop, final ColumnDefinitionContext columnDefinition,
                                                                  final FirstOrAfterColumnContext firstOrAfterColumn) {
        ModifyColumnDefinitionSegment result = new ModifyColumnDefinitionSegment(start.getStartIndex(), stop.getStopIndex(),
                (ColumnDefinitionSegment) visit(columnDefinition));
        if (null != firstOrAfterColumn) {
            result.setColumnPosition(extractColumnDefinition(result.getColumnDefinition(), (ColumnPositionSegment) visit(firstOrAfterColumn)));
        }
        return result;
    }
    
    private ColumnPositionSegment extractColumnDefinition(final ColumnDefinitionSegment columnDefinition, final ColumnPositionSegment columnPosition) {
        return columnPosition instanceof ColumnFirstPositionSegment
                ? new ColumnFirstPositionSegment(columnPosition.getStartIndex(), columnPosition.getStopIndex(), columnDefinition.getColumnName())
                : new ColumnAfterPositionSegment(columnPosition.getStartIndex(), columnPosition.getStopIndex(), columnDefinition.getColumnName(),
                ((ColumnAfterPositionSegment) columnPosition).getAfterColumnName());
    }
    
    private Collection<TableSegment> extractColumnDefinition(final ColumnDefinitionContext columnDefinition) {
        Collection<TableSegment> result = new LinkedList<>();
        for (InlineDataType_Context inlineDataType : columnDefinition.inlineDataType_()) {
            if (null != inlineDataType.commonDataTypeOption_() && null != inlineDataType.commonDataTypeOption_().referenceDefinition_()) {
                result.add((TableSegment) visit(inlineDataType.commonDataTypeOption_().referenceDefinition_()));
            }
        }
        for (GeneratedDataType_Context generatedDataType : columnDefinition.generatedDataType_()) {
            if (null != generatedDataType.commonDataTypeOption_() && null != generatedDataType.commonDataTypeOption_().referenceDefinition_()) {
                result.add((TableSegment) visit(generatedDataType.commonDataTypeOption_().referenceDefinition_()));
            }
        }
        return result;
    }
    
    private Collection<TableSegment> extractColumnDefinitions(final List<ColumnDefinitionContext> columnDefinitions) {
        Collection<TableSegment> result = new LinkedList<>();
        for (ColumnDefinitionContext columnDefinition : columnDefinitions) {
            result.addAll(extractColumnDefinition(columnDefinition));
        }
        return result;
    }
}
