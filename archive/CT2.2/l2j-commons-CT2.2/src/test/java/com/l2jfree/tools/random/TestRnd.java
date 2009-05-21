package com.l2jfree.tools.random;

import junit.framework.TestCase;

public class TestRnd extends TestCase {
	public void testNextGaussian() {
		for (int i = 0; i < 50; i++) {
			double value = Rnd.nextGaussian();
			assertTrue("Value was " + value, value <= 10.0 && value >= -10.0);
		}
	}

	public void testInteger() {
		assertTrue(Rnd.nextInt(0) == 0);
		assertTrue(Rnd.nextInt(-600) <= 0);
		assertTrue(Rnd.get(-60, 50) <= 50 && Rnd.get(-60, 50) >= -60);
	}

	public void testSpeed() {
		long timeInt = 0;
		long timeDbl = 0;

		@SuppressWarnings("unused")
		int res = 0;

		for (int j = 0; j < 2; j++) {
			timeInt = System.currentTimeMillis();

			for (int i = 0; i < 1000000; i++) {
				res = Rnd.get(-100000, 100000);
			}

			timeInt = System.currentTimeMillis() - timeInt;

			timeDbl = System.currentTimeMillis();

			for (int i = 0; i < 1000000; i++) {
				res = -100000 + (int) (Rnd.nextDouble() * 100000);
			}

			timeDbl = System.currentTimeMillis() - timeDbl;
		}

		assertTrue(timeDbl > timeInt);
	}
}
