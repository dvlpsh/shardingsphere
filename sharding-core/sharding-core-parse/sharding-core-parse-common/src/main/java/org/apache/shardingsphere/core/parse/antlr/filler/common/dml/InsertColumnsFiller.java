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

package org.apache.shardingsphere.core.parse.antlr.filler.common.dml;

import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.antlr.constant.QuoteCharacter;
import org.apache.shardingsphere.core.parse.antlr.filler.SQLSegmentFiller;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.parse.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parse.parser.context.condition.Column;
import org.apache.shardingsphere.core.parse.parser.token.InsertValuesToken;
import org.apache.shardingsphere.core.parse.parser.token.TableToken;
import org.apache.shardingsphere.core.rule.BaseRule;

/**
 * Insert columns filler.
 *
 * @author zhangliang
 */
public final class InsertColumnsFiller implements SQLSegmentFiller<InsertColumnsSegment, BaseRule> {
    
    @Override
    public void fill(final InsertColumnsSegment sqlSegment, final SQLStatement sqlStatement, final BaseRule rule, final ShardingTableMetaData shardingTableMetaData) {
        if (sqlStatement instanceof InsertStatement) {
            InsertStatement insertStatement = (InsertStatement) sqlStatement;
            if (sqlSegment.getColumns().isEmpty()) {
                fillFromMetaData(insertStatement, shardingTableMetaData);
            } else {
                fillFromSegment(sqlSegment, insertStatement);
            }
            insertStatement.getSQLTokens().add(new InsertValuesToken(sqlSegment.getStartIndex(), DefaultKeyword.VALUES));
        }
    }
    
    private void fillFromMetaData(final InsertStatement insertStatement, final ShardingTableMetaData shardingTableMetaData) {
        String tableName = insertStatement.getTables().getSingleTableName();
        if (shardingTableMetaData.containsTable(tableName)) {
            for (String each : shardingTableMetaData.getAllColumnNames(tableName)) {
                Column column = new Column(each, tableName);
                insertStatement.getColumns().add(column);
            }
        }
    }
    
    private void fillFromSegment(final InsertColumnsSegment sqlSegment, final InsertStatement insertStatement) {
        String tableName = insertStatement.getTables().getSingleTableName();
        for (ColumnSegment each : sqlSegment.getColumns()) {
            insertStatement.getColumns().add(new Column(each.getName(), tableName));
            if (each.getOwner().isPresent() && tableName.equals(each.getOwner().get())) {
                insertStatement.getSQLTokens().add(new TableToken(each.getStartIndex(), tableName, QuoteCharacter.getQuoteCharacter(tableName), 0));
            }
        }
    }
}
