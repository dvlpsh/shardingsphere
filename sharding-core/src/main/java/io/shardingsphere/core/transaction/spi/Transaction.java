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

package io.shardingsphere.core.transaction.spi;

import io.shardingsphere.core.transaction.event.TransactionEvent;

/**
 * Transaction Spi interface.
 *
 * @author zhaojun
 */
public interface Transaction {
    
    /**
     * Do start a user transaction.
     *
     * @param transactionEvent transaction event
     * @throws Exception Exception
     */
    void begin(TransactionEvent transactionEvent) throws Exception;
    
    /**
     * Do transaction commit.
     *
     * @param transactionEvent transaction event
     * @throws Exception Exception
     */
    void commit(TransactionEvent transactionEvent) throws Exception;
    
    /**
     * Do transaction rollback.
     *
     * @param transactionEvent transaction event
     * @throws Exception Exception
     */
    void rollback(TransactionEvent transactionEvent) throws Exception;
}
