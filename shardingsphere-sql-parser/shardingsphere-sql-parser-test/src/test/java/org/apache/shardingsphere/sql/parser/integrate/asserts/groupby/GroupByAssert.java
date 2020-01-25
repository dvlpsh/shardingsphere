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

package org.apache.shardingsphere.sql.parser.integrate.asserts.groupby;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.integrate.asserts.SQLStatementAssertMessage;
import org.apache.shardingsphere.sql.parser.integrate.asserts.orderby.OrderByItemAssert;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.orderby.ExpectedOrderBy;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.test.sql.SQLCaseType;

/**
 * Group by assert.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GroupByAssert {
    
    /**
     * Assert actual group by segment is correct with expected group by.
     * 
     * @param assertMessage assert message
     * @param actual actual group by segment
     * @param expected expected group by
     * @param sqlCaseType SQL case type
     */
    public static void assertIs(final SQLStatementAssertMessage assertMessage, final GroupBySegment actual, final ExpectedOrderBy expected, final SQLCaseType sqlCaseType) {
        OrderByItemAssert.assertIs(assertMessage, actual.getGroupByItems(), expected, sqlCaseType, "Group by");
    }
}
