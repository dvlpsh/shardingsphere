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

package io.shardingsphere.core.parsing.antlr.rule.registry.statement;

import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import io.shardingsphere.core.parsing.antlr.optimizer.impl.SQLStatementOptimizer;
import io.shardingsphere.core.parsing.antlr.rule.jaxb.entity.statement.SQLStatementRuleDefinitionEntity;
import io.shardingsphere.core.parsing.antlr.rule.jaxb.entity.statement.SQLStatementRuleEntity;
import io.shardingsphere.core.parsing.antlr.rule.registry.segment.SQLSegmentRule;
import io.shardingsphere.core.parsing.antlr.rule.registry.segment.SQLSegmentRuleDefinition;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * SQL statement rule definition.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public final class SQLStatementRuleDefinition {
    
    private final Map<String, SQLStatementRule> rules = new LinkedHashMap<>();
    
    /**
     * Initialize SQL statement rule definition.
     * 
     * @param dialectRuleDefinitionEntity SQL dialect statement rule definition entity
     * @param sqlSegmentRuleDefinition SQL segment rule definition
     */
    @SuppressWarnings("unchecked")
    @SneakyThrows
    public void init(final SQLStatementRuleDefinitionEntity dialectRuleDefinitionEntity, final SQLSegmentRuleDefinition sqlSegmentRuleDefinition) {
        for (SQLStatementRuleEntity each : dialectRuleDefinitionEntity.getRules()) {
            SQLStatementRule sqlStatementRule = new SQLStatementRule(each.getContext(), 
                    (Class<? extends SQLStatement>) Class.forName(each.getSqlStatementClass()),
                    (SQLStatementOptimizer) newClassInstance(dialectRuleDefinitionEntity.getBasePackage(), dialectRuleDefinitionEntity.getOptimizerBasePackage(), each.getOptimizerClass()));
            sqlStatementRule.getSqlSegmentRules().addAll(createSQLSegmentRules(each.getSqlSegmentRuleRefs(), sqlSegmentRuleDefinition));
            rules.put(getContextClassName(each.getContext()), sqlStatementRule);
        }
    }
    
    private Collection<SQLSegmentRule> createSQLSegmentRules(final String sqlSegmentRuleRefs, final SQLSegmentRuleDefinition sqlSegmentRuleDefinition) {
        Collection<SQLSegmentRule> result = new LinkedList<>();
        for (String each : Splitter.on(',').trimResults().splitToList(sqlSegmentRuleRefs)) {
            result.add(sqlSegmentRuleDefinition.getRules().get(each));
        }
        return result;
    }
    
    private String getContextClassName(final String context) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, context + "Context");
    }
    
    @SneakyThrows
    private Object newClassInstance(final String basePackage, final String classPackage, final String className) {
        return Strings.isNullOrEmpty(className) ? null : Class.forName(Joiner.on('.').join(basePackage, classPackage, className)).newInstance();
    }
}
