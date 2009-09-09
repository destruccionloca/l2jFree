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

import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.RecipeShopSellList;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.2.1.2.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestRecipeShopManagePrev extends L2GameClientPacket
{
	private static final String _C__B7_RequestRecipeShopPrev = "[C] b7 RequestRecipeShopPrev";

    @Override
    protected void readImpl()
    {
        // trigger
    }

    @Override
    protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null) return;

        // Player shouldn't be able to view stores if he/she is alike dead (dead or fake death)
        if (player.isAlikeDead())
        {
            sendAF();
            return;
        }

        L2Object target = player.getTarget();

        if (target == null)
		{
			requestFailed(SystemMessageId.TARGET_CANT_FOUND);
			return;
		}
		else if (!(target instanceof L2PcInstance))
		{
			requestFailed(SystemMessageId.TARGET_IS_INCORRECT);
	        return;
		}

		sendPacket(new RecipeShopSellList(player, (L2PcInstance) target));

		sendAF();
	}

	@Override
	public String getType()
	{
		return _C__B7_RequestRecipeShopPrev;
	}
}
