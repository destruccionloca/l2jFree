/**
 * 
 */
package com.l2jfree.util.logging;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

import org.apache.commons.lang.ArrayUtils;

/**
 * @author NB4L1
 */
public class L2LogHandler extends StreamHandler
{
	public L2LogHandler()
	{
		setOutputStream(new OutputStream() {
			@Override
			public void write(int b) throws IOException
			{
				L2LogHandler.this.write(new String(new byte[] { (byte)b }));
			}
			
			@Override
			public void write(byte[] b) throws IOException
			{
				L2LogHandler.this.write(new String(b));
			}
			
			@Override
			public void write(byte[] b, int off, int len) throws IOException
			{
				L2LogHandler.this.write(new String(b, off, len));
			}
		});
	}
	
	@Override
	public synchronized void publish(LogRecord record)
	{
		super.publish(record);
		
		flush();
	}
	
	protected void write(String s)
	{
		for (LogListener listener : _listeners)
			listener.write(s);
	}
	
	private static LogListener[] _listeners = new LogListener[0];
	
	public static void addListener(LogListener listener)
	{
		_listeners = (LogListener[])ArrayUtils.add(_listeners, listener);
	}
	
	public interface LogListener
	{
		public void write(String s);
	}
}
