/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.instancemanager.CoupleManager;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.entity.Couple;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2WeddingManagerInstance extends L2NpcInstance
{
    /**
     * @author evill33t & squeezed
     */
    public L2WeddingManagerInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }
    
    public void onAction(L2PcInstance player)
    {
        player.sendPacket(new ActionFailed());
        player.setTarget(this);
        player.sendPacket(new MyTargetSelected(getObjectId(), -15));

        showMessageWindow(player);
    }
    
    private void showMessageWindow(L2PcInstance player)
    {
        String filename = "data/html/wedding/start.htm";
        String replace = "";
        
        NpcHtmlMessage html = new NpcHtmlMessage(1);
        html.setFile(filename);
        html.replace("%objectId%", String.valueOf(getObjectId()));
        html.replace("%replace%", replace);
        html.replace("%npcname%", getName());
        player.sendPacket(html);
    }
    
    public void onBypassFeedback(L2PcInstance player, String command)
    {
        // standard msg
        String filename = "data/html/wedding/start.htm";
        String replace = "";
        
        // if player has no partner
        if(player.getPartnerId()==0){
            filename = "data/html/wedding/nopartner.htm";
            this.sendHtmlMessage(player, filename, replace);
            return;
        }
        else
        {
            L2PcInstance ptarget = (L2PcInstance)L2World.getInstance().findObject(player.getPartnerId());
            // partner online ?
            if(ptarget==null || ptarget.isOnline()==0)
            {
                filename = "data/html/wedding/notfound.htm";
                this.sendHtmlMessage(player, filename, replace);
                return;
            }
            else
            {
                // already married ?
                if(player.isMaried())
                {
                    filename = "data/html/wedding/already.htm";
                    this.sendHtmlMessage(player, filename, replace);
                    return;
                }
                else if (player.isMaryAccepted())
                {
                    filename = "data/html/wedding/waitforpartner.htm";
                    this.sendHtmlMessage(player, filename, replace);
                    return;
                }
                else if (command.startsWith("AcceptWedding"))
                {
                    // accept the wedding request
                    player.setMaryAccepted(true);
                    Couple couple = CoupleManager.getInstance().getCouple(player.getCoupleId());
                    couple.marry();
                    
                    //messages to the couple
                    player.sendMessage("Congratulations you are married!");
                    player.setMaried(true);
                    player.setMaryRequest(false);
                    ptarget.sendMessage("Congratulations you are married!"); 
                    ptarget.setMaried(true);
                    ptarget.setMaryRequest(false);
                    
                    //wedding march
                    MagicSkillUser MSU = new MagicSkillUser(player, player, 2230, 1, 1, 0);
                    player.broadcastPacket(MSU);
                    MSU = new MagicSkillUser(ptarget, ptarget, 2230, 1, 1, 0);
                    ptarget.broadcastPacket(MSU);
                    
                    // fireworks
                    L2Skill skill = SkillTable.getInstance().getInfo(2025,1);
                    if (skill != null) 
                    {
                        MSU = new MagicSkillUser(player, player, 2025, 1, 1, 0);
                        player.sendPacket(MSU);
                        player.broadcastPacket(MSU);
                        player.useMagic(skill, false, false);

                        MSU = new MagicSkillUser(ptarget, ptarget, 2025, 1, 1, 0);
                        ptarget.sendPacket(MSU);
                        ptarget.broadcastPacket(MSU);
                        ptarget.useMagic(skill, false, false);

                    }
                    
                    Announcements.getInstance().announceToAll("Gratulations, "+player.getName()+" and "+ptarget.getName()+" has married.");            
                    
                    MSU = null;
                    
                    filename = "data/html/wedding/accepted.htm";
                    replace = ptarget.getName();
                    this.sendHtmlMessage(ptarget, filename, replace);
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
                    this.sendHtmlMessage(ptarget, filename, replace);
                    return;
                }
                else if (player.isMaryRequest())
                {
                    // check for formalwear
                    if(Config.WEDDING_FORMALWEAR && !player.isWearingFormalWear())
                    {
                        filename = "data/html/wedding/noformal.htm";
                        this.sendHtmlMessage(player, filename, replace);
                        return;
                    }
                    filename = "data/html/wedding/ask.htm";
                    player.setMaryRequest(false);
                    ptarget.setMaryRequest(false);
                    replace = ptarget.getName();
                    this.sendHtmlMessage(player, filename, replace);
                    return;
                }  
                else if (command.startsWith("AskWedding"))
                {
                    // check for formalwear
                    if(Config.WEDDING_FORMALWEAR && !player.isWearingFormalWear())
                    {
                        filename = "data/html/wedding/noformal.htm";
                        this.sendHtmlMessage(player, filename, replace);
                        return;
                    }
                    else if(player.getAdena()<Config.WEDDING_PRICE)
                    {
                        filename = "data/html/wedding/adena.htm";
                        replace = String.valueOf(Config.WEDDING_PRICE);
                        this.sendHtmlMessage(player, filename, replace);
                        return;
                    }
                    else
                    {
                        player.setMaryAccepted(true);
                        ptarget.setMaryRequest(true);
                        replace = ptarget.getName();
                        filename = "data/html/wedding/requested.htm";
                        player.getInventory().reduceAdena("Wedding", Config.WEDDING_PRICE, player, player.getLastFolkNPC());                       
                        this.sendHtmlMessage(player, filename, replace);
                        return;
                    }                    
                } 
            }
        }                
        this.sendHtmlMessage(player, filename, replace);
    } 

    private void sendHtmlMessage(L2PcInstance player, String filename, String replace)
    {
        NpcHtmlMessage html = new NpcHtmlMessage(1);
        html.setFile(filename);
        html.replace("%objectId%", String.valueOf(getObjectId()));
        html.replace("%replace%", replace);
        html.replace("%npcname%", getName());
        player.sendPacket(html);
    }
}
