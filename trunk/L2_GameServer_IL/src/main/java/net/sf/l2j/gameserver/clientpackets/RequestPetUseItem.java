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

import net.sf.l2j.gameserver.datatables.PetDataTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.handler.ItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.PetInfo;
import net.sf.l2j.gameserver.serverpackets.PetItemList;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    	//check if the item matches the pet
        if ((PetDataTable.isWolf(pet.getNpcId()) && item.getItem().isForWolf()) ||
            (PetDataTable.isHatchling(pet.getNpcId()) && item.getItem().isForHatchling()) ||
            (PetDataTable.isBaby(pet.getNpcId()) && item.getItem().isForBabyPet()) ||
            (PetDataTable.isStrider(pet.getNpcId()) && item.getItem().isForStrider()))
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
                pet.getInventory().unEquipItemInSlot(item.getEquipSlot());
            else
                pet.getInventory().equipItem(item);
            
            PetItemList pil = new PetItemList(pet);
            activeChar.sendPacket(pil);
            
            PetInfo pi = new PetInfo(pet);
            activeChar.sendPacket(pi);
            // The PetInfo packet wipes the PartySpelled (list of active spells' icons).  Re-add them
            pet.updateEffectIcons(true);
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
     * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
     */
    @Override
    public String getType()
    {
        return _C__8A_REQUESTPETUSEITEM;
    }
}