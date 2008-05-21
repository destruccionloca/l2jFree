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
package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.Shutdown;
import net.sf.l2j.gameserver.datatables.GmListTable;
import net.sf.l2j.gameserver.instancemanager.CursedWeaponsManager;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.util.IllegalPlayerAction;
import net.sf.l2j.gameserver.util.Util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class ...
 * 
 * @version $Revision: 1.11.2.1.2.7 $ $Date: 2005/04/02 21:25:21 $
 */
public class RequestDropItem extends L2GameClientPacket
{
    private static final String _C__12_REQUESTDROPITEM = "[C] 12 RequestDropItem";
    private final static Log _log = LogFactory.getLog(RequestDropItem.class.getName());

    private int _objectId;
    private int _count;
    private int _x;
    private int _y;
    private int _z;
    /**
     * packet type id 0x12
     * 
     * sample
     * 
     * 12 
     * 09 00 00 40         // object id
     * 01 00 00 00         // count ??
     * fd e7 fe ff         // x
     * e5 eb 03 00         // y
     * bb f3 ff ff         // z 
     * 
     * format:        cdd ddd 
     * @param decrypt
     */
    @Override
    protected void readImpl()
    {
        _objectId = readD();
        _count    = readD();
        _x        = readD();
        _y        = readD();
        _z        = readD();
    }

    @Override
    protected void runImpl()
    {
        L2PcInstance activeChar = getClient().getActiveChar();
        if (activeChar == null) return;
        
        if (Config.SAFE_REBOOT && Config.SAFE_REBOOT_DISABLE_TRANSACTION && Shutdown.getCounterInstance() != null 
            && Shutdown.getCounterInstance().getCountdown() <= Config.SAFE_REBOOT_TIME)
        {
            activeChar.sendMessage("Transactions are not allowed during restart/shutdown.");
            activeChar.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
            return;
        }
        
        L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
        
        if (item == null 
                || _count == 0 
                || !activeChar.validateItemManipulation(_objectId, "drop") 
                || (!Config.ALLOW_DISCARDITEM && !activeChar.isGM()) 
                || (!item.isDropable() && !activeChar.isGM()))
        {
            activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_DISCARD_THIS_ITEM));
            return;
        }
        
        if(Config.ALT_STRICT_HERO_SYSTEM)
        {
           if (item.isHeroItem())
            {
                activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_DISCARD_THIS_ITEM));
                return;
            }
        }
        
        // Cursed Weapons cannot be dropped
        if (CursedWeaponsManager.getInstance().isCursed(item.getItemId()))
        {
            activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_DISCARD_THIS_ITEM));
            return;
        }
       
        if(_count > item.getCount()) 
        {
            activeChar.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
            return;
        }

        if (Config.PLAYER_SPAWN_PROTECTION > 0 && activeChar.isInvul() && !activeChar.isGM())
        {
            activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_DISCARD_THIS_ITEM));
            return;
        }

        if(_count < 0)
        {
            Util.handleIllegalPlayerAction(activeChar,"[RequestDropItem] count <= 0! ban! oid: "+_objectId+" owner: "+activeChar.getName(),IllegalPlayerAction.PUNISH_KICK);
            return;
        }
        
        if(!item.isStackable() && _count > 1)
        {
            Util.handleIllegalPlayerAction(activeChar,"[RequestDropItem] count > 1 but item is not stackable! ban! oid: "+_objectId+" owner: "+activeChar.getName(),IllegalPlayerAction.PUNISH_KICK);
            return;
        }
        
        if (Config.GM_DISABLE_TRANSACTION && activeChar.getAccessLevel() >= Config.GM_TRANSACTION_MIN && activeChar.getAccessLevel() <= Config.GM_TRANSACTION_MAX)
        {
            activeChar.sendMessage("Transactions are disable for your Access Level");
            activeChar.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
            return;
        }
        
        if (activeChar.isProcessingTransaction() || activeChar.getPrivateStoreType() != 0)
        {
            activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE));
            return;
        }
        if (activeChar.isFishing())
        {
            //You can't mount, dismount, break and drop items while fishing
            activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_DO_WHILE_FISHING_2));
            return;
        }
 
        // Cannot discard item that the skill is consumming
        if (activeChar.isCastingNow())
        {
            if (activeChar.getCurrentSkill() != null && activeChar.getCurrentSkill().getSkill().getItemConsumeId() == item.getItemId())
            {
                activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_DISCARD_THIS_ITEM));
                return;
            }
        }

        if (L2Item.TYPE2_QUEST == item.getItem().getType2() && !activeChar.isGM())
        {
            if (_log.isDebugEnabled()) _log.debug(activeChar.getObjectId()+":player tried to drop quest item");
            activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_DISCARD_EXCHANGE_ITEM));
            return;
        }
        
        if (!activeChar.isInsideRadius(_x, _y, 150, false) || Math.abs(_z - activeChar.getZ()) > 50)
        { 
            if (_log.isDebugEnabled()) _log.debug(activeChar.getObjectId()+": trying to drop too far away");
            activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_DISCARD_DISTANCE_TOO_FAR));
            return;
        }

        if (_log.isDebugEnabled()) _log.debug("requested drop item " + _objectId + "("+ item.getCount()+") at "+_x+"/"+_y+"/"+_z);
        
        if (item.isEquipped())
        {
            L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInBodySlotAndRecord(item.getItem().getBodyPart());
            InventoryUpdate iu = new InventoryUpdate();
            for (L2ItemInstance element : unequiped)
            {
                activeChar.checkSSMatch(null, element);
                iu.addModifiedItem(element);
            }
            activeChar.sendPacket(iu);
            activeChar.broadcastUserInfo();
        }
        
        L2ItemInstance dropedItem = activeChar.dropItem("Drop", _objectId, _count, _x, _y, _z, null, false);
        
        if (_log.isDebugEnabled()) _log.debug("dropping " + _objectId + " item("+_count+") at: " + _x + " " + _y + " " + _z);

        //activeChar.broadcastUserInfo();

        if (dropedItem != null && dropedItem.getItemId() == 57 && dropedItem.getCount() >= 1000000)
        {
            String msg = "Character ("+activeChar.getName()+") has dropped ("+dropedItem.getCount()+")adena at ("+_x+","+_y+","+_z+")";
            _log.warn(msg);
            GmListTable.broadcastMessageToGMs(msg);
        }
    }
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
     */
    @Override
    public String getType()
    {
        return _C__12_REQUESTDROPITEM;
    }
}
