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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zkybase.kite.GuardCallback;
import org.zkybase.kite.exception.CircuitOpenException;
import org.zkybase.kite.test.BarrierThread;
import org.zkybase.kite.test.KiteObjectMother;

/**
 * Test case for {@link CircuitBreakerTemplate}.
 * 
 * @author Willie Wheeler (willie.wheeler@gmail.com)
 * @since 1.0
 */
public class CircuitBreakerTemplateTestCase {
	private static KiteObjectMother mom = KiteObjectMother.instance();
	
	// Class under test
	private CircuitBreakerTemplate breaker;
	
	private GuardCallback<String> goodAction;
	private GuardCallback<String> badAction;
	private GuardCallback<String> actionThatAlwaysThrowsSqlException;

	@Before
	public void setUp() throws Exception {
		this.goodAction = mom.getGoodCircuitBreakerAction();
		this.badAction = mom.getBadCircuitBreakerAction();
		this.actionThatAlwaysThrowsSqlException = mom.getCircuitBreakerActionThatAlwaysThrowsSqlException();
		
		this.breaker = new CircuitBreakerTemplate();
		breaker.setBeanName("messageServiceBreaker");

		// Set this fairly high so we can safely assume that the breaker is open immediately after a trip.
		breaker.setTimeout(60000L);
	}

	@After
	public void tearDown() throws Exception {
		this.goodAction = null;
		this.badAction = null;
		this.breaker = null;
	}

	
	// ========================================================================
	// Basic tests
	// ========================================================================

	@Test
	public void testTripOpensBreakerAndSetsTimeout() {
		assertBreakerIsClosed();
		
		long now = System.currentTimeMillis();
		long expected = now + breaker.getTimeout();
		
		// The test
		breaker.trip();
		assertBreakerIsOpen();
		long delta = breaker.getRetryTime() - expected;
		assertThat(delta, is(lessThan(100L)));
	}
	
	@Test
	public void testResetClosesBreakerAndClearsExceptionCount() {
		breaker.trip();
		breaker.setExceptionCount(5);
		assertBreakerIsOpen();
		assertThat(breaker.getExceptionCount(), is(5));
		
		// The test
		breaker.reset();
		assertBreakerIsClosed();
		assertThat(breaker.getExceptionCount(), is(0));
	}

	
	// ========================================================================
	// Closed breaker tests
	// ========================================================================

	@Test
	public void testCallPassesThroughClosedBreaker() throws Throwable {
		assertBreakerIsClosed();
		int exceptionCount = breaker.getExceptionCount();
		breaker.execute(goodAction);
		assertBreakerIsClosed();
		assertThat(breaker.getExceptionCount(), is(exceptionCount));
	}

	@Test
	public void testSuccessfulCallResetsClosedBreakerExceptionCount() throws Throwable {
		breaker.setExceptionThreshold(10);
		breaker.setExceptionCount(5);
		assertThat(breaker.getExceptionCount(), is(5)); // paranoia
		assertBreakerIsClosed();
		breaker.execute(goodAction);
		assertThat(breaker.getExceptionCount(), is(0));
	}

	@Test
	public void testFailedCallThrowsExceptionAndIncrementsClosedBreakerExceptionCount() {
		assertBreakerIsClosed();
		int exceptionCount = breaker.getExceptionCount();

		try {
			breaker.execute(badAction);
			fail("Expected exception");
		} catch (Throwable t) {
			assertThat(breaker.getExceptionCount(), is(exceptionCount + 1));
		}
	}

	@Test
	public void testCallsPassThroughClosedBreakerTilThresholdReached() throws Throwable {
		final int exceptionThreshold = 3;

		breaker.setExceptionThreshold(exceptionThreshold);
		breaker.reset();

		assertThat(breaker.getExceptionThreshold(), is(exceptionThreshold));
		assertThat(breaker.getExceptionCount(), is(0));
		assertBreakerIsClosed();

		// These should all pass through.
		for (int i = 0; i < exceptionThreshold; i++) {
			try {
				breaker.execute(badAction);
			} catch (CircuitOpenException e) {
				fail("Unexpected exception: " + e);
			} catch (Throwable t) {
				if (i < exceptionThreshold - 1) {
					assertBreakerIsClosed();
				} else {
					assertBreakerIsOpen();
				}
			}
		}

		// This one should not pass through.
		try {
			breaker.execute(badAction);
			fail("Expected CircuitOpenException");
		} catch (CircuitOpenException e) {
			// Good
			assertBreakerIsOpen();
		}
	}

