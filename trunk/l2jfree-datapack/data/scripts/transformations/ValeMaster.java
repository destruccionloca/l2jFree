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
import com.l2jfree.gameserver.model.L2DefaultTransformation;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

/**
 * Description: <br>
 * This will handle the transformation, giving the skills, and removing them, when the player logs out and is transformed these skills
 * do not save.
 * When the player logs back in, there will be a call from the enterworld packet that will add all their skills.
 * The enterworld packet will transform a player.
 *
 * @author Ahmed
 *
 */
public class ValeMaster extends L2DefaultTransformation
{
	public ValeMaster()
	{
		// id, colRadius, colHeight
		super(4, 12.0, 40.0);
	}

	@Override
	public void transformedSkills(L2PcInstance player)
	{
		int level = 1;
		if (player.getLevel() >= 76)
		{
			level = 3;
		}
		else if (player.getLevel() >= 73)
		{
			level = 2;
		}
		addSkill(player, 742, level);
		addSkill(player, 743, level);
		addSkill(player, 744, level);
		addSkill(player, 745, level); // Vale Master Dark Curse
	}

	@Override
	public void removeSkills(L2PcInstance player)
	{
		removeSkill(player, 742);
		removeSkill(player, 743);
		removeSkill(player, 744);
		removeSkill(player, 745); // Vale Master Dark Curse
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new ValeMaster());
	}
}
