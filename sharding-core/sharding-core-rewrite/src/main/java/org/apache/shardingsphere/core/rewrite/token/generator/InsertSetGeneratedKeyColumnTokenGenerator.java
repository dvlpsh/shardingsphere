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

package org.apache.shardingsphere.core.rewrite.token.generator;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.optimize.statement.impl.InsertSQLStatementContext;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.SetAssignmentsSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.rewrite.builder.parameter.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.statement.RewriteStatement;
import org.apache.shardingsphere.core.rewrite.token.pojo.InsertSetGeneratedKeyColumnToken;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Insert set add items token generator.
 *
 * @author panjuan
 */
public final class InsertSetGeneratedKeyColumnTokenGenerator implements OptionalSQLTokenGenerator<ShardingRule> {
    
    @Override
    public Optional<InsertSetGeneratedKeyColumnToken> generateSQLToken(final RewriteStatement rewriteStatement, 
                                                                       final ParameterBuilder parameterBuilder, final ShardingRule shardingRule, final boolean isQueryWithCipherColumn) {
        Optional<SetAssignmentsSegment> setAssignmentsSegment = rewriteStatement.getSqlStatementContext().getSqlStatement().findSQLSegment(SetAssignmentsSegment.class);
        if (!(rewriteStatement.getSqlStatementContext() instanceof InsertSQLStatementContext && setAssignmentsSegment.isPresent())) {
            return Optional.absent();
        }
        return createInsertSetGeneratedKeyColumnToken((InsertSQLStatementContext) rewriteStatement.getSqlStatementContext(), shardingRule, setAssignmentsSegment.get());
    }
    
    private Optional<InsertSetGeneratedKeyColumnToken> createInsertSetGeneratedKeyColumnToken(final InsertSQLStatementContext insertSQLStatementContext, 
                                                                                              final ShardingRule shardingRule, final SetAssignmentsSegment segment) {
        Optional<String> generatedKeyColumn = getGeneratedKeyColumn(insertSQLStatementContext, shardingRule);
        if (generatedKeyColumn.isPresent()) {
            return Optional.of(createInsertSetGeneratedKeyColumnToken(insertSQLStatementContext, generatedKeyColumn.get(), new ArrayList<>(segment.getAssignments())));
        }
        return Optional.absent();
    }
    
    private InsertSetGeneratedKeyColumnToken createInsertSetGeneratedKeyColumnToken(final InsertSQLStatementContext insertSQLStatementContext, 
                                                                                    final String generatedKeyColumn, final List<AssignmentSegment> assignments) {
        int index = insertSQLStatementContext.getColumnNames().contains(generatedKeyColumn)
                ? insertSQLStatementContext.getColumnNames().indexOf(generatedKeyColumn) : insertSQLStatementContext.getColumnNames().size();
        ExpressionSegment expressionSegment = insertSQLStatementContext.getInsertValueContexts().get(0).getValueExpressions().get(index);
        return new InsertSetGeneratedKeyColumnToken(assignments.get(assignments.size() - 1).getStopIndex() + 1, generatedKeyColumn, expressionSegment);
    }
    
    private Optional<String> getGeneratedKeyColumn(final InsertSQLStatementContext insertSQLStatementContext, final ShardingRule shardingRule) {
        Optional<String> generateKeyColumn = shardingRule.findGenerateKeyColumnName(insertSQLStatementContext.getTablesContext().getSingleTableName());
        return generateKeyColumn.isPresent() && !insertSQLStatementContext.getColumnNames().contains(generateKeyColumn.get()) ? generateKeyColumn : Optional.<String>absent();
    }
}
