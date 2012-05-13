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
package com.williewheeler.kite.circuitbreaker.interceptor;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import com.williewheeler.kite.annotation.GuardedByCircuitBreaker;
import com.williewheeler.kite.circuitbreaker.CircuitBreakerTemplate;

// TODO Consider implementing breaker cache. See Spring's
// AbstractFallbackTransactionAttributeSource

/**
 * @version $Id: AnnotationCircuitBreakerSource.java 75 2010-03-29 07:34:59Z willie.wheeler $
 * @author Willie Wheeler
 */
@SuppressWarnings("serial")
public class AnnotationCircuitBreakerSource
	implements CircuitBreakerSource, BeanFactoryAware, Serializable {
	
	private static final Class<GuardedByCircuitBreaker> ANN_CLASS = GuardedByCircuitBreaker.class;
//	private static Logger log = LoggerFactory.getLogger(AnnotationCircuitBreakerSource.class);
	
	private BeanFactory beanFactory;

	/**
	 * @param beanFactory
	 */
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	public CircuitBreakerTemplate getBreaker(Method method, Class<?> targetClass) {
		Assert.notNull(method, "method can't be null");
		
		// Method may be on an interface, but we need annotations from the
		// target class. If target class is null, method will be unchanged.
		Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
		
		// If we are dealing with a method with generic parameters, find the
		// original, bridged method.
		specificMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);
		
		CircuitBreakerTemplate breaker = parseAnnotation(specificMethod);
		return (breaker != null ? breaker : parseAnnotation(method));
	}
	
	// Based on SpringTransactionAnnotationParser. Don't worry about meta-
	// annotations here, because GuardedByCircuitBreaker is allowed only on
	// methods.
	private CircuitBreakerTemplate parseAnnotation(Method method) {
		GuardedByCircuitBreaker ann = method.getAnnotation(ANN_CLASS);
		return (ann == null ? null :
			beanFactory.getBean(ann.value(), CircuitBreakerTemplate.class));
	}
}
