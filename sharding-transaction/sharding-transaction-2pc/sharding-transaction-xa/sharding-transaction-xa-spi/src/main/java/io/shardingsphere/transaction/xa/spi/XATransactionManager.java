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

package io.shardingsphere.transaction.xa.spi;

import io.shardingsphere.transaction.core.ShardingTransactionManager;

import javax.sql.XADataSource;
import javax.transaction.xa.XAResource;

/**
 * XA transaction manager.
 *
 * @author zhangliang
 * @author zhaojun
 */
public interface XATransactionManager extends ShardingTransactionManager {
    
    /**
     * Startup XA transaction manager.
     */
    void startup();
    
    /**
     * Register recovery resource.
     *
     * @param dataSourceName data source name
     * @param xaDataSource   XA data source
     */
    void registerRecoveryResource(String dataSourceName, XADataSource xaDataSource);
    
    /**
     * Remove recovery resource.
     *
     * @param dataSourceName data source name
     * @param xaDataSource   XA data source
     */
    void removeRecoveryResource(String dataSourceName, XADataSource xaDataSource);
    
    /**
     * Enlist resource.
     * 
     * @param xaResource XA resource
     */
    void enlistResource(XAResource xaResource);
    
    /**
     * Destroy the transaction manager and could be helpful with shutdown gracefully.
     */
    void destroy();
}
