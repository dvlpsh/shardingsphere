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

package org.apache.shardingsphere.core.route.router.masterslave;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.api.hint.HintManager;
import org.apache.shardingsphere.core.route.RouteResult;
import org.apache.shardingsphere.core.route.router.DateNodeRouteDecorator;
import org.apache.shardingsphere.core.route.type.RoutingUnit;
import org.apache.shardingsphere.core.rule.MasterSlaveRule;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Sharding with master-slave router interface.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class ShardingMasterSlaveRouter implements DateNodeRouteDecorator {
    
    private final Collection<MasterSlaveRule> masterSlaveRules;
    
    @Override
    public RouteResult decorate(final RouteResult routeResult) {
        for (MasterSlaveRule each : masterSlaveRules) {
            route(each, routeResult);
        }
        return routeResult;
    }
    
    private void route(final MasterSlaveRule masterSlaveRule, final RouteResult routeResult) {
        Collection<RoutingUnit> toBeRemoved = new LinkedList<>();
        Collection<RoutingUnit> toBeAdded = new LinkedList<>();
        for (RoutingUnit each : routeResult.getRoutingResult().getRoutingUnits()) {
            if (!masterSlaveRule.getName().equalsIgnoreCase(each.getDataSourceName())) {
                continue;
            }
            toBeRemoved.add(each);
            String actualDataSourceName;
            if (isMasterRoute(routeResult.getSqlStatementContext().getSqlStatement())) {
                MasterVisitedManager.setMasterVisited();
                actualDataSourceName = masterSlaveRule.getMasterDataSourceName();
            } else {
                actualDataSourceName = masterSlaveRule.getLoadBalanceAlgorithm().getDataSource(
                        masterSlaveRule.getName(), masterSlaveRule.getMasterDataSourceName(), new ArrayList<>(masterSlaveRule.getSlaveDataSourceNames()));
            }
            toBeAdded.add(createNewRoutingUnit(actualDataSourceName, each));
        }
        routeResult.getRoutingResult().getRoutingUnits().removeAll(toBeRemoved);
        routeResult.getRoutingResult().getRoutingUnits().addAll(toBeAdded);
    }
    
    private boolean isMasterRoute(final SQLStatement sqlStatement) {
        return containsLockSegment(sqlStatement) || !(sqlStatement instanceof SelectStatement) || MasterVisitedManager.isMasterVisited() || HintManager.isMasterRouteOnly();
    }

    private boolean containsLockSegment(final SQLStatement sqlStatement) {
        return sqlStatement instanceof SelectStatement && ((SelectStatement) sqlStatement).getLock().isPresent();
    }
    
    private RoutingUnit createNewRoutingUnit(final String actualDataSourceName, final RoutingUnit originalTableUnit) {
        RoutingUnit result = new RoutingUnit(actualDataSourceName, originalTableUnit.getDataSourceName());
        result.getTableUnits().addAll(originalTableUnit.getTableUnits());
        return result;
    }
}
