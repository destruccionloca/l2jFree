package com.l2jfree.gameserver.model.actor;

import com.l2jfree.gameserver.ai.CtrlIntention;
import com.l2jfree.gameserver.geodata.GeoData;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;

public abstract class L2Tower extends L2Npc
{
	protected L2Tower(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setIsInvul(false);
	}
	
	@Override
	public final boolean isAttackable()
	{
		// Attackable during siege by attacker only
		return getCastle() != null
			&& getCastle().getCastleId() > 0
			&& getCastle().getSiege().getIsInProgress();
	}
	
	@Override
	public final boolean isAutoAttackable(L2Character attacker)
	{
		// Attackable during siege by attacker only
		return attacker instanceof L2PcInstance
			&& getCastle() != null
			&& getCastle().getCastleId() > 0
			&& getCastle().getSiege().getIsInProgress()
			&& getCastle().getSiege().checkIsAttacker(((L2PcInstance)attacker).getClan());
	}
	
	@Override
	public final void onForcedAttack(L2PcInstance player)
	{
		onAction(player);
	}
	
	@Override
	public final void onAction(L2PcInstance player, boolean interact)
	{
		if (!canTarget(player))
			return;
		
		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);
			
			// Send a Server->Client packet StatusUpdate of the L2NpcInstance to the L2PcInstance to update its HP bar
			StatusUpdate su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.CUR_HP, (int)getStatus().getCurrentHp());
			su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
			player.sendPacket(su);
		}
		else if (interact)
		{
			if (isAutoAttackable(player) && Math.abs(player.getZ() - getZ()) < 100) // Less then max height difference, delete check when geo
			{
				if (GeoData.getInstance().canSeeTarget(player, this))
				{
					// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
				}
			}
		}
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
			return false;
		
		// TODO: now spawn another NPC which represents dead tower
		return true;
	}
}
