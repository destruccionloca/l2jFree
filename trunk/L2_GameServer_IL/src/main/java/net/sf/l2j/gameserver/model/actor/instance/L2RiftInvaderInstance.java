/*
 * This program is free software; you can redistribute it and/or modify
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
package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.instancemanager.DimensionalRiftManager.RoomType;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2RiftInvaderInstance extends L2MonsterInstance
{
    private RoomType _roomType;
    private byte _roomId;

	public L2RiftInvaderInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	public RoomType  getType()
	{
		return _roomType;
	}
	
	public byte getRoom()
	{
		return _roomId;
	}
	
	public void setType(RoomType roomType)
	{
        roomType = _roomType;
	}
	
	public void setRoom(byte roomId)
	{
        _roomId = roomId;
	}
}