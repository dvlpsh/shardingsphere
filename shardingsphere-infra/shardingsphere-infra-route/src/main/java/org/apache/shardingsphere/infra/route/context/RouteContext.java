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

package org.apache.shardingsphere.infra.route.context;

import lombok.Getter;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Route context.
 */
@Getter
public final class RouteContext {
    
    private final RouteResult routeResult = new RouteResult();
    
    private final Map<Class<? extends ShardingSphereRule>, RouteStageContext> routeStageContexts = new LinkedHashMap<>();
    
    /**
     * Add next route stage context.
     *
     * @param ruleType rule type
     * @param nextRouteStageContext next route stage contexts
     */
    public void addNextRouteStageContext(final Class<? extends ShardingSphereRule> ruleType, final RouteStageContext nextRouteStageContext) {
        routeStageContexts.put(ruleType, nextRouteStageContext);
    }
    
    /**
     * Get route stage context by rule type.
     *
     * @param ruleType rule type
     * @return route stage context
     */
    public RouteStageContext getRouteStageContext(final Class<? extends ShardingSphereRule> ruleType) {
        return routeStageContexts.get(ruleType);
    }
}
