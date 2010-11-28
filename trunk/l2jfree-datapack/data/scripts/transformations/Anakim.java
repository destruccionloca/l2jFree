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

public class Anakim extends L2Transformation
{
	private static final int[] SKILLS = new int[]{};

	public Anakim()
	{
		// id, colRadius, colHeight
		super(306, 15.5, 29);
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
			addSkill(player, 720, level); // Anakim Holy Light Burst (2 levels)
			addSkill(player, 721, level); // Anakim Energy Attack (2 levels)
			addSkill(player, 722, level); // Anakim Holy Beam (2 levels)
			addSkill(player, 723, 1); // Anakim Sunshine
			addSkill(player, 724, 1); // Anakim Cleanse
		}

		player.addTransformAllowedSkill(SKILLS);
	}

	@Override
	public void removeSkills(L2PcInstance player)
	{
		removeSkill(player, 720); // Anakim Holy Light Burst (2 levels)
		removeSkill(player, 721); // Anakim Energy Attack (2 levels)
		removeSkill(player, 722); // Anakim Holy Beam (2 levels)
		removeSkill(player, 723); // Anakim Sunshine
		removeSkill(player, 724); // Anakim Cleanse
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new Anakim());
	}
}
