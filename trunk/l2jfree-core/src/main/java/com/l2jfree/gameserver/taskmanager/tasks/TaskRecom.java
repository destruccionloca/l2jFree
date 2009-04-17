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
package com.l2jfree.gameserver.taskmanager.tasks;

import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.taskmanager.tasks.TaskManager.ExecutedTask;

/**
 * @author Layane
 */
final class TaskRecom extends TaskHandler
{
	TaskRecom()
	{
		TaskManager.addUniqueTask(getName(), TaskTypes.TYPE_GLOBAL_TASK, "1", "13:00:00", "");
	}
	
	@Override
	void onTimeElapsed(ExecutedTask task, String[] params)
	{
		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			player.restartRecom();
			player.broadcastUserInfo();
		}
		
		_log.info("Recommendation Global Task: launched.");
	}
}
