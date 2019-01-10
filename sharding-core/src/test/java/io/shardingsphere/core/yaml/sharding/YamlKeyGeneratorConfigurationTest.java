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

import io.shardingsphere.core.exception.ShardingConfigurationException;
import io.shardingsphere.core.keygen.SnowflakeKeyGenerator;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class YamlKeyGeneratorConfigurationTest {
    
    @Test
    public void assertGetKeyGeneratorWithClassName() {
        YamlKeyGeneratorConfiguration keyGeneratorConfiguration = new YamlKeyGeneratorConfiguration();
        keyGeneratorConfiguration.setClassName("io.shardingsphere.core.keygen.SnowflakeKeyGenerator");
        assertThat(keyGeneratorConfiguration.getKeyGenerator().getClass().getName(), is(SnowflakeKeyGenerator.class.getName()));
    }
    
    @Test
    public void assertGetKeyGeneratorWithSnowflakeType() {
        YamlKeyGeneratorConfiguration keyGeneratorConfiguration = new YamlKeyGeneratorConfiguration();
        keyGeneratorConfiguration.setType("SNOWFLAKE");
        assertThat(keyGeneratorConfiguration.getKeyGenerator().getClass().getName(), is(SnowflakeKeyGenerator.class.getName()));
    }
    
    @Test
    public void assertGetKeyGeneratorWithoutTypeAndClassName() {
        YamlKeyGeneratorConfiguration keyGeneratorConfiguration = new YamlKeyGeneratorConfiguration();
        assertThat(keyGeneratorConfiguration.getKeyGenerator().getClass().getName(), is(SnowflakeKeyGenerator.class.getName()));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertGetKeyGeneratorClassNameWithLeaf() {
        YamlKeyGeneratorConfiguration keyGeneratorConfiguration = new YamlKeyGeneratorConfiguration();
        keyGeneratorConfiguration.setType("LEAF");
        keyGeneratorConfiguration.getKeyGenerator();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertGetKeyGeneratorClassNameWithUUID() {
        YamlKeyGeneratorConfiguration keyGeneratorConfiguration = new YamlKeyGeneratorConfiguration();
        keyGeneratorConfiguration.setType("UUID");
        keyGeneratorConfiguration.getKeyGenerator();
    }
    
    @Test(expected = ShardingConfigurationException.class)
    public void assertGetKeyGeneratorClassNameWithException() {
        YamlKeyGeneratorConfiguration keyGeneratorConfiguration = new YamlKeyGeneratorConfiguration();
        keyGeneratorConfiguration.setType("DEFAULT");
        keyGeneratorConfiguration.getKeyGenerator();
    }
}
