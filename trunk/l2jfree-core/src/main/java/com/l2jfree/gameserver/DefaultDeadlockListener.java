package com.l2jfree.gameserver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DefaultDeadlockListener implements ThreadDeadlockDetector.Listener
{
	protected static final Log _log = LogFactory.getLog(DefaultDeadlockListener.class.getName());
	
	public void deadlockDetected(Thread[] threads)
	{
		_log.fatal("Deadlocked Threads:");
		_log.fatal("-------------------");
		for (Thread thread : threads)
		{
			_log.fatal(thread);
			for (StackTraceElement ste : thread.getStackTrace())
			{
				_log.fatal("t" + ste);
			}
		}
	}
}