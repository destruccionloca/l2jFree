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
import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jfree.gameserver.templates.skills.L2EffectType;
import com.l2jfree.gameserver.templates.skills.L2SkillType;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.2.4 $ $Date: 2005/04/06 16:13:51 $
 */

public class Remedy implements IItemHandler
{
	// All the item IDs that this handler knows.
	private static final int[]	ITEM_IDS	=
											{ 1831, 1832, 1833, 1834, 3889 };

	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		L2PcInstance activeChar;
		if (playable instanceof L2PcInstance)
			activeChar = (L2PcInstance) playable;
		else if (playable instanceof L2PetInstance)
			activeChar = ((L2PetInstance) playable).getOwner();
		else
			return;

		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
			return;
		}

		int itemId = item.getItemId();
		if (itemId == 1831) // Antidote
		{
			L2Effect[] effects = activeChar.getAllEffects();
			for (L2Effect e : effects)
			{
				if (e.getSkill().getSkillType() == L2SkillType.POISON && e.getSkill().getLevel() <= 3)
				{
					e.exit();
					break;
				}
			}
			MagicSkillUse MSU = new MagicSkillUse(playable, playable, 2042, 1, 0, 0);
			activeChar.sendPacket(MSU);
			activeChar.broadcastPacket(MSU);
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
		else if (itemId == 1832) // Advanced Antidote
		{
			L2Effect[] effects = activeChar.getAllEffects();
			for (L2Effect e : effects)
			{
				if (e.getSkill().getSkillType() == L2SkillType.POISON && e.getSkill().getLevel() <= 7)
				{
					e.exit();
					break;
				}
			}
			MagicSkillUse MSU = new MagicSkillUse(playable, playable, 2043, 1, 0, 0);
			activeChar.sendPacket(MSU);
			activeChar.broadcastPacket(MSU);
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
		else if (itemId == 1833) // Bandage
		{
			L2Effect[] effects = activeChar.getAllEffects();
			for (L2Effect e : effects)
			{
				if (e.getSkill().getSkillType() == L2SkillType.BLEED && e.getSkill().getLevel() <= 3)
				{
					e.exit();
					break;
				}
			}
			MagicSkillUse MSU = new MagicSkillUse(playable, playable, 34, 1, 0, 0);
			activeChar.sendPacket(MSU);
			activeChar.broadcastPacket(MSU);
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
		else if (itemId == 1834) // Emergency Dressing
		{
			L2Effect[] effects = activeChar.getAllEffects();
			for (L2Effect e : effects)
			{
				if (e.getSkill().getSkillType() == L2SkillType.BLEED && e.getSkill().getLevel() <= 7)
				{
					e.exit();
					break;
				}
			}
			MagicSkillUse MSU = new MagicSkillUse(playable, playable, 2045, 1, 0, 0);
			activeChar.sendPacket(MSU);
			activeChar.broadcastPacket(MSU);
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
		else if (itemId == 3889) // Potion of Recovery
		{
			L2Effect[] effects = activeChar.getAllEffects();
			for (L2Effect e : effects)
			{
				if (e.getSkill().getId() == 4082)
					e.exit();
			}
			activeChar.setIsImmobilized(false);
			if (activeChar.getFirstEffect(L2EffectType.ROOT) == null)
				activeChar.stopRooting(true);
			MagicSkillUse MSU = new MagicSkillUse(playable, playable, 2042, 1, 0, 0);
			activeChar.sendPacket(MSU);
			activeChar.broadcastPacket(MSU);
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
	}

	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}