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

import java.io.Serializable;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.util.ObjectUtils;

/**
 * @version $Id$
 * @author Willie Wheeler (willie.wheeler@gmail.com)
 */
@SuppressWarnings("serial")
public class GuardListSourcePointcut extends StaticMethodMatcherPointcut implements Serializable {
	private static final Logger log = LoggerFactory.getLogger(GuardListSourcePointcut.class);
	
	private GuardListSource source;
	
	public GuardListSource getSource() { return source; }
	
	public void setSource(GuardListSource source) { this.source = source; }
	
	/* (non-Javadoc)
	 * @see org.springframework.aop.MethodMatcher#matches(java.lang.reflect.Method, java.lang.Class)
	 */
	@Override
	public boolean matches(Method method, Class<?> targetClass) {
		if (source == null) {
			throw new IllegalStateException("source can't be null");
		}
		
		boolean match = (source.getGuards(method, targetClass) != null);
		if (match) {
			log.debug("Found pointcut match for {}.{}", targetClass.getName(), method.getName());
		}
		return match;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object other) {
		if (this == other) { return true; }
		if (!(other instanceof GuardListSourcePointcut)) { return false; }
		GuardListSourcePointcut otherPc = (GuardListSourcePointcut) other;
		return ObjectUtils.nullSafeEquals(source, otherPc.source);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() { return GuardListSourcePointcut.class.hashCode(); }
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() { return getClass().getName() + ": " + source; }

}
