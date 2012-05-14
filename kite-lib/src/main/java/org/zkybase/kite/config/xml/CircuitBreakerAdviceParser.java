/*
 * Copyright (c) 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zkybase.kite.config.xml;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;
import org.zkybase.kite.circuitbreaker.interceptor.CircuitBreakerInterceptor;
import org.zkybase.kite.circuitbreaker.interceptor.DefaultCircuitBreakerSource;


/**
 * Parses <code>&lt;kite:circuit-breaker-advice&gt;</code> elements in Spring application context configuration files.
 * 
 * @author Willie Wheeler
 * @since 1.0
 */
class CircuitBreakerAdviceParser extends AbstractSingleBeanDefinitionParser {
	
	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser#getBeanClass(org.w3c.dom.Element)
	 */
	@Override
	protected Class<?> getBeanClass(Element elem) {
		return CircuitBreakerInterceptor.class;
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser#doParse(org.w3c.dom.Element,
	 * org.springframework.beans.factory.support.BeanDefinitionBuilder)
	 */
	@Override
	protected void doParse(Element elem, BeanDefinitionBuilder builder) {
		builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
		
		// If an advice is explicitly defined, then we're using the
		// DefaultCircuitBreakerSource.
		RootBeanDefinition srcDef = new RootBeanDefinition(DefaultCircuitBreakerSource.class);
		srcDef.setSource(elem);
		srcDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
		srcDef.getPropertyValues().add("breaker", new RuntimeBeanReference(elem.getAttribute("breaker")));
		builder.addPropertyValue("circuitBreakerSource", srcDef);
	}
}
