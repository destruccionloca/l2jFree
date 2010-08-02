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
package com.l2jfree.gameserver.model.actor.instance;

import com.l2jfree.Config;
import com.l2jfree.gameserver.cache.HtmCache;
import com.l2jfree.gameserver.model.L2Clan;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfree.gameserver.network.serverpackets.SortedWareHouseWithdrawalList;
import com.l2jfree.gameserver.network.serverpackets.SortedWareHouseWithdrawalList.WarehouseListType;
import com.l2jfree.gameserver.network.serverpackets.WareHouseDepositList;
import com.l2jfree.gameserver.network.serverpackets.WareHouseWithdrawalList;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;
import com.l2jfree.gameserver.util.IllegalPlayerAction;
import com.l2jfree.gameserver.util.Util;

public final class L2WarehouseInstance extends L2NpcInstance
{
	public L2WarehouseInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public boolean isWarehouse()
	{
		return true;
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		if (val == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + val;
		}
		return "data/html/warehouse/" + pom + ".htm";
	}
	
	private void showRetrieveWindow(L2PcInstance player, WarehouseListType itemtype, byte sortorder)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		player.setActiveWarehouse(player.getWarehouse());
		
		if (player.getActiveWarehouse().getSize() == 0)
		{
			player.sendPacket(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH);
			return;
		}
		
		if (_log.isDebugEnabled())
			_log.debug("Showing stored items");
		
		if (itemtype != null)
			player.sendPacket(new SortedWareHouseWithdrawalList(player, WareHouseWithdrawalList.PRIVATE, itemtype, sortorder));
		else
			player.sendPacket(new WareHouseWithdrawalList(player, WareHouseWithdrawalList.PRIVATE));
	}
	
	private void showDepositWindow(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		player.setActiveWarehouse(player.getWarehouse());
		player.tempInventoryDisable();
		if (_log.isDebugEnabled())
			_log.debug("Showing items to deposit");
		
		player.sendPacket(new WareHouseDepositList(player, WareHouseDepositList.PRIVATE));
	}
	
	private void showDepositWindowClan(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		if (player.getClan() != null)
		{
			if (player.getClan().getLevel() == 0)
			{
				player.sendPacket(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE);
			}
			else
			{
				if (!L2Clan.checkPrivileges(player, L2Clan.CP_CL_VIEW_WAREHOUSE))
					player.sendPacket(SystemMessageId.ONLY_CLAN_LEADER_CAN_RETRIEVE_ITEMS_FROM_CLAN_WAREHOUSE);
				player.setActiveWarehouse(player.getClan().getWarehouse());
				player.tempInventoryDisable();
				if (_log.isDebugEnabled())
					_log.debug("Showing items to deposit - clan");
				player.sendPacket(new WareHouseDepositList(player, WareHouseDepositList.CLAN));
			}
		}
	}
	
	private void showWithdrawWindowClan(L2PcInstance player, WarehouseListType itemtype, byte sortorder)
	{
		if (player.getClan() == null || player.getClan().getLevel() == 0)
		{
			player.sendPacket(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE);
		}
		else if (!L2Clan.checkPrivileges(player, L2Clan.CP_CL_VIEW_WAREHOUSE))
		{
			player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE);
			return;
		}
		else
		{
			player.setActiveWarehouse(player.getClan().getWarehouse());
			if (_log.isDebugEnabled())
				_log.debug("Showing items to deposit - clan");
			
			if (itemtype != null)
				player.sendPacket(new SortedWareHouseWithdrawalList(player, WareHouseWithdrawalList.CLAN, itemtype, sortorder));
			else
				player.sendPacket(new WareHouseWithdrawalList(player, WareHouseWithdrawalList.CLAN));
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		// Lil check to prevent enchant exploit
		if (player.getActiveEnchantItem() != null)
		{
			Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " trying to use enchant exploit, ban this player!",
					IllegalPlayerAction.PUNISH_KICK);
			return;
		}
		
		String param[] = command.split("_");
		
		if (command.startsWith("WithdrawP"))
		{
			if (Config.ENABLE_WAREHOUSESORTING_PRIVATE)
			{
				String htmFile = "data/html/custom/WhSortedP.htm";
				String htmContent = HtmCache.getInstance().getHtm(htmFile);
				if (htmContent != null)
				{
					NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());
					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%objectId%", String.valueOf(getObjectId()));
					player.sendPacket(npcHtmlMessage);
				}
				else
				{
					_log.warn("Missing htm: " + htmFile + " !");
				}
			}
			else
				showRetrieveWindow(player, null, (byte) 0);
		}
		else if (command.startsWith("WithdrawSortedP"))
		{
			if (param.length > 2)
				showRetrieveWindow(player, WarehouseListType.valueOf(param[1]), SortedWareHouseWithdrawalList.getOrder(param[2]));
			else if (param.length > 1)
				showRetrieveWindow(player, WarehouseListType.valueOf(param[1]), SortedWareHouseWithdrawalList.A2Z);
			else
				showRetrieveWindow(player, WarehouseListType.ALL, SortedWareHouseWithdrawalList.A2Z);
		}
		else if (command.equals("DepositP"))
		{
			showDepositWindow(player);
		}
		else if (command.startsWith("WithdrawC"))
		{
			if (Config.ENABLE_WAREHOUSESORTING_CLAN)
			{
				String htmFile = "data/html/custom/WhSortedC.htm";
				String htmContent = HtmCache.getInstance().getHtm(htmFile);
				if (htmContent != null)
				{
					NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());
					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%objectId%", String.valueOf(getObjectId()));
					player.sendPacket(npcHtmlMessage);
				}
				else
				{
					_log.warn("Missing htm: " + htmFile + " !");
				}
			}
			else
				showWithdrawWindowClan(player, null, (byte) 0);
		}
		else if (command.startsWith("WithdrawSortedC"))
		{
			if (param.length > 2)
				showWithdrawWindowClan(player, WarehouseListType.valueOf(param[1]), SortedWareHouseWithdrawalList.getOrder(param[2]));
			else if (param.length > 1)
				showWithdrawWindowClan(player, WarehouseListType.valueOf(param[1]), SortedWareHouseWithdrawalList.A2Z);
			else
				showWithdrawWindowClan(player, WarehouseListType.ALL, SortedWareHouseWithdrawalList.A2Z);
		}
		else if (command.equals("DepositC"))
		{
			showDepositWindowClan(player);
		}
		else
		{
			// This class dont know any other commands, let forward
			// the command to the parent class
			super.onBypassFeedback(player, command);
		}
	}
}