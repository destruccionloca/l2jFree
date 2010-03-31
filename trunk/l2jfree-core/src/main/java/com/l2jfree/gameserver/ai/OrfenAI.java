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
package com.l2jfree.gameserver.ai;

import com.l2jfree.gameserver.model.actor.L2Character.AIAccessor;

/** 
 * @author hex1r0
 **/
public class OrfenAI extends L2AttackableAI
{
	private enum Position {FIELD, NEST};
	private Position _pos = Position.FIELD;
	
	public OrfenAI(AIAccessor accessor)
	{
		super(accessor);
	}

	@Override
	protected void thinkActive()
	{
		super.thinkActive();
		if (_actor.getCurrentHp() > _actor.getMaxHp() / 2 && _pos == Position.NEST)
		{
			_pos = Position.FIELD;
			// TODO tele to field
		}
	}

	@Override
	protected void thinkAttack()
	{
		super.thinkAttack();
		
		if (_actor.getCurrentHp() < _actor.getMaxHp() / 2 && _pos == Position.FIELD)
		{
			_pos = Position.NEST;
			// TODO tele to nest
		}
		// TODO figth algorithm
	}
}