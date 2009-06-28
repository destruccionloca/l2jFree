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

import java.util.Arrays;
import java.util.StringTokenizer;

import com.l2jfree.Config;
import com.l2jfree.gameserver.ai.CtrlIntention;
import com.l2jfree.gameserver.datatables.ClanTable;
import com.l2jfree.gameserver.instancemanager.ClanHallManager;
import com.l2jfree.gameserver.model.L2Clan;
import com.l2jfree.gameserver.model.entity.ClanHall;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;
import com.l2jfree.gameserver.util.Evolve;

/**
 * This class ...
 * 
 * @version $Revision$ $Date$
 */
public class L2DoormenInstance extends L2NpcInstance
{
	private ClanHall			_clanHall;
	private static final int	COND_ALL_FALSE				= 0;
	private static final int	COND_BUSY_BECAUSE_OF_SIEGE	= 1;
	private static final int	COND_CASTLE_OWNER			= 2;
	private static final int	COND_HALL_OWNER				= 3;
	private static final int	COND_FORT_OWNER				= 4;

	// list of clan halls with evolve function, should be sorted
	private static final int[] CH_WITH_EVOLVE = {36, 37, 38, 39, 40, 41, 51, 52, 53, 54, 55, 56, 57};

	/**
	 * @param template
	 */
	public L2DoormenInstance(int objectID, L2NpcTemplate template)
	{
		super(objectID, template);
	}

	public final ClanHall getClanHall()
	{
		if (_clanHall == null)
			_clanHall = ClanHallManager.getInstance().getNearbyClanHall(getX(), getY(), 500);
		return _clanHall;
	}

