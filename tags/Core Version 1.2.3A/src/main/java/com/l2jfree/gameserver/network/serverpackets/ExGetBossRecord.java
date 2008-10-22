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

import com.l2jfree.gameserver.instancemanager.RaidPointsManager.PointList;
import java.util.Map;

/**
 * @author KenM
 */
public class ExGetBossRecord extends L2GameServerPacket
{
	private static final String _S__FE_34_EXGETBOSSRECORD = "[S] FE:34 ExGetBossRecord [ddd (dddd)]";

	private PointList _list;

	public ExGetBossRecord(PointList list)
	{
		_list = list;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x34);

		if (_list == null)
		{
			writeD(0);
			writeD(0);
			writeD(0);
			return;
		}

		writeD(_list.ranking);
		writeD(_list.scoreSum);
		writeD(_list.size()); //list size
		for (Map.Entry<Integer, Integer> e : _list.entrySet())
		{
			writeD(e.getKey());
			writeD(e.getValue());
			writeD(0x00); //??
		}
	}

	@Override
	public String getType() 
	{
		return _S__FE_34_EXGETBOSSRECORD;
	}
}
