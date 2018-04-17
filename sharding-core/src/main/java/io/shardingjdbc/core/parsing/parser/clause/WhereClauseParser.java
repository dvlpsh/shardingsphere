/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.core.parsing.parser.clause;

import com.google.common.base.Optional;
import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.token.DefaultKeyword;
import io.shardingjdbc.core.parsing.lexer.token.Keyword;
import io.shardingjdbc.core.parsing.lexer.token.Symbol;
import io.shardingjdbc.core.parsing.parser.clause.expression.AliasExpressionParser;
import io.shardingjdbc.core.parsing.parser.clause.expression.BasicExpressionParser;
import io.shardingjdbc.core.parsing.parser.context.condition.AndCondition;
import io.shardingjdbc.core.parsing.parser.context.condition.Column;
import io.shardingjdbc.core.parsing.parser.context.condition.Condition;
import io.shardingjdbc.core.parsing.parser.context.condition.OrCondition;
import io.shardingjdbc.core.parsing.parser.context.limit.Limit;
import io.shardingjdbc.core.parsing.parser.context.limit.LimitValue;
import io.shardingjdbc.core.parsing.parser.context.selectitem.SelectItem;
import io.shardingjdbc.core.parsing.parser.context.table.Table;
import io.shardingjdbc.core.parsing.parser.context.table.Tables;
import io.shardingjdbc.core.parsing.parser.dialect.ExpressionParserFactory;
import io.shardingjdbc.core.parsing.parser.expression.SQLExpression;
import io.shardingjdbc.core.parsing.parser.expression.SQLIdentifierExpression;
import io.shardingjdbc.core.parsing.parser.expression.SQLNumberExpression;
import io.shardingjdbc.core.parsing.parser.expression.SQLPlaceholderExpression;
import io.shardingjdbc.core.parsing.parser.expression.SQLPropertyExpression;
import io.shardingjdbc.core.parsing.parser.expression.SQLTextExpression;
import io.shardingjdbc.core.parsing.parser.sql.SQLStatement;
import io.shardingjdbc.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingjdbc.core.parsing.parser.token.OffsetToken;
import io.shardingjdbc.core.parsing.parser.token.RowCountToken;
import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.util.SQLUtil;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Where clause parser.
 *
 * @author zhangliang
 * @author maxiaoguang
 */
public class WhereClauseParser implements SQLClauseParser {
    
    private final DatabaseType databaseType;
    
    private final LexerEngine lexerEngine;
    
    private final AliasExpressionParser aliasExpressionParser;
    
    private final BasicExpressionParser basicExpressionParser;
    
    public WhereClauseParser(final DatabaseType databaseType, final LexerEngine lexerEngine) {
        this.databaseType = databaseType;
        this.lexerEngine = lexerEngine;
        aliasExpressionParser = ExpressionParserFactory.createAliasExpressionParser(lexerEngine);
        basicExpressionParser = ExpressionParserFactory.createBasicExpressionParser(lexerEngine);
    }
    
    /**
     * Parse where.
     *
     * @param shardingRule databases and tables sharding rule
     * @param sqlStatement SQL statement
     * @param items select items
     */
    public void parse(final ShardingRule shardingRule, final SQLStatement sqlStatement, final List<SelectItem> items) {
        aliasExpressionParser.parseTableAlias();
        if (lexerEngine.skipIfEqual(DefaultKeyword.WHERE)) {
            parseWhere(shardingRule, sqlStatement, items);
        }
    }
    
    private void parseWhere(final ShardingRule shardingRule, final SQLStatement sqlStatement, final List<SelectItem> items) {
        do {
            OrCondition orCondition = parseOr(shardingRule, sqlStatement, items);
            sqlStatement.getConditions().getOrCondition().getAndConditions().addAll(orCondition.getAndConditions());
        } while (lexerEngine.skipIfEqual(DefaultKeyword.AND));
        lexerEngine.unsupportedIfEqual(DefaultKeyword.OR);
    }
    
