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
package com.williewheeler.kite.throttle;

import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * <p>
 * Template component that fails with an exception when a concurrency threshold
 * is exceeded.
 * </p>
 * <p>
 * Implementation is based on a counting semaphore.
 * </p>
 * 
 * @author Willie Wheeler
 * @since 1.0
 */
@ManagedResource
public class ThrottleTemplate implements BeanNameAware {
	private static Logger log = LoggerFactory.getLogger(ThrottleTemplate.class);
	
	private String beanName;
	private final int limit;
	private final Semaphore semaphore;

	/**
	 * @param limit
	 * @throws IllegalArgumentException if limit &lt; 1
	 */
	public ThrottleTemplate(int limit) {
		if (limit < 1) {
			throw new IllegalArgumentException("limit must be >= 1");
		}
		this.limit = limit;
		this.semaphore = new Semaphore(limit, true);
	}
	
	@ManagedAttribute(description = "Throttle name")
	public String getBeanName() { return beanName; }
	
	public void setBeanName(String beanName) { this.beanName = beanName; }
	
	@ManagedAttribute(description = "Concurrency limit, after which requests are rejected")
	public int getLimit() { return limit; }
	
	public <T> T execute(ThrottleCallback<T> action) throws Exception {
		log.debug("Entered concurrency throttle");
		try {
			if (semaphore.tryAcquire()) {
				try {
					return action.doInThrottle();
				} finally {
					semaphore.release();
				}
			} else {
				log.warn("Request rejected: concurrency limit {} exceeded", limit);
				throw new ConcurrencyLimitExceededException(limit);
			}
		} finally {
			log.debug("Exiting concurrency throttle");
		}
	}
}
