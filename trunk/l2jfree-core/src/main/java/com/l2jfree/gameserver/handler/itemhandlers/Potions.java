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
import com.l2jfree.gameserver.model.zone.L2Zone;
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
			728,
			1060,
			1061,
			1073,
			8193,
			8194,
			8195,
			8196,
			8197,
			8198,
			8199,
			8200,
			8201,
			10155,
			13032,
			10157,
			13032,
			4416,
			7061,
			// Bottles of souls
			10410,
			10411,
			10412,
			// CT2.2 Herb
			13028,
			20034,
			// Caravaners Remedy
			9702,
			// Bless of Eva
			4679,
			20393,
			20394							};

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
		// Mana potions
		case 726: // mana drug, xml: 2003
			if (!Config.ALT_MANA_POTIONS)
				return;
			usePotion(playable, 2003, 1); // configurable through xml
			break;
		// till handler implemented
		case 728: // mana_potion, xml: 2005
			if (!Config.ALT_MANA_POTIONS)
				return;
			usePotion(playable, 2005, 1);
			break;

		// Healing and speed potions
		case 727: // _healing_potion, xml: 2032
			if (!isUseable(playable, L2EffectType.HEAL_OVER_TIME, item, 2032))
				return;
			usePotion(playable, 2032, 1);
			break;
		case 1060: // lesser_healing_potion,
		case 1061: //
		case 1073: // beginner's potion, xml: 2031
			if (!isUseable(playable, L2EffectType.HEAL_OVER_TIME, item, 2031))
				return;
			res = usePotion(playable, 2031, 1);
			break;

		// Valakas Amulets
		case 6652: // Amulet Protection of Valakas
			usePotion(playable, 2231, 1);
			break;
		case 6653: // Amulet Flames of Valakas
			usePotion(playable, 2233, 1);
			break;
		case 6654: // Amulet Flames of Valakas
			usePotion(playable, 2233, 1);
			break;
		case 6655: // Amulet Slay Valakas
			usePotion(playable, 2232, 1);
			break;
		case 13028:
			usePotion(playable, 2580, 1);
			break;
		case 20034: //Revita-Pop
			activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
			activeChar.setVitalityPoints(20000.0, true);
			activeChar.updateVitalityLevel(false);
			activeChar.sendMessage("Your Vitality Level is set to 4");
			break;
		case 8193: // Fisherman's Potion - Green
			if (!(playable instanceof L2PcInstance))
			{
				itemNotForPets(activeChar);
				return;
			}
			if (activeChar.getSkillLevel(1315) <= 3)
			{
				activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
				activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
				return;
			}
			usePotion(activeChar, 2274, 1);
			break;
		case 8194: // Fisherman's Potion - Jade
			if (!(playable instanceof L2PcInstance))
			{
				itemNotForPets(activeChar);
				return;
			}
			if (activeChar.getSkillLevel(1315) <= 6)
			{
				activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
				activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
				return;
			}
			usePotion(activeChar, 2274, 2);
			break;
		case 8195: // Fisherman's Potion - Blue
			if (!(playable instanceof L2PcInstance))
			{
				itemNotForPets(activeChar);
				return;
			}
			if (activeChar.getSkillLevel(1315) <= 9)
			{
				activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
				activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
				return;
			}
			usePotion(activeChar, 2274, 3);
			break;
		case 8196: // Fisherman's Potion - Yellow
			if (!(playable instanceof L2PcInstance))
			{
				itemNotForPets(activeChar);
				return;
			}
			if (activeChar.getSkillLevel(1315) <= 12)
			{
				activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
				activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
				return;
			}
			usePotion(activeChar, 2274, 4);
			break;
		case 8197: // Fisherman's Potion - Orange
			if (!(playable instanceof L2PcInstance))
			{
				itemNotForPets(activeChar);
				return;
			}
			if (activeChar.getSkillLevel(1315) <= 15)
			{
				activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
				activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
				return;
			}
			usePotion(activeChar, 2274, 5);
			break;
		case 8198: // Fisherman's Potion - Purple
			if (!(playable instanceof L2PcInstance))
			{
				itemNotForPets(activeChar);
				return;
			}
			if (activeChar.getSkillLevel(1315) <= 18)
			{
				activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
				activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
				return;
			}
			usePotion(activeChar, 2274, 6);
			break;
		case 8199: // Fisherman's Potion - Red
			if (!(playable instanceof L2PcInstance))
			{
				itemNotForPets(activeChar);
				return;
			}
			if (activeChar.getSkillLevel(1315) <= 21)
			{
				activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
				activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
				return;
			}
			usePotion(activeChar, 2274, 7);
			break;
		case 8200: // Fisherman's Potion - White
			if (!(playable instanceof L2PcInstance))
			{
				itemNotForPets(activeChar);
				return;
			}
			if (activeChar.getSkillLevel(1315) <= 24)
			{
				activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
				activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
				return;
			}
			usePotion(activeChar, 2274, 8);
			break;
		case 8201: // Fisherman's Potion - Black
			if (!(playable instanceof L2PcInstance))
			{
				itemNotForPets(activeChar);
				return;
			}
			usePotion(activeChar, 2274, 9);
			break;
		case 4679: // Bless of Eva
			if (!(playable instanceof L2PcInstance))
			{
				itemNotForPets(activeChar);
				return;
			}
			if (!isUseable(activeChar, item, 2076))
				return;
			usePotion(activeChar, 2076, 1);
			break;
		case 13032: // Pailaka Instant Shield XML:2577
			usePotion(playable, 2577, 1);
			break;
		case 10409: // Empty Bottle of Souls
			if (!(playable instanceof L2PcInstance))
			{
				itemNotForPets(activeChar);
				return;
			}
			usePotion(activeChar, 2498, 1);
			break;
		case 10410: // 5 Souls Bottle
			if (!(playable instanceof L2PcInstance))
			{
				itemNotForPets(activeChar);
				return;
			}
			if (activeChar.isKamaelic())
				res = usePotion(activeChar, 2499, 1);
			else
				activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
			break;
		case 10411: // 5 Souls Bottle Combat
			if (!(playable instanceof L2PcInstance))
			{
				itemNotForPets(activeChar);
				return;
			}
			if (activeChar.isKamaelic() && activeChar.isInsideZone(L2Zone.FLAG_SIEGE))
				res = usePotion(activeChar, 2499, 1);
			else
				activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
			break;
		case 10412: // 10 Souls Bottle
			if (!(playable instanceof L2PcInstance))
			{
				itemNotForPets(activeChar);
				return;
			}
			if (activeChar.isKamaelic())
				res = usePotion(activeChar, 2499, 2);
			else
				activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
			break;
		case 9702: // Caravaners Remedy
			if (!isUseable(playable, item, 2341))
				return;
			usePotion(playable, 2341, 1);
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

	private void itemNotForPets(L2PcInstance activeChar)
	{
		activeChar.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
	}

	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}