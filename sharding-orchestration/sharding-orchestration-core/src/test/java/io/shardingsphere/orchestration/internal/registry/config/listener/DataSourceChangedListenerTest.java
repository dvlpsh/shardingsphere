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

package io.shardingsphere.orchestration.internal.registry.config.listener;

import io.shardingsphere.core.config.DataSourceConfiguration;
import io.shardingsphere.orchestration.internal.registry.config.event.DataSourceChangedEvent;
import io.shardingsphere.orchestration.internal.registry.state.service.DataSourceService;
import io.shardingsphere.orchestration.reg.api.RegistryCenter;
import io.shardingsphere.orchestration.reg.listener.DataChangedEvent;
import io.shardingsphere.orchestration.util.FieldUtil;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DataSourceChangedListenerTest {
    
    private DataSourceChangedListener dataSourceChangedListener;
    
    @Mock
    private RegistryCenter regCenter;
    
    @Mock
    private DataSourceService dataSourceService;
    
    @Before
    @SneakyThrows
    public void setUp() {
        dataSourceChangedListener = new DataSourceChangedListener("test", regCenter, "sharding_db");
        FieldUtil.setField(dataSourceChangedListener, "dataSourceService", dataSourceService);
    }
    
    @Test
    public void assertCreateOrchestrationEvent() {
        Map<String, DataSourceConfiguration> expected = new HashMap<>(1, 1);
        expected.put("sharding_db", mock(DataSourceConfiguration.class));
        when(dataSourceService.getAvailableDataSourceConfigurations("sharding_db")).thenReturn(expected);
        DataSourceChangedEvent actual = dataSourceChangedListener.createOrchestrationEvent(mock(DataChangedEvent.class));
        assertThat(actual.getShardingSchemaName(), is("sharding_db"));
        assertThat(actual.getDataSourceConfigurations(), is(expected));
    }
}
