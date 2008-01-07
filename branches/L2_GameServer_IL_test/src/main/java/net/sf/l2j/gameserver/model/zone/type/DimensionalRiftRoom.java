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

import javolution.util.FastList;

import net.sf.l2j.gameserver.instancemanager.DimensionalRiftManager.RoomType;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.zone.L2Zone;
import net.sf.l2j.gameserver.model.zone.ZoneEnum.RestartType;

public class DimensionalRiftRoom extends L2Zone
{
	private int _id;
	private boolean _isBoss;
	private RoomType _roomType;
	private final FastList<L2Spawn> _roomSpawns;

	public DimensionalRiftRoom(int id)
	{
		super(null);
		_id = id;
		_roomSpawns = new FastList<L2Spawn>();
	}

	public int getId()
	{
		return _id;
	}

	public void setRoomType(RoomType roomType)
	{
		_roomType = roomType;
	}

	public RoomType getRoomType()
	{
		return _roomType;
	}

	public void setIsBoss(boolean isBoss)
	{
		_isBoss = isBoss;
	}

	public boolean isBoss()
	{
		return _isBoss;
	}

	public Location getTeleport()
	{
		return getRestartPoint(RestartType.RestartNormal);
	}

	public FastList<L2Spawn> getSpawns()
	{
		return _roomSpawns;
	}

	public void spawn()
	{
		for (L2Spawn spawn : _roomSpawns)
		{
			spawn.doSpawn();
			spawn.startRespawn();
		}
	}

	public void unspawn()
	{
		for (L2Spawn spawn : _roomSpawns)
		{
			spawn.stopRespawn();
			if (spawn.getLastSpawn() != null)
				spawn.getLastSpawn().deleteMe();
		}
	}

	@Override
	protected void onEnter(L2Character character)
	{
	}
	
	@Override
	protected void onExit(L2Character character)
	{
	}
}