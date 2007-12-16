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
package net.sf.l2j.gameserver.handler.zonehandlers;

import javolution.util.FastList;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.handler.IZoneHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.zone.IZone;
import net.sf.l2j.gameserver.model.zone.ZoneEnum.ZoneType;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * @author G1ta0
 *
 */
public class DefaultZone implements IZoneHandler
{
	private int onEnterMsgId = 0;
	private int onExitMsgId = 0;
	private FastList<ZoneType> zoneType = null;
	private String onEnterMsg = null;
	private String onExitMsg = null;
	private FastList<L2Skill> effects = null;
	private IZone _zone = null;

	public DefaultZone(IZone zone)
	{

		_zone = zone;
		zoneType = new FastList<ZoneType>();
		zoneType.add(_zone.getZoneType());
		
		if (zone.getSettings() == null) return;
		
		try
		{
			String zoneTypes = zone.getSettings().getString("secondaryType");
			for(String szt : zoneTypes.trim().split(" "))
			{
				ZoneType zt = ZoneType.getZoneTypeEnum(szt);
				if(zt != null)
					zoneType.add(zt);
			}
		}
		catch (IllegalArgumentException ia)
		{

		}

		try
		{
			onEnterMsgId = zone.getSettings().getInteger("onEnter");
		}
		catch (IllegalArgumentException ia)
		{
			try
			{
				onEnterMsg = zone.getSettings().getString("onEnter");
			}
			catch (IllegalArgumentException ia2)
			{}
		}

		try
		{
			onExitMsgId = zone.getSettings().getInteger("onExit");
		}
		catch (IllegalArgumentException ia)
		{
			try
			{
				onExitMsg = zone.getSettings().getString("onExit");
			}
			catch (IllegalArgumentException ia2)
			{}
		}

		try
		{
			int[] _skills = zone.getSettings().getIntegerArray("effects");

			effects = new FastList<L2Skill>();

			for(int i = 0; i < _skills.length;)
			{
				int _skillId = _skills[i];
				i++;
				int _skillLevel = _skills[i];
				L2Skill skill = SkillTable.getInstance().getInfo(_skillId, _skillLevel);
				if(skill != null)
					effects.add(skill);
			}

			if(effects.size() == 0)
				effects = null;

		}
		catch (IllegalArgumentException ia)
		{
			effects = null;
		}
	}

	public IZone getZone()
	{
		return _zone;
	}

	/**
	 *  Returns current zone types
	 */
	public FastList<ZoneType> getZoneTypes()
	{
		return zoneType;
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.handler.IZoneHandler#onEnter(net.sf.l2j.gameserver.model.zone.IZone, net.sf.l2j.gameserver.model.L2Character)
	 */
	public void onEnter(L2Character character)
	{
		// Doing the stuff for players only
		if(character instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) character;

			// Sending system messages
			if(onEnterMsgId > 0)
				player.sendPacket(new SystemMessage(onEnterMsgId));

			// Sending text messages
			if(onEnterMsg != null)
				player.sendMessage(onEnterMsg);
		}

		for(ZoneType zt : getZoneTypes())
			character.setInZone(zt, getZone());

		applyEffects(character);
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.handler.IZoneHandler#onMove(net.sf.l2j.gameserver.model.zone.IZone, net.sf.l2j.gameserver.model.L2Character)
	 */
	public void onMove(L2Character character)
	{
		
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.handler.IZoneHandler#onExit(net.sf.l2j.gameserver.model.zone.IZone, net.sf.l2j.gameserver.model.L2Character)
	 */
	public void onExit(L2Character character)
	{
		// Doing the stuff for players only
		if(character instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) character;

			// Sending system messages
			if(onExitMsgId > 0)
				player.sendPacket(new SystemMessage(onExitMsgId));

			// Sending text messages
			if(onExitMsg != null)
				player.sendMessage(onExitMsg);
		}

		for(ZoneType zt : getZoneTypes())
			character.setOutZone(zt, getZone());

		removeEffects(character);
	}

	// Applying the zone effects to the character
	private void applyEffects(L2Character character)
	{
		if(effects != null)
		{
			for(L2Skill skill : effects)
			{
				if(skill.checkCondition(character, character, false)) //TODO: G1ta0-> fix zone effects
				{
					try
					{
						L2Effect[] effects = character.getAllEffects();
						if(effects != null)
						{
							for(L2Effect e : effects)
							{
								if(e != null && e.getSkill().getId() == skill.getId())
								{
									e.exit();
								}
							}
						}
						skill.getEffects(null, character);
					}
					catch (Exception e)
					{}
				}
			}
		}
	}

	// Removing the zone effects from the character
	private void removeEffects(L2Character character)
	{
		if(effects != null)
		{
			for(L2Skill skill : effects)
			{
				L2Effect[] effects = character.getAllEffects();
				if(effects != null)
				{
					for(L2Effect e : effects)
					{
						if(e != null && e.getSkill().getId() == skill.getId())
						{
							e.exit();
						}
					}
				}
			}
		}
	}
}
