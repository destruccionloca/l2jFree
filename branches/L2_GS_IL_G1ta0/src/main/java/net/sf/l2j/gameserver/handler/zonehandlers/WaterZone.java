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

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.zone.IZone;
import net.sf.l2j.gameserver.model.zone.ZoneEnum.ZoneType;
/**
 * @author G1ta0
 *
 */
public class WaterZone extends DefaultZone
{
	public WaterZone(IZone zone)
	{
		super(zone);
	}
	
	@Override
	public void onEnter(L2Character character)
	{
		if (character instanceof L2PcInstance && !character.isInsideZone(ZoneType.Water))
		{
			((L2PcInstance)character).startWaterTask();
		}
		
		super.onEnter(character);
	}
	
	@Override
	public void onExit(L2Character character)
	{
		super.onExit(character);
		
		if (character instanceof L2PcInstance)
		{
			((L2PcInstance)character).stopWaterTask();
		}
	}
}
