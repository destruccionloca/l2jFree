package com.l2jfree.gameserver.model.actor.status;

import com.l2jfree.gameserver.instancemanager.CCHManager;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.instance.L2CCHBossInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.entity.CCHSiege;

public final class CCHLeaderStatus extends AttackableStatus
{
	public CCHLeaderStatus(L2CCHBossInstance activeChar)
	{
		super(activeChar);
	}

	@Override
	public final void reduceHp0(double value, L2Character attacker, boolean awake, boolean isDOT,
			boolean isConsume)
	{
		if (!canReduceHp(value, attacker, awake, isDOT, isConsume))
			return;
		super.reduceHp0(value, attacker, awake, isDOT, isConsume);
		L2PcInstance player = attacker.getActingPlayer();
		if (player == null)
			return;
		CCHSiege siege = CCHManager.getInstance().getSiege(player.getClan());
		if (siege != null && siege.equals(getActiveChar().getSiege()))
			getActiveChar().getDamageTable().put(player.getClanId(), (int) value);
	}

	@Override
	public final L2CCHBossInstance getActiveChar()
	{
		return (L2CCHBossInstance) super.getActiveChar();
	}
}
