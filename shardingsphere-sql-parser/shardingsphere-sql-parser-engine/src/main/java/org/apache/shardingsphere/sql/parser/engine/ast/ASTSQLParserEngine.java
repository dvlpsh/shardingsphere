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

package org.apache.shardingsphere.sql.parser.engine.ast;

import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.shardingsphere.sql.parser.core.parser.SQLParserExecutor;
import org.apache.shardingsphere.sql.parser.engine.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.engine.SQLParsedResultCache;

import java.util.Optional;

/**
 * AST SQL parser engine.
 */
@RequiredArgsConstructor
public final class ASTSQLParserEngine implements SQLParserEngine<ParseTree> {
    
    private final String databaseTypeName;
    
    private final SQLParsedResultCache<ParseTree> cache = new SQLParsedResultCache<>();
    
    @Override
    public ParseTree parse(final String sql, final boolean useCache) {
        if (useCache) {
            Optional<ParseTree> parsedResult = cache.get(sql);
            if (parsedResult.isPresent()) {
                return parsedResult.get();
            }
        }
        ParseTree result = new SQLParserExecutor(databaseTypeName, sql).execute().getRootNode();
        if (useCache) {
            cache.put(sql, result);
        }
        return result;
    }
}
