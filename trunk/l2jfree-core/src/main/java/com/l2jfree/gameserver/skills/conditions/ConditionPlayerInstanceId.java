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
package com.l2jfree.gameserver.skills.conditions;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import com.l2jfree.gameserver.instancemanager.InstanceManager;
import com.l2jfree.gameserver.instancemanager.InstanceManager.InstanceWorld;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.skills.Env;

public class ConditionPlayerInstanceId extends Condition
{
	private final int[] _instanceIds;

	public ConditionPlayerInstanceId(List<Integer> instanceIds)
	{
		_instanceIds = ArrayUtils.toPrimitive(instanceIds.toArray(new Integer[instanceIds.size()]), 0);

		Arrays.sort(_instanceIds);
	}

	@Override
	public boolean testImpl(Env env)
	{
		if (!(env.player instanceof L2PcInstance))
			return false;

		if (!env.player.isInInstance())
			return false; // player not in instance

		final InstanceWorld world = InstanceManager.getInstance().getPlayerWorld((L2PcInstance)env.player);
		if (world == null || !env.player.isSameInstance(world.instanceId))
			return false; // player in the different instance

		return Arrays.binarySearch(_instanceIds, world.templateId) >= 0;
	}
}
