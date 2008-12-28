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
package com.l2jfree.gameserver.network.clientpackets;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.gameserver.datatables.PetDataTable;
import com.l2jfree.gameserver.handler.IItemHandler;
import com.l2jfree.gameserver.handler.ItemHandler;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.PetInfo;
import com.l2jfree.gameserver.network.serverpackets.PetItemList;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.templates.item.L2ArmorType;
import com.l2jfree.gameserver.templates.item.L2Item;

public class RequestPetUseItem extends L2GameClientPacket
{
    private final static Log _log = LogFactory.getLog(RequestPetUseItem.class.getName());
    private static final String _C__8A_REQUESTPETUSEITEM = "[C] 8a RequestPetUseItem";
    
    private int _objectId;
    /**
     * packet type id 0x8a
     * format:      cd
     * @param decrypt
     */
    @Override
    protected void readImpl()
    {
        _objectId = readD();
    }

    @Override
    protected void runImpl()
    {
        L2PcInstance activeChar = getClient().getActiveChar();
        
        if (activeChar == null)
            return;
        
        L2PetInstance pet = (L2PetInstance)activeChar.getPet();
        
        if (pet == null)
            return;
        
        L2ItemInstance item = pet.getInventory().getItemByObjectId(_objectId);
        
        if (item == null)
            return;
        
        if (item.isWear())
            return;

        if (activeChar.isAlikeDead() || pet.isDead()) 
        {
            SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
            sm.addItemName(item);
            activeChar.sendPacket(sm);
            return;
        }
        
        if (_log.isDebugEnabled()) 
            _log.debug(activeChar.getObjectId()+": pet use item " + _objectId);

        // check if the food matches the pet
        if (PetDataTable.getFoodItemId(pet.getNpcId()) == item.getItemId())
        {
            feed(pet, item);
            return;
        }
        
        if (item.getItem().getBodyPart() == L2Item.SLOT_NECK)
        {
            if (item.getItem().getItemType() == L2ArmorType.PET)
            {
                useItem(pet, item, activeChar);
                return;
            }
        }
        
        //check if the item matches the pet
        if ((PetDataTable.isWolf(pet.getNpcId()) && item.getItem().isForWolf()) ||
            (PetDataTable.isHatchling(pet.getNpcId()) && item.getItem().isForHatchling()) ||
            (PetDataTable.isBaby(pet.getNpcId()) && item.getItem().isForBabyPet()) ||
            (PetDataTable.isStrider(pet.getNpcId()) && item.getItem().isForStrider()) ||
            (PetDataTable.isRedStrider(pet.getNpcId()) && item.getItem().isForStrider()) ||
            (PetDataTable.isGreatWolf(pet.getNpcId()) && (item.getItem().isForGreatWolf() || item.getItem().isForWolf())) ||
            (PetDataTable.isWGreatWolf(pet.getNpcId()) && (item.getItem().isForGreatWolf() || item.getItem().isForWolf())) ||
            (PetDataTable.isBlackWolf(pet.getNpcId()) && (item.getItem().isForGreatWolf() || item.getItem().isForWolf())) ||
            (PetDataTable.isFenrirWolf(pet.getNpcId()) && (item.getItem().isForGreatWolf() || item.getItem().isForWolf())) ||
            (PetDataTable.isWFenrirWolf(pet.getNpcId()) && (item.getItem().isForGreatWolf() || item.getItem().isForWolf())) ||
            (PetDataTable.isImprovedBaby(pet.getNpcId()) && item.getItem().isForBabyPet())
            )
        {
            useItem(pet, item, activeChar);
            return;
        }

        IItemHandler handler = ItemHandler.getInstance().getItemHandler(item.getItemId());
        
        if (handler != null)
        {
            useItem(pet, item, activeChar);
        }
        else
        {
            SystemMessage sm = new SystemMessage(SystemMessageId.ITEM_NOT_FOR_PETS);
            activeChar.sendPacket(sm);
        }
        
        return;
    }
    
    private synchronized void useItem(L2PetInstance pet, L2ItemInstance item, L2PcInstance activeChar)
    {
        if (item.isEquipable())
        {
            if (item.isEquipped())
                pet.getInventory().unEquipItemInSlot(item.getLocationSlot());
            else
                pet.getInventory().equipItem(item);
            
            PetItemList pil = new PetItemList(pet);
            activeChar.sendPacket(pil);
            
            PetInfo pi = new PetInfo(pet);
            activeChar.sendPacket(pi);
        }
        else
        {
            //_log.debug("item not equipable id:"+ item.getItemId());
            IItemHandler handler = ItemHandler.getInstance().getItemHandler(item.getItemId());
            
            if (handler == null)
                _log.warn("no itemhandler registered for itemId:" + item.getItemId());
            else
                handler.useItem(pet, item);
        }
    }

    /**
     * When fed by owner double click on food from pet inventory. <BR><BR>
     * 
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : 1 food = 100 points of currentFed</B></FONT><BR><BR>
     */
    private void feed(L2PetInstance pet, L2ItemInstance item)
    {
		// if pet has food in inventory
		if (pet.destroyItem("Feed", item.getObjectId(), 1, pet, false))
            pet.setCurrentFed(pet.getCurrentFed() + 100);
		pet.broadcastStatusUpdate();
    }
    
    /* (non-Javadoc)
     * @see com.l2jfree.gameserver.clientpackets.ClientBasePacket#getType()
     */
    @Override
    public String getType()
    {
        return _C__8A_REQUESTPETUSEITEM;
    }
}
