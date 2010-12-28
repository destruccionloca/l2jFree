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

public class AurabirdFalcon extends L2Transformation
{
	private static final int[] SKILLS = new int[] { 932 };
	
	public AurabirdFalcon()
	{
		// id, colRadius, colHeight
		super(8, 38, 14.25);
	}
	
	@Override
	public void transformedSkills(L2PcInstance player)
	{
		// Air Blink
		if (player.getLevel() >= 75)
			addSkill(player, 885, 1);
		
		// Exhilarate
		if (player.getLevel() >= 83)
			addSkill(player, 894, 1);
		
		int level = player.getLevel() - 74;
		if (level > 0)
		{
			addSkill(player, 884, level); // Air Assault (11 levels)
			addSkill(player, 886, level); // Air Shock Bomb (11 levels)
			addSkill(player, 888, level); // Energy Storm (11 levels)
			addSkill(player, 890, level); // Prodigious Flare (11 levels)
			addSkill(player, 891, level); // Energy Shot (11 levels)
			addSkill(player, 911, level); // Energy Burst (11 levels)
		}
		
		player.addTransformAllowedSkill(SKILLS);
		
		player.setIsFlyingMounted(true);
	}
	
	@Override
	public void removeSkills(L2PcInstance player)
	{
		removeSkill(player, 885); // Air Blink
		
		removeSkill(player, 894); // Exhilarate
		
		removeSkill(player, 884); // Air Assault (11 levels)
		removeSkill(player, 886); // Air Shock Bomb (11 levels)
		removeSkill(player, 888); // Energy Storm (11 levels)
		removeSkill(player, 890); // Prodigious Flare (11 levels)
		removeSkill(player, 891); // Energy Shot (11 levels)
		removeSkill(player, 911); // Energy Burst (11 levels)
		
		player.setIsFlyingMounted(false);
	}
	
	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new AurabirdFalcon());
	}
}
