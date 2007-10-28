/**
 * 
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
public class SiegeBattlefieldZone extends DefaultZone
{

	private int castleId = 0;
	private Castle castle = null;

	/**
	 * @param zone
	 */
	public SiegeBattlefieldZone(IZone zone)
	{
		super(zone);

		try
		{
			castleId = zone.getSettings().getInteger("castleId");
		}
		catch (IllegalArgumentException ia)
		{}

		// Register battlefield to the correct castle
		if(castleId > 0)
			castle = CastleManager.getInstance().getCastleById(castleId);
	}

	@Override
	public void onEnter(L2Character character)
	{
		super.onEnter(character);

		if(character instanceof L2PcInstance)
		{
			int siegeState = 0;
			L2PcInstance pc = (L2PcInstance) character;
			
			if(castle != null && castle.getSiege() != null)
			{
				if(castle.getSiege().getIsInProgress())
				{
					character.setInZone(ZoneType.Arena); // set PvP state on active siege

					if(pc.getClan() != null)
					{
						if(castle.getSiege().getAttackerClan(pc.getClan()) != null)
						{
							siegeState = 1; // attacker
						}
						else if(castle.getSiege().getDefenderClan(pc.getClan()) != null)
						{
							siegeState = 2; // defender
						}
					}
					
			        // if this is a castle that is currently being sieged, and the rider is NOT a castle owner
			        // he cannot land.
			        // castle owner is the leader of the clan that owns the castle where the pc is
					if (pc.getMountType() == 2)
					{
						if (!(pc.getClan() != null && pc.getClanId() == castle.getOwnerId() && pc.getClan().getLeaderId() == pc.getObjectId()))
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
		
		character.setInsideSiege(castleId);
	}

	@Override
	public void onExit(L2Character character)
	{
		super.onExit(character);

		character.setOutZone(ZoneType.Arena);
		character.setOutZone(ZoneType.NoLanding);
		((L2PcInstance) character).setSiegeState(0);
		character.setInsideSiege(0);
	}

}
