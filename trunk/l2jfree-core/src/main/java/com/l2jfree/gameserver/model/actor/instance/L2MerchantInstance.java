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

import java.util.StringTokenizer;

import com.l2jfree.Config;
import com.l2jfree.gameserver.datatables.MerchantPriceConfigTable;
import com.l2jfree.gameserver.datatables.TradeListTable;
import com.l2jfree.gameserver.datatables.MerchantPriceConfigTable.MerchantPriceConfig;
import com.l2jfree.gameserver.model.L2Multisell;
import com.l2jfree.gameserver.model.L2TradeList;
import com.l2jfree.gameserver.model.actor.L2Merchant;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.ExBuySellListPacket;
import com.l2jfree.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfree.gameserver.network.serverpackets.SellList;
import com.l2jfree.gameserver.network.serverpackets.SetupGauge;
import com.l2jfree.gameserver.network.serverpackets.ShopPreviewList;
import com.l2jfree.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;
import com.l2jfree.gameserver.util.StringUtil;
import com.l2jfree.lang.L2TextBuilder;

/**
 * This class ...
 *
 * @version $Revision: 1.10.4.9 $ $Date: 2005/04/11 10:06:08 $
 */
public class L2MerchantInstance extends L2NpcInstance implements L2Merchant
{
    private MerchantPriceConfig _mpc;

