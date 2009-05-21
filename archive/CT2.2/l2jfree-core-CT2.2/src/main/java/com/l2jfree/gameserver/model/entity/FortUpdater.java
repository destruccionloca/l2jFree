/* This program is free software: you can redistribute it and/or modify it under
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

import com.l2jfree.gameserver.model.L2Clan;

/**
 * Vice - 2008 Class managing periodical events with castle
 */
public class FortUpdater implements Runnable
{
	protected static Log _log = LogFactory.getLog(FortUpdater.class.getName());
	
	@SuppressWarnings("unused")
	private L2Clan _clan;
	@SuppressWarnings("unused")
	private int _runCount = 0;
	
	public FortUpdater(L2Clan clan, int runCount)
	{
		_clan = clan;
		_runCount = runCount;
	}
	
	public void run()
	{
		try
		{
		}
		catch (Exception e)
		{
			_log.error(e.getMessage(), e);
		}
	}
}
