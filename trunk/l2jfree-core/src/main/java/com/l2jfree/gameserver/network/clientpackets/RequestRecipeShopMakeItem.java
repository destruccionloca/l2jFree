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

import com.l2jfree.gameserver.RecipeController;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.util.Util;

public class RequestRecipeShopMakeItem extends L2GameClientPacket
{
	private static final String _C__AF_REQUESTRECIPESHOPMAKEITEM = "[C] B6 RequestRecipeShopMakeItem";

	private int _id;
	private int _recipeId;
	//private long _unknown;

	/**
	 * packet type id 0xac
	 * format:		cd
	 * @param decrypt
	 */
	@Override
	protected void readImpl()
	{
		_id = readD();
		_recipeId = readD();
		/*_unknown = */readCompQ();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null) return;

		L2Object object = null;

		// Get object from target
		if (activeChar.getTargetId() == _id)
			object = activeChar.getTarget();

		// Get object from world
		if (object == null)
			object = L2World.getInstance().getPlayer(_id);

		if (!(object instanceof L2PcInstance))
		{
			requestFailed(SystemMessageId.TARGET_IS_INCORRECT);
			return;
		}

		L2PcInstance manufacturer = (L2PcInstance) object;

		if (activeChar.getPrivateStoreType() != 0)
		{
			requestFailed(SystemMessageId.PRIVATE_STORE_UNDER_WAY);
			return;
		}
		if (manufacturer.getPrivateStoreType() != 5)
		{
			//activeChar.sendMessage("Cannot make items while trading");
			sendAF();
			return;
		}

		if (activeChar.isInCraftMode() || manufacturer.isInCraftMode())
		{
			//activeChar.sendMessage("Currently in Craft Mode");
			sendAF();
			return;
		}
		if (manufacturer.isInDuel() || activeChar.isInDuel())
		{
			requestFailed(SystemMessageId.CANT_CRAFT_DURING_COMBAT);
			return;
		}

		if (Util.checkIfInRange(150, activeChar, manufacturer, true))
			RecipeController.getInstance().requestManufactureItem(manufacturer, _recipeId, activeChar);
		else
			sendPacket(SystemMessageId.TARGET_TOO_FAR);

		sendAF();
	}

	@Override
	public String getType()
	{
		return _C__AF_REQUESTRECIPESHOPMAKEITEM;
	}
}
