/**
 * 
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
	/**
	 * @param zone
	 */
	public ClanHallZone(IZone zone)
	{
		super(zone);
		
		try
		{
			clanHallId = zone.getSettings().getInteger("clanHallId");
		}
		catch (IllegalArgumentException ia)
		{
		}
	}
	
	@Override
	public void onEnter(L2Character character)
	{
		super.onEnter(character);
		
		character.setInsideClanHall(clanHallId);
		
		if (character instanceof L2PcInstance)
		{
			ClanHall clanHall = ClanHallManager.getInstance().getClanHall(clanHallId);
			if (clanHall == null) return;
			
			// Send decoration packet
			ClanHallDecoration deco = new ClanHallDecoration(clanHall);
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
