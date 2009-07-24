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

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmocore.network.SelectorConfig;
import org.mmocore.network.SelectorThread;

import com.l2jfree.Config;
import com.l2jfree.L2Registry;
import com.l2jfree.loginserver.manager.BanManager;
import com.l2jfree.loginserver.manager.GameServerManager;
import com.l2jfree.loginserver.manager.LoginManager;
import com.l2jfree.loginserver.thread.GameServerListener;
import com.l2jfree.status.Status;

/**
 * Main class for loginserver
 * 
 */
public class L2LoginServer extends Config
{
	/** Version sent if {@link Config#PROTOCOL_LEGACY} is false */
	public static final int PROTOCOL_L2J = 258;
	/** Version sent if {@link Config#PROTOCOL_LEGACY} is true */
	public static final int PROTOCOL_LEGACY = 259;
	/** Current network protocol version */
	public static final int PROTOCOL_CURRENT = 1;

	private static L2LoginServer			_instance;
	private static Log						_log			= LogFactory.getLog(L2LoginServer.class);
	/**the gameserver listener store all gameserver connected to the client*/
	private GameServerListener				_gameServerListener;
	private SelectorThread<L2LoginClient>	_selectorThread;

	/**
	 * @return the instance of L2LoginServer
	 */
	public static L2LoginServer getInstance()
	{
		return _instance;
	}

	/**
	 * Instantiate loginserver and launch it
	 * Initialize log folder, telnet console and registry
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException
	{
		_instance = new L2LoginServer();
	}

	public L2LoginServer() throws IOException
	{
		// Create log folder
		// ------------------
		new File("log/login").mkdirs();
		
		// Initialize config
		// ------------------
		Config.load();

		// Initialize Application context (registry of beans)
		// ---------------------------------------------------
		L2Registry.loadRegistry(new String[]
		{ "spring.xml" });

		// o Initialize LoginManager
		// -------------------------
		LoginManager.load();

		// o Initialize GameServer Manager
		// ------------------------------
		GameServerManager.getInstance();

		// o Initialize ban list
		// ----------------------
		BanManager.getInstance();

		// o Initialize SelectorThread
		// ----------------------------
		initNetworkLayer();

		// o Initialize GS listener
		// ----------------------------
		initGSListener();

		// o Start status telnet server
		// --------------------------
		initTelnetServer();

		System.gc();

		// o Start the server
		// ------------------
		startServer();
		_log.info("Login Server ready on " + Config.LOGIN_SERVER_HOSTNAME + ":" + Config.LOGIN_SERVER_PORT);
	}

	private void startServer()
	{
		try
		{
			_selectorThread.openServerSocket(InetAddress.getByName(Config.LOGIN_SERVER_HOSTNAME), Config.LOGIN_SERVER_PORT);
		}
		catch (IOException e)
		{
			_log.fatal("FATAL: Failed to open server socket. Reason: " + e.getMessage(), e);
			System.exit(1);
		}
		_selectorThread.start();
	}

	private void initTelnetServer() throws IOException
	{
		if (Config.IS_TELNET_ENABLED)
			Status.initInstance();
		
		else
			_log.info("Telnet server is currently disabled.");
	}

	private void initGSListener()
	{
		_gameServerListener = new GameServerListener();
		_gameServerListener.start();
		_log.info("Listening for GameServers on " + Config.LOGIN_HOSTNAME + ":" + Config.LOGIN_PORT);
	}

	private void initNetworkLayer()
	{
		L2LoginPacketHandler loginPacketHandler = new L2LoginPacketHandler();
		SelectorHelper sh = new SelectorHelper();
		SelectorConfig<L2LoginClient> ssc = new SelectorConfig<L2LoginClient>(null, null, sh, loginPacketHandler);
		try
		{
			_selectorThread = new SelectorThread<L2LoginClient>(ssc, sh, sh, sh);
		}
		catch (IOException e)
		{
			_log.fatal("FATAL: Failed to open Selector. Reason: " + e.getMessage(), e);
			System.exit(1);
		}
	}

	public GameServerListener getGameServerListener()
	{
		return _gameServerListener;
	}
}
