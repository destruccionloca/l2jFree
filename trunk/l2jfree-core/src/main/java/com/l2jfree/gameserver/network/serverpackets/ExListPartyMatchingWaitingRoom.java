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

import java.util.Collection;

import javolution.util.FastList;

import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

/**
 * Format:(ch) d [sdd]
 * @author  Crion/kombat
 */

public class ExListPartyMatchingWaitingRoom extends L2GameServerPacket
{
	private FastList<L2PcInstance> _waiting;

	private int _minLevel = 1; // To be implemented :)
	private int _maxLevel = 80; // To be implemented :)

	/**
	 * @param waiting  
	 * @param searcher  
	 * @param page  
	 */
	public ExListPartyMatchingWaitingRoom(Collection<L2PcInstance> waiting, L2PcInstance searcher, int page)
	{
		int first = (page - 1) * 64;
		int firstNot = page * 64;

		int i = -1;
		for (L2PcInstance pc : waiting)
		{
			if (pc.getLevel() >= _minLevel && pc.getLevel() <= _maxLevel && !pc.isGM())
			{
				i++;

				if (i < first || i >= firstNot)
					continue;
				if (_waiting == null)
					_waiting = new FastList<L2PcInstance>();
				_waiting.add(pc);
			}
		}
	}

	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x36);

		writeD(_waiting.size()); // Client shows size/64 max pages, so there should be 64 players per page on offi... but could modify this

		if (_waiting == null)
		{
			writeD(0);
			return;
		}

		writeD(_waiting.size());
		for (L2PcInstance p : _waiting)
		{
			writeS(p.getName());
			writeD(p.getClassId().getId());
			writeD(p.getLevel());
		}
	}

	/**
	 * @see com.l2jfree.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return "FE_36_ExListPartyMatchingWaitingRoom";
	}
}
