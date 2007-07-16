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

import java.util.StringTokenizer;

import javolution.text.TextBuilder;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.datatables.SkillTreeTable;
import net.sf.l2j.gameserver.datatables.TradeListTable;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2SkillLearn;
import net.sf.l2j.gameserver.model.L2TradeList;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.AquireSkillList;
import net.sf.l2j.gameserver.serverpackets.BuyList;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.SellList;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class L2FishermanInstance extends L2MerchantInstance
{
    private final static Log _log = LogFactory.getLog(L2FishermanInstance.class.getName());
    /**
     * @param objectId
     * @param template
     */
    public L2FishermanInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }
    
    /**
     * this is called when a player interacts with this NPC
     * @param player
     */
    public void onAction(L2PcInstance player)
    {
        player.setLastFolkNPC(this);
        super.onAction(player);
    }
    
    public String getHtmlPath(int npcId, int val)
    {
        String pom = "";
        
        if (val == 0)
            pom = "" + npcId;
        else 
            pom = npcId + "-" + val;
        
        return "data/html/fisherman/" + pom + ".htm";
    }
	
	private void showBuyWindow(L2PcInstance player, int val)
    {
        double taxRate = 0;
        if (getIsInTown()) taxRate = getCastle().getTaxRate();
        player.tempInvetoryDisable();
        if (_log.isDebugEnabled()) _log.debug("Showing buylist");
        L2TradeList list = TradeListTable.getInstance().getBuyList(val);

        if (list != null && list.getNpcId()== getNpcId())
        {
            BuyList bl = new BuyList(list, player.getAdena(), taxRate);
            player.sendPacket(bl);
        }
        else
        {
            _log.warn("possible client hacker: " + player.getName()
                + " attempting to buy from GM shop! < Ban him!");
            _log.warn("buylist id:" + val);
        }

        player.sendPacket(new ActionFailed());
    }
	
	private void showSellWindow(L2PcInstance player)
    {
        if (_log.isDebugEnabled()) _log.debug("Showing selllist");

        player.sendPacket(new SellList(player));

        if (_log.isDebugEnabled()) _log.debug("Showing sell window");

        player.sendPacket(new ActionFailed());
    }
    
    public void onBypassFeedback(L2PcInstance player, String command)
	{	
        if (command.startsWith("FishSkillList"))
        {
            player.setSkillLearningClassId(player.getClassId());
            showSkillList(player);
		}

		StringTokenizer st = new StringTokenizer(command, " ");
        String command2 = st.nextToken();
		
		if (command2.equalsIgnoreCase("Buy"))
        {
            if (st.countTokens() < 1) return;
            int val = Integer.parseInt(st.nextToken());
            showBuyWindow(player, val);
        }
        else if (command2.equalsIgnoreCase("Sell"))
        {
        	showSellWindow(player);
        }
        else 
        {
            super.onBypassFeedback(player, command);
        }
    }   
    
    public void showSkillList(L2PcInstance player)
    {       
        L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableSkills(player);
        AquireSkillList asl = new AquireSkillList(AquireSkillList.skillType.Fishing);
        
        int counts = 0;

        for (L2SkillLearn s : skills)
        {
            L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
            
            if (sk == null)
				continue;	
            
            counts++;
            asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), s.getSpCost(), 1);
        }
        
        if (counts == 0)
        {
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            int minlevel = SkillTreeTable.getInstance().getMinLevelForNewSkill(player);
            
		    if (minlevel > 0)
            {
                // No more skills to learn, come back when you level.
		        SystemMessage sm = new SystemMessage(SystemMessage.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN);
		        sm.addNumber(minlevel);
		        player.sendPacket(sm);
		    }
            else
            {
                TextBuilder sb = new TextBuilder();
                sb.append("<html><head><body>");
                sb.append("You've learned all skills.<br>");
                sb.append("</body></html>");
                html.setHtml(sb.toString());
                player.sendPacket(html);
            }
        }
		else 
		{
		    player.sendPacket(asl);
		}
        
        player.sendPacket(new ActionFailed());
    }
}