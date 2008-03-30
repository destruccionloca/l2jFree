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
package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.items.model.L2ExtractableItem;
import net.sf.l2j.gameserver.items.model.L2ExtractableProductItem;
import net.sf.l2j.gameserver.items.service.ExtractableItemsService;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.registry.IServiceRegistry;
import net.sf.l2j.tools.L2Registry;
import net.sf.l2j.tools.random.Rnd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author FBIagent 11/12/2006
 * 
 */

public class ExtractableItems implements IItemHandler
{
	protected static Log			_log					= LogFactory.getLog(ExtractableItems.class);

	private ExtractableItemsService	extractableItemsService	= (ExtractableItemsService) L2Registry.getBean(IServiceRegistry.EXTRACTABLE_ITEM);

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.l2j.gameserver.handler.IItemHandler#useItem(net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance,
	 *      net.sf.l2j.gameserver.model.L2ItemInstance)
	 */
	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;

		L2PcInstance activeChar = (L2PcInstance) playable;

		int itemID = item.getItemId();
		L2ExtractableItem exitem = extractableItemsService.getExtractableItem(itemID);

		if (exitem == null)
			return;

		int createItemID = 0, createAmount = 0, rndNum = Rnd.get(100), chanceFrom = 0;

		// calculate extraction
		for (L2ExtractableProductItem expi : exitem.getProductItemsArray())
		{
			int chance = expi.getChance();

			if (rndNum >= chanceFrom && rndNum <= chance + chanceFrom)
			{
				createItemID = expi.getId();
				createAmount = expi.getAmmount();
				break;
			}

			chanceFrom += chance;
		}

		if (createItemID == 0)
		{
			activeChar.sendMessage("Nothing happened.");
		}

		if (createItemID > 0)
		{
			if (ItemTable.getInstance().createDummyItem(createItemID) == null)
			{
				_log.warn("createItemID " + createItemID + " doesn't have template!");
				activeChar.sendMessage("Nothing happened.");
				return;
			}
			if (ItemTable.getInstance().createDummyItem(createItemID).isStackable())
				activeChar.addItem("Extract", createItemID, createAmount, item, false);
			else
			{
				for (int i = 0; i < createAmount; i++)
					activeChar.addItem("Extract", createItemID, 1, item, false);
			}
			SystemMessage sm;

			if (createAmount > 1)
			{
				sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
				sm.addItemNameById(createItemID);
				sm.addNumber(createAmount);
			}
			else
			{
				sm = new SystemMessage(SystemMessageId.EARNED_ITEM);
				sm.addItemNameById(createItemID);
			}
			activeChar.sendPacket(sm);
		}
		else
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_FAILED);
			sm.addString("Extraction");
			activeChar.sendPacket(sm);
		}

		activeChar.destroyItemByItemId("Extract", itemID, 1, activeChar.getTarget(), true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.l2j.gameserver.handler.IItemHandler#getItemIds()
	 */
	public int[] getItemIds()
	{
		return extractableItemsService.itemIDs();
	}
}
