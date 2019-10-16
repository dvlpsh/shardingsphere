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

package org.apache.shardingsphere.core.rewrite.sql.impl;

import org.apache.shardingsphere.core.constant.ShardingOperator;
import org.apache.shardingsphere.core.rewrite.feature.encrypt.token.pojo.EncryptPredicateToken;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class EncryptPredicateTokenTest {
    
    @Test
    public void assertToStringWithoutPlaceholderWithoutTableOwnerWithEqual() {
        Map<Integer, Object> indexValues = new LinkedHashMap<>();
        indexValues.put(0, "a");
        EncryptPredicateToken actual = new EncryptPredicateToken(0, 0, null, "column_x", indexValues, Collections.<Integer>emptyList(), ShardingOperator.EQUAL);
        assertThat(actual.toString(), is("column_x = 'a'"));
    }

    @Test
    public void assertToStringWithoutPlaceholderWithTableOwnerWithEqual() {
        Map<Integer, Object> indexValues = new LinkedHashMap<>();
        indexValues.put(0, "a");
        EncryptPredicateToken actual = new EncryptPredicateToken(0, 0, "table_x", "column_x", indexValues, Collections.<Integer>emptyList(), ShardingOperator.EQUAL);
        assertThat(actual.toString(), is("table_x.column_x = 'a'"));
    }

    @Test
    public void assertToStringWithPlaceholderWithoutTableOwnerWithEqual() {
        EncryptPredicateToken actual = new EncryptPredicateToken(0, 0, null, "column_x", Collections.<Integer, Object>emptyMap(), Collections.singletonList(0), ShardingOperator.EQUAL);
        assertThat(actual.toString(), is("column_x = ?"));
    }
    
    @Test
    public void assertToStringWithPlaceholderWithTableOwnerWithEqual() {
        EncryptPredicateToken actual = new EncryptPredicateToken(0, 0, "table_x", "column_x", Collections.<Integer, Object>emptyMap(), Collections.singletonList(0), ShardingOperator.EQUAL);
        assertThat(actual.toString(), is("table_x.column_x = ?"));
    }

    @Test
    public void assertToStringWithoutPlaceholderWithoutTableOwnerWithIn() {
        Map<Integer, Object> indexValues = new LinkedHashMap<>();
        indexValues.put(0, "a");
        indexValues.put(1, "b");
        EncryptPredicateToken actual = new EncryptPredicateToken(0, 0, null, "column_x", indexValues, Collections.<Integer>emptyList(), ShardingOperator.IN);
        assertThat(actual.toString(), is("column_x IN ('a', 'b')"));
    }
    
    @Test
    public void assertToStringWithoutPlaceholderWithTableOwnerWithIn() {
        Map<Integer, Object> indexValues = new LinkedHashMap<>();
        indexValues.put(0, "a");
        indexValues.put(1, "b");
        EncryptPredicateToken actual = new EncryptPredicateToken(0, 0, "table_x", "column_x", indexValues, Collections.<Integer>emptyList(), ShardingOperator.IN);
        assertThat(actual.toString(), is("table_x.column_x IN ('a', 'b')"));
    }

    @Test
    public void assertToStringWithPlaceholderWithoutTableOwnerWithIn() {
        EncryptPredicateToken actual = new EncryptPredicateToken(0, 0, null, "column_x", Collections.<Integer, Object>emptyMap(), Collections.singletonList(0), ShardingOperator.IN);
        assertThat(actual.toString(), is("column_x IN (?)"));
    }
    
    @Test
    public void assertToStringWithPlaceholderWithTableOwnerWithIn() {
        EncryptPredicateToken actual = new EncryptPredicateToken(0, 0, "table_x", "column_x", Collections.<Integer, Object>emptyMap(), Collections.singletonList(0), ShardingOperator.IN);
        assertThat(actual.toString(), is("table_x.column_x IN (?)"));
    }
}
