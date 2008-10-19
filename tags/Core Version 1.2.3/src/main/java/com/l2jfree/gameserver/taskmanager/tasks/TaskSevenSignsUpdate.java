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

import com.l2jfree.gameserver.SevenSigns;
import com.l2jfree.gameserver.SevenSignsFestival;
import com.l2jfree.gameserver.taskmanager.Task;
import com.l2jfree.gameserver.taskmanager.TaskManager;
import com.l2jfree.gameserver.taskmanager.TaskTypes;
import com.l2jfree.gameserver.taskmanager.TaskManager.ExecutedTask;

/**
 * Updates all data for the Seven Signs and Festival of Darkness engines,
 * when time is elapsed.
 * 
 * @author Tempy
 */
public class TaskSevenSignsUpdate extends Task
{
	private static final Log	_log	= LogFactory.getLog(TaskSevenSignsUpdate.class);

	public static final String	NAME	= "seven_signs_update";

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		try
		{
			SevenSigns.getInstance().saveSevenSignsData(null, true);

			if (!SevenSigns.getInstance().isSealValidationPeriod())
				SevenSignsFestival.getInstance().saveFestivalData(false);

			_log.info("SevenSigns: Data updated successfully.");
		}
		catch (Exception e)
		{
			_log.error("SevenSigns: Failed to save Seven Signs configuration: " + e, e);
		}
	}

	@Override
	public void initializate()
	{
		super.initializate();
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_FIXED_SHEDULED, "1800000", "1800000", "");
	}
}
