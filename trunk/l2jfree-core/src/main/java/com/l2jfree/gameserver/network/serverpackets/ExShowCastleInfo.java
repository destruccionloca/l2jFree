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

import java.util.Map;

import com.l2jfree.gameserver.datatables.ClanTable;
import com.l2jfree.gameserver.instancemanager.CastleManager;
import com.l2jfree.gameserver.model.L2Clan;
import com.l2jfree.gameserver.model.entity.Castle;

/**
 *
 * @author  KenM
 */
public class ExShowCastleInfo extends L2GameServerPacket
{
    private static final String S_FE_14_EX_SHOW_CASTLE_INFO = "[S] FE:14 ExShowFortressInfo";

    /**
     * @see com.l2jfree.gameserver.serverpackets.L2GameServerPacket#getType()
     */
    @Override
    public String getType()
    {
        return S_FE_14_EX_SHOW_CASTLE_INFO;
    }

    /**
     * @see com.l2jfree.gameserver.serverpackets.L2GameServerPacket#writeImpl()
     */
    @Override
    protected void writeImpl()
    {
        writeC(0xfe);
        writeH(0x14);
        Map<Integer, Castle> castles = CastleManager.getInstance().getCastles();
        writeD(castles.size());
        for (Castle castle : castles.values())
        {
            writeD(castle.getCastleId());
            if (castle.getOwnerId() > 0)
            {
                L2Clan owner = ClanTable.getInstance().getClan(castle.getOwnerId());
                if (owner != null)
                    writeS(owner.getName());
                else
                {
                    _log.warn("Castle owner with no name! Castle: " + castle.getName() + " has an OwnerId = " + castle.getOwnerId() + " who does not have a  name!");
                    writeS("");
                }
            }
            else
                writeS("");
            writeD(castle.getTaxPercent());
            writeD((int)(castle.getSiege().getSiegeDate().getTimeInMillis()/1000));
        }
    }
}
