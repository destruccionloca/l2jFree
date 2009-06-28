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
package com.l2jfree.gameserver.model.entity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.gameserver.model.L2Clan;

/**
 * Vice - 2008 Class managing periodical events with castle
 */
public class FortUpdater implements Runnable
{
	protected static Log _log = LogFactory.getLog(FortUpdater.class.getName());
	
	private L2Clan _clan;
	private Fort _fort;
	private int _runCount;
	
	public FortUpdater(Fort fort, L2Clan clan, int runCount)
	{
		_fort = fort;
		_clan = clan;
		_runCount = runCount;
	}
	
	public void run()
	{
		try
		{
			_runCount++;
			if (_fort.getOwnerClan() == null || _fort.getOwnerClan() != _clan
					|| (_runCount * Config.FS_BLOOD_OATH_FRQ * 60) > (_fort.getOwnedTime() + 60))
				return;
			
			_fort.setBloodOathReward(_fort.getBloodOathReward() + Config.FS_BLOOD_OATH_COUNT);
		}
		catch (Exception e)
		{
			_log.error(e.getMessage(), e);
		}
	}
}
