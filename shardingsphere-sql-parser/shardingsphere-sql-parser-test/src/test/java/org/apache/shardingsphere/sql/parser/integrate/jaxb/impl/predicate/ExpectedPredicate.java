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

package org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.predicate;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.generic.AbstractExpectedSegment;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.predicate.value.ExpectedPredicateBetweenRightValue;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.predicate.value.ExpectedPredicateCompareRightValue;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.predicate.value.ExpectedPredicateInRightValue;
import org.apache.shardingsphere.sql.parser.integrate.jaxb.impl.predicate.value.ExpectedPredicateRightValue;

import javax.xml.bind.annotation.XmlElement;

@Getter
@Setter
public final class ExpectedPredicate extends AbstractExpectedSegment {
    
    @XmlElement
    private ExpectedColumn column;
    
    @XmlElement(name = "predicate-between-right-value")
    private ExpectedPredicateBetweenRightValue betweenRightValue = new ExpectedPredicateBetweenRightValue();
    
    @XmlElement(name = "predicate-in-right-value")
    private ExpectedPredicateInRightValue inRightValue = new ExpectedPredicateInRightValue();
    
    @XmlElement(name = "predicate-compare-right-value")
    private ExpectedPredicateCompareRightValue compareRightValue = new ExpectedPredicateCompareRightValue();
    
    /**
     * Find expected right value type.
     * 
     * @param expectedPredicateRightValue expected predicate right value
     * @param <T> type of expected predicate right value
     * @return right value
     */
    @SuppressWarnings("unchecked")
    public <T extends ExpectedPredicateRightValue> T findExpectedRightValue(final Class<T> expectedPredicateRightValue) {
        if (expectedPredicateRightValue.isAssignableFrom(ExpectedPredicateCompareRightValue.class)) {
            return (T) compareRightValue;
        }
        if (expectedPredicateRightValue.isAssignableFrom(ExpectedPredicateInRightValue.class)) {
            return (T) inRightValue;
        }
        if (expectedPredicateRightValue.isAssignableFrom(ExpectedPredicateBetweenRightValue.class)) {
            return (T) betweenRightValue;
        }
        return null;
    }
}
