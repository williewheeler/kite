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

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkybase.kite.Guard;
import org.zkybase.kite.GuardCallback;

/**
 * @author Willie Wheeler (willie.wheeler@gmail.com)
 * @since 1.0
 */
public class GuardListInterceptor implements MethodInterceptor {
	private static final Logger log = LoggerFactory.getLogger(GuardListInterceptor.class);
	
	private GuardListSource source;
	
	public GuardListSource getSource() { return source; }
	
	public void setSource(GuardListSource source) { this.source = source; }

	/* (non-Javadoc)
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
	 */
	@Override
	public Object invoke(final MethodInvocation invocation) throws Throwable {
		List<Guard> guards = getGuards(invocation);
		
		if (guards == null) {
			log.debug("Executing method {} without guards", invocation.getMethod().getName());
			return invocation.proceed();
		}
		
		// Build a chain of custom interceptors (one per guard), terminating with the invocation.
		LinkedList<Guard> guardStack = new LinkedList<Guard>(guards);
		Collections.reverse(guardStack);
		
		Guard lastGuard = guardStack.pop();
		Interceptor interceptor = new LastInterceptor(lastGuard, invocation);
		
		while (!guardStack.isEmpty()) {
			Guard guard = guardStack.pop();
			interceptor = new NotLastInterceptor(guard, interceptor);
		}
		
		return interceptor.invoke();
	}
	
	private List<Guard> getGuards(MethodInvocation invocation) {
		Method method = invocation.getMethod();
		
		// thisObj can be null if the invocation's static part (i.e. static joinpoint) is static (i.e. class-scoped)
		Object thisObj = invocation.getThis();
		Class<?> clazz = (thisObj != null ? thisObj.getClass() : null);
		
		return source.getGuards(method, clazz);
	}
	
	private static interface Interceptor {
		
		Object invoke() throws Throwable;
	}
	
	private static class NotLastInterceptor implements Interceptor {
		private Guard guard;
		private Interceptor interceptor;
		
		public NotLastInterceptor(Guard guard, Interceptor interceptor) {
			this.guard = guard;
			this.interceptor = interceptor;
		}
		
		@Override
		public Object invoke() throws Throwable {
			return guard.execute(new GuardCallback<Object>() {
				
				@Override
				public Object doInGuard() throws Throwable {
					return interceptor.invoke();
				}
			});
		}
	}
	
	private static class LastInterceptor implements Interceptor {
		private Guard guard;
		private MethodInvocation invocation;
		
		public LastInterceptor(Guard guard, MethodInvocation invocation) {
			this.guard = guard;
			this.invocation = invocation;
		}
		
		@Override
		public Object invoke() throws Throwable {
			return guard.execute(new GuardCallback<Object>() {

				@Override
				public Object doInGuard() throws Throwable {
					return invocation.proceed();
				}
			});
		}
	}
}
