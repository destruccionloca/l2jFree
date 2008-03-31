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

import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.recipes.manager.CraftManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RequestRecipeBookOpen extends L2GameClientPacket 
{
    private static final String _C__AC_REQUESTRECIPEBOOKOPEN = "[C] AC RequestRecipeBookOpen";
	private final static Log _log = LogFactory.getLog(RequestRecipeBookOpen.class.getName());
    
    private boolean isDwarvenCraft;

	/**
	 * packet type id 0xac
	 * packet format rev656  cd
	 * @param decrypt
	 */
    @Override
    protected void readImpl()
	{
        isDwarvenCraft = (readD() == 0);
        if (_log.isDebugEnabled()) _log.info("RequestRecipeBookOpen : " + (isDwarvenCraft ? "dwarvenCraft" : "commonCraft"));
	}

    @Override
    protected void runImpl()
	{
	    if (getClient().getActiveChar() == null)
	        return;
        
        if (getClient().getActiveChar().getPrivateStoreType() != 0)
        {
        	getClient().getActiveChar().sendPacket(new SystemMessage(SystemMessageId.PRIVATE_STORE_UNDER_WAY));
            return;
        }
        
        CraftManager.requestBookOpen(getClient().getActiveChar(), isDwarvenCraft);
	}
	
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
     */
    @Override
    public String getType() 
    {
        return _C__AC_REQUESTRECIPEBOOKOPEN;
    }
}
