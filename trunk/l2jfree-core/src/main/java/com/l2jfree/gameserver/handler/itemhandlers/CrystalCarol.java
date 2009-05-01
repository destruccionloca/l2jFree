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
package com.l2jfree.gameserver.handler.itemhandlers;

import com.l2jfree.gameserver.handler.IItemHandler;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.actor.L2Playable;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.util.FloodProtector;
import com.l2jfree.gameserver.util.FloodProtector.Protected;

/**
 * This class ...
 * 
 * @version $Revision: 1.2.4.4 $ $Date: 2005/03/27 15:30:07 $
 * @author Skatershi (Optimisations)
 */
public class CrystalCarol implements IItemHandler
{
	// All the item IDs that this handler knows.
	private static final int[]	ITEM_IDS	=
											{
			5562,
			5563,
			5564,
			5565,
			5566,
			5583,
			5584,
			5585,
			5586,
			5587,
			4411,
			4412,
			4413,
			4414,
			4415,
			4416,
			4417,
			5010,
			6903,
			7061,
			7062,
			8555							};

	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;

		L2PcInstance activeChar = (L2PcInstance) playable;
		int itemId = item.getItemId();
		int skillId = -1;

		if (!FloodProtector.tryPerformAction(activeChar, Protected.FIREWORK))
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addItemName(item);
			activeChar.sendPacket(sm);
			return;
		}

		switch (itemId)
		{
		case 5562: // Crystal Carol 1
			skillId = 2140; // sndRef = SkillSound2.crystal_carol_01
			break;
		case 5563: // Crystal Carol 2
			skillId = 2141; // sndRef = SkillSound2.crystal_carol_02
			break;
		case 5564: // Crystal Carol 3
			skillId = 2142; // sndRef = SkillSound2.crystal_carol_03
			break;
		case 5565: // Crystal Carol 4
			skillId = 2143; // sndRef = SkillSound2.crystal_carol_04
			break;
		case 5566: // Crystal Carol 5
			skillId = 2144; // sndRef = SkillSound2.crystal_carol_05
			break;
		case 5583: // Crystal Carol 6
			skillId = 2145; // sndRef = SkillSound2.crystal_carol_06
			break;
		case 5584: // Crystal Carol 7
			skillId = 2146; // sndRef = SkillSound2.crystal_carol_07
			break;
		case 5585: // Crystal Carol 8
			skillId = 2147; // sndRef = SkillSound2.crystal_carol_08
			break;
		case 5586: // Crystal Carol 9
			skillId = 2148; // sndRef = SkillSound2.crystal_carol_09
			break;
		case 5587: // Crystal Carol 10
			skillId = 2149; // sndRef = SkillSound2.crystal_carol_10
			break;
		case 4411: // Crystal Journey
			skillId = 2069; // sndRef = SkillSound2.crystal_journey
			break;
		case 4412: // Crystal Battle
			skillId = 2068; // sndRef = SkillSound2.crystal_battle
			break;
		case 4413: // Crystal Love
			skillId = 2070; // sndRef = SkillSound2.crystal_love
			break;
		case 4414: // Crystal Solitude
			skillId = 2072; // sndRef = SkillSound2.crystal_solitude
			break;
		case 4415: // Crystal Festival
			skillId = 2071; // sndRef = SkillSound2.crystal_festival
			break;
		case 4416: // Crystal Celebration
			skillId = 2073; // sndRef = SkillSound2.crystal_celebration
			break;
		case 4417: // Crystal Comedy
			skillId = 2067; // sndRef = SkillSound2.crystal_comedy
			break;
		case 5010: // Crystal Victory
			skillId = 2066; // sndRef = SkillSound2.crystal_victory
			break;
		case 6903: // Crystal Music Box M
			skillId = 2187; // sndRef = EtcSound.battle
			break;
		case 7061: // Crystal Birthday
			skillId = 2073; // sndRef = SkillSound2.crystal_celebration
			break;
		case 7062: // Crystal Wedding
			skillId = 2230; // sndRef = SkillSound5.wedding
			break;
		case 8555: // Crystal Viva Victory Korea
			skillId = 2272; // sndRef = EtcSound.VVKorea
			break;
		}

		MagicSkillUse MSU = new MagicSkillUse(playable, activeChar, skillId, 1, 1, 0);

		activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
		activeChar.broadcastPacket(MSU);
	}

	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}