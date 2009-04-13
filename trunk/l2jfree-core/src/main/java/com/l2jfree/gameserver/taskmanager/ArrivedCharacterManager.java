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

import com.l2jfree.gameserver.GameTimeController;
import com.l2jfree.gameserver.ai.CtrlEvent;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.actor.instance.L2BoatInstance;

/**
 * @author NB4L1
 */
public final class ArrivedCharacterManager extends AbstractFIFOPeriodicTaskManager<L2Character>
{
	private static ArrivedCharacterManager _instance;
	
	public static ArrivedCharacterManager getInstance()
	{
		if (_instance == null)
			_instance = new ArrivedCharacterManager();
		
		return _instance;
	}
	
	private ArrivedCharacterManager()
	{
		super(GameTimeController.MILLIS_IN_TICK);
	}
	
	@Override
	void callTask(L2Character cha)
	{
		cha.getKnownList().updateKnownObjects();
		
		if (cha instanceof L2BoatInstance)
			((L2BoatInstance)cha).evtArrived();
		
		if (cha.hasAI())
			cha.getAI().notifyEvent(CtrlEvent.EVT_ARRIVED);
	}
}
