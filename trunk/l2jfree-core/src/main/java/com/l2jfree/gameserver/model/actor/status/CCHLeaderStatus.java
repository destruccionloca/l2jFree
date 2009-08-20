package com.l2jfree.gameserver.model.actor.status;

import com.l2jfree.gameserver.instancemanager.CCHManager;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.instance.L2CCHBossInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.entity.CCHSiege;

/**
 * @author savormix
 */
public final class CCHLeaderStatus extends AttackableStatus
{
	public CCHLeaderStatus(L2CCHBossInstance activeChar)
	{
		super(activeChar);
	}
	
	@Override
	void reduceHp0(double value, L2Character attacker, boolean awake, boolean isDOT, boolean isConsume)
	{
		super.reduceHp0(value, attacker, awake, isDOT, isConsume);
		
		final L2PcInstance player = L2Object.getActingPlayer(attacker);
		if (player == null)
			return;
		
		CCHSiege siege = CCHManager.getInstance().getSiege(player.getClan());
		if (siege != null && siege.equals(getActiveChar().getSiege()))
		{
			Integer previousValue = getActiveChar().getDamageTable().get(player.getClanId());
			int newValue = (previousValue == null ? 0 : previousValue.intValue()) + (int)value;
			
			getActiveChar().getDamageTable().put(player.getClanId(), newValue);
		}
	}
	
	@Override
	public L2CCHBossInstance getActiveChar()
	{
		return (L2CCHBossInstance)_activeChar;
	}
}
