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

package io.shardingsphere.core.parsing.antlr.extractor.segment.engine;

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.antlr.extractor.segment.OptionalSQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.constant.RuleName;
import io.shardingsphere.core.parsing.antlr.extractor.util.ASTUtils;
import io.shardingsphere.core.parsing.antlr.sql.segment.ConstraintDefinitionSegment;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Collection;

/**
 * Primary key for create table extractor.
 *
 * @author duhongjun
 */
public final class PrimaryKeyForCreateTableExtractor implements OptionalSQLSegmentExtractor {
    
    @Override
    public Optional<ConstraintDefinitionSegment> extract(final ParserRuleContext ancestorNode) {
        Optional<ParserRuleContext> primaryKeyNode = ASTUtils.findFirstChildNode(ancestorNode, RuleName.PRIMARY_KEY);
        if (!primaryKeyNode.isPresent()) {
            return Optional.absent();
        }
        Optional<ParserRuleContext> columnListNode = ASTUtils.findFirstChildNode(primaryKeyNode.get().getParent().getParent(), RuleName.COLUMN_LIST);
        if (!columnListNode.isPresent()) {
            return Optional.absent();
        }
        Collection<ParserRuleContext> columnNameNodes = ASTUtils.getAllDescendantNodes(columnListNode.get(), RuleName.COLUMN_NAME);
        if (columnNameNodes.isEmpty()) {
            return Optional.absent();
        }
        ConstraintDefinitionSegment result = new ConstraintDefinitionSegment();
        for (ParserRuleContext each : columnNameNodes) {
            result.getPrimaryKeyColumnNames().add(each.getText());
        }
        return Optional.of(result);
    }
}
