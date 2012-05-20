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
package org.zkybase.kite.exception;

/**
 * Runtime exception indicating that a call protected by a rate-limiting throttle failed due to too many requests in the
 * time window.
 * 
 * @author Willie Wheeler (willie.wheeler@gmail.com)
 * @since 1.0
 */
@SuppressWarnings("serial")
public class RateLimitExceededException extends GuardException {
	private int limit;
	
	public RateLimitExceededException(int limit) {
		super("Rate limit is " + limit);
		this.limit = limit;
	}
	
	public int getLimit() { return limit; }
}
