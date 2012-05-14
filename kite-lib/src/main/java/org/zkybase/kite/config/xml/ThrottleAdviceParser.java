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
import org.zkybase.kite.throttle.interceptor.DefaultThrottleSource;
import org.zkybase.kite.throttle.interceptor.ThrottleInterceptor;


/**
 * <p>
 * Parses <code>&lt;kite:throttle-advice&gt;</code> elements in Spring
 * application context configuration files.
 * </p>
 * 
 * @version $Id: ThrottleAdviceParser.java 69 2010-03-28 05:28:31Z willie.wheeler $
 * @author Willie Wheeler
 * @since 1.0
 */
class ThrottleAdviceParser extends AbstractSingleBeanDefinitionParser {

	/**
	 * <p>
	 * Returns the <code>Class</code> associated with the
	 * <code>&lt;kite:throttle-advice&gt;</code> tag; viz.,
	 * <code>ThrottleInterceptor</code>.
	 * </p>
	 * 
	 * @param elem
	 *            <code>&lt;kite:throttle-advice&gt;</code> element
	 * @return class associated with the
	 *         <code>&lt;kite:throttle-advice&gt;</code> tag
	 */
	@Override
	protected Class<?> getBeanClass(Element elem) {
		return ThrottleInterceptor.class;
	}
	
	@Override
	protected void doParse(Element elem, BeanDefinitionBuilder builder) {
		builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
		
		// If an advice is explicitly defined, then we're using the
		// DefaultThrottleSource.
		RootBeanDefinition srcDef = new RootBeanDefinition(DefaultThrottleSource.class);
		srcDef.setSource(elem);
		srcDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
		srcDef.getPropertyValues().add("throttle", new RuntimeBeanReference(elem.getAttribute("throttle")));
		builder.addPropertyValue("throttleSource", srcDef);
	}
}
