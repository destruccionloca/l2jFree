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

/**
 * @author durgus
 */
public class GolemGuardianStrong extends L2Transformation
{
	public GolemGuardianStrong()
	{
		// id, colRadius, colHeight
		super(210, 8.0, 23.5);
	}
	
	@Override
	public void transformedSkills(L2PcInstance player)
	{
		addSkill(player, 572, 4); // Double Slasher
		addSkill(player, 573, 4); // Earthquake
		addSkill(player, 574, 4); // Bomb Installation
		addSkill(player, 575, 4); // Steel Cutter
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
		TransformationManager.getInstance().registerTransformation(new GolemGuardianStrong());
	}
}
