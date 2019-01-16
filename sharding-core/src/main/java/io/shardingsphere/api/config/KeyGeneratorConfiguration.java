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

package io.shardingsphere.api.config;

import com.google.common.base.Strings;
import io.shardingsphere.core.keygen.KeyGeneratorFactory;
import io.shardingsphere.core.keygen.generator.KeyGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Properties;

/**
 * Key generator configuration.
 *
 * @author panjuan
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public final class KeyGeneratorConfiguration {
    
    private String column;
    
    private String type;
    
    private Properties props = new Properties();
    
    /**
     * Build key generator configuration.
     *
     * @return table rule configuration
     */
    public KeyGenerator getKeyGenerator() {
        if (Strings.isNullOrEmpty(type)) {
            return null;
        }
        KeyGenerator result = KeyGeneratorFactory.newInstance(type);
        result.setProperties(props);
        return result;
    }
}
