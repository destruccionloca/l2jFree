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
public class DivineRogue extends L2DefaultTransformation
{
	public DivineRogue()
	{
		// id, colRadius, colHeight
		super(254, 12.0, 30.0);
	}

	@Override
	public void transformedSkills(L2PcInstance player)
	{
		/* Commented till we get proper values for these skills
		addSkill(player, 686, 1); // Divine Rogue Stun Shot
		addSkill(player, 687, 1); // Divine Rogue Double Shot
		addSkill(player, 688, 1); // Divine Rogue Bleed Attack
		addSkill(player, 689, 1); // Divine Rogue Deadly Blow
		addSkill(player, 690, 1); // Divine Rogue Agility
		addSkill(player, 691, 1); // Sacrifice Rogue
		addSkill(player, 797, 1); // Divine Rogue Piercing Attack
		*/
	}

	@Override
	public void removeSkills(L2PcInstance player)
	{
		/* Commented till we get proper values for these skills
		removeSkill(player, 686); // Divine Rogue Stun Shot
		removeSkill(player, 687); // Divine Rogue Double Shot
		removeSkill(player, 688); // Divine Rogue Bleed Attack
		removeSkill(player, 689); // Divine Rogue Deadly Blow
		removeSkill(player, 690); // Divine Rogue Agility
		removeSkill(player, 691); // Sacrifice Rogue
		removeSkill(player, 797); // Divine Rogue Piercing Attack
		*/
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new DivineRogue());
	}
}
