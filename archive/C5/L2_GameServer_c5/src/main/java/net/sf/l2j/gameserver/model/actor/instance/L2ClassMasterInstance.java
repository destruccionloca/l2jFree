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
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.datatables.CharTemplateTable;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class Master implementation
 * ths npc is used for changing character occupation
 **/
public final class L2ClassMasterInstance extends L2FolkInstance
{
    private final static Log _log = LogFactory.getLog(L2ClassMasterInstance.class.getName());
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
            sb.append(getName()+":<br>");
            sb.append("<br>");
            
            ClassId classId = player.getClassId();
            int level = player.getLevel();
            int jobLevel = classId.level();
            
            int newJobLevel = jobLevel + 1;
            
            if(((level >= 20 && jobLevel == 0 ) || 
                (level >= 40 && jobLevel == 1 ) || 
                (level >= 75 && jobLevel == 2)) &&
                Config.CLASS_MASTER_SETTINGS.isAllowed(newJobLevel))
            {
            	sb.append("You can change your occupation to following:<br>");
            	
            	for (ClassId child : ClassId.values())
                	if (child.childOf(classId) && child.level() == newJobLevel)
                		sb.append("<br><a action=\"bypass -h npc_" + getObjectId() + "_change_class " + (child.getId()) + "\"> " + CharTemplateTable.getClassNameById(child.getId()) + "</a>");
            	
            	if (Config.CLASS_MASTER_SETTINGS.getRequireItems(newJobLevel) != null &&
            		Config.CLASS_MASTER_SETTINGS.getRequireItems(newJobLevel).size() > 0)
            	{
            		sb.append("<br><br>Item(s) required for class change:");
            		sb.append("<table width=270>");
            		for(Integer _itemId : Config.CLASS_MASTER_SETTINGS.getRequireItems(newJobLevel).keySet())
            		{
            			int _count = Config.CLASS_MASTER_SETTINGS.getRequireItems(newJobLevel).get(_itemId);
            			sb.append("<tr><td><font color=\"LEVEL\">"+_count+"</font></td><td>"+ItemTable.getInstance().getTemplate(_itemId).getName()+"</td></tr>");
            		}
            		sb.append("</table>");
            	}
            	
            	sb.append("<br>");
            }
            else
            {
                switch (jobLevel)
                {
                    case 0:
                    	if (Config.CLASS_MASTER_SETTINGS.isAllowed(1))
                    		sb.append("Come back here when you reached level 20 to change your class.<br>");
                        else
                        	if (Config.CLASS_MASTER_SETTINGS.isAllowed(2))
                        		sb.append("Come back after your first occupation change.<br>");
                        	else
                        		if (Config.CLASS_MASTER_SETTINGS.isAllowed(3))
                        			sb.append("Come back after your second occupation change.<br>");
                        		else
                        			sb.append("I can't change your occupation.<br>");
                        break;
                    case 1:
                    	if (Config.CLASS_MASTER_SETTINGS.isAllowed(2))
                    		sb.append("Come back here when you reached level 40 to change your class.<br>");
                    	else
                    		if (Config.CLASS_MASTER_SETTINGS.isAllowed(3))
                        		sb.append("Come back after your second occupation change.<br>");
                    		else 
                    			sb.append("I can't change your occupation.<br>");
                        break;
                    case 2:
                    	if (Config.CLASS_MASTER_SETTINGS.isAllowed(3))
                    		sb.append("Come back here when you reached level 76 to change your class.<br>");
                    	else 
                			sb.append("I can't change your occupation.<br>");
                        break;
                    case 3:
                        sb.append("There is no class change available for you anymore.<br>");
                        break;
                }
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
             
            ClassId classId = player.getClassId();
            ClassId newClassId = ClassId.values()[val];
            
            int level = player.getLevel();
            int jobLevel = classId.level();
            int newJobLevel = newClassId.level();
             
            // -- exploit prevention
            // prevents changing if config option disabled
            if (!Config.CLASS_MASTER_SETTINGS.isAllowed(newJobLevel)) return;
            
            // prevents changing to class not in same class tree
            if (!newClassId.childOf(classId)) return;
            
            // prevents changing between same level jobs
            if(newJobLevel != jobLevel + 1) return;
            
            // check for player level
            if (level < 20 && newJobLevel > 1) return;
            if (level < 40 && newJobLevel > 2) return;
            if (level < 75 && newJobLevel > 3) return;
            // -- prevention ends

            // check if player have all required items for class transfer
    		for(Integer _itemId : Config.CLASS_MASTER_SETTINGS.getRequireItems(newJobLevel).keySet())
    		{
    			int _count = Config.CLASS_MASTER_SETTINGS.getRequireItems(newJobLevel).get(_itemId);
    			if (player.getInventory().getInventoryItemCount(_itemId, -1) < _count)
    			{
    				player.sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_ITEMS));
    				return;
    			}
    		}
            
            // get all required items for class transfer
    		for(Integer _itemId : Config.CLASS_MASTER_SETTINGS.getRequireItems(newJobLevel).keySet())
    		{
    			int _count = Config.CLASS_MASTER_SETTINGS.getRequireItems(newJobLevel).get(_itemId);
    			player.destroyItemByItemId("ClassMaster", _itemId, _count, player, true);
    		}

            // reward player with items
    		for(Integer _itemId : Config.CLASS_MASTER_SETTINGS.getRewardItems(newJobLevel).keySet())
    		{
    			int _count = Config.CLASS_MASTER_SETTINGS.getRewardItems(newJobLevel).get(_itemId);
    			player.addItem("ClassMaster", _itemId, _count, player, true);
    		}
    		
            changeClass(player, val);
    		
            player.rewardSkills();
            
            if(newJobLevel == 3)
           	 	// system sound 3rd occupation
            	player.sendPacket(new SystemMessage(SystemMessage.SOUND_3RD_OCCUPATION));
            else
            	// system sound for 1st and 2nd occupation
            	player.sendPacket(new SystemMessage(SystemMessage.SOUND_2ND_OCCUPATION));    

            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            TextBuilder sb = new TextBuilder();
            sb.append("<html><body>");
            sb.append(getName()+":<br>");
            sb.append("<br>");          
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