    private OrCondition parseOr(final ShardingRule shardingRule, final SQLStatement sqlStatement, final List<SelectItem> items) {
        OrCondition result = new OrCondition();
        do {
            if (lexerEngine.skipIfEqual(Symbol.LEFT_PAREN)) {
                OrCondition subOrCondition = parseOr(shardingRule, sqlStatement, items);
                lexerEngine.skipIfEqual(Symbol.RIGHT_PAREN);
                OrCondition orCondition = null;
                if (lexerEngine.skipIfEqual(DefaultKeyword.AND)) {
                    orCondition = parseAnd(shardingRule, sqlStatement, items);
                }
                result.getAndConditions().addAll(merge(subOrCondition, orCondition).getAndConditions());
            } else {
                OrCondition orCondition = parseAnd(shardingRule, sqlStatement, items);
                try {
                    result.getAndConditions().addAll(orCondition.getAndConditions());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } while (lexerEngine.skipIfEqual(DefaultKeyword.OR));
        return result;
    }
    
    private OrCondition parseAnd(final ShardingRule shardingRule, final SQLStatement sqlStatement, final List<SelectItem> items) {
        OrCondition result = new OrCondition();
        do {
            if (lexerEngine.skipIfEqual(Symbol.LEFT_PAREN)) {
                OrCondition subOrCondition = parseOr(shardingRule, sqlStatement, items);
                lexerEngine.skipIfEqual(Symbol.RIGHT_PAREN);
                result = merge(result, subOrCondition);
            } else {
                Optional<Condition> condition = parseComparisonCondition(shardingRule, sqlStatement, items);
                skipsDoubleColon();
                if (condition.isPresent() && shardingRule.isShardingColumn(condition.get().getColumn())) {
                    OrCondition orCondition = new OrCondition();
                    orCondition.add(condition.get());
                    result = merge(result, orCondition);
                }
            }
        } while (lexerEngine.skipIfEqual(DefaultKeyword.AND));
        return result;
    }
    
    private OrCondition merge(final OrCondition orCondition1, final OrCondition orCondition2) {
        if (null == orCondition1 || orCondition1.getAndConditions().isEmpty()) {
            return orCondition2;
        }
        if (null == orCondition2 || orCondition2.getAndConditions().isEmpty()) {
            return orCondition1;
        }
        OrCondition result = new OrCondition();
        for (AndCondition each1 : orCondition1.getAndConditions()) {
            for (AndCondition each2 : orCondition2.getAndConditions()) {
                result.getAndConditions().add(merge(each1, each2));
            }
        }
        return result;
    }
    
    private AndCondition merge(final AndCondition andCondition1, final AndCondition andCondition2) {
        AndCondition result = new AndCondition();
        for (Condition each : andCondition1.getConditions()) {
            result.getConditions().add(each);
        }
        for (Condition each : andCondition2.getConditions()) {
            result.getConditions().add(each);
        }
        return result;
    }
    
    private Optional<Condition> parseComparisonCondition(final ShardingRule shardingRule, final SQLStatement sqlStatement, final List<SelectItem> items) {
        Optional<Condition> result = Optional.absent();
        SQLExpression left = basicExpressionParser.parse(sqlStatement);
        if (lexerEngine.skipIfEqual(Symbol.EQ)) {
            result = parseEqualCondition(shardingRule, sqlStatement, left);
            return result;
        }
        if (lexerEngine.skipIfEqual(DefaultKeyword.IN)) {
            result = parseInCondition(shardingRule, sqlStatement, left);
            return result;
        }
        if (lexerEngine.skipIfEqual(DefaultKeyword.BETWEEN)) {
            result = parseBetweenCondition(shardingRule, sqlStatement, left);
            return result;
        }
        if (sqlStatement instanceof SelectStatement && isRowNumberCondition(items, left)) {
            if (lexerEngine.skipIfEqual(Symbol.LT)) {
                parseRowCountCondition((SelectStatement) sqlStatement, false);
                return result;
            }
            if (lexerEngine.skipIfEqual(Symbol.LT_EQ)) {
                parseRowCountCondition((SelectStatement) sqlStatement, true);
                return result;
            }
            if (lexerEngine.skipIfEqual(Symbol.GT)) {
                parseOffsetCondition((SelectStatement) sqlStatement, false);
                return result;
            }
            if (lexerEngine.skipIfEqual(Symbol.GT_EQ)) {
                parseOffsetCondition((SelectStatement) sqlStatement, true);
                return result;
            }
        }
        List<Keyword> otherConditionOperators = new LinkedList<>(Arrays.asList(getCustomizedOtherConditionOperators()));
        otherConditionOperators.addAll(
                Arrays.asList(Symbol.LT, Symbol.LT_EQ, Symbol.GT, Symbol.GT_EQ, Symbol.LT_GT, Symbol.BANG_EQ, Symbol.BANG_GT, Symbol.BANG_LT, DefaultKeyword.LIKE, DefaultKeyword.IS));
        if (lexerEngine.skipIfEqual(otherConditionOperators.toArray(new Keyword[otherConditionOperators.size()]))) {
            lexerEngine.skipIfEqual(DefaultKeyword.NOT);
            parseOtherCondition(sqlStatement);
        }
        if (lexerEngine.skipIfEqual(DefaultKeyword.NOT)) {
            lexerEngine.nextToken();
            lexerEngine.skipIfEqual(Symbol.LEFT_PAREN);
            parseOtherCondition(sqlStatement);
            lexerEngine.skipIfEqual(Symbol.RIGHT_PAREN);
        }
        return result;
    }
    
    private Optional<Condition> parseEqualCondition(final ShardingRule shardingRule, final SQLStatement sqlStatement, final SQLExpression left) {
        SQLExpression right = basicExpressionParser.parse(sqlStatement);
        // TODO if have more tables, and cannot find column belong to, should not add to condition, should parse binding table rule.
        if ((sqlStatement.getTables().isSingleTable() || left instanceof SQLPropertyExpression)
                && (right instanceof SQLNumberExpression || right instanceof SQLTextExpression || right instanceof SQLPlaceholderExpression)) {
            Optional<Column> column = find(sqlStatement.getTables(), left);
            if (column.isPresent()) {
                return Optional.of(new Condition(column.get(), right));
            }
        }
        return Optional.absent();
    }
    
    private Optional<Condition> parseInCondition(final ShardingRule shardingRule, final SQLStatement sqlStatement, final SQLExpression left) {
        lexerEngine.accept(Symbol.LEFT_PAREN);
        List<SQLExpression> rights = new LinkedList<>();
        do {
            lexerEngine.skipIfEqual(Symbol.COMMA);
            rights.add(basicExpressionParser.parse(sqlStatement));
        } while (!lexerEngine.equalAny(Symbol.RIGHT_PAREN));
        lexerEngine.nextToken();
        Optional<Column> column = find(sqlStatement.getTables(), left);
        if (column.isPresent()) {
            return Optional.of(new Condition(column.get(), rights));
        }
        return Optional.absent();
    }
    
    private Optional<Condition> parseBetweenCondition(final ShardingRule shardingRule, final SQLStatement sqlStatement, final SQLExpression left) {
        List<SQLExpression> rights = new LinkedList<>();
        rights.add(basicExpressionParser.parse(sqlStatement));
        skipsDoubleColon();
        lexerEngine.accept(DefaultKeyword.AND);
        rights.add(basicExpressionParser.parse(sqlStatement));
        Optional<Column> column = find(sqlStatement.getTables(), left);
        if (column.isPresent()) {
            return Optional.of(new Condition(column.get(), rights.get(0), rights.get(1)));
        }
        return Optional.absent();
    }
    
    private boolean isRowNumberCondition(final List<SelectItem> items, final SQLExpression sqlExpression) {
        String columnLabel = null;
        if (sqlExpression instanceof SQLIdentifierExpression) {
            columnLabel = ((SQLIdentifierExpression) sqlExpression).getName();
        } else if (sqlExpression instanceof SQLPropertyExpression) {
            columnLabel = ((SQLPropertyExpression) sqlExpression).getName();
        }
        return null != columnLabel && isRowNumberCondition(items, columnLabel);
    }
    
    protected boolean isRowNumberCondition(final List<SelectItem> items, final String columnLabel) {
        return false;
    }
    
    private void parseRowCountCondition(final SelectStatement selectStatement, final boolean includeRowCount) {
        SQLExpression sqlExpression = basicExpressionParser.parse(selectStatement);
        if (null == selectStatement.getLimit()) {
            selectStatement.setLimit(new Limit(databaseType));
        }
        if (sqlExpression instanceof SQLNumberExpression) {
            int rowCount = ((SQLNumberExpression) sqlExpression).getNumber().intValue();
            selectStatement.getLimit().setRowCount(new LimitValue(rowCount, -1, includeRowCount));
            selectStatement.getSqlTokens().add(new RowCountToken(
                    lexerEngine.getCurrentToken().getEndPosition() - String.valueOf(rowCount).length() - lexerEngine.getCurrentToken().getLiterals().length(), rowCount));
        } else if (sqlExpression instanceof SQLPlaceholderExpression) {
            selectStatement.getLimit().setRowCount(new LimitValue(-1, ((SQLPlaceholderExpression) sqlExpression).getIndex(), includeRowCount));
        }
    }
    
    private void parseOffsetCondition(final SelectStatement selectStatement, final boolean includeOffset) {
        SQLExpression sqlExpression = basicExpressionParser.parse(selectStatement);
        if (null == selectStatement.getLimit()) {
            selectStatement.setLimit(new Limit(databaseType));
        }
        if (sqlExpression instanceof SQLNumberExpression) {
            int offset = ((SQLNumberExpression) sqlExpression).getNumber().intValue();
            selectStatement.getLimit().setOffset(new LimitValue(offset, -1, includeOffset));
            selectStatement.getSqlTokens().add(new OffsetToken(
                    lexerEngine.getCurrentToken().getEndPosition() - String.valueOf(offset).length() - lexerEngine.getCurrentToken().getLiterals().length(), offset));
        } else if (sqlExpression instanceof SQLPlaceholderExpression) {
            selectStatement.getLimit().setOffset(new LimitValue(-1, ((SQLPlaceholderExpression) sqlExpression).getIndex(), includeOffset));
        }
    }
    
    protected Keyword[] getCustomizedOtherConditionOperators() {
        return new Keyword[0];
    }
    
    private void parseOtherCondition(final SQLStatement sqlStatement) {
        basicExpressionParser.parse(sqlStatement);
    }
    
    private Optional<Column> find(final Tables tables, final SQLExpression sqlExpression) {
        if (sqlExpression instanceof SQLPropertyExpression) {
            return getColumnWithOwner(tables, (SQLPropertyExpression) sqlExpression);
        }
        if (sqlExpression instanceof SQLIdentifierExpression) {
            return getColumnWithoutOwner(tables, (SQLIdentifierExpression) sqlExpression);
        }
        return Optional.absent();
    }
    
    private Optional<Column> getColumnWithOwner(final Tables tables, final SQLPropertyExpression propertyExpression) {
        Optional<Table> table = tables.find(SQLUtil.getExactlyValue((propertyExpression.getOwner()).getName()));
        return propertyExpression.getOwner() instanceof SQLIdentifierExpression && table.isPresent()
                ? Optional.of(new Column(SQLUtil.getExactlyValue(propertyExpression.getName()), table.get().getName())) : Optional.<Column>absent();
    }
    
    private Optional<Column> getColumnWithoutOwner(final Tables tables, final SQLIdentifierExpression identifierExpression) {
        return tables.isSingleTable() ? Optional.of(new Column(SQLUtil.getExactlyValue(identifierExpression.getName()), tables.getSingleTableName())) : Optional.<Column>absent();
    }
    
    private void skipsDoubleColon() {
        if (lexerEngine.skipIfEqual(Symbol.DOUBLE_COLON)) {
            lexerEngine.nextToken();
        }
    }
}
