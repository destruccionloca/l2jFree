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

import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Character.AIAccessor;

/**
 * This class manages AI of Body Destroyer in Steel Citadel
 * * first attaker get 30 debuff that will kill player if mob will be alive
 * 
 * @author hex1r0
 **/
public class SCBodyDestroyerAI extends L2AttackableAI
{
	private int _firstAttakerObjectId = 0;
	
	public SCBodyDestroyerAI(AIAccessor accessor)
	{
		super(accessor);
	}
	
	@Override
	protected void onEvtAttacked(L2Character attacker)
	{
		if (_firstAttakerObjectId == 0)
		{
			_firstAttakerObjectId = attacker.getObjectId();
			L2Skill skill = SkillTable.getInstance().getInfo(5256, 1);
			if (skill != null)
			{
				skill.getEffects(getActor(), attacker);
			}
		}
		super.onEvtAttacked(attacker);
	}
	
	@Override
	protected void onEvtDead()
	{
		if (_firstAttakerObjectId > 0)
		{
			L2World.getInstance().getPlayer(_firstAttakerObjectId).getEffects().stopEffects(5256);
			_firstAttakerObjectId = 0;
		}
		super.onEvtDead();
	}
}