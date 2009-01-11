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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.taskmanager.Task;
import com.l2jfree.gameserver.taskmanager.TaskManager;
import com.l2jfree.gameserver.taskmanager.TaskTypes;
import com.l2jfree.gameserver.taskmanager.TaskManager.ExecutedTask;

/**
 * @author Layane
 *
 */
public class TaskRecom extends Task
{
	private static final Log	_log	= LogFactory.getLog(TaskRecom.class.getName());
	private static final String	NAME	= "sp_recommendations";

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.taskmanager.Task#getName()
	 */
	@Override
	public String getName()
	{
		return NAME;
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.taskmanager.Task#onTimeElapsed(com.l2jfree.gameserver.taskmanager.TaskManager.ExecutedTask)
	 */
	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			player.restartRecom();
			player.broadcastUserInfo();
		}
		_log.info("Recommendation Global Task: launched.");
	}

	@Override
	public void initializate()
	{
		super.initializate();
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_GLOBAL_TASK, "1", "13:00:00", "");
	}

}
