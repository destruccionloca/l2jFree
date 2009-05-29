package com.l2jfree.gameserver.handler.voicedcommandhandlers;

import com.l2jfree.gameserver.handler.IVoicedCommandHandler;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.entity.events.AutomatedTvT;

/**
 * @author savormix
 *
 */
public class JoinEvent implements IVoicedCommandHandler
{
	private static final String[] CMDS = {
		"jointvt", "joinTvT", "joinTvt"
	};

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.handler.IVoicedCommandHandler#useVoicedCommand(java.lang.String, com.l2jfree.gameserver.model.actor.instance.L2PcInstance, java.lang.String)
	 */
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.equalsIgnoreCase(CMDS[0]))
		{
			AutomatedTvT.getInstance().registerPlayer(activeChar);
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.handler.IVoicedCommandHandler#getVoicedCommandList()
	 */
	@Override
	public String[] getVoicedCommandList()
	{
		return CMDS;
	}
}
