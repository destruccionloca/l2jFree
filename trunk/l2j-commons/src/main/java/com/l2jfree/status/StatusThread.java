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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.util.HandlerRegistry;

/**
 * @author NB4L1
 */
public abstract class StatusThread extends Thread
{
	protected static final Log _log = LogFactory.getLog(StatusThread.class);
	
	private final StatusServer _server;
	private final Socket _socket;
	private final PrintWriter _out;
	private final BufferedReader _in;
	
	protected StatusThread(StatusServer server, Socket socket) throws IOException
	{
		_server = server;
		_socket = socket;
		_out = new PrintWriter(_socket.getOutputStream(), true);
		_in = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
		
		register(new Quit());
		register(new Help());
	}
	
	protected final StatusThread print(Object obj)
	{
		_out.print(obj);
		
		return this;
	}
	
	protected final StatusThread println(Object obj)
	{
		_out.println(obj);
		
		return this;
	}
	
	protected final StatusThread println()
	{
		_out.println();
		
		return this;
	}
	
	protected final String readLine() throws IOException
	{
		String line = _in.readLine();
		if (line == null)
			return null;
		
		StringBuilder sb = new StringBuilder(line);
		
		for (int index; (index = sb.indexOf("\b")) != -1;)
			sb.replace(index, index + 1, "");
		
		return sb.toString();
	}
	
	private final HandlerRegistry<String, StatusCommand> _handlers = new HandlerRegistry<String, StatusCommand>();
	
	protected final void register(StatusCommand handler)
	{
		_handlers.registerAll(handler, handler.getCommands());
	}
	
	protected abstract boolean login();
	
	@Override
	public final void run()
	{
		if (!login())
			return;
		
		try
		{
			_server.addStatusThread(this);
			
			for (String line; !_socket.isClosed() && (line = readLine()) != null;)
			{
				if (line.isEmpty())
					continue;
				
				final String command = line.split(" ")[0];
				
				final StatusCommand handler = _handlers.get(command);
				
				if (handler == null)
				{
					unknownCommand(command);
					continue;
				}
				
				try
				{
					handler.useCommand(command, line);
				}
				catch (RuntimeException e)
				{
					println(e);
					
					_log.warn("", e);
				}
			}
		}
		catch (IOException e)
		{
			_log.warn("", e);
		}
		finally
		{
			_server.removeStatusThread(this);
			
			println("Bye-bye!");
			close();
		}
	}
	
	protected void unknownCommand(String command)
	{
		println("No handler registered for '" + command + "'.");
	}
	
	protected final void close()
	{
		IOUtils.closeQuietly(_in);
		IOUtils.closeQuietly(_out);
		
		try
		{
			_socket.close();
		}
		catch (IOException e)
		{
		}
	}
	
	private final class Quit extends StatusCommand
	{
		@Override
		protected void useCommand(String command, String line)
		{
			close();
		}
		
		private final String[] COMMANDS = { "quit", "exit" };
		
		@Override
		protected String[] getCommands()
		{
			return COMMANDS;
		}
		
		@Override
		protected String getDescription()
		{
			return "closes telnet session";
		}
	}
	
	private final class Help extends StatusCommand
	{
		@Override
		protected void useCommand(String command, String line)
		{
			final Map<String, StatusCommand> handlers = new HashMap<String, StatusCommand>();
			
			int length = 20;
			for (StatusCommand handler : _handlers.getHandlers().values())
			{
				final String commands = StringUtils.join(handler.getCommands(), "|");
				
				handlers.put(commands, handler);
				
				length = Math.max(length, commands.length());
			}
			
			final String format = "%" + length + "s";
			
			for (Map.Entry<String, StatusCommand> entry : handlers.entrySet())
			{
				print(String.format(format, entry.getKey())).print(" - ").println(entry.getValue().getDescription());
				
				final String parameterUsage = entry.getValue().getParameterUsage();
				if (parameterUsage != null)
					print("\t").println(parameterUsage);
			}
		}
		
		private final String[] COMMANDS = { "help" };
		
		@Override
		protected String[] getCommands()
		{
			return COMMANDS;
		}
		
		@Override
		protected String getDescription()
		{
			return "shows this help";
		}
	}
}
