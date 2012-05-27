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
import java.util.List;

import org.zkybase.kite.Guard;

/**
 * <p>
 * Strategy interface to return the list of guards associated with a given joinpoint. The {@link GuardListInterceptor}
 * uses this to determine which guards to apply to the joinpoint.
 * </p>
 * <p>
 * One strategy, for example, is the {@link DefaultGuardListSource}, which uses a fixed list, usually specified
 * explicitly in the XML configuration. Another strategy, the {@link AnnotationGuardListSource}, uses annotations to
 * specify the guards. It's possible to imagine even more dynamic guard list sources, such as a database. The ops team
 * might for instance dynamically create and apply guard lists in a database through a management interface.
 * </p>
 * 
 * @author Willie Wheeler (willie.wheeler@gmail.com)
 * @since 1.0
 */
public interface GuardListSource {
	
	/**
	 * Return the list of guards for a method, or <code>null</code> if the method is unguarded.
	 * 
	 * @param method
	 *            method
	 * @param targetClass
	 *            target class. May be <code>null</code>, in which case the declaring class of the method must be used.
	 * @return the matching guard list, or <code>null</code> if none found
	 */
	List<Guard> getGuards(Method method, Class<?> targetClass);
}
