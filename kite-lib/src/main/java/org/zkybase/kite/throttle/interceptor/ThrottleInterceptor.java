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

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkybase.kite.throttle.ThrottleCallback;
import org.zkybase.kite.throttle.ThrottleTemplate;


/**
 * <p>
 * Interceptor to use the {@link kite.throttle.ThrottleTemplate} in
 * an AOP setting.
 * </p>
 * 
 * @version $Id: ThrottleInterceptor.java 90 2011-01-20 05:29:41Z williewheeler $
 * @author Willie Wheeler
 * @since 1.0
 */
public class ThrottleInterceptor implements MethodInterceptor {
	private static Logger log =
		LoggerFactory.getLogger(ThrottleInterceptor.class);
	
	private ThrottleSource source;
	
	public ThrottleSource getThrottleSource() { return source; }
	
	public void setThrottleSource(ThrottleSource source) { this.source = source; }
	
	@SuppressWarnings("unchecked")
	public Object invoke(final MethodInvocation invocation) throws Throwable {
		Method method = invocation.getMethod();
		String methodName = method.getName();
		Object thisObj = invocation.getThis();
		Class<?> clazz = (thisObj != null ? thisObj.getClass() : null);
		ThrottleTemplate throttle = source.getThrottle(method, clazz);
		
		if (throttle != null) {
			log.debug("Invoking method {} in throttle {}", methodName, throttle.getBeanName());
			return throttle.execute(new ThrottleCallback() {
				public Object doInThrottle() throws Exception {
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
			log.debug("Invoking {} without throttle", methodName);
			return invocation.proceed();
		}
	}
}
