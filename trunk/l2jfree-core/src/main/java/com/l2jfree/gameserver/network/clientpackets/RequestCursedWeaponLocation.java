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

import javolution.util.FastList;

import com.l2jfree.gameserver.instancemanager.CursedWeaponsManager;
import com.l2jfree.gameserver.model.CursedWeapon;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.Location;
import com.l2jfree.gameserver.network.serverpackets.ExCursedWeaponLocation;
import com.l2jfree.gameserver.network.serverpackets.ExCursedWeaponLocation.CursedWeaponInfo;

/**
 * Format: (ch)
 * @author  -Wooden-
 */
public class RequestCursedWeaponLocation extends L2GameClientPacket
{
    private static final String _C__D0_23_REQUESTCURSEDWEAPONLOCATION = "[C] D0:23 RequestCursedWeaponLocation";

    @Override
    protected void readImpl()
    {
        //nothing to read it's just a trigger
    }

    /**
     * @see com.l2jfree.gameserver.network.clientpackets.ClientBasePacket#runImpl()
     */
    @Override
    protected void runImpl()
    {
        L2Character activeChar = getClient().getActiveChar();
        if (activeChar == null)
            return;
        
        FastList<CursedWeaponInfo> list = new FastList<CursedWeaponInfo>();
        for (CursedWeapon cw : CursedWeaponsManager.getInstance().getCursedWeapons())
        {
            if (!cw.isActive()) continue;
            
            Location loc = cw.getCurrentLocation();
            
            if (loc != null)
                list.add(new CursedWeaponInfo(loc, cw.getItemId(), cw.isActivated() ? 1 : 0));
        }
        

        //send the ExCursedWeaponLocation
        if (!list.isEmpty())
        {
            activeChar.sendPacket(new ExCursedWeaponLocation(list));
        }
    }

    /**
     * @see com.l2jfree.gameserver.network.BasePacket#getType()
     */
    @Override
    public String getType()
    {
        return _C__D0_23_REQUESTCURSEDWEAPONLOCATION;
    }
}
