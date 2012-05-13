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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.config.AopNamespaceUtils;
import org.springframework.aop.support.DefaultBeanFactoryPointcutAdvisor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.williewheeler.kite.circuitbreaker.interceptor.AnnotationCircuitBreakerSource;
import com.williewheeler.kite.circuitbreaker.interceptor.CircuitBreakerInterceptor;
import com.williewheeler.kite.circuitbreaker.interceptor.CircuitBreakerSourcePointcut;
import com.williewheeler.kite.throttle.interceptor.AnnotationThrottleSource;
import com.williewheeler.kite.throttle.interceptor.ThrottleInterceptor;
import com.williewheeler.kite.throttle.interceptor.ThrottleSourcePointcut;

/**
 * <p>
 * Parses &lt;kite:annotation-config&gt; into Kite infrastructure beans.
 * </p>
 * 
 * @version $Id: AnnotationConfigParser.java 75 2010-03-29 07:34:59Z willie.wheeler $
 * @author Willie Wheeler
 * @since 1.0
 */
class AnnotationConfigParser implements BeanDefinitionParser {
	private static final String BREAKER_ADV_BEAN_NAME =
		"kite.circuitbreaker.interceptor.internalCircuitBreakerAdvisor";
	
	private static final String THROTTLE_ADV_BEAN_NAME =
		"kite.throttle.interceptor.internalThrottleAdvisor";
	
	private static final int BREAKER_ORDER = 0;
	private static final int THROTTLE_ORDER = 1;
	
	private static Logger log = LoggerFactory.getLogger(AnnotationConfigParser.class);

	public BeanDefinition parse(Element elem, ParserContext parserCtx) {
		
		// Right now only JDK 1.5 proxies are supported but later we'll support
		// CGLIB too. See Spring's AnnotationDrivenBeanDefinitionParser (part of
		// tx support) for implementation details.
		new AopAutoProxyConfigurer(elem, parserCtx);
		return null;
	}
	
	/**
	 * <p>
	 * Inner class to avoid introducing an AOP framework dependency unless it's
	 * actually required (i.e. unless we're in proxy mode). Right now that's the
	 * only mode that's available, so this is more like preparation for adding
	 * CGLIB support in the future.
	 * </p>
	 */
	private static class AopAutoProxyConfigurer {
		private final String tagName;
		private final ParserContext parserCtx;
		private final BeanDefinitionRegistry reg;
		private final Object src;
		private final int baseOrder;
		
		public AopAutoProxyConfigurer(Element elem, ParserContext parserCtx) {
			this.tagName = elem.getTagName();
			this.parserCtx = parserCtx;
			this.reg = parserCtx.getRegistry();
			this.src = parserCtx.extractSource(elem);
			
			this.baseOrder = elem.hasAttribute("order") ?
				Integer.parseInt(elem.getAttribute("order")) : 0;
			
			AopNamespaceUtils.registerAutoProxyCreatorIfNecessary(parserCtx, elem);
			configureBreaker();
			configureThrottle();
		}
		
		private void configureBreaker() {
			if (reg.containsBeanDefinition(BREAKER_ADV_BEAN_NAME)) { return; }
			
			RootBeanDefinition sdef = createDef(AnnotationCircuitBreakerSource.class);
			String sname = registerWithGeneratedName(sdef);
			
			RootBeanDefinition idef = createDef(CircuitBreakerInterceptor.class);
			addRuntimeProp(idef, "circuitBreakerSource", sname);
			String iname = registerWithGeneratedName(idef);
			
			RootBeanDefinition pdef = createDef(CircuitBreakerSourcePointcut.class);
			addRuntimeProp(pdef, "source", sname);
			String pname = registerWithGeneratedName(pdef);
			
			RootBeanDefinition adef = createDef(DefaultBeanFactoryPointcutAdvisor.class);
			addProp(adef, "adviceBeanName", iname);
			addRuntimeProp(adef, "pointcut", pname);
			addOrderProp(adef, BREAKER_ORDER);
			reg.registerBeanDefinition(BREAKER_ADV_BEAN_NAME, adef);
			
			// TODO Add pointcut definition
			doLogicalView(sdef, sname, idef, iname, adef, BREAKER_ADV_BEAN_NAME);
		}
		
		private void configureThrottle() {
			if (reg.containsBeanDefinition(THROTTLE_ADV_BEAN_NAME)) { return; }
			
			RootBeanDefinition sdef = createDef(AnnotationThrottleSource.class);
			String sname = registerWithGeneratedName(sdef);
			
			RootBeanDefinition idef = createDef(ThrottleInterceptor.class);
			addRuntimeProp(idef, "throttleSource", sname);
			String iname = registerWithGeneratedName(idef);
			
			RootBeanDefinition pdef = createDef(ThrottleSourcePointcut.class);
			addRuntimeProp(pdef, "source", sname);
			String pname = registerWithGeneratedName(pdef);
			
			RootBeanDefinition adef = createDef(DefaultBeanFactoryPointcutAdvisor.class);
			addProp(adef, "adviceBeanName", iname);
			addRuntimeProp(adef, "pointcut", pname);
			addOrderProp(adef, THROTTLE_ORDER);
			reg.registerBeanDefinition(THROTTLE_ADV_BEAN_NAME, adef);
			
			// TODO Add pointcut definition
			doLogicalView(sdef, sname, idef, iname, adef, THROTTLE_ADV_BEAN_NAME);
		}
		
		
		// ====================================================================
		// Helper methods
		// ====================================================================
		
		private RootBeanDefinition createDef(Class<?> clazz) {
			RootBeanDefinition def = new RootBeanDefinition(clazz);
			def.setSource(src);
			def.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
			return def;
		}
		
		private void addProp(RootBeanDefinition def, String name, Object value) {
			def.getPropertyValues().add(name, value);
		}
		
		private void addRuntimeProp(RootBeanDefinition def, String name, String value) {
			addProp(def, name, new RuntimeBeanReference(value));
		}
		
		private void addOrderProp(RootBeanDefinition def, int offset) {
			addProp(def, "order", baseOrder + offset);
		}
		
		private String registerWithGeneratedName(RootBeanDefinition def) {
			return parserCtx.getReaderContext().registerWithGeneratedName(def);
		}
		
		// Describes logical view of BeanDefinitions and BeanReferences for
		// visualization, e.g., in Spring IDE. See ComponentDefinition Javadocs
		// for more information. Note that this requires Spring IDE integration
		// based on the Eclipse plug-in rather than the tool namespace technique.
		private void doLogicalView(
				RootBeanDefinition sdef, String sname,
				RootBeanDefinition idef, String iname,
				RootBeanDefinition adef, String aname) {
			
			CompositeComponentDefinition ldef = new CompositeComponentDefinition(tagName, src);
			addComp(ldef, sdef, sname);
			addComp(ldef, idef, iname);
			addComp(ldef, adef, aname);
			parserCtx.registerComponent(ldef);
			log.info("Registered {} components", tagName);
		}
		
		private void addComp(CompositeComponentDefinition ldef, RootBeanDefinition def, String name) {
			ldef.addNestedComponent(new BeanComponentDefinition(def, name));
		}
	}
}
