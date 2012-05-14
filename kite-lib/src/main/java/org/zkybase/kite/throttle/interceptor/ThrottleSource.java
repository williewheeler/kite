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

import org.zkybase.kite.throttle.ThrottleTemplate;


/**
 * Interface used by {@link ThrottleInterceptor}. Implementations know how to source throttles, whether from
 * configuration, annotations or anywhere else.
 * 
 * @author Willie Wheeler
 * @since 1.0
 */
public interface ThrottleSource {

	/**
	 * <p>
	 * Return the throttle for this method, or <code>null</code> if the method
	 * isn't guarded by a throttle.
	 * </p>
	 * 
	 * @param method
	 *            method
	 * @param targetClass
	 *            target class. May be <code>null</code>, in which case the
	 *            declaring class of the method must be used.
	 * @return the matching throttle, or <code>null</code> if none found
	 */
	ThrottleTemplate getThrottle(Method method, Class<?> targetClass);

}
