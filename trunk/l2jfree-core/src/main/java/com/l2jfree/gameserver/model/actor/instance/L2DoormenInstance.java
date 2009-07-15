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

import java.util.StringTokenizer;

import com.l2jfree.Config;
import com.l2jfree.gameserver.ai.CtrlIntention;
import com.l2jfree.gameserver.datatables.DoorTable;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;

/**
 * This class ...
 * 
 * @version $Revision$ $Date$
 */
public class L2DoormenInstance extends L2NpcInstance
{
	/**
	 * @param template
	 */
	public L2DoormenInstance(int objectID, L2NpcTemplate template)
	{
		super(objectID, template);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (command.startsWith("Chat"))
		{
			showMessageWindow(player);
		}
		else if (command.startsWith("open_doors"))
		{
			StringTokenizer st = new StringTokenizer(command.substring(10), ", ");
			while (st.hasMoreTokens())
			{
				DoorTable.getInstance().getDoor(Integer.parseInt(st.nextToken())).openMe();
			}
		}
		else if (command.startsWith("close_doors"))
		{
			StringTokenizer st = new StringTokenizer(command.substring(11), ", ");
			while (st.hasMoreTokens())
			{
				DoorTable.getInstance().getDoor(Integer.parseInt(st.nextToken())).closeMe();
			}
		}
		else
			super.onBypassFeedback(player, command);
	}

	/**
	 * this is called when a player interacts with this NPC
	 * 
	 * @param player
	 */
	@Override
	public void onAction(L2PcInstance player)
	{
		if (!canTarget(player))
			return;

		player.setLastFolkNPC(this);

		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);
		}
		else
		{
			// Calculate the distance between the L2PcInstance and the
			// L2NpcInstance
			if (!canInteract(player))
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			}
			else
			{
				showMessageWindow(player);
			}
		}
		// Send a Server->Client ActionFailed to the L2PcInstance in order to
		// avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	public void showMessageWindow(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);

		// Prepare doormen for clan hall
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile("data/html/doormen/" + getNpcId() + ".htm");
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}
}