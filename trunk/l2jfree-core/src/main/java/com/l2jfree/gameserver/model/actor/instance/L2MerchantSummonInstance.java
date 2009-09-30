/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jfree.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import com.l2jfree.gameserver.ai.CtrlIntention;
import com.l2jfree.gameserver.ai.L2CharacterAI;
import com.l2jfree.gameserver.datatables.TradeListTable;
import com.l2jfree.gameserver.model.L2Party;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2TradeList;
import com.l2jfree.gameserver.model.L2WorldRegion;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Merchant;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.BuyList;
import com.l2jfree.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfree.gameserver.network.serverpackets.SellList;
import com.l2jfree.gameserver.skills.l2skills.L2SkillSummon;
import com.l2jfree.gameserver.taskmanager.SQLQueue;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;

/**
 * @author Kerberos
 */
public class L2MerchantSummonInstance extends L2SummonInstance implements L2Merchant
{
	public L2MerchantSummonInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, L2SkillSummon skill)
	{
		super(objectId, template, owner, skill);
	}
	
	@Override
	public boolean hasAI()
	{
		return false;
	}
	
	@Override
	public L2CharacterAI getAI()
	{
		return null;
	}
	
    @Override
    public void deleteMe(L2PcInstance owner)
    {

    }

    @Override
    public void unSummon(L2PcInstance owner)
    {
    	if (isVisible())
    	{
			stopAllEffects();
	        L2WorldRegion oldRegion = getWorldRegion();
		    decayMe();
		    if (oldRegion != null) oldRegion.removeFromZones(this);
            getKnownList().removeAllKnownObjects();
	        setTarget(null);
	        SQLQueue.getInstance().run();
    	}
    }

    @Override
    public void setFollowStatus(boolean state)
    {
    	
    }
    
	@Override
	public boolean isAutoAttackable(L2Character attacker)
    {
		return false;
    }

    @Override
    public boolean isInvul()
    {
    	return true;
    }

    @Override
    public L2Party getParty()
    {
    	return null;
    }

    @Override
    public boolean isInParty()
    {
    	return false;
    }

    @Override
    public void useMagic(L2Skill skill, boolean forceUse, boolean dontMove)
    {

    }

    @Override
    public void doCast(L2Skill skill)
    {

    }

    @Override
    public boolean isInCombat()
    {
    	return false;
    }
	
	@Override
	public void onAction(L2PcInstance player)
	{
		if (player.isOutOfControl())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);
		}
		else
		{
			// Calculate the distance between the L2PcInstance and the L2NpcInstance
			if (!isInsideRadius(player, 150, false, false))
			{
				// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			}
			else
			{
				showMessageWindow(player);
			}
		}
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public int getMyTargetSelectedColor(L2PcInstance player)
	{
		return 0;
	}

	public void onBypassFeedback(L2PcInstance player, String command)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken(); // Get actual command

		if (actualCommand.equalsIgnoreCase("Buy"))
		{
			if (st.countTokens() < 1)
				return;

			int val = Integer.parseInt(st.nextToken());
			showBuyWindow(player, val);
		}
		else if (actualCommand.equalsIgnoreCase("Sell"))
		{
			showSellWindow(player);
		}
	}

	protected final void showBuyWindow(L2PcInstance player, int val)
	{
		double taxRate = 0;

		taxRate = 50;
		
		player.tempInventoryDisable();

		L2TradeList list = TradeListTable.getInstance().getBuyList(val);

		if (list != null && list.getNpcId() == getNpcId())
		{
			BuyList bl = new BuyList(list, player.getAdena(), taxRate);
			player.sendPacket(bl);
		}
		else
		{
			_log.warn("possible client hacker: " + player.getName() + " attempting to buy from GM shop! < Ban him!");
			_log.warn("buylist id:" + val);
		}

		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	protected final void showSellWindow(L2PcInstance player)
	{
		player.sendPacket(new SellList(player));
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	private void showMessageWindow(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		String filename = "data/html/merchant/"+getNpcId()+".htm";
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}
}
