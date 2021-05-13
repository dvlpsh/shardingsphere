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

package org.apache.shardingsphere.governance.core.facade.repository;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.apache.shardingsphere.governance.repository.api.RegistryCenterRepository;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceCenterConfiguration;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceConfiguration;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;

/**
 * Registry center repository facade.
 */
@Getter
public final class RegistryCenterRepositoryFacade implements AutoCloseable {
    
    static {
        ShardingSphereServiceLoader.register(RegistryCenterRepository.class);
    }
    
    private final RegistryCenterRepository registryCenterRepository;
    
    public RegistryCenterRepositoryFacade(final GovernanceConfiguration config) {
        registryCenterRepository = createGovernanceRepository(config);
    }
    
    private RegistryCenterRepository createGovernanceRepository(final GovernanceConfiguration config) {
        GovernanceCenterConfiguration governanceCenterConfig = config.getGovernanceCenterConfiguration();
        Preconditions.checkNotNull(governanceCenterConfig, "Governance center configuration cannot be null.");
        RegistryCenterRepository result = TypedSPIRegistry.getRegisteredService(RegistryCenterRepository.class, governanceCenterConfig.getType(), governanceCenterConfig.getProps());
        result.init(config.getName(), governanceCenterConfig);
        return result;
    }
    
    @Override
    public void close() {
        registryCenterRepository.close();
    }
}
