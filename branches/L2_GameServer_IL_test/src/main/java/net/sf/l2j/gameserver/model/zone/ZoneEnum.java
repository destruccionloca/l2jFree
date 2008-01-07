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
package net.sf.l2j.gameserver.model.zone;

import java.lang.reflect.Constructor;

import net.sf.l2j.gameserver.model.zone.ZoneSettings;
import net.sf.l2j.gameserver.model.zone.type.L2BigheadZone;
import net.sf.l2j.gameserver.model.zone.type.L2BossLairZone;
import net.sf.l2j.gameserver.model.zone.type.L2CastleZone;
import net.sf.l2j.gameserver.model.zone.type.L2ClanHallZone;
import net.sf.l2j.gameserver.model.zone.type.L2DefaultZone;
import net.sf.l2j.gameserver.model.zone.type.L2MotherTreeZone;
import net.sf.l2j.gameserver.model.zone.type.L2OlympiadStadiumZone;
import net.sf.l2j.gameserver.model.zone.type.L2TownZone;
import net.sf.l2j.gameserver.model.zone.type.L2WaterZone;

/**
 * @author G1ta0
 * 
 */

public final class ZoneEnum
{
	public enum ZoneType
	{
		Default,
        Arena,
		BigHead (L2BigheadZone.class),
		CastleArea (L2CastleZone.class),
		CastleHQ,
		ClanHall (L2ClanHallZone.class),
        DefenderSpawn,
		Dungeon,
		Fishing,
		FourSepulchers,
		Jail,
		MotherTree (L2MotherTreeZone.class),
		MonsterDerbyTrack,
		Newbie,
		NoEscape,
		NoLanding,
		OlympiadStadia (L2OlympiadStadiumZone.class),
		Peace,
		SiegeBattleField,
		Town (L2TownZone.class),
		Water (L2WaterZone.class),
		
		AntharasLair,
		BaiumsLair,
		SailrensLair,
		ValakasLair,
		SunLightRoom;

		private Class<? extends L2Zone> _zoneClass;
		private ZoneSettings _settings = null;

		private ZoneType()
		{
			_zoneClass = L2DefaultZone.class;
		}

		private ZoneType(Class<? extends L2Zone> zoneClass) 
		{
			_zoneClass = zoneClass;
		}
		
		private ZoneType(Class<? extends L2Zone> zoneClass, ZoneSettings settings) 
		{
			_zoneClass = zoneClass;
			_settings = settings;
		}
		
		public Class<? extends L2Zone> getZoneClass()
		{
			return _zoneClass;
		}

		public ZoneSettings getSettings()
		{
			return _settings;
		}

		public final static ZoneType getZoneTypeEnum(String typeName)
		{
			for (ZoneType zt : ZoneType.values())
				if (zt.toString().equalsIgnoreCase(typeName))
					return zt;
			return null;
		}
	}
	
	public L2Zone getNewZone(ZoneType type, ZoneSettings set)
	{
		try
		{
			Constructor<? extends L2Zone> c = type.getZoneClass().getConstructor(ZoneSettings.class);
			return c.newInstance(set);
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public static enum RestartType
	{
		RestartNormal, RestartChaotic, RestartOwner, RestartRandom
	}
}
