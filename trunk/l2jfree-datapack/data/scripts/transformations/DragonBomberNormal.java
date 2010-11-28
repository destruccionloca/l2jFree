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

public class DragonBomberNormal extends L2Transformation
{
	private static final int[] SKILLS = new int[]{};

	public DragonBomberNormal()
	{
		// id, colRadius, colHeight
		super(217, 16, 24);
	}

	@Override
	public void transformedSkills(L2PcInstance player)
	{
			int level = -1;
			if (player.getLevel() >= 60)
				level = 3;
			else if (player.getLevel() >= 1)
				level = 1;
		{
			addSkill(player, 580, level); // Death Blow (4 levels)
			addSkill(player, 581, level); // Sand Cloud (4 levels)
			addSkill(player, 582, level); // Scope Bleed (4 levels)
			addSkill(player, 583, level); // Assimilation (4 levels)
		}

		player.addTransformAllowedSkill(SKILLS);
	}

	@Override
	public void removeSkills(L2PcInstance player)
	{
		removeSkill(player, 580); // Death Blow (4 levels)
		removeSkill(player, 581); // Sand Cloud (4 levels)
		removeSkill(player, 582); // Scope Bleed (4 levels)
		removeSkill(player, 583); // Assimilation (4 levels)
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new DragonBomberNormal());
	}
}
