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
import org.python.util.PythonInterpreter;

import com.l2jfree.gameserver.taskmanager.Task;
import com.l2jfree.gameserver.taskmanager.TaskManager.ExecutedTask;

/**
 * @author Layane
 *
 */
public class TaskJython extends Task
{
	public static final String		NAME	= "jython";
	protected static final Log		_log	= LogFactory.getLog(TaskJython.class.getName());

	private final PythonInterpreter	_python	= new PythonInterpreter();

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
		_log.info("executing cron: data/scripts/cron/" + task.getParams()[2]);
		_python.cleanup();
		_python.exec("import sys");
		_python.execfile("data/scripts/cron/" + task.getParams()[2]);
	}

}
