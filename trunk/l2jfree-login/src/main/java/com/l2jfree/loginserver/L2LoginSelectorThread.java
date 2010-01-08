package com.l2jfree.loginserver;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.l2jfree.loginserver.clientpackets.L2LoginClientPacket;
import com.l2jfree.loginserver.manager.BanManager;
import com.l2jfree.loginserver.manager.LoginManager;
import com.l2jfree.loginserver.serverpackets.Init;
import com.l2jfree.loginserver.serverpackets.L2LoginServerPacket;
import com.l2jfree.mmocore.network.IPacketHandler;
import com.l2jfree.mmocore.network.SelectorConfig;
import com.l2jfree.mmocore.network.SelectorThread;
import com.l2jfree.util.concurrent.ExecuteWrapper;

public final class L2LoginSelectorThread extends
	SelectorThread<L2LoginClient, L2LoginClientPacket, L2LoginServerPacket>
{
	private static final class SingletonHolder
	{
		private static final L2LoginSelectorThread INSTANCE;
		
		static
		{
			final SelectorConfig sc = new SelectorConfig();
			
			try
			{
				INSTANCE = new L2LoginSelectorThread(sc, new L2LoginPacketHandler());
			}
			catch (Exception e)
			{
				throw new Error(e);
			}
		}
	}
	
	public static L2LoginSelectorThread getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private L2LoginSelectorThread(SelectorConfig sc, IPacketHandler<L2LoginClient, L2LoginClientPacket, L2LoginServerPacket> packetHandler)
		throws IOException
	{
		super(sc, packetHandler);
	}
	
	// ==============================================
	
	@Override
	protected L2LoginClient createClient(Socket socket, SelectionKey key)
	{
		L2LoginClient client = new L2LoginClient(this, socket, key);
		client.sendPacket(new Init(client));
		LoginManager.getInstance().addConnection(client);
		return client;
	}
	
	private final ThreadPoolExecutor _generalPacketsThreadPool = new ThreadPoolExecutor(1, Integer.MAX_VALUE, 60L,
		TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
	
	@Override
	protected void executePacket(L2LoginClientPacket packet)
	{
		_generalPacketsThreadPool.execute(new ExecuteWrapper(packet));
	}
	
	// ==============================================
	
	@Override
	public boolean acceptConnectionFrom(SocketChannel sc)
	{
		if (!super.acceptConnectionFrom(sc))
			return false;
		
		// Ignore permabanned IPs
		if (BanManager.getInstance().isRestrictedAddress(sc.socket().getInetAddress()))
			return false;
		
		return true;
	}
}
