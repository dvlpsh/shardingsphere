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

package org.apache.shardingsphere.core.route.router.sharding;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.api.hint.HintManager;
import org.apache.shardingsphere.core.metadata.ShardingMetaData;
import org.apache.shardingsphere.core.optimize.OptimizeEngineFactory;
import org.apache.shardingsphere.core.optimize.statement.OptimizedStatement;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.ShardingWhereOptimizedStatement;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.condition.ShardingConditions;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.insert.ShardingInsertOptimizedStatement;
import org.apache.shardingsphere.core.parse.cache.ParsingResultCache;
import org.apache.shardingsphere.core.parse.entry.ShardingSQLParseEntry;
import org.apache.shardingsphere.core.parse.hook.ParsingHook;
import org.apache.shardingsphere.core.parse.hook.SPIParsingHook;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.route.type.RoutingResult;
import org.apache.shardingsphere.core.rule.BindingTableRule;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.rule.TableRule;
import org.apache.shardingsphere.core.strategy.route.value.ListRouteValue;
import org.apache.shardingsphere.core.strategy.route.value.RouteValue;
import org.apache.shardingsphere.spi.database.DatabaseType;

import java.util.LinkedList;
import java.util.List;

/**
 * Sharding router with parse.
 *
 * @author zhangliang
 * @author maxiaoguang
 * @author panjuan
 * @author zhangyonglun
 */
@RequiredArgsConstructor
public final class ParsingSQLRouter implements ShardingRouter {
    
    private final ShardingRule shardingRule;
    
    private final ShardingMetaData shardingMetaData;
    
    private final DatabaseType databaseType;
    
    private final ParsingResultCache parsingResultCache;
    
    private final List<Comparable<?>> generatedValues = new LinkedList<>();
    
    private final ParsingHook parsingHook = new SPIParsingHook();
    
    @Override
    public SQLStatement parse(final String logicSQL, final boolean useCache) {
        parsingHook.start(logicSQL);
        try {
            SQLStatement result = new ShardingSQLParseEntry(databaseType, shardingMetaData.getTable(), parsingResultCache).parse(logicSQL, useCache);
            parsingHook.finishSuccess(result, shardingMetaData.getTable());
            return result;
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            parsingHook.finishFailure(ex);
            throw ex;
        }
    }
    
    @Override
    public SQLRouteResult route(final SQLStatement sqlStatement, final List<Object> parameters) {
        OptimizedStatement optimizedStatement = OptimizeEngineFactory.newInstance(shardingRule, shardingMetaData.getTable(), sqlStatement, parameters).optimize();
        boolean needMergeShardingValues = isNeedMergeShardingValues(sqlStatement);
        if (optimizedStatement instanceof ShardingWhereOptimizedStatement && needMergeShardingValues) {
            checkSubqueryShardingValues(sqlStatement, ((ShardingWhereOptimizedStatement) optimizedStatement).getShardingConditions());
            mergeShardingConditions(((ShardingWhereOptimizedStatement) optimizedStatement).getShardingConditions());
        }
        RoutingResult routingResult = RoutingEngineFactory.newInstance(shardingRule, shardingMetaData.getDataSource(), optimizedStatement).route();
        if (needMergeShardingValues) {
            Preconditions.checkState(1 == routingResult.getRoutingUnits().size(), "Must have one sharding with subquery.");
        }
        if (optimizedStatement instanceof ShardingInsertOptimizedStatement) {
            setGeneratedValues((ShardingInsertOptimizedStatement) optimizedStatement);
        }
        SQLRouteResult result = new SQLRouteResult(optimizedStatement);
        result.setRoutingResult(routingResult);
        return result;
    }
    
    private void setGeneratedValues(final ShardingInsertOptimizedStatement optimizedStatement) {
        if (optimizedStatement.getGeneratedKey().isPresent()) {
            generatedValues.addAll(optimizedStatement.getGeneratedKey().get().getGeneratedValues());
            optimizedStatement.getGeneratedKey().get().getGeneratedValues().clear();
            optimizedStatement.getGeneratedKey().get().getGeneratedValues().addAll(generatedValues);
        }
    }
    
    private boolean isNeedMergeShardingValues(final SQLStatement sqlStatement) {
        return sqlStatement instanceof SelectStatement 
                && ((SelectStatement) sqlStatement).isContainsSubquery() && !shardingRule.getShardingLogicTableNames(sqlStatement.getTables().getTableNames()).isEmpty();
    }
    
    private void checkSubqueryShardingValues(final SQLStatement sqlStatement, final ShardingConditions shardingConditions) {
        for (String each : sqlStatement.getTables().getTableNames()) {
            Optional<TableRule> tableRule = shardingRule.findTableRule(each);
            if (tableRule.isPresent() && shardingRule.isRoutingByHint(tableRule.get()) && !HintManager.getDatabaseShardingValues(each).isEmpty()
                    && !HintManager.getTableShardingValues(each).isEmpty()) {
                return;
            }
        }
        Preconditions.checkState(null != shardingConditions.getShardingConditions() && !shardingConditions.getShardingConditions().isEmpty(), "Must have sharding column with subquery.");
        if (shardingConditions.getShardingConditions().size() > 1) {
            Preconditions.checkState(isSameShardingCondition(shardingConditions), "Sharding value must same with subquery.");
        }
    }
    
    private boolean isSameShardingCondition(final ShardingConditions shardingConditions) {
        ShardingCondition example = shardingConditions.getShardingConditions().remove(shardingConditions.getShardingConditions().size() - 1);
        for (ShardingCondition each : shardingConditions.getShardingConditions()) {
            if (!isSameShardingCondition(example, each)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isSameShardingCondition(final ShardingCondition shardingCondition1, final ShardingCondition shardingCondition2) {
        if (shardingCondition1.getRouteValues().size() != shardingCondition2.getRouteValues().size()) {
            return false;
        }
        for (int i = 0; i < shardingCondition1.getRouteValues().size(); i++) {
            RouteValue shardingValue1 = shardingCondition1.getRouteValues().get(i);
            RouteValue shardingValue2 = shardingCondition2.getRouteValues().get(i);
            if (!isSameRouteValue((ListRouteValue) shardingValue1, (ListRouteValue) shardingValue2)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isSameRouteValue(final ListRouteValue routeValue1, final ListRouteValue routeValue2) {
        return isSameLogicTable(routeValue1, routeValue2)
                && routeValue1.getColumnName().equals(routeValue2.getColumnName()) && routeValue1.getValues().equals(routeValue2.getValues());
    }
    
    private boolean isSameLogicTable(final ListRouteValue shardingValue1, final ListRouteValue shardingValue2) {
        return shardingValue1.getTableName().equals(shardingValue2.getTableName()) || isBindingTable(shardingValue1, shardingValue2);
    }
    
    private boolean isBindingTable(final ListRouteValue shardingValue1, final ListRouteValue shardingValue2) {
        Optional<BindingTableRule> bindingRule = shardingRule.findBindingTableRule(shardingValue1.getTableName());
        return bindingRule.isPresent() && bindingRule.get().hasLogicTable(shardingValue2.getTableName());
    }
    
    private void mergeShardingConditions(final ShardingConditions shardingConditions) {
        if (shardingConditions.getShardingConditions().size() > 1) {
            ShardingCondition shardingCondition = shardingConditions.getShardingConditions().remove(shardingConditions.getShardingConditions().size() - 1);
            shardingConditions.getShardingConditions().clear();
            shardingConditions.getShardingConditions().add(shardingCondition);
        }
    }
}
