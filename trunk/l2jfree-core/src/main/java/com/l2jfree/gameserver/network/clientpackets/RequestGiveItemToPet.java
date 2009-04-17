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

import com.l2jfree.Config;
import com.l2jfree.gameserver.Shutdown;
import com.l2jfree.gameserver.Shutdown.DisableType;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.2.1.2.5 $ $Date: 2005/03/29 23:15:33 $
 */
public class RequestGiveItemToPet extends L2GameClientPacket
{
	private static final String	REQUESTCIVEITEMTOPET__C__8B	= "[C] 8B RequestGiveItemToPet";
	private final static Log	_log						= LogFactory.getLog(RequestGetItemFromPet.class.getName());

	private int					_objectId;
	private int					_amount;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_amount = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null || !(player.getPet() instanceof L2PetInstance))
			return;

		if (Shutdown.isActionDisabled(DisableType.TRANSACTION))
		{
			player.sendMessage("Transactions are not allowed during restart/shutdown.");
			player.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
			return;
		}

		if (Config.GM_DISABLE_TRANSACTION && player.getAccessLevel() >= Config.GM_TRANSACTION_MIN && player.getAccessLevel() <= Config.GM_TRANSACTION_MAX)
		{
			player.sendMessage("Unsufficient privileges.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		// Alt game - Karma punishment
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_TRADE && player.getKarma() > 0)
			return;

		if (player.getPrivateStoreType() != 0)
		{
			sendPacket(SystemMessageId.ITEMS_CANNOT_BE_DISCARDED_OR_DESTROYED_WHILE_OPERATING_PRIVATE_STORE_OR_WORKSHOP);
			return;
		}

		if (player.getRequest().getRequestPacket() instanceof TradeRequest || player.getRequest().getRequestPacket() instanceof TradeDone)
		{
			sendPacket(new SystemMessage(SystemMessageId.CANNOT_DISCARD_OR_DESTROY_ITEM_WHILE_TRADING));
			return;
		}

		L2PetInstance pet = (L2PetInstance) player.getPet();
		if (pet.isDead())
		{
			sendPacket(new SystemMessage(SystemMessageId.CANNOT_GIVE_ITEMS_TO_DEAD_PET));
			return;
		}

		if (_amount < 0)
			return;

		L2ItemInstance item = player.getInventory().getItemByObjectId(_objectId);
		if (!item.isDropable() || !item.isDestroyable() || !item.isTradeable())
		{
			sendPacket(new SystemMessage(SystemMessageId.ITEM_NOT_FOR_PETS));
			return;
		}
		
		if (!item.isAvailable(player, true))
		{
			sendPacket(SystemMessageId.PET_CANNOT_USE_ITEM);
			return;
		}

		if (item.isAugmented())
		{
			sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
			return;
		}

		if (Config.ALT_STRICT_HERO_SYSTEM && item.isHeroItem())
		{
			sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
			return;
		}

		/*long weight = item.getItem().getWeight() * _amount;
		if (weight >= Integer.MAX_VALUE || weight < 0 || !pet.getInventory().validateWeight((int) weight))
		{
			sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_ANY_MORE_ITEMS);
			return;
		}*/
		
		if (!pet.getInventory().validateCapacity(item))
		{
			pet.getOwner().sendPacket(new SystemMessage(SystemMessageId.YOUR_PET_CANNOT_CARRY_ANY_MORE_ITEMS));
			return;
		}
		if (!pet.getInventory().validateWeight(item,_amount))
		{
			pet.getOwner().sendPacket(new SystemMessage(SystemMessageId.UNABLE_TO_PLACE_ITEM_YOUR_PET_IS_TOO_ENCUMBERED));
			return;
		}
		
		if (player.transferItem("Transfer", _objectId, _amount, pet.getInventory(), pet) == null)
		{
			_log.warn("Invalid item transfer request: " + pet.getName() + "(pet) --> " + player.getName());
		}
	}

	@Override
	public String getType()
	{
		return REQUESTCIVEITEMTOPET__C__8B;
	}
}
