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

package org.apache.shardingsphere.sql.parser.relation.segment.table;

import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.relation.metadata.RelationMetas;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableSegment;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

/**
 * Tables context.
 */
@RequiredArgsConstructor
public final class TablesContext {
    
    private final Collection<TableSegment> tables;
    
    public TablesContext(final TableSegment tableSegment) {
        this(null == tableSegment ? Collections.<TableSegment>emptyList() : Collections.singletonList(tableSegment));
    }
    
    /**
     * Get table names.
     * 
     * @return table names
     */
    public Collection<String> getTableNames() {
        Collection<String> result = new LinkedHashSet<>(tables.size(), 1);
        for (TableSegment each : tables) {
            result.add(each.getTableName().getIdentifier().getValue());
        }
        return result;
    }
    
    /**
     * Find table name.
     *
     * @param predicate predicate
     * @param relationMetas relation metas
     * @return table name
     */
    public Optional<String> findTableName(final PredicateSegment predicate, final RelationMetas relationMetas) {
        if (1 == tables.size()) {
            return Optional.of(tables.iterator().next().getTableName().getIdentifier().getValue());
        }
        if (predicate.getColumn().getOwner().isPresent()) {
            return Optional.of(findTableNameFromSQL(predicate.getColumn().getOwner().get().getIdentifier().getValue()));
        }
        return findTableNameFromMetaData(predicate.getColumn().getIdentifier().getValue(), relationMetas);
    }
    
    private String findTableNameFromSQL(final String tableNameOrAlias) {
        for (TableSegment each : tables) {
            if (tableNameOrAlias.equalsIgnoreCase(each.getTableName().getIdentifier().getValue()) || tableNameOrAlias.equals(each.getAlias().orNull())) {
                return each.getTableName().getIdentifier().getValue();
            }
        }
        throw new IllegalStateException("Can not find owner from table.");
    }
    
    private Optional<String> findTableNameFromMetaData(final String columnName, final RelationMetas relationMetas) {
        for (TableSegment each : tables) {
            if (relationMetas.containsColumn(each.getTableName().getIdentifier().getValue(), columnName)) {
                return Optional.of(each.getTableName().getIdentifier().getValue());
            }
        }
        return Optional.absent();
    }
}
