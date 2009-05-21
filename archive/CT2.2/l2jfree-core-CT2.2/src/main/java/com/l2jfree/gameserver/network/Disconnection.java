package com.l2jfree.gameserver.network;

import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.taskmanager.AttackStanceTaskManager;

/**
 * @author NB4L1
 */
public final class Disconnection
{
	public static L2GameClient getClient(L2GameClient client, L2PcInstance activeChar)
	{
		if (client != null)
			return client;
		
		if (activeChar != null)
			return activeChar.getClient();
		
		return null;
	}
	
	public static L2PcInstance getActiveChar(L2GameClient client, L2PcInstance activeChar)
	{
		if (activeChar != null)
			return activeChar;
		
		if (client != null)
			return client.getActiveChar();
		
		return null;
	}
	
	private final L2GameClient _client;
	private final L2PcInstance _activeChar;
	
	public Disconnection(L2GameClient client)
	{
		this(client, null);
	}
	
	public Disconnection(L2PcInstance activeChar)
	{
		this(null, activeChar);
	}
	
	public Disconnection(L2GameClient client, L2PcInstance activeChar)
	{
		_client = getClient(client, activeChar);
		_activeChar = getActiveChar(client, activeChar);
		
		if (_client != null)
			_client.setActiveChar(null);
		
		if (_activeChar != null)
			_activeChar.setClient(null);
	}
	
	public Disconnection store()
	{
		try
		{
			if (_activeChar != null)
				_activeChar.store(true);
		}
		catch (RuntimeException e)
		{
			e.printStackTrace();
		}
		
		return this;
	}
	
	public Disconnection deleteMe()
	{
		try
		{
			if (_activeChar != null)
				_activeChar.deleteMe();
		}
		catch (RuntimeException e)
		{
			e.printStackTrace();
		}
		
		return this;
	}
	
	public Disconnection close(boolean toLoginScreen)
	{
		if (_client != null)
			_client.close(toLoginScreen);
		
		return this;
	}
	
	public void defaultSequence(boolean toLoginScreen)
	{
		store();
		deleteMe();
		close(toLoginScreen);
	}
	
	public void onDisconnection()
	{
		if (_activeChar != null)
		{
			ThreadPoolManager.getInstance().schedule(new Runnable() {
				@Override
				public void run()
				{
					store();
					deleteMe();
				}
			}, _activeChar.canLogout() ? 0 : AttackStanceTaskManager.COMBAT_TIME);
		}
	}
}
