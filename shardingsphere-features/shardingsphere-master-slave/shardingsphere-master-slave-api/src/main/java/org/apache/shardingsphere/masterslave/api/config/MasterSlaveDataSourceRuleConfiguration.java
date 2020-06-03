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

package org.apache.shardingsphere.masterslave.api.config;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;

import java.util.List;

/**
 * Master-slave data source rule configuration.
 */
@Getter
public final class MasterSlaveDataSourceRuleConfiguration {
    
    private final String name;
    
    private final String masterDataSourceName;
    
    private final List<String> slaveDataSourceNames;
    
    private final LoadBalanceStrategyConfiguration loadBalanceStrategy;
    
    public MasterSlaveDataSourceRuleConfiguration(final String name, final String masterDataSourceName, final List<String> slaveDataSourceNames) {
        this(name, masterDataSourceName, slaveDataSourceNames, null);
    }
    
    public MasterSlaveDataSourceRuleConfiguration(final String name,
                                                  final String masterDataSourceName, final List<String> slaveDataSourceNames, final LoadBalanceStrategyConfiguration loadBalanceStrategy) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "Name is required.");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(masterDataSourceName), "Master data source name is required.");
        Preconditions.checkArgument(null != slaveDataSourceNames && !slaveDataSourceNames.isEmpty(), "Slave data source names are required.");
        this.name = name;
        this.masterDataSourceName = masterDataSourceName;
        this.slaveDataSourceNames = slaveDataSourceNames;
        this.loadBalanceStrategy = loadBalanceStrategy;
    }
}
