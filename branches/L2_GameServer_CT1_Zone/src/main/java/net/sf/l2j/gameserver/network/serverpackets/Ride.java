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

import net.sf.l2j.gameserver.datatables.PetDataTable;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class Ride extends L2GameServerPacket
{
    private static final String _S__8c_Ride = "[S] 8C Ride";
    public static final int ACTION_MOUNT = 1;
    public static final int ACTION_DISMOUNT = 0;
    private int _id;
    private int _bRide;
    private int _rideType;
    private int _rideClassID;

    /**
     * 0x86 UnknownPackets         dddd 
     * @param _
     */

    public Ride(int id, int action, int npcId)
    {
        _id = id; // charobjectID
        _bRide = action; // 1 for mount ; 2 for dismount
        _rideClassID = npcId + 1000000; // npcID

        // 1 for Strider ; 2 for wyvern
        if (PetDataTable.isStrider(npcId))
            _rideType = 1;
        else if (PetDataTable.isWyvern(npcId))
            _rideType = 2;
        else if (PetDataTable.isGreatWolf(npcId))
            _rideType = 3;
    }

    public int getMountType()
    {
        return _rideType;
    }

    @Override
    public void runImpl()
    {
        L2PcInstance cha = getClient().getActiveChar();
        if (cha == null) return;
        // Don't allow ride with Zariche equiped
        if (cha.isCursedWeaponEquiped()) return;
    }

    @Override
    protected final void writeImpl()
    {
        L2PcInstance cha = getClient().getActiveChar();
        if (cha == null) return;        
        writeC(0x8c);
        writeD(_id);
        writeD(_bRide);
        writeD(_rideType);
        writeD(_rideClassID);
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
     */
    @Override
    public String getType()
    {
        return _S__8c_Ride;
    }
}
