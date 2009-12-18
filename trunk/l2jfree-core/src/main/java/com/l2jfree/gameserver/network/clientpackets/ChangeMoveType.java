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
import com.l2jfree.gameserver.util.Util;

/**
 * This packet is never sent since by the official client since Gracia P2.
 * @see RequestActionUse#RequestActionUse()
 */
public class ChangeMoveType extends L2GameClientPacket
{
	private static final String _C__35_CHANGEMOVETYPE = "[C] 35 ChangeMoveType";

	//private boolean _typeRun;

    @Override
    protected void readImpl()
    {
        /*_typeRun = (*/readD();// == 1);
    }

    @Override
    protected void runImpl()
	{
    	L2PcInstance player = getActiveChar();
		if (player != null)
			Util.handleIllegalPlayerAction(player, "Sent a deprecated packet!");
	}

	@Override
	public String getType()
	{
		return _C__35_CHANGEMOVETYPE;
	}
}
