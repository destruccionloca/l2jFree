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

import java.util.List;

import com.l2jfree.Config;
import com.l2jfree.gameserver.instancemanager.InstanceManager;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2Summon;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.entity.Instance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;

public class L2DefaultZone extends L2Zone
{
	public final static int REASON_OK = 0, REASON_MULTIPLE_INSTANCE = 1, REASON_INSTANCE_FULL = 2, REASON_SMALL_GROUP = 3;

	private static class InstanceResult
	{
		public int instanceId = 0;
		public int reason = REASON_OK;
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if (_onEnterMsg != null && character instanceof L2PcInstance)
			character.sendPacket(_onEnterMsg);
		
		if (_abnormal > 0)
			character.startAbnormalEffect(_abnormal);
		
		if (_applyEnter != null)
		{
			for (L2Skill sk : _applyEnter)
				sk.getEffects(character, character);
		}
		if (_removeEnter != null)
		{
			for (L2Skill sk : _removeEnter)
				character.stopSkillEffects(sk.getId());
		}

		if (_funcTemplates != null)
		{
			character.addStatFuncs(getStatFuncs(character));
		}
		
		if (_pvp == PvpSettings.ARENA)
		{
			character.setInsideZone(FLAG_NOSUMMON, true);
			character.setInsideZone(FLAG_PVP, true);
			character.sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);
		}
		else if (_pvp == PvpSettings.PEACE)
		{
			if (Config.ZONE_TOWN != 2)
				character.setInsideZone(FLAG_PEACE, true);
		}

		if (_noLanding && character instanceof L2PcInstance)
		{
			character.setInsideZone(FLAG_NOLANDING, true);
			if (((L2PcInstance) character).getMountType() == 2)
			{
				character.sendPacket(SystemMessageId.AREA_CANNOT_BE_ENTERED_WHILE_MOUNTED_WYVERN);
				((L2PcInstance) character).enteredNoLanding();
			}
		}
		if (_noEscape)
		{
			character.setInsideZone(FLAG_NOESCAPE, true);
		}
		if (_noPrivateStore)
		{
			character.setInsideZone(FLAG_NOSTORE, true);
		}
		if (_noSummon)
		{
			character.setInsideZone(FLAG_NOSUMMON, true);
		}
		if (_envSlow)
			character.setInsideZone(FLAG_SWAMP, true);
		if (_noHeal)
			character.setInsideZone(FLAG_NOHEAL, true);
		if (_padown)
			character.setInsideZone(FLAG_P_ATK_DOWN, true);
		if (_pddown)
			character.setInsideZone(FLAG_P_DEF_DOWN, true);

