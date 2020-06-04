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

package org.apache.shardingsphere.encrypt.spring.namespace.parser;

import org.apache.shardingsphere.encrypt.api.config.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptStrategyConfiguration;
import org.apache.shardingsphere.encrypt.spring.namespace.tag.EncryptRuleBeanDefinitionTag;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * Encrypt rule bean definition parser.
 */
public final class EncryptRuleBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(EncryptRuleConfiguration.class);
        factory.addConstructorArgValue(parseEncryptStrategyConfigurations(element, parserContext));
        factory.addConstructorArgValue(parseEncryptTableRuleConfigurations(element));
        return factory.getBeanDefinition();
    }
    
    private static Collection<BeanDefinition> parseEncryptStrategyConfigurations(final Element element, final ParserContext parserContext) {
        Element encryptStrategiesRuleElement = DomUtils.getChildElementByTagName(element, EncryptRuleBeanDefinitionTag.ENCRYPT_STRATEGIES_TAG);
        List<Element> encryptAlgorithmElements = DomUtils.getChildElementsByTagName(encryptStrategiesRuleElement, EncryptRuleBeanDefinitionTag.ENCRYPT_STRATEGY_CONFIG_TAG);
        Collection<BeanDefinition> result = new ManagedList<>(encryptAlgorithmElements.size());
        for (Element each : encryptAlgorithmElements) {
            result.add(parseEncryptStrategyConfiguration(each, parserContext));
        }
        return result;
    }
    
    private static AbstractBeanDefinition parseEncryptStrategyConfiguration(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(EncryptStrategyConfiguration.class);
        factory.addConstructorArgValue(element.getAttribute(BeanDefinitionParserDelegate.ID_ATTRIBUTE));
        factory.addConstructorArgValue(element.getAttribute(EncryptRuleBeanDefinitionTag.ENCRYPT_ALGORITHM_TYPE_ATTRIBUTE));
        factory.addConstructorArgValue(parseProperties(element, parserContext));
        return factory.getBeanDefinition();
    }
    
    private static Properties parseProperties(final Element element, final ParserContext parserContext) {
        Element propsElement = DomUtils.getChildElementByTagName(element, EncryptRuleBeanDefinitionTag.ENCRYPT_ALGORITHM_PROPS_TAG);
        return null == propsElement ? new Properties() : parserContext.getDelegate().parsePropsElement(propsElement);
    }
    
    private static Collection<BeanDefinition> parseEncryptTableRuleConfigurations(final Element element) {
        Element encryptTablesElement = DomUtils.getChildElementByTagName(element, EncryptRuleBeanDefinitionTag.TABLES_CONFIG_TAG);
        List<Element> encryptTableElements = DomUtils.getChildElementsByTagName(encryptTablesElement, EncryptRuleBeanDefinitionTag.TABLE_CONFIG_TAG);
        Collection<BeanDefinition> result = new ManagedList<>(encryptTableElements.size());
        for (Element each : encryptTableElements) {
            result.add(parseEncryptTableRuleConfiguration(each));
        }
        return result;
    }
    
    private static AbstractBeanDefinition parseEncryptTableRuleConfiguration(final Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(EncryptTableRuleConfiguration.class);
        factory.addConstructorArgValue(element.getAttribute(BeanDefinitionParserDelegate.NAME_ATTRIBUTE));
        factory.addConstructorArgValue(parseEncryptColumnRuleConfigurations(element));
        return factory.getBeanDefinition();
    }
    
    private static Collection<BeanDefinition> parseEncryptColumnRuleConfigurations(final Element element) {
        List<Element> encryptColumnElements = DomUtils.getChildElementsByTagName(element, EncryptRuleBeanDefinitionTag.COLUMN_CONFIG_TAG);
        Collection<BeanDefinition> result = new ManagedList<>(encryptColumnElements.size());
        for (Element each : encryptColumnElements) {
            result.add(parseEncryptColumnRuleConfiguration(each));
        }
        return result;
    }
    
    private static AbstractBeanDefinition parseEncryptColumnRuleConfiguration(final Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(EncryptColumnRuleConfiguration.class);
        factory.addConstructorArgValue(element.getAttribute(EncryptRuleBeanDefinitionTag.COLUMN_LOGIC_COLUMN_ATTRIBUTE));
        factory.addConstructorArgValue(element.getAttribute(EncryptRuleBeanDefinitionTag.COLUMN_PLAIN_COLUMN_ATTRIBUTE));
        factory.addConstructorArgValue(element.getAttribute(EncryptRuleBeanDefinitionTag.COLUMN_CIPHER_COLUMN_ATTRIBUTE));
        factory.addConstructorArgValue(element.getAttribute(EncryptRuleBeanDefinitionTag.COLUMN_ASSISTED_QUERY_COLUMN_ATTRIBUTE));
        factory.addConstructorArgValue(element.getAttribute(EncryptRuleBeanDefinitionTag.COLUMN_ENCRYPT_STRATEGY_REF_ATTRIBUTE));
        return factory.getBeanDefinition();
    }
}
