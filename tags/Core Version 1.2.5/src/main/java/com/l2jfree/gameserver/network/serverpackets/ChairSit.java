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

import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

public class ChairSit extends L2GameServerPacket
{
    private static final String _S__ED_CHAIRSIT = "[S] ed ChairSit [dd]";
    
    private L2PcInstance _activeChar;
    private int _staticObjectId;

    public ChairSit(L2PcInstance player, int staticObjectId)
    {
        _activeChar = player;
        _staticObjectId = staticObjectId;
    }
    
    @Override
    protected final void writeImpl()
    {
        writeC(0xed);
        writeD(_activeChar.getObjectId());
        writeD(_staticObjectId);
    }

    /* (non-Javadoc)
     * @see com.l2jfree.gameserver.serverpackets.ServerBasePacket#getType()
     */
    @Override
    public String getType()
    {
        return _S__ED_CHAIRSIT;
    }
}
