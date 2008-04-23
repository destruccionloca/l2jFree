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
package net.sf.l2j.gameserver.network.serverpackets;

import java.util.List;

import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.instancemanager.FortManager;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.entity.Fort;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author  KenM
 */
public class ExShowFortressInfo extends L2GameServerPacket
{
    private static final String S_FE_15_EX_SHOW_FORTRESS_INFO = "[S] FE:15 ExShowFortressInfo";
    private static final Log _log = LogFactory.getLog(ExShowFortressInfo.class.getName());

    /**
     * @see net.sf.l2j.gameserver.serverpackets.L2GameServerPacket#getType()
     */
    @Override
    public String getType()
    {
        return S_FE_15_EX_SHOW_FORTRESS_INFO;
    }

    /**
     * @see net.sf.l2j.gameserver.serverpackets.L2GameServerPacket#writeImpl()
     */
    @Override
    protected void writeImpl()
    {
        writeC(0xfe);
        writeH(0x15);
        List<Fort> forts = FortManager.getInstance().getForts();
        writeD(forts.size());
        for (Fort fort : forts)
        {
            writeD(fort.getFortId());
            if (fort.getOwnerId() > 0)
            {
                L2Clan owner = ClanTable.getInstance().getClan(fort.getOwnerId());
                if (owner != null)
                    writeS(owner.getName());
                else
                {
                    _log.warn("Fort owner with no name! Fort: " + fort.getName() + " has an OwnerId = " + fort.getOwnerId() + " who does not have a  name!");
                    writeS("");
               }
            }
            else
                writeS("");
            
            if ((fort.getSiege().getIsScheduled()) || (fort.getSiege().getIsInProgress()))
                writeD(1);
            else
                writeD(0);
            
            // Time of possession
            writeD(fort.getOwnedTime());
        }
    }
}
