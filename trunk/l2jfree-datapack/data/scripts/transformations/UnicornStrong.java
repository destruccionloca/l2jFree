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
public class UnicornStrong extends L2DefaultTransformation
{
	public UnicornStrong()
	{
		// id, colRadius, colHeight
		super(204, 8.0, 25.5);
	}

	@Override
	public void transformedSkills(L2PcInstance player)
	{
		addSkill(player, 563, 4); // Horn of Doom
		addSkill(player, 564, 4); // Gravity Control
		addSkill(player, 565, 4); // Horn Assault
		addSkill(player, 567, 4); // Light of Heal
	}

	@Override
	public void removeSkills(L2PcInstance player)
	{
		removeSkill(player, 563); // Horn of Doom
		removeSkill(player, 564); // Gravity Control
		removeSkill(player, 565); // Horn Assault
		removeSkill(player, 567); // Light of Heal
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new UnicornStrong());
	}
}
