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

import com.l2jfree.Config;
import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.handler.IItemHandler;
import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.L2Playable;
import com.l2jfree.gameserver.model.actor.L2Summon;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfree.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.templates.skills.L2EffectType;

/**
 * This class ...
 * 
 * @version $Revision: 1.2.4.4 $ $Date: 2005/03/27 15:30:07 $
 */

public class Potions implements IItemHandler
{
	// All the item IDs that this handler knows.
	private static final int[]	ITEM_IDS	=
											{
			725,
			726,
			727,
			1061,
			1060,
			1073,
			10410,
			10411,
			20393,
			20394,
			4416,
			7061,
			8515,
			8516,
			8517,
			8518,
			8519,
			8520,
			10143,
			10144,
			10145,
			10146,
			10147,
			10148							};

	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		L2PcInstance activeChar; // use activeChar only for L2PcInstance checks where cannot be used PetInstance
		boolean res = false;
		if (playable instanceof L2PcInstance)
			activeChar = (L2PcInstance) playable;
		else if (playable instanceof L2PetInstance)
			activeChar = ((L2PetInstance) playable).getOwner();
		else
			return;

		if (playable.isAllSkillsDisabled())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		int itemId = item.getItemId();

		switch (itemId)
		{
		case 726: // Custom Mana Drug, xml: 9007
			if (Config.ALT_MANA_POTIONS)
				usePotion(activeChar, 9007, 1);
			else
				playable.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
			break;
		case 728: // Custom Mana Potion, xml: 9008
			if (Config.ALT_MANA_POTIONS)
				usePotion(activeChar, 9008, 1);
			else
				playable.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
			break;
		case 727: // Healing_potion, xml: 2032
		case 1061:
			if (!isUseable(playable, L2EffectType.HEAL_OVER_TIME, item, 2032))
				return;
			res = usePotion(playable, 2032, 1);
			break;
		case 1060: // Lesser Healing Potion
		case 1073: // Beginner's Potion, xml: 2031
			if (!isUseable(playable, L2EffectType.HEAL_OVER_TIME, item, 2031))
				return;
			res = usePotion(playable, 2031, 1);
			break;
/*
Control of this needs to be moved back into potions.java so proper message support can be handled for specific events.
		case 10409: // Empty Bottle of Souls
			if (activeChar.getActiveClass() >= 123 && activeChar.getActiveClass() <= 136) //Kamael classes only
			{
				if (activeChar.getSouls() >= 6)
					res = usePotion(activeChar, 2498, 1);
				else
					playable.sendPacket(new SystemMessage(SystemMessageId.THERE_IS_NOT_ENOUGH_SOUL));
			}
			else
				playable.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
			break;
*/
		case 10410: // 5 Souls Bottle
		case 10411:
			if (activeChar.getActiveClass() >= 123 && activeChar.getActiveClass() <= 136) // Kamael classes only
				res = usePotion(activeChar, 2499, 1);
			else
				playable.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
			break;
		case 20393: // Sweet Fruit Cocktail  
			res = usePotion(playable, 22056, 1);  
			usePotion(playable, 22057, 1);  
			usePotion(playable, 22058, 1);  
			usePotion(playable, 22059, 1);  
			usePotion(playable, 22060, 1);  
			usePotion(playable, 22061, 1);  
			usePotion(playable, 22064, 1);  
			usePotion(playable, 22065, 1);  
			break;  
		case 20394: // Fresh Fruit Cocktail  
			res = usePotion(playable, 22062, 1);  
			usePotion(playable, 22063, 1);  
			usePotion(playable, 22065, 1);  
			usePotion(playable, 22066, 1);  
			usePotion(playable, 22067, 1);  
			usePotion(playable, 22068, 1);  
			usePotion(playable, 22069, 1);  
			usePotion(playable, 22070, 1);  
			break;  
		case 4416:
		case 7061:
			res = usePotion(playable, 2073, 1);
			break;
		case 8515:
		case 8516:
		case 8517:
		case 8518:
		case 8519:
		case 8520:
			res = usePotion(playable, 5041, 1);
			break;
		case 10143:  
			res = usePotion(playable, 2379, 1);  
			usePotion(playable, 2380, 1);  
			usePotion(playable, 2381, 1);  
			usePotion(playable, 2382, 1);  
			usePotion(playable, 2383, 1);  
			break;  
		case 10144:  
			res = usePotion(playable, 2379, 1);  
			usePotion(playable, 2380, 1);  
			usePotion(playable, 2381, 1);  
			usePotion(playable, 2384, 1);  
			usePotion(playable, 2385, 1);  
			break;  
		case 10145:  
			res = usePotion(playable, 2379, 1);  
			usePotion(playable, 2380, 1);  
			usePotion(playable, 2381, 1);  
			usePotion(playable, 2384, 1);  
			usePotion(playable, 2386, 1);  
			break;  
		case 10146:  
			res = usePotion(playable, 2379, 1);  
			usePotion(playable, 2387, 1);  
			usePotion(playable, 2381, 1);  
			usePotion(playable, 2388, 1);  
			usePotion(playable, 2383, 1);  
			break;  
		case 10147:  
			res = usePotion(playable, 2379, 1);  
			usePotion(playable, 2387, 1);  
			usePotion(playable, 2381, 1);  
			usePotion(playable, 2383, 1);  
			usePotion(playable, 2389, 1);  
			break;  
		case 10148:  
			res = usePotion(playable, 2390, 1);  
			usePotion(playable, 2391, 1);  
			break;  	
			
		default:
		}

