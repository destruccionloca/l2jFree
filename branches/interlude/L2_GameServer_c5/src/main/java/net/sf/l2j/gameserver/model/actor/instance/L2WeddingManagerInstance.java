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
import net.sf.l2j.gameserver.SkillTable;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.entity.Couple;
import net.sf.l2j.gameserver.instancemanager.CoupleManager;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;

public class L2WeddingManagerInstance extends L2FolkInstance
{
    /**
     * @author evill33t
     */
    public L2WeddingManagerInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }
    
    public void onAction(L2PcInstance player)
    {
        player.setLastFolkNPC(this);
        super.onAction(player);
    }
    
    public void onBypassFeedback(L2PcInstance player, String command)
    {
        // standard msg
        String filename = "data/html/wedding/start.htm";
        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
        String replace = "";
        
        // if player has no partner
        if(player.getPartnerId()==0)
            filename = "data/html/wedding/nopartner.htm";
        else
        {
            L2PcInstance ptarget = (L2PcInstance)L2World.getInstance().findObject(player.getPartnerId());
            // partner online ?
            if(ptarget==null)
                filename = "data/html/wedding/notfound.htm";
            else
            {
                // already married ?
                if(player.isMaried())
                    filename = "data/html/wedding/already.htm";
                else if (player.isMaryRequest())
                {
                    // check for formalwear TODO make this adjustable
                    if(player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST).getItemId()!=6408
                            || ptarget.getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST).getItemId()!=6408)
                        filename = "data/html/wedding/noformal.htm";
                    else
                        filename = "data/html/wedding/ask.htm";
                }
                else if (command.startsWith("AcceptWedding"))
                {
                    // accept the wedding request
                    filename = "data/html/wedding/accepted.htm";
                    player.setMaryAccepted(true);
                    // check if partner already confirmed
                    if(ptarget.isMaryAccepted())
                    {
                        Couple couple = CoupleManager.getInstance().getCouple(player.getCoupleId());
                        couple.marry();
                        
                        player.sendMessage("Gratulations you are married!");
                        player.setMaried(true);
                        player.setMaryRequest(false);
                        ptarget.sendMessage("Gratulations you are married!");
                        ptarget.setMaried(true);
                        ptarget.setMaryRequest(false);
                        
                        // fireworks
                        L2Skill skill = SkillTable.getInstance().getInfo(2025,1);
                        if (skill != null) 
                        {
                            MagicSkillUser MSU = new MagicSkillUser(player, player, 2025, 1, 1, 0);
                            player.sendPacket(MSU);
                            player.broadcastPacket(MSU);
                            player.useMagic(skill, false, false);

                            MSU = new MagicSkillUser(ptarget, ptarget, 2025, 1, 1, 0);
                            ptarget.sendPacket(MSU);
                            ptarget.broadcastPacket(MSU);
                            ptarget.useMagic(skill, false, false);

                        }
                        // TODO nice npc shouts
                    }
                }
                else if (command.startsWith("DeclineWedding"))
                {
                    filename = "data/html/wedding/declined.htm";
                    player.setMaryAccepted(false);
                    ptarget.setMaryAccepted(false);
                    player.sendMessage("You declined");
                    ptarget.sendMessage("Your partner declined");
                }
                else if (command.startsWith("AskWedding"))
                {
                    if(player.getAdena()<Config.WEDDING_PRICE)
                    {
                        filename = "data/html/wedding/adena.htm";
                        replace = String.valueOf(Config.WEDDING_PRICE);
                    }
                    else
                    {
                        filename = "data/html/wedding/requested.htm";
                        player.setMaryRequest(true);
                        ptarget.setMaryRequest(true);
                    }
                }
                replace = ptarget.getName();                
            }
        }
        
        html.replace("%replace%", replace);
        html.setFile(filename);
        player.sendPacket(html);
    }   
}