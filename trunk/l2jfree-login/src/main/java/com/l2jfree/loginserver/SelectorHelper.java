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

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.mmocore.network.HeaderInfo;
import org.mmocore.network.IAcceptFilter;
import org.mmocore.network.IClientFactory;
import org.mmocore.network.IMMOExecutor;
import org.mmocore.network.ISocket;
import org.mmocore.network.ReceivablePacket;
import org.mmocore.network.SelectorThread;
import org.mmocore.network.TCPHeaderHandler;

import com.l2jfree.loginserver.serverpackets.Init;
import com.l2jfree.util.concurrent.ExecuteWrapper;

/**
 * @author KenM
 */
public class SelectorHelper extends TCPHeaderHandler<L2LoginClient> implements IMMOExecutor<L2LoginClient>,
	IClientFactory<L2LoginClient>, IAcceptFilter
{
	private final ThreadPoolExecutor _generalPacketsThreadPool =
		new ThreadPoolExecutor(1, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
	
	public SelectorHelper()
	{
		super(null);
	}
	
	/**
	 * @see com.l2jserver.mmocore.network.IMMOExecutor#execute(com.l2jserver.mmocore.network.ReceivablePacket)
	 */
	public void execute(ReceivablePacket<L2LoginClient> packet)
	{
		_generalPacketsThreadPool.execute(new ExecuteWrapper(packet));
	}
	
	/**
	 * @see com.l2jserver.mmocore.network.IClientFactory#create(com.l2jserver.mmocore.network.MMOConnection)
	 */
	public L2LoginClient create(SelectorThread<L2LoginClient> selectorThread, ISocket socket, SelectionKey key)
	{
		L2LoginClient client = new L2LoginClient(selectorThread, socket, key);
		client.sendPacket(new Init(client));
		return client;
	}
	
	/**
	 * @see com.l2jserver.mmocore.network.IAcceptFilter#accept(java.nio.channels.SocketChannel)
	 */
	public boolean accept(SocketChannel sc)
	{
		return true;
		//return !BanManager.getInstance().isBannedAddress(sc.socket().getInetAddress());
	}
	
	/**
	 * @see org.mmocore.network.TCPHeaderHandler#handleHeader(java.nio.channels.SelectionKey, java.nio.ByteBuffer)
	 */
	@Override
	public HeaderInfo<L2LoginClient> handleHeader(SelectionKey key, ByteBuffer buf)
	{
		if (buf.remaining() >= 2)
		{
			int dataPending = (buf.getShort() & 0xffff) - 2;
			
			return getHeaderInfoReturn().set(0, dataPending, false, (L2LoginClient)key.attachment());
		}
		else
		{
			return getHeaderInfoReturn().set(2 - buf.remaining(), 0, false, (L2LoginClient)key.attachment());
		}
	}
}
