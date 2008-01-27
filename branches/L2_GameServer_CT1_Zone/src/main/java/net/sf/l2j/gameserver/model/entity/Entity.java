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
package net.sf.l2j.gameserver.model.entity;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.mapregion.TeleportWhereType;
import net.sf.l2j.gameserver.model.zone.L2Zone;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Entity
{
	protected static Log _log = LogFactory.getLog(Entity.class.getName());

	protected L2Zone _zone;

	public void registerZone(L2Zone zone)
	{
		_zone = zone;
	}

	public L2Zone getZone()
	{
		return _zone;
	}

	public int getTownId()
	{
		return _zone.getTownId();
	}

	public int getCastleId()
	{
		return _zone.getCastleId();
	}

	public boolean checkIfInZone(L2Character cha)
	{
		return _zone.isInsideZone(cha);
	}

	public boolean checkIfInZone(int x, int y, int z)
	{
		return _zone.isInsideZone(x, y, z);
	}

	public boolean checkIfInZone(int x, int y)
	{
		return _zone.isInsideZone(x, y);
	}

	public double getDistanceToZone(int x, int y) 
	{
		return _zone.getDistanceToZone(x, y);
	}

	protected boolean checkBanish(L2PcInstance cha)
	{
		return false;
	}

	public void banishForeigner(L2PcInstance activeChar)
	{
		// Get players from this and nearest world regions
		for (L2PlayableInstance player : L2World.getInstance().getVisiblePlayable(activeChar))
		{
			if(!(player instanceof L2PcInstance))
				continue;

			if (!checkBanish((L2PcInstance)player))
				continue;

			if (checkIfInZone(player))
				player.teleToLocation(TeleportWhereType.Town); 
		}
	}

	public void broadcastToPlayers(String message)
	{
		SystemMessage msg = SystemMessage.sendString(message);
		// Get players from this and nearest world regions
		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			if (checkIfInZone(player))
				player.sendPacket(msg);
		}
	}

	public void broadcastToPlayers(L2GameServerPacket gsp)
	{
		// Get players from this and nearest world regions
		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			if (checkIfInZone(player))
				player.sendPacket(gsp); 
		}
	}
}