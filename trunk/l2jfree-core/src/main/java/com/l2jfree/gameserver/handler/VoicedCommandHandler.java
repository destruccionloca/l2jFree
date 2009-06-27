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
package com.l2jfree.gameserver.handler;

import com.l2jfree.Config;
import com.l2jfree.gameserver.handler.voicedcommandhandlers.Auction;
import com.l2jfree.gameserver.handler.voicedcommandhandlers.Banking;
import com.l2jfree.gameserver.handler.voicedcommandhandlers.CastleDoors;
import com.l2jfree.gameserver.handler.voicedcommandhandlers.Hellbound;
import com.l2jfree.gameserver.handler.voicedcommandhandlers.JoinEvent;
import com.l2jfree.gameserver.handler.voicedcommandhandlers.Mail;
import com.l2jfree.gameserver.handler.voicedcommandhandlers.Offline;
import com.l2jfree.gameserver.handler.voicedcommandhandlers.VersionInfo;
import com.l2jfree.gameserver.handler.voicedcommandhandlers.Wedding;
import com.l2jfree.util.HandlerRegistry;

public final class VoicedCommandHandler extends HandlerRegistry<String, IVoicedCommandHandler>
{
	private static VoicedCommandHandler	_instance;

	public static VoicedCommandHandler getInstance()
	{
		if (_instance == null)
			_instance = new VoicedCommandHandler();

		return _instance;
	}

	/**
	 * Reloads the voiced command list. Does nothing if it hasn't been loaded
	 * yet
	 */
	public static void reload()
	{
		if (_instance != null)
			_instance = new VoicedCommandHandler();
	}

	private VoicedCommandHandler()
	{
		if (Config.BANKING_SYSTEM_ENABLED)
			registerVoicedCommandHandler(new Banking());
		if (Config.ALLOW_OFFLINE_TRADE)
			registerVoicedCommandHandler(new Offline());
		if (Config.ALLOW_WEDDING)
			registerVoicedCommandHandler(new Wedding());
		registerVoicedCommandHandler(new CastleDoors());
		registerVoicedCommandHandler(new Hellbound());
		if (Config.AUTO_TVT_ENABLED)
			registerVoicedCommandHandler(new JoinEvent());
		registerVoicedCommandHandler(new VersionInfo());
		registerVoicedCommandHandler(new Mail());
		registerVoicedCommandHandler(new Auction());
		_log.info("VoicedCommandHandler: Loaded " + size() + " handlers.");
	}

	private void registerVoicedCommandHandler(IVoicedCommandHandler handler)
	{
		registerAll(handler, handler.getVoicedCommandList());
	}

	public IVoicedCommandHandler getVoicedCommandHandler(String voicedCommand)
	{
		if (voicedCommand.indexOf(" ") != -1)
			voicedCommand = voicedCommand.substring(0, voicedCommand.indexOf(" "));

		return get(voicedCommand);
	}
}
