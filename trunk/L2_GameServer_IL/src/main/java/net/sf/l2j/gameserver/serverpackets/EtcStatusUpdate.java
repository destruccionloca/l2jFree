/* This program is free software; you can redistribute it and/or modify
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

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 *
 * @author  Luca Baldi
 */
public class EtcStatusUpdate extends L2GameServerPacket
{
	private static final String _S__F3_ETCSTATUSUPDATE = "[S] f3 EtcStatusUpdate";

	/**
	 *
	 * Packet for lvl 3 client buff line
	 *
	 * Example:(C4)
	 * F3 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 - empty statusbar
	 * F3 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 - increased force lvl 1
	 * F3 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 - weight penalty lvl 1
	 * F3 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 - chat banned
	 * F3 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00 - Danger Area lvl 1
	 * F3 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 - lvl 1 grade penalty
	 *
	 * packet format: cdd //and last three are ddd???
 	 *
	 * Some test results:
	 * F3 07 00 00 00 04 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 - lvl 7 increased force lvl 4 weight penalty
	 *
	 * Example:(C5 709)
	 * F3 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 00 00 00 0F 00 00 00 - lvl 1 charm of courage lvl 15 Death Penalty
	 *
	 *
	 * NOTE:
	 * End of buff:
	 * You must send empty packet
	 * F3 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
	 * to remove the statusbar or just empty 
	 */

	int _IcreasedForce = 0;		//4271, 7 lvl
	int _weightPenalty = 0;		//4270, 4 lvl
	int _messageRefusal = 0;	//4269, 1 lvl
	int _isInDangerArea = 0;	//4268, 1 lvl
	int _expertisePenalty = 0;	//4267, 1 lvl at off c4 server scripts
	int _charmOfCourage = 0;	//Charm of Courage, "Prevents experience value decreasing if killed during a siege war".
	int _deathPenalty = 0;		////Death Penalty max lvl 15, "Combat ability is decreased due to death."

	public EtcStatusUpdate(L2PcInstance _activeChar)
	{
		_IcreasedForce = 0;
		_weightPenalty = _activeChar.getWeightPenalty();
		_messageRefusal = _activeChar.getMessageRefusal() ? 1 : 0;
		_isInDangerArea = 0;
		_expertisePenalty = _activeChar.getexpertisePenalty() > 0 ? 1 : 0;
		_charmOfCourage = 0;
		_deathPenalty = 0;
	}

	/**
	 * @see net.sf.l2j.gameserver.serverpackets.L2GameServerPacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0xF3);				//several icons to a separate line (0 = disabled)
		writeD(_IcreasedForce);		//1-7 increase force, lvl
		writeD(_weightPenalty);		//1-4 weight penalty, lvl (1=50%, 2=66.6%, 3=80%, 4=100%)
		writeD(_messageRefusal);	//1 = block all chat
		writeD(_isInDangerArea);	//1 = danger area
		writeD(_expertisePenalty);	//1 = grade penalty
		writeD(_charmOfCourage);	//1 = charm of courage (no xp loss in siege..)
		writeD(_deathPenalty);		//1-15 death penalty, lvl (combat ability decreased due to death)
	}
	
	/**
	 * @see net.sf.l2j.gameserver.serverpackets.L2GameServerPacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__F3_ETCSTATUSUPDATE;
	}
}