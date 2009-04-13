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

import com.l2jfree.gameserver.model.actor.status.CharStatus;

public final class RegenTaskManager extends AbstractIterativePeriodicTaskManager<CharStatus>
{
	private static RegenTaskManager _instance;
	
	public static RegenTaskManager getInstance()
	{
		if (_instance == null)
			_instance = new RegenTaskManager();
		
		return _instance;
	}
	
	private RegenTaskManager()
	{
		super(1000);
	}
	
	public synchronized boolean hasRegenTask(CharStatus status)
	{
		return hasTask(status);
	}
	
	public synchronized void startRegenTask(CharStatus status)
	{
		startTask(status);
	}
	
	public synchronized void stopRegenTask(CharStatus status)
	{
		stopTask(status);
	}
	
	@Override
	void callTask(CharStatus task)
	{
		task.regenTask();
	}
}
