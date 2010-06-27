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

import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;

/**
 * This class ...
 * 
 * @version $Revision$ $Date$
 */
public class L2SiegeNpcInstance extends L2NpcInstance
{
	public L2SiegeNpcInstance(int objectID, L2NpcTemplate template)
	{
		super(objectID, template);
	}

	@Override
	public void showChatWindow(L2PcInstance player)
	{
		showSiegeInfoWindow(player);
	}

	/**
	 * If siege is in progress shows the Busy HTML<BR>
	 * else Shows the SiegeInfo window
	 * @param player
	 */
	public void showSiegeInfoWindow(L2PcInstance player)
	{
		if (validateCondition(player))
			getCastle().getSiege().listRegisterClan(player);
		else
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile("data/html/siege/" + getTemplate().getNpcId() + "-busy.htm");
			html.replace("%castlename%",getCastle().getName());
			html.replace("%objectId%",String.valueOf(getObjectId()));
			player.sendPacket(html);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	/**
	 * @param player
	 */
	private boolean validateCondition(L2PcInstance player)
	{
		return !getCastle().getSiege().getIsInProgress();
	}
}