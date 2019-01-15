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

package io.shardingsphere.core.keygen.generator.impl;

import io.shardingsphere.core.keygen.generator.KeyGenerator;
import lombok.Getter;
import lombok.Setter;

import java.util.Properties;
import java.util.UUID;

/**
 * UUID key generator.
 *
 * @author panjuan
 */
@Getter
@Setter
public final class UUIDKeyGenerator implements KeyGenerator {
    
    private Properties properties = new Properties();
    
    @Override
    public String getType() {
        return "UUID";
    }
    
    @Override
    public synchronized Comparable<?> generateKey() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
