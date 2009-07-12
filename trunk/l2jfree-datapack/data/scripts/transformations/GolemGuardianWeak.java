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
 * @author durgus
 *
 */
public class GolemGuardianWeak extends L2DefaultTransformation
{
	public GolemGuardianWeak()
	{
		// id, colRadius, colHeight
		super(212, 8.0, 23.5);
	}

	@Override
	public void transformedSkills(L2PcInstance player)
	{
		addSkill(player, 572, 2); // Double Slasher
		addSkill(player, 573, 2); // Earthquake
		addSkill(player, 574, 2); // Bomb Installation
		addSkill(player, 575, 2); // Steel Cutter
	}

	@Override
	public void removeSkills(L2PcInstance player)
	{
		removeSkill(player, 572); // Double Slasher
		removeSkill(player, 573); // Earthquake
		removeSkill(player, 574); // Bomb Installation
		removeSkill(player, 575); // Steel Cutter
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new GolemGuardianWeak());
	}
}
