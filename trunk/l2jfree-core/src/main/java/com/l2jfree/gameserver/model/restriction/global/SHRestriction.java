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
package com.l2jfree.gameserver.model.restriction.global;

import com.l2jfree.Config;
import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Npc;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.entity.events.SH;
import com.l2jfree.gameserver.model.entity.events.TvTInstanced.*;
import com.l2jfree.gameserver.network.serverpackets.PlaySound;

final class SHRestriction extends AbstractFunEventRestriction
{

	@Override
	boolean allowInterference()
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	boolean allowPotions()
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	boolean allowSummon()
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	boolean isInFunEvent(L2PcInstance player)
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	boolean started()
	{
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	public boolean onAction(L2Npc npc, L2PcInstance activeChar)
	{
		if (npc._isEventMobSH)
		{
			SH.showEventHtml(activeChar, String.valueOf(npc.getObjectId()));
			return true;
		}
		
		return false;
	}
}
