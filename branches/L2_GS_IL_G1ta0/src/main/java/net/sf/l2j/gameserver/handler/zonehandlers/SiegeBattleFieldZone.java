/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version. This
 * program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.handler.zonehandlers;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.zone.IZone;
import net.sf.l2j.gameserver.model.zone.ZoneEnum.ZoneType;

/**
 * @author G1ta0
 *
 */
public class SiegeBattleFieldZone extends CastleAreaZone
{

	/**
	 * @param zone
	 */
	public SiegeBattleFieldZone(IZone zone)
	{
		super(zone);
	}

	@Override
	public void onEnter(L2Character character)
	{
		super.onEnter(character);

		if(getCastle() == null)
			return;

		if(character instanceof L2PcInstance)
		{
			int siegeState = 0;
			L2PcInstance pc = (L2PcInstance) character;

			if(getCastle().getSiege() != null)
			{
				if(getCastle().getSiege().getIsInProgress())
				{
					character.setInZone(ZoneType.Arena); // set PvP state on active siege

					if(pc.getClan() != null)
					{
						if(getCastle().getSiege().getAttackerClan(pc.getClan()) != null)
						{
							siegeState = 1; // attacker
						}
						else if(getCastle().getSiege().getDefenderClan(pc.getClan()) != null)
						{
							siegeState = 2; // defender
						}
					}

					// if this is a castle that is currently being sieged, and the rider is NOT a castle owner
					// he cannot land.
					// castle owner is the leader of the clan that owns the castle where the pc is
					if(pc.getMountType() == 2)
					{
						if(!(pc.getClan() != null && pc.getClanId() == getCastle().getOwnerId() && pc.getClan().getLeaderId() == pc.getObjectId()))
						{
							character.setInZone(ZoneType.NoLanding);
						}
					}
				}
				else
					character.setOutZone(ZoneType.SiegeBattleField);
			}
			else
				character.setOutZone(ZoneType.SiegeBattleField);

			pc.setSiegeState(siegeState);
		}

		character.setInsideSiege(getCastle().getCastleId());
	}

	@Override
	public void onExit(L2Character character)
	{
		super.onExit(character);

		if(getCastle() == null)
			return;

		if(character instanceof L2PcInstance)
		{
			character.setOutZone(ZoneType.Arena);
			character.setOutZone(ZoneType.NoLanding);
			((L2PcInstance) character).setSiegeState(0);
			character.setInsideSiege(0);
		}
	}

}
