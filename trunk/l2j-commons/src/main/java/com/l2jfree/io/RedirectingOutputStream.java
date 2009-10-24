package com.l2jfree.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author NB4L1
 */
public abstract class RedirectingOutputStream extends OutputStream
{
	@Override
	public final void write(int b) throws IOException
	{
		handleString(new String(new byte[] { (byte)b }));
	}
	
	@Override
	public final void write(byte[] b) throws IOException
	{
		handleString(new String(b));
	}
	
	@Override
	public final void write(byte[] b, int off, int len) throws IOException
	{
		handleString(new String(b, off, len));
	}
	
	protected abstract void handleString(String s);
	
	public static abstract class BufferedRedirectingOutputStream extends RedirectingOutputStream
	{
		private final StringBuilder _buffer = new StringBuilder();
		
		@Override
		protected synchronized final void handleString(String s)
		{
			for (int i = 0, length = s.length(); i < length; i++)
			{
				final char c = s.charAt(i);
				
				switch (c)
				{
					case '\r':
					case '\n':
					{
						if (_buffer.length() == 0)
							break;
						
						handleLine(_buffer.toString());
						
						_buffer.setLength(0);
						break;
					}
					default:
					{
						_buffer.append(c);
						break;
					}
				}
			}
		}
		
		protected abstract void handleLine(String line);
	}
}
