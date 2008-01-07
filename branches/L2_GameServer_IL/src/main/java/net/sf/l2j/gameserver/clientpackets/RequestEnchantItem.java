/* This program is free software; you can redistribute it and/or modify
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
package net.sf.l2j.gameserver.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.Shutdown;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.base.Race;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.EnchantResult;
import net.sf.l2j.gameserver.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.gameserver.util.IllegalPlayerAction;
import net.sf.l2j.gameserver.util.Util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RequestEnchantItem extends L2GameClientPacket
{
    protected static final Log _log = LogFactory.getLog(Inventory.class.getName());
    private static final String _C__58_REQUESTENCHANTITEM = "[C] 58 RequestEnchantItem";
    private static final int[] ENCHANT_SCROLLS = {729, 730, 947, 948, 951, 952, 955, 956, 959, 960 };
    private static final int[] CRYSTAL_SCROLLS = {731, 732, 949, 950, 953, 954, 957, 958, 961, 962 };
    private static final int[] BLESSED_SCROLLS = {6569, 6570, 6571, 6572, 6573, 6574, 6575, 6576, 6577, 6578 };

    private int _objectId;
    /**
     * packet type id 0x58
     * 
     * sample
     * 
     * 58
     * c0 d5 00 10 // objectId
     * 
     * format:      cd
     * @param decrypt
     */
    @Override
    protected void readImpl()
    {
        _objectId = 0;
        try
        {
            _objectId = readD();
        }
        catch (Exception e) {}
    }

    @Override
    protected void runImpl()
    {
        L2PcInstance activeChar = getClient().getActiveChar();
        if (activeChar == null || _objectId == 0) return;

        // Restrict enchant during restart/shutdown (because of an existing exploit)
        if (Config.SAFE_REBOOT && Config.SAFE_REBOOT_DISABLE_ENCHANT && Shutdown.getCounterInstance() != null 
        		&& Shutdown.getCounterInstance().getCountdown() <= Config.SAFE_REBOOT_TIME)
        {
            activeChar.sendMessage("Enchanting items is not allowed during restart/shutdown.");
            return;
        }
        
        L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
        L2ItemInstance scroll = activeChar.getActiveEnchantItem();
        activeChar.setActiveEnchantItem(null);
        if (item == null || scroll == null) return;
        if ((item.getLocation() != L2ItemInstance.ItemLocation.INVENTORY) && (item.getLocation() != L2ItemInstance.ItemLocation.PAPERDOLL))
            return;
        
        int itemId = item.getItemId();
        
        if (item.isWear())
        {
            Util.handleIllegalPlayerAction(activeChar,"Player "+activeChar.getName()+" tried to enchant a weared Item", IllegalPlayerAction.PUNISH_KICK);
            return;
        }
        SystemMessage sm;
        //can't enchant rods, shadow items, hero items
        if(item.getItem().getItemType() == L2WeaponType.ROD || item.isShadowItem() || (!Config.ENCHANT_HERO_WEAPONS && item.isHeroitem())) 
        {
        	sm = new SystemMessage(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION);
        	activeChar.sendPacket(sm);
            return;
        }

        int itemType2 = item.getItem().getType2();
        boolean enchantItem = false;
        boolean enchantBreak = false;
        int crystalId = 0;
        
        /** pretty code ;D */
        switch (item.getItem().getCrystalType())
        {
            case L2Item.CRYSTAL_A:
                crystalId = 1461;
                switch(scroll.getItemId())
                {
                    case 729: case 731: case 6569:
                        if(itemType2 == L2Item.TYPE2_WEAPON)
                            enchantItem = true;
                        break;
                    case 730: case 732: case 6570:
                        if((itemType2 == L2Item.TYPE2_SHIELD_ARMOR) || (itemType2 == L2Item.TYPE2_ACCESSORY))
                            enchantItem = true;
                        break;
                }
                break;
            case L2Item.CRYSTAL_B:
                crystalId = 1460;
                switch(scroll.getItemId())
                {
                    case 947: case 949: case 6571:
                        if(itemType2 == L2Item.TYPE2_WEAPON)
                            enchantItem = true;
                        break;
                    case 948: case 950: case 6572:
                        if((itemType2 == L2Item.TYPE2_SHIELD_ARMOR) || (itemType2 == L2Item.TYPE2_ACCESSORY))
                            enchantItem = true;
                        break;
                }
                break;
            case L2Item.CRYSTAL_C:
                crystalId = 1459;
                switch(scroll.getItemId())
                {
                    case 951: case 953: case 6573:
                        if(itemType2 == L2Item.TYPE2_WEAPON)
                            enchantItem = true;
                        break;
                    case 952: case 954: case 6574:
                        if((itemType2 == L2Item.TYPE2_SHIELD_ARMOR) || (itemType2 == L2Item.TYPE2_ACCESSORY))
                            enchantItem = true;
                        break;
                }
                break;
            case L2Item.CRYSTAL_D:
                crystalId = 1458;
                switch(scroll.getItemId())
                {
                    case 955: case 957: case 6575:
                        if(itemType2 == L2Item.TYPE2_WEAPON)
                            enchantItem = true;
                        break;
                    case 956: case 958: case 6576:
                        if((itemType2 == L2Item.TYPE2_SHIELD_ARMOR) || (itemType2 == L2Item.TYPE2_ACCESSORY))
                            enchantItem = true;
                        break;
                }
                break;
            case L2Item.CRYSTAL_S:
                crystalId = 1462;
                switch(scroll.getItemId())
                {
                    case 959: case 961: case 6577:
                        if(itemType2 == L2Item.TYPE2_WEAPON)
                            enchantItem = true;
                        break;
                    case 960: case 962: case 6578:
                        if((itemType2 == L2Item.TYPE2_SHIELD_ARMOR) || (itemType2 == L2Item.TYPE2_ACCESSORY))
                            enchantItem = true;
                        break;
                }
                break;
        }
        
        if (!enchantItem)
        {
        	sm = new SystemMessage(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION);
            activeChar.sendPacket(sm);
            return;
        }
        
        int chance = 0;
        int maxEnchantLevel = 0;

        if (item.getItem().getType2() == L2Item.TYPE2_WEAPON) // its a weapon
        {
            maxEnchantLevel = Config.ENCHANT_MAX_WEAPON;
            
            for (int scrollId : ENCHANT_SCROLLS)
                if(scroll.getItemId() == scrollId)
                {
                    chance = Config.ENCHANT_CHANCE_WEAPON;
                    enchantBreak = Config.ENCHANT_BREAK_WEAPON;
                    break;
                }
            for (int scrollId : CRYSTAL_SCROLLS)
                if(scroll.getItemId() == scrollId)
                {
                    chance = Config.ENCHANT_CHANCE_WEAPON_CRYSTAL;
                    enchantBreak = Config.ENCHANT_BREAK_WEAPON_CRYSTAL;
                    break;
                }
            for (int scrollId : BLESSED_SCROLLS)
                if(scroll.getItemId() == scrollId)
                {
                    chance = Config.ENCHANT_CHANCE_WEAPON_BLESSED;
                    enchantBreak = Config.ENCHANT_BREAK_WEAPON_BLESSED;
                    break;
                }
        }
        else if(item.getItem().getType2() == L2Item.TYPE2_ACCESSORY) // its jewelry 
        {
            maxEnchantLevel = Config.ENCHANT_MAX_JEWELRY;
            
            for (int scrollId : ENCHANT_SCROLLS)
                if(scroll.getItemId() == scrollId)
                {
                    chance = Config.ENCHANT_CHANCE_JEWELRY;
                    enchantBreak = Config.ENCHANT_BREAK_JEWELRY;
                    break;
                }
            for (int scrollId : CRYSTAL_SCROLLS)
                if(scroll.getItemId() == scrollId)
                {
                    chance = Config.ENCHANT_CHANCE_JEWELRY_CRYSTAL;
                    enchantBreak = Config.ENCHANT_BREAK_JEWELRY_CRYSTAL;
                    break;
                }
            for (int scrollId : BLESSED_SCROLLS)
                if(scroll.getItemId() == scrollId)
                {
                    chance = Config.ENCHANT_CHANCE_JEWELRY_BLESSED;
                    enchantBreak = Config.ENCHANT_BREAK_JEWELRY_BLESSED;
                    break;
                }
        }
        else // its an armor 
        {
            maxEnchantLevel = Config.ENCHANT_MAX_ARMOR;
            
            for (int scrollId : ENCHANT_SCROLLS)
                if(scroll.getItemId() == scrollId)
                {
                    chance = Config.ENCHANT_CHANCE_ARMOR;
                    enchantBreak = Config.ENCHANT_BREAK_ARMOR;
                    break;
                }
            for (int scrollId : CRYSTAL_SCROLLS)
                if(scroll.getItemId() == scrollId)
                {
                    chance = Config.ENCHANT_CHANCE_ARMOR_CRYSTAL;
                    enchantBreak = Config.ENCHANT_BREAK_ARMOR_CRYSTAL;
                    break;
                }
            for (int scrollId : BLESSED_SCROLLS)
                if(scroll.getItemId() == scrollId)
                {
                    chance = Config.ENCHANT_CHANCE_ARMOR_BLESSED;
                    enchantBreak = Config.ENCHANT_BREAK_ARMOR_BLESSED;
                    break;
                }
        }
        
        if (item.getEnchantLevel() >= maxEnchantLevel && maxEnchantLevel != 0)
        {
            activeChar.sendPacket(new SystemMessage(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION));
            return;
        }
        
        scroll = activeChar.getInventory().destroyItem("Enchant", scroll, activeChar, item);
        if(scroll == null)
        {
        	sm = new SystemMessage(SystemMessageId.ENCHANT_SCROLL_CANCELLED);
            activeChar.sendPacket(sm);
            Util.handleIllegalPlayerAction(activeChar,"Player "+activeChar.getName()+" tried to enchant with a scroll he doesnt have", Config.DEFAULT_PUNISH);
            return;
        }
        activeChar.getInventory().updateInventory(scroll);
        
        if (item.getEnchantLevel() < Config.ENCHANT_SAFE_MAX
                || (item.getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR
                        && item.getEnchantLevel() < Config.ENCHANT_SAFE_MAX_FULL))
            chance = 100;
        
        else if(activeChar.getRace() == Race.dwarf && Config.ENCHANT_DWARF_SYSTEM)
        {
            int _charlevel = activeChar.getLevel();
            int _itemlevel = item.getEnchantLevel();
            if(_charlevel>=20 && _itemlevel <= Config.ENCHANT_DWARF_1_ENCHANTLEVEL)     
                chance = chance + Config.ENCHANT_DWARF_1_CHANCE;
            else if(_charlevel>=40 && _itemlevel <= Config.ENCHANT_DWARF_2_ENCHANTLEVEL)
                chance = chance + Config.ENCHANT_DWARF_2_CHANCE;
            else if(_charlevel>=76 && _itemlevel <= Config.ENCHANT_DWARF_3_ENCHANTLEVEL)
                chance = chance + Config.ENCHANT_DWARF_3_CHANCE;
        }

        if (Rnd.get(100) < chance)
        {
            synchronized(item)
            {
                if (item.getOwnerId() != activeChar.getObjectId()) // has just lost the item
                {
                    activeChar.sendPacket(new SystemMessage(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION));
                    return;
                }
                if (item.getEnchantLevel() == 0)
                {
                    sm = new SystemMessage(SystemMessageId.S1_SUCCESSFULLY_ENCHANTED);
                    sm.addItemName(item);
                    activeChar.sendPacket(sm);
                }
                else
                {
                    sm = new SystemMessage(SystemMessageId.S1_S2_SUCCESSFULLY_ENCHANTED);
                    sm.addNumber(item.getEnchantLevel());
                    sm.addItemName(item);
                    activeChar.sendPacket(sm);
                }
                item.setEnchantLevel(item.getEnchantLevel()+1);
                item.setLastChange(L2ItemInstance.MODIFIED);
                item.updateDatabase();
            }
        }
        else
        {
            if (enchantBreak)
            {
                if (item.getEnchantLevel() > 0)
                {
                    sm = new SystemMessage(SystemMessageId.ENCHANTMENT_FAILED_S1_S2_EVAPORATED);
                    sm.addNumber(item.getEnchantLevel());
                    sm.addItemName(item);
                    activeChar.sendPacket(sm);
                }
                else
                {
                    sm = new SystemMessage(SystemMessageId.ENCHANTMENT_FAILED_S1_EVAPORATED);
                    sm.addItemName(item);
                    activeChar.sendPacket(sm);
                }
                
                if (item.getEnchantLevel() > 0)
                {
                    sm = new SystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
                    sm.addNumber(item.getEnchantLevel());
                    sm.addItemName(item);
                    activeChar.sendPacket(sm);
                }
                else
                {
                    sm = new SystemMessage(SystemMessageId.S1_DISARMED);
                    sm.addItemName(item);
                    activeChar.sendPacket(sm);
                }

                if (item.isEquipped())
                {
					L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInSlotAndRecord(item.getEquipSlot());
                	InventoryUpdate iu = new InventoryUpdate();
                    for (L2ItemInstance element : unequiped) {
                        iu.addItem(element);
                    }
                    activeChar.sendPacket(iu);
                }
                
                int count = item.getCrystalCount() - (item.getItem().getCrystalCount() +1) / 2;
                if (count < 1) count = 1;
    
                L2ItemInstance destroyItem = activeChar.getInventory().destroyItem("Enchant", item, activeChar, null);
                if (destroyItem == null) return;
                sm = new SystemMessage(SystemMessageId.S1_DISAPPEARED);
                sm.addItemName(destroyItem);
                activeChar.sendPacket(sm);
                L2World.getInstance().removeObject(destroyItem);
                
                L2ItemInstance crystals = activeChar.getInventory().addItem("Enchant", crystalId, count, activeChar, destroyItem);            
                sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
                sm.addItemName(crystals);
                sm.addNumber(count);
                activeChar.sendPacket(sm);
                activeChar.getInventory().updateInventory(crystals);
            }
            else
            {
                sm = new SystemMessage(SystemMessageId.BLESSED_ENCHANT_FAILED);
                activeChar.sendPacket(sm);
            
                item.setEnchantLevel(0);
                item.setLastChange(L2ItemInstance.MODIFIED);
                item.updateDatabase();
            }
        }
        sm = null;
        activeChar.sendPacket(new EnchantResult(item.getEnchantLevel()));
        activeChar.getInventory().updateInventory(item);
        activeChar.broadcastUserInfo();
    }
    
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
     */
    @Override
    public String getType()
    {
        return _C__58_REQUESTENCHANTITEM;
    }
}
