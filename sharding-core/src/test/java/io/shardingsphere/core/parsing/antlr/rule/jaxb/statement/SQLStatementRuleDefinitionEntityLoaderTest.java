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

package io.shardingsphere.core.parsing.antlr.rule.jaxb.statement;

import io.shardingsphere.core.parsing.antlr.rule.jaxb.entity.statement.SQLStatementRuleDefinitionEntity;
import io.shardingsphere.core.parsing.antlr.rule.jaxb.loader.statement.SQLStatementRuleDefinitionEntityLoader;
import org.junit.Test;

import javax.xml.bind.JAXBException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class SQLStatementRuleDefinitionEntityLoaderTest {
    
    @Test
    public void assertLoadForMySQL() throws JAXBException {
        SQLStatementRuleDefinitionEntity actual = new SQLStatementRuleDefinitionEntityLoader().load("parsing-rule-definition/mysql/sql-statement-rule-definition.xml");
        assertThat(actual.getBasePackage(), is("io.shardingsphere.core.parsing.antlr"));
        assertThat(actual.getOptimizerBasePackage(), is("optimizer.impl"));
        assertThat(actual.getRules().size(), is(7));
    }
    
    @Test
    public void assertLoadForPostgreSQL() throws JAXBException {
        SQLStatementRuleDefinitionEntity actual = new SQLStatementRuleDefinitionEntityLoader().load("parsing-rule-definition/postgresql/sql-statement-rule-definition.xml");
        assertThat(actual.getBasePackage(), is("io.shardingsphere.core.parsing.antlr"));
        assertThat(actual.getOptimizerBasePackage(), is("optimizer.impl"));
        assertThat(actual.getRules().size(), is(8));
    }
    
    @Test
    public void assertLoadForOracle() throws JAXBException {
        SQLStatementRuleDefinitionEntity actual = new SQLStatementRuleDefinitionEntityLoader().load("parsing-rule-definition/oracle/sql-statement-rule-definition.xml");
        assertThat(actual.getBasePackage(), is("io.shardingsphere.core.parsing.antlr"));
        assertThat(actual.getOptimizerBasePackage(), is("optimizer.impl"));
        assertThat(actual.getRules().size(), is(7));
    }
    
    @Test
    public void assertLoadForSQLServer() throws JAXBException {
        SQLStatementRuleDefinitionEntity actual = new SQLStatementRuleDefinitionEntityLoader().load("parsing-rule-definition/sqlserver/sql-statement-rule-definition.xml");
        assertThat(actual.getBasePackage(), is("io.shardingsphere.core.parsing.antlr"));
        assertThat(actual.getOptimizerBasePackage(), is("optimizer.impl"));
        assertThat(actual.getRules().size(), is(7));
    }
}
