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
package org.zkybase.kite.guard;

import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.zkybase.kite.AbstractGuard;
import org.zkybase.kite.GuardCallback;
import org.zkybase.kite.exception.ConcurrencyLimitExceededException;

/**
 * <p>
 * Template component that fails with an exception when a concurrency threshold is exceeded.
 * </p>
 * <p>
 * Implementation is based on a counting semaphore.
 * </p>
 * 
 * @author Willie Wheeler (willie.wheeler@gmail.com)
 * @since 1.0
 */
@ManagedResource
public class ConcurrencyThrottleTemplate extends AbstractGuard {
	private static Logger log = LoggerFactory.getLogger(ConcurrencyThrottleTemplate.class);
	
	private final int limit;
	private final Semaphore semaphore;

	/**
	 * @param limit
	 * @throws IllegalArgumentException if limit &lt; 1
	 */
	public ConcurrencyThrottleTemplate(int limit) {
		if (limit < 1) {
			throw new IllegalArgumentException("limit must be >= 1");
		}
		this.limit = limit;
		this.semaphore = new Semaphore(limit, true);
	}
	
	@ManagedAttribute(description = "Concurrency limit, after which requests are rejected")
	public int getLimit() { return limit; }
	
	public <T> T execute(GuardCallback<T> action) throws Throwable {
		if (semaphore.tryAcquire()) {
			try {
				return action.doInGuard();
			} finally {
				semaphore.release();
			}
		} else {
			log.warn("Request rejected: concurrency limit {} exceeded", limit);
			throw new ConcurrencyLimitExceededException(limit);
		}
	}
}
