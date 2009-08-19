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
package com.l2jfree.gameserver.network;

import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmocore.network.IAcceptFilter;

import com.l2jfree.gameserver.CoreInfo;
import com.l2jfree.gameserver.network.clientpackets.L2GameClientPacket;
import com.l2jfree.lang.L2System;

/**
 * @author NB4L1
 */
public final class IOFloodManager implements IAcceptFilter
{
	private static final Log _log = LogFactory.getLog(IOFloodManager.class);
	
	public static enum ErrorMode
	{
		INVALID_OPCODE,
		BUFFER_UNDER_FLOW,
		FAILED_READING,
		FAILED_RUNNING;
	}
	
	private static enum Result
	{
		ACCEPTED,
		WARNED,
		REJECTED;
		
		public static Result max(Result r1, Result r2)
		{
			if (r1.ordinal() > r2.ordinal())
				return r1;
			
			return r2;
		}
	}
	
	private static final class SingletonHolder
	{
		private static final IOFloodManager INSTANCE = new IOFloodManager();
	}
	
	public static IOFloodManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static final FloodManager _packets;
	private static final FloodManager _errors;
	
	static
	{
		// TODO: fine tune
		_packets = new FloodManager(1000, 10);
		_packets.addFloodFilter(100, 120, 1);
		
		_errors = new FloodManager(100, 10);
		_errors.addFloodFilter(10, 10, 1);
		
		_log.info("IOFloodManager: initialized.");
	}
	
	public static void report(ErrorMode mode, L2GameClient client, L2GameClientPacket packet, Throwable throwable)
	{
		final Result isFlooding = _errors.isFlooding(client.getAccountName(), true);
		
		final StringBuilder sb = new StringBuilder();
		if (isFlooding != Result.ACCEPTED)
		{
			sb.append("Flooding with ");
		}
		sb.append(mode);
		sb.append(": ");
		sb.append(client);
		if (packet != null)
		{
			sb.append(" - ");
			sb.append(packet.getType());
		}
		sb.append(" - ");
		sb.append(CoreInfo.getVersionInfo());
		
		if (throwable != null)
			_log.info(sb, throwable);
		else
			_log.info(sb);
		
		//if (isFlooding != Result.ACCEPTED)
		//{
		//	// TODO: punish, warn, log, etc
		//}
	}
	
	public static boolean canReceivePacketFrom(L2GameClient client, int opcode)
	{
		// thats spammed by the client even without external tools
		switch (opcode)
		{
			case 0x59: // ValidatePosition
			case 0x0f: // MoveBackwardToLocation
				return true;
		}
		
		final String key = client.getAccountName();
		
		switch (Result.max(_packets.isFlooding(key, true), _errors.isFlooding(key, false)))
		{
			case REJECTED:
			{
				// TODO: punish, warn, log, etc
				_log.warn("Rejected packet (0x" + Integer.toHexString(opcode) + ") from " + client);
				return false;
			}
			case WARNED:
			{
				// TODO: punish, warn, log, etc
				_log.warn("Packet over warn limit (0x" + Integer.toHexString(opcode) + ") from " + client);
				return true;
			}
			default:
				return true;
		}
	}
	
	@Override
	public boolean accept(SocketChannel socketChannel)
	{
		// TODO: we don't know the account yet, so it must be mapped by host address,
		// BUT a lot of players could have the same, so it's complicated
		return true;
	}
	
	private static final class FloodManager
	{
		private final Map<String, LogEntry> _entries = new HashMap<String, LogEntry>();
		
		private final ReentrantLock _lock = new ReentrantLock();
		
		private final int _tickLength;
		private final int _tickAmount;
		
		private FloodFilter[] _filters = new FloodFilter[0];
		
		private FloodManager(int msecPerTick, int tickAmount)
		{
			_tickLength = msecPerTick;
			_tickAmount = tickAmount;
		}
		
		private void addFloodFilter(int warnLimit, int rejectLimit, int tickLimit)
		{
			_filters = Arrays.copyOf(_filters, _filters.length + 1);
			_filters[_filters.length - 1] = new FloodFilter(warnLimit, rejectLimit, tickLimit);
		}
		
		private static final class FloodFilter
		{
			private final int _warnLimit;
			private final int _rejectLimit;
			private final int _tickLimit;
			
			private FloodFilter(int warnLimit, int rejectLimit, int tickLimit)
			{
				_warnLimit = warnLimit;
				_rejectLimit = rejectLimit;
				_tickLimit = tickLimit;
			}
			
			public int getWarnLimit()
			{
				return _warnLimit;
			}
			
			public int getRejectLimit()
			{
				return _rejectLimit;
			}
			
			public int getTickLimit()
			{
				return _tickLimit;
			}
		}
		
		public Result isFlooding(String key, boolean increment)
		{
			if (key == null || key.isEmpty())
				return Result.ACCEPTED;
			
			_lock.lock();
			try
			{
				LogEntry entry = _entries.get(key);
				
				if (entry == null)
				{
					entry = new LogEntry();
					
					_entries.put(key, entry);
				}
				
				return entry.isFlooding(increment);
			}
			finally
			{
				_lock.unlock();
			}
		}
		
		private final class LogEntry
		{
			private final short[] _ticks = new short[_tickAmount];
			
			private int _lastTick = getCurrentTick();
			
			public int getCurrentTick()
			{
				return (int)(L2System.milliTime() / _tickLength);
			}
			
			public Result isFlooding(boolean increment)
			{
				final int currentTick = getCurrentTick();
				
				if (currentTick - _lastTick >= _ticks.length)
				{
					_lastTick = currentTick;
					
					Arrays.fill(_ticks, (short)0);
				}
				else
				{
					while (currentTick != _lastTick)
					{
						_lastTick++;
						
						_ticks[_lastTick % _ticks.length] = 0;
					}
				}
				
				if (increment)
					_ticks[_lastTick % _ticks.length]++;
				
				for (FloodFilter filter : _filters)
				{
					int previousSum = 0;
					int currentSum = 0;
					
					for (int i = 0; i <= filter.getTickLimit(); i++)
					{
						int value = _ticks[(_lastTick - i) % _ticks.length];
						
						if (i != 0)
							previousSum += value;
						
						if (i != filter.getTickLimit())
							currentSum += value;
					}
					
					if (previousSum > filter.getRejectLimit() || currentSum > filter.getRejectLimit())
						return Result.REJECTED;
					
					if (previousSum > filter.getWarnLimit() || currentSum > filter.getWarnLimit())
						return Result.WARNED;
				}
				
				return Result.ACCEPTED;
			}
		}
	}
}
