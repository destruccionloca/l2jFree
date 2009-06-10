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
package com.l2jfree.gameserver.network.clientpackets;

import java.util.StringTokenizer;

import com.l2jfree.Config;
import com.l2jfree.gameserver.ai.CtrlIntention;
import com.l2jfree.gameserver.communitybbs.CommunityBoard;
import com.l2jfree.gameserver.datatables.ClanTable;
import com.l2jfree.gameserver.handler.AdminCommandHandler;
import com.l2jfree.gameserver.model.L2CharPosition;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.L2Npc;
import com.l2jfree.gameserver.model.actor.instance.L2MerchantSummonInstance;
import com.l2jfree.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.entity.L2Event;
import com.l2jfree.gameserver.model.entity.events.CTF;
import com.l2jfree.gameserver.model.entity.events.DM;
import com.l2jfree.gameserver.model.entity.events.TvT;
import com.l2jfree.gameserver.model.entity.events.VIP;
import com.l2jfree.gameserver.model.entity.events.TvTInstanced.TvTIMain;
import com.l2jfree.gameserver.model.olympiad.Olympiad;
import com.l2jfree.gameserver.network.InvalidPacketException;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.GMViewPledgeInfo;
import com.l2jfree.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * This class represents a packet sent when player clicks a link in the chat dialog.
 * In HTML files it is <a action="bypass -h command"/>
 * 
 * @version $Revision: 1.12.4.5 $ $Date: 2005/04/11 10:06:11 $
 */
public class RequestBypassToServer extends L2GameClientPacket
{
	private static final String	_C__21_REQUESTBYPASSTOSERVER	= "[C] 21 RequestBypassToServer";

	// S
	private String				_command;

	@Override
	protected void readImpl()
	{
		_command = readS();
	}

