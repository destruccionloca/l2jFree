/*
 * This program is free software; you can redistribute it and/or modify
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
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.zone.IZone;
import net.sf.l2j.gameserver.model.zone.ZoneEnum.ZoneType;

/**
 * @author G1ta0
 *
 */
public class SiegeBattleFieldZone extends DefaultZone
{

	private int castleId = 0;
	private Castle castle = null;

	/**
	 * @param zone
	 */
	public SiegeBattleFieldZone(IZone zone)
	{
		super(zone);
	}

	/**
	 * Return Castle that this siege zone belongs to
	 * @return Castle
	 */
	protected Castle getCastle()
	{
		// if castle id not initialized, get id from zone settings
		if(castleId == 0)
			try
			{
				castleId = getZone().getSettings().getInteger("castleId");
			}
			catch (IllegalArgumentException ia)
			{}

		if(castleId > 0 && castle == null)
			castle = CastleManager.getInstance().getCastleById(castleId);

		return castle;
	}

	/**
	 * Check and return right siege state for player
	 * @param pc - player
	 * @return is siege state set
	 */
	protected int checkForSiegeState(L2PcInstance pc)
	{
		int siegeState = 0;

		if(getCastle() != null && getCastle().getSiege() != null)
		{
			if(getCastle().getSiege().getIsInProgress())
			{
				if(pc.getClan() != null && pc.getSiegeState() != 0)
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
				else
					siegeState = pc.getSiegeState();
			}
		}

		return siegeState;
	}

	protected boolean checkForFlyZone(L2PcInstance pc)
	{
		// if this is a castle that is currently being sieged, and the rider is NOT a castle owner
		// he cannot land.
		// castle owner is the leader of the clan that owns the castle where the pc is
		if(getCastle() != null && pc.getMountType() == 2)
		{
			if(!(pc.getClan() != null && 
				 pc.getClanId() == getCastle().getOwnerId() && 
				 pc.getClan().getLeaderId() == pc.getObjectId())){ return false; }
		}

		return true;
	}

	@Override
	public void onEnter(L2Character character)
	{
		super.onEnter(character);

		if(getCastle() == null) return;

		if(character instanceof L2PcInstance)
		{

			L2PcInstance pc = (L2PcInstance) character;

			// check if siege now in progress
			if(getCastle() != null && 
			   getCastle().getSiege() != null && 
			   getCastle().getSiege().getIsInProgress())
			{
				pc.setInZone(ZoneType.Arena, getZone());      // set PvP state in active siege zone
			}

			if(checkForFlyZone(pc))
				pc.setInZone(ZoneType.NoLanding, getZone()); // set no landing zone
		}

		character.setInsideSiegeCastleId(getCastle().getCastleId()); // set current sieged castle id
	}

	@Override
	public void onMove(L2Character character)
	{
		super.onMove(character);

		if(character instanceof L2PcInstance)
		{
			L2PcInstance pc = (L2PcInstance) character;

			// check if siege already done
			if(getCastle() != null && 
			   getCastle().getSiege() != null && 
			   pc.getSiegeState() == 0 &&
			   pc.isInsideZone(ZoneType.Arena))
			{
				pc.setOutZone(ZoneType.Arena, getZone()); // remove PvP state
			}
		}
	}

	@Override
	public void onExit(L2Character character)
	{
		super.onExit(character);

		if(getCastle() == null) return;
		
		if(character instanceof L2PcInstance)
		{
			L2PcInstance pc = (L2PcInstance) character;
			
			pc.setOutZone(ZoneType.Arena, getZone()); // remove PvP state
			pc.setOutZone(ZoneType.NoLanding, getZone()); // remove no landing state
		}
		
		character.setInsideSiegeCastleId(0);
	}

}
