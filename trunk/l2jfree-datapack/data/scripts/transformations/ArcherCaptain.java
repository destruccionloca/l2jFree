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
 * @author Psychokiller1888
 *
 */
public class ArcherCaptain extends L2DefaultTransformation
{
	public ArcherCaptain()
	{
		// id, colRadius, colHeight
		super(17, 13.0, 24.0);
	}

	@Override
	public void transformedSkills(L2PcInstance player)
	{
		addSkill(player,8248, 1); // Transfrom Dispel
	}

	@Override
	public void removeSkills(L2PcInstance player)
	{
		removeSkill(player,8248); // Transfrom Dispel
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new ArcherCaptain());
	}
}