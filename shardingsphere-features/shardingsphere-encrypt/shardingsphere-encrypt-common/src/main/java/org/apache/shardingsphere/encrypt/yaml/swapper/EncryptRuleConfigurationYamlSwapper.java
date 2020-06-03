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

package org.apache.shardingsphere.encrypt.yaml.swapper;

import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptorConfiguration;
import org.apache.shardingsphere.encrypt.constant.EncryptOrder;
import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptorConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapper;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map.Entry;

/**
 * Encrypt rule configuration YAML swapper.
 */
public final class EncryptRuleConfigurationYamlSwapper implements YamlRuleConfigurationSwapper<YamlEncryptRuleConfiguration, EncryptRuleConfiguration> {
    
    private final EncryptorConfigurationYamlSwapper encryptorConfigurationYamlSwapper = new EncryptorConfigurationYamlSwapper();
    
    private final EncryptTableRuleConfigurationYamlSwapper encryptTableRuleConfigurationYamlSwapper = new EncryptTableRuleConfigurationYamlSwapper();
    
    @Override
    public YamlEncryptRuleConfiguration swap(final EncryptRuleConfiguration data) {
        YamlEncryptRuleConfiguration result = new YamlEncryptRuleConfiguration();
        data.getEncryptors().forEach(each -> result.getEncryptors().put(each.getName(), encryptorConfigurationYamlSwapper.swap(each)));
        data.getTables().forEach(each -> result.getTables().put(each.getName(), encryptTableRuleConfigurationYamlSwapper.swap(each)));
        return result;
    }
    
    @Override
    public EncryptRuleConfiguration swap(final YamlEncryptRuleConfiguration yamlConfiguration) {
        return new EncryptRuleConfiguration(swapEncryptors(yamlConfiguration), swapTables(yamlConfiguration));
    }
    
    private Collection<EncryptorConfiguration> swapEncryptors(final YamlEncryptRuleConfiguration yamlConfiguration) {
        Collection<EncryptorConfiguration> result = new LinkedList<>();
        for (Entry<String, YamlEncryptorConfiguration> entry : yamlConfiguration.getEncryptors().entrySet()) {
            YamlEncryptorConfiguration yamlEncryptorConfiguration = entry.getValue();
            yamlEncryptorConfiguration.setName(entry.getKey());
            result.add(encryptorConfigurationYamlSwapper.swap(yamlEncryptorConfiguration));
        }
        return result;
    }
    
    private Collection<EncryptTableRuleConfiguration> swapTables(final YamlEncryptRuleConfiguration yamlConfiguration) {
        Collection<EncryptTableRuleConfiguration> result = new LinkedList<>();
        for (Entry<String, YamlEncryptTableRuleConfiguration> entry : yamlConfiguration.getTables().entrySet()) {
            YamlEncryptTableRuleConfiguration yamlEncryptTableRuleConfiguration = entry.getValue();
            yamlEncryptTableRuleConfiguration.setName(entry.getKey());
            result.add(encryptTableRuleConfigurationYamlSwapper.swap(yamlEncryptTableRuleConfiguration));
        }
        return result;
    }
    
    @Override
    public Class<EncryptRuleConfiguration> getTypeClass() {
        return EncryptRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "ENCRYPT";
    }
    
    @Override
    public int getOrder() {
        return EncryptOrder.ORDER;
    }
}
