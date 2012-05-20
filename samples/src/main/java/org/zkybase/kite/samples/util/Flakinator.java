package org.zkybase.kite.samples.util;

/**
 * @author Willie Wheeler (willie.wheeler@gmail.com)
 */
public class Flakinator {
	private boolean up = true;
		
	public void simulateFlakiness() {
		if (up) {
			if (Math.random() < 0.05) {
				this.up = false;
			}
		} else {
			if (Math.random() < 0.2) {
				this.up = true;
			}
		}
		
		if (!up) {
			throw new RuntimeException("Oops, service down");
		}
	}

}
