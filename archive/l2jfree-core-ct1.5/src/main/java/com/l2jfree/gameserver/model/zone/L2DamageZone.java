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
package com.l2jfree.gameserver.model.zone;

import java.util.concurrent.ScheduledFuture;

import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.model.L2Character;


public class L2DamageZone extends L2DefaultZone
{
	private ScheduledFuture<?> task;

	@Override
	protected void onEnter(L2Character character)
	{
		character.setInsideZone(FLAG_DANGER, true);

		super.onEnter(character);

		startDamageTask();
	}

	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(FLAG_DANGER, false);
		super.onExit(character);
		if (getCharactersInside().size() == 0)
			stopDamageTask();
	}

	private synchronized void startDamageTask()
	{
		if (task == null)
			task = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new DamageTask(), 0, 3300);
	}

	private synchronized void stopDamageTask()
	{
		if (task != null)
		{
			task.cancel(false);
			task = null;
		}
	}

	private class DamageTask implements Runnable
	{
		public void run()
		{
			for (L2Character cha : getCharactersInside().values())
			{
				if (_hpDamage > 0)
					cha.reduceCurrentHp(_hpDamage, null);
				if (_mpDamage > 0)
					cha.reduceCurrentMp(_mpDamage);
			}
		}
	}
}
