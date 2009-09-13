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

import java.util.StringTokenizer;

import com.l2jfree.gameserver.handler.voicedcommandhandlers.Auction;
import com.l2jfree.gameserver.handler.voicedcommandhandlers.Banking;
import com.l2jfree.gameserver.handler.voicedcommandhandlers.CastleDoors;
import com.l2jfree.gameserver.handler.voicedcommandhandlers.Hellbound;
import com.l2jfree.gameserver.handler.voicedcommandhandlers.Mail;
import com.l2jfree.gameserver.handler.voicedcommandhandlers.Offline;
import com.l2jfree.gameserver.handler.voicedcommandhandlers.VersionInfo;
import com.l2jfree.gameserver.handler.voicedcommandhandlers.Wedding;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.restriction.global.GlobalRestrictions;
import com.l2jfree.util.HandlerRegistry;

public final class VoicedCommandHandler extends HandlerRegistry<String, IVoicedCommandHandler>
{
	private static final class SingletonHolder
	{
		private static final VoicedCommandHandler INSTANCE = new VoicedCommandHandler();
	}
	
	public static VoicedCommandHandler getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private VoicedCommandHandler()
	{
		registerVoicedCommandHandler(new Banking());
		registerVoicedCommandHandler(new Offline());
		registerVoicedCommandHandler(new Wedding());
		registerVoicedCommandHandler(new CastleDoors());
		registerVoicedCommandHandler(new Hellbound());
		registerVoicedCommandHandler(new VersionInfo());
		registerVoicedCommandHandler(new Mail());
		registerVoicedCommandHandler(new Auction());
		
		_log.info("VoicedCommandHandler: Loaded " + size() + " handlers.");
	}
	
	@Override
	protected String standardizeKey(String key)
	{
		return key.trim().toLowerCase();
	}
	
	private void registerVoicedCommandHandler(IVoicedCommandHandler handler)
	{
		registerAll(handler, handler.getVoicedCommandList());
	}
	
	public boolean useVoicedCommand(String text, L2PcInstance activeChar)
	{
		if (!text.startsWith("."))
			return false;
		
		final StringTokenizer st = new StringTokenizer(text);
		
		final String command = st.nextToken(" .").toLowerCase(); // until the first space without the starting dot
		final String params;
		
		if (st.hasMoreTokens())
			params = st.nextToken("").trim(); // the rest
		else if (activeChar.getTarget() != null)
			params = activeChar.getTarget().getName();
		else
			params = "";
		
		if (GlobalRestrictions.useVoicedCommand(command, activeChar, params))
			return true;
		
		final IVoicedCommandHandler handler = get(command);
		
		if (handler != null)
			return handler.useVoicedCommand(command, activeChar, params);
		
		return false;
	}
}
