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
import com.l2jfree.gameserver.model.L2Summon;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfree.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jfree.gameserver.model.entity.events.CTF;
import com.l2jfree.gameserver.model.entity.events.DM;
import com.l2jfree.gameserver.model.entity.events.TvT;
import com.l2jfree.gameserver.model.entity.events.VIP;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.MagicSkillUse;
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
			65,
			725,
			726,
			727,
			728,
			733,
			734,
			735,
			1060,
			1061,
			1073,
			1374,
			1375,
			1539,
			1540,
			5283,
			5591,
			5592,
			6035,
			6036,
			6652,
			6653,
			6654,
			6655,
			8193,
			8194,
			8195,
			8196,
			8197,
			8198,
			8199,
			8200,
			8201,
			8202,
			8600,
			8601,
			8602,
			8603,
			8604,
			8605,
			8606,
			8607,
			8608,
			8609,
			8610,
			8611,
			8612,
			8613,
			8614,
			10155,
			10157,
			// Attribute Potion
			9997,
			9998,
			9999,
			10000,
			10001,
			10002,
			// Elixir of life
			8622,
			8623,
			8624,
			8625,
			8626,
			8627,
			// Elixir of Strength
			8628,
			8629,
			8630,
			8631,
			8632,
			8633,
			// Elixir of cp 
			8634,
			8635,
			8636,
			8637,
			8638,
			8639,
			// Bottles of souls
			10409,
			10410,
			10411,
			10412,
			// Juices
			10260,
			10261,
			10262,
			10263,
			10264,
			10265,
			10266,
			10267,
			10268,
			10269,
			10270,
			// CT2 Herbs
			10655,
			10656,
			10657,
			// CT2.2 Herb
			13028,
			20034,
			// Caravaners Remedy
			9702,
			// Bless of Eva
			4679							};

	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		L2PcInstance activeChar; // use activeChar only for L2PcInstance checks where cannot be used PetInstance
		boolean res = false;
		if (playable instanceof L2PcInstance)
			activeChar = (L2PcInstance) playable;
		else if (playable instanceof L2PetInstance)
			activeChar = ((L2PetInstance) playable).getOwner();
		else
			return;

		if ((activeChar._inEventTvT && TvT._started && !Config.TVT_ALLOW_POTIONS) || (activeChar._inEventCTF && CTF._started && !Config.CTF_ALLOW_POTIONS)
				|| (activeChar._inEventDM && DM._started && !Config.DM_ALLOW_POTIONS) || (activeChar._inEventVIP && VIP._started && !Config.VIP_ALLOW_POTIONS))
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
			return;
		}

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
			res = usePotion(playable, 2003, 1); // configurable through xml
			break;
		// till handler implemented
		case 728: // mana_potion, xml: 2005
			if (!Config.ALT_MANA_POTIONS)
				return;
			res = usePotion(playable, 2005, 1);
			break;

		// Healing and speed potions
		case 65: // red_potion, xml: 2001
			if (!isUseable(playable, item, 2001))
				return;
			res = usePotion(playable, 2001, 1);
			break;
		case 725: // healing_drug, xml: 2002
			if (!isUseable(playable, L2EffectType.HEAL_OVER_TIME, item, 2002))
				return;
			res = usePotion(playable, 2002, 1);
			break;
		case 727: // _healing_potion, xml: 2032
			if (!isUseable(playable, L2EffectType.HEAL_OVER_TIME, item, 2032))
				return;
			res = usePotion(playable, 2032, 1);
			break;
		case 733: //_Endeavor potion, xml: 2010
			if (!isUseable(playable, item, 2010))
				return;
			res = usePotion(playable, 2010, 1);
			break;
		case 734: // quick_step_potion, xml: 2011
			if (!isUseable(playable, item, 2011))
				return;
			res = usePotion(playable, 2011, 1);
			break;
		case 735: // swift_attack_potion, xml: 2012
			if (!isUseable(playable, item, 2012))
				return;
			res = usePotion(playable, 2012, 1);
			break;
		case 1060: // lesser_healing_potion,
		case 1073: // beginner's potion, xml: 2031
			if (!isUseable(playable, L2EffectType.HEAL_OVER_TIME, item, 2031))
				return;
			res = usePotion(playable, 2031, 1);
			break;
		case 1061: // healing_potion, xml: 2032
			if (!isUseable(playable, L2EffectType.HEAL_OVER_TIME, item, 2032))
				return;
			res = usePotion(playable, 2032, 1);
			break;
		case 10157: // instant haste_potion, xml: 2398
			if (!isUseable(playable, item, 2398))
				return;
			res = usePotion(playable, 2398, 1);
			break;
		case 1374: // adv_quick_step_potion, xml: 2034
			if (!isUseable(playable, item, 2034))
				return;
			res = usePotion(playable, 2034, 1);
			break;
		case 1375: // adv_swift_attack_potion, xml: 2035
			if (!isUseable(playable, item, 2035))
				return;
			res = usePotion(playable, 2035, 1);
			break;
		case 1539: // greater_healing_potion, xml: 2037
			if (!isUseable(playable, L2EffectType.HEAL_OVER_TIME, item, 2037))
				return;
			res = usePotion(playable, 2037, 1);
			break;
		case 1540: // quick_healing_potion, xml: 2038
			if (!isUseable(playable, L2EffectType.HEAL_OVER_TIME, item, 2038))
				return;
			res = usePotion(playable, 2038, 1);
			break;
		case 5283: // Rice Cake, xml: 2136
			if (!isUseable(playable, L2EffectType.HEAL_OVER_TIME, item, 2136))
				return;
			playable.broadcastPacket(new MagicSkillUse(playable, playable, 2136, 1, 1, 0));
			res = usePotion(playable, 2136, 1);
			break;
		case 5591: // CP and Greater CP
		case 5592: // Potion
			// elixir of Mental Strength
			if (!isUseable(playable, L2EffectType.COMBAT_POINT_HEAL_OVER_TIME, item, 2166))
				return;
			res = usePotion(playable, 2166, (itemId == 5591) ? 1 : 2);
			break;
		case 6035:
		case 6036: // Magic Haste Potion, xml: 2169
			if (!isUseable(playable, item, 2169))
				return;
			res = usePotion(playable, 2169, (itemId == 6035) ? 1 : 2);
			break;
		case 10155: //Mental Potion XML:2396
			if (!isUseable(playable, item, 2396))
				return;
			res = usePotion(playable, 2396, 1);
			break;

		// ATTRIBUTE POTION
		case 9997: // Fire Resist Potion, xml: 2335
			if (!isUseable(playable, item, 2335))
				return;
			res = usePotion(playable, 2335, 1);
			break;
		case 9998: // Water Resist Potion, xml: 2336
			if (!isUseable(playable, item, 2336))
				return;
			res = usePotion(playable, 2336, 1);
			break;
		case 9999: // Earth Resist Potion, xml: 2338
			if (!isUseable(playable, item, 2338))
				return;
			res = usePotion(playable, 2338, 1);
			break;
		case 10000: // Wind Resist Potion, xml: 2337
			if (!isUseable(playable, item, 2337))
				return;
			res = usePotion(playable, 2337, 1);
			break;
		case 10001: // Dark Resist Potion, xml: 2340
			if (!isUseable(playable, item, 2340))
				return;
			res = usePotion(playable, 2340, 1);
			break;
		case 10002: // Divine Resist Potion, xml: 2339
			if (!isUseable(playable, item, 2339))
				return;
			res = usePotion(playable, 2339, 1);
			break;

		// ELIXIR 
		case 8622:
		case 8623:
		case 8624:
		case 8625:
		case 8626:
		case 8627:
		{
			if (!(playable instanceof L2PcInstance))
			{
				itemNotForPets(activeChar);
				return;
			}
			// Elixir of Life
			if (!isUseable(activeChar, item, 2287))
				return;
			byte expIndex = (byte) activeChar.getExpertiseIndex();
			res = usePotion(activeChar, 2287, (expIndex > 5 ? expIndex : expIndex + 1));
			break;
		}
		case 8628:
		case 8629:
		case 8630:
		case 8631:
		case 8632:
		case 8633:
		{
			if (!(playable instanceof L2PcInstance))
			{
				itemNotForPets(activeChar);
				return;
			}
			// Elixir of Strength
			if (!isUseable(activeChar, item, 2288))
				return;
			byte expIndex = (byte) activeChar.getExpertiseIndex();
			// Elixir of Strength
			res = usePotion(activeChar, 2288, (expIndex > 5 ? expIndex : expIndex + 1));
		}
		case 8634:
		case 8635:
		case 8636:
		case 8637:
		case 8638:
		case 8639:
		{
			if (!(playable instanceof L2PcInstance))
			{
				itemNotForPets(activeChar);
				return;
			}
			// Elixir of cp
			if (!isUseable(activeChar, item, 2289))
				return;
			byte expIndex = (byte) activeChar.getExpertiseIndex();
			// Elixir of cp
			res = usePotion(activeChar, 2289, (expIndex > 5 ? expIndex : expIndex + 1));
		}
			// Valakas Amulets
		case 6652: // Amulet Protection of Valakas
			res = usePotion(playable, 2231, 1);
			break;
		case 6653: // Amulet Flames of Valakas
			res = usePotion(playable, 2233, 1);
			break;
		case 6654: // Amulet Flames of Valakas
			res = usePotion(playable, 2233, 1);
			break;
		case 6655: // Amulet Slay Valakas
			res = usePotion(playable, 2232, 1);
			break;

		// Herbs
		case 8600: // Herb of Life
			res = usePotion(playable, 2278, 1);
			break;
		case 8601: // Greater Herb of Life
			res = usePotion(playable, 2278, 2);
			break;
		case 8602: // Superior Herb of Life
			res = usePotion(playable, 2278, 3);
			break;
		case 8603: // Herb of Mana
			res = usePotion(playable, 2279, 1);
			break;
		case 8604: // Greater Herb of Mane
			res = usePotion(playable, 2279, 2);
			break;
		case 8605: // Superior Herb of Mane
			res = usePotion(playable, 2279, 3);
			break;
		case 8606: // Herb of Strength
			res = usePotion(playable, 2280, 1);
			break;
		case 8607: // Herb of Magic
			res = usePotion(playable, 2281, 1);
			break;
		case 8608: // Herb of Atk. Spd.
			res = usePotion(playable, 2282, 1);
			break;
		case 8609: // Herb of Casting Spd.
			res = usePotion(playable, 2283, 1);
			break;
		case 8610: // Herb of Critical Attack
			res = usePotion(playable, 2284, 1);
			break;
		case 8611: // Herb of Speed
			res = usePotion(playable, 2285, 1);
			break;
		case 8612: // Herb of Warrior
			res = usePotion(playable, 2280, 1);// Herb of Strength
			res = usePotion(playable, 2282, 1);// Herb of Atk. Spd
			res = usePotion(playable, 2284, 1);// Herb of Critical Attack
			break;
		case 8613: // Herb of Mystic
			res = usePotion(playable, 2281, 1);// Herb of Magic
			res = usePotion(playable, 2283, 1);// Herb of Casting Spd.
			break;
		case 8614: // Herb of Warrior
			res = usePotion(playable, 2278, 3);// Superior Herb of Life
			res = usePotion(playable, 2279, 3);// Superior Herb of Mana
			break;
		case 10655:
			res = usePotion(playable, 2512, 1);
			break;
		case 10656:
			res = usePotion(playable, 2514, 1);
			break;
		case 10657:
			res = usePotion(playable, 2513, 1);
			break;
		case 13028:
			res = usePotion(playable, 2580, 1);
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
			res = usePotion(activeChar, 2274, 1);
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
			res = usePotion(activeChar, 2274, 2);
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
			res = usePotion(activeChar, 2274, 3);
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
			res = usePotion(activeChar, 2274, 4);
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
			res = usePotion(activeChar, 2274, 5);
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
			res = usePotion(activeChar, 2274, 6);
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
			res = usePotion(activeChar, 2274, 7);
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
			res = usePotion(activeChar, 2274, 8);
			break;
		case 8201: // Fisherman's Potion - Black
			if (!(playable instanceof L2PcInstance))
			{
				itemNotForPets(activeChar);
				return;
			}
			res = usePotion(activeChar, 2274, 9);
			break;
		case 8202: // Fishing Potion
			if (!(playable instanceof L2PcInstance))
			{
				itemNotForPets(activeChar);
				return;
			}
			res = usePotion(activeChar, 2275, 1);
			break;
		case 4679: // Bless of Eva
			if (!(playable instanceof L2PcInstance))
			{
				itemNotForPets(activeChar);
				return;
			}
			if (!isUseable(activeChar, item, 2076))
				return;
			res = usePotion(activeChar, 2076, 1);
			break;
		case 10409: // Empty Bottle of Souls
			if (!(playable instanceof L2PcInstance))
			{
				itemNotForPets(activeChar);
				return;
			}
			if (activeChar.isKamaelic()) //Kamael classes only
			{
				if (activeChar.getSouls() >= 6)
				{
					res = usePotion(activeChar, 2498, 1);
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.THERE_IS_NOT_ENOUGH_SOUL);
				}
			}
			else
			{
				playable.sendPacket(SystemMessageId.NOTHING_HAPPENED);
			}
			break;
		case 10410: // 5 Souls Bottle
			if (!(playable instanceof L2PcInstance))
			{
				itemNotForPets(activeChar);
				return;
			}
			if (activeChar.isKamaelic()) //Kamael classes only
			{
				res = usePotion(activeChar, 2499, 1);
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
			}
			break;
		case 10411: // 5 Souls Bottle Combat
			if (!(playable instanceof L2PcInstance))
			{
				itemNotForPets(activeChar);
				return;
			}
			if (activeChar.isKamaelic()) //Kamael classes only
			{
				res = usePotion(activeChar, 2499, 1);
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
			}
			break;
		case 10412: // 10 Souls Bottle
			if (!(playable instanceof L2PcInstance))
			{
				itemNotForPets(activeChar);
				return;
			}
			if (activeChar.isKamaelic()) //Kamael classes only
			{
				res = usePotion(activeChar, 2499, 2);
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
			}
			break;

		// Juices
		// added by Z0mbie!
		case 10260: // Haste Juice,xml:2429
			if (!isUseable(playable, item, 2429))
				return;
			res = usePotion(playable, 2429, 1);
			break;
		case 10261: // Accuracy Juice,xml:2430
			if (!isUseable(playable, item, 2430))
				return;
			res = usePotion(playable, 2430, 1);
			break;
		case 10262: // Critical Power Juice,xml:2431
			if (!isUseable(playable, item, 2431))
				return;
			res = usePotion(playable, 2431, 1);
			break;
		case 10263: // Critical Attack Juice,xml:2432
			if (!isUseable(playable, item, 2432))
				return;
			res = usePotion(playable, 2432, 1);
			break;
		case 10264: // Casting Speed Juice,xml:2433
			if (!isUseable(playable, item, 2433))
				return;
			res = usePotion(playable, 2433, 1);
			break;
		case 10265: // Evasion Juice,xml:2434
			if (!isUseable(playable, item, 2434))
				return;
			res = usePotion(playable, 2434, 1);
			break;
		case 10266: // Magic Power Juice,xml:2435
			if (!isUseable(playable, item, 2435))
				return;
			res = usePotion(playable, 2435, 1);
			break;
		case 10267: // Power Juice,xml:2436
			if (!isUseable(playable, item, 2436))
				return;
			res = usePotion(playable, 2436, 1);
			break;
		case 10268: // Speed Juice,xml:2437
			if (!isUseable(playable, item, 2437))
				return;
			res = usePotion(playable, 2437, 1);
			break;
		case 10269: // Defense Juice,xml:2438
			if (!isUseable(playable, item, 2438))
				return;
			res = usePotion(playable, 2438, 1);
			break;
		case 10270: // MP Consumption Juice,xml: 2439
			if (!isUseable(playable, item, 2439))
				return;
			res = usePotion(playable, 2439, 1);
			break;
		case 9702: // Caravaners Remedy
			if (!isUseable(playable, item, 2341))
				return;
			res = usePotion(playable, 2341, 1);
		default:
		}

		if (res)
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
	}

	private boolean isEffectReplaceable(L2PlayableInstance playable, L2EffectType effectType, L2ItemInstance item)
	{
		L2Effect[] effects = playable.getAllEffects();

		if (effects == null)
			return true;

		L2PcInstance activeChar =  ((playable instanceof L2PcInstance) ? ((L2PcInstance) playable) : ((L2Summon) playable).getOwner());

		for (L2Effect e : effects)
		{
			if (e.getEffectType() == effectType)
			{
				// One can reuse pots after 2/3 of their duration is over.
				// It would be faster to check if its > 10 but that would screw custom pot durations...
				if (e.getElapsedTaskTime() > (e.getTotalTaskTime() * 2 / 3))
					return true;
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE);
				sm.addItemName(item);
				activeChar.sendPacket(sm);
				return false;
			}
		}
		return true;
	}

	private boolean isUseable(L2PlayableInstance playable, L2ItemInstance item, int skillid)
	{
		L2PcInstance activeChar =  ((playable instanceof L2PcInstance) ? ((L2PcInstance) playable) : ((L2Summon) playable).getOwner());
		if (activeChar.isSkillDisabled(skillid))
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE);
			sm.addItemName(item);
			activeChar.sendPacket(sm);
			return false;
		}
		return true;
	}

	private boolean isUseable(L2PlayableInstance playable, L2EffectType effectType, L2ItemInstance item, int skillid)
	{
		return (isEffectReplaceable(playable, effectType, item) && isUseable(playable, item, skillid));
	}

	public boolean usePotion(L2PlayableInstance activeChar, int magicId, int level)
	{
		L2Skill skill = SkillTable.getInstance().getInfo(magicId, level);
		if (skill != null)
		{
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