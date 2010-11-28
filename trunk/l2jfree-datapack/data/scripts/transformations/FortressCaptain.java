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

public class FortressCaptain extends L2Transformation
{
	private static final int[] SKILLS = new int[]{};

	public FortressCaptain()
	{
		// id, colRadius, colHeight
		super(21, 14, 27);
	}

	@Override
	public void transformedSkills(L2PcInstance player)
	{
		{
			addSkill(player, 881, 1); // Fortress Captain Twister
			addSkill(player, 882, 1); // Fortress Captain Blaze
			addSkill(player, 883, 1); // Fortress Captain Seal of Binding
		}

		player.addTransformAllowedSkill(SKILLS);
	}

	@Override
	public void removeSkills(L2PcInstance player)
	{
		removeSkill(player, 881); // Fortress Captain Twister
		removeSkill(player, 882); // Fortress Captain Blaze
		removeSkill(player, 883); // Fortress Captain Seal of Binding
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new FortressCaptain());
	}
}
