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
package transformations;

import com.l2jfree.gameserver.instancemanager.TransformationManager;
import com.l2jfree.gameserver.model.L2Transformation;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

/*
 * TODO: Skill levels. How do they work? Transformation is given at level 83, there are 6 levels of the skill. How are they assigned? Based on player level somehow? Based on servitor?
 */
public class DemonRace extends L2Transformation
{
	private static final int[] SKILLS = new int[] {};
	
	public DemonRace()
	{
		// id, colRadius, colHeight
		super(221, 11, 27);
	}
	
	@Override
	public void transformedSkills(L2PcInstance player)
	{
		{
			addSkill(player, 901, 4); // Dark Strike (6 levels)
			addSkill(player, 902, 4); // Bursting Flame (6 levels)
			addSkill(player, 903, 4); // Stratum Explosion (6 levels)
			addSkill(player, 904, 4); // Corpse Burst (6 levels)
			addSkill(player, 905, 4); // Dark Detonation (6 levels)
		}
		
		player.addTransformAllowedSkill(SKILLS);
	}
	
	@Override
	public void removeSkills(L2PcInstance player)
	{
		removeSkill(player, 901); // Dark Strike (6 levels)
		removeSkill(player, 902); // Bursting Flame (6 levels)
		removeSkill(player, 903); // Stratum Explosion (6 levels)
		removeSkill(player, 904); // Corpse Burst (6 levels)
		removeSkill(player, 905); // Dark Detonation (6 levels)
	}
	
	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new DemonRace());
	}
}
