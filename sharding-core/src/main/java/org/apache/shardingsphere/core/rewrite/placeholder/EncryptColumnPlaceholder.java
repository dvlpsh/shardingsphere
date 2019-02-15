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

package org.apache.shardingsphere.core.rewrite.placeholder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.constant.ShardingOperator;

import java.util.Collection;
import java.util.Map;

/**
 * Encrypt Column placeholder for rewrite.
 *
 * @author panjuan
 */
@RequiredArgsConstructor
@Getter
public final class EncryptColumnPlaceholder implements ShardingPlaceholder {
    
    private final String logicTableName;
    
    private final String columnName;
    
    private final Map<Integer, Comparable<?>> indexValues;
    
    private final Collection<Integer> placeholderIndex;
    
    private final ShardingOperator operator;
    
    @Override
    public String toString() {
        switch (operator) {
            case EQUAL:
                return placeholderIndex.isEmpty() ? String.format("%s %s %s", columnName, operator.name(), indexValues.get(0)) : String.format("%s = ?", columnName);
            case BETWEEN:
                return toStringFromBetween();
            case IN:
                
    
        }
       
    }
    
    private String toStringFromBetween() {
        if (placeholderIndex.isEmpty()) {
            return String.format("%s %s %s", indexValues.get(0), operator.name(), indexValues.get(1));
        }
        if (2 == placeholderIndex.size()) {
            return String.format("? %s ?", operator.name());
        }
        if (0 == placeholderIndex.iterator().next()) {
            return String.format("? %s %s", operator.name(), indexValues.get(0));
        }
        return String.format("%s %s ?", indexValues.get(0), operator.name());
    }
}
