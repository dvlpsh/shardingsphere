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

package org.apache.shardingsphere.core.parse.antlr.filler.sharding.dml;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.FromWhereSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.UpdateSetWhereSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.dml.DMLStatement;
import org.apache.shardingsphere.core.parse.parser.context.condition.Column;
import org.apache.shardingsphere.core.parse.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.parse.parser.token.EncryptColumnToken;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Map.Entry;

/**
 * Update set where filler.
 *
 * @author duhongjun
 */
public final class UpdateSetWhereFiller extends DeleteFromWhereFiller {
    
    @Override
    public void fill(final FromWhereSegment sqlSegment, final SQLStatement sqlStatement, final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData) {
        super.fill(sqlSegment, sqlStatement, shardingRule, shardingTableMetaData);
        UpdateSetWhereSegment updateSetWhereSegment = (UpdateSetWhereSegment) sqlSegment;
        DMLStatement dmlStatement = (DMLStatement) sqlStatement;
        String updateTable = dmlStatement.getUpdateTableAlias().values().iterator().next();
        for (Entry<ColumnSegment, ExpressionSegment> entry : updateSetWhereSegment.getUpdateColumns().entrySet()) {
            Column column = new Column(entry.getKey().getName(), updateTable);
            Optional<SQLExpression> expression = entry.getValue().convertToSQLExpression(sqlStatement.getLogicSQL());
            Preconditions.checkState(expression.isPresent());
            dmlStatement.getUpdateColumnValues().put(column, expression.get());
            fillEncryptCondition(entry.getKey(), entry.getValue(), updateTable, shardingRule, dmlStatement);
        }
        dmlStatement.setDeleteStatement(false);
    }
    
    private void fillEncryptCondition(final ColumnSegment columnSegment, final ExpressionSegment expressionSegment, 
                                      final String updateTable, final ShardingRule shardingRule, final DMLStatement dmlStatement) {
        Column column = new Column(columnSegment.getName(), updateTable);
        Optional<SQLExpression> expression = expressionSegment.convertToSQLExpression(dmlStatement.getLogicSQL());
        Preconditions.checkState(expression.isPresent());
        dmlStatement.getUpdateColumnValues().put(column, expression.get());
        if (!shardingRule.getShardingEncryptorEngine().getShardingEncryptor(column.getTableName(), column.getName()).isPresent()) {
            return;
        }
        dmlStatement.getSQLTokens().add(new EncryptColumnToken(columnSegment.getStartIndex(), expressionSegment.getStopIndex(), column, false));
    }
}
