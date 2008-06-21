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

import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.serverpackets.ExPrivateStoreSetWholeMsg;

/**
 *
 * @author  KenM
 */
public class SetPrivateStoreWholeMsg extends L2GameClientPacket
{
    private String _msg;
    
    /**
     * @see com.l2jfree.gameserver.clientpackets.L2GameClientPacket#getType()
     */
    @Override
    public String getType()
    {
        return "[C] D0:4D SetPrivateStoreWholeMsg";
    }

    /**
     * @see com.l2jfree.gameserver.clientpackets.L2GameClientPacket#readImpl()
     */
    @Override
    protected void readImpl()
    {
        _msg = readS();
    }

    /**
     * @see com.l2jfree.gameserver.clientpackets.L2GameClientPacket#runImpl()
     */
    @Override
    protected void runImpl()
    {
        L2PcInstance player = getClient().getActiveChar();
        if (player == null || player.getSellList() == null) return;

        player.getSellList().setTitle(_msg);
        sendPacket(new ExPrivateStoreSetWholeMsg(player));
    }
}
