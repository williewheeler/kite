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
package org.zkybase.kite;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.aopalliance.intercept.MethodInvocation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zkybase.kite.guard.CircuitBreakerTemplate;
import org.zkybase.kite.interceptor.GuardListInterceptor;
import org.zkybase.kite.interceptor.GuardListSource;

/**
 * @author Willie Wheeler (willie.wheeler@gmail.com)
 * @since 1.0
 */
public class GuardListInterceptorTests {
	
	// Class under test
	@InjectMocks private GuardListInterceptor interceptorWithGuards;
	@InjectMocks private GuardListInterceptor interceptorNoGuards;
	
	// Dependencies
	@Mock private GuardListSource sourceWithGuards;
	@Mock private GuardListSource sourceNoGuards;
	
	// Test objects
	@Mock private MethodInvocation invocation;
	@Mock private CircuitBreakerTemplate breaker;
	private List<Guard> guards;
	
	@Before
	@SuppressWarnings("unchecked")
	public void setUp() throws Throwable {
		this.interceptorWithGuards = new GuardListInterceptor();
		this.interceptorNoGuards = new GuardListInterceptor();
		
		MockitoAnnotations.initMocks(this);
		
		when(invocation.getMethod()).thenReturn(String.class.getMethod("toString"));
		when(invocation.getThis()).thenReturn("Willie Wheeler");
		when(invocation.proceed()).thenReturn("williewheeler");
		
		when(breaker.execute(isA(GuardCallback.class))).thenReturn("williewheeler");
		
		this.guards = new ArrayList<Guard>();
		guards.add(breaker);
		
		when(sourceWithGuards.getGuards(isA(Method.class), isA(Class.class))).thenReturn(guards);
		interceptorWithGuards.setSource(sourceWithGuards);
		
		when(sourceNoGuards.getGuards(isA(Method.class), isA(Class.class))).thenReturn(null);
		interceptorNoGuards.setSource(sourceNoGuards);
	}
	
	@After
	public void tearDown() throws Exception { }
	
	@Test
	public void testInvokeWorksWithGuards() throws Throwable {
		String result = (String) interceptorWithGuards.invoke(invocation);
		assertThat(result, is("williewheeler"));
	}
	
	@Test
	public void testInvokeWorksNoGuards() throws Throwable {
		String result = (String) interceptorNoGuards.invoke(invocation);
		assertThat(result, is("williewheeler"));
	}
}
