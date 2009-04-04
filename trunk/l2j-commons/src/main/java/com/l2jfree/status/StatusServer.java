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
package com.l2jfree.status;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Set;

import javolution.util.FastMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.tools.random.Rnd;
import com.l2jfree.util.L2FastSet;

/**
 * @author NB4L1
 */
public abstract class StatusServer extends Thread
{
	protected static final Log _log = LogFactory.getLog(StatusServer.class);
	
	private final ServerSocket _socket;
	private final Map<String, Long> _connectionTimes = new FastMap<String, Long>();
	private final Set<StatusThread> _threads = new L2FastSet<StatusThread>().setShared(true);
	
	protected StatusServer(int port) throws IOException
	{
		_socket = new ServerSocket(port);
		
		setPriority(Thread.MAX_PRIORITY);
		setDaemon(true);
		start();
	}
	
	@Override
	public void run()
	{
		try
		{
			while (!_socket.isClosed())
			{
				try
				{
					final Socket socket = _socket.accept();
					final String host = socket.getInetAddress().getHostAddress();
					
					final Long lastConnectionTime = _connectionTimes.put(host, System.currentTimeMillis());
					
					if (lastConnectionTime != null && System.currentTimeMillis() - 1000 < lastConnectionTime)
						continue;
					
					newStatusThread(socket).start();
				}
				catch (IOException e)
				{
					_log.warn("", e);
				}
			}
		}
		finally
		{
			close();
		}
	}
	
	protected final void close()
	{
		try
		{
			_socket.close();
		}
		catch (IOException e)
		{
		}
	}
	
	protected abstract StatusThread newStatusThread(Socket socket) throws IOException;
	
	public final void addStatusThread(StatusThread thread)
	{
		_threads.add(thread);
	}
	
	public final void removeStatusThread(StatusThread thread)
	{
		_threads.remove(thread);
	}
	
	public final Set<StatusThread> getStatusThreads()
	{
		return _threads;
	}
	
	public final void broadcast(String message)
	{
		for (StatusThread thread : getStatusThreads())
			thread.println(message);
	}
	
	protected final String generateRandomPassword(int length)
	{
		final String chars = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM0123456789";
		
		final StringBuilder sb = new StringBuilder(length);
		
		for (int i = 0; i < length; i++)
			sb.append(chars.charAt(Rnd.get(chars.length())));
		
		return sb.toString();
	}
}
