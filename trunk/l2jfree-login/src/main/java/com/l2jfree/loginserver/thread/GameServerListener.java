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
package com.l2jfree.loginserver.thread;

import java.net.Socket;
import java.util.List;

import javolution.util.FastList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;

/**
 *
 * @author KenM
 */
public class GameServerListener extends FloodProtectedListener
{
	private static Log _log								= LogFactory.getLog(GameServerListener.class);
	private static List<GameServerThread> _gameServers	= new FastList<GameServerThread>();

	public GameServerListener()
	{
		super(Config.LOGIN_HOSTNAME, Config.LOGIN_PORT);
	}

	/**
	 * @see com.l2jfree.loginserver.FloodProtectedListener#addClient(java.net.Socket)
	 */
	@Override
	public void addClient(Socket s)
	{
		if (_log.isDebugEnabled())
		{
			_log.info("Received gameserver connection from: " + s.getInetAddress().getHostAddress());
		}
		GameServerThread gst = new GameServerThread(s);
		_gameServers.add(gst);
	}

	public void removeGameServer(GameServerThread gst)
	{
		_gameServers.remove(gst);
	}

	public void playerSelectedServer(int id, String ip)
	{
		for (GameServerThread gst : _gameServers)
		{
			if (gst.getServerId() == id)
			{
				gst.playerSelectedServer(ip);
				break;
			}
		}
	}
}
