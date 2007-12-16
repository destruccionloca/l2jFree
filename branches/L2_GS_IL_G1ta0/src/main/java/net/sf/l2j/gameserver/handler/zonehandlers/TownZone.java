/**
 * 
 */
package net.sf.l2j.gameserver.handler.zonehandlers;

import net.sf.l2j.Config;
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
public class TownZone extends DefaultZone
{

	private int castleId = 0; // not initialized
	private Castle castle = null;
	
	/**
	 * @param zone
	 */
	public TownZone(IZone zone)
	{
		super(zone);
	}

	/**
	 * Return Castle that Town belongs to
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
	
	@Override
	public void onEnter(L2Character character)
	{
		super.onEnter(character);
		
		if(character instanceof L2PcInstance)
		{
			L2PcInstance pc = (L2PcInstance) character;
			ZoneType zoneType = ZoneType.Peace; // 0 = Peace all the time
       		//   1 = PVP During Siege for siege participants
			if (Config.ZONE_TOWN == 1 && checkForSiegeState(pc) != 0 ) 
				zoneType = ZoneType.Arena; else
			if (Config.ZONE_TOWN == 2) zoneType = ZoneType.Arena; // 2 = PvP all the time
			
			pc.setInZone(zoneType, getZone());
		}
		
		if (getCastle() != null)
			character.setInsideTownCastleId(getCastle().getCastleId());
	}

	@Override
	public void onMove(L2Character character)
	{
		super.onMove(character);
		
		if (Config.ZONE_TOWN != 1) return;
				
		if(character instanceof L2PcInstance)
		{
			L2PcInstance pc = (L2PcInstance) character;

			// check if siege already done
			if(checkForSiegeState(pc) == 0)
			{
				pc.setOutZone(ZoneType.Arena, getZone()); // remove PvP state from zone
			}
		}
		
	}
	
	@Override
	public void onExit(L2Character character)
	{
		super.onExit(character);
		
		if(character instanceof L2PcInstance)
		{
			character.setOutZone(ZoneType.Arena, getZone());
			character.setOutZone(ZoneType.Peace, getZone());
		}
		
		character.setInsideTownCastleId(0);
	}
}
