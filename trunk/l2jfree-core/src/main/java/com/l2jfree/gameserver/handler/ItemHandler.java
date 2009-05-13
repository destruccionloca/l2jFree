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
package com.l2jfree.gameserver.handler;

import com.l2jfree.gameserver.handler.itemhandlers.AdvQuestItems;
import com.l2jfree.gameserver.handler.itemhandlers.BallistaBomb;
import com.l2jfree.gameserver.handler.itemhandlers.BeastSoulShot;
import com.l2jfree.gameserver.handler.itemhandlers.BeastSpice;
import com.l2jfree.gameserver.handler.itemhandlers.BeastSpiritShot;
import com.l2jfree.gameserver.handler.itemhandlers.BlessedSpiritShot;
import com.l2jfree.gameserver.handler.itemhandlers.Book;
import com.l2jfree.gameserver.handler.itemhandlers.CharChangePotions;
import com.l2jfree.gameserver.handler.itemhandlers.ChestKey;
import com.l2jfree.gameserver.handler.itemhandlers.CrystalCarol;
import com.l2jfree.gameserver.handler.itemhandlers.DoorKey;
import com.l2jfree.gameserver.handler.itemhandlers.EnchantAttr;
import com.l2jfree.gameserver.handler.itemhandlers.EnchantScrolls;
import com.l2jfree.gameserver.handler.itemhandlers.EnergyStone;
import com.l2jfree.gameserver.handler.itemhandlers.ExtractableItems;
import com.l2jfree.gameserver.handler.itemhandlers.Firework;
import com.l2jfree.gameserver.handler.itemhandlers.FishShots;
import com.l2jfree.gameserver.handler.itemhandlers.ForgottenScroll;
import com.l2jfree.gameserver.handler.itemhandlers.Harvester;
import com.l2jfree.gameserver.handler.itemhandlers.HolyWater;
import com.l2jfree.gameserver.handler.itemhandlers.Maps;
import com.l2jfree.gameserver.handler.itemhandlers.MercTicket;
import com.l2jfree.gameserver.handler.itemhandlers.MysteryPotion;
import com.l2jfree.gameserver.handler.itemhandlers.PetFood;
import com.l2jfree.gameserver.handler.itemhandlers.Potions;
import com.l2jfree.gameserver.handler.itemhandlers.Recipes;
import com.l2jfree.gameserver.handler.itemhandlers.Remedy;
import com.l2jfree.gameserver.handler.itemhandlers.RollingDice;
import com.l2jfree.gameserver.handler.itemhandlers.ScrollOfEscape;
import com.l2jfree.gameserver.handler.itemhandlers.ScrollOfResurrection;
import com.l2jfree.gameserver.handler.itemhandlers.Scrolls;
import com.l2jfree.gameserver.handler.itemhandlers.Seed;
import com.l2jfree.gameserver.handler.itemhandlers.SevenSignsRecord;
import com.l2jfree.gameserver.handler.itemhandlers.SoulCrystals;
import com.l2jfree.gameserver.handler.itemhandlers.SoulShots;
import com.l2jfree.gameserver.handler.itemhandlers.SpecialXMas;
import com.l2jfree.gameserver.handler.itemhandlers.SpiritLake;
import com.l2jfree.gameserver.handler.itemhandlers.SpiritShot;
import com.l2jfree.gameserver.handler.itemhandlers.SummonItems;
import com.l2jfree.gameserver.handler.itemhandlers.TransformationItems;
import com.l2jfree.gameserver.handler.itemhandlers.WorldMap;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.actor.L2Playable;
import com.l2jfree.util.NumberHandlerRegistry;

public final class ItemHandler extends NumberHandlerRegistry<IItemHandler>
{
	private static ItemHandler _instance;
	
	public static ItemHandler getInstance()
	{
		if (_instance == null)
			_instance = new ItemHandler();
		
		return _instance;
	}
	
	private ItemHandler()
	{
		registerItemHandler(new AdvQuestItems());
		registerItemHandler(new BallistaBomb());
		registerItemHandler(new BeastSoulShot());
		registerItemHandler(new BeastSpice());
		registerItemHandler(new BeastSpiritShot());
		registerItemHandler(new BlessedSpiritShot());
		registerItemHandler(new Book());
		registerItemHandler(new CharChangePotions());
		registerItemHandler(new ChestKey());
		registerItemHandler(new CrystalCarol());
		registerItemHandler(new DoorKey());
		registerItemHandler(new EnchantAttr());
		registerItemHandler(new EnchantScrolls());
		registerItemHandler(new EnergyStone());
		registerItemHandler(new ExtractableItems());
		registerItemHandler(new Firework());
		registerItemHandler(new FishShots());
		registerItemHandler(new ForgottenScroll());
		registerItemHandler(new Harvester());
		registerItemHandler(new HolyWater());
		registerItemHandler(new Maps());
		registerItemHandler(new MercTicket());
		registerItemHandler(new MysteryPotion());
		registerItemHandler(new PetFood());
		registerItemHandler(new Potions());
		registerItemHandler(new Recipes());
		registerItemHandler(new Remedy());
		registerItemHandler(new RollingDice());
		registerItemHandler(new ScrollOfEscape());
		registerItemHandler(new ScrollOfResurrection());
		registerItemHandler(new Scrolls());
		registerItemHandler(new Seed());
		registerItemHandler(new SevenSignsRecord());
		registerItemHandler(new SoulCrystals());
		registerItemHandler(new SoulShots());
		registerItemHandler(new SpecialXMas());
		registerItemHandler(new SpiritLake());
		registerItemHandler(new SpiritShot());
		registerItemHandler(new SummonItems());
		registerItemHandler(new TransformationItems());
		registerItemHandler(new WorldMap());
		
		_log.info("ItemHandler: Loaded " + size() + " handlers.");
	}
	
	public void registerItemHandler(IItemHandler handler)
	{
		registerAll(handler, handler.getItemIds());
	}
	
	public boolean hasItemHandler(int itemId)
	{
		return get(itemId) != null;
	}
	
	public boolean useItem(int itemId, L2Playable playable, L2ItemInstance item)
	{
		final IItemHandler handler = get(itemId);
		
		if (handler == null)
		{
			_log.warn("No item handler registered for item ID " + itemId + ".");
			return false;
		}
		
		handler.useItem(playable, item);
		return true;
	}
}
