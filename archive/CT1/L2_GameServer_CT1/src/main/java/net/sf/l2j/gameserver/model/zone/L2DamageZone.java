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
package net.sf.l2j.gameserver.model.zone;

import java.util.concurrent.ScheduledFuture;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class L2DamageZone extends L2DefaultZone
{
	private ScheduledFuture<?> task;

	@Override
	protected void onEnter(L2Character character)
	{
		character.setInsideZone(FLAG_DANGER, true);

		super.onEnter(character);

		if (_damage != 0 && getCharactersInside().size() > 0)
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
		if (task != null)
			return;
		task = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new DamageTask(), 1000, 1000);
	}

	private synchronized void stopDamageTask()
	{
		if (task != null)
			task.cancel(false);
		task = null;
	}

	private class DamageTask implements Runnable
	{
		public void run()
		{
			for (L2Character cha : getCharactersInside().values())
			{
				cha.reduceCurrentHp(_damage, cha);
			}
		}
	}
}
