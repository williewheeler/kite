package org.zkybase.kite.samples.util;

/**
 * @author Willie Wheeler (willie.wheeler@gmail.com)
 */
public final class Flakinator {
	
	private Flakinator() { }
	
	public static void simulateFlakiness() {
		if (Math.random() < 0.4) {
			throw new RuntimeException("Oops, service down");
		}
	}

}
