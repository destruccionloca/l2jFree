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
package com.l2jfree.gameserver.network.client;

import com.l2jfree.gameserver.gameobjects.L2Player;
import com.l2jfree.gameserver.network.client.packets.L2ServerPacket;

/**
 * @author NB4L1
 */
public final class EmptyClient implements IL2Client
{
	private static final class SingletonHolder
	{
		public static final EmptyClient INSTANCE = new EmptyClient();
	}
	
	public static EmptyClient getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	@Override
	public void setActiveChar(L2Player player)
	{
		// do nothing
	}
	
	@Override
	public boolean sendPacket(L2ServerPacket sp)
	{
		// do nothing
		return false;
	}
}
