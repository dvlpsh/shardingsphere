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

package io.shardingsphere.core.parsing.antler.phrase.visitor.mysql;

import org.antlr.v4.runtime.ParserRuleContext;

import io.shardingsphere.core.parsing.antler.phrase.visitor.PhraseVisitor;
import io.shardingsphere.core.parsing.antler.sql.ddl.ColumnDefinition;
import io.shardingsphere.core.parsing.antler.sql.ddl.ColumnPosition;
import io.shardingsphere.core.parsing.antler.sql.ddl.mysql.MySQLAlterTableStatement;
import io.shardingsphere.core.parsing.antler.utils.RuleNameConstants;
import io.shardingsphere.core.parsing.antler.utils.TreeUtils;
import io.shardingsphere.core.parsing.antler.utils.VisitorUtils;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;

/**
 * Visit MySQL change column phrase.
 * 
 * @author duhongjun
 */
public class MySQLChangeColumnVisitor implements PhraseVisitor {

    /** 
     * Visit change column node.
     * 
     * @param ancestorNode ancestor node of ast
     * @param statement SQL statement
     */
    @Override
    public void visit(final ParserRuleContext ancestorNode, final SQLStatement statement) {
        MySQLAlterTableStatement alterStatement = (MySQLAlterTableStatement) statement;

        ParserRuleContext changeColumnCtx = TreeUtils.getFirstChildByRuleName(ancestorNode,
                RuleNameConstants.CHANGE_COLUMN);
        if (null == changeColumnCtx) {
            return;
        }

        ParserRuleContext oldColumnCtx = TreeUtils.getFirstChildByRuleName(changeColumnCtx,
                RuleNameConstants.COLUMN_NAME);

        if (null == oldColumnCtx) {
            return;
        }

        ParserRuleContext columnDefinitionCtx = TreeUtils.getFirstChildByRuleName(changeColumnCtx,
                RuleNameConstants.COLUMN_DEFINITION);

        if (null == columnDefinitionCtx) {
            return;
        }

        ColumnDefinition column = VisitorUtils.visitColumnDefinition(columnDefinitionCtx);
        if (null != column) {
            alterStatement.getUpdateColumns().put(oldColumnCtx.getText(), column);
            ColumnPosition columnPosition = VisitorUtils.visitFirstOrAfter(changeColumnCtx, column.getName());
            if (null != columnPosition) {
                alterStatement.getPositionChangedColumns().add(columnPosition);
            }
        }
    }
}
