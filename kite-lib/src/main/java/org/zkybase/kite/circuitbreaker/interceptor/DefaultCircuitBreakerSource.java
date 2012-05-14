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

import org.zkybase.kite.circuitbreaker.CircuitBreakerTemplate;


/**
 * @author Willie Wheeler
 * @since 1.0
 */
public class DefaultCircuitBreakerSource implements CircuitBreakerSource {
	private CircuitBreakerTemplate breaker;
	
	public CircuitBreakerTemplate getBreaker() {
		return breaker;
	}

	public void setBreaker(CircuitBreakerTemplate breaker) {
		this.breaker = breaker;
	}

	public CircuitBreakerTemplate getBreaker(Method method, Class<?> targetClass) {
		return breaker;
	}
}
