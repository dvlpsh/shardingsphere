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
import org.apache.shardingsphere.core.parse.sql.context.table.Table;
import org.apache.shardingsphere.core.parse.sql.segment.OwnerAvailable;
import org.apache.shardingsphere.core.parse.sql.segment.SQLSegment;
import org.apache.shardingsphere.core.parse.sql.segment.TableAvailable;
import org.apache.shardingsphere.core.parse.sql.segment.common.TableSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.SelectItemsSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.SelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.item.ShorthandSelectItemSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.rewrite.token.pojo.TableToken;
import org.apache.shardingsphere.core.rule.BaseRule;
import org.apache.shardingsphere.core.rule.MasterSlaveRule;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Table token generator.
 *
 * @author zhangliang
 * @author panjuan
 */
public final class TableTokenGenerator implements CollectionSQLTokenGenerator<BaseRule> {
    
    @Override
    public Collection<TableToken> generateSQLTokens(final SQLStatement sqlStatement, final List<Object> parameters, final BaseRule baseRule) {
        Collection<TableToken> result = new LinkedList<>();
        for (SQLSegment each : sqlStatement.getSQLSegments()) {
            if (each instanceof SelectItemsSegment) {
                result.addAll(createTableTokens(sqlStatement, baseRule, (SelectItemsSegment) each));
            } else if (each instanceof ColumnSegment) {
                Optional<TableToken> tableToken = createTableToken(sqlStatement, baseRule, (ColumnSegment) each);
                if (tableToken.isPresent()) {
                    result.add(tableToken.get());
                }
            } else if (each instanceof TableAvailable) {
                Optional<TableToken> tableToken = createTableToken(sqlStatement, baseRule, (TableAvailable) each);
                if (tableToken.isPresent()) {
                    result.add(tableToken.get());
                }
            }
        }
        return result;
    }
    
    private Collection<TableToken> createTableTokens(final SQLStatement sqlStatement, final BaseRule baseRule, final SelectItemsSegment selectItemsSegment) {
        Collection<TableToken> result = new LinkedList<>();
        for (SelectItemSegment each : selectItemsSegment.getSelectItems()) {
            if (each instanceof ShorthandSelectItemSegment) {
                Optional<TableToken> tableToken = createTableToken(sqlStatement, baseRule, (ShorthandSelectItemSegment) each);
                if (tableToken.isPresent()) {
                    result.add(tableToken.get());
                }
            }
        }
        return result;
    }
    
    private Optional<TableToken> createTableToken(final SQLStatement sqlStatement, final BaseRule baseRule, final OwnerAvailable<TableSegment> segment) {
        Optional<TableSegment> owner = segment.getOwner();
        if (!owner.isPresent()) {
            return Optional.absent();
        }
        if (isToGenerateTableToken(sqlStatement, baseRule, owner.get())) {
            return Optional.of(new TableToken(owner.get().getStartIndex(), owner.get().getStopIndex(), owner.get().getName(), owner.get().getQuoteCharacter()));
        }
        return Optional.absent();
    }
    
    private Optional<TableToken> createTableToken(final SQLStatement sqlStatement, final BaseRule baseRule, final TableAvailable segment) {
        if (isToGenerateTableToken(sqlStatement, baseRule, segment)) {
            return Optional.of(new TableToken(segment.getStartIndex(), segment.getStopIndex(), segment.getTableName(), segment.getTableQuoteCharacter()));
        }
        return Optional.absent();
    }
    
    private boolean isToGenerateTableToken(final SQLStatement sqlStatement, final BaseRule baseRule, final TableSegment tableSegment) {
        if (baseRule instanceof ShardingRule) {
            Optional<Table> table = sqlStatement.getTables().find(tableSegment.getName());
            return table.isPresent() && !table.get().getAlias().isPresent() && ((ShardingRule) baseRule).findTableRule(table.get().getName()).isPresent(); 
        }
        return baseRule instanceof MasterSlaveRule;
    }
    
    private boolean isToGenerateTableToken(final SQLStatement sqlStatement, final BaseRule baseRule, final TableAvailable segment) {
        if (baseRule instanceof ShardingRule) {
            return ((ShardingRule) baseRule).findTableRule(segment.getTableName()).isPresent() || !(sqlStatement instanceof SelectStatement);
        }
        return baseRule instanceof MasterSlaveRule;
    }
}
