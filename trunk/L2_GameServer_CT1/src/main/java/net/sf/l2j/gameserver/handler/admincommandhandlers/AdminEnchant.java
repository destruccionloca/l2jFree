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
package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CharInfo;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class handles following admin commands:
 * - enchant_armor
 * 
 * @version $Revision: 1.3.2.1.2.10 $ $Date: 2005/08/24 21:06:06 $
 */
public class AdminEnchant implements IAdminCommandHandler
{
    private static final String[] ADMIN_COMMANDS = {
                                              "admin_seteh",//6
                                              "admin_setec",//10
                                              "admin_seteg",//9
                                              "admin_setel",//11
                                              "admin_seteb",//12
                                              "admin_setew",//7
                                              "admin_setes",//8
                                              "admin_setle",//1
                                              "admin_setre",//2
                                              "admin_setlf",//4
                                              "admin_setrf",//5
                                              "admin_seten",//3
                                              "admin_setun",//0
                                              "admin_setba",//13
                                              "admin_enchant"};
    private static final int REQUIRED_LEVEL = Config.GM_ENCHANT;

    private final static Log _log = LogFactory.getLog(AdminEnchant.class);
    
    public boolean useAdminCommand(String command, L2PcInstance activeChar)
    {
        if (!Config.ALT_PRIVILEGES_ADMIN)
        	if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM()))
        		return false;

        if (command.equals("admin_enchant"))
        {
            showMainPage(activeChar);
        } else
        {
            int armorType = -1;
            
            if (command.startsWith("admin_seteh"))
                armorType = Inventory.PAPERDOLL_HEAD;
            else if (command.startsWith("admin_setec"))
                armorType = Inventory.PAPERDOLL_CHEST;
            else if (command.startsWith("admin_seteg"))
                armorType = Inventory.PAPERDOLL_GLOVES;
            else if (command.startsWith("admin_seteb"))
                armorType = Inventory.PAPERDOLL_FEET;
            else if (command.startsWith("admin_setel"))
                armorType = Inventory.PAPERDOLL_LEGS;
            else if (command.startsWith("admin_setew"))
                armorType = Inventory.PAPERDOLL_RHAND;
            else if (command.startsWith("admin_setes"))
                armorType = Inventory.PAPERDOLL_LHAND;
            else if (command.startsWith("admin_setle"))
                armorType = Inventory.PAPERDOLL_LEAR;
            else if (command.startsWith("admin_setre"))
                armorType = Inventory.PAPERDOLL_REAR;
            else if (command.startsWith("admin_setlf"))
                armorType = Inventory.PAPERDOLL_LFINGER;
            else if (command.startsWith("admin_setrf"))
                armorType = Inventory.PAPERDOLL_RFINGER;
            else if (command.startsWith("admin_seten"))
                armorType = Inventory.PAPERDOLL_NECK;
            else if (command.startsWith("admin_setun"))
                armorType = Inventory.PAPERDOLL_UNDER;
            
            if (armorType != -1)
            {
                try
                {
                    int ench = Integer.parseInt(command.substring(12));
                    
                    // check value
                    if (ench < 0 || ench > 65535)
                        activeChar.sendMessage("You must set the enchant level to be between 0-65535.");
                    else
                        setEnchant(activeChar, ench, armorType);
                }
                catch (StringIndexOutOfBoundsException e)
                {
                    if (_log.isDebugEnabled()) _log.debug("Set enchant error: " + e);
                    activeChar.sendMessage("Please specify a new enchant value.");
                }
                catch (NumberFormatException e)
                {
                    if (_log.isDebugEnabled()) _log.debug("Set enchant error: " + e);
                    activeChar.sendMessage("Please specify a valid new enchant value.");
                }
            }
            
            // show the enchant menu after an action
            showMainPage(activeChar);
        }
        
        return true;
    }

    private void setEnchant(L2PcInstance activeChar, int ench, int armorType)
    {
        // get the target
        L2Object target = activeChar.getTarget();
        if (target == null) target = activeChar;
        L2PcInstance player = null;
        if (target instanceof L2PcInstance)
        {
            player = (L2PcInstance) target;
        }
        else
        {
            activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
            return;
        }

        // now we need to find the equipped weapon of the targeted character...
        int curEnchant = 0; // display purposes only
        L2ItemInstance itemInstance = null;

        // only attempt to enchant if there is a weapon equipped
        L2ItemInstance parmorInstance = player.getInventory().getPaperdollItem(armorType);
        if (parmorInstance != null && parmorInstance.getLocationSlot() == armorType)
        {
            itemInstance = parmorInstance;
        } else 
        {
            // for bows and double handed weapons
            parmorInstance = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
            if (parmorInstance != null && parmorInstance.getLocationSlot() == Inventory.PAPERDOLL_RHAND)
                itemInstance = parmorInstance;
        }
        
        if (itemInstance != null)
        {
            curEnchant = itemInstance.getEnchantLevel();
            
            // set enchant value
            player.getInventory().unEquipItemInSlotAndRecord(armorType);
            itemInstance.setEnchantLevel(ench);
            player.getInventory().equipItemAndRecord(itemInstance);

            // send packets
            InventoryUpdate iu = new InventoryUpdate();
            iu.addModifiedItem(itemInstance);
            player.sendPacket(iu);
            player.broadcastPacket(new CharInfo(player));
            player.sendPacket(new UserInfo(player));

            // informations
            activeChar.sendMessage("Changed enchantment of " + player.getName() + "'s "
                + itemInstance.getItem().getName() + " from " + curEnchant + " to " + ench + ".");
            player.sendMessage("Admin has changed the enchantment of your "
                + itemInstance.getItem().getName() + " from " + curEnchant + " to " + ench + ".");
        }
    }

    public void showMainPage(L2PcInstance activeChar)
    {
        AdminHelpPage.showHelpPage(activeChar, "enchant.htm");
    }

    public String[] getAdminCommandList()
    {
        return ADMIN_COMMANDS;
    }

    private boolean checkLevel(int level)
    {
        return (level >= REQUIRED_LEVEL);
    }
}
