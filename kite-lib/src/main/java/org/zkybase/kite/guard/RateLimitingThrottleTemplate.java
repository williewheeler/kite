package org.zkybase.kite.guard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.zkybase.kite.AbstractGuard;
import org.zkybase.kite.GuardCallback;
import org.zkybase.kite.exception.RateLimitExceededException;

/**
 * @author Willie Wheeler (willie.wheeler@gmail.com)
 * @since 1.0
 */
@ManagedResource
public class RateLimitingThrottleTemplate extends AbstractGuard {
	private static Logger log = LoggerFactory.getLogger(RateLimitingThrottleTemplate.class);
	
	private final int limit;
	
	/**
	 * @param limit maximum number of requests permitted in the time window
	 */
	public RateLimitingThrottleTemplate(int limit) {
		if (limit < 1) {
			throw new IllegalArgumentException("limit must be >= 1");
		}
		this.limit = limit;
	}
	
	public int getLimit() { return limit; }
	
	public <T> T execute(GuardCallback<T> action) throws Throwable {
		log.debug("Entered rate-limiting throttle");
		try {
			if (withinLimit()) {
				return action.doInGuard();
			} else {
				log.warn("Request rejected: rate limit {} exceeded", limit);
				throw new RateLimitExceededException(limit);
			}
		} finally {
			log.debug("Exiting rate-limiting throttle");
		}
	}
	
	private boolean withinLimit() {
		// FIXME
		return true;
	}
}
