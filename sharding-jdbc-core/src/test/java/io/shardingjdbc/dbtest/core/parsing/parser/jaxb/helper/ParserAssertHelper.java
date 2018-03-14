package io.shardingjdbc.dbtest.core.parsing.parser.jaxb.helper;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;

import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.constant.ShardingOperator;
import io.shardingjdbc.core.parsing.parser.context.OrderItem;
import io.shardingjdbc.core.parsing.parser.context.condition.Column;
import io.shardingjdbc.core.parsing.parser.context.limit.Limit;
import io.shardingjdbc.core.parsing.parser.context.limit.LimitValue;
import io.shardingjdbc.core.parsing.parser.context.selectitem.AggregationSelectItem;
import io.shardingjdbc.core.parsing.parser.expression.SQLExpression;
import io.shardingjdbc.core.parsing.parser.expression.SQLNumberExpression;
import io.shardingjdbc.core.parsing.parser.expression.SQLPlaceholderExpression;
import io.shardingjdbc.core.parsing.parser.expression.SQLTextExpression;
import io.shardingjdbc.core.parsing.parser.token.GeneratedKeyToken;
import io.shardingjdbc.core.parsing.parser.token.IndexToken;
import io.shardingjdbc.core.parsing.parser.token.ItemsToken;
import io.shardingjdbc.core.parsing.parser.token.MultipleInsertValuesToken;
import io.shardingjdbc.core.parsing.parser.token.OffsetToken;
import io.shardingjdbc.core.parsing.parser.token.OrderByToken;
import io.shardingjdbc.core.parsing.parser.token.RowCountToken;
import io.shardingjdbc.core.parsing.parser.token.SQLToken;
import io.shardingjdbc.core.parsing.parser.token.TableToken;
import io.shardingjdbc.dbtest.config.bean.parsecontext.Condition;
import io.shardingjdbc.dbtest.config.bean.parsecontext.Conditions;
import io.shardingjdbc.dbtest.config.bean.parsecontext.Tables;
import io.shardingjdbc.dbtest.config.bean.parsecontext.Value;

public class ParserAssertHelper {
    
    public static void assertTables(final Tables expected, final io.shardingjdbc.core.parsing.parser.context.table.Tables actual) {
        assertTrue(EqualsBuilder.reflectionEquals(ParserJAXBHelper.getTables(expected), actual));
    }
    
    public static void assertConditions(
            final Conditions expected, final io.shardingjdbc.core.parsing.parser.context.condition.Conditions actual, final boolean isPreparedStatement) {
        assertTrue(EqualsBuilder.reflectionEquals(buildExpectedConditions(expected, isPreparedStatement), actual));
    }
    
