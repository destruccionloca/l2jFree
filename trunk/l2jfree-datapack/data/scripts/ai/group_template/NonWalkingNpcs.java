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
package ai.group_template;

import com.l2jfree.gameserver.model.actor.L2Npc;

/**
 * @author Psycho(killer1888) / L2jFree
 * 
 * This class rules every npcs that should not walk around when they spawn but still having a walk speed
 * for display and returning home functions...
 */

public class NonWalkingNpcs extends L2AttackableAIScript
{
	private static final int[] MOBLIST = {
		18343, // Gatekeeper Zombie
		18345, // Sprigant
		18367, // Prison Guard
		18368, // Prison Guard
		22138, // Chapel Guard
	};

	public NonWalkingNpcs(int id, String name, String descr)
	{
		super(id, name, descr);
        for (int mobId : MOBLIST)
            super.addSpawnId(mobId);
	}

	@Override
	public String onSpawn (L2Npc npc)
	{
		npc.setIsNoRndWalk(true);
		return null;
	}
	
	public static void main(String[] args)
	{
		new NonWalkingNpcs(-1,"nonwalkingnpcs","ai");
	}
}