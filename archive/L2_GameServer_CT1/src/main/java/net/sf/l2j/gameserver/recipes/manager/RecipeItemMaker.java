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
package net.sf.l2j.gameserver.recipes.manager;

import java.util.List;

import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.items.model.TempItem;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2ManufactureItem;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.network.serverpackets.RecipeItemMakeInfo;
import net.sf.l2j.gameserver.network.serverpackets.RecipeShopItemInfo;
import net.sf.l2j.gameserver.network.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.recipes.model.L2Recipe;
import net.sf.l2j.gameserver.recipes.model.L2RecipeComponent;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.tools.random.Rnd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RecipeItemMaker implements Runnable
{
	private final static Log	_log	= LogFactory.getLog(RecipeItemMaker.class.getName());

	private boolean				isValid;
	private List<TempItem>		items	= null;
	private final L2Recipe		recipe;
	private final L2PcInstance	player;														// "crafter"
	private final L2PcInstance	target;														// "customer"
	private final L2Skill		skill;
	private final int			skillId;
	private final int			skillLevel;
	private double				creationPasses;
	private double				manaRequired;
	private int					price;
	private int					totalItems;
	private int					materialsRefPrice;
	private int					delay;

	public RecipeItemMaker(L2PcInstance pPlayer, L2Recipe pRecipe, L2PcInstance pTarget)
	{
		player = pPlayer;
		target = pTarget;
		recipe = pRecipe;

		isValid = false;
		skillId = recipe.isDwarvenRecipe() ? L2Skill.SKILL_CREATE_DWARVEN : L2Skill.SKILL_CREATE_COMMON;
		skillLevel = player.getSkillLevel(skillId);
		skill = player.getKnownSkill(skillId);

		player.isInCraftMode(true);

		if (player.isAlikeDead())
		{
			player.sendMessage("Dead people don't craft.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			abort();
			return;
		}

		if (target.isAlikeDead())
		{
			target.sendMessage("Dead customers can't use manufacture.");
			target.sendPacket(ActionFailed.STATIC_PACKET);
			abort();
			return;
		}

		if (target.isProcessingTransaction())
		{
			target.sendMessage("You are busy.");
			target.sendPacket(ActionFailed.STATIC_PACKET);
			abort();
			return;
		}

		if (player.isProcessingTransaction())
		{
			if (player != target)
			{
				target.sendMessage("Manufacturer " + player.getName() + " is busy.");
			}
			player.sendPacket(ActionFailed.STATIC_PACKET);
			abort();
			return;
		}

		// validate recipe list
		if ((recipe == null) || (recipe.getRecipeComponents().length == 0))
		{
			player.sendMessage("No such recipe");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			abort();
			return;
		}

		manaRequired = recipe.getMpCost();

		// validate skill level
		if (recipe.getLevel() > skillLevel)
		{
			player.sendMessage("Need skill level " + recipe.getLevel());
			player.sendPacket(ActionFailed.STATIC_PACKET);
			abort();
			return;
		}

		// check that customer can afford to pay for creation services
		if (player != target)
		{
			for (L2ManufactureItem temp : player.getCreateList().getList())
				if (temp.getRecipeId() == recipe.getId()) // find recipe for item we want manufactured
				{
					price = temp.getCost();
					if (target.getAdena() < price) // check price
					{
						target.sendPacket(new SystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
						abort();
						return;
					}
					break;
				}
		}

		// make temporary items
		if ((items = listItems(false)) == null)
		{
			abort();
			return;
		}

		// calculate reference price
		for (TempItem i : items)
		{
			materialsRefPrice += i.getReferencePrice() * i.getQuantity();
			totalItems += i.getQuantity();
		}
		// initial mana check requires MP as written on recipe
		if (player.getStatus().getCurrentMp() < manaRequired)
		{
			target.sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_MP));
			abort();
			return;
		}

		// determine number of creation passes needed
		// can "equip" skillLevel items each pass
		creationPasses = (totalItems / skillLevel) + ((totalItems % skillLevel) != 0 ? 1 : 0);

		if (Config.ALT_GAME_CREATION && creationPasses != 0) // update mana required to "per pass"
			manaRequired /= creationPasses; // checks to validateMp() will only need portion of mp for one pass

		updateMakeInfo(true);
		updateCurMp();
		updateCurLoad();

		player.isInCraftMode(false);
		isValid = true;
	}

	public void run()
	{
		if (!Config.IS_CRAFTING_ENABLED)
		{
			target.sendMessage("Item creation is currently disabled.");
			abort();
			return;
		}

		if (player == null || target == null)
		{
			_log.warn("player or target == null (disconnected?), aborting" + target + player);
			abort();
			return;
		}

		if (player.isOnline() == 0 || target.isOnline() == 0)
		{
			_log.warn("player or target is not online, aborting " + target + player);
			abort();
			return;
		}

		// this runnable task should be launched only in craft mode
		if (Config.ALT_GAME_CREATION && !CraftManager.isPlayerCrafting(player))
		{
			if (target != player)
			{
				target.sendMessage("Manufacture aborted");
				player.sendMessage("Manufacture aborted");
			}
			else
			{
				player.sendMessage("Item creation aborted");
			}

			abort();
			return;
		}

		if (Config.ALT_GAME_CREATION && !items.isEmpty())
		{

			if (!validateMp())
				return; // check mana
			player.reduceCurrentMp(manaRequired); // use some mp
			updateCurMp(); // update craft window mp bar

			grabSomeItems(); // grab (equip) some more items with a nice msg to player

			// if still not empty, schedule another pass
			if (!items.isEmpty())
			{
				// divided by RATE_CONSUMABLES_COST to remove craft time increase on higher consumables rates
				delay = (int) (Config.ALT_GAME_CREATION_SPEED * player.getStat().getMReuseRate(skill) * GameTimeController.TICKS_PER_SECOND / Config.RATE_CRAFT_COST)
						* GameTimeController.MILLIS_IN_TICK;

				// FIXME: please fix this packet to show crafting animation
				// (somebody)
				MagicSkillUse msk = new MagicSkillUse(player, skillId, skillLevel, delay, 0);
				player.broadcastPacket(msk);

				player.sendPacket(new SetupGauge(0, delay));
				ThreadPoolManager.getInstance().scheduleGeneral(this, 100 + delay);
			}
			else
			{
				// for alt mode, sleep delay msec before finishing
				player.sendPacket(new SetupGauge(0, delay));

				try
				{
					Thread.sleep(delay);
				}
				catch (InterruptedException e)
				{
				}
				finally
				{
					finishCrafting();
				}
			}
		} // for old craft mode just finish
		else
			finishCrafting();
	}

	private void finishCrafting()
	{
		if (!Config.ALT_GAME_CREATION)
			player.reduceCurrentMp(manaRequired);

		// first take adena for manufacture
		if ((target != player) && price > 0) // customer must pay for services
		{
			// attempt to pay for item
			L2ItemInstance adenatransfer = target.transferItem("PayManufacture", target.getInventory().getAdenaInstance().getObjectId(), price, player
					.getInventory(), player);

			if (adenatransfer == null)
			{
				target.sendPacket(new SystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
				abort();
				return;
			}
		}

		if ((items = listItems(true)) == null) // this line actually takes materials from inventory
		{ // handle possible cheaters here (they click craft then try to get rid of items in order to get free craft)
		}
		else if (Rnd.get(100) < recipe.getSuccessRate())
		{
			RewardPlayer(); // and immediately puts created item in its place
			updateMakeInfo(true);
		}
		else
		{
			player.sendMessage("Item(s) failed to create");
			if (target != player)
				target.sendMessage("Item(s) failed to create");

			updateMakeInfo(false);
		}
		// update load and mana bar of craft window
		updateCurMp();
		updateCurLoad();
		CraftManager.requestMakeItemAbort(player);
		player.isInCraftMode(false);
		target.sendPacket(new ItemList(target, false));
	}

	private void updateMakeInfo(boolean success)
	{
		if (target == player)
			target.sendPacket(new RecipeItemMakeInfo(recipe.getId(), target, success));
		else
			target.sendPacket(new RecipeShopItemInfo(player.getObjectId(), recipe.getId()));
	}

	private void updateCurLoad()
	{
		StatusUpdate su = new StatusUpdate(target.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, target.getCurrentLoad());
		target.sendPacket(su);
	}

	private void updateCurMp()
	{
		StatusUpdate su = new StatusUpdate(target.getObjectId());
		su.addAttribute(StatusUpdate.CUR_MP, (int) target.getStatus().getCurrentMp());
		target.sendPacket(su);
	}

	private void grabSomeItems()
	{
		int numItems = skillLevel;

		while (numItems > 0 && !items.isEmpty())
		{
			TempItem item = items.get(0);

			int count = item.getQuantity();
			if (count >= numItems)
				count = numItems;

			item.setQuantity(item.getQuantity() - count);
			if (item.getQuantity() <= 0)
				items.remove(0);
			else
				items.set(0, item);

			numItems -= count;

			if (target == player)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2_EQUIPPED); // you
																						// equipped
																						// ...
				sm.addNumber(count);
				sm.addItemNameById(item.getItemId());
				player.sendPacket(sm);
			}
			else
				target.sendMessage("Manufacturer " + player.getName() + " used " + count + " " + item.getItemName());
		}
	}

	private boolean validateMp()
	{
		if (player.getStatus().getCurrentMp() < manaRequired)
		{
			// rest (wait for MP)
			if (Config.ALT_GAME_CREATION)
			{
				player.sendPacket(new SetupGauge(0, delay));
				ThreadPoolManager.getInstance().scheduleGeneral(this, 100 + delay);
			}
			else
			// no rest - report no mana
			{
				target.sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_MP));
				abort();
			}
			return false;
		}
		return true;
	}

	private List<TempItem> listItems(boolean remove)
	{
		L2RecipeComponent[] recipes = recipe.getRecipeComponents();
		Inventory inv = target.getInventory();
		List<TempItem> materials = new FastList<TempItem>();

		for (L2RecipeComponent recipeComponent : recipes)
		{
			int quantity = recipe.isConsumable() ? (int) (recipeComponent.getQuantity() * Config.RATE_CRAFT_COST) : (int) recipeComponent.getQuantity();

			if (quantity > 0)
			{
				L2ItemInstance item = inv.getItemByItemId(recipeComponent.getItemId());

				// check materials
				if (item == null || item.getCount() < quantity)
				{
					target.sendMessage("You dont have the right elements for making this item"
							+ ((recipe.isConsumable() && Config.RATE_CRAFT_COST != 1) ? ".\nDue to server rates you need " + Config.RATE_CRAFT_COST
									+ "x more material than listed in recipe" : ""));
					abort();
					return null;
				}

				// make new temporary object, just for counting puroses

				TempItem temp = new TempItem(item, quantity);
				materials.add(temp);
			}
		}

		if (remove)
		{
			for (TempItem tmp : materials)
			{
				inv.destroyItemByItemId("Manufacture", tmp.getItemId(), tmp.getQuantity(), target, player);
			}
		}
		return materials;
	}

	private void abort()
	{
		updateMakeInfo(false);
		player.isInCraftMode(false);
		CraftManager.requestMakeItemAbort(player);
	}

	private void RewardPlayer()
	{
		int itemId = recipe.getItemId();
		int itemCount = recipe.getCount();

		if (player.isGM())
		{
			// DaDummy: this way we log _every_ gmtransfer with all related info
			String params = target.getName() + " - " + String.valueOf(itemCount) + " - 0 - " + String.valueOf(itemId) + " - "
					+ ItemTable.getInstance().getTemplate(itemId).getName();

			GMAudit.auditGMAction(player, "transferitem", "PcInventory", params);
		}

		L2ItemInstance createdItem = target.addItem("Manufacture", itemId, itemCount, target, true, true);

		if (target != player)
		{
			// inform manufacturer of earned profit
			SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_ADENA);
			sm.addNumber(price);
			player.sendPacket(sm);
		}

		if (Config.ALT_GAME_CREATION)
		{
			int recipeLevel = recipe.getLevel();
			int exp = createdItem.getReferencePrice() * itemCount;
			// one variation

			// exp -= materialsRefPrice; // mat. ref. price is not accurate so other method is better

			if (exp < 0)
				exp = 0;

			// another variation
			exp /= recipeLevel;
			for (int i = skillLevel; i > recipeLevel; i--)
				exp /= 4;

			int sp = exp / 10;

			// Added multiplication of Creation speed with XP/SP gain
			// slower crafting -> more XP, faster crafting -> less XP
			// you can use ALT_GAME_CREATION_XP_RATE/SP to
			// modify XP/SP gained (default = 1)

			player.addExpAndSp((int) player.calcStat(Stats.EXPSP_RATE, exp * Config.ALT_GAME_CREATION_XP_RATE * Config.ALT_GAME_CREATION_SPEED, null, null),
					(int) player.calcStat(Stats.EXPSP_RATE, sp * Config.ALT_GAME_CREATION_SP_RATE * Config.ALT_GAME_CREATION_SPEED, null, null));
		}
		updateMakeInfo(true); // success
	}

	/**
	 * @return the isValid
	 */
	public boolean isValid()
	{
		return isValid;
	}
}