    private static io.shardingjdbc.core.parsing.parser.context.condition.Conditions buildExpectedConditions(
            final Conditions conditions, final boolean isPreparedStatement) {
        io.shardingjdbc.core.parsing.parser.context.condition.Conditions result = new io.shardingjdbc.core.parsing.parser.context.condition.Conditions();
        if (null == conditions) {
            return result;
        }
        for (Condition each : conditions.getConditions()) {
            List<SQLExpression> sqlExpressions = new LinkedList<>();
            for (Value value : each.getValues()) {
                if (isPreparedStatement) {
                    sqlExpressions.add(new SQLPlaceholderExpression(value.getIndex()));
                } else {
                    Comparable<?> valueWithType = value.getValueWithType();
                    if (valueWithType instanceof Number) {
                        sqlExpressions.add(new SQLNumberExpression((Number) valueWithType));
                    } else {
                        sqlExpressions.add(new SQLTextExpression(null == valueWithType ? "" : valueWithType.toString()));
                    }
                }
            }
            io.shardingjdbc.core.parsing.parser.context.condition.Condition condition;
            switch (ShardingOperator.valueOf(each.getOperator().toUpperCase())) {
                case EQUAL:
                    condition = new io.shardingjdbc.core.parsing.parser.context.condition.Condition(new Column(each.getColumnName(), each.getTableName()), sqlExpressions.get(0));
                    break;
                case BETWEEN:
                    condition = new io.shardingjdbc.core.parsing.parser.context.condition.Condition(
                            new Column(each.getColumnName(), each.getTableName()), sqlExpressions.get(0), sqlExpressions.get(1));
                    break;
                case IN:
                    condition = new io.shardingjdbc.core.parsing.parser.context.condition.Condition(new Column(each.getColumnName(), each.getTableName()), sqlExpressions);
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
            result.add(condition);
        }
        return result;
    }
    
    public static void assertSqlTokens(final List<io.shardingjdbc.dbtest.config.bean.parsecontext.SQLToken> expected, final List<SQLToken> actual, final boolean isPreparedStatement) {
        if (null == expected || expected.size() == 0) {
            return;
        }
        List<SQLToken> expectedSqlTokens = buildExpectedSqlTokens(expected, isPreparedStatement);
        assertTrue(expectedSqlTokens.size() == actual.size());
        for (SQLToken each : actual) {
            boolean hasData = false;
            for (SQLToken sqlToken : expectedSqlTokens) {
                if (each.getBeginPosition() == sqlToken.getBeginPosition()) {
                    hasData = true;
                    assertTrue(EqualsBuilder.reflectionEquals(sqlToken, each));
                }
            }
            assertTrue(hasData);
        }
    }
    
    private static List<SQLToken> buildExpectedSqlTokens(final List<io.shardingjdbc.dbtest.config.bean.parsecontext.SQLToken> sqlTokens,
            final boolean isPreparedStatement) {
        List<SQLToken> result = new ArrayList<>(sqlTokens.size());
        for (io.shardingjdbc.dbtest.config.bean.parsecontext.SQLToken each : sqlTokens) {
            if (isPreparedStatement && (each instanceof io.shardingjdbc.dbtest.config.bean.parsecontext.OffsetToken
                    || each instanceof io.shardingjdbc.dbtest.config.bean.parsecontext.RowCountToken)) {
                continue;
            }
            result.add(buildExpectedSQLToken(each, isPreparedStatement));
        }
        return result;
    }
    
    private static SQLToken buildExpectedSQLToken(final io.shardingjdbc.dbtest.config.bean.parsecontext.SQLToken sqlToken, final boolean isPreparedStatement) {
        if (sqlToken instanceof io.shardingjdbc.dbtest.config.bean.parsecontext.TableToken) {
            return new TableToken(sqlToken.getBeginPosition(), ((io.shardingjdbc.dbtest.config.bean.parsecontext.TableToken) sqlToken).getOriginalLiterals());
        }
        if (sqlToken instanceof io.shardingjdbc.dbtest.config.bean.parsecontext.IndexToken) {
            return new IndexToken(sqlToken.getBeginPosition(), ((io.shardingjdbc.dbtest.config.bean.parsecontext.IndexToken) sqlToken).getOriginalLiterals(),
                    ((io.shardingjdbc.dbtest.config.bean.parsecontext.IndexToken) sqlToken).getTableName());
        } else if (sqlToken instanceof io.shardingjdbc.dbtest.config.bean.parsecontext.ItemsToken) {
            ItemsToken itemsToken = new ItemsToken(sqlToken.getBeginPosition());
            itemsToken.getItems().addAll(((io.shardingjdbc.dbtest.config.bean.parsecontext.ItemsToken) sqlToken).getItems());
            return itemsToken;
        } else if (sqlToken instanceof io.shardingjdbc.dbtest.config.bean.parsecontext.GeneratedKeyToken) {
            if (isPreparedStatement) {
                return new GeneratedKeyToken(((io.shardingjdbc.dbtest.config.bean.parsecontext.GeneratedKeyToken) sqlToken).getBeginPositionOfPreparedStatement());
            } else {
                return new GeneratedKeyToken(((io.shardingjdbc.dbtest.config.bean.parsecontext.GeneratedKeyToken) sqlToken).getBeginPositionOfStatement());
            }
        } else if (sqlToken instanceof io.shardingjdbc.dbtest.config.bean.parsecontext.MultipleInsertValuesToken) {
            MultipleInsertValuesToken multipleInsertValuesToken = new MultipleInsertValuesToken(sqlToken.getBeginPosition());
            multipleInsertValuesToken.getValues().addAll(((io.shardingjdbc.dbtest.config.bean.parsecontext.MultipleInsertValuesToken) sqlToken).getValues());
            return multipleInsertValuesToken;
        } else if (sqlToken instanceof io.shardingjdbc.dbtest.config.bean.parsecontext.RowCountToken) {
            return new RowCountToken(sqlToken.getBeginPosition(), ((io.shardingjdbc.dbtest.config.bean.parsecontext.RowCountToken) sqlToken).getRowCount());
        } else if (sqlToken instanceof io.shardingjdbc.dbtest.config.bean.parsecontext.OrderByToken) {
            if (isPreparedStatement) {
                return new OrderByToken(((io.shardingjdbc.dbtest.config.bean.parsecontext.OrderByToken) sqlToken).getBeginPositionOfPreparedStatement());
            } else {
                return new OrderByToken(((io.shardingjdbc.dbtest.config.bean.parsecontext.OrderByToken) sqlToken).getBeginPositionOfStatement());
            }
        } else if (sqlToken instanceof io.shardingjdbc.dbtest.config.bean.parsecontext.OffsetToken) {
            return new OffsetToken(sqlToken.getBeginPosition(), ((io.shardingjdbc.dbtest.config.bean.parsecontext.OffsetToken) sqlToken).getOffset());
        }
        return null;
    }
    
    public static void assertLimit(final io.shardingjdbc.dbtest.config.bean.parsecontext.Limit limit, final Limit actual, final boolean isPreparedStatement) {
        Limit expected = buildExpectedLimit(limit, isPreparedStatement);
        if (null == expected) {
            assertNull(actual);
            return;
        }
        if (null != expected.getRowCount()) {
            assertTrue(EqualsBuilder.reflectionEquals(expected.getRowCount(), actual.getRowCount(), "boundOpened"));
        }
        if (null != expected.getOffset()) {
            assertTrue(EqualsBuilder.reflectionEquals(expected.getOffset(), actual.getOffset(), "boundOpened"));
        }
    }
    
    private static Limit buildExpectedLimit(final io.shardingjdbc.dbtest.config.bean.parsecontext.Limit limit, final boolean isPreparedStatement) {
        if (null == limit) {
            return null;
        }
        Limit result = new Limit(DatabaseType.MySQL);
        if (isPreparedStatement) {
            if (null != limit.getOffsetParameterIndex()) {
                result.setOffset(new LimitValue(-1, limit.getOffsetParameterIndex(), true));
            }
            if (null != limit.getRowCountParameterIndex()) {
                result.setRowCount(new LimitValue(-1, limit.getRowCountParameterIndex(), false));
            }
        } else {
            if (null != limit.getOffset()) {
                result.setOffset(new LimitValue(limit.getOffset(), -1, true));
                
            }
            if (null != limit.getRowCount()) {
                result.setRowCount(new LimitValue(limit.getRowCount(), -1, false));
            }
        }
        return result;
    }
    
    public static void assertOrderBy(final List<OrderItem> expected, final List<OrderItem> actual) {
        Iterator<OrderItem> orderByColumns = expected.iterator();
        for (OrderItem each : actual) {
            OrderItem expectedOrderItem = orderByColumns.next();
            // TODO assert nullOrderType
            assertTrue(EqualsBuilder.reflectionEquals(expectedOrderItem, each, "nullOrderType"));
        }
        assertFalse(orderByColumns.hasNext());
    }
    
    public static void assertGroupBy(final List<OrderItem> expected, final List<OrderItem> actual) {
        Iterator<OrderItem> groupByColumns = expected.iterator();
        for (OrderItem each : actual) {
            OrderItem groupByColumn = groupByColumns.next();
            // TODO assert nullOrderType
            assertTrue(EqualsBuilder.reflectionEquals(groupByColumn, each, "nullOrderType"));
        }
        assertFalse(groupByColumns.hasNext());
    }
    
    public static void assertAggregationSelectItem(final List<AggregationSelectItem> expected, final List<AggregationSelectItem> actual) {
        Iterator<AggregationSelectItem> aggregationSelectItems = expected.iterator();
        for (AggregationSelectItem each : actual) {
            AggregationSelectItem aggregationSelectItem = aggregationSelectItems.next();
            assertTrue(EqualsBuilder.reflectionEquals(aggregationSelectItem, each, "derivedAggregationSelectItems"));
            for (int i = 0; i < each.getDerivedAggregationSelectItems().size(); i++) {
                assertTrue(EqualsBuilder.reflectionEquals(aggregationSelectItem.getDerivedAggregationSelectItems().get(i), each.getDerivedAggregationSelectItems().get(i)));
            }
        }
        assertFalse(aggregationSelectItems.hasNext());
    }
    
}