		if (_instanceName != null && _instanceGroup != null && character instanceof L2PcInstance)
		{
			L2PcInstance pl = (L2PcInstance) character;
			InstanceResult ir = new InstanceResult();

			if (_instanceGroup.equals("party"))
			{
				if (pl.isInParty())
				{
					List<L2PcInstance> list = pl.getParty().getPartyMembers();
					getInstanceFromGroup(ir, list, false);
					checkPlayersInside(ir, list);
				}
			}
			else if (_instanceGroup.equals("clan"))
			{
				if (pl.getClan() != null)
				{
					List<L2PcInstance> list = pl.getClan().getOnlineMembersList();
					getInstanceFromGroup(ir, list, true);
					checkPlayersInside(ir, list);
				}
			}
			else if (_instanceGroup.equals("alliance"))
			{
				if (pl.getAllyId() > 0)
				{
					List<L2PcInstance> list = pl.getClan().getOnlineAllyMembers();
					getInstanceFromGroup(ir, list, true);
					checkPlayersInside(ir, list);
				}
			}

			if (ir.reason == REASON_MULTIPLE_INSTANCE)
			{
				pl.sendMessage("You cannot enter this instance while other " + _instanceGroup + " members are in another instance.");
			}
			else if (ir.reason == REASON_INSTANCE_FULL)
			{
				pl.sendMessage("This instance is full. There is a maximum of " + _maxPlayers + " players inside.");
			}
			else if (ir.reason == REASON_SMALL_GROUP)
			{
				pl.sendMessage("Your " + _instanceGroup + " is too small. There is a minimum of " + _minPlayers + " players inside.");
			}
			else
			{
				try
				{
					if (ir.instanceId == 0)
						ir.instanceId = InstanceManager.getInstance().createDynamicInstance(_instanceName);
					portIntoInstance(pl, ir.instanceId);
				}
				catch (Exception e)
				{
					pl.sendMessage("The requested instance could not be created.");
				}
			}
		}
	}

	@Override
	protected void onExit(L2Character character)
	{
		if(_onExitMsg != null && character instanceof L2PcInstance)
			character.sendPacket(_onExitMsg);
		
		if(_abnormal > 0)
			character.stopAbnormalEffect(_abnormal);

		if(_applyExit != null)
		{
			for (L2Skill sk : _applyExit)
				sk.getEffects(character, character);
		}
		if(_removeExit != null)
		{
			for (L2Skill sk : _removeExit)
				character.stopSkillEffects(sk.getId());
		}
		if (_funcTemplates != null)
		{
			character.removeStatsOwner(this);
		}

		if (_pvp == PvpSettings.ARENA)
		{
			character.setInsideZone(FLAG_NOSUMMON, false);
			character.setInsideZone(FLAG_PVP, false);
			if (character instanceof L2PcInstance)
				character.sendPacket(new SystemMessage(SystemMessageId.LEFT_COMBAT_ZONE));
		}
		else if (_pvp == PvpSettings.PEACE)
		{
			character.setInsideZone(FLAG_PEACE, false);
		}

		if (_noLanding && character instanceof L2PcInstance)
		{
			character.setInsideZone(FLAG_NOLANDING, false);
			if (((L2PcInstance) character).getMountType() == 2)
			{
				((L2PcInstance) character).exitedNoLanding();
			}
		}
		if (_noEscape)
		{
			character.setInsideZone(FLAG_NOESCAPE, false);
		}
		if (_noPrivateStore)
		{
			character.setInsideZone(FLAG_NOSTORE, false);
		}
		if (_noSummon)
		{
			character.setInsideZone(FLAG_NOSUMMON, false);
		}
		if (_envSlow)
			character.setInsideZone(FLAG_SWAMP, false);
		if (_noHeal)
			character.setInsideZone(FLAG_NOHEAL, false);
		if (_padown)
			character.setInsideZone(FLAG_P_ATK_DOWN, false);
		if (_pddown)
			character.setInsideZone(FLAG_P_DEF_DOWN, false);

		if (character instanceof L2PcInstance && _instanceName != null && character.getInstanceId() > 0)
		{
			portIntoInstance((L2PcInstance) character, 0);
		}
	}

	@Override
	public void onDieInside(L2Character character)
	{
		if (_exitOnDeath)
			onExit(character);
	}
	
	@Override
	public void onReviveInside(L2Character character)
	{
		if (_exitOnDeath)
			onEnter(character);
	}

	private void getInstanceFromGroup(InstanceResult ir, List<L2PcInstance> group, boolean allowMultiple)
	{
		for (L2PcInstance mem : group)
		{
			if (mem == null || mem.getInstanceId() == 0)
				continue;

			Instance i = InstanceManager.getInstance().getInstance(mem.getInstanceId());
			if (i.getName().equals(_instanceName))
			{
				ir.instanceId = i.getId(); // Player in this instance template found
				return;
			}
			else if (!allowMultiple)
			{
				ir.reason = REASON_MULTIPLE_INSTANCE;
				return;
			}
		}
	}

	private void checkPlayersInside(InstanceResult ir, List<L2PcInstance> group)
	{
		if (ir.reason != REASON_OK)
			return;

		int valid = 0, all = 0;

		for (L2PcInstance mem : group)
		{
			if (mem != null && mem.getInstanceId() == ir.instanceId)
				valid++;
			all++;

			if (valid == _maxPlayers)
			{
				ir.reason = REASON_INSTANCE_FULL;
				return;
			}
		}
		if (all < _minPlayers)
		{
			ir.reason = REASON_SMALL_GROUP;
		}
	}

	private void portIntoInstance(L2PcInstance pl, int instanceId)
	{
		pl.setInstanceId(instanceId);
		pl.getKnownList().updateKnownObjects();
		L2Summon pet = pl.getPet();
		if (pet != null)
		{
			pet.setInstanceId(instanceId);
			pet.getKnownList().updateKnownObjects();
		}
	}
}