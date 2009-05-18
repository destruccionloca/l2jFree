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
package com.l2jfree.lang;

import java.util.concurrent.TimeUnit;

/**
 * @author NB4L1
 */
public final class L2System
{
	
	private L2System()
	{
	}
	
	private interface MilliTime
	{
		long milliTime();
	}
	
	private static final MilliTime systemCurrentTimeMillisBased = new MilliTime() {
		private final long ZERO = System.currentTimeMillis();
		
		@Override
		public long milliTime()
		{
			return System.currentTimeMillis() - ZERO;
		}
	};
	
	private static final MilliTime systemNanoTimeBased = new MilliTime() {
		private final long ZERO = System.nanoTime();
		
		@Override
		public long milliTime()
		{
			return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - ZERO);
		}
	};
	
	/**
	 * Ugly as hell, but unfortunately nanoTime is influenced by the cpu clock speed, so if it's changed, it shouldn't
	 * be used at all.
	 */
	static
	{
		new Thread() {
			@Override
			public void run()
			{
				long begin1 = systemCurrentTimeMillisBased.milliTime();
				long begin2 = systemNanoTimeBased.milliTime();
				
				try
				{
					Thread.sleep(10000);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				
				double diff1 = systemCurrentTimeMillisBased.milliTime() - begin1;
				double diff2 = systemNanoTimeBased.milliTime() - begin2;
				
				if (Math.abs((diff1 / diff2) - 1.0) < 0.005)
					milliTime = systemNanoTimeBased;
			}
		}.start();
	}
	
	private static MilliTime milliTime = systemCurrentTimeMillisBased;
	
	public static long milliTime()
	{
		return milliTime.milliTime();
	}
	
	/**
	 * Copy of HashMap.hash().
	 */
	public static int hash(int h)
	{
		h ^= (h >>> 20) ^ (h >>> 12);
		return h ^ (h >>> 7) ^ (h >>> 4);
	}
}
