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

package io.shardingsphere.transaction.event;

import com.google.common.base.Optional;
import io.shardingsphere.core.constant.TCLType;
import io.shardingsphere.core.exception.ShardingException;
import lombok.Getter;

import java.sql.Connection;
import java.util.Collection;

/**
 * Local transaction event.
 *
 * @author zhaojun
 */
@Getter
public final class LocalTransactionEvent extends TransactionEvent {
    
    private final Collection<Connection> cachedConnections;
    
    private final boolean autoCommit;
    
    public LocalTransactionEvent(final TCLType tclType, final Collection<Connection> cachedConnections, final boolean autoCommit) {
        super(tclType);
        this.cachedConnections = cachedConnections;
        this.autoCommit = autoCommit;
    }
    
    @Override
    public Optional<ShardingException> getException() {
        return Optional.fromNullable((ShardingException) super.getException().orNull());
    }
}
