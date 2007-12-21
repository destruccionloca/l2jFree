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
		MonsterDerbyTrack,
		OlympiadStadia,
		CastleArea,
		CastleHQ,
		DefenderSpawn,
		SiegeBattleField,
		ClanHall,
		Newbie,
		Fishing,
		Peace,
		Dungeon,
		Water, 
		NoLanding,
		NoEscape,
		Jail,
		MotherTree,
		BossDungeon;


		public final static ZoneType getZoneTypeEnum(String typeName)
		{
			for (ZoneType zt : ZoneType.values())
				if (zt.toString().equalsIgnoreCase(typeName))
					return zt;

			return null;
		}
	}
	public static enum RestartType
	{
		RestartNormal, RestartChaotic, RestartOwner, RestartRandom
	}

}