	@Test
	public void testExceptionCountIsThreadSafe() throws Exception {
		breaker.setExceptionThreshold(Integer.MAX_VALUE);

		// Not positive, but it seems like this test doesn't generate failures
		// til the JIT kicks in. Maybe exceptionCount++ is atomic when
		// interpreted but not when compiled to native? Anyway these numbers
		// need to be large enough to cause the JIT to kick in...
		final int numThreads = 10;
		final int callsPerThread = 1000000;

		CyclicBarrier entryBarrier = new CyclicBarrier(numThreads + 1);
		CyclicBarrier exitBarrier = new CyclicBarrier(numThreads + 1);

		Runnable runnable = new Runnable() {
			public void run() {
				for (int i = 0; i < callsPerThread; i++) {
					try {
						breaker.execute(badAction);
					} catch (Throwable t) {
						// Ignore
					}
				}
			}
		};

		for (int i = 0; i < numThreads; i++) {
			new BarrierThread(runnable, "BarrierThread-" + i, entryBarrier,
					exitBarrier).start();
		}

		assertThat(breaker.getExceptionCount(), is(0));
		entryBarrier.await(); // can throw exception
		exitBarrier.await(); // can throw exception
		assertThat(breaker.getExceptionCount(), is(numThreads * callsPerThread));
	}
	
	@Test
	public void testHandledExceptionsTripBreaker() throws Throwable {
		breaker.setExceptionCount(3);
		assertBreakerIsClosed();
		
		List<Class<? extends Exception>> handledExceptions =
			new ArrayList<Class<? extends Exception>>();
		handledExceptions.add(SQLException.class);
		breaker.setHandledExceptions(handledExceptions);
		
		for (int i = 0; i < 3; i++) {
			try {
				breaker.execute(actionThatAlwaysThrowsSqlException);
			} catch (Throwable t) {
				// Ignore
			}
		}
		
		try {
			breaker.execute(actionThatAlwaysThrowsSqlException);
			fail("Expected CircuitOpenException");
		} catch (CircuitOpenException e) {
			// Good, this is what we expected
		}
	}
	
	@Test
	public void testUnhandledExceptionsDontTripBreaker() throws Throwable {
		breaker.setExceptionCount(3);
		assertBreakerIsClosed();
		
		List<Class<? extends Exception>> handledExceptions =
			new ArrayList<Class<? extends Exception>>();
		handledExceptions.add(RuntimeException.class);
		breaker.setHandledExceptions(handledExceptions);
		
		for (int i = 0; i < 3; i++) {
			try {
				breaker.execute(actionThatAlwaysThrowsSqlException);
			} catch (SQLException e) {
				// Ignore, this is the expected SQLException
			}
		}
		
		try {
			breaker.execute(actionThatAlwaysThrowsSqlException);
			fail("Expected SQLException");
		} catch (SQLException e) {
			// Ignore, this is what we wanted
		}
	}
	

	// ========================================================================
	// Open breaker tests
	// ========================================================================

	@Test
	public void testCallingOpenBreakerThrowsCircuitOpenException() throws Throwable {
		breaker.trip();
		assertBreakerIsOpen();

		try {
			breaker.execute(goodAction);
			fail("Expected CircuitOpenException, but no exception occurred at all");
		} catch (CircuitOpenException e) {
			// Good, this is what we wanted.
		}
	}

	// ========================================================================
	// Half-open breaker tests
	// ========================================================================

	@Test
	public void testSuccessfulCallAgainstHalfOpenResetsBreaker() throws Throwable {
		breaker.setTimeout(500L);
		assertBreakerIsClosed();
		breaker.trip();
		assertBreakerIsOpen();
		Thread.sleep(550L);
		assertBreakerIsHalfOpen();
		breaker.execute(goodAction);
		assertBreakerIsClosed();
	}

	@Test
	public void testFailedCallAgainstHalfOpenTripsBreaker() throws Exception {
		breaker.setTimeout(500L);
		assertBreakerIsClosed();
		breaker.trip();
		assertBreakerIsOpen();
		Thread.sleep(550L);
		assertBreakerIsHalfOpen();

		try {
			breaker.execute(badAction);
			fail("Expected exception");
		} catch (CircuitOpenException e) {
			fail("Unexpected CircuitOpenException");
		} catch (Throwable e) {
			// Ignore; this is expected.
		}
		
		assertBreakerIsOpen();
	}
	
	@Test
	public void testHalfOpenBreakerRemainsHalfOpenWhenUnhandledExceptionOccurs() throws Throwable {
		breaker.setState(CircuitBreakerTemplate.State.HALF_OPEN);
		assertBreakerIsHalfOpen();
		
		List<Class<? extends Exception>> handledExceptions = new ArrayList<Class<? extends Exception>>();
		handledExceptions.add(RuntimeException.class);
		breaker.setHandledExceptions(handledExceptions);
		
		try {
			breaker.execute(actionThatAlwaysThrowsSqlException);
			fail("Expected SQLException");
		} catch (SQLException e) {
			// Ignore, this is what we wanted
		}
		
		assertBreakerIsHalfOpen();
	}
	
	
	// ========================================================================
	// Helper methods
	// ========================================================================

	private void assertBreakerIsClosed() {
		assertBreakerState(CircuitBreakerTemplate.State.CLOSED);
	}

	private void assertBreakerIsOpen() {
		assertBreakerState(CircuitBreakerTemplate.State.OPEN);
	}

	private void assertBreakerIsHalfOpen() {
		assertBreakerState(CircuitBreakerTemplate.State.HALF_OPEN);
	}

	private void assertBreakerState(CircuitBreakerTemplate.State state) {
		assertThat(breaker.getState(), is(state));
	}
}
