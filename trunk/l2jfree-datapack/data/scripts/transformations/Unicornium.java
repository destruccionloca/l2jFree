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
public class Unicornium extends L2Transformation
{
	private static final int[] SKILLS = new int[]{};

	public Unicornium()
	{
		// id, colRadius, colHeight
		super(220, 8, 30);
	}

	@Override
	public void transformedSkills(L2PcInstance player)
	{
		{
			addSkill(player, 906, 4); // Lance Step (6 levels)
			addSkill(player, 907, 4); // Aqua Blast (6 levels)
			addSkill(player, 908, 4); // Spin Slash (6 levels)
			addSkill(player, 909, 4); // Ice Focus (6 levels)
			addSkill(player, 910, 4); // Water Jet (6 levels)
		}

		player.addTransformAllowedSkill(SKILLS);
	}

	@Override
	public void removeSkills(L2PcInstance player)
	{
		removeSkill(player, 906); // Lance Step (6 levels)
		removeSkill(player, 907); // Aqua Blast (6 levels)
		removeSkill(player, 908); // Spin Slash (6 levels)
		removeSkill(player, 909); // Ice Focus (6 levels)
		removeSkill(player, 910); // Water Jet (6 levels)
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new Unicornium());
	}
}
