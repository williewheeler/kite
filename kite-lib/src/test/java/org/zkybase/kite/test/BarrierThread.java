/*
 * Copyright (c) 2010 the original author or authors.
 */
package org.zkybase.kite.test;

import java.util.concurrent.CyclicBarrier;

/**
 * From the book Test Driven (Manning).
 * 
 * @author Lasse Koskela
 * @since 1.0
 */
public class BarrierThread extends Thread {
	private CyclicBarrier entryBarrier;
	private CyclicBarrier exitBarrier;

	public BarrierThread(Runnable runnable, String name, CyclicBarrier entryBarrier, CyclicBarrier exitBarrier) {
		super(runnable, name);
		this.entryBarrier = entryBarrier;
		this.exitBarrier = exitBarrier;
	}
	
	@Override
	public void run() {
		try {
			entryBarrier.await();
			super.run();
			exitBarrier.await();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
