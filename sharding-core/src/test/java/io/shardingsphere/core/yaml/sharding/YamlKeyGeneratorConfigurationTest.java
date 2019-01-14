/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.yaml.sharding;

import io.shardingsphere.api.config.KeyGeneratorConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class YamlKeyGeneratorConfigurationTest {
    
    private Properties props = new Properties();
    
    private KeyGeneratorConfiguration keyGeneratorConfiguration;
    
    @Before
    public void setUp() {
        props.setProperty("key1", "value1");
        keyGeneratorConfiguration = new KeyGeneratorConfiguration("order_id", "UUID", props);
    }
    
    @Test
    public void createYamlKeyGeneratorConfiguration() {
        YamlKeyGeneratorConfiguration yamlKeyGeneratorConfiguration = new YamlKeyGeneratorConfiguration(keyGeneratorConfiguration);
        assertThat(yamlKeyGeneratorConfiguration.getColumn(), is("order_id"));
        assertThat(yamlKeyGeneratorConfiguration.getType(), is("UUID"));
    }
    
    @Test
    public void getKeyGeneratorConfiguration() {
        YamlKeyGeneratorConfiguration yamlKeyGeneratorConfiguration = new YamlKeyGeneratorConfiguration();
        yamlKeyGeneratorConfiguration.setColumn("order_id");
        yamlKeyGeneratorConfiguration.setType("UUID");
        yamlKeyGeneratorConfiguration.setProps(props);
        KeyGeneratorConfiguration keyGeneratorConfiguration = yamlKeyGeneratorConfiguration.getKeyGeneratorConfiguration();
        assertThat(keyGeneratorConfiguration.getColumn(), is(this.keyGeneratorConfiguration.getColumn()));
        assertThat(keyGeneratorConfiguration.getType(), is(this.keyGeneratorConfiguration.getType()));
        assertThat(keyGeneratorConfiguration.getProps().getProperty("key1"), is(this.keyGeneratorConfiguration.getProps().getProperty("key1")));
    }
}
