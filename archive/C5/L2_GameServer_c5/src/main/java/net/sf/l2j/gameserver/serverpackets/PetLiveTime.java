/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.serverpackets;

import net.sf.l2j.gameserver.model.L2Summon;

public class PetLiveTime extends ServerBasePacket
{

    private static final String _S__D1_PETLIVETIME = "[S] D1 PetLiveTime";
    private final int _maxFed;
    private final int _curFed;
    
    public PetLiveTime(L2Summon summon)
    {
        _curFed = summon.getCurrentFed();
        _maxFed = summon.getMaxFed();
    }

    void runImpl()
    {
    }

    void writeImpl()
    {
        writeC(0xD1);
        writeD(_maxFed);
        writeD(_curFed);
    }

    public String getType()
    {
        return _S__D1_PETLIVETIME;
    }
}
