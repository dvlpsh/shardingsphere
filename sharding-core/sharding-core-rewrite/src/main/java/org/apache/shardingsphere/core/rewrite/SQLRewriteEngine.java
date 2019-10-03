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

package org.apache.shardingsphere.core.rewrite;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.optimize.segment.insert.InsertValueContext;
import org.apache.shardingsphere.core.optimize.statement.SQLStatementContext;
import org.apache.shardingsphere.core.optimize.statement.impl.InsertSQLStatementContext;
import org.apache.shardingsphere.core.rewrite.builder.sql.SQLBuilder;
import org.apache.shardingsphere.core.rewrite.constant.EncryptDerivedColumnType;
import org.apache.shardingsphere.core.rewrite.constant.ShardingDerivedColumnType;
import org.apache.shardingsphere.core.rewrite.parameter.builder.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.parameter.builder.ParameterBuilderFactory;
import org.apache.shardingsphere.core.rewrite.parameter.rewriter.encrypt.EncryptParameterBuilderFactory;
import org.apache.shardingsphere.core.rewrite.parameter.rewriter.sharding.ShardingParameterBuilderFactory;
import org.apache.shardingsphere.core.rewrite.token.SQLTokenGenerators;
import org.apache.shardingsphere.core.rewrite.token.builder.BaseTokenGeneratorBuilder;
import org.apache.shardingsphere.core.rewrite.token.builder.EncryptTokenGenerateBuilder;
import org.apache.shardingsphere.core.rewrite.token.builder.ShardingTokenGenerateBuilder;
import org.apache.shardingsphere.core.rewrite.token.pojo.SQLToken;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.route.SQLUnit;
import org.apache.shardingsphere.core.route.router.sharding.keygen.GeneratedKey;
import org.apache.shardingsphere.core.route.type.RoutingUnit;
import org.apache.shardingsphere.core.rule.BaseRule;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.rule.MasterSlaveRule;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.strategy.encrypt.EncryptTable;
import org.apache.shardingsphere.spi.encrypt.ShardingEncryptor;
import org.apache.shardingsphere.spi.encrypt.ShardingQueryAssistedEncryptor;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * SQL rewrite engine.
 * 
 * @author panjuan
 * @author zhangliang
 */
public final class SQLRewriteEngine {
    
    private final BaseRule baseRule;
            
    private final SQLStatementContext sqlStatementContext;
    
    private final List<SQLToken> sqlTokens;
    
    private final SQLBuilder sqlBuilder;
    
    private final ParameterBuilder parameterBuilder;
    
    public SQLRewriteEngine(final ShardingRule shardingRule, final TableMetas tableMetas, 
                            final SQLRouteResult sqlRouteResult, final String sql, final List<Object> parameters, final boolean isQueryWithCipherColumn) {
        baseRule = shardingRule;
        sqlStatementContext = sqlRouteResult.getSqlStatementContext();
        parameterBuilder = createParameterBuilder(tableMetas, sqlRouteResult, parameters, isQueryWithCipherColumn);
        if (sqlRouteResult.getSqlStatementContext() instanceof InsertSQLStatementContext) {
            processGeneratedKey((InsertSQLStatementContext) sqlRouteResult.getSqlStatementContext(), sqlRouteResult.getGeneratedKey().orNull());
            processEncrypt((InsertSQLStatementContext) sqlRouteResult.getSqlStatementContext(), shardingRule.getEncryptRule());
        }
        sqlTokens = createSQLTokensForSharding(tableMetas, parameters, sqlRouteResult, isQueryWithCipherColumn);
        sqlBuilder = new SQLBuilder(sql, sqlTokens);
    }
    
    public SQLRewriteEngine(final EncryptRule encryptRule, final TableMetas tableMetas,
                            final SQLStatementContext sqlStatementContext, final String sql, final List<Object> parameters, final boolean isQueryWithCipherColumn) {
        baseRule = encryptRule;
        this.sqlStatementContext = sqlStatementContext;
        parameterBuilder = createParameterBuilder(tableMetas, sqlStatementContext, parameters, isQueryWithCipherColumn);
        if (sqlStatementContext instanceof InsertSQLStatementContext) {
            processEncrypt((InsertSQLStatementContext) sqlStatementContext, encryptRule);
        }
        sqlTokens = createSQLTokensForEncrypt(tableMetas, parameters, isQueryWithCipherColumn);
        sqlBuilder = new SQLBuilder(sql, sqlTokens);
    }
    
    public SQLRewriteEngine(final MasterSlaveRule masterSlaveRule, final SQLStatementContext sqlStatementContext, final String sql) {
        baseRule = masterSlaveRule;
        this.sqlStatementContext = sqlStatementContext;
        parameterBuilder = ParameterBuilderFactory.newInstance(Collections.emptyList(), sqlStatementContext);
        sqlTokens = createSQLTokensForEncrypt(null, Collections.emptyList(), false);
        sqlBuilder = new SQLBuilder(sql, sqlTokens);
    }
    
    private void processGeneratedKey(final InsertSQLStatementContext insertSQLStatementContext, final GeneratedKey generatedKey) {
        if (null != generatedKey && generatedKey.isGenerated()) {
            Iterator<Comparable<?>> generatedValues = generatedKey.getGeneratedValues().descendingIterator();
            for (InsertValueContext each : insertSQLStatementContext.getInsertValueContexts()) {
                each.appendValue(generatedValues.next(), ShardingDerivedColumnType.KEY_GEN);
            }
        }
    }
    
