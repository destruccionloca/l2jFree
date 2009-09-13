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
public class DoomWraith extends L2Transformation
{
	public DoomWraith()
	{
		// id, colRadius, colHeight
		super(2, 8, 22);
	}
	
	@Override
	public void transformedSkills(L2PcInstance player)
	{
		addSkill(player, 586, 2); // Rolling Attack
		addSkill(player, 588, 2); // Curse of Darkness
		addSkill(player, 587, 2); // Dig Attack
		addSkill(player, 589, 2); // Darkness Energy Drain
	}
	
	@Override
	public void removeSkills(L2PcInstance player)
	{
		removeSkill(player, 586); // Rolling Attack
		removeSkill(player, 588); // Curse of Darkness
		removeSkill(player, 587); // Dig Attack
		removeSkill(player, 589); // Darkness Energy Drain
	}
	
	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new DoomWraith());
	}
}