		if (res)
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
	}

	private boolean isEffectReplaceable(L2Playable playable, L2EffectType effectType, L2ItemInstance item)
	{
		for (L2Effect e : playable.getAllEffects())
		{
			if (e.getEffectType() == effectType)
			{
				// One can reuse pots after 2/3 of their duration is over.
				// It would be faster to check if its > 10 but that would screw custom pot durations...
				if (e.getElapsedTaskTime() > (e.getTotalTaskTime() * 2 / 3))
					continue;
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE);
				sm.addItemName(item);
				playable.getActingPlayer().sendPacket(sm);
				return false;
			}
		}
		
		return true;
	}

	private boolean isUseable(L2Playable playable, L2ItemInstance item, int skillid)
	{
		L2PcInstance activeChar = ((playable instanceof L2PcInstance) ? ((L2PcInstance) playable) : ((L2Summon) playable).getOwner());
		if (activeChar.isSkillDisabled(skillid))
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE);
			sm.addItemName(item);
			activeChar.sendPacket(sm);
			return false;
		}
		return true;
	}

	private boolean isUseable(L2Playable playable, L2EffectType effectType, L2ItemInstance item, int skillid)
	{
		return (isEffectReplaceable(playable, effectType, item) && isUseable(playable, item, skillid));
	}
	
	public boolean usePotion(L2Playable activeChar, int magicId, int level)
	{
		L2Skill skill = SkillTable.getInstance().getInfo(magicId, level);
		if (skill != null)
		{
			if (!skill.checkCondition(activeChar, activeChar))
				return false;

			// Return false if potion is in reuse
			// so is not destroyed from inventory
			if (activeChar.isSkillDisabled(skill.getId()))
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE);
				sm.addSkillName(skill);
				activeChar.sendPacket(sm);
				return false;
			}

			activeChar.doSimultaneousCast(skill);
			if (activeChar instanceof L2PcInstance)
			{
				L2PcInstance player = (L2PcInstance) activeChar;
				// Only for Heal potions
				if (magicId == 2031 || magicId == 2032 || magicId == 2037)
				{
					player.shortBuffStatusUpdate(magicId, level, 15);
				}
				// Summons should be affected by herbs too, self time effect is handled at L2Effect constructor
				else if (((magicId > 2277 && magicId < 2286) || (magicId >= 2512 && magicId <= 2514)) && (player.getPet() instanceof L2SummonInstance))
				{
					player.getPet().doSimultaneousCast(skill);
				}

				if (!(player.isSitting() && !skill.isPotion()))
					return true;
			}
			else if (activeChar instanceof L2PetInstance)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.PET_USES_S1);
				sm.addString(skill.getName());
				((L2PetInstance) (activeChar)).getOwner().sendPacket(sm);
				return true;
			}
		}
		return false;
	}

	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}