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
package com.l2jfree.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import com.l2jfree.Config;
import com.l2jfree.gameserver.model.actor.L2Playable;

/**
 * @author NB4L1
 */
public abstract class EffectInfoPacket extends L2GameServerPacket
{
	private final EffectInfoPacketList _list;
	
	protected EffectInfoPacket(EffectInfoPacketList list)
	{
		_list = list;
	}
	
	protected final L2Playable getPlayable()
	{
		return _list._playable;
	}
	
	protected final int size()
	{
		return _list._effects.size();
	}
	
	protected final void writeEffectInfos()
	{
		for (Effect e : _list._effects)
		{
			writeD(e._id);
			writeH(e._level);
			writeD(e._duration);
		}
	}
	
	private static final class Effect
	{
		private final int _id;
		private final int _level;
		private final int _duration;
		
		private Effect(int id, int level, int duration)
		{
			_id = id;
			_level = level;
			_duration = duration;
		}
	}
	
	public static final class EffectInfoPacketList
	{
		private final List<Effect> _effects = new ArrayList<Effect>(Config.ALT_BUFFS_MAX_AMOUNT + 5);
		private final L2Playable _playable;
		
		public EffectInfoPacketList(L2Playable playable)
		{
			_playable = playable;
			_playable.getEffects().addPacket(EffectInfoPacketList.this);
		}
		
		public final void addEffect(int id, int level, int duration)
		{
			_effects.add(new Effect(id, level, duration));
		}
	}
}
