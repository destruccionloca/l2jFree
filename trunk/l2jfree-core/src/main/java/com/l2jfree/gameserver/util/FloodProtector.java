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

import javolution.util.FastMap;
import javolution.util.FastMap.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.gameserver.GameTimeController;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

/**
 * Flood protector
 * 
 * @author durgus
 */
public class FloodProtector
{
	private static final Log		_log	= LogFactory.getLog(FloodProtector.class.getName());
	private static FloodProtector	_instance;

	public static final FloodProtector getInstance()
	{
		if (_instance == null)
			_instance = new FloodProtector();
		return _instance;
	}

	// =========================================================
	// Data Field
	private FastMap<Integer, Integer[]>	_floodClient;

	// =========================================================

	// reuse delays for protected actions (in game ticks 1 tick = 100ms)
	private static final int[]			REUSEDELAY				= new int[]
																{ 4, 42, 42, Config.GLOBAL_CHAT_TIME, Config.TRADE_CHAT_TIME, 16, 100, Config.SOCIAL_TIME, 20, 10 };

	// protected actions
	public static final int				PROTECTED_USEITEM		= 0;
	public static final int				PROTECTED_ROLLDICE		= 1;
	public static final int				PROTECTED_FIREWORK		= 2;
	public static final int				PROTECTED_GLOBAL_CHAT	= 3;
	public static final int				PROTECTED_TRADE_CHAT	= 4;
	public static final int				PROTECTED_ITEMPETSUMMON	= 5;
	public static final int				PROTECTED_HEROVOICE		= 6;
	public static final int				PROTECTED_SOCIAL		= 7;
	public static final int				PROTECTED_SUBCLASS		= 8;
	public static final int				PROTECTED_DROPITEM		= 9;

	// =========================================================
	// Constructor
	private FloodProtector()
	{
		_log.info("FloodProtector: initalized.");
		_floodClient = new FastMap<Integer, Integer[]>().setShared(true);
	}

	/**
	 * Add a new player to the flood protector (should be done for all players
	 * when they enter the world)
	 * 
	 * @param playerObjId
	 */
	public void registerNewPlayer(int playerObjId)
	{
		// create a new array
		Integer[] array = new Integer[REUSEDELAY.length];
		for (int i = 0; i < array.length; i++)
			array[i] = 0;

		// register the player with an empty array
		_floodClient.put(playerObjId, array);
	}

	/**
	 * Remove a player from the flood protector (should be done if player loggs
	 * off)
	 * 
	 * @param playerObjId
	 */
	public void removePlayer(int playerObjId)
	{
		_floodClient.remove(playerObjId);
	}

	/**
	 * Return the size of the flood protector
	 * 
	 * @return size
	 */
	public int getSize()
	{
		return _floodClient.size();
	}

	/**
	 * Try to perform the requested action
	 * 
	 * @param playerObjId
	 * @param action
	 * @return true if the action may be performed
	 */
	public boolean tryPerformAction(int playerObjId, int action)
	{
		Entry<Integer, Integer[]> entry = _floodClient.getEntry(playerObjId);
		if (entry == null || entry.getValue() == null)
		{
			registerNewPlayer(playerObjId);
			_log.warn("Player " + playerObjId + " tried to Perform action " + action + " but wasnt registered to Floodprotector!!");
		}

		entry = _floodClient.getEntry(playerObjId);
		Integer[] value = entry.getValue();

		if (value[action] < GameTimeController.getGameTicks())
		{
			value[action] = GameTimeController.getGameTicks() + REUSEDELAY[action];
			entry.setValue(value);
			return true;
		}
		return false;
	}
	
	public boolean tryPerformAction(L2PcInstance player, int action)
	{
		return tryPerformAction(player.getObjectId(), action);
	}
}