	public final boolean hasEvolve()
	{
		if (getClanHall() == null)
			return false;

		return Arrays.binarySearch(CH_WITH_EVOLVE, getClanHall().getId()) >= 0;
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		int condition = validateCondition(player);
		if (condition <= COND_ALL_FALSE)
			return;
		if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
			return;
		else if (condition == COND_CASTLE_OWNER || condition == COND_HALL_OWNER || condition == COND_FORT_OWNER)
		{
			if (command.startsWith("Chat"))
			{
				showMessageWindow(player);
				return;
			}
			else if (command.startsWith("open_doors"))
			{
				if (condition == COND_HALL_OWNER)
				{
					getClanHall().openCloseDoors(true);
					player
							.sendPacket(new NpcHtmlMessage(
									getObjectId(),
									"<html><body>You have <font color=\"FF9955\">opened</font> the clan hall door.<br>Outsiders may enter the clan hall while the door is open. Please close it when you've finished your business.<br><center><button value=\"Close\" action=\"bypass -h npc_"
											+ getObjectId()
											+ "_close_doors\" width=80 height=27 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></body></html>"));
				}
				else if (condition == COND_CASTLE_OWNER)
				{
					if (!validatePrivileges(player, L2Clan.CP_CS_OPEN_DOOR)) return;
					if (!Config.SIEGE_GATE_CONTROL && getCastle().getSiege().getIsInProgress()) {
						player.sendPacket(SystemMessageId.GATES_NOT_OPENED_CLOSED_DURING_SIEGE);
						return;
					}
					StringTokenizer st = new StringTokenizer(command.substring(10), ", ");
					st.nextToken(); // Bypass first value since its castleid/hallid/fortid

					while (st.hasMoreTokens())
					{
						getCastle().openDoor(player, Integer.parseInt(st.nextToken()));
					}
					return;
				}
				else if (condition == COND_FORT_OWNER)
				{
					StringTokenizer st = new StringTokenizer(command.substring(10), ", ");
					st.nextToken(); // Bypass first value since its castleid/hallid/fortid

					while (st.hasMoreTokens())
					{
						getFort().openDoor(Integer.parseInt(st.nextToken()));
					}
					return;
				}
			}
			else if (command.startsWith("close_doors"))
			{
				if (condition == COND_HALL_OWNER)
				{
					getClanHall().openCloseDoors(false);
					player.sendPacket(new NpcHtmlMessage(getObjectId(),
							"<html><body>You have <font color=\"FF9955\">closed</font> the clan hall door.<br>Good day!<br><center><button value=\"To Beginning\" action=\"bypass -h npc_"
									+ getObjectId()
									+ "_Chat\" width=80 height=27 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></center></body></html>"));
				}
				else if (condition == COND_CASTLE_OWNER)
				{
					if (!validatePrivileges(player, L2Clan.CP_CS_OPEN_DOOR)) return;
					if (!Config.SIEGE_GATE_CONTROL && getCastle().getSiege().getIsInProgress()) {
						player.sendPacket(SystemMessageId.GATES_NOT_OPENED_CLOSED_DURING_SIEGE);
						return;
					}
					StringTokenizer st = new StringTokenizer(command.substring(10), ", ");
					st.nextToken(); // Bypass first value since its castleid/hallid/fortid

					while (st.hasMoreTokens())
					{
						getCastle().closeDoor(player, Integer.parseInt(st.nextToken()));
					}
					return;
				}
				else if (condition == COND_FORT_OWNER)
				{
					StringTokenizer st = new StringTokenizer(command.substring(10), ", ");
					st.nextToken(); // Bypass first value since its castleid/hallid/fortid

					while (st.hasMoreTokens())
					{
						getFort().closeDoor(Integer.parseInt(st.nextToken()));
					}
					return;
				}
			}
			else if (command.startsWith("evolve"))
			{
				StringTokenizer st = new StringTokenizer(command, " ");
				if (st.countTokens() < 2 || !hasEvolve())
					return;
				
				st.nextToken();
				boolean ok = false;
				switch (Integer.parseInt(st.nextToken()))
				{
					case 1:
						ok = Evolve.doEvolve(player, this, 9882, 10307, 55);
						break;
					case 2:
						ok = Evolve.doEvolve(player, this, 4422, 10308, 55);
						break;
					case 3:
						ok = Evolve.doEvolve(player, this, 4423, 10309, 55);
						break;
					case 4:
						ok = Evolve.doEvolve(player, this, 4424, 10310, 55);
						break;
					case 5:
						ok = Evolve.doEvolve(player, this, 10426, 10611, 70);
						break;
				}
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				if (ok)
					html.setFile("data/html/clanHallDoormen/evolve-ok.htm");
				else
					html.setFile("data/html/clanHallDoormen/evolve-no.htm");
				player.sendPacket(html);
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
		String filename = "data/html/doormen/" + getTemplate().getNpcId() + "-no.htm";

		int condition = validateCondition(player);
		if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
			filename = "data/html/doormen/" + getTemplate().getNpcId() + "-busy.htm"; // Busy because of siege
		else if (condition == COND_CASTLE_OWNER || condition == COND_FORT_OWNER) // Clan owns castle or fort
			filename = "data/html/doormen/" + getTemplate().getNpcId() + ".htm"; // Owner message window

		// Prepare doormen for clan hall
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		if (getClanHall() != null)
		{
			L2Clan owner = ClanTable.getInstance().getClan(getClanHall().getOwnerId());
			if (condition == COND_HALL_OWNER)
			{
				if (hasEvolve())
				{
					html.setFile("data/html/clanHallDoormen/doormen2.htm");
					html.replace("%clanname%", owner.getName());
				}
				else
				{
					html.setFile("data/html/clanHallDoormen/doormen1.htm");
					html.replace("%clanname%", owner.getName());
				}
			}
			else
			{
				if (owner != null && owner.getLeader() != null)
				{
					html.setFile("data/html/clanHallDoormen/doormen-no.htm");
					html.replace("%leadername%", owner.getLeaderName());
					html.replace("%clanname%", owner.getName());
				}
				else
				{
					html.setFile("data/html/clanHallDoormen/emptyowner.htm");
					html.replace("%hallname%", getClanHall().getName());
				}
			}
		}
		else
			html.setFile(filename);

		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}

	private boolean validatePrivileges(L2PcInstance player, int privilege)
	{
		if ((player.getClanPrivileges() & privilege) != privilege)
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return false;
		}
		return true;
	}

	private int validateCondition(L2PcInstance player)
	{
		if (player.getClan() != null)
		{
			int clanId = player.getClanId();
			// Prepare doormen for clan hall
			if (getClanHall() != null)
			{
				if (getClanHall().getOwnerId() == clanId)
					return COND_HALL_OWNER;
			}
			if (getCastle() != null && getCastle().getCastleId() > 0)
			{
				if (getCastle().getOwnerId() == clanId) // Clan owns castle
					return COND_CASTLE_OWNER;
			}
			if (getFort() != null && getFort().getFortId() > 0)
			{
				if (getFort().getOwnerId() == clanId) // Clan owns fort
					return COND_FORT_OWNER;
			}
		}
		return COND_ALL_FALSE;
	}
}