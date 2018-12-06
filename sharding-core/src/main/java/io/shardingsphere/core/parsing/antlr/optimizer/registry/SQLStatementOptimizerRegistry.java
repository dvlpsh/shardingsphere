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

package io.shardingsphere.core.parsing.antlr.optimizer.registry;

import io.shardingsphere.core.parsing.antlr.parser.SQLStatementType;
import io.shardingsphere.core.parsing.antlr.optimizer.impl.SQLStatementOptimizer;

/**
 * SQL statement optimizer registry.
 * 
 * @author zhangliang
 */
public interface SQLStatementOptimizerRegistry {
    
    /**
     * Get SQL statement optimizer.
     * 
     * @param type SQL statement type
     * @return SQL statement optimizer
     */
    SQLStatementOptimizer getOptimizer(SQLStatementType type);
}
