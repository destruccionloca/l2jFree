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

public class InfernoDrakeStrong extends L2Transformation
{
	private static final int[] SKILLS = new int[]{};

	public InfernoDrakeStrong()
	{
		// id, colRadius, colHeight
		super(213, 15, 24);
	}

	@Override
	public void transformedSkills(L2PcInstance player)
	{
			int level = -1;
			if (player.getLevel() >= 60)
				level = 4;
			else if (player.getLevel() >= 1)
				level = 1;
		{
			addSkill(player, 576, level); // Paw Strike (4 levels)
			addSkill(player, 577, level); // Fire Breath (4 levels)
			addSkill(player, 578, level); // Blaze Quake (4 levels)
			addSkill(player, 579, level); // Fire Armor (4 levels)
		}

		player.addTransformAllowedSkill(SKILLS);
	}

	@Override
	public void removeSkills(L2PcInstance player)
	{
		removeSkill(player, 576); // Paw Strike (4 levels)
		removeSkill(player, 577); // Fire Breath (4 levels)
		removeSkill(player, 578); // Blaze Quake (4 levels)
		removeSkill(player, 579); // Fire Armor (4 levels)
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new InfernoDrakeStrong());
	}
}