    private void processEncrypt(final InsertSQLStatementContext insertSQLStatementContext, final EncryptRule encryptRule) {
        String tableName = insertSQLStatementContext.getTablesContext().getSingleTableName();
        Optional<EncryptTable> encryptTable = encryptRule.findEncryptTable(tableName);
        if (!encryptTable.isPresent()) {
            return;
        }
        for (String each : encryptTable.get().getLogicColumns()) {
            Optional<ShardingEncryptor> shardingEncryptor = encryptRule.findShardingEncryptor(tableName, each);
            if (shardingEncryptor.isPresent()) {
                encryptInsertValues(insertSQLStatementContext, encryptRule, shardingEncryptor.get(), tableName, each);
            }
        }
    }
    
    private void encryptInsertValues(final InsertSQLStatementContext insertSQLStatementContext, final EncryptRule encryptRule, final ShardingEncryptor shardingEncryptor,
                                     final String tableName, final String encryptLogicColumnName) {
        int columnIndex = insertSQLStatementContext.getColumnNames().indexOf(encryptLogicColumnName);
        for (InsertValueContext each : insertSQLStatementContext.getInsertValueContexts()) {
            encryptInsertValue(encryptRule, shardingEncryptor, tableName, columnIndex, each, encryptLogicColumnName);
        }
    }
    
    private void encryptInsertValue(final EncryptRule encryptRule, final ShardingEncryptor shardingEncryptor,
                                    final String tableName, final int columnIndex, final InsertValueContext insertValueContext, final String encryptLogicColumnName) {
        Object originalValue = insertValueContext.getValue(columnIndex);
        insertValueContext.setValue(columnIndex, shardingEncryptor.encrypt(originalValue));
        if (shardingEncryptor instanceof ShardingQueryAssistedEncryptor) {
            Optional<String> assistedColumnName = encryptRule.findAssistedQueryColumn(tableName, encryptLogicColumnName);
            Preconditions.checkArgument(assistedColumnName.isPresent(), "Can not find assisted query Column Name");
            insertValueContext.appendValue(((ShardingQueryAssistedEncryptor) shardingEncryptor).queryAssistedEncrypt(originalValue.toString()), EncryptDerivedColumnType.ENCRYPT);
        }
        if (encryptRule.findPlainColumn(tableName, encryptLogicColumnName).isPresent()) {
            insertValueContext.appendValue(originalValue, EncryptDerivedColumnType.ENCRYPT);
        }
    }
    
    private ParameterBuilder createParameterBuilder(final TableMetas tableMetas, final SQLRouteResult sqlRouteResult, final List<Object> parameters, final boolean isQueryWithCipherColumn) {
        ParameterBuilder result = ParameterBuilderFactory.newInstance(parameters, sqlRouteResult);
        ShardingParameterBuilderFactory.build(result, (ShardingRule) baseRule, tableMetas, sqlRouteResult, parameters);
        EncryptParameterBuilderFactory.build(result, ((ShardingRule) baseRule).getEncryptRule(), tableMetas, sqlRouteResult.getSqlStatementContext(), parameters, isQueryWithCipherColumn);
        return result;
    }
    
    private ParameterBuilder createParameterBuilder(final TableMetas tableMetas, final SQLStatementContext sqlStatementContext, final List<Object> parameters, final boolean isQueryWithCipherColumn) {
        ParameterBuilder result = ParameterBuilderFactory.newInstance(parameters, sqlStatementContext);
        EncryptParameterBuilderFactory.build(result, (EncryptRule) baseRule, tableMetas, sqlStatementContext, parameters, isQueryWithCipherColumn);
        return result;
    }
    
    private List<SQLToken> createSQLTokensForSharding(final TableMetas tableMetas, final List<Object> parameters, final SQLRouteResult sqlRouteResult, final boolean isQueryWithCipherColumn) {
        SQLTokenGenerators sqlTokenGenerators = new SQLTokenGenerators();
        sqlTokenGenerators.addAll(new BaseTokenGeneratorBuilder().getSQLTokenGenerators());
        sqlTokenGenerators.addAll(new ShardingTokenGenerateBuilder((ShardingRule) baseRule, sqlRouteResult).getSQLTokenGenerators());
        sqlTokenGenerators.addAll(new EncryptTokenGenerateBuilder(((ShardingRule) baseRule).getEncryptRule(), isQueryWithCipherColumn).getSQLTokenGenerators());
        return sqlTokenGenerators.generateSQLTokens(sqlStatementContext, parameters, tableMetas, sqlRouteResult.getRoutingResult().isSingleRouting());
    }
    
    private List<SQLToken> createSQLTokensForEncrypt(final TableMetas tableMetas, final List<Object> parameters, final boolean isQueryWithCipherColumn) {
        SQLTokenGenerators sqlTokenGenerators = new SQLTokenGenerators();
        sqlTokenGenerators.addAll(new BaseTokenGeneratorBuilder().getSQLTokenGenerators());
        sqlTokenGenerators.addAll(new EncryptTokenGenerateBuilder((EncryptRule) baseRule, isQueryWithCipherColumn).getSQLTokenGenerators());
        return sqlTokenGenerators.generateSQLTokens(sqlStatementContext, parameters, tableMetas, true);
    }
    
    /**
     * Generate SQL.
     * 
     * @return SQL unit
     */
    public SQLUnit generateSQL() {
        return new SQLUnit(sqlBuilder.toSQL(), parameterBuilder.getParameters());
    }
    
    /**
     * Generate SQL.
     * 
     * @param routingUnit routing unit
     * @param logicAndActualTables logic and actual tables
     * @return SQL unit
     */
    public SQLUnit generateSQL(final RoutingUnit routingUnit, final Map<String, String> logicAndActualTables) {
        return new SQLUnit(sqlBuilder.toSQL(routingUnit, logicAndActualTables), parameterBuilder.getParameters(routingUnit));
    }
}
