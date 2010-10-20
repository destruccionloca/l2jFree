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
package com.l2jfree.gameserver.model.actor.instance;

import java.util.Set;

import com.l2jfree.gameserver.model.L2Spawn;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Tower;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;
import com.l2jfree.util.L2FastSet;

public final class L2ControlTowerInstance extends L2Tower
{
	private final Set<L2Spawn> _guards = new L2FastSet<L2Spawn>().setShared(true);
	
	public L2ControlTowerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	public Set<L2Spawn> getGuards()
	{
		return _guards;
	}
	
	public void registerGuard(L2Spawn guard)
	{
		getGuards().add(guard);
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
			return false;
		
		if (getCastle().getSiege().getIsInProgress())
			getCastle().getSiege().killedCT(this);
		
		for (L2Spawn spawn : getGuards())
			if (spawn != null)
				spawn.stopRespawn();
		
		getGuards().clear();
		return true;
	}
	
}
