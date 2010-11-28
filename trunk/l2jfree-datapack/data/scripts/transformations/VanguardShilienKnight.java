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

public class VanguardShilienKnight extends L2Transformation
{
	private static final int[] SKILLS = new int[]{18, 22, 28, 33, 278, 279, 289, 401};

	public VanguardShilienKnight()
	{
		// id
		super(315);
	}

	@Override
	public void transformedSkills(L2PcInstance player)
	{
		if (player.getLevel() > 43)
		{
			int level = player.getLevel() - 43;
			addSkill(player, 144, level); // Dual Weapon Mastery
			addSkill(player, 815, level); // Blade Hurricane
			addSkill(player, 817, level); // Double Strike
		}

		if (player.getLevel() > 48)
		{
			int level = player.getLevel() - 48;
			addSkill(player, 958, level); // Triple Blade Slash
		}

		int level = -1;
		if (player.getLevel() >= 73)
			level = 3;
		else if (player.getLevel() >= 65)
			level = 2;
		else if (player.getLevel() >= 57)
			level = 1;
		{
			addSkill(player, 956, level); // Boost Morale
		}

		player.addTransformAllowedSkill(SKILLS);
	}

	@Override
	public void removeSkills(L2PcInstance player)
	{
		removeSkill(player, 144); // Dual Weapon Mastery
		removeSkill(player, 815); // Blade Hurricane
		removeSkill(player, 817); // Double Strike
		removeSkill(player, 956); // Boost Morale
		removeSkill(player, 958); // Triple Blade Slash
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new VanguardShilienKnight());
	}
}
