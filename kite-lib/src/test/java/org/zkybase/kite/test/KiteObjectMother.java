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
package org.zkybase.kite.test;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.sql.SQLException;

import org.aopalliance.intercept.MethodInvocation;
import org.zkybase.kite.GuardCallback;
import org.zkybase.kite.guard.CircuitBreakerTemplate;

/**
 * Object mother for testing Kite.
 * 
 * @author Willie Wheeler (willie.wheeler@gmail.com)
 * @since 1.0
 */
public final class KiteObjectMother {
	private static KiteObjectMother mom = new KiteObjectMother();
	
	// Circuit breakers
	private CircuitBreakerTemplate breaker;
	private GuardCallback<String> goodBreakerAction;
	private GuardCallback<String> badBreakerAction;
	private GuardCallback<String> breakerActionThatAlwaysThrowsSqlException;
	
	// Throttles
	private GuardCallback<String> throttleAction;
	private GuardCallback<String> slowThrottleAction;
	
	// Generic
	private Method dummyMethod;
	private MethodInvocation invocation;
	private Exception serviceException = new RuntimeException("fail");

	public static KiteObjectMother instance() { return mom; }

	private KiteObjectMother() {
		try {
			initGenericStuff();
			initCircuitBreakerStuff();
			initThrottleStuff();
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
	
	private void initGenericStuff() {
		this.dummyMethod = String.class.getMethods()[0];
		
		this.invocation = mock(MethodInvocation.class);
		when(invocation.getMethod()).thenReturn(dummyMethod);
		when(invocation.getThis()).thenReturn("dummyObject");
		try {
			when(invocation.proceed()).thenReturn("Winterlong");
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void initCircuitBreakerStuff() throws Throwable {
		this.breaker = mock(CircuitBreakerTemplate.class);
		when(breaker.execute(isA(GuardCallback.class))).thenReturn("Planet of Sound");
		
		this.goodBreakerAction = new GuardCallback<String>() {
			public String doInGuard() throws Throwable {
				return "good";
			}
		};
		
		this.badBreakerAction = new GuardCallback<String>() {
			public String doInGuard() throws Throwable {
				// Use shared exception to speed up calls in tight loops. This
				// is much faster than creating a new exception each time.
				throw serviceException;
			}
		};
		
		this.breakerActionThatAlwaysThrowsSqlException = new GuardCallback<String>() {
			public String doInGuard() throws Throwable {
				throw new SQLException();
			}
		};
	}

	private void initThrottleStuff() {
		this.throttleAction = new GuardCallback<String>() {
			public String doInGuard() throws Throwable {
				return "good";
			}
		};
		
		this.slowThrottleAction = new GuardCallback<String>() {
			public String doInGuard() throws Throwable {
				Thread.sleep(1000L);
				return "good";
			}
		};
	}
	
	public Method getDummyMethod() { return dummyMethod; }
	
	/**
	 * Returns a method invocation that always returns the string "Winterlong" when calling <code>proceed()</code> with
	 * any argument.
	 * 
	 * @return method invocation that returns the string "Winterlong" when
	 *         calling <code>proceed()</code>
	 */
	public MethodInvocation getMethodInvocation() { return invocation; }

	/**
	 * Returns a circuit breaker that always returns the string "Planet of Sound" when calling <code>execute()</code>
	 * with any argument.
	 * 
	 * @return method invocation that returns the string "Planet of Sound" when
	 *         calling <code>execute()</code>
	 */
	public CircuitBreakerTemplate getCircuitBreaker() { return breaker; }

	public GuardCallback<String> getGoodCircuitBreakerAction() {
		return goodBreakerAction;
	}

	public GuardCallback<String> getBadCircuitBreakerAction() {
		return badBreakerAction;
	}
	
	public GuardCallback<String> getCircuitBreakerActionThatAlwaysThrowsSqlException() {
		return breakerActionThatAlwaysThrowsSqlException;
	}
	
	public GuardCallback<String> getThrottleAction() {
		return throttleAction;
	}
	
	public GuardCallback<String> getSlowThrottleAction() {
		return slowThrottleAction;
	}
}
