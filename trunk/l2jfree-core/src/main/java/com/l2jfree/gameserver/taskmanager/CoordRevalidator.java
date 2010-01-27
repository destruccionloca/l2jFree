package com.l2jfree.gameserver.taskmanager;

import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.instance.L2AirShipInstance;
import com.l2jfree.gameserver.model.actor.instance.L2BoatInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.serverpackets.PartyMemberPosition;

/**
 * Used to revalidate/update/broadcast/execute tasks depending on current coordinates.<br>
 * <br>
 * The tasks gets triggered by the change of coordinates, but do not require instant execution.
 * 
 * @author NB4L1
 */
public final class CoordRevalidator extends AbstractFIFOPeriodicTaskManager<L2Object>
{
	private static final class SingletonHolder
	{
		private static final CoordRevalidator INSTANCE = new CoordRevalidator();
	}
	
	public static CoordRevalidator getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	protected CoordRevalidator()
	{
		super(500);
	}
	
	@Override
	protected void callTask(L2Object obj)
	{
		if (obj instanceof L2Character && obj.isVisible())
		{
			final L2Character cha = (L2Character)obj;
			
			cha.getKnownList().updateKnownObjects();
			
			cha.revalidateZone(true);
			
			if (cha instanceof L2BoatInstance)
			{
				((L2BoatInstance)cha).updatePeopleInTheBoat(cha.getX(), cha.getY(), cha.getZ());
			}
			else if (cha instanceof L2AirShipInstance)
			{
				((L2AirShipInstance)cha).updatePeopleInTheAirShip(cha.getX(), cha.getY(), cha.getZ());
			}
			
			if (cha instanceof L2PcInstance)
			{
				final L2PcInstance player = (L2PcInstance)cha;
				
				if (player.getParty() != null)
					player.getParty().broadcastToPartyMembers(player, new PartyMemberPosition(player));
			}
		}
	}
	
	@Override
	protected String getCalledMethodName()
	{
		return "revalidateCoords()";
	}
}