    /**
     * @param template
     */
    public L2MerchantInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }

    @Override
    public String getHtmlPath(int npcId, int val)
    {
        String pom = "";

        if (val == 0)
            pom = "" + npcId;
        else
            pom = npcId + "-" + val;

        return "data/html/merchant/" + pom + ".htm";
    }

    @Override
    public void onSpawn()
    {
        super.onSpawn();
        _mpc = MerchantPriceConfigTable.getInstance().getMerchantPriceConfig(this);
    }

    /**
     * @return Returns the mpc.
     */
    public MerchantPriceConfig getMpc()
    {
        return _mpc;
    }

    private final void showWearWindow(L2PcInstance player, int val)
    {
        player.tempInventoryDisable();

        if (_log.isDebugEnabled()) _log.debug("Showing wearlist");

        L2TradeList list = TradeListTable.getInstance().getBuyList(val);

        if (list != null)
        {
            ShopPreviewList bl = new ShopPreviewList(list, player.getAdena(), player.getExpertiseIndex());
            player.sendPacket(bl);
        }
        else
        {
            _log.warn("no buylist with id:" + val);
            player.sendPacket(ActionFailed.STATIC_PACKET);
        }
    }

    protected void showBuyWindow(L2PcInstance player, int val)
    {
        double taxRate = 1.;

        // Not ready yet
        //taxRate = getMpc().getTotalTaxRate();
        //instead:
        if (getIsInTown()) taxRate = getCastle().getTaxRate();

        player.tempInventoryDisable();

        if (_log.isDebugEnabled()) _log.debug("Showing buylist");

        L2TradeList list = TradeListTable.getInstance().getBuyList(val);

        if (list != null && list.getNpcId()== getNpcId())
            player.sendPacket(new ExBuySellListPacket(player, list, taxRate, false));
        else
        {
            _log.warn("possible client hacker: " + player.getName()
                + " attempting to buy from GM shop! < Ban him!");
            _log.warn("buylist id:" + val);
        }

        player.sendPacket(ActionFailed.STATIC_PACKET);
    }

    @Deprecated // FIXME 1.4.0 was removed at l2jserver 3696
    protected final void showSellWindow(L2PcInstance player)
    {
        if (_log.isDebugEnabled()) _log.debug("Showing selllist");

        player.sendPacket(new SellList(player));

        if (_log.isDebugEnabled()) _log.debug("Showing sell window");

        player.sendPacket(ActionFailed.STATIC_PACKET);
    }

    @Override
    public void onBypassFeedback(L2PcInstance player, String command)
    {
        StringTokenizer st = new StringTokenizer(command, " ");
        String actualCommand = st.nextToken(); // Get actual command

        if (actualCommand.equalsIgnoreCase("Buy"))
        {
            if (st.countTokens() < 1) return;

            int val = Integer.parseInt(st.nextToken());
            showBuyWindow(player, val);
        }
        else if (actualCommand.equalsIgnoreCase("BuyShadowItem"))
        {
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            if (player.getLevel() >= 40)
                html.setFile("data/html/merchant/shadow_item.htm");
            else
                html.setFile("data/html/merchant/shadow_item-lowlevel.htm");
            html.replace("%objectId%", String.valueOf(getObjectId()));
            player.sendPacket(html);
        }
        else if (actualCommand.equalsIgnoreCase("RentPet"))
        {
            if (Config.ALLOW_RENTPET)
            {
                if (st.countTokens() < 1)
                {
                    showRentPetWindow(player);
                }
                else
                {
                    int val = Integer.parseInt(st.nextToken());
                    tryRentPet(player, val);
                }
            }
        }
        else if (actualCommand.equalsIgnoreCase("Wear") && Config.ALLOW_WEAR)
        {
            if (st.countTokens() < 1) return;

            int val = Integer.parseInt(st.nextToken());
            showWearWindow(player, val);
        }
        else if (actualCommand.equalsIgnoreCase("Multisell"))
        {
            if (st.countTokens() < 1) return;

            int val = Integer.parseInt(st.nextToken());
            L2Multisell.getInstance().separateAndSend(val, player, getNpcId(), false, getCastle().getTaxRate());
        }
        else if (actualCommand.equalsIgnoreCase("Exc_Multisell"))
        {
            if (st.countTokens() < 1) return;

            int val = Integer.parseInt(st.nextToken());
            L2Multisell.getInstance().separateAndSend(val, player, getNpcId(), true, getCastle().getTaxRate());
        }
        else
        {
            // this class dont know any other commands, let forward
            // the command to the parent class
            super.onBypassFeedback(player, command);
        }
    }

	public final void showRentPetWindow(L2PcInstance player)
	{
	    if (!Config.LIST_PET_RENT_NPC.contains(getTemplate().getNpcId())) return;
	
	    L2TextBuilder html1 = L2TextBuilder.newInstance("<html><body>Pet Manager:<br>");
	    html1.append("You can rent a wyvern or strider for adena.<br>My prices:<br1>");
	    html1.append("<table border=0><tr><td>Ride</td></tr>");
	    html1.append("<tr><td>Wyvern</td><td>Strider</td></tr>");
	    html1.append("<tr><td><a action=\"bypass -h npc_%objectId%_RentPet 1\">30 sec/1800 adena</a></td><td><a action=\"bypass -h npc_%objectId%_RentPet 11\">30 sec/900 adena</a></td></tr>");
	    html1.append("<tr><td><a action=\"bypass -h npc_%objectId%_RentPet 2\">1 min/7200 adena</a></td><td><a action=\"bypass -h npc_%objectId%_RentPet 12\">1 min/3600 adena</a></td></tr>");
	    html1.append("<tr><td><a action=\"bypass -h npc_%objectId%_RentPet 3\">10 min/720000 adena</a></td><td><a action=\"bypass -h npc_%objectId%_RentPet 13\">10 min/360000 adena</a></td></tr>");
	    html1.append("<tr><td><a action=\"bypass -h npc_%objectId%_RentPet 4\">30 min/6480000 adena</a></td><td><a action=\"bypass -h npc_%objectId%_RentPet 14\">30 min/3240000 adena</a></td></tr>");
	    html1.append("</table>");
	    html1.append("</body></html>");
	
	    insertObjectIdAndShowChatWindow(player, html1.moveToString());
	}
	
	public void tryRentPet(L2PcInstance player, int val)
	{
		if (player == null || player.getPet() != null || player.isMounted() || player.isRentedPet() || player.isTransformed() || player.isCursedWeaponEquipped())
			return;

		if (!player.disarmWeapons(true))
			return;

		int petId;
		long cost[] = {1800, 7200, 720000, 6480000};
		int ridetime[] = {30, 60, 600, 1800};

		if (val < 1 || val > 4)
			return;

		long price;
		if (val > 10)
		{
			petId = 12526;
			val -= 10;
			price = cost[val - 1] / 2;
		}
		else
		{
			petId = 12621;
			price = cost[val - 1];
		}

		int time = ridetime[val - 1];

		if (!player.reduceAdena("Rent", price, player.getLastFolkNPC(), true))
			return;

		player.mount(petId, 0, false);
		SetupGauge sg = new SetupGauge(3, time * 1000);
		player.sendPacket(sg);
		player.startRentPet(time);
	}

	@Override
	public final void onActionShift(L2PcInstance player)
	{
		if (player.getAccessLevel() >= Config.GM_ACCESSLEVEL)
		{
			player.setTarget(this);
			
			if (isAutoAttackable(player))
			{
				StatusUpdate su = new StatusUpdate(getObjectId());
				su.addAttribute(StatusUpdate.CUR_HP, (int)getStatus().getCurrentHp());
				su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
				player.sendPacket(su);
			}
			
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			final StringBuilder html1 = StringUtil
					.startAppend(
							2000,
							"<html><body><center><font color=\"LEVEL\">Merchant Info</font></center><br><table border=0><tr><td>Object ID: </td><td>",
							String.valueOf(getObjectId()),
							"</td></tr><tr><td>Template ID: </td><td>",
							String.valueOf(getTemplate().getNpcId()),
							"</td></tr><tr><td><br></td></tr><tr><td>HP: </td><td>",
							String.valueOf(getCurrentHp()),
							"</td></tr><tr><td>MP: </td><td>",
							String.valueOf(getCurrentMp()),
							"</td></tr><tr><td>Level: </td><td>",
							String.valueOf(getLevel()),
							"</td></tr><tr><td><br></td></tr><tr><td>Class: </td><td>",
							getClass().getSimpleName(),
							"</td></tr><tr><td><br></td></tr></table><table><tr><td><button value=\"Edit NPC\" action=\"bypass -h admin_edit_npc ",
							String.valueOf(getTemplate().getNpcId()),
							"\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>"
									+ "<td><button value=\"Kill\" action=\"bypass -h admin_kill\" width=40 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>"
									+ "<tr><td><button value=\"Show DropList\" action=\"bypass -h admin_show_droplist ",
							String.valueOf(getTemplate().getNpcId()),
							"\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>"
									+ "<td><button value=\"Delete\" action=\"bypass -h admin_delete\" width=40 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>"
									+ "<tr><td><button value=\"View Shop\" action=\"bypass -h admin_showShop ", String
									.valueOf(getTemplate().getNpcId()),
							"\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table>");
			
			/**
			 * Lease doesn't work at all for now!!! StringUtil.append(html1, "<button value=\"Lease next week\" action=\"bypass -h npc_", String.valueOf(getObjectId()), "_Lease\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">" + "<button value=\"Abort current
			 * leasing\" action=\"bypass -h npc_", String.valueOf(getObjectId()), "_Lease next\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">" + "<button value=\"Manage items\" action=\"bypass -h npc_", String.valueOf(getObjectId()), "_Lease manage\" width=100
			 * height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">" );
			 */
			
			html1.append("</body></html>");
			
			html.setHtml(html1.toString());
			player.sendPacket(html);
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
}