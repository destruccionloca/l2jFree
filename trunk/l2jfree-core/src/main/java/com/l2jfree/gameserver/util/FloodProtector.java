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

import com.l2jfree.Config;
import com.l2jfree.gameserver.GameTimeController;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

/**
 * Flood protector
 * 
 * @author durgus
 */
public final class FloodProtector
{
	public static enum Protected
	{
		USEITEM(400),
		ROLLDICE(4200),
		FIREWORK(4200),
		GLOBAL_CHAT(Config.GLOBAL_CHAT_TIME * GameTimeController.MILLIS_IN_TICK),
		TRADE_CHAT(Config.TRADE_CHAT_TIME * GameTimeController.MILLIS_IN_TICK),
		ITEMPETSUMMON(1600),
		HEROVOICE(10000),
		SOCIAL(Config.SOCIAL_TIME * GameTimeController.MILLIS_IN_TICK),
		SUBCLASS(2000),
		DROPITEM(1000),
		MULTISELL(100);
		
		private final int _reuseDelay;
		
		private Protected(int reuseDelay)
		{
			_reuseDelay = reuseDelay;
		}
		
		private int getReuseDelay()
		{
			return _reuseDelay;
		}
		
		public static final int VALUES_LENGTH = Protected.values().length;
	}
	
	public static boolean tryPerformAction(L2PcInstance player, Protected action)
	{
		if (player.isGM())
			return true;
		
		long[] value = player.getFloodProtectors();
		
		synchronized (value)
		{
			if (value[action.ordinal()] > System.currentTimeMillis())
				return false;
			
			value[action.ordinal()] = System.currentTimeMillis() + action.getReuseDelay();
			return true;
		}
	}
}
