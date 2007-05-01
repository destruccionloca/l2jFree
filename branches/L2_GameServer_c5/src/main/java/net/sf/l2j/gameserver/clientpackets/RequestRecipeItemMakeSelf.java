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

import java.nio.ByteBuffer;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.Shutdown;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.recipes.manager.CraftManager;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class RequestRecipeItemMakeSelf extends ClientBasePacket 
{
    private static final String _C__AF_REQUESTRECIPEITEMMAKESELF = "[C] AF RequestRecipeItemMakeSelf";
	//private final static Log _log = LogFactory.getLog(RequestSellItem.class.getName());

	private final int _id;
	/**
	 * packet type id 0xac
	 * format:		cd
	 * @param decrypt
	 */
	public RequestRecipeItemMakeSelf(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_id = readD();
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		    return;
        
		if (Config.SAFE_REBOOT && Config.SAFE_REBOOT_DISABLE_CREATEITEM && Shutdown.getCounterInstance() != null 
        		&& Shutdown.getCounterInstance().getCountdow() <= Config.SAFE_REBOOT_TIME)
        {
			activeChar.sendMessage("Item creation isn't allowed during restart/shutdown!");
			activeChar.sendPacket(new SystemMessage(SystemMessage.NOTHING_HAPPENED));
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
    public String getType() 
    {
        return _C__AF_REQUESTRECIPEITEMMAKESELF;
    }

}
