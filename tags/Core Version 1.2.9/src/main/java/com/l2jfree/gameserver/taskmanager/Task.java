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
package com.l2jfree.gameserver.taskmanager;

import java.util.concurrent.ScheduledFuture;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.gameserver.taskmanager.TaskManager.ExecutedTask;

/**
 * @author Layane
 *
 */
public abstract class Task
{
	private final static Log	_log	= LogFactory.getLog(Task.class.getName());

	public void initializate()
	{
		if (_log.isDebugEnabled())
			_log.debug("Task" + getName() + " inializate");
	}

	/**
	 * @param instance  
	 */
	public ScheduledFuture<?> launchSpecial(ExecutedTask instance)
	{
		return null;
	}

	public abstract String getName();

	public abstract void onTimeElapsed(ExecutedTask task);

	public void onDestroy()
	{
	}
}
