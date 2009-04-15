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
package com.l2jfree.gameserver.network.serverpackets;

import javolution.text.TextBuilder;

import com.l2jfree.gameserver.datatables.ClanTable;
import com.l2jfree.gameserver.model.L2Clan;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.L2GameClient;
import com.l2jfree.gameserver.network.SystemMessageId;

/**
 */
public class AllyInfo extends L2GameServerPacket
{
	//private final static Log _log = LogFactory.getLog(AllyInfo.class.getName());
	private static final String _S__B5_ALLYINFO = "[S] b5 AllyInfo";
	private L2PcInstance _cha;
	
	public AllyInfo(L2PcInstance cha)
	{
		_cha = cha;
	}
	
	@Override
	public void runImpl(final L2GameClient client, final L2PcInstance activeChar)
	{
		if (activeChar == null)
			return;
		
		if (activeChar.getAllyId() == 0)
		{
			_cha.sendPacket(SystemMessageId.NO_CURRENT_ALLIANCES);
			return;
		}
		
		//======<AllyInfo>======
		SystemMessage sm = null;
		_cha.sendPacket(SystemMessageId.ALLIANCE_INFO_HEAD);
		//======<Ally Name>======
		sm = new SystemMessage(SystemMessageId.ALLIANCE_NAME_S1);
		sm.addString(_cha.getClan().getAllyName());
		_cha.sendPacket(sm);
		int online = 0;
		int count = 0;
		int clancount = 0;
		for (L2Clan clan : ClanTable.getInstance().getClans())
		{
			if (clan.getAllyId() == _cha.getAllyId())
			{
				clancount++;
				online += clan.getOnlineMembers(0).length;
				count += clan.getMembers().length;
			}
		}
		//Connection
		sm = new SystemMessage(SystemMessageId.CONNECTION_S1_TOTAL_S2);
		sm.addNumber(online);
		sm.addNumber(count);
		_cha.sendPacket(sm);
		L2Clan leaderclan = ClanTable.getInstance().getClan(_cha.getAllyId());
		sm = new SystemMessage(SystemMessageId.ALLIANCE_LEADER_S2_OF_S1);
		sm.addString(leaderclan.getName());
		sm.addString(leaderclan.getLeaderName());
		_cha.sendPacket(sm);
		//clan count
		sm = new SystemMessage(SystemMessageId.ALLIANCE_CLAN_TOTAL_S1);
		sm.addNumber(clancount);
		_cha.sendPacket(sm);
		//clan information
		_cha.sendPacket(SystemMessageId.CLAN_INFO_HEAD);
		for (L2Clan clan : ClanTable.getInstance().getClans())
		{
			if (clan.getAllyId() == _cha.getAllyId())
			{
				//clan name
				sm = new SystemMessage(SystemMessageId.CLAN_INFO_NAME_S1);
				sm.addString(clan.getName());
				_cha.sendPacket(sm);
				//clan leader name
				sm = new SystemMessage(SystemMessageId.CLAN_INFO_LEADER_S1);
				sm.addString(clan.getLeaderName());
				_cha.sendPacket(sm);
				//clan level
				sm = new SystemMessage(SystemMessageId.CLAN_INFO_LEVEL_S1);
				sm.addNumber(clan.getLevel());
				_cha.sendPacket(sm);
				//---------
				_cha.sendPacket(SystemMessageId.CLAN_INFO_SEPARATOR);
			}
		}
		//=========================
		_cha.sendPacket(SystemMessageId.CLAN_INFO_FOOT);
		NpcHtmlMessage adminReply = new NpcHtmlMessage(0);
		TextBuilder replyMSG = new TextBuilder("<html><title>Alliance Information</title><body>");
		replyMSG.append("<center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32></center>");
		for (L2Clan clan : ClanTable.getInstance().getClans())
			if (clan.getAllyId() == _cha.getAllyId())
			{
				replyMSG
					.append("<br><center><button value=\"")
					.append(clan.getName())
					.append("\" action=\"bypass -h show_clan_info ")
					.append(clan.getName())
					.append(
						"\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></center><br>");
			}
		replyMSG.append("<center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32></center>");
		replyMSG.append("</body></html>");
		adminReply.setHtml(replyMSG.toString());
		_cha.sendPacket(adminReply);
	}
	
	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__B5_ALLYINFO;
	}
}
