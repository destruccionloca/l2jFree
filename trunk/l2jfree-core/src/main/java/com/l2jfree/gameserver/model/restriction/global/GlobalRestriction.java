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
package com.l2jfree.gameserver.model.restriction.global;

import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author NB4L1
 */
public interface GlobalRestriction
{
	public boolean canInviteToParty(L2PcInstance activeChar, L2PcInstance target);
	
	public boolean canCreateEffect(L2Character activeChar, L2Character target, L2Skill skill);
	
	public boolean isInvul(L2Character activeChar, L2Character target, boolean isOffensive);
	
	// TODO
	
	public void levelChanged(L2PcInstance activeChar);
	
	public void effectCreated(L2Effect effect);
	
	public void playerLoggedIn(L2PcInstance activeChar);
	
	public void playerDisconnected(L2PcInstance activeChar);
	
	// TODO
}
