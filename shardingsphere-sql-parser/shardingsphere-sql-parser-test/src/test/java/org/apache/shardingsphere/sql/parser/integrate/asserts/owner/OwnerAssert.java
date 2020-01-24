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

package org.apache.shardingsphere.sql.parser.integrate.asserts.owner;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.integrate.asserts.SQLSegmentAssert;
import org.apache.shardingsphere.sql.parser.integrate.asserts.SQLStatementAssertMessage;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.owner.ExpectedSchemaOwner;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.owner.ExpectedTableOwner;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.SchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.test.sql.SQLCaseType;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Owner assert.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OwnerAssert {
    
    /**
     * Assert actual table segment is correct with expected table owner.
     * 
     * @param assertMessage assert message
     * @param actual actual table segment
     * @param expected expected table owner
     * @param sqlCaseType SQL case type
     */
    public static void assertTable(final SQLStatementAssertMessage assertMessage, final TableSegment actual, final ExpectedTableOwner expected, final SQLCaseType sqlCaseType) {
        assertThat(assertMessage.getText("Owner name assertion error: "), actual.getTableName(), is(expected.getName()));
        assertThat(assertMessage.getText("Owner name start delimiter assertion error: "), actual.getTableQuoteCharacter().getStartDelimiter(), is(expected.getStartDelimiter()));
        assertThat(assertMessage.getText("Owner name end delimiter assertion error: "), actual.getTableQuoteCharacter().getEndDelimiter(), is(expected.getEndDelimiter()));
        SQLSegmentAssert.assertIs(assertMessage, actual, expected, sqlCaseType);
    }
    
    /**
     * Assert actual schema segment is correct with expected schema owner.
     *
     * @param assertMessage assert message
     * @param actual actual schema segment
     * @param expected expected schema owner
     * @param sqlCaseType SQL case type
     */
    public static void assertSchema(final SQLStatementAssertMessage assertMessage, final SchemaSegment actual, final ExpectedSchemaOwner expected, final SQLCaseType sqlCaseType) {
        assertThat(assertMessage.getText("Owner name assertion error: "), actual.getName(), is(expected.getName()));
        assertThat(assertMessage.getText("Owner start delimiter assertion error: "), actual.getQuoteCharacter().getStartDelimiter(), is(expected.getStartDelimiter()));
        assertThat(assertMessage.getText("Owner end delimiter assertion error: "), actual.getQuoteCharacter().getEndDelimiter(), is(expected.getEndDelimiter()));
        SQLSegmentAssert.assertIs(assertMessage, actual, expected, sqlCaseType);
    }
}
