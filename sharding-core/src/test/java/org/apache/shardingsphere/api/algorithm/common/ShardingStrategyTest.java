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

package org.apache.shardingsphere.api.algorithm.common;

import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import org.apache.shardingsphere.api.algorithm.fixture.TestComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.api.algorithm.fixture.TestPreciseShardingAlgorithm;
import org.apache.shardingsphere.api.algorithm.fixture.TestRangeShardingAlgorithm;
import org.apache.shardingsphere.api.algorithm.sharding.PreciseShardingValue;
import org.apache.shardingsphere.api.algorithm.sharding.RangeShardingValue;
import org.apache.shardingsphere.api.algorithm.sharding.ShardingValue;
import org.apache.shardingsphere.api.config.strategy.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.api.config.strategy.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.core.routing.strategy.complex.ComplexShardingStrategy;
import org.apache.shardingsphere.core.routing.strategy.none.NoneShardingStrategy;
import org.apache.shardingsphere.core.routing.strategy.standard.StandardShardingStrategy;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ShardingStrategyTest {
    
    private final Collection<String> targets = Sets.newHashSet("1", "2", "3");
    
    @Test
    public void assertDoShardingWithoutShardingColumns() {
        NoneShardingStrategy strategy = new NoneShardingStrategy();
        assertThat(strategy.doSharding(targets, Collections.<ShardingValue>emptySet()), is(targets));
    }
    
    @Test
    public void assertDoShardingForBetweenSingleKey() {
        StandardShardingStrategy strategy = new StandardShardingStrategy(new StandardShardingStrategyConfiguration("column", new TestPreciseShardingAlgorithm(), new TestRangeShardingAlgorithm()));
        assertThat(strategy.doSharding(targets, Collections.<ShardingValue>singletonList(new RangeShardingValue<>("logicTable", "column", Range.open("1", "3")))), 
                is((Collection<String>) Sets.newHashSet("1", "2", "3")));
    }
    
    @Test
    public void assertDoShardingForMultipleKeys() {
        ComplexShardingStrategy strategy = new ComplexShardingStrategy(new ComplexShardingStrategyConfiguration("column", new TestComplexKeysShardingAlgorithm()));
        assertThat(strategy.doSharding(targets, Collections.<ShardingValue>singletonList(new PreciseShardingValue<>("logicTable", "column", "1"))), 
                is((Collection<String>) Sets.newHashSet("1", "2", "3")));
    }
}
