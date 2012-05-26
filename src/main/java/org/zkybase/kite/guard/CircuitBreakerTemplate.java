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

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.zkybase.kite.AbstractGuard;
import org.zkybase.kite.GuardCallback;
import org.zkybase.kite.exception.CircuitOpenException;

/**
 * <p>
 * Template for circuit breakers, which are components designed to protect clients from broken services by preventing
 * faults from propagating across integration points.
 * </p>
 * <p>
 * For example, suppose that we have a web application that calls a web service. If the web service is having a problem
 * (e.g., severe latency or perhaps complete unavailability), we don't want this to create problems for the app. Instead
 * we want to isolate the problem.
 * </p>
 * <p>
 * The circuit breaker allows us to do just this. We associate a circuit breaker with each integration point, and the
 * breaker mediates calls from the client to the service. Under normal circumstances the breaker is in the closed state,
 * and calls pass through to the service. If however there is a problem, then the breaker goes into the open state for
 * some period of time. While the breaker is open, all attempts to call the service fail with a {@link
 * CircuitOpenException}. Once the problem is resolved, the breaker returns to the closed state and normal operations
 * resume.
 * </p>
 * <p>
 * <img src="doc-files/circuit-breaker-state-transition.png" alt="State transition diagram" />
 * </p>
 * <p>
 * The circuit breaker pattern is described in detail in Michael Nygard's book, <a
 * href="http://www.pragprog.com/titles/mnee/release-it">Release It!</a> (Pragmatic).
 * </p>
 * <p>
 * We expose this guard as a JMX MBean so it can be queried and manipulated in management contexts. You can, for
 * example, use JMX to trip and reset breakers manually.
 * </p>
 * 
 * @author Willie Wheeler (willie.wheeler@gmail.com)
 * @since 1.0
 */
@ManagedResource
public class CircuitBreakerTemplate extends AbstractGuard {
	public enum State { CLOSED, OPEN, HALF_OPEN };

	private static final long NO_SCHEDULED_RETRY = Long.MAX_VALUE;
	private static Logger log = LoggerFactory.getLogger(CircuitBreakerTemplate.class);
	
	// Configuration
	private int exceptionThreshold = 5;
	private long timeout = 30000L;
	private List<Class<? extends Exception>> handledExceptions = new ArrayList<Class<? extends Exception>>();

	// Volatile state
	private volatile State state = State.CLOSED;
	private final AtomicInteger exceptionCount = new AtomicInteger();
	private volatile long retryTime = NO_SCHEDULED_RETRY;
	
	public CircuitBreakerTemplate() { handledExceptions.add(Exception.class); }

	/**
	 * <p>
	 * Returns the exception threshold for this breaker. This is the number of
	 * exceptions causing the breaker to trip.
	 * </p>
	 * 
	 * @return exception threshold for this breaker
	 */
	@ManagedAttribute(description = "Breaker trips when threshold is reached")
	public int getExceptionThreshold() { return exceptionThreshold; }

	/**
	 * <p>
	 * Sets the exception threshold for this breaker. Once the threshold is
	 * reached, the breaker trips.
	 * </p>
	 * <p>
	 * The default exception threshold is 5.
	 * </p>
	 * 
	 * @param threshold
	 *            number of exceptions causing the breaker to trip
	 * @throws IllegalArgumentException
	 *             if threshold &lt; 1
	 */
	@ManagedAttribute(
		description = "Breaker trips when threshold is reached",
		defaultValue = "5")
	public void setExceptionThreshold(int threshold) {
		isTrue(threshold >= 1, "threshold must be >= 1");
		this.exceptionThreshold = threshold;
	}

	/**
	 * <p>
	 * Returns the open state timeout in milliseconds. After the timeout
	 * expires, the next call causes the breaker to go into the half-open state.
	 * </p>
	 * 
	 * @return open state timeout in milliseconds
	 */
	@ManagedAttribute(description = "Delay in ms before open breaker goes half-open")
	public long getTimeout() { return timeout; }

	/**
	 * <p>
	 * Sets the open state timeout in milliseconds.
	 * </p>
	 * 
	 * @param timeout
	 *            open state timeout in milliseconds
	 * @throws IllegalArgumentException
	 *             if timeout &lt; 0
	 */
	@ManagedAttribute(
		description = "Delay in ms before open breaker goes half-open",
		defaultValue = "30000")
	public void setTimeout(long timeout) {
		isTrue(timeout >= 0L, "timeout must be >= 0");
		this.timeout = timeout;
	}
	
