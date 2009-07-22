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
import com.l2jfree.gameserver.model.zone.L2Zone;

/**
 *
 * @author  Luca Baldi
 */
public class EtcStatusUpdate extends L2GameServerPacket
{
	private static final String _S__F9_ETCSTATUSUPDATE = "[S] f9 EtcStatusUpdate [dddddddd]";

	private final L2PcInstance _activeChar;

	public EtcStatusUpdate(L2PcInstance activeChar)
	{
		 _activeChar = activeChar;
	}

	/**
	 * @see com.l2jfree.gameserver.network.serverpackets.L2GameServerPacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0xF9);											//several icons to a separate line (0 = disabled)
		writeD(_activeChar.getCharges());
		writeD(_activeChar.getWeightPenalty());
		writeD(_activeChar.getMessageRefusal() ? 1 : 0);
		writeD(_activeChar.isInsideZone(L2Zone.FLAG_DANGER) ? 1 : 0);
		writeD(_activeChar.getExpertisePenalty());
		writeD(_activeChar.getCharmOfCourage() ? 1 : 0); // 1 = charm of courage (allows resurrection on the same spot upon death on the siege battlefield)
		writeD(_activeChar.getDeathPenaltyBuffLevel());
		writeD(_activeChar.getSouls());
	}

	/**
	 * @see com.l2jfree.gameserver.network.serverpackets.L2GameServerPacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__F9_ETCSTATUSUPDATE;
	}
}