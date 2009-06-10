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

import com.l2jfree.Config;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.L2Object;

/**
 * @author Forsaiken
 */
public final class Attack extends L2GameServerPacket
{
	private static final String	_S__06_ATTACK	= "[S] 33 Attack";

	private final int			_attackerObjId;
	private final int			_attackerX;
	private final int			_attackerY;
	private final int			_attackerZ;
	private int					_targetX;
	private int					_targetY;
	private int					_targetZ;

	public final boolean		soulshot;
	protected final int			_grade;

	private int					_targetObjId;
	private int					_targetDamage;
	private int					_targetFlags;

	private int[][]				_hits;

	public Attack(final L2Character attacker, final boolean ss, final int grade)
	{
		_attackerObjId = attacker.getObjectId();
		soulshot = ss;
		_grade = grade;
		_attackerX = attacker.getX();
		_attackerY = attacker.getY();
		_attackerZ = attacker.getZ();
	}

	private final int getFlags(final boolean miss, final boolean crit, final byte shld)
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

	public final void addHit(final L2Object targetObj, final int damage, final boolean miss, final boolean crit, final byte shld)
	{
		_targetX = targetObj.getX();
		_targetY = targetObj.getY();
		_targetZ = targetObj.getZ();

		if (_targetObjId == 0)
		{
			_targetObjId = targetObj.getObjectId();
			_targetDamage = damage;
			_targetFlags = getFlags(miss, crit, shld);
		}
		else
		{
			final int[] hit = new int[] { _targetObjId, _targetDamage, getFlags(miss, crit, shld) };

			if (_hits == null)
			{
				_hits = new int[][] { hit };
			}
			else
			{
				final int off = _hits.length;
				final int[][] temp = new int[off + 1][];
				System.arraycopy(_hits, 0, temp, 0, _hits.length);
				_hits = temp;
				_hits[off] = hit;
			}
		}

	}

	public final boolean hasHits()
	{
		return _targetObjId > 0;
	}

	@Override
	protected final void writeImpl()
	{
		super.writeC(0x33);
		super.writeD(_attackerObjId);
		super.writeD(_targetObjId);
		super.writeD(_targetDamage);
		super.writeC(_targetFlags);
		super.writeD(_attackerX);
		super.writeD(_attackerY);
		super.writeD(_attackerZ);

		if (_hits == null)
		{
			super.writeH(0x00);
		}
		else
		{
			super.writeH(_hits.length);
			for (final int[] hit : _hits)
			{
				super.writeD(hit[0]);
				super.writeD(hit[1]);
				super.writeC(hit[2]);
			}
		}
		if(_targetX!=0)
			writeD(_targetX);
		else
			writeD(_attackerX);
		
		if(_targetY!=0)
			writeD(_targetY);
		else
			writeD(_attackerY);
		
		if(_targetZ!=0)
			writeD(_targetZ);
		else
			writeD(_attackerZ);
		
	}

	@Override
	public final String getType()
	{
		return _S__06_ATTACK;
	}
}