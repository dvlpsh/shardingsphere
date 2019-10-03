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

package org.apache.shardingsphere.core.rewrite.token.generator.optional.impl;

import com.google.common.base.Optional;
import lombok.Setter;
import org.apache.shardingsphere.core.optimize.statement.SQLStatementContext;
import org.apache.shardingsphere.core.optimize.statement.impl.InsertSQLStatementContext;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.core.rewrite.token.generator.SQLRouteResultAware;
import org.apache.shardingsphere.core.rewrite.token.generator.ShardingRuleAware;
import org.apache.shardingsphere.core.rewrite.token.generator.optional.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.core.rewrite.token.pojo.impl.InsertGeneratedKeyNameToken;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.rule.ShardingRule;

/**
 * Insert generated key name token generator.
 *
 * @author panjuan
 */
@Setter
public final class InsertGeneratedKeyNameTokenGenerator implements OptionalSQLTokenGenerator, ShardingRuleAware, SQLRouteResultAware {
    
    private ShardingRule shardingRule;
    
    private SQLRouteResult sqlRouteResult;
    
    @Override
    public Optional<InsertGeneratedKeyNameToken> generateSQLToken(final SQLStatementContext sqlStatementContext) {
        Optional<InsertColumnsSegment> insertColumnsSegment = sqlStatementContext.getSqlStatement().findSQLSegment(InsertColumnsSegment.class);
        if (!insertColumnsSegment.isPresent() || insertColumnsSegment.get().getColumns().isEmpty()) {
            return Optional.absent();
        }
        if (sqlStatementContext instanceof InsertSQLStatementContext) {
            return createInsertGeneratedKeyToken(sqlStatementContext, insertColumnsSegment.get());
        }
        return Optional.absent();
    }
    
    private Optional<InsertGeneratedKeyNameToken> createInsertGeneratedKeyToken(final SQLStatementContext sqlStatementContext, final InsertColumnsSegment segment) {
        String tableName = sqlStatementContext.getTablesContext().getSingleTableName();
        return sqlRouteResult.getGeneratedKey().isPresent() && sqlRouteResult.getGeneratedKey().get().isGenerated()
                ? Optional.of(new InsertGeneratedKeyNameToken(segment.getStopIndex(), sqlRouteResult.getGeneratedKey().get().getColumnName(), isToAddCloseParenthesis(tableName, segment)))
                : Optional.<InsertGeneratedKeyNameToken>absent();
    }
    
    private boolean isToAddCloseParenthesis(final String tableName, final InsertColumnsSegment segment) {
        return segment.getColumns().isEmpty() && shardingRule.getEncryptRule().getAssistedQueryAndPlainColumns(tableName).isEmpty();
    }
}
