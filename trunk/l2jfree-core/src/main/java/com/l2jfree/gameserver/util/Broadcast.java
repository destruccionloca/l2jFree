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
package com.l2jfree.gameserver.util;

import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.serverpackets.CharInfo;
import com.l2jfree.gameserver.network.serverpackets.L2GameServerPacket;

/**
 * @author luisantonioa
 */
public final class Broadcast
{
	private Broadcast()
	{
	}
	
	public static void toKnownPlayers(L2Character character, L2GameServerPacket mov)
	{
		for (L2PcInstance player : character.getKnownList().getKnownPlayers().values())
			sendPacket(character, player, mov);
	}
	
	public static void toKnownPlayersInRadius(L2Character character, L2GameServerPacket mov, int radius)
	{
		if (radius < 0)
		{
			toKnownPlayers(character, mov);
			return;
		}
		
		for (L2PcInstance player : character.getKnownList().getKnownPlayers().values())
			if (character.isInsideRadius(player, radius, false, false))
				sendPacket(character, player, mov);
	}
	
	public static void toKnownPlayersInRadius(L2Character character, L2GameServerPacket mov, long radiusSq)
	{
		if (radiusSq < 0)
		{
			toKnownPlayers(character, mov);
			return;
		}
		
		for (L2PcInstance player : character.getKnownList().getKnownPlayers().values())
			if (character.getDistanceSq(player) <= radiusSq)
				sendPacket(character, player, mov);
	}
	
	public static void toSelfAndKnownPlayers(L2Character character, L2GameServerPacket mov)
	{
		sendPacketToSelf(character, mov);
		
		toKnownPlayers(character, mov);
	}
	
	public static void toSelfAndKnownPlayersInRadius(L2Character character, L2GameServerPacket mov, int radius)
	{
		sendPacketToSelf(character, mov);
		
		toKnownPlayersInRadius(character, mov, radius);
	}
	
	public static void toSelfAndKnownPlayersInRadius(L2Character character, L2GameServerPacket mov, long radiusSq)
	{
		sendPacketToSelf(character, mov);
		
		toKnownPlayersInRadius(character, mov, radiusSq);
	}
	
	public static void toAllOnlinePlayers(L2GameServerPacket mov)
	{
		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
			sendPacket(player, player, mov);
	}
	
	private static void sendPacketToSelf(L2Character character, L2GameServerPacket packet)
	{
		if (character instanceof L2PcInstance)
			sendPacket(character, (L2PcInstance)character, packet);
	}
	
	private static void sendPacket(L2Character src, L2PcInstance target, L2GameServerPacket packet)
	{
		if (target == null || target == src && packet instanceof CharInfo)
			return;
		
		target.sendPacket(packet);
	}
}
