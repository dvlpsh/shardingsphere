package io.shardingsphere.core.parsing.antlr.extractor.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import io.shardingsphere.core.parsing.antlr.extractor.OptionalSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.util.ExtractorUtils;
import io.shardingsphere.core.parsing.antlr.extractor.util.RuleName;
import io.shardingsphere.core.parsing.antlr.sql.segment.FromWhereSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.condition.OrConditionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.table.TableJoinSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.table.TableSegment;

public abstract class AbstractFromWhereExtractor implements OptionalSQLSegmentExtractor {
    
    private final TableNameExtractor tableNameExtractor = new TableNameExtractor();
    
    private PredicateExtractor predicateSegmentExtractor;
    
    @Override
    public Optional<FromWhereSegment> extract(final ParserRuleContext ancestorNode) {
        return extract(ancestorNode, ancestorNode);
    }
    
    /**
     * Extract SQL segment from SQL AST.
     *
     * @param ancestorNode ancestor node of AST
     * @param rootNode root node of AST
     * @return SQL segment
     */
    public Optional<FromWhereSegment> extract(final ParserRuleContext ancestorNode, final ParserRuleContext rootNode) {
        FromWhereSegment result = new FromWhereSegment();
        Map<ParserRuleContext, Integer> questionNodeIndexMap = getPlaceholderAndNodeIndexMap(result, rootNode);
        Optional<ParserRuleContext> whereNode = extractTable(result, ancestorNode, questionNodeIndexMap);
        if(!whereNode.isPresent()) {
            return Optional.absent(); 
        }
        predicateSegmentExtractor = new PredicateExtractor(result.getTableAliases());
        extractAndFillWhere(result, questionNodeIndexMap, whereNode.get());
        return Optional.of(result);
    }
    
    private Map<ParserRuleContext, Integer> getPlaceholderAndNodeIndexMap(final FromWhereSegment fromWhereSegment, final ParserRuleContext rootNode) {
        Collection<ParserRuleContext> questionNodes = ExtractorUtils.getAllDescendantNodes(rootNode, RuleName.QUESTION);
        Map<ParserRuleContext, Integer> result = new HashMap<>(questionNodes.size(), 1);
        int index = 0;
        for (ParserRuleContext each : questionNodes) {
            result.put(each, index++);
        }
        fromWhereSegment.setParameterCount(questionNodes.size());
        return result;
    }
    
    protected abstract Optional<ParserRuleContext> extractTable(final FromWhereSegment fromWhereSegment, final ParserRuleContext ancestorNode,
                                                                final Map<ParserRuleContext, Integer> questionNodeIndexMap);
    
    protected void extractTableRefrence(final FromWhereSegment fromWhereSegment, final ParserRuleContext tableReferenceNode, final Map<ParserRuleContext, Integer> questionNodeIndexMap) {
        for (int i = 0; i < tableReferenceNode.getChildCount(); i++) {
            if (tableReferenceNode.getChild(i) instanceof TerminalNode) {
                continue;
            }
            ParserRuleContext childNode = (ParserRuleContext) tableReferenceNode.getChild(i);
            if (RuleName.TABLE_REFERENCES.getName().equals(childNode.getClass().getSimpleName())) {
                Collection<ParserRuleContext> subTableReferenceNodes = ExtractorUtils.getAllDescendantNodes(childNode, RuleName.TABLE_REFERENCE);
                for(ParserRuleContext each : subTableReferenceNodes) {
                    extractTableRefrence(fromWhereSegment, each, questionNodeIndexMap);
                }
            }else {
                fillTable(fromWhereSegment, childNode, questionNodeIndexMap);
            }
        }
    }
    
    protected void fillTable(final FromWhereSegment fromWhereSegment, final ParserRuleContext joinOrTableFactorNode, final Map<ParserRuleContext, Integer> questionNodeIndexMap) {
        if (!RuleName.JOIN_TABLE.getName().endsWith(joinOrTableFactorNode.getClass().getSimpleName())) {
            Optional<TableSegment> tableSegment = tableNameExtractor.extract(joinOrTableFactorNode);
            Preconditions.checkState(tableSegment.isPresent());
            fillTableResult(fromWhereSegment, tableSegment.get());
        }
        Optional<ParserRuleContext> joinConditionNode = ExtractorUtils.findFirstChildNode(joinOrTableFactorNode, RuleName.JOIN_CONDITION);
        if (!joinConditionNode.isPresent()) {
            return;
        }
        Optional<ParserRuleContext> tableFactorNode = ExtractorUtils.findFirstChildNode(joinOrTableFactorNode, RuleName.TABLE_FACTOR);
        Preconditions.checkState(tableFactorNode.isPresent());
        Optional<TableSegment> tableSegment = tableNameExtractor.extract(tableFactorNode.get());
        Preconditions.checkState(tableSegment.isPresent());
        TableJoinSegment tableJoinResult = new TableJoinSegment(tableSegment.get());
        Optional<OrConditionSegment> conditionResult = buildCondition(joinConditionNode.get(), questionNodeIndexMap);
        if (conditionResult.isPresent()) {
            tableJoinResult.getJoinConditions().getAndConditions().addAll(conditionResult.get().getAndConditions());
            fromWhereSegment.getConditions().getAndConditions().addAll(conditionResult.get().getAndConditions());
        }
        fillTableResult(fromWhereSegment, tableJoinResult);
    }
    
    protected void fillTableResult(final FromWhereSegment fromWhereSegment, final TableSegment tableSegment) {
        String alias = tableSegment.getName();
        if (tableSegment.getAlias().isPresent()) {
            alias = tableSegment.getAlias().get();
        }
        fromWhereSegment.getTableAliases().put(alias, tableSegment.getName());
    }
    
    private void extractAndFillWhere(final FromWhereSegment fromWhereSegment, final Map<ParserRuleContext, Integer> questionNodeIndexMap, final ParserRuleContext whereNode) {
        Optional<OrConditionSegment> conditions = buildCondition((ParserRuleContext)whereNode.getChild(1), questionNodeIndexMap);
        if (conditions.isPresent()) {
            fromWhereSegment.getConditions().getAndConditions().addAll(conditions.get().getAndConditions());
        }
    }
    
    private Optional<OrConditionSegment> buildCondition(final ParserRuleContext node, final Map<ParserRuleContext, Integer> questionNodeIndexMap) {
        Optional<ParserRuleContext> exprNode = ExtractorUtils.findFirstChildNode(node, RuleName.EXPR);
        return exprNode.isPresent() ? predicateSegmentExtractor.extractCondition(questionNodeIndexMap, exprNode.get()) : Optional.<OrConditionSegment>absent();
    }
}
