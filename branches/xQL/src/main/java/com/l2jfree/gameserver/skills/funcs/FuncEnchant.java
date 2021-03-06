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
package com.l2jfree.gameserver.skills.funcs;

import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.skills.Env;
import com.l2jfree.gameserver.skills.Stats;
import com.l2jfree.gameserver.templates.L2Item;
import com.l2jfree.gameserver.templates.L2WeaponType;

public class FuncEnchant extends Func
{
	public FuncEnchant(Stats pStat, int pOrder, Object owner, @SuppressWarnings("unused")
	Lambda lambda)
	{
		super(pStat, pOrder, owner);
	}

	public void calc(Env env)
	{
		if (cond != null && !cond.test(env))
			return;

		L2ItemInstance item = (L2ItemInstance) funcOwner;

		if (item.getItem().getCrystalType() == L2Item.CRYSTAL_NONE)
			return;

		int enchant = item.getEnchantLevel();
		int overenchant = 0;

		if (enchant > 3)
		{
			overenchant = enchant - 3;
			enchant = 3;
		}

		if (stat == Stats.MAGIC_DEFENCE || stat == Stats.POWER_DEFENCE || stat == Stats.SHIELD_DEFENCE)
		{
			env.value += enchant + 3 * overenchant;
			return;
		}

		if (stat == Stats.MAGIC_ATTACK)
		{
			switch (item.getItem().getCrystalType())
			{
			case L2Item.CRYSTAL_S:
			case L2Item.CRYSTAL_S80:
				env.value += 4 * enchant + 8 * overenchant;
				break;
			case L2Item.CRYSTAL_A:
				env.value += 3 * enchant + 6 * overenchant;
				break;
			case L2Item.CRYSTAL_B:
				env.value += 3 * enchant + 6 * overenchant;
				break;
			case L2Item.CRYSTAL_C:
				env.value += 3 * enchant + 6 * overenchant;
				break;
			case L2Item.CRYSTAL_D:
				env.value += 2 * enchant + 4 * overenchant;
				break;
			}
			return;
		}

		if (item.getItemType() instanceof L2WeaponType)
		{
			L2WeaponType type = (L2WeaponType) item.getItemType();

			switch (item.getItem().getCrystalType())
			{
			case L2Item.CRYSTAL_A:
				switch (type)
				{
				case BOW:
				case CROSSBOW:
					env.value += 8 * enchant + 16 * overenchant;
					break;
				case DUALFIST:
				case DUAL:
				case SWORD:
				case RAPIER:
					if (item.getItem().getBodyPart() == L2Item.SLOT_LR_HAND)
					{
						env.value += 5 * enchant + 10 * overenchant;
						break;
					}
				default:
					env.value += 4 * enchant + 8 * overenchant;
					break;
				}
				break;
			case L2Item.CRYSTAL_B:
				switch (type)
				{
				case BOW:
				case CROSSBOW:
					env.value += 6 * enchant + 12 * overenchant;
					break;
				case DUALFIST:
				case DUAL:
				case SWORD:
				case RAPIER:
					if (item.getItem().getBodyPart() == L2Item.SLOT_LR_HAND)
					{
						env.value += 4 * enchant + 8 * overenchant;
						break;
					}
				default:
					env.value += 3 * enchant + 6 * overenchant;
					break;
				}
				break;
			case L2Item.CRYSTAL_C:
				switch (type)
				{
				case BOW:
				case CROSSBOW:
					env.value += 6 * enchant + 12 * overenchant;
					break;
				case DUALFIST:
				case DUAL:
				case SWORD:
				case RAPIER:
					if (item.getItem().getBodyPart() == L2Item.SLOT_LR_HAND)
					{
						env.value += 4 * enchant + 8 * overenchant;
						break;
					}
				default:
					env.value += 3 * enchant + 6 * overenchant;
					break;
				}
				break;
			case L2Item.CRYSTAL_D:
				switch (type)
				{
				case BOW:
				case CROSSBOW:
				{
					env.value += 4 * enchant + 8 * overenchant;
					break;
				}
					//case DUALFIST:
					//case DUAL:
					//case SWORD:
					//case RAPIER:
					//    if (item.getItem().getBodyPart() == L2Item.SLOT_LR_HAND)
					//    {
					//        break;
					//    }
				default:
					env.value += 2 * enchant + 4 * overenchant;
					break;
				}
				break;
			case L2Item.CRYSTAL_S:
			case L2Item.CRYSTAL_S80:
				switch (type)
				{
				case BOW:
				case CROSSBOW:
					env.value += 10 * enchant + 20 * overenchant;
					break;
				case DUALFIST:
				case DUAL:
				case SWORD:
				case RAPIER:
					if (item.getItem().getBodyPart() == L2Item.SLOT_LR_HAND)
					{
						env.value += 4 * enchant + 12 * overenchant;
						break;
					}
				default:
					env.value += 4 * enchant + 10 * overenchant;
					break;
				}
				break;
			}
		}
		return;
	}
}
