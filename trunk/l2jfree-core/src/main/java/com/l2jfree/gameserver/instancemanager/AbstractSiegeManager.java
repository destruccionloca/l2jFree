package com.l2jfree.gameserver.instancemanager;

import com.l2jfree.Config;
import com.l2jfree.gameserver.SevenSigns;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.entity.AbstractSiege;
import com.l2jfree.gameserver.model.entity.Siege;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;

public abstract class AbstractSiegeManager
{
	protected abstract AbstractSiege getSiege(L2Object activeObject);
	
	public final boolean checkIfOkToUseStriderSiegeAssault(L2PcInstance player, L2Character target, boolean isCheckOnly)
	{
		// Get siege battleground
		final AbstractSiege siege = getSiege(player);
		
		final SystemMessage sm;
		
		if (siege == null)
		{
			sm = SystemMessageId.YOU_ARE_NOT_IN_SIEGE.getSystemMessage();
		}
		else if (!siege.getIsInProgress())
		{
			sm = SystemMessageId.ONLY_DURING_SIEGE.getSystemMessage();
		}
		else if (siege.getAttackerClan(player.getClan()) == null)
		{
			sm = SystemMessage.sendString("You must be registered as attacker in order to do this.");
		}
		else if (!(target instanceof L2DoorInstance))
		{
			sm = SystemMessageId.TARGET_IS_INCORRECT.getSystemMessage();
		}
		else if (!player.isRidingStrider() && !player.isRidingRedStrider())
		{
			sm = SystemMessage.sendString("You must ride a strider in order to do this.");
		}
		else
			return true;
		
		if (!isCheckOnly)
			player.sendPacket(sm);
		return false;
	}
	
	public final boolean checkIfOkToSummon(L2PcInstance player, boolean isCheckOnly)
	{
		// Get siege battleground
		final AbstractSiege siege = getSiege(player);
		
		final SystemMessage sm;
		
		if (siege == null)
		{
			sm = SystemMessageId.YOU_ARE_NOT_IN_SIEGE.getSystemMessage();
		}
		else if (!siege.getIsInProgress())
		{
			sm = SystemMessageId.ONLY_DURING_SIEGE.getSystemMessage();
		}
		else if (siege.getAttackerClan(player.getClan()) == null)
		{
			sm = SystemMessage.sendString("You must be registered as attacker in order to do this.");
		}
		else if (siege instanceof Siege && ((Siege)siege).getCastle().getOwnerId() > 0
				&& SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE) == SevenSigns.CABAL_DAWN)
		{
			sm = SystemMessageId.SEAL_OF_STRIFE_FORBIDS_SUMMONING.getSystemMessage();
		}
		else
			return true;
		
		if (!isCheckOnly)
			player.sendPacket(sm);
		return false;
	}
	
	public final boolean checkIfOkToPlaceFlag(L2PcInstance player, boolean isCheckOnly)
	{
		// Get siege battleground
		final AbstractSiege siege = getSiege(player);
		
		final SystemMessage sm;
		
		if (siege == null)
		{
			sm = SystemMessageId.YOU_ARE_NOT_IN_SIEGE.getSystemMessage();
		}
		else if (!siege.getIsInProgress())
		{
			sm = SystemMessageId.ONLY_DURING_SIEGE.getSystemMessage();
		}
		else if (siege.getAttackerClan(player.getClan()) == null)
		{
			sm = SystemMessage.sendString("You must be registered as attacker in order to do this.");
		}
		else if (siege.getAttackerClan(player.getClan()).getNumFlags() >= Config.SIEGE_FLAG_MAX_COUNT)
		{
			sm = SystemMessageId.NOT_ANOTHER_HEADQUARTERS.getSystemMessage();
		}
		else if (!player.isClanLeader())
		{
			sm = SystemMessage.sendString("You must be the clan leader in order to do this.");
		}
		else if (!siege.getSiegeable().checkIfInZoneHeadQuarters(player))
		{
			sm = SystemMessageId.NOT_SET_UP_BASE_HERE.getSystemMessage(); // message?
		}
		else
			return true;
		
		if (!isCheckOnly)
			player.sendPacket(sm);
		return false;
	}
}
