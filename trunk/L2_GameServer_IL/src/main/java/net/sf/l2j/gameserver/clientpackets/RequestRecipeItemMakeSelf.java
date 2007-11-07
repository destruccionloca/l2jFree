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
package net.sf.l2j.gameserver.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.Shutdown;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.recipes.manager.CraftManager;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * @author Administrator
 */
public class RequestRecipeItemMakeSelf extends L2GameClientPacket 
{
    private static final String _C__AF_REQUESTRECIPEITEMMAKESELF = "[C] AF RequestRecipeItemMakeSelf";

	private int _id;
	/**
	 * packet type id 0xac
	 * format:		cd
	 * @param decrypt
	 */
    @Override
    protected void readImpl()
    {
        _id = readD();
    }

    @Override
    protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		    return;
        
		if (Config.SAFE_REBOOT && Config.SAFE_REBOOT_DISABLE_CREATEITEM && Shutdown.getCounterInstance() != null 
        		&& Shutdown.getCounterInstance().getCountdown() <= Config.SAFE_REBOOT_TIME)
        {
			activeChar.sendMessage("Item creation is not allowed during restart/shutdown.");
			activeChar.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
            return;
        }
		
        if (activeChar.getPrivateStoreType() != 0)
        {
            activeChar.sendMessage("Cannot make items while trading");
            return;
        }
        
        if (activeChar.isInCraftMode())
        {
            activeChar.sendMessage("Currently in Craft Mode");
            return;
        }
        
		CraftManager.requestMakeItem(activeChar, _id);
	}
	
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
     */
    @Override
    public String getType() 
    {
        return _C__AF_REQUESTRECIPEITEMMAKESELF;
    }
}
