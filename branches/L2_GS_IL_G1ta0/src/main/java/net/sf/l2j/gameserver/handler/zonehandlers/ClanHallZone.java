/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.handler.zonehandlers;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.serverpackets.ClanHallDecoration;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.model.zone.IZone;

/**
 * @author G1ta0
 *
 */
public class ClanHallZone extends DefaultZone
{

	private int clanHallId = 0;
	private ClanHall clanHall = null;
	/**
	 * @param zone
	 */
	public ClanHallZone(IZone zone)
	{
		super(zone);
	}
	
	private ClanHall getClanHall()
	{
		if (clanHallId == 0 || clanHall == null)
		{
			try
			{
				clanHallId = getZone().getSettings().getInteger("clanHallId");
				clanHall = ClanHallManager.getInstance().getClanHallById(clanHallId);
			}
			catch (IllegalArgumentException ia)
			{
			}
		}
		
		return clanHall;
	}
	
	@Override
	public void onEnter(L2Character character)
	{
		super.onEnter(character);
		 
		if (getClanHall() == null) return;
		
		character.setInsideClanHall(getClanHall().getClanHallId());
		
		if (character instanceof L2PcInstance)
		{
			// Send decoration packet
			ClanHallDecoration deco = new ClanHallDecoration(getClanHall());
			((L2PcInstance)character).sendPacket(deco);
		}
	}
	
	@Override
	public void onExit(L2Character character)
	{
		super.onExit(character);
		
		character.setInsideClanHall(0);
	}

}
