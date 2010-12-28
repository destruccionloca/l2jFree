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

public class VanguardPaladin extends L2Transformation
{
	private static final int[] SKILLS = new int[] { 18, 28, 196, 197, 400, 406 };
	
	public VanguardPaladin()
	{
		// id
		super(312);
	}
	
	@Override
	public void transformedSkills(L2PcInstance player)
	{
		if (player.getLevel() > 43)
		{
			int level = player.getLevel() - 43;
			addSkill(player, 293, level); // Two Handed Mastery
			addSkill(player, 814, level); // Full Swing
			addSkill(player, 816, level); // Cleave
		}
		
		if (player.getLevel() > 48)
		{
			int level = player.getLevel() - 48;
			addSkill(player, 957, level); // Guillotine Attack
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
		removeSkill(player, 293); // Two Handed Mastery
		removeSkill(player, 814); // Full Swing
		removeSkill(player, 816); // Cleave
		removeSkill(player, 956); // Boost Morale
		removeSkill(player, 957); // Guillotine Attack
	}
	
	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new VanguardPaladin());
	}
}
