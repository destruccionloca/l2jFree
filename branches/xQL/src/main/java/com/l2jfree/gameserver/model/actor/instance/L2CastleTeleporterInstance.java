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
package com.l2jfree.gameserver.model.actor.instance;

/**
 * @author NightMarez
 * @version $Revision: 1.3.2.2.2.5 $ $Date: 2005/03/27 15:29:32 $
 *
 */

import java.util.StringTokenizer;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.gameserver.datatables.TeleportLocationTable;
import com.l2jfree.gameserver.model.L2CharPosition;
import com.l2jfree.gameserver.model.L2TeleportLocation;
import com.l2jfree.gameserver.model.restriction.AvailableRestriction;
import com.l2jfree.gameserver.model.restriction.ObjectRestrictions;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfree.gameserver.templates.L2NpcTemplate;

public final class L2CastleTeleporterInstance extends L2FolkInstance
{
    private final static Log _log = LogFactory.getLog(L2CastleTeleporterInstance.class.getName());

    private static final int COND_ALL_FALSE = 0;
	private static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	private static final int COND_OWNER = 2;
	private static final int COND_REGULAR = 3;
	
	/**
	 * @param template
	 */
	public L2CastleTeleporterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{	
		int condition = validateCondition(player);
		if (condition <= COND_BUSY_BECAUSE_OF_SIEGE)
			return;

		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken(); // Get actual command

		if (actualCommand.equalsIgnoreCase("goto"))
		{
			if (st.countTokens() <= 0) {return;}
			int whereTo = Integer.parseInt(st.nextToken());
			if (condition == COND_REGULAR)
			{
				doTeleport(player, whereTo);
				return;
			}
			else if (condition == COND_OWNER)
			{
				int minPrivilegeLevel = 0; // NOTE: Replace 0 with highest level when privilege level is implemented
				if (st.countTokens() >= 1) {minPrivilegeLevel = Integer.parseInt(st.nextToken());}
				if (10 >= minPrivilegeLevel) // NOTE: Replace 10 with privilege level of player
					doTeleport(player, whereTo);
				else
					player.sendMessage("You don't have the sufficient access level to teleport there.");
				return;
			}
		}
		super.onBypassFeedback(player, command);
	}

	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		if (val == 0)
		{
			pom = "" + npcId;
		} 
		else 
		{
			pom = npcId + "-" + val;
		}
		
		return "data/html/teleporter/" + pom + ".htm";
	}

	
	@Override
	public void showChatWindow(L2PcInstance player)
	{
		String filename = "data/html/teleporter/castleteleporter-no.htm";
		
		int condition = validateCondition(player);
		if (condition == COND_REGULAR)
		{
		    super.showChatWindow(player);
		    return;
		}
		else if (condition > COND_ALL_FALSE)
		{
	        if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
	            filename = "data/html/teleporter/castleteleporter-busy.htm";  // Busy because of siege
	        else if (condition == COND_OWNER)                                 // Clan owns castle
	            filename = "data/html/teleporter/" + getNpcId() + ".htm";     // Owner message window
		}

        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}

	private void doTeleport(L2PcInstance player, int val)
	{
		L2TeleportLocation list = TeleportLocationTable.getInstance().getTemplate(val);
		if (list != null)
		{
			if (ObjectRestrictions.getInstance()
					.checkRestriction(player, AvailableRestriction.PlayerTeleport)) {
				player.sendMessage("You cannot teleport due to a restriction.");
				return;
			}
				
			if(player.reduceAdena("Teleport", list.getPrice(), player.getLastFolkNPC(), true))
			{
				if (_log.isDebugEnabled())
					_log.debug("Teleporting player "+player.getName()+" to new location: "+list.getLocX()+":"+list.getLocY()+":"+list.getLocZ());
                
				// teleport
				player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ());
				player.stopMove(new L2CharPosition(list.getLocX(), list.getLocY(), list.getLocZ(), player.getHeading()));
			}
		}
		else
		{
			_log.warn("No teleport destination with id:" +val);
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	private int validateCondition(L2PcInstance player)
	{
		if (player.getClan() != null && getCastle() != null)
		{
			if (getCastle().getSiege().getIsInProgress())
				return COND_BUSY_BECAUSE_OF_SIEGE;                   // Busy because of siege
			else if (getCastle().getOwnerId() == player.getClanId()) // Clan owns castle
				return COND_OWNER;                                   // Owner
		}
		
		return COND_ALL_FALSE;
	}
}
