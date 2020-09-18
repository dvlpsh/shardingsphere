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

package org.apache.shardingsphere.proxy.init.impl;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.SchemaContexts;
import org.apache.shardingsphere.proxy.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.config.YamlProxyConfiguration;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class AbstractBootstrapInitializerTest {
    
    private static final String HOST = "127.0.0.1";
    
    private static final int PORT = 2020;
    
    @SneakyThrows
    @Test
    public void assertInit() {
        new Thread(this::triggerAbstractBootstrapInitializerInit).start();
        TimeUnit.SECONDS.sleep(5);
        assertTrue(isAvailable());
    }
    
    @SneakyThrows
    private void triggerAbstractBootstrapInitializerInit() {
        AbstractBootstrapInitializer abstractBootstrapInitializer = mock(AbstractBootstrapInitializer.class, Mockito.CALLS_REAL_METHODS);
        doReturn(mock(ProxyConfiguration.class)).when(abstractBootstrapInitializer).getProxyConfiguration(any());
        SchemaContexts schemaContexts = mock(SchemaContexts.class);
        ConfigurationProperties properties = mock(ConfigurationProperties.class);
        when(properties.getValue(any())).thenReturn(Boolean.FALSE);
        when(schemaContexts.getProps()).thenReturn(properties);
        doReturn(schemaContexts).when(abstractBootstrapInitializer).decorateSchemaContexts(any());
        doReturn(mock(TransactionContexts.class)).when(abstractBootstrapInitializer).decorateTransactionContexts(any());
        YamlProxyConfiguration yamlConfig = mock(YamlProxyConfiguration.class);
        abstractBootstrapInitializer.init(yamlConfig, PORT);
    }
    
    private static boolean isAvailable() {
        boolean portFree;
        try (Socket socket = new Socket(HOST, PORT)) {
            portFree = true;
        } catch (UnknownHostException ex) {
            portFree = false;
        } catch (IOException e) {
            portFree = false;
        }
        return portFree;
    }
}
