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

import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.zone.IZone;

/**
 * @author G1ta0
 *
 */
public class CastleAreaZone extends DefaultZone
{

	private int castleId = 0;
	private Castle castle = null;

	/**
	 * @param zone
	 */
	public CastleAreaZone(IZone zone)
	{
		super(zone);

	}

	/**
	 * Return Castle that this zone belongs to
	 * @return Castle
	 */
	protected Castle getCastle()
	{
		if(castleId == 0 || castle == null)
		{

			try
			{
				castleId = getZone().getSettings().getInteger("castleId");
				castle = CastleManager.getInstance().getCastleById(castleId);
			}
			catch (IllegalArgumentException ia)
			{}
		}
		
		return castle;
	}

	@Override
	public void onEnter(L2Character character)
	{
		super.onEnter(character);

		if (getCastle() == null) return;
		
		character.setInsideCastle(getCastle().getCastleId());
	}

	@Override
	public void onExit(L2Character character)
	{
		super.onExit(character);

		character.setInsideCastle(0);
	}

}
