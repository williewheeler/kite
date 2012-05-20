package org.zkybase.kite.guard;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.zkybase.kite.AbstractGuard;
import org.zkybase.kite.GuardCallback;
import org.zkybase.kite.exception.RateLimitExceededException;
import org.zkybase.kite.exception.UnauthenticatedException;

/**
 * <p>
 * A rate-limiting throttle. Currently this guard rate-limits on an hourly basis. A future version of this guard will
 * allow a range of options with respect to the time window.
 * </p>
 * <p>
 * Note that all counts reset on the hour, as measured by the wall clock.
 * </p>
 * 
 * @author Willie Wheeler (willie.wheeler@gmail.com)
 * @since 1.0
 */
@ManagedResource
public class RateLimitingThrottleTemplate extends AbstractGuard {
	private static final int MILLIS_PER_HOUR = 1000 * 60 * 60;
	private static Logger log = LoggerFactory.getLogger(RateLimitingThrottleTemplate.class);
	
	private final int limit;
	
	private volatile int currentHour;
	private final Map<Object, Integer> counts = new ConcurrentHashMap<Object, Integer>();
	
	/**
	 * @param limit maximum number of requests permitted in the time window
	 */
	public RateLimitingThrottleTemplate(int limit) {
		if (limit < 1) {
			throw new IllegalArgumentException("limit must be >= 1");
		}
		this.limit = limit;
		this.currentHour = (int) (System.currentTimeMillis() / MILLIS_PER_HOUR);
	}
	
	public int getLimit() { return limit; }
	
	public <T> T execute(GuardCallback<T> action) throws Throwable {
		resetCountsOnTheHour();
		Object principal = getPrincipal();
		int count = getCount(principal);
		
		if (++count <= limit) {
			log.debug("principal={}, count={}", principal, count);
			counts.put(principal, count);
			return action.doInGuard();
		} else {
			log.warn("Request rejected: rate limit {} exceeded", limit);
			throw new RateLimitExceededException(limit);
		}
	}
	
	private void resetCountsOnTheHour() {
		int newHour = (int) (System.currentTimeMillis() / MILLIS_PER_HOUR);
		if (newHour > currentHour) {
			this.currentHour = newHour;
			counts.clear();
		}
	}
	
	private Object getPrincipal() {
		SecurityContext context = SecurityContextHolder.getContext();
		Authentication auth = context.getAuthentication();
		
		// FIXME There's probably a better way to detect anonymous auth.
		if (auth == null || auth instanceof AnonymousAuthenticationToken) {
			log.debug("Authentication required");
			throw new UnauthenticatedException();
		}
		
		return auth.getPrincipal();
	}
	
	private int getCount(Object principal) {
		Integer count = counts.get(principal);
		if (count == null) { count = 0; }
		return count;
	}
}
