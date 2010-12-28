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

public class Zaken extends L2Transformation
{
	private static final int[] SKILLS = new int[] {};
	
	public Zaken()
	{
		// id, colRadius, colHeight
		super(305, 16, 32);
	}
	
	@Override
	public void transformedSkills(L2PcInstance player)
	{
		int level = -1;
		if (player.getLevel() >= 82)
			level = 4;
		else if (player.getLevel() >= 80)
			level = 3;
		else if (player.getLevel() >= 75)
			level = 2;
		else if (player.getLevel() >= 70)
			level = 1;
		{
			addSkill(player, 715, level); // Zaken Energy Drain (4 levels)
			addSkill(player, 716, level); // Zaken Hold (4 levels)
			addSkill(player, 717, level); // Zaken Concentrated Attack (4 levels)
			addSkill(player, 718, level); // Zaken Dancing Sword (4 levels)
			addSkill(player, 719, 1); // Zaken Vampiric Rage
		}
		
		player.addTransformAllowedSkill(SKILLS);
	}
	
	@Override
	public void removeSkills(L2PcInstance player)
	{
		removeSkill(player, 715); // Zaken Energy Drain (4 levels)
		removeSkill(player, 716); // Zaken Hold (4 levels)
		removeSkill(player, 717); // Zaken Concentrated Attack (4 levels)
		removeSkill(player, 718); // Zaken Dancing Sword (4 levels)
		removeSkill(player, 719); // Zaken Vampiric Rage
	}
	
	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new Zaken());
	}
}
