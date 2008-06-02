/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jfree.tools.random;

public class Rnd {
	/**
	 * Get random number from 0.0 to 1.0
	 */
	public static final double nextDouble() {
		return RandomIntGenerator.getInstance().getSecureRandom().nextDouble();
	}

	/**
	 * Get random number from 0 to n-1
	 */
	public static final int nextInt(int n) {
		if (n < 0)
			return RandomIntGenerator.getInstance().getSecureRandom().nextInt(
					Math.abs(n))
					* (-1);
		else if (n == 0)
			return n;

		return RandomIntGenerator.getInstance().getSecureRandom().nextInt(n);
	}

	/**
	 * Get random number from 0 to n-1
	 */
	public static final int get(int n) {
		return nextInt(n);
	}

	/**
	 * Get random number from min to max <b>(not max-1)</b>
	 */
	public static final int get(int min, int max) {
		return min + nextInt(max - min + 1);
	}

	public static final double nextGaussian() {
		return RandomIntGenerator.getInstance().getSecureRandom()
				.nextGaussian();
	}

	public static final boolean nextBoolean() {
		return RandomIntGenerator.getInstance().getSecureRandom().nextBoolean();
	}

	public static final void nextBytes(byte[] array) {
		RandomIntGenerator.getInstance().getSecureRandom().nextBytes(array);
	}
}
