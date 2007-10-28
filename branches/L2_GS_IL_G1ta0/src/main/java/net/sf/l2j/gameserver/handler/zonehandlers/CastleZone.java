/**
 * 
 */
package net.sf.l2j.gameserver.handler.zonehandlers;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.zone.IZone;

/**
 * @author G1ta0
 *
 */
public class CastleZone extends DefaultZone
{

	private int castleId = 0;
	/**
	 * @param zone
	 */
	public CastleZone(IZone zone)
	{
		super(zone);
		
		try
		{
			castleId = zone.getSettings().getInteger("castleId");
		}
		catch (IllegalArgumentException ia)
		{
		}
	}

	@Override
	public void onEnter(L2Character character)
	{
		super.onEnter(character);
		
		character.setInsideCastle(castleId);
	}
	
	@Override
	public void onExit(L2Character character)
	{
		super.onExit(character);
		
		character.setInsideCastle(0);
	}

}
