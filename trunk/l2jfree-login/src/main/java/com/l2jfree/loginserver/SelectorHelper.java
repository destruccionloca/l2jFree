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

import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.mmocore.network.IAcceptFilter;
import org.mmocore.network.IClientFactory;
import org.mmocore.network.IMMOExecutor;
import org.mmocore.network.SelectorThread;

import com.l2jfree.loginserver.clientpackets.L2LoginClientPacket;
import com.l2jfree.loginserver.manager.BanManager;
import com.l2jfree.loginserver.manager.LoginManager;
import com.l2jfree.loginserver.serverpackets.Init;
import com.l2jfree.loginserver.serverpackets.L2LoginServerPacket;
import com.l2jfree.util.concurrent.ExecuteWrapper;

/**
 * @author KenM
 */
public final class SelectorHelper implements IMMOExecutor<L2LoginClient, L2LoginClientPacket, L2LoginServerPacket>,
	IClientFactory<L2LoginClient, L2LoginClientPacket, L2LoginServerPacket>, IAcceptFilter
{
	private final ThreadPoolExecutor _generalPacketsThreadPool =
		new ThreadPoolExecutor(1, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
	
	public SelectorHelper()
	{
	}
	
	public void execute(L2LoginClientPacket packet)
	{
		_generalPacketsThreadPool.execute(new ExecuteWrapper(packet));
	}
	
	public L2LoginClient create(SelectorThread<L2LoginClient, L2LoginClientPacket, L2LoginServerPacket> selectorThread,
		Socket socket, SelectionKey key)
	{
		L2LoginClient client = new L2LoginClient(selectorThread, socket, key);
		client.sendPacket(new Init(client));
		LoginManager.getInstance().addConnection(client);
		return client;
	}
	
	public boolean accept(SocketChannel sc)
	{
		// Ignore permabanned IPs
		return !BanManager.getInstance().isRestrictedAddress(sc.socket().getInetAddress());
	}
}
