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

import java.util.Calendar;
import java.util.Map;

import com.l2jfree.gameserver.datatables.ClanTable;
import com.l2jfree.gameserver.instancemanager.RaidPointsManager;
import com.l2jfree.gameserver.model.L2Clan;
import com.l2jfree.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import com.l2jfree.gameserver.taskmanager.tasks.TaskManager.ExecutedTask;

final class TaskRaidPointsReset extends TaskHandler
{
	TaskRaidPointsReset()
	{
		TaskManager.addUniqueTask(getName(), TaskTypes.TYPE_GLOBAL_TASK, "1", "00:10:00", "");
	}
	
	@Override
	void onTimeElapsed(ExecutedTask task, String[] params)
	{
		if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY)
		{
			// reward clan reputation points
			for (L2Clan c : ClanTable.getInstance().getClans())
			{
				for (Map.Entry<Integer, Integer> entry : RaidPointsManager.getRankList().entrySet())
				{
					if (entry.getValue() <= 100 && c.isMember(entry.getKey()))
					{
						final int reputation;
						
						switch (entry.getValue())
						{
							case 1:
								reputation = 1250;
								break;
							case 2:
								reputation = 900;
								break;
							case 3:
								reputation = 700;
								break;
							case 4:
								reputation = 600;
								break;
							case 5:
								reputation = 450;
								break;
							case 6:
								reputation = 350;
								break;
							case 7:
								reputation = 300;
								break;
							case 8:
								reputation = 200;
								break;
							case 9:
								reputation = 150;
								break;
							case 10:
								reputation = 100;
								break;
							default:
								if (entry.getValue() <= 50)
									reputation = 25;
								else
									reputation = 12;
								break;
						}
						
						c.setReputationScore(c.getReputationScore() + reputation, true);
						c.broadcastToOnlineMembers(new PledgeShowInfoUpdate(c));
					}
				}
			}
			
			RaidPointsManager.cleanUp();
			
			_log.info("Raid Points Reset Global Task: launched.");
		}
	}
}
