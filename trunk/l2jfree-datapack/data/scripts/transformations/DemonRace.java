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

public class DemonRace extends L2Transformation
{
	public DemonRace()
	{
		// id, colRadius, colHeight
		super(220, 16, 29);
	}
	
	@Override
	public void transformedSkills(L2PcInstance player)
	{
		addSkill(player, 901, 4); // Dark Strike
		addSkill(player, 902, 4); // Bursting Flame
		addSkill(player, 903, 4); // Stratum Explosion
		addSkill(player, 904, 4); // Corpse Burst
		addSkill(player, 905, 4); // Dark Detonation
		addSkill(player, 8248, 1); // Transfrom Dispel
	}
	
	@Override
	public void removeSkills(L2PcInstance player)
	{
		removeSkill(player, 901); // Dark Strike
		removeSkill(player, 902); // Bursting Flame
		removeSkill(player, 903); // Stratum Explosion
		removeSkill(player, 904); // Corpse Burst
		removeSkill(player, 905); // Dark Detonation
		removeSkill(player, 8248); // Transfrom Dispel
	}
	
	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new DemonRace());
	}
}
