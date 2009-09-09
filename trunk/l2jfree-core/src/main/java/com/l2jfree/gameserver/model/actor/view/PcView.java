package com.l2jfree.gameserver.model.actor.view;

import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author NB4L1
 */
public final class PcView extends CharView<L2PcInstance> implements UniversalCharView
{
	public PcView(L2PcInstance activeChar)
	{
		super(activeChar);
	}
	
	@Override
	protected void refreshImpl()
	{
		super.refreshImpl();
		
		float moveMultiplier = _activeChar.getStat().getMovementSpeedMultiplier();
		
		_runSpd = (int) (_activeChar.getRunSpeed() / moveMultiplier);
		_walkSpd = (int) (_activeChar.getStat().getWalkSpeed() / moveMultiplier);
	}
}
