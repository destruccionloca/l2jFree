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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.gameserver.network.serverpackets.ActionFailed;

/**
 * Format: (ch) dd
 * @author -Wooden-
 */
public class RequestDismissPartyRoom extends L2GameClientPacket
{
	private static final String _C__D0_02_REQUESTDISMISSPARTYROOM = "[C] D0:02 RequestDismissPartyRoom";
	private static final Log _log = LogFactory.getLog(RequestDismissPartyRoom.class.getName());

	private int _data1;
	private int _data2;

    @Override
    protected void readImpl()
    {
		_data1 = readD();
		_data2 = readD();
	}

	@Override
    protected void runImpl()
	{
		//TODO: implement
		_log.info("This packet is not well known : RequestDismissPartyRoom");
		_log.info("Data received: d:"+_data1+" d:"+_data2);
		sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public String getType()
	{
		return _C__D0_02_REQUESTDISMISSPARTYROOM;
	}
}
