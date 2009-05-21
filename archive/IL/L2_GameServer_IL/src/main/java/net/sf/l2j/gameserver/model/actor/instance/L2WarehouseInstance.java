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
package net.sf.l2j.gameserver.model.actor.instance;

import java.util.Map;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.PcFreight;
import net.sf.l2j.gameserver.model.entity.Town;
import net.sf.l2j.gameserver.instancemanager.TownManager;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.PackageToList;
import net.sf.l2j.gameserver.serverpackets.SortedWareHouseWithdrawalList;
import net.sf.l2j.gameserver.serverpackets.SortedWareHouseWithdrawalList.WarehouseListType;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.WareHouseDepositList;
import net.sf.l2j.gameserver.serverpackets.WareHouseWithdrawalList;

import net.sf.l2j.gameserver.templates.L2NpcTemplate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 * This class ...
 *
 * @version $Revision: 1.3.4.10 $ $Date: 2005/04/06 16:13:41 $
 */
public final class L2WarehouseInstance extends L2FolkInstance
{
    private final static Log _log = LogFactory.getLog(L2WarehouseInstance.class.getName());
    
    private int _closestTownId = -1;
    
    public L2WarehouseInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
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
        player.sendPacket(new ActionFailed());
        player.setActiveWarehouse(player.getWarehouse());

        if (player.getActiveWarehouse().getSize() == 0)
        {
            player.sendPacket(new SystemMessage(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH));
            return;
        }
        
