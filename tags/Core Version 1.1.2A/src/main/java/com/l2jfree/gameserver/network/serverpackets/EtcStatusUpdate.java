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

import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.zone.L2Zone;
import com.l2jfree.gameserver.skills.effects.EffectCharge;

/**
 *
 * @author  Luca Baldi
 */
public class EtcStatusUpdate extends L2GameServerPacket
{
	private static final String _S__F9_ETCSTATUSUPDATE = "[S] f9 EtcStatusUpdate [dddddddd]";

	private L2PcInstance _activeChar;
	private EffectCharge _effect;

	public EtcStatusUpdate(L2PcInstance activeChar)
	{
		 _activeChar = activeChar;
		 _effect = (EffectCharge)_activeChar.getFirstEffect(L2Effect.EffectType.CHARGE);
	}

	/**
	 * @see com.l2jfree.gameserver.network.serverpackets.L2GameServerPacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0xF9);											//several icons to a separate line (0 = disabled)
		if (_effect != null)
			writeD(_effect.getLevel()); 						// 1-7 increase force, lvl
		else
			writeD(0x00); 										// 1-7 increase force, lvl
        writeD(_activeChar.getWeightPenalty());
        writeD(_activeChar.getMessageRefusal() ? 1 : 0);
        writeD(_activeChar.isInsideZone(L2Zone.FLAG_DANGER) ? 1 : 0);
        writeD(_activeChar.getExpertisePenalty());
        writeD(_activeChar.getCharmOfCourage() ? 1 : 0);
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