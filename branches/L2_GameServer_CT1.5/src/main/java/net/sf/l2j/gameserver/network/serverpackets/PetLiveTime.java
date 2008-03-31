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

import net.sf.l2j.gameserver.model.L2Summon;

public class PetLiveTime extends L2GameServerPacket
{

    private static final String _S__D1_PETLIVETIME = "[S] D1 PetLiveTime";
    private final int _maxFed;
    private final int _curFed;
    
    public PetLiveTime(L2Summon summon)
    {
        _curFed = summon.getCurrentFed();
        _maxFed = summon.getMaxFed();
    }

    @Override
    protected void writeImpl()
    {
        writeC(0xD1);
        writeD(_maxFed);
        writeD(_curFed);
    }

    @Override
    public String getType()
    {
        return _S__D1_PETLIVETIME;
    }
}
