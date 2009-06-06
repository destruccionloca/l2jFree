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

import com.l2jfree.Config;

/**
 *
 * @author  KenM
 */
public class ExEnchantSkillInfoDetail extends L2GameServerPacket
{
    private final int _itemId;
    private final long _itemCount;

    public ExEnchantSkillInfoDetail(int itemId, long itemCount)
    {
        _itemId = itemId;
        _itemCount = itemCount;
    }

    /**
     * @see com.l2jfree.gameserver.serverpackets.L2GameServerPacket#getType()
     */
    @Override
    public String getType()
    {
        return "[S] FE:5E ExEnchantSkillInfoDetail";
    }

    /**
     * @see com.l2jfree.gameserver.serverpackets.L2GameServerPacket#writeImpl()
     */
    @Override
    protected void writeImpl()
    {
        writeC(0xfe);
        writeH(0x5e);

        writeD(0);
        writeD(0);
        writeD(0);
        writeD(0);
        writeQ(0);
        writeD(0);
        if (Config.PACKET_FINAL)
        	writeQ(_itemCount); // Count
        else
        	writeD(toInt(_itemCount)); // Count
        writeD(0);
        writeD(_itemId); // ItemId Required
        writeD(0);
    }
}
