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

import javolution.text.TextBuilder;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.CharTemplateTable;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;


/**
 * This class ...
 * 
 * @version $Revision: 1.4.2.1.2.7 $ $Date: 2005/03/27 15:29:32 $
 */
public final class L2ClassMasterInstance extends L2FolkInstance
{
    //private final static Log _log = LogFactory.getLog(L2ClassMasterInstance.class.getName());
    
    /**
     * @param template
     */
    public L2ClassMasterInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }
    
    public void onAction(L2PcInstance player)
    {
        if (getObjectId() != player.getTargetId())
        {
            player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
            
            player.setTarget(this);
            player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
            // correct location
            player.sendPacket(new ValidateLocation(this));
        }
        else
        {
            if (!isInsideRadius(player, INTERACTION_DISTANCE, false, false))
            {
                player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
                return;
            }
            
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            TextBuilder sb = new TextBuilder();
            sb.append("<html><body>");
            sb.append("Buzz the Cat:<br>");
            sb.append("<br>");
            
            ClassId classId = player.getClassId();
            int level = player.getLevel();
            int lvl = classId.level();
            
            if ((level >= 20 && lvl == 0) || (level >= 40 && lvl == 1) || (level >= 76 && lvl == 2))
            {
                if ((lvl == 0 && Config.ALLOW_CLASS_MASTER_1) || (lvl == 1 && Config.ALLOW_CLASS_MASTER_2) || (lvl == 2 && Config.ALLOW_CLASS_MASTER_3))
                {
                    for (ClassId child : ClassId.values())
                        if (child.childOf(classId) && child.level() == lvl+1)
                            sb.append("<a action=\"bypass -h npc_" + getObjectId() + "_change_class " + (child.getId()) + "\">Advance to " + CharTemplateTable.getClassNameById(child.getId()) + "</a><br>");
                    sb.append("<br>");
                    sb.append("<a action=\"bypass -h npc_" + getObjectId() + "_upgrade_hatchling\">Upgrade Hatchling to Strider</a><br>");
                    sb.append("<br>");
                }
            }
            else
            {
                switch (lvl)
                {
                    case 0:
                        sb.append("Come back here when you reached level 20 to change your class.<br>");
                        break;
                    case 1:
                        sb.append("Come back here when you reached level 40 to change your class.<br>");
                        break;
                    case 2:
                        sb.append("Come back here when you reached level 76 to change your class.<br>");
                        break;
                    case 3:
                        sb.append("There is no class change available for you anymore.<br>");
                        break;
                }
                sb.append("<br>");
                sb.append("<a action=\"bypass -h npc_" + getObjectId() + "_upgrade_hatchling\">Upgrade Hatchling to Strider</a><br>");
                sb.append("<br>");
            }
            
            for (Quest q : Quest.findAllEvents())
                sb.append("Event: <a action=\"bypass -h Quest " + q.getName() + "\">" + q.getDescr() + "</a><br>");
            sb.append("</body></html>");
            html.setHtml(sb.toString());
            player.sendPacket(html);
            
            player.sendPacket(new ActionFailed());
        }
    }
    
    public void onBypassFeedback(L2PcInstance player, String command)
    {
        if (command.startsWith("change_class"))
        {
            int val = Integer.parseInt(command.substring(13));
            int level = player.getLevel();
            int jobLevel;
            int newJobLevel;
            
            jobLevel = player.getClassId().level();  
            newJobLevel = ClassId.values()[val].level();
            
            if(jobLevel == 3) return; // no more job changes
            
            // prevents changing between same level jobs
            if(newJobLevel != jobLevel + 1) return;
                
            if (level < 20 && newJobLevel > 0) return;
            if (level < 40 && newJobLevel > 1) return;
            if (level < 75 && newJobLevel > 2) return;
            // -- prevention ends
                        
                        
            changeClass(player, val);
            player.rewardSkills();
            
            if (newJobLevel == 3) player.sendPacket(new SystemMessage(1606)); // system sound 3rd occupation
            else player.sendPacket(new SystemMessage(1308)); // system sound for 1st and 2nd occupation
            
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            TextBuilder sb = new TextBuilder();
            sb.append("<html><head><body>");
            sb.append("You have now become a <font color=\"LEVEL\">" + CharTemplateTable.getClassNameById(player.getClassId().getId()) + "</font>.");
            sb.append("</body></html>");
            
            html.setHtml(sb.toString());
            player.sendPacket(html);
        }
        else
        {
            super.onBypassFeedback(player, command);
        }
    }
    
    private void changeClass(L2PcInstance player, int val)
    {
        if (_log.isDebugEnabled()) _log.debug("Changing class to ClassId:" + val);
        player.setClassId(val);
        
        if (player.isSubClassActive()) player.getSubClasses().get(player.getClassIndex()).setClassId(
                                                                                                     player.getActiveClass());
        else player.setBaseClass(player.getActiveClass());
        
        player.broadcastUserInfo();
    }
}
