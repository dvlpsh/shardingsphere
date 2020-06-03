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

package org.apache.shardingsphere.example.shadow.table.raw.jdbc.config;

import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.encrypt.api.config.EncryptColumnConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptorConfiguration;
import org.apache.shardingsphere.example.config.ExampleConfiguration;
import org.apache.shardingsphere.example.core.api.DataSourceUtil;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

public final class EncryptShadowDatabasesConfiguration implements ExampleConfiguration {
    
    @Override
    public DataSource getDataSource() throws SQLException {
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("ds", DataSourceUtil.createDataSource("demo_ds"));
        dataSourceMap.put("ds_0", DataSourceUtil.createDataSource("shadow_demo_ds"));
        EncryptRuleConfiguration encryptRuleConfiguration = new EncryptRuleConfiguration(getEncryptorConfiguration(), getEncryptTableRuleConfigurations());
        Properties properties = new Properties();
        properties.setProperty("sql.show", "true");
        properties.setProperty("query.with.cipher.column", "true");
        ShadowRuleConfiguration shadowRuleConfiguration = new ShadowRuleConfiguration("shadow", Collections.singletonMap("ds", "ds_0"));
        return ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, Arrays.asList(shadowRuleConfiguration, encryptRuleConfiguration), properties);
    }
    
    private Collection<EncryptorConfiguration> getEncryptorConfiguration() {
        Collection<EncryptorConfiguration> result = new LinkedList<>();
        Properties properties = new Properties();
        properties.setProperty("aes.key.value", "123456");
        EncryptorConfiguration nameEncryptorConfiguration = new EncryptorConfiguration("name_encryptror", "aes", properties);
        EncryptorConfiguration pwdEncryptorConfiguration = new EncryptorConfiguration("pwd_encryptror", "assistedTest", null);
        result.add(nameEncryptorConfiguration);
        result.add(pwdEncryptorConfiguration);
        return result;
    }
    
    private Collection<EncryptTableRuleConfiguration> getEncryptTableRuleConfigurations() {
        Collection<EncryptTableRuleConfiguration> result = new LinkedList<>();
        Collection<EncryptColumnConfiguration> columns = new LinkedList<>();
        columns.add(new EncryptColumnConfiguration("user_name", "user_name_plain", "user_name", "", "name_encryptror"));
        columns.add(new EncryptColumnConfiguration("pwd", "", "pwd", "assisted_query_pwd", "pwd_encryptror"));
        result.add(new EncryptTableRuleConfiguration("t_user", columns));
        return result;
    }
}