        if (_log.isDebugEnabled()) _log.debug("Showing stored items");
        player.sendPacket(new SortedWareHouseWithdrawalList(player, WareHouseWithdrawalList.PRIVATE, itemtype, sortorder));
    }

    private void showRetrieveWindow(L2PcInstance player)
    {
        player.sendPacket(new ActionFailed());
        player.setActiveWarehouse(player.getWarehouse());
        
        if (player.getActiveWarehouse().getSize() == 0)
        {
        	player.sendPacket(new SystemMessage(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH));
        	return;
        }

        if (_log.isDebugEnabled()) _log.debug("Showing stored items");
        player.sendPacket(new WareHouseWithdrawalList(player, WareHouseWithdrawalList.PRIVATE));
    }

    private void showDepositWindow(L2PcInstance player)
    {
        player.sendPacket(new ActionFailed());
        player.setActiveWarehouse(player.getWarehouse());
        player.tempInvetoryDisable();
        if (_log.isDebugEnabled()) _log.debug("Showing items to deposit");

        player.sendPacket(new WareHouseDepositList(player, WareHouseDepositList.PRIVATE));
    }

    private void showDepositWindowClan(L2PcInstance player)
    {
        player.sendPacket(new ActionFailed());
        if (player.getClan() != null)
        {
            if (player.getClan().getLevel() == 0)
            {
                player.sendPacket(new SystemMessage(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE));
            }
            else
            {
                if ((player.getClanPrivileges() & L2Clan.CP_CL_VIEW_WAREHOUSE) != L2Clan.CP_CL_VIEW_WAREHOUSE)
                {
                    player.sendPacket(new SystemMessage(SystemMessageId.ONLY_CLAN_LEADER_CAN_RETRIEVE_ITEMS_FROM_CLAN_WAREHOUSE));
                }
                player.setActiveWarehouse(player.getClan().getWarehouse());
                player.tempInvetoryDisable();
                if (_log.isDebugEnabled()) _log.debug("Showing items to deposit - clan");

                WareHouseDepositList dl = new WareHouseDepositList(player, WareHouseDepositList.CLAN);
                player.sendPacket(dl);
            }
        }
    }

    private void showWithdrawWindowClan(L2PcInstance player, WarehouseListType itemtype, byte sortorder)
    {
        player.sendPacket(new ActionFailed());
        if ((player.getClanPrivileges() & L2Clan.CP_CL_VIEW_WAREHOUSE) != L2Clan.CP_CL_VIEW_WAREHOUSE)
        {
            player.sendPacket(new SystemMessage(SystemMessageId.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE));
            return;
        }
        else
        {
            if (player.getClan().getLevel() == 0)
                player.sendPacket(new SystemMessage(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE));
            else
            {
                player.setActiveWarehouse(player.getClan().getWarehouse());
                if (_log.isDebugEnabled()) _log.debug("Showing items to deposit - clan"); 
                player.sendPacket(new SortedWareHouseWithdrawalList(player, WareHouseWithdrawalList.CLAN, itemtype, sortorder));
            }
        }
    }

    private void showWithdrawWindowClan(L2PcInstance player)
    {
        player.sendPacket(new ActionFailed());
        if ((player.getClanPrivileges() & L2Clan.CP_CL_VIEW_WAREHOUSE) != L2Clan.CP_CL_VIEW_WAREHOUSE)
        {
        	player.sendPacket(new SystemMessage(SystemMessageId.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE));
        	return;
        }
        else
        {
            if (player.getClan().getLevel() == 0)
            {
                player.sendPacket(new SystemMessage(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE));
            }
            else
            {
                player.setActiveWarehouse(player.getClan().getWarehouse());
                if (_log.isDebugEnabled()) _log.debug("Showing items to deposit - clan");
                player.sendPacket(new WareHouseWithdrawalList(player, WareHouseWithdrawalList.CLAN));
            }
        }
    }

    private void showWithdrawWindowFreight(L2PcInstance player, WarehouseListType itemtype, byte sortorder)
    {
        player.sendPacket(new ActionFailed());
        if (_log.isDebugEnabled()) _log.debug("Showing freightened items");

        PcFreight freight = player.getFreight();

        if (freight != null)
        {
            if (freight.getSize() > 0)
            {
                if (Config.ALT_GAME_FREIGHTS)
                {
                    freight.setActiveLocation(0);
                }
                else
                {
                    freight.setActiveLocation(getClosestTown());
                }
                player.setActiveWarehouse(freight);
                player.sendPacket(new SortedWareHouseWithdrawalList(player, WareHouseWithdrawalList.FREIGHT, itemtype, sortorder));
            }
            else
            {
                player.sendPacket(new SystemMessage(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH));
            }
        }
        else
        {
            if (_log.isDebugEnabled()) _log.debug("no items freightened");
        }
    }
    

    private void showWithdrawWindowFreight(L2PcInstance player)
    {
        player.sendPacket(new ActionFailed());
        if (_log.isDebugEnabled()) _log.debug("Showing freightened items");

        PcFreight freight = player.getFreight();

        if (freight != null)
        {
        	if (freight.getSize() > 0)
        	{
        		if (Config.ALT_GAME_FREIGHTS)
        		{
        			freight.setActiveLocation(0);
        		}else
        		{
        			freight.setActiveLocation(getClosestTown());
        		}
        		player.setActiveWarehouse(freight);
        		player.sendPacket(new WareHouseWithdrawalList(player, WareHouseWithdrawalList.FREIGHT));
        	}
        	else
        	{
            	player.sendPacket(new SystemMessage(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH));
        	}
        }
        else
        {
            if (_log.isDebugEnabled()) _log.debug("no items freightened");
        }
    }

    private void showDepositWindowFreight(L2PcInstance player)
    {
        // No other chars in the account of this player
        if (player.getAccountChars().size() == 0)
        {
            player.sendPacket(new SystemMessage(SystemMessageId.CHARACTER_DOES_NOT_EXIST));
        }
        // One or more chars other than this player for this account
        else
        {

            Map<Integer, String> chars = player.getAccountChars();

            if (chars.size() < 1)
            {
                player.sendPacket(new ActionFailed());
                return;
            }
            
            player.sendPacket(new PackageToList(chars));
            
            if (_log.isDebugEnabled())
                _log.debug("Showing destination chars to freight - char src: " + player.getName());
        }
    }

    private void showDepositWindowFreight(L2PcInstance player, int obj_Id)
    {
        player.sendPacket(new ActionFailed());
        L2PcInstance destChar = L2PcInstance.load(obj_Id);
        if (destChar == null)
        {
            // Something went wrong!
            if (_log.isDebugEnabled())
                _log.warn("Error retrieving a target object for char " + player.getName()
                    + " - using freight.");
            return;
        }
        PcFreight freight = destChar.getFreight();
    	if (Config.ALT_GAME_FREIGHTS)
    	{
            freight.setActiveLocation(0);
    	} else
    	{
    		freight.setActiveLocation(getClosestTown());
    	}
        player.setActiveWarehouse(freight);
        player.tempInvetoryDisable();
        destChar.deleteMe();

        if (_log.isDebugEnabled()) _log.debug("Showing items to freight");
        player.sendPacket(new WareHouseDepositList(player, WareHouseDepositList.FREIGHT));
    }

    @Override
    public void onBypassFeedback(L2PcInstance player, String command)
    {
        // lil check to prevent enchant exploit
        if (player.getActiveEnchantItem() != null)
        {
            _log.info("Player " + player.getName() + " trying to use enchant exploit, ban this player!");
            player.closeNetConnection();
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
                showRetrieveWindow(player);
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
                showWithdrawWindowClan(player);
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
        else if (command.startsWith("WithdrawF"))
        {
            if (Config.ALLOW_FREIGHT)
            {
                if (Config.ENABLE_WAREHOUSESORTING_FREIGHT)
                {
                    String htmFile = "data/html/custom/WhSortedF.htm";
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
                    showWithdrawWindowFreight(player);
            }
        }
        else if (command.startsWith("WithdrawSortedF"))
        {
            if (Config.ALLOW_FREIGHT)
            {
                if (param.length > 2) 
                    showWithdrawWindowFreight(player, WarehouseListType.valueOf(param[1]), SortedWareHouseWithdrawalList.getOrder(param[2]));
                else if (param.length > 1)
                    showWithdrawWindowFreight(player, WarehouseListType.valueOf(param[1]), SortedWareHouseWithdrawalList.A2Z);
                else
                    showWithdrawWindowFreight(player, WarehouseListType.ALL, SortedWareHouseWithdrawalList.A2Z);
            }
        }
        else if (command.startsWith("DepositF"))
        {
            if (Config.ALLOW_FREIGHT)
            {
                showDepositWindowFreight(player);
            }
        }
        else if (command.startsWith("FreightChar"))
        {
            if (Config.ALLOW_FREIGHT && param.length > 1)
                showDepositWindowFreight(player, Integer.parseInt(param[1]));
        }
        else
        {
            // this class dont know any other commands, let forward
            // the command to the parent class
            super.onBypassFeedback(player, command);
        }
    }
    
    private int getClosestTown()
    {
    	if ( _closestTownId < 0)
    	{
    		Town town = TownManager.getInstance().getClosestTown(this);
    		if (town != null)
    		{
    			_closestTownId = town.getTownId();
    		}
    		else
    			_closestTownId = 0;
    	}
    	
    	return _closestTownId;
    }
}