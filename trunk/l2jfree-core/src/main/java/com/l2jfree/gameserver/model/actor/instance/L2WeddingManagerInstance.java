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

import com.l2jfree.Config;
import com.l2jfree.gameserver.Announcements;
import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.ai.CtrlIntention;
import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.instancemanager.CoupleManager;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.L2Npc;
import com.l2jfree.gameserver.model.entity.Couple;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jfree.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;

/**
 * @author evill33t & squeezed
 */
public class L2WeddingManagerInstance extends L2Npc
{
	public L2WeddingManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		if (!canTarget(player)) return;

		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);
		}
		else
		{
			// Calculate the distance between the L2PcInstance and the L2NpcInstance
			if (!canInteract(player))
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

    private void showMessageWindow(L2PcInstance player)
    {
        String filename = "data/html/wedding/start.htm";
        String replace = "";
        
        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
        html.setFile(filename);
        html.replace("%objectId%", String.valueOf(getObjectId()));
        html.replace("%replace%", replace);
        html.replace("%npcname%", getName());
        player.sendPacket(html);
    }
    
    @Override
    public synchronized void onBypassFeedback(final L2PcInstance player, String command)
    {
        // Standard msg
        String filename = "data/html/wedding/start.htm";
        String replace = "";
        
        // If player has no partner
        if(player.getPartnerId() == 0)
        {
            filename = "data/html/wedding/nopartner.htm";
            sendHtmlMessage(player, filename, replace);
            return;
        }

        L2Object obj = L2World.getInstance().findObject(player.getPartnerId());
        final L2PcInstance ptarget = obj instanceof L2PcInstance ? (L2PcInstance) obj : null;
        // Partner online ?
        if(ptarget == null || ptarget.isOnline() == 0)
        {
            filename = "data/html/wedding/notfound.htm";
            sendHtmlMessage(player, filename, replace);
            return;
        }

        // Already married ?
        if(player.isMaried())
        {
            filename = "data/html/wedding/already.htm";
            sendHtmlMessage(player, filename, replace);
            return;
        }
        else if (player.isMaryAccepted())
        {
            filename = "data/html/wedding/waitforpartner.htm";
            sendHtmlMessage(player, filename, replace);
            return;
        }
        else if (command.startsWith("AcceptWedding"))
        {
            // Accept the wedding request
            player.setMaryAccepted(true);
            Couple couple = CoupleManager.getInstance().getCouple(player.getCoupleId());
            couple.marry();

            // Messages to the couple
            player.sendMessage("Congratulations, you are married!");
            player.setMaried(true);
            player.setMaryRequest(false);
            ptarget.sendMessage("Congratulations, you are married!");
            ptarget.setMaried(true);
            ptarget.setMaryRequest(false);
            
            if(Config.WEDDING_GIVE_CUPID_BOW)
            {
            	// give cupid's bows to couple's
            	player.addItem("Cupids Bow", 9140, 1, player, true, true); // give cupids bow
            	player.getInventory().updateDatabase(); // update database
            	ptarget.addItem("Cupids Bow", 9140, 1, ptarget, true, true); // give cupids bow
            	ptarget.getInventory().updateDatabase(); // update database
                // Refresh client side skill lists
                player.sendSkillList();
                ptarget.sendSkillList();
            }

            // Wedding march
            MagicSkillUse MSU = new MagicSkillUse(player, player, 2230, 1, 1, 0);
            player.broadcastPacket(MSU);
            MSU = new MagicSkillUse(ptarget, ptarget, 2230, 1, 1, 0);
            ptarget.broadcastPacket(MSU);
            
            // Fireworks
            L2Skill skill = SkillTable.getInstance().getInfo(2025,1);
            if (skill != null)
            {
                MSU = new MagicSkillUse(player, player, 2025, 1, 1, 0);
                player.sendPacket(MSU);
                player.broadcastPacket(MSU);
                player.useMagic(skill, false, false);

                MSU = new MagicSkillUse(ptarget, ptarget, 2025, 1, 1, 0);
                ptarget.sendPacket(MSU);
                ptarget.broadcastPacket(MSU);
                ptarget.useMagic(skill, false, false);
            }
            
            Announcements.getInstance().announceToAll("Congratulations, "+player.getName()+" and "+ptarget.getName()+" have married!");
            
            MSU = null;
            
            filename = "data/html/wedding/accepted.htm";
            replace = ptarget.getName();
            sendHtmlMessage(ptarget, filename, replace);
			
			if (Config.WEDDING_HONEYMOON_PORT)
			{
				// Wait a little for all effects, and then go on honeymoon
				ThreadPoolManager.getInstance().schedule(new Runnable() {
					@Override
					public void run()
					{
						// Port both players to Fantasy Isle for happy time
						player.teleToLocation(-56641, -56345, -2005);
						ptarget.teleToLocation(-56641, -56345, -2005);
					}
				}, 10000);
			}
			return;
		}
        else if (command.startsWith("DeclineWedding"))
        {
            player.setMaryRequest(false);
            ptarget.setMaryRequest(false);
            player.setMaryAccepted(false);
            ptarget.setMaryAccepted(false);
            player.sendMessage("You declined");
            ptarget.sendMessage("Your partner declined");
            replace = ptarget.getName();
            filename = "data/html/wedding/declined.htm";
            sendHtmlMessage(ptarget, filename, replace);
            return;
        }
        else if (player.isMary())
        {
            // Check for formalwear
            if(Config.WEDDING_FORMALWEAR && !player.isWearingFormalWear())
            {
                filename = "data/html/wedding/noformal.htm";
                sendHtmlMessage(player, filename, replace);
                return;
            }
            filename = "data/html/wedding/ask.htm";
            player.setMaryRequest(false);
            ptarget.setMaryRequest(false);
            replace = ptarget.getName();
            sendHtmlMessage(player, filename, replace);
            return;
        }
        else if (command.startsWith("AskWedding"))
        {
            // Check for formalwear
            if(Config.WEDDING_FORMALWEAR && !player.isWearingFormalWear())
            {
                filename = "data/html/wedding/noformal.htm";
                sendHtmlMessage(player, filename, replace);
                return;
            }
            else if(player.getAdena()<Config.WEDDING_PRICE)
            {
                filename = "data/html/wedding/adena.htm";
                replace = String.valueOf(Config.WEDDING_PRICE);
                sendHtmlMessage(player, filename, replace);
                return;
            }
            else
            {
                player.setMaryAccepted(true);
                ptarget.setMaryRequest(true);
                replace = ptarget.getName();
                filename = "data/html/wedding/requested.htm";
                player.getInventory().reduceAdena("Wedding", Config.WEDDING_PRICE, player, player.getLastFolkNPC());
                sendHtmlMessage(player, filename, replace);
                return;
            }
        }

        sendHtmlMessage(player, filename, replace);
    }

    private void sendHtmlMessage(L2PcInstance player, String filename, String replace)
    {
        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
        html.setFile(filename);
        html.replace("%objectId%", String.valueOf(getObjectId()));
        html.replace("%replace%", replace);
        html.replace("%npcname%", getName());
        player.sendPacket(html);
    }
}