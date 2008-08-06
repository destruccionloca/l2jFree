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

import java.util.List;

import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

import javolution.util.FastList;

public class PartySmallWindowAll extends L2GameServerPacket
{
	private static final String _S__4E_PARTYSMALLWINDOWALL = "[S] 4e PartySmallWindowAll [ddd (dsddddddddddd)]";
	private List<L2PcInstance> _partyMembers = new FastList<L2PcInstance>();
	private L2PcInstance _exclude;

	public PartySmallWindowAll(L2PcInstance exclude, List<L2PcInstance> party)
	{
		_exclude = exclude;
		_partyMembers = party;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x4e);
		writeD(_partyMembers.get(0).getObjectId()); // c3 party leader id
		writeD(_partyMembers.get(0).getParty().getLootDistribution());//c3 party loot type (0,1,2,....)
		writeD(_partyMembers.size()-1);
		
		for(int i = 0; i < _partyMembers.size(); i++) 
		{
			L2PcInstance member = _partyMembers.get(i);
			if (member != _exclude)
			{
				writeD(member.getObjectId());
				writeS(member.getName());
				
				writeD((int) member.getStatus().getCurrentCp()); //c4
				writeD(member.getMaxCp()); //c4
				
				writeD((int) member.getStatus().getCurrentHp());
				writeD(member.getMaxHp());
				writeD((int) member.getStatus().getCurrentMp());
				writeD(member.getMaxMp());
				writeD(member.getLevel());
				writeD(member.getClassId().getId());
				writeD(0x00);//writeD(0x01); ??
				writeD(member.getRace().ordinal());
                writeD(0x00);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__4E_PARTYSMALLWINDOWALL;
	}
}
