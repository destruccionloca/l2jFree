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
public class FlyingFinalForm extends L2Transformation
{
	public FlyingFinalForm()
	{
		// id, colRadius, colHeight
		super(260, 9, 38);
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
		// FIXME: addSkills(932, 950, 951, 953, 1544, 1545);
	}
	
	@Override
	public void removeSkills(L2PcInstance player)
	{
		// FIXME: removeSkills(932, 950, 951, 953, 1544, 1545);
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
		// TransformationManager.getInstance().registerTransformation(new FlyingFinalForm());
	}
}
