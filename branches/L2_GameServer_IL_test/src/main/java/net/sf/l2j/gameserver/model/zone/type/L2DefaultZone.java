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
package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.zone.L2Zone;
import net.sf.l2j.gameserver.model.zone.ZoneSettings;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

public class L2DefaultZone extends L2Zone
{
	public L2DefaultZone(ZoneSettings set)
	{
		super(set);
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if(_set.getMask() > 0)
			character.setInsideZone(_set.getMask(), true);
		
		// Doing the stuff for players only
		if(character instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance)character;
			
			// Sending system messages
			if(_set.getOnEnterSysMsg() > 0)
				player.sendPacket(new SystemMessage(_set.getOnEnterSysMsg()));
			
			// Sending text messages
			if(_set.getOnEnterMsg() != null)
				player.sendMessage(_set.getOnEnterMsg());
		}
		
		// Applying the zone skills to the character
		if(_set.getZoneSkills() != null)
		{
			for(L2Skill skill : _set.getZoneSkills())
			{
				if(skill.checkCondition(character, false))
				{
					try
					{
						L2Effect[] effects = character.getAllEffects();
						if (effects != null)
						{
							for (L2Effect e : effects)
							{
								if (e != null && e.getSkill().getId() == skill.getId())
								{
									e.exit();
								}
							}
						}
						skill.getEffects(null, character);
					}
					catch(Exception e) {}
				}
			}
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		if(_set.getMask() > 0)
			character.setInsideZone(_set.getMask(), false);

		// Doing the stuff for players only
		if(character instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance)character;
			
			// Sending system messages
			if(_set.getOnExitSysMsg() > 0)
				player.sendPacket(new SystemMessage(_set.getOnExitSysMsg()));
			
			// Sending text messages
			if(_set.getOnExitMsg() != null)
				player.sendMessage(_set.getOnExitMsg());
		}

		//Removing zone skills
		if(_set.getZoneSkills() != null)
		{
			for(L2Skill skill : _set.getZoneSkills())
			{
				L2Effect[] effects = character.getAllEffects();
				if (effects != null)
				{
					for (L2Effect e : effects)
					{
						if (e != null && e.getSkill().getId() == skill.getId())
						{
							e.exit();
						}
					}
				}
			}
		}
	}
}
