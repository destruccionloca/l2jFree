package net.sf.l2j.gameserver.model.actor.instance;

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

import java.util.StringTokenizer;

import javolution.text.TextBuilder;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.PlaySound;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public final class L2UrnInstance extends L2FolkInstance
{
    public L2UrnInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }
    
    protected static int _ingredient1;
    protected static int _ingCount1;
    protected static int _ingredient2;
    protected static int _ingCount2;
    protected static int _product;
    protected static int _prodCount;
    protected static int _mixTemp;
    protected static int mixChance;
    protected static int tempChance;
    
    public void onAction(L2PcInstance player)
    {
        player.sendPacket(new ActionFailed());
        player.setTarget(this);
        player.sendPacket(new MyTargetSelected(getObjectId(), -15));

        if (isInsideRadius(player, INTERACTION_DISTANCE, false, false))
            showMessageWindow(player);
    }
    
    public void onBypassFeedback(L2PcInstance player, String command)
    {
        if (!isInsideRadius(player, INTERACTION_DISTANCE, false, false)) return;

        player.sendPacket(new ActionFailed());
        
        StringTokenizer st = new StringTokenizer(command, " ");
        String actualCommand = st.nextToken(); // Get actual command

        String val = "";
        if (st.countTokens() >= 1)
        {
            val = st.nextToken();
        }
        
        if (actualCommand.equalsIgnoreCase("make_low"))
        {
            if (val != "")
            {
                player.sendPacket( new ActionFailed() );
                String filename = "data/html/urn/low" +val+ ".htm";
                NpcHtmlMessage html = new NpcHtmlMessage(1);
                html.setFile(filename);
                html.replace("%objectId%", String.valueOf(getObjectId()));
                html.replace("%npcname%", getName());
                player.sendPacket(html);
                return;
            }
            
            player.sendPacket( new ActionFailed() );
            String filename = "data/html/urn/low.htm";
            NpcHtmlMessage html = new NpcHtmlMessage(1);
            html.setFile(filename);
            html.replace("%objectId%", String.valueOf(getObjectId()));
            html.replace("%npcname%", getName());
            player.sendPacket(html);

            return;
        }
        else if (actualCommand.equalsIgnoreCase("make_high"))
        {
            if (val != "")
            {
                player.sendPacket( new ActionFailed() );
                String filename = "data/html/urn/high" +val+ ".htm";
                NpcHtmlMessage html = new NpcHtmlMessage(1);
                html.setFile(filename);
                html.replace("%objectId%", String.valueOf(getObjectId()));
                html.replace("%npcname%", getName());
                player.sendPacket(html);
                return;
            }
            
            player.sendPacket( new ActionFailed() );
            String filename = "data/html/urn/high.htm";
            NpcHtmlMessage html = new NpcHtmlMessage(1);
            html.setFile(filename);
            html.replace("%objectId%", String.valueOf(getObjectId()));
            html.replace("%npcname%", getName());
            player.sendPacket(html);

            return;
        }
        else if (actualCommand.equalsIgnoreCase("urn_stone"))
        {
            if (getUrnItemsCount(player, 5904) >= 1)
            {
                // show urn mix menu with low - high options
                player.sendPacket( new ActionFailed() );
                String filename = "data/html/urn/insertstone.htm";
                NpcHtmlMessage html = new NpcHtmlMessage(1);
                html.setFile(filename);
                html.replace("%objectId%", String.valueOf(getObjectId()));
                html.replace("%npcname%", getName());
                player.sendPacket(html);
                
                return;
            }
            player.sendPacket( new ActionFailed() );
            String filename = "data/html/urn/nostone.htm";
            NpcHtmlMessage html = new NpcHtmlMessage(1);
            html.setFile(filename);
            html.replace("%objectId%", String.valueOf(getObjectId()));
            html.replace("%npcname%", getName());
            player.sendPacket(html);
            
            return;
        }
        else if (actualCommand.equalsIgnoreCase("urn_mix"))
        {
            if (val != "")
            {
                String w1 = val;
                    int ingId1 = Integer.parseInt(w1);
                val = st.nextToken();
                String x1 = val;
                    int ingCount1 = Integer.parseInt(x1);
                val = st.nextToken();
                String y1 = val;
                    int ingId2 = Integer.parseInt(y1);
                val = st.nextToken();
                String z1 = val;
                    int ingCount2 = Integer.parseInt(z1);
                val = st.nextToken();
                String go1 = val;
                    int prodId = Integer.parseInt(go1);
                val = st.nextToken();
                String go2 = val;
                    int tempSet = Integer.parseInt(go2);
                val = st.nextToken();
                String go3 = val;
                    boolean mixLvl = Boolean.parseBoolean(go3);
                val = st.nextToken();
                String prodName1 = val;
                runUrnMix(player, ingId1, ingCount1, ingId2, ingCount2, prodId, tempSet, mixLvl, prodName1);
                
            }
        }
        else if (actualCommand.equalsIgnoreCase("urn_main"))
        {
            showMessageWindow(player);
        }  
    }
    
    protected void runUrnMix(L2PcInstance player, int ingId1, int ingCount1, int ingId2, int ingCount2, int prodId, int tempSet, boolean mixLvl, String prodName2)
    {
        boolean correctMix1 = false;
        boolean correctMix2 = false;
        int ingHas1 = getUrnItemsCount(player, ingId1);
        int ingHas2 = getUrnItemsCount(player, ingId2);

        // See if they have the correct components, first ing1 then ing2
        if (ingHas1 >= ingCount1)
        {
            // take the items and return true
            takeUrnItems(player, ingId1, ingCount1);
            correctMix1 = true;
        }
        else
        {
            takeUrnItems(player, ingId1, ingHas1);
        }
        if (ingHas2 >= ingCount2)
        {
            // take the items and return true
            takeUrnItems(player, ingId2, ingCount2);
            correctMix2 = true;
        }
        else
        {
            takeUrnItems(player, ingId2, ingHas2);
        }

        // check if mix is successful
        if (checkUrnSuccess(tempSet) && correctMix1 && correctMix2)
        {
            giveUrnItems(player, prodId, tempSet, 0);
            showSuccessWindow(player, mixLvl, prodName2, tempSet);
        }
        else
        {
            showFailureWindow(player);
        }
        return;
    }
    
    public void takeUrnItems(L2PcInstance player, int itemId, int count) 
    {
        // Get object item from player's inventory list
        L2ItemInstance item = player.getInventory().getItemByItemId(itemId);
        
        if (item == null)
            return;
        
        // Tests on count value in order not to have negative value
        if (count < 0 || count > item.getCount())
            count = item.getCount();
        
        // Destroy the quantity of items wanted
        if (itemId == 57)
            player.reduceAdena("Quest", count, player, true);
        else
            player.destroyItemByItemId("Quest", itemId, count, player, true);
    }
    
    public void giveUrnItems(L2PcInstance player, int itemId, int count, int enchantlevel) 
    {
        if (count <= 0) 
            return;

        // Set quantity of item
        
        // Add items to player's inventory
        L2ItemInstance item = player.getInventory().addItem("Quest", itemId, count, player, player.getTarget());
        
        if (item == null) 
            return;
        if (enchantlevel > 0) 
            item.setEnchantLevel(enchantlevel);
        
        // If item for reward is gold, send message of gold reward to client
        if (itemId == 57) 
        {
            SystemMessage smsg = new SystemMessage(SystemMessage.EARNED_ADENA);
            smsg.addNumber(count);
            player.sendPacket(smsg);
        } 
        // Otherwise, send message of object reward to client
        else 
        {
            if (count > 1)
            {
                SystemMessage smsg = new SystemMessage(SystemMessage.EARNED_S2_S1_s);
                smsg.addItemName(item.getItemId());
                smsg.addNumber(count);
                player.sendPacket(smsg);
            } else
            {
                SystemMessage smsg = new SystemMessage(SystemMessage.EARNED_ITEM);
                smsg.addItemName(item.getItemId());
                player.sendPacket(smsg);
            }
        }
    }
    
    public void playSound(L2PcInstance player, String sound) 
    {
        player.sendPacket(new PlaySound(sound));
    }
    
    public int getUrnItemsCount(L2PcInstance player, int itemId) 
    {
        int count = 0;
        
        for (L2ItemInstance item: player.getInventory().getItems())
            if (item.getItemId() == itemId)
                count += item.getCount();
        return count;
    }
    
    protected boolean checkUrnSuccess(int urnTemperature)
    {
        int mixFail = Config.ALT_URN_TEMP_FAIL;
        int mixTemperature = (urnTemperature - 1);
        if (mixFail < 10)
        {
            mixFail = 10;
        }
        else if (mixFail > 40);
            mixFail = 40;
        mixChance = (Rnd.get(100));
        tempChance = (100 - (mixTemperature * mixFail));
        if (mixChance < tempChance)
            return true;
        else
            return false;
    }
    
    public void showSuccessWindow(L2PcInstance player, boolean urnLvl, String prodName3, int prodNum3)
    {
        String rankName = "";
        String urnEffect = "";
        int randomEffect = (Rnd.get(3));
        if (randomEffect == 3)
        {
            urnEffect = "The contents burble and boil, smoke and steam rise from the urn.";
        }
        else if (randomEffect == 2)
        {
            urnEffect = "A swirl of red and yellow steam rises from the urn.";
        }
        else if (randomEffect == 1)
        {
            urnEffect = "Flame bursts from the urn with a cloud of black smoke.";
        }
        else if (randomEffect == 0)
        {
            urnEffect = "Black smoke billows forth from the urn and with a loud bang you are knocked to the floor.";
        }
        
        if (urnLvl)
        {
            rankName = "Apprentice Alchemist";
        }
        else
            rankName = "Master Alchemist";
        
        TextBuilder msg = new TextBuilder("<html><body>");
        msg.append("%npcname%:<br><br>");
        msg.append(urnEffect + "<BR>");
        msg.append("You peer into the urn to see " + prodName3 + " (" + prodNum3 + ") !<br>");
        msg.append("<font color=\"LEVEL\">Success!</font><br>");
        msg.append("You may yet become a " +rankName+ " !<br>");
        msg.append("<table width=200>");
        msg.append("<tr><td><button value=\"Back\" action=\"bypass -h npc_%objectId%_urn_main\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
        msg.append("</table>");
        msg.append("</body></html>");

        this.sendHtmlMessage(player, msg.toString());
    }
    
    public void showFailureWindow(L2PcInstance player)
    {
        
        TextBuilder msg = new TextBuilder("<html><body>");
        msg.append("%npcname%:<br><br>");
        msg.append("The contents burble and boil, smoke and steam rise from the urn.<BR>");
        msg.append("You peer into the urn to see nothing remains, the temperature was too hot!<br>");
        msg.append("<font color=\"LEVEL\">You have failed!</font><br>");
        msg.append("<table width=200>");
        msg.append("<tr><td><button value=\"Back\" action=\"bypass -h npc_%objectId%_urn_main\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
        msg.append("</table>");
        msg.append("</body></html>");

        this.sendHtmlMessage(player, msg.toString());
    }

    public void showMessageWindow(L2PcInstance player)
    {
        TextBuilder msg = new TextBuilder("<html><body>");
        msg.append("%npcname%:<br><br>");
        msg.append("If you have brought the <font color=\"LEVEL\">Mixing Stone</font> then you must insert it to continue!<BR>");
        msg.append("<table width=200>");
        msg.append("<tr><td><a action=\"bypass -h npc_%objectId%_urn_stone\">Insert Stone</a></td></tr>");
        msg.append("</table>");
        msg.append("</body></html>");

        this.sendHtmlMessage(player, msg.toString());
    }
    
    private void sendHtmlMessage(L2PcInstance player, String htmlMessage)
    {
        NpcHtmlMessage html = new NpcHtmlMessage(1);
        html.setHtml(htmlMessage);
        html.replace("%objectId%", String.valueOf(getObjectId()));
        html.replace("%npcname%", getName());
        player.sendPacket(html);
    }
    
}