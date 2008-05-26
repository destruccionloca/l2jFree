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

import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ExAutoSoulShot;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.templates.L2Item;

/**
 * This class ...
 * 
 * @version $Revision: 1.0.0.0 $ $Date: 2005/07/11 15:29:30 $
 */
public class RequestAutoSoulShot extends L2GameClientPacket
{
    private static final String _C__CF_REQUESTAUTOSOULSHOT = "[C] CF RequestAutoSoulShot";
    private final static Log _log = LogFactory.getLog(RequestAutoSoulShot.class.getName());

    // format  cd
    private int _itemId;
    private int _type; // 1 = on : 0 = off;

    /**
     * packet type id 0xcf
     * format:      chdd
     * @param decrypt
     */
    @Override
    protected void readImpl()
    {
        _itemId = readD();
        _type = readD();
    }

    @Override
    protected void runImpl()
    {
        L2PcInstance activeChar = getClient().getActiveChar();
        
        if (activeChar == null) 
            return;

        if (activeChar.getPrivateStoreType() == 0 && 
                activeChar.getActiveRequester() == null &&
                !activeChar.isDead())
        {
            if (_log.isDebugEnabled()) 
                _log.info("AutoSoulShot:" + _itemId);

            L2ItemInstance item = activeChar.getInventory().getItemByItemId(_itemId);
            
            if (item != null)
            {
                if (_type == 1)
                {
                    //Fishingshots are not automatic on retail
                    if (_itemId < 6535 || _itemId > 6540)
                    {
                        // Attempt to charge first shot on activation
                        if (_itemId == 6645 || _itemId == 6646 || _itemId == 6647)
                        {
                            activeChar.addAutoSoulShot(_itemId);
                            ExAutoSoulShot atk = new ExAutoSoulShot(_itemId, _type);
                            activeChar.sendPacket(atk);
    
                            //start the auto soulshot use
                            SystemMessage sm = new SystemMessage(SystemMessageId.USE_OF_S1_WILL_BE_AUTO);
                            sm.addString(item.getItemName());
                            activeChar.sendPacket(sm);
                            sm = null;
                            
                            activeChar.rechargeAutoSoulShot(true, true, true);
                        }
                        else
                        {
                            int shotType = item.getItem().getCrystalType();
                            // is the on Blessed Spiritshots not allowed ?? added to check SoulShots and SpiritShots all grades. remove it if i'm not correct
                            if (activeChar.getActiveWeaponItem() != activeChar.getFistsWeaponItem()
                                    && (shotType == activeChar.getActiveWeaponItem().getCrystalType()
                                    || (shotType == L2Item.CRYSTAL_S && activeChar.getActiveWeaponItem().getCrystalType() == L2Item.CRYSTAL_S80)))
                            {
                                    activeChar.addAutoSoulShot(_itemId);
                                    ExAutoSoulShot atk = new ExAutoSoulShot(_itemId, _type);
                                    activeChar.sendPacket(atk);
    
                                    //start the auto soulshot use
                                    SystemMessage sm = new SystemMessage(SystemMessageId.USE_OF_S1_WILL_BE_AUTO);
                                    sm.addString(item.getItemName());
                                    activeChar.sendPacket(sm);
                                    sm = null;
                                
                                    activeChar.rechargeAutoSoulShot(true, true, false);
                            }
                            else
                            {
                                if ((_itemId >= 2509 && _itemId <= 2514) || (_itemId >= 3947 && _itemId <= 3952) || _itemId == 5790)
                                    activeChar.sendPacket(new SystemMessage(SystemMessageId.SPIRITSHOTS_GRADE_MISMATCH));
                                else
                                    activeChar.sendPacket(new SystemMessage(SystemMessageId.SOULSHOTS_GRADE_MISMATCH));
                            }
                        }
                    }
                }
                else if (_type == 0)
                {
                    activeChar.removeAutoSoulShot(_itemId);
                    ExAutoSoulShot atk = new ExAutoSoulShot(_itemId, _type);
                    activeChar.sendPacket(atk);

                    //cancel the auto soulshot use
                    SystemMessage sm = new SystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED);
                    sm.addString(item.getItemName());
                    activeChar.sendPacket(sm);
                    sm = null;
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see com.l2jfree.gameserver.clientpackets.ClientBasePacket#getType()
     */
    @Override
    public String getType()
    {
        return _C__CF_REQUESTAUTOSOULSHOT;
    }
}
