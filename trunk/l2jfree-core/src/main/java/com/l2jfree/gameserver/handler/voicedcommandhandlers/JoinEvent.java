package com.l2jfree.gameserver.handler.voicedcommandhandlers;

import com.l2jfree.Config;
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
		"jointvt", "joinTvT", "joinTVT", "JOINTVT", "leavetvt", "leaveTvT", "leaveTVT", "LEAVETVT"
	};

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.handler.IVoicedCommandHandler#useVoicedCommand(java.lang.String, com.l2jfree.gameserver.model.actor.instance.L2PcInstance, java.lang.String)
	 */
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.equals(CMDS[0]) || command.equals(CMDS[1]) || command.equals(CMDS[2]) || command.equals(CMDS[3]))
		{
			AutomatedTvT.getInstance().registerPlayer(activeChar);
			return true;
		}
		else if (Config.AUTO_TVT_REGISTER_CANCEL)
		{
			AutomatedTvT.getInstance().cancelRegistration(activeChar);
			return true;
		}
		else
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
