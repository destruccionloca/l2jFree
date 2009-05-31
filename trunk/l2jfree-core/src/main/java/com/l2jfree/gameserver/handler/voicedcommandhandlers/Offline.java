package com.l2jfree.gameserver.handler.voicedcommandhandlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.gameserver.handler.IVoicedCommandHandler;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.zone.L2Zone;

public class Offline implements IVoicedCommandHandler
{
	protected static Log			_log			= LogFactory.getLog(Offline.class);
	private static final String[]	VOICED_COMMANDS	= { "offline" };

	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if ((activeChar.getPrivateStoreType() == 1 || activeChar.getPrivateStoreType() == 3 || (activeChar.getPrivateStoreType() == 5 && Config.ALLOW_OFFLINE_TRADE_CRAFT)))
		{
			if (activeChar.isInsideZone(L2Zone.FLAG_PEACE) || activeChar.isGM())
			{
				return activeChar.enterOfflineMode();
			}
			else
			{
				activeChar.sendMessage("You must be in a peace zone to use offline mode");
				return false;
			}
		}
		else
		{
			activeChar.sendMessage("You must be in a peace zone to use offline mode");
			return false;
		}
	}

	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
