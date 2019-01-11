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

package io.shardingsphere.core.keygen;

import io.shardingsphere.core.keygen.generator.UUIDKeyGenerator;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class UUIDKeyGeneratorTest {
    
    private UUIDKeyGenerator uuidKeyGenerator = new UUIDKeyGenerator();
    
    @Test
    public void assertGenerateKey() {
        assertThat(((String) uuidKeyGenerator.generateKey()).length(), is(32));
    }
    
    @Test
    public void assertGetProperties() {
        assertThat(uuidKeyGenerator.getProperties().entrySet().size(), is(0));
    }
    
    @Test
    public void assertSetProperties() {
        Properties properties = new Properties();
        properties.setProperty("key1", "value1");
        uuidKeyGenerator.setProperties(properties);
        assertThat(uuidKeyGenerator.getProperties().get("key1"), is((Object) "value1"));
    }
}
