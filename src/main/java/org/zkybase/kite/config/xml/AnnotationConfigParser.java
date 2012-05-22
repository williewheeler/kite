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
import org.zkybase.kite.interceptor.AnnotationGuardListSource;
import org.zkybase.kite.interceptor.GuardListInterceptor;
import org.zkybase.kite.interceptor.GuardListSourcePointcut;


/**
 * Parses &lt;kite:annotation-config&gt; into Kite infrastructure beans.
 * 
 * @author Willie Wheeler (willie.wheeler@gmail.com)
 * @since 1.0
 */
class AnnotationConfigParser implements BeanDefinitionParser {
	private static final String GUARD_LIST_ADV_BEAN_NAME = "org.zkybase.kite.interceptor.internalGuardListAdvisor";
	private static final Logger log = LoggerFactory.getLogger(AnnotationConfigParser.class);

	public BeanDefinition parse(Element elem, ParserContext parserCtx) {
		
		// Right now only JDK 1.5 proxies are supported but later we'll support CGLIB too. See Spring's
		// AnnotationDrivenBeanDefinitionParser (part of tx support) for implementation details.
		new AopAutoProxyConfigurer(elem, parserCtx);
		return null;
	}
	
	/**
	 * Inner class to avoid introducing an AOP framework dependency unless it's actually required (i.e. unless we're in
	 * proxy mode). Right now that's the only mode that's available, so this is more like preparation for adding CGLIB
	 * support in the future.
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
			
			this.baseOrder = elem.hasAttribute("order") ? Integer.parseInt(elem.getAttribute("order")) : 0;
			AopNamespaceUtils.registerAutoProxyCreatorIfNecessary(parserCtx, elem);
			configureGuardList();
		}
		
		private void configureGuardList() {
			if (reg.containsBeanDefinition(GUARD_LIST_ADV_BEAN_NAME)) { return; }
			
			RootBeanDefinition sdef = createDef(AnnotationGuardListSource.class);
			String sname = registerWithGeneratedName(sdef);
			
			RootBeanDefinition idef = createDef(GuardListInterceptor.class);
			addRuntimeProp(idef, "source", sname);
			String iname = registerWithGeneratedName(idef);
			
			RootBeanDefinition pdef = createDef(GuardListSourcePointcut.class);
			addRuntimeProp(pdef, "source", sname);
			String pname = registerWithGeneratedName(pdef);
			
			RootBeanDefinition adef = createDef(DefaultBeanFactoryPointcutAdvisor.class);
			addProp(adef, "adviceBeanName", iname);
			addRuntimeProp(adef, "pointcut", pname);
			addOrderProp(adef, 0);
			reg.registerBeanDefinition(GUARD_LIST_ADV_BEAN_NAME, adef);
			
			// TODO Add pointcut definition
			doLogicalView(sdef, sname, idef, iname, adef, GUARD_LIST_ADV_BEAN_NAME);
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
