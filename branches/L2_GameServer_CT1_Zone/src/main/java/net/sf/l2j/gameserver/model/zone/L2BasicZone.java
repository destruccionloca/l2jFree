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

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.L2Character;

public abstract class L2BasicZone extends L2Zone
{
	@Override
	protected void onEnter(L2Character character)
	{
		if(_onEnterMsg != null && character instanceof L2PcInstance)
			character.sendPacket(_onEnterMsg);
		
		if(_abnormal > 0)
			character.startAbnormalEffect(_abnormal);
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		if(_onExitMsg != null && character instanceof L2PcInstance)
			character.sendPacket(_onExitMsg);

		if(_abnormal > 0)
			character.stopAbnormalEffect(_abnormal);
	}

	@Override
	public final void onDieInside(L2Character character){}
	
	@Override
	public final void onReviveInside(L2Character character){}
}
