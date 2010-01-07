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
package com.l2jfree.loginserver;

import java.net.InetAddress;

import org.mmocore.network.SelectorConfig;
import org.mmocore.network.SelectorThread;

import com.l2jfree.Config;
import com.l2jfree.L2Registry;
import com.l2jfree.loginserver.clientpackets.L2LoginClientPacket;
import com.l2jfree.loginserver.manager.BanManager;
import com.l2jfree.loginserver.manager.GameServerManager;
import com.l2jfree.loginserver.manager.LoginManager;
import com.l2jfree.loginserver.serverpackets.L2LoginServerPacket;
import com.l2jfree.loginserver.thread.GameServerListener;
import com.l2jfree.status.Status;

public final class L2LoginServer extends Config
{
	/** Version sent if {@link Config#PROTOCOL_LEGACY} is false */
	public static final int PROTOCOL_L2J = 258;
	/** Version sent if {@link Config#PROTOCOL_LEGACY} is true */
	public static final int PROTOCOL_LEGACY = 259;
	/** Current network protocol version */
	// protocol 1 does not support connection filtering
	public static final int PROTOCOL_CURRENT = 2;
	
	public static void main(String[] args) throws Throwable
	{
		// Initialize config
		// ------------------
		Config.load();
		
		// Initialize Application context (registry of beans)
		// ---------------------------------------------------
		L2Registry.loadRegistry(new String[] { "spring.xml" });
		
		// o Initialize LoginManager
		// -------------------------
		LoginManager.getInstance();
		
		// o Initialize GameServer Manager
		// ------------------------------
		GameServerManager.getInstance();
		
		// o Initialize ban list
		// ----------------------
		BanManager.getInstance();
		
		// o Initialize SelectorThread
		// ----------------------------
		final SelectorThread<L2LoginClient, L2LoginClientPacket, L2LoginServerPacket> selectorThread;
		final SelectorConfig<L2LoginClient, L2LoginClientPacket, L2LoginServerPacket> ssc;
		
		final L2LoginPacketHandler loginPacketHandler = new L2LoginPacketHandler();
		final SelectorHelper sh = new SelectorHelper();
		ssc = new SelectorConfig<L2LoginClient, L2LoginClientPacket, L2LoginServerPacket>();
		ssc.setAcceptFilter(sh);
		ssc.setClientFactory(sh);
		ssc.setExecutor(sh);
		ssc.setPacketHandler(loginPacketHandler);
		
		selectorThread = new SelectorThread<L2LoginClient, L2LoginClientPacket, L2LoginServerPacket>(ssc);
		
		// o Initialize GS listener
		// ----------------------------
		GameServerListener.getInstance();
		
		_log.info("Listening for GameServers on " + Config.LOGIN_HOSTNAME + ":" + Config.LOGIN_PORT);
		
		// o Start status telnet server
		// --------------------------
		if (Config.IS_TELNET_ENABLED)
			Status.initInstance();
		else
			_log.info("Telnet server is currently disabled.");
		
		System.gc();
		
		// o Start the server
		// ------------------
		
		selectorThread.openServerSocket(InetAddress.getByName(Config.LOGIN_SERVER_HOSTNAME), Config.LOGIN_SERVER_PORT);
		selectorThread.start();
		
		_log.info("Login Server ready on " + Config.LOGIN_SERVER_HOSTNAME + ":" + Config.LOGIN_SERVER_PORT);
	}
}
