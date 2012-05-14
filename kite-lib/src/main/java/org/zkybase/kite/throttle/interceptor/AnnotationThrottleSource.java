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
package org.zkybase.kite.throttle.interceptor;

import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.zkybase.kite.annotation.GuardedByThrottle;
import org.zkybase.kite.throttle.ThrottleTemplate;


// TODO Consider implementing throttle. See Spring's
// AbstractFallbackTransactionAttributeSource

/**
 * @version $Id$
 * @author Willie Wheeler
 */
@SuppressWarnings("serial")
public class AnnotationThrottleSource
	implements ThrottleSource, BeanFactoryAware, Serializable {
	
	private static final Class<GuardedByThrottle> ANN_CLASS = GuardedByThrottle.class;
//	private static Logger log = LoggerFactory.getLogger(AnnotationThrottleSource.class);
	
	private BeanFactory beanFactory;

	/**
	 * @param beanFactory
	 */
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	public ThrottleTemplate getThrottle(Method method, Class<?> targetClass) {
		Assert.notNull(method, "method can't be null");
		
		// Method may be on an interface, but we need annotations from the
		// target class. If target class is null, method will be unchanged.
		Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
		
		// If we are dealing with a method with generic parameters, find the
		// original method.
		specificMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);
		
		ThrottleTemplate throttle = parseAnnotation(specificMethod);
		if (throttle != null) {
			return throttle;
		} else { 
			return parseAnnotation(method);
		}
	}
	
	// Based on SpringTransactionAnnotationParser
	private ThrottleTemplate parseAnnotation(AnnotatedElement elem) {
		assert (elem != null);
		
		GuardedByThrottle ann = elem.getAnnotation(ANN_CLASS);
		
		// Meta-annotation search (Spring 3)
		// FIXME It looks like this is getting annotations off the element, not
		// off of other annotations...
//		if (ann == null) {
//			for (Annotation metaAnn : elem.getAnnotations()) {
//				ann = metaAnn.annotationType().getAnnotation(ANN_CLASS);
//				if (ann != null) { break; }
//			}
//		}
		
		// If ann is null, then this returns null
		return parseAnnotation(ann);
	}

	/**
	 * @param ann
	 *            annotation (<code>null</code> ok)
	 * @return referenced throttle, or <code>null</code> if <code>ann</code> is
	 *         <code>null</code>
	 * @throws BeansException
	 *             if the annotation references a throttle that doesn't exist
	 */
	private ThrottleTemplate parseAnnotation(GuardedByThrottle ann) {
		if (ann == null) { return null; }
		
		// Throws BeansException if the bean can't be found
		return beanFactory.getBean(ann.value(), ThrottleTemplate.class);
	}
}
