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
package net.sf.l2j.gameserver.model.zone;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class L2DefaultZone extends L2Zone
{
	@Override
	protected void onEnter(L2Character character)
	{
		if(_onEnterMsg != null && character instanceof L2PcInstance)
			character.sendPacket(_onEnterMsg);
		
		if(_abnormal > 0)
			character.startAbnormalEffect(_abnormal);
		
		if(_applyEnter != null)
		{
			for(L2Skill sk : _applyEnter)
				sk.getEffects(character, character);
		}
		if(_removeEnter != null)
		{
			for(L2Skill sk : _removeEnter)
				character.stopSkillEffects(sk.getId());
		}

		if (_pvp == PvpSettings.ARENA)
		{
			character.setInsideZone(FLAG_PVP, true);
			if (character instanceof L2PcInstance)
				((L2PcInstance)character).sendPacket(new SystemMessage(SystemMessageId.ENTERED_COMBAT_ZONE));
		}
		else if (_pvp == PvpSettings.PEACE)
		{
			if (Config.ZONE_TOWN != 2)
				character.setInsideZone(FLAG_PEACE, true);
		}

		if (_noLanding)
		{
			character.setInsideZone(FLAG_NOLANDING, true);
		}
		if (_noEscape)
		{
			character.setInsideZone(FLAG_NOESCAPE, true);
		}
		if (_noPrivateStore)
		{
			character.setInsideZone(FLAG_NOSTORE, true);
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
			for(L2Skill sk : _applyExit)
				sk.getEffects(character, character);
		}
		if(_removeExit != null)
		{
			for(L2Skill sk : _removeExit)
				character.stopSkillEffects(sk.getId());
		}
		
		if (_pvp == PvpSettings.ARENA)
		{
			character.setInsideZone(FLAG_PVP, false);
			if (character instanceof L2PcInstance)
				((L2PcInstance)character).sendPacket(new SystemMessage(SystemMessageId.LEFT_COMBAT_ZONE));
		}
		else if (_pvp == PvpSettings.PEACE)
		{
			character.setInsideZone(FLAG_PEACE, false);
		}

		if (_noLanding)
		{
			character.setInsideZone(FLAG_NOLANDING, false);
		}
		if (_noEscape)
		{
			character.setInsideZone(FLAG_NOESCAPE, false);
		}
		if (_noPrivateStore)
		{
			character.setInsideZone(FLAG_NOSTORE, false);
		}
	}

	@Override
	public final void onDieInside(L2Character character)
	{
		if (_exitOnDeath)
			onExit(character);
	}
	
	@Override
	public final void onReviveInside(L2Character character)
	{
		if (_exitOnDeath)
			onEnter(character);
	}
}
