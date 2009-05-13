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

import com.l2jfree.Config;
import com.l2jfree.gameserver.Announcements;
import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Npc;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.entity.events.VIP;

/**
 * @author NB4L1
 */
final class VIPRestriction extends AbstractFunEventRestriction
{
	@Override
	boolean started()
	{
		return VIP._started;
	}
	
	@Override
	boolean allowInterference()
	{
		return Config.VIP_ALLOW_INTERFERENCE;
	}
	
	@Override
	boolean isInFunEvent(L2PcInstance player)
	{
		return player._inEventVIP;
	}
	
	@Override
	public void playerLoggedIn(L2PcInstance activeChar)
	{
		if (VIP._savePlayers.contains(activeChar.getName()))
			VIP.addDisconnectedPlayer(activeChar);
	}
	
	@Override
	public boolean playerKilled(L2Character activeChar, final L2PcInstance target)
	{
		if (!target._inEventVIP)
			return false;
		
		L2PcInstance pk = activeChar.getActingPlayer();
		
		if (pk != null)
		{
			if (VIP._started)
			{
				if (target._isTheVIP && pk._inEventVIP)
				{
					VIP.vipDied();
				}
				else if (target._isTheVIP && !pk._inEventVIP)
				{
					Announcements.getInstance().announceToAll("VIP Killed by non-event character. VIP going back to initial spawn.");
					target.doRevive();
					target.teleToLocation(VIP._startX, VIP._startY, VIP._startZ);
				}
				else if (target._isTheVIP && pk._isVIP)
				{
					Announcements.getInstance().announceToAll("VIP Killed by same team player. VIP going back to initial spawn.");
					target.doRevive();
					target.teleToLocation(VIP._startX, VIP._startY, VIP._startZ);
				}
				else
				{
					target.sendMessage("You will be revived and teleported to team spot in 20 seconds!");
					ThreadPoolManager.getInstance().scheduleGeneral(new Runnable() {
						public void run()
						{
							target.doRevive();
							if (target._isVIP)
								target.teleToLocation(VIP._startX, VIP._startY, VIP._startZ);
							else
								target.teleToLocation(VIP._endX, VIP._endY, VIP._endZ);
						}
					}, 20000);
				}
			}
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean onBypassFeedback(L2Npc npc, L2PcInstance activeChar, String command)
	{
		if (command.startsWith("vip_joinVIPTeam"))
		{
			VIP.addPlayerVIP(activeChar);
			return true;
		}
		else if (command.startsWith("vip_joinNotVIPTeam"))
		{
			VIP.addPlayerNotVIP(activeChar);
			return true;
		}
		else if (command.startsWith("vip_finishVIP"))
		{
			VIP.vipWin(activeChar);
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean onAction(L2Npc npc, L2PcInstance activeChar)
	{
		if (npc._isEventVIPNPC)
		{
			VIP.showJoinHTML(activeChar, String.valueOf(npc.getObjectId()));
			return true;
		}
		else if (npc._isEventVIPNPCEnd)
		{
			VIP.showEndHTML(activeChar, String.valueOf(npc.getObjectId()));
			return true;
		}
		
		return false;
	}
}
