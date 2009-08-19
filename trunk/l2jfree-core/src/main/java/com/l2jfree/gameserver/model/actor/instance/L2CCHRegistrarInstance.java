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

import com.l2jfree.gameserver.ai.CtrlIntention;
import com.l2jfree.gameserver.instancemanager.ClanHallManager;
import com.l2jfree.gameserver.model.entity.ClanHall;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jfree.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfree.gameserver.network.serverpackets.ValidateLocation;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;

/**
 * Represents a NPC that registers clans for clan hall siege.
 * @author Savormix
 */
public final class L2CCHRegistrarInstance extends L2NpcInstance
{
	private int _hideoutIndex = -2;

	public L2CCHRegistrarInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		switch (getNpcId())
		{
		case 35420: _hideoutIndex = 34; break;
		case 35639: _hideoutIndex = 64; break;
		}
	}

	private final ClanHall getHideout()
	{
		if (_hideoutIndex < 0)
			return null;
		else
			return ClanHallManager.getInstance().getClanHallById(_hideoutIndex);
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		if (!canTarget(player)) return;

		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);

			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			MyTargetSelected my = new MyTargetSelected(player, this);
			player.sendPacket(my);

			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			// Calculate the distance between the L2PcInstance and the L2NpcInstance
			if (!canInteract(player))
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			}
			else
			{
				showSiegeInfoWindow(player);
			}
		}
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	public void showSiegeInfoWindow(L2PcInstance player)
	{
		if (validateCondition(player))
			getHideout().getSiege().listRegisterClan(player);
		else
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile("data/html/siege/" + getTemplate().getNpcId() + "-busy.htm");
			html.replace("%castlename%",getCastle().getName());
			html.replace("%objectId%",String.valueOf(getObjectId()));
			player.sendPacket(html);
		}
	}

	private boolean validateCondition(L2PcInstance player)
	{
		if (getHideout() == null)
			return false;
        return !getHideout().getSiege().getIsInProgress();
    }
}
