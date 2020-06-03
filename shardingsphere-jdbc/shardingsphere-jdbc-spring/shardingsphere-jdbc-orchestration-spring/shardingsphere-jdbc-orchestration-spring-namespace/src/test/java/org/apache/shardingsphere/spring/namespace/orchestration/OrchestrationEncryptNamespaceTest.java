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

package org.apache.shardingsphere.spring.namespace.orchestration;

import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.encrypt.api.config.EncryptColumnConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptorConfiguration;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.spring.namespace.orchestration.util.EmbedTestingServer;
import org.apache.shardingsphere.spring.namespace.orchestration.util.FieldValueUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(locations = "classpath:META-INF/rdb/encryptOrchestration.xml")
public class OrchestrationEncryptNamespaceTest extends AbstractJUnit4SpringContextTests {
    
    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
    }
    
    @Test
    public void assertEncryptDataSourceType() {
        assertNotNull(applicationContext.getBean("encryptDataSourceOrchestration", OrchestrationSpringShardingSphereDataSource.class));
        assertEncryptRule(getEncryptRuleConfiguration());
    }
    
    private EncryptRuleConfiguration getEncryptRuleConfiguration() {
        OrchestrationSpringShardingSphereDataSource orchestrationDataSource = (OrchestrationSpringShardingSphereDataSource) applicationContext.getBean("encryptDataSourceOrchestration");
        ShardingSphereDataSource dataSource = (ShardingSphereDataSource) FieldValueUtil.getFieldValue(orchestrationDataSource, "dataSource", true);
        return (EncryptRuleConfiguration) dataSource.getSchemaContexts().getDefaultSchemaContext().getSchema().getConfigurations().iterator().next();
    }
    
    private void assertEncryptRule(final EncryptRuleConfiguration configuration) {
        assertThat(configuration.getEncryptors().size(), is(2));
        assertThat(configuration.getTables().size(), is(1));
        EncryptTableRuleConfiguration tableRuleConfiguration = configuration.getTables().iterator().next();
        Iterator<EncryptColumnConfiguration> encryptColumnConfigurations = tableRuleConfiguration.getColumns().iterator();
        EncryptColumnConfiguration userIdColumn = encryptColumnConfigurations.next();
        EncryptColumnConfiguration orderIdColumn = encryptColumnConfigurations.next();
        assertThat(userIdColumn.getCipherColumn(), is("user_encrypt"));
        assertThat(orderIdColumn.getPlainColumn(), is("order_decrypt"));
        Iterator<EncryptorConfiguration> encryptors = configuration.getEncryptors().iterator();
        EncryptorConfiguration aesEncryptorConfig = encryptors.next();
        assertThat(aesEncryptorConfig.getType(), is("AES"));
        assertThat(aesEncryptorConfig.getProperties().getProperty("aes.key.value"), is("123456"));
        EncryptorConfiguration md5EncryptorConfig = encryptors.next();
        assertThat(md5EncryptorConfig.getName(), is("encryptor_md5"));
        assertThat(md5EncryptorConfig.getType(), is("MD5"));
    }
    
    @Test
    public void assertProperties() {
        boolean showSQL = getProperties("encryptDataSourceOrchestration").getValue(ConfigurationPropertyKey.SQL_SHOW);
        boolean queryWithCipherColumn = getProperties("encryptDataSourceOrchestration").getValue(ConfigurationPropertyKey.QUERY_WITH_CIPHER_COLUMN);
        assertTrue(showSQL);
        assertFalse(queryWithCipherColumn);
    }
    
    private ConfigurationProperties getProperties(final String encryptDatasourceName) {
        OrchestrationSpringShardingSphereDataSource orchestrationDataSource = applicationContext.getBean(encryptDatasourceName, OrchestrationSpringShardingSphereDataSource.class);
        ShardingSphereDataSource dataSource = (ShardingSphereDataSource) FieldValueUtil.getFieldValue(orchestrationDataSource, "dataSource", true);
        return dataSource.getSchemaContexts().getProperties();
    }
}
