/**
 * 
 */
package net.sf.l2j.gameserver.handler.zonehandlers;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.base.Race;
import net.sf.l2j.gameserver.model.zone.IZone;
import net.sf.l2j.gameserver.model.zone.ZoneEnum.ZoneType;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * @author G1ta0
 *
 */
public class MotherTreeZone extends DefaultZone
{

	/**
	 * @param zone
	 */
	public MotherTreeZone(IZone zone)
	{
		super(zone);
	}

	@Override
	public void onEnter(L2Character character)
	{
		if(character instanceof L2PcInstance)
		{
			L2PcInstance pc = (L2PcInstance) character;

			if(pc.getRace() != Race.elf)
				return;

			if(pc.isInParty())
				for(L2PcInstance member : pc.getParty().getPartyMembers())
					if(member.getRace() != Race.elf)
						// if player is in party with a non-elven race Mother Tree effect is cancelled
						return;
		}
		super.onEnter(character);
	}

	@Override
	public void onExit(L2Character character)
	{
		super.onExit(character);
	}
}
