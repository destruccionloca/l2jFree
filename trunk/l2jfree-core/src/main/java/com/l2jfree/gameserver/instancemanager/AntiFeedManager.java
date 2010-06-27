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
package com.l2jfree.gameserver.instancemanager;

import java.util.Map;

import javolution.util.FastMap;

import com.l2jfree.Config;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.L2GameClient;

public class AntiFeedManager
{
	private final Map<Integer, Long> _lastDeathTimes;
	
	public static final AntiFeedManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private AntiFeedManager()
	{
		_lastDeathTimes = new FastMap<Integer, Long>().setShared(true);
	}
	
	/**
	 * Set time of the last player's death to current
	 * 
	 * @param objectId Player's objectId
	 */
	public final void setLastDeathTime(int objectId)
	{
		_lastDeathTimes.put(objectId, System.currentTimeMillis());
	}
	
	/**
	 * Check if current kill should be counted as non-feeded.
	 * 
	 * @param attacker Attacker character
	 * @param target Target character
	 * @return True if kill is non-feeded.
	 */
	public final boolean check(L2Character attacker, L2Character target)
	{
		if (!Config.ANTIFEED_ENABLE)
			return true;
		
		final L2PcInstance targetPlayer = L2Object.getActingPlayer(target);
		final L2PcInstance attackerPlayer = L2Object.getActingPlayer(attacker);
		
		if (targetPlayer == null || attackerPlayer == null)
			return true;
		
		if (Config.ANTIFEED_INTERVAL > 0)
		{
			final Long lastDeathTime = _lastDeathTimes.get(targetPlayer.getObjectId());
			
			if (lastDeathTime != null && System.currentTimeMillis() - lastDeathTime.longValue() < Config.ANTIFEED_INTERVAL)
				return false;
		}
		
		if (Config.ANTIFEED_DUALBOX)
		{
			final L2GameClient targetClient = targetPlayer.getClient();
			final L2GameClient attackerClient = attackerPlayer.getClient();
			
			// unable to check ip address
			if (targetClient == null || attackerClient == null)
			{
				if (Config.ANTIFEED_DISCONNECTED_AS_DUALBOX)
					return false;
			}
			else if (targetClient.getInetAddress().equals(attackerClient.getInetAddress()))
			{
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Clears all timestamps
	 */
	public final void clear()
	{
		_lastDeathTimes.clear();
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final AntiFeedManager _instance = new AntiFeedManager();
	}
}
