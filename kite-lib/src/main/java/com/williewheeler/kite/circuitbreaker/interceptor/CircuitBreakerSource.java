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

import java.lang.reflect.Method;

import com.williewheeler.kite.circuitbreaker.CircuitBreakerTemplate;

/**
 * <p>
 * Interface used by {@link CircuitBreakerInterceptor} to provide a level of
 * indirection for obtaining circuit breakers. Implementations know how to
 * source circuit breakers, whether from configuration, annotations or anywhere
 * else.
 * </p>
 * 
 * @author Willie Wheeler
 * @since 1.0
 */
public interface CircuitBreakerSource {

	/**
	 * <p>
	 * Return the circuit breaker for this method, or <code>null</code> if the
	 * method isn't guarded by a circuit breaker.
	 * </p>
	 * 
	 * @param method
	 *            method
	 * @param targetClass
	 *            target class. May be <code>null</code>, in which case the
	 *            declaring class of the method must be used.
	 * @return the matching circuit breaker, or <code>null</code> if none found
	 */
	CircuitBreakerTemplate getBreaker(Method method, Class<?> targetClass);
}
