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
public class PumpkinGhost extends L2Transformation
{
	public PumpkinGhost()
	{
		// id, colRadius, colHeight
		super(108, 15.5, 29);
	}
	
	@Override
	public void transformedSkills(L2PcInstance player)
	{
		addSkill(player, 619, 1); // Transfrom Dispel
		addSkill(player, 5437, 2); // Dissonance
	}
	
	@Override
	public void removeSkills(L2PcInstance player)
	{
		removeSkill(player, 619); // Transfrom Dispel
		removeSkill(player, 5437); // Dissonance
	}
	
	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new PumpkinGhost());
	}
}
