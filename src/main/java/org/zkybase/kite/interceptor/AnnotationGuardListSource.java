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
package org.zkybase.kite.interceptor;

import static org.springframework.util.Assert.notNull;

import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.util.ClassUtils;
import org.zkybase.kite.Guard;
import org.zkybase.kite.GuardedBy;

/**
 * Strategy returning a guard list sourced from the {@link GuardedBy} annotation.
 * 
 * @author Willie Wheeler (willie.wheeler@gmail.com)
 * @since 1.0
 */
@SuppressWarnings("serial")
public class AnnotationGuardListSource implements GuardListSource, BeanFactoryAware, Serializable {
	private BeanFactory beanFactory;

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory
	 * (org.springframework.beans.factory.BeanFactory)
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException { this.beanFactory = beanFactory; }

	/* (non-Javadoc)
	 * @see org.zkybase.kite.interceptor.GuardListSource#getGuards(java.lang.reflect.Method, java.lang.Class)
	 */
	@Override
	public List<Guard> getGuards(Method method, Class<?> targetClass) {
		notNull(method, "method can't be null");
		
		// Method may be on an interface, but we need annotations from the target class. If target class is null, method
		// will be unchanged.
		Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
		
		// If we are dealing with a method with generic parameters, find the original method.
		specificMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);
		
		List<Guard> guards = parseAnnotation(specificMethod);
		return (guards != null ? guards : parseAnnotation(method));
	}
	
	private List<Guard> parseAnnotation(AnnotatedElement elem) {
		assert (elem != null);
		return parseAnnotation(elem.getAnnotation(GuardedBy.class));
	}
	
	private List<Guard> parseAnnotation(GuardedBy ann) {
		if (ann == null) { return null; }
		
		List<Guard> guards = new ArrayList<Guard>();
		String[] guardNames = ann.value();
		for (String guardName : guardNames) {
			guards.add(beanFactory.getBean(guardName, Guard.class));
		}
		return guards;
	}
}
