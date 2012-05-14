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
package org.zkybase.kite.circuitbreaker.interceptor;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkybase.kite.circuitbreaker.CircuitBreakerCallback;
import org.zkybase.kite.circuitbreaker.CircuitBreakerTemplate;


/**
 * <p>
 * AOP interceptor for invoking methods inside a circuit breaker. Supports
 * arbitrary breaker sources, including XML- and annotation-based configuration.
 * </p>
 * 
 * @author Willie Wheeler
 * @since 1.0
 */
public class CircuitBreakerInterceptor implements MethodInterceptor {
	private static Logger log = LoggerFactory.getLogger(CircuitBreakerInterceptor.class);
	
	private CircuitBreakerSource source;
	
	public void setCircuitBreakerSource(CircuitBreakerSource source) {
		this.source = source;
	}

	/**
	 * <p>
	 * Invokes the method in the circuit breaker.
	 * </p>
	 * 
	 * @param invocation
	 *            method invocation
	 * @throws any
	 *             throwable that occurs as a result of the invocation
	 */
	@SuppressWarnings("unchecked")
	public Object invoke(final MethodInvocation invocation) throws Throwable {
		Method method = invocation.getMethod();
		String methodName = method.getName();
		
		// thisObj can be null if the invocation's static part (i.e. static
		// joinpoint) is static (i.e. class-scoped)
		Object thisObj = invocation.getThis();
		Class clazz = (thisObj != null ? thisObj.getClass() : null);
		
		CircuitBreakerTemplate breaker = source.getBreaker(method, clazz);
		if (breaker != null) {
			log.debug("Invoking method {} in breaker {}", methodName, breaker.getBeanName());
			return breaker.execute(new CircuitBreakerCallback() {
				public Object doInCircuitBreaker() throws Exception {
					try {
						return invocation.proceed();
					} catch (Exception e) {
						throw e;
					} catch (Throwable t) {
						throw new RuntimeException(t);
					}
				}
			});
		} else {
			log.debug("Invoking {} without breaker", methodName);
			return invocation.proceed();
		}
	}
}
