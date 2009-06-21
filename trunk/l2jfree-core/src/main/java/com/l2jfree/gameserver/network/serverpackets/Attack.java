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

import org.apache.commons.lang.ArrayUtils;

import com.l2jfree.Config;
import com.l2jfree.gameserver.model.actor.L2Character;

/**
 * @author Forsaiken
 */
public final class Attack extends L2GameServerPacket
{
	private static final String _S__06_ATTACK = "[S] 33 Attack";
	
	private static final class Hit
	{
		private static final Hit[] EMPTY_ARRAY = new Hit[0];
		
		private final int _targetId;
		private final int _damage;
		private final int _flags;
		
		private Hit(int targetId, int damage, int flags)
		{
			_targetId = targetId;
			_damage = damage;
			_flags = flags;
		}
	}
	
	private final int _attackerObjId;
	private final int _attackerX;
	private final int _attackerY;
	private final int _attackerZ;
	private final int _targetX;
	private final int _targetY;
	private final int _targetZ;
	
	public final boolean soulshot;
	private final int _grade;
	
	private Hit[] _hits = Hit.EMPTY_ARRAY;
	
	public Attack(L2Character attacker, L2Character target, boolean ss, int grade)
	{
		_attackerObjId = attacker.getObjectId();
		soulshot = ss;
		_grade = grade;
		_attackerX = attacker.getX();
		_attackerY = attacker.getY();
		_attackerZ = attacker.getZ();
		_targetX = target.getX();
		_targetY = target.getY();
		_targetZ = target.getZ();
	}
	
	private int getFlags(boolean miss, boolean crit, byte shld)
	{
		int flags = 0;
		
		if (soulshot)
			flags |= 0x10 | _grade;
		
		if (crit)
			flags |= 0x20;
		
		if (shld > 0)
			flags |= 0x40;
		
		if (miss)
			flags |= 0x80;
		
		return flags;
	}
	
	public void addHit(L2Character target, int damage, boolean miss, boolean crit, byte shld)
	{
		_hits = (Hit[])ArrayUtils.add(_hits, new Hit(target.getObjectId(), damage, getFlags(miss, crit, shld)));
	}
	
	public boolean hasHits()
	{
		return _hits.length > 0;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x33);
		
		writeD(_attackerObjId);
		writeD(_hits[0]._targetId);
		writeD(_hits[0]._damage);
		writeC(_hits[0]._flags);
		writeD(_attackerX);
		writeD(_attackerY);
		writeD(_attackerZ);
		
		writeH(_hits.length - 1);
		for (int i = 1; i < _hits.length; i++)
		{
			final Hit hit = _hits[i];
			
			writeD(hit._targetId);
			writeD(hit._damage);
			writeC(hit._flags);
		}
		
		if (Config.PACKET_FINAL)
		{
			writeD(_targetX);
			writeD(_targetY);
			writeD(_targetZ);
		}
	}
	
	@Override
	public String getType()
	{
		return _S__06_ATTACK;
	}
}
