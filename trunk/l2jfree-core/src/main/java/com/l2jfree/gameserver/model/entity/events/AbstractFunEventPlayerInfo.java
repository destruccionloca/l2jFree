package com.l2jfree.gameserver.model.entity.events;

import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

/**
 * Used to store extra informations that could be useful later.<br>
 * For example original coords, karma, etc to restore it, when the event ends.
 * 
 * @author NB4L1
 */
public abstract class AbstractFunEventPlayerInfo
{
	private final L2PcInstance _player;
	
	protected AbstractFunEventPlayerInfo(L2PcInstance player)
	{
		_player = player;
	}
	
	public final L2PcInstance getPlayer()
	{
		return _player;
	}
}
