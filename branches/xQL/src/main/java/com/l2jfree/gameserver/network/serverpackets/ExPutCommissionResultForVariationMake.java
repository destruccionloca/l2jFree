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

/**
 * Format: (ch)ddddd
 *
 */
public class ExPutCommissionResultForVariationMake extends L2GameServerPacket
{
	private static final String S_FE_55_EXPUTCOMMISSIONRESULTFORVARIATIONMAKE = "[S] FE:55 ExPutCommissionResultForVariationMake";

	private int _gemstoneObjId;
	private int _unk1;
	private int _gemstoneCount;
	private int _unk2;
	private int _unk3;

	public ExPutCommissionResultForVariationMake(int gemstoneObjId, int count)
	{
		_gemstoneObjId = gemstoneObjId;
		_unk1 = 1;
		_gemstoneCount = count;
		_unk2 = 1;
		_unk3 = 1;
	}

	/**
	 * @see com.l2jfree.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x55);
        writeD(_gemstoneObjId);
        writeD(_unk1);
        writeD(_gemstoneCount);
        writeD(_unk2);
        writeD(_unk3);
	}

	/**
	 * @see com.l2jfree.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return S_FE_55_EXPUTCOMMISSIONRESULTFORVARIATIONMAKE;
	}
}
