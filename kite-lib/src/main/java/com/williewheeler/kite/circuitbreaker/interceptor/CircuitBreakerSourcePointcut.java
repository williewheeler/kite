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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.util.ObjectUtils;

/**
 * <p>
 * A circuit breaker pointcut that matches iff the underlying source has a breaker for the given method. 
 * </p>
 * 
 * @author Willie Wheeler
 * @since 1.0
 */
@SuppressWarnings("serial")
public class CircuitBreakerSourcePointcut
	extends StaticMethodMatcherPointcut implements Serializable {
	
	private static Logger log = LoggerFactory.getLogger(CircuitBreakerSourcePointcut.class);
	
	private CircuitBreakerSource source;
	
	public CircuitBreakerSource getSource() { return source; }

	public void setSource(CircuitBreakerSource source) { this.source = source; }

	/**
	 * @param method
	 *            method to evaluate
	 * @param targetClass
	 *            target class to evaluate (if <code>null</code>, then we
	 *            evaluate against the method's declaring class
	 * @return boolean indicating whether the target method is eligible for
	 *         advice
	 */
	public boolean matches(Method method, Class<?> targetClass) {
		if (source == null) {
			throw new IllegalStateException("source can't be null");
		}
		
		// Hm, the existence of a circuit breaker source is actually runtime
		// information, not static. Does that violate the spirit of the
		// StaticMethodMatcher contract? (The contract states explicitly that
		// we won't get runtime info about the method call, but I don't think it
		// says anything about other runtime information...) At any rate, it
		// definitely wouldn't make any sense to declare this as a
		// DynamicMethodMatcherPointcut since the method arguments never matter.
		boolean match = source.getBreaker(method, targetClass) != null;
		if (match) {
			Class<?> clazz = (targetClass != null ? targetClass : method.getDeclaringClass());
			log.debug("Found pointcut match for {}.{}", clazz.getName(), method.getName());
		}
		return match;
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) { return true; }
		if (!(other instanceof CircuitBreakerSourcePointcut)) { return false; }
		CircuitBreakerSourcePointcut otherPc = (CircuitBreakerSourcePointcut) other;
		return ObjectUtils.nullSafeEquals(source, otherPc.source);
	}
	
	@Override
	public int hashCode() {
		return CircuitBreakerSourcePointcut.class.hashCode();
	}
	
	@Override
	public String toString() {
		return getClass().getName() + ": " + source;
	}
}
