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

package org.apache.shardingsphere.core.parse;

import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.extractor.SQLSegmentsExtractorEngine;
import org.apache.shardingsphere.core.parse.filler.SQLStatementFillerEngine;
import org.apache.shardingsphere.core.parse.optimizer.SQLStatementOptimizerEngine;
import org.apache.shardingsphere.core.parse.parser.SQLAST;
import org.apache.shardingsphere.core.parse.parser.SQLParserEngine;
import org.apache.shardingsphere.core.parse.rule.registry.EncryptParsingRuleRegistry;
import org.apache.shardingsphere.core.parse.rule.registry.ParsingRuleRegistry;
import org.apache.shardingsphere.core.parse.rule.registry.ShardingParsingRuleRegistry;
import org.apache.shardingsphere.core.parse.sql.segment.SQLSegment;
import org.apache.shardingsphere.core.parse.sql.statement.GeneralSQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.rule.BaseRule;
import org.apache.shardingsphere.core.rule.EncryptRule;

import java.util.Collection;

/**
 * SQL parse engine.
 *
 * @author duhongjun
 * @author zhangliang
 */
public final class SQLParseEngine {
    
    private final ParsingRuleRegistry parsingRuleRegistry;
    
    private final SQLParserEngine parserEngine;
    
    private final SQLSegmentsExtractorEngine extractorEngine;
    
    private final SQLStatementFillerEngine fillerEngine;
    
    private final SQLStatementOptimizerEngine optimizerEngine;
    
    public SQLParseEngine(final DatabaseType databaseType, final String sql, final BaseRule rule, final ShardingTableMetaData shardingTableMetaData) {
        parsingRuleRegistry = rule instanceof EncryptRule ? EncryptParsingRuleRegistry.getInstance() : ShardingParsingRuleRegistry.getInstance();
        parserEngine = new SQLParserEngine(parsingRuleRegistry, databaseType, sql);
        extractorEngine = new SQLSegmentsExtractorEngine();
        fillerEngine = new SQLStatementFillerEngine(parsingRuleRegistry, databaseType, sql, rule, shardingTableMetaData);
        optimizerEngine = new SQLStatementOptimizerEngine(shardingTableMetaData);
    }
    
    /**
     * Parse SQL.
     *
     * @return SQL statement
     */
    public SQLStatement parse() {
        SQLAST ast = parserEngine.parse();
        if (!ast.getSQLStatementRule().isPresent() && (parsingRuleRegistry instanceof EncryptParsingRuleRegistry)) {
            return new GeneralSQLStatement();
        }
        Collection<SQLSegment> sqlSegments = extractorEngine.extract(ast);
        SQLStatement result = fillerEngine.fill(sqlSegments, ast.getSQLStatementRule().get());
        optimizerEngine.optimize(ast.getSQLStatementRule().get(), result);
        return result;
    }
}