	@Override
	protected void runImpl() throws InvalidPacketException
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null) return;

		if (_command.startsWith("admin_"))
			AdminCommandHandler.getInstance().useAdminCommand(activeChar, _command);
		else if (_command.equals("come_here") && activeChar.getAccessLevel() >= Config.GM_ACCESSLEVEL)
			comeHere(activeChar);
		else if (_command.startsWith("show_clan_info "))
			activeChar.sendPacket(new GMViewPledgeInfo(ClanTable.getInstance().getClanByName(_command.substring(15)), activeChar));
		else if (_command.startsWith("player_help "))
			playerHelp(activeChar, _command.substring(12));
		else if (_command.startsWith("npc_"))
		{
			activeChar.validateBypass(_command);

			int endOfId = _command.indexOf('_', 5);
			String id;
			if (endOfId > 0)
				id = _command.substring(4, endOfId);
			else
				id = _command.substring(4);
			try
			{
				L2Object object = null;
				int objectId = Integer.parseInt(id);

				// Get object from target
				if (activeChar.getTargetId() == objectId)
					object = activeChar.getTarget();

				// Get object from world
				if (object == null)
				{
					object = L2World.getInstance().findObject(objectId);
					// _log.warn("Player "+activeChar.getName()+" bypassed command to NPC outside of his knownlist.");
				}

				if (_command.substring(endOfId + 1).startsWith("event_participate"))
					L2Event.inscribePlayer(activeChar);

				else if (_command.substring(endOfId + 1).startsWith("vip_joinVIPTeam"))
					VIP.addPlayerVIP(activeChar);

				else if (_command.substring(endOfId + 1).startsWith("vip_joinNotVIPTeam"))
					VIP.addPlayerNotVIP(activeChar);

				else if (_command.substring(endOfId + 1).startsWith("vip_finishVIP"))
					VIP.vipWin(activeChar);

				else if (_command.substring(endOfId + 1).startsWith("tvt_player_join "))
				{
					String teamName = _command.substring(endOfId + 1).substring(16);

					if (TvT._joining)
						TvT.addPlayer(activeChar, teamName);
					else
						activeChar.sendMessage("The event is already started. You can not join now!");
				}

				else if (_command.substring(endOfId + 1).startsWith("tvt_player_leave"))
				{
					if (TvT._joining)
						TvT.removePlayer(activeChar);
					else
						activeChar.sendMessage("The event is already started. You can not leave now!");
				}

				else if (_command.substring(endOfId + 1).startsWith("dmevent_player_join"))
				{
					if (DM._joining)
						DM.addPlayer(activeChar);
					else
						activeChar.sendMessage("The event is already started. You can not join now!");
				}

				else if (_command.substring(endOfId + 1).startsWith("dmevent_player_leave"))
				{
					if (DM._joining)
						DM.removePlayer(activeChar);
					else
						activeChar.sendMessage("The event is already started. You can not leave now!");
				}

				else if (_command.substring(endOfId + 1).startsWith("ctf_player_join "))
				{
					String teamName = _command.substring(endOfId + 1).substring(16);

					if (CTF._joining)
						CTF.addPlayer(activeChar, teamName);
					else
						activeChar.sendMessage("The event is already started. You can not join now!");
				}

				else if (_command.substring(endOfId + 1).startsWith("ctf_player_leave"))
				{
					if (CTF._joining)
						CTF.removePlayer(activeChar);
					else
						activeChar.sendMessage("The event is already started. You can not leave now!");
				}

				else if (_command.substring(endOfId + 1).startsWith("tvt_player_leave"))
				{
					if (TvT._joining)
						TvT.removePlayer(activeChar);
					else
						activeChar.sendMessage("The event is already started. You can not leave now!");
				}

				else if (_command.substring(endOfId + 1).startsWith("tvti_player_join_page"))
				{
					TvTIMain.showInstancesHtml(activeChar, String.valueOf(TvTIMain.getJoinNpc().getLastSpawn().getObjectId()));
				}

				else if (_command.substring(endOfId + 1).startsWith("tvti_player_join "))
				{
					int instanceId = Integer.parseInt(_command.substring(endOfId + 1).substring(17));

					TvTIMain.addPlayer(activeChar, instanceId);
				}

				else if (_command.substring(endOfId + 1).startsWith("tvti_player_leave"))
				{
					TvTIMain.removePlayer(activeChar);
				}
				else if (object instanceof L2NpcInstance && endOfId > 0 && activeChar.isInsideRadius(object, L2NpcInstance.INTERACTION_DISTANCE, false, false))
					((L2NpcInstance) object).onBypassFeedback(activeChar, _command.substring(endOfId + 1));
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			}
			catch (NumberFormatException nfe)
			{
			} 
		}
		else if (_command.startsWith("summon_"))
		{
			activeChar.validateBypass(_command);
			
			int endOfId = _command.indexOf('_', 8);
			String id;
			if (endOfId > 0)
				id = _command.substring(7, endOfId);
			else
				id = _command.substring(7);
			try
			{
				int objectId = Integer.parseInt(id);
				
				L2MerchantSummonInstance summon = activeChar.getTarget(L2MerchantSummonInstance.class, objectId);
				
				if (summon != null && endOfId > 0
					&& activeChar.isInsideRadius(summon, L2Npc.INTERACTION_DISTANCE, false, false))
				{
					summon.onBypassFeedback(activeChar, _command.substring(endOfId + 1));
				}
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			}
			catch (NumberFormatException nfe)
			{
			}
		}
		//  Draw a Symbol
		else if (_command.equals("menu_select?ask=-16&reply=1"))
		{
			activeChar.validateBypass(_command);

			L2Object object = activeChar.getTarget();
			if (object instanceof L2Npc)
				((L2Npc) object).onBypassFeedback(activeChar, _command);
		}
		else if (_command.equals("menu_select?ask=-16&reply=2"))
		{
			//activeChar.validateBypass(_command); // FIXME: shouldn't we validate here too?

			L2Object object = activeChar.getTarget();
			if (object instanceof L2Npc)
				((L2Npc) object).onBypassFeedback(activeChar, _command);
		}
		// Navigate throught Manor windows
		else if (_command.startsWith("manor_menu_select?"))
		{
			//activeChar.validateBypass(_command); // FIXME: shouldn't we validate here too?

			L2Object object = activeChar.getTarget();
			if (object instanceof L2Npc)
				((L2Npc) object).onBypassFeedback(activeChar, _command);
		}
		else if (_command.startsWith("bbs_"))
			CommunityBoard.getInstance().handleCommands(getClient(), _command);
		else if (_command.startsWith("_bbs"))
			CommunityBoard.getInstance().handleCommands(getClient(), _command);
		else if (_command.startsWith("Quest "))
		{
			activeChar.validateBypass(_command);

			String p = _command.substring(6).trim();
			int idx = p.indexOf(' ');
			if (idx < 0)
				activeChar.processQuestEvent(p, "");
			else
				activeChar.processQuestEvent(p.substring(0, idx), p.substring(idx).trim());
		}
		else if (_command.startsWith("OlympiadArenaChange"))
			Olympiad.bypassChangeArena(_command, activeChar);

		sendPacket(ActionFailed.STATIC_PACKET);
	}

	private void comeHere(L2PcInstance activeChar)
	{
		L2Object obj = activeChar.getTarget();
		if (obj instanceof L2Npc)
		{
			L2Npc temp = (L2Npc) obj;
			temp.setTarget(activeChar);
			temp.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(activeChar.getX(), activeChar.getY(), activeChar.getZ(), 0));
		}
	}

	private void playerHelp(L2PcInstance activeChar, String path)
	{
		if (path.indexOf("..") != -1)
			return;

		StringTokenizer st = new StringTokenizer(path);
		String[] cmd = st.nextToken().split("#");

		if (cmd.length > 1)
		{
			int itemId = 0;
			itemId = Integer.parseInt(cmd[1]);
			String filename = "data/html/help/" + cmd[0];
			NpcHtmlMessage html = new NpcHtmlMessage(1, itemId);
			html.setFile(filename);
			activeChar.sendPacket(html);
		}
		else
		{
			String filename = "data/html/help/" + path;
			NpcHtmlMessage html = new NpcHtmlMessage(1);
			html.setFile(filename);
			activeChar.sendPacket(html);
		}
	}

	@Override
	public String getType()
	{
		return _C__21_REQUESTBYPASSTOSERVER;
	}
}