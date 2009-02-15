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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmocore.network.IAcceptFilter;

import com.l2jfree.gameserver.GameServer;
import com.l2jfree.gameserver.network.clientpackets.L2GameClientPacket;

/**
 * @author NB4L1
 */
public final class IOFloodManager implements IAcceptFilter
{
	private static final Log _log = LogFactory.getLog(IOFloodManager.class);
	
	public static enum FloodLogMode
	{
		BUFFER_UNDER_FLOW,
		FAILED_READING,
		FAILED_RUNNING;
		// TODO
	}
	
	private static IOFloodManager _instance;
	
	public static IOFloodManager getInstance()
	{
		if (_instance == null)
			_instance = new IOFloodManager();
		
		return _instance;
	}
	
	private IOFloodManager()
	{
		_log.info("IOFloodManager: initialized.");
	}
	
	public void log(FloodLogMode mode, L2GameClient client, L2GameClientPacket packet, Throwable throwable)
	{
		final String log = mode + ": " + client + " - " + packet.getType() + " - " + GameServer.getVersionNumber();
		
		if (throwable != null)
			_log.fatal(log, throwable);
		else
			_log.fatal(log);
		
		// TODO
	}
	
	@Override
	public boolean accept(SocketChannel socketChannel)
	{
		// TODO
		return true;
	}
}
