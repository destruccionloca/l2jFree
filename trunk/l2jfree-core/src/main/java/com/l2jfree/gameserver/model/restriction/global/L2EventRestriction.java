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
package com.l2jfree.gameserver.model.restriction.global;

import com.l2jfree.gameserver.model.actor.L2Npc;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.entity.EventData;
import com.l2jfree.gameserver.model.entity.L2Event;

/**
 * @author NB4L1
 */
public final class L2EventRestriction extends AbstractRestriction// extends AbstractFunEventRestriction
{
	private static final class SingletonHolder
	{
		private static final L2EventRestriction INSTANCE = new L2EventRestriction();
	}
	
	public static L2EventRestriction getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private L2EventRestriction()
	{
	}
	
	@Override
	public boolean isRestricted(L2PcInstance activeChar, Class<? extends GlobalRestriction> callingRestriction)
	{
		if (activeChar.atEvent)
		{
			activeChar.sendMessage("You are in an event!");
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean canRequestRevive(L2PcInstance activeChar)
	{
		if (activeChar.atEvent)
			return false;
		
		return true;
	}
	
	@Override
	public boolean canStandUp(L2PcInstance activeChar)
	{
		if (L2Event.active && activeChar.eventSitForced)
		{
			activeChar.sendMessage("A dark force beyond your mortal understanding makes your knees to shake when you try to stand up...");
			return false;
		}
		
		return true;
	}
	
	@Override
	public void playerLoggedIn(L2PcInstance activeChar)
	{
		if (L2Event.connectionLossData.containsKey(activeChar.getName()))
		{
			if (L2Event.active && L2Event.isOnEvent(activeChar))
				L2Event.restoreChar(activeChar);
			else
				L2Event.restoreAndTeleChar(activeChar);
		}
	}
	
	@Override
	public void playerDisconnected(L2PcInstance activeChar)
	{
		// we store all data from players who are disconnected while
		// in an event in order to restore it in the next login
		if (activeChar.atEvent)
		{
			L2Event.connectionLossData.put(activeChar.getName(), new EventData(activeChar.eventX, activeChar.eventY,
				activeChar.eventZ, activeChar.eventKarma, activeChar.eventPvpKills, activeChar.eventPkKills,
				activeChar.eventTitle, activeChar.kills, activeChar.eventSitForced));
		}
	}
	
	@Override
	public boolean onBypassFeedback(L2Npc npc, L2PcInstance activeChar, String command)
	{
		if (command.startsWith("event_participate"))
		{
			L2Event.inscribePlayer(activeChar);
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean onAction(L2Npc npc, L2PcInstance activeChar)
	{
		if (npc.isEventMob)
		{
			L2Event.showEventHtml(activeChar, String.valueOf(npc.getObjectId()));
			return true;
		}
		
		return false;
	}
}
