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

public class Benom extends L2Transformation
{
	private static final int[] SKILLS = new int[]{};

	public Benom()
	{
		// id, colRadius, colHeight
		super(307, 20, 56);
	}

	@Override
	public void transformedSkills(L2PcInstance player)
	{
			int level = -1;
			if (player.getLevel() >= 82)
				level = 2;
			else if (player.getLevel() >= 80)
				level = 1;
		{
			addSkill(player, 725, level); // Benom Power Smash (2 levels)
			addSkill(player, 726, level); // Benom Sonic Storm (2 levels)
			addSkill(player, 727, 1); // Benom Disillusion
		}

		player.addTransformAllowedSkill(SKILLS);
	}

	@Override
	public void removeSkills(L2PcInstance player)
	{
		removeSkill(player, 725); // Benom Power Smash (2 levels)
		removeSkill(player, 726); // Benom Sonic Storm (2 levels)
		removeSkill(player, 727); // Benom Disillusion
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new Benom());
	}
}
