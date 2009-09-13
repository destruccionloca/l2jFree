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
 * @author Psychokiller1888
 */
public class Unicornium extends L2Transformation
{
	public Unicornium()
	{
		// id, colRadius, colHeight
		super(219, 16, 24);
	}
	
	@Override
	public void transformedSkills(L2PcInstance player)
	{
		addSkill(player, 906, 4); // Lance Step
		addSkill(player, 907, 4); // Aqua Blast
		addSkill(player, 908, 4); // Spin Slash
		addSkill(player, 909, 4); // Ice Focus
		addSkill(player, 910, 4); // Water Jet
		addSkill(player, 8248, 1); // Transfrom Dispel
	}
	
	@Override
	public void removeSkills(L2PcInstance player)
	{
		removeSkill(player, 906); // Lance Step
		removeSkill(player, 907); // Aqua Blast
		removeSkill(player, 908); // Spin Slash
		removeSkill(player, 909); // Ice Focus
		removeSkill(player, 910); // Water Jet
		removeSkill(player, 8248); // Transfrom Dispel
	}
	
	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new Unicornium());
	}
}
