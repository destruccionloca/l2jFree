/**
 * 
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
public class WaterZone extends DefaultZone
{
	public WaterZone(IZone zone)
	{
		super(zone);
	}
	
	@Override
	public void onEnter(L2Character character)
	{
		if (character instanceof L2PcInstance && !character.isInsideZone(ZoneType.Water))
		{
			((L2PcInstance)character).startWaterTask();
		}
		
		super.onEnter(character);
	}
	
	@Override
	public void onExit(L2Character character)
	{
		super.onExit(character);
		
		if (character instanceof L2PcInstance)
		{
			((L2PcInstance)character).stopWaterTask();
		}
	}
}
