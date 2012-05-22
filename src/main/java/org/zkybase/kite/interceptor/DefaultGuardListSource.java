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
 * Source with a fixed guard list.
 * 
 * @author Willie Wheeler (willie.wheeler@gmail.com)
 * @since 1.0
 */
public class DefaultGuardListSource implements GuardListSource {
	private List<Guard> guards;
	
	public List<Guard> getGuards() { return guards; }
	
	public void setGuards(List<Guard> guards) { this.guards = guards; }
	
	/* (non-Javadoc)
	 * @see org.zkybase.kite.interceptor.GuardListSource#getGuards(java.lang.reflect.Method, java.lang.Class)
	 */
	@Override
	public List<Guard> getGuards(Method method, Class<?> targetClass) { return guards; }
}
