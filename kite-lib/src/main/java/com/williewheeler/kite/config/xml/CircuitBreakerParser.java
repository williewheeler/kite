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
package com.williewheeler.kite.config.xml;

import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.w3c.dom.Element;

import com.williewheeler.kite.circuitbreaker.CircuitBreakerTemplate;

/**
 * <p>
 * Parses <code>&lt;kite:circuit-breaker&gt;</code> elements in Spring
 * application context configuration files.
 * </p>
 * 
 * @version $Id: CircuitBreakerParser.java 69 2010-03-28 05:28:31Z willie.wheeler $
 * @author Willie Wheeler
 * @since 1.0
 */
class CircuitBreakerParser extends AbstractSimpleBeanDefinitionParser {
	
	@Override
	protected Class<?> getBeanClass(Element elem) {
		return CircuitBreakerTemplate.class;
	}
}
