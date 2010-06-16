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
package com.l2jfree.gameserver.model.actor.knownlist;

import com.l2jfree.gameserver.ai.CtrlIntention;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.actor.instance.L2DefenderInstance;

public final class DefenderKnownList extends AttackableKnownList
{
	// =========================================================
	// Data Field
	
	// =========================================================
	// Constructor
	public DefenderKnownList(L2DefenderInstance activeChar)
	{
		super(activeChar);
	}
	
	// =========================================================
	// Method - Public
	@Override
	public boolean addKnownObject(L2Object object)
	{
		if (!super.addKnownObject(object))
			return false;
		
		if (getActiveChar().shouldAttack(object.getActingPlayer()))
		{
			if (getActiveChar().getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
				getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
		}
		
		return true;
	}
	
	// =========================================================
	// Property - Public
	@Override
	public L2DefenderInstance getActiveChar()
	{
		return (L2DefenderInstance)_activeChar;
	}
}
