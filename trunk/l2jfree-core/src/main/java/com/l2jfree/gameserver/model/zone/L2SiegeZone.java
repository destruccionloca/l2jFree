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
package com.l2jfree.gameserver.model.zone;

import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.instancemanager.FortManager;
import com.l2jfree.gameserver.instancemanager.FortSiegeManager;
import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2SiegeClan;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2SiegeSummonInstance;
import com.l2jfree.gameserver.model.entity.Fort;
import com.l2jfree.gameserver.network.SystemMessageId;

public class L2SiegeZone extends SiegeableEntityZone
{
	@Override
	protected void register() throws Exception
	{
		_entity = initSiegeableEntity();
		// Init siege task
		_entity.getSiege();
		_entity.registerSiegeZone(this);
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		character.setInsideZone(FLAG_PVP, true);
		character.setInsideZone(FLAG_SIEGE, true);
		character.setInsideZone(FLAG_NOSUMMON, true);
		
		character.sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);
		
		if (character instanceof L2PcInstance)
		{
			L2PcInstance pc = (L2PcInstance) character;
			if (pc.getClan() != null 
				&& (_entity.getSiege().checkIsAttacker(pc.getClan())
				|| _entity.getSiege().checkIsDefender(pc.getClan())))
			{
				pc.setIsInSiege(true);
			}
		}
		
		super.onEnter(character);
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(FLAG_PVP, false);
		character.setInsideZone(FLAG_SIEGE, false);
		character.setInsideZone(FLAG_NOSUMMON, false);
		
		character.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
		
		if (character instanceof L2SiegeSummonInstance)
			((L2SiegeSummonInstance)character).unSummon();
		
		else if (character instanceof L2PcInstance)
		{
			final L2PcInstance activeChar = (L2PcInstance)character;
			
			// Set pvp flag
			if (activeChar.getPvpFlag() == 0)
				activeChar.startPvPFlag();
			
			activeChar.stopFameTask();
			activeChar.setIsInSiege(false);
			
			L2ItemInstance item = activeChar.getInventory().getItemByItemId(9819);
			if (item != null)
			{
				Fort fort = FortManager.getInstance().getFort(activeChar);
				if (fort != null)
				{
					FortSiegeManager.getInstance().dropCombatFlag(activeChar);
				}
				else
				{
					int slot = item.getItem().getBodyPart();
					activeChar.getInventory().unEquipItemInBodySlotAndRecord(slot);
					activeChar.destroyItem("CombatFlag", item, null, true);
				}
			}
		}
		
		super.onExit(character);
	}
	
	@Override
	protected boolean checkDynamicConditions(L2Character character)
	{
		if (!isSiegeInProgress())
			return false;
		
		return super.checkDynamicConditions(character);
	}
	
	public void updateSiegeStatus()
	{
		revalidateAllInZone();
	}
	
	@Override
	protected void onDieInside(L2Character character)
	{
		if (_entity instanceof Fort)
		{
			Fort fort = (Fort)_entity;
			
			// debuff participants only if they die inside siege zone
			if (character instanceof L2PcInstance && ((L2PcInstance)character).getClan() != null)
			{
				int lvl;
				
				L2Effect effect = character.getFirstEffect(5660);
				if (effect != null)
					lvl = Math.min(effect.getLevel() + 1, SkillTable.getInstance().getMaxLevel(5660));
				else
					lvl = 1;
				
				if (fort.getOwnerClan() == ((L2PcInstance)character).getClan())
				{
					L2Skill skill = SkillTable.getInstance().getInfo(5660, lvl);
					if (skill != null)
						skill.getEffects(character, character);
				}
				else
				{
					for (L2SiegeClan siegeclan : fort.getSiege().getAttackerClans())
					{
						if (siegeclan == null)
							continue;
						if (((L2PcInstance)character).getClanId() == siegeclan.getClanId())
						{
							L2Skill skill = SkillTable.getInstance().getInfo(5660, lvl);
							if (skill != null)
								skill.getEffects(character, character);
							break;
						}
					}
				}
			}
		}
		
		super.onDieInside(character);
	}
}
