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
package com.williewheeler.kite.throttle.interceptor;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.util.ObjectUtils;

/**
 * A throttle pointcut that matches iff either the underlying source has a throttle for the given method.
 * 
 * @author Willie Wheeler
 * @since 1.0
 */
@SuppressWarnings("serial")
public class ThrottleSourcePointcut
	extends StaticMethodMatcherPointcut implements Serializable {
	
	private static Logger log = LoggerFactory.getLogger(ThrottleSourcePointcut.class);
	
	private ThrottleSource source;
	
	public ThrottleSource getSource() { return source; }

	public void setSource(ThrottleSource source) { this.source = source; }

	public boolean matches(Method method, Class<?> targetClass) {
		if (source == null) {
			throw new IllegalStateException("source can't be null");
		}
		
		boolean match = source.getThrottle(method, targetClass) != null;
		if (match) {
			log.debug("Found pointcut match for {}.{}", targetClass.getName(), method.getName());
		}
		return match;
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) { return true; }
		if (!(other instanceof ThrottleSourcePointcut)) { return false; }
		ThrottleSourcePointcut otherPc = (ThrottleSourcePointcut) other;
		return ObjectUtils.nullSafeEquals(source, otherPc.source);
	}
	
	@Override
	public int hashCode() {
		return ThrottleSourcePointcut.class.hashCode();
	}
	
	@Override
	public String toString() {
		return getClass().getName() + ": " + source;
	}
}