	public List<Class<? extends Exception>> getHandledExceptions() {
		return handledExceptions;
	}
	
	public void setHandledExceptions(List<Class<? extends Exception>> exceptions) {
		notNull(exceptions, "handledExceptions can't be null");
		this.handledExceptions = exceptions;
	}

	/**
	 * <p>
	 * Returns the breaker's state, which is {@link State#CLOSED},
	 * {@link State#OPEN} or {@link State#HALF_OPEN}.
	 * </p>
	 * 
	 * @return breaker's state
	 */
	@ManagedAttribute(description = "Breaker state (closed, open, half-open)")
	public State getState() {
		if (state == State.OPEN) {
			if (System.currentTimeMillis() >= retryTime) {
				log.info("Setting circuit breaker half-open: {}", getName());
				this.state = State.HALF_OPEN;
			}
		}
		return state;
	}
	
	// For testing
	void setState(State state) { this.state = state; }

	@ManagedAttribute(description = "Number of exceptions since last reset")
	public int getExceptionCount() { return exceptionCount.get(); }
	
	@ManagedAttribute(description = "Breaker will retry circuit at or after this time")
	public long getRetryTime() { return retryTime; }

	/**
	 * <p>
	 * Restricted visibility method to support unit tests.
	 * </p>
	 * 
	 * @param exceptionCount
	 */
	void setExceptionCount(int exceptionCount) {
		this.exceptionCount.set(exceptionCount);
	}

	/**
	 * <p>
	 * Forces the breaker into the closed state, which is the default state. The
	 * closed state allows calls to pass through.
	 * </p>
	 */
	@ManagedOperation(description = "Resets the breaker")
	public void reset() {
		log.info("Resetting circuit breaker: {}", getName());
		this.state = State.CLOSED;
		this.exceptionCount.set(0);
	}

	/**
	 * <p>
	 * Forces the breaker into the open state. The open state prevents calls
	 * from passing through.
	 * </p>
	 */
	@ManagedOperation(description = "Trips the breaker, auto-resetting after timeout")
	public void trip() { trip(true); }
	
	@ManagedOperation(description = "Trips the breaker without auto-resetting")
	public void tripWithoutAutoReset() { trip(false); }
	
	private void trip(boolean autoReset) {
		log.warn("Tripping breaker {}, autoReset={}", getName(), autoReset);
		this.state = State.OPEN;

		// FIXME Don't want races to mosh explicit tripWithoutAutoReset() requests...
		this.retryTime = (autoReset ?
			System.currentTimeMillis() + timeout : NO_SCHEDULED_RETRY);
	}

	/**
	 * <p>
	 * Executes the specified action inside the circuit breaker.
	 * </p>
	 * 
	 * @param <T>
	 *            action return type
	 * @param action
	 *            action to execute
	 * @return result of the action
	 * @throws CircuitOpenException
	 *             if the breaker is in the open state
	 * @throws Exception
	 *             exception thrown by the action, if any
	 */
	public <T> T execute(GuardCallback<T> action) throws Exception {
		final State currState = getState();
		switch (currState) {

		case CLOSED:
			try {
				T value = action.doInGuard();
				this.exceptionCount.set(0);
				return value;
			} catch (Exception e) {
				if (isHandledException(e.getClass()) && exceptionCount.incrementAndGet() >= exceptionThreshold) {
					trip();
				}
				
				// In any event, throw the exception.
				throw e;
			}

		case OPEN:
			throw new CircuitOpenException();

		case HALF_OPEN:
			try {
				T value = action.doInGuard();
				reset();
				return value;
			} catch (Exception e) {
				if (isHandledException(e.getClass())) { trip(); }
				throw e;
			}

		default:
			// This shouldn't happen...
			throw new IllegalStateException("Unknown state: " + currState);
		}
	}

	// Check the exception against the list of handled exceptions.
	private boolean isHandledException(Class<? extends Exception> exceptionClass) {
		for (Class<? extends Exception> handledExceptionClass : handledExceptions) {
			if (handledExceptionClass.isAssignableFrom(exceptionClass)) {
				return true;
			}
		}
		return false;
	}
}
