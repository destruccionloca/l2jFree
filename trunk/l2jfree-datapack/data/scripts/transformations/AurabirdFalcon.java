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

import com.l2jfree.gameserver.model.L2Transformation;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

/**
 * FIXME: move missing methods from L2Jserver!
 * 
 * @author Kerberos, Respawner
 */
public class AurabirdFalcon extends L2Transformation
{
	public AurabirdFalcon()
	{
		// id, colRadius, colHeight
		super(8, 38, 14);
	}
	
	@Override
	public void onTransform(L2PcInstance player)
	{
		// FIXME: super();
		player.setIsFlyingMounted(true);
	}
	
	@Override
	public void transformedSkills(L2PcInstance player)
	{
		// FIXME: super();
		
		if (player.getLevel() >= 75)
			addSkill(player, 885, 1);
		
		int level = player.getLevel() - 74;
		if (level > 0)
		{
			addSkill(player, 884, level);
			addSkill(player, 886, level);
			addSkill(player, 888, level);
			addSkill(player, 891, level);
			addSkill(player, 911, level);
		}
	}
	
	@Override
	public void removeSkills(L2PcInstance player)
	{
		/* FIXME
		removeSkill(player, 885, 1);

		int level = player.getLevel() - 74;
		if (level > 0)
		{
			removeSkill(player, 884, level);
			removeSkill(player, 886, level);
			removeSkill(player, 888, level);
			removeSkill(player, 891, level);
			removeSkill(player, 911, level);
		}
		*/
	}
	
	@Override
	public void onUntransform(L2PcInstance player)
	{
		// FIXME: super();
		player.setIsFlyingMounted(false);
	}
	
	public static void main(String[] args)
	{
		// FIXME: remove when fixed
		// TransformationManager.getInstance().registerTransformation(new AurabirdFalcon());
	}
}
