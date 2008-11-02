package com.l2jfree.gameserver.handler.voicedcommandhandlers;

import com.l2jfree.gameserver.handler.IVoicedCommandHandler;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.L2JfreeInfo;

/** 
 * @author evill33t
 * 
 */
public class VersionInfo  implements IVoicedCommandHandler
{
	private static final String[]	VOICED_COMMANDS	=
													{ "version" };

	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.startsWith("version"))
		{
			L2JfreeInfo.versionInfo(activeChar);
			return true;
		}
		return false;
	}
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
