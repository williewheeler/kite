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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * <code>NamespaceHandler</code> allowing for declarative Kite configuration using either XML or using annotations.
 * 
 * @author Willie Wheeler
 * @since 1.0
 */
public class KiteNamespaceHandler extends NamespaceHandlerSupport {
	private static Logger log = LoggerFactory.getLogger(KiteNamespaceHandler.class);

	/**
	 * Registers bean definition parsers for the various custom top-level Kite tags, such as
	 * <code>&lt;kite:circuit-breaker&gt;</code>.
	 */
	public void init() {
		log.info("Initializing KiteNamespaceHandler");
		registerBeanDefinitionParser("annotation-config", new AnnotationConfigParser());
		registerBeanDefinitionParser("circuit-breaker", new CircuitBreakerParser());
		registerBeanDefinitionParser("circuit-breaker-advice", new CircuitBreakerAdviceParser());
		registerBeanDefinitionParser("throttle", new ThrottleParser());
		registerBeanDefinitionParser("throttle-advice", new ThrottleAdviceParser());
	}
}
