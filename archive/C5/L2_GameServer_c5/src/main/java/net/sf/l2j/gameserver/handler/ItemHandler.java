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
package net.sf.l2j.gameserver.handler;

import java.util.Map;
import java.util.TreeMap;

import net.sf.l2j.gameserver.handler.itemhandlers.BeastSoulShot;
import net.sf.l2j.gameserver.handler.itemhandlers.BeastSpiritShot;
import net.sf.l2j.gameserver.handler.itemhandlers.BlessedSpiritShot;
import net.sf.l2j.gameserver.handler.itemhandlers.CharChangePotions;
import net.sf.l2j.gameserver.handler.itemhandlers.ChestKey;
import net.sf.l2j.gameserver.handler.itemhandlers.CrystalCarol;
import net.sf.l2j.gameserver.handler.itemhandlers.EnchantScrolls;
import net.sf.l2j.gameserver.handler.itemhandlers.EnergyStone;
import net.sf.l2j.gameserver.handler.itemhandlers.ExtractableItems;
import net.sf.l2j.gameserver.handler.itemhandlers.Firework;
import net.sf.l2j.gameserver.handler.itemhandlers.FishShots;
import net.sf.l2j.gameserver.handler.itemhandlers.Guide;
import net.sf.l2j.gameserver.handler.itemhandlers.Harvester;
import net.sf.l2j.gameserver.handler.itemhandlers.MercTicket;
import net.sf.l2j.gameserver.handler.itemhandlers.MysteryPotion;
import net.sf.l2j.gameserver.handler.itemhandlers.Potions;
import net.sf.l2j.gameserver.handler.itemhandlers.Recipes;
import net.sf.l2j.gameserver.handler.itemhandlers.Remedy;
import net.sf.l2j.gameserver.handler.itemhandlers.RollingDice;
import net.sf.l2j.gameserver.handler.itemhandlers.ScrollOfEscape;
import net.sf.l2j.gameserver.handler.itemhandlers.ScrollOfResurrection;
import net.sf.l2j.gameserver.handler.itemhandlers.Scrolls;
import net.sf.l2j.gameserver.handler.itemhandlers.Seed;
import net.sf.l2j.gameserver.handler.itemhandlers.SevenSignsRecord;
import net.sf.l2j.gameserver.handler.itemhandlers.SoulCrystals;
import net.sf.l2j.gameserver.handler.itemhandlers.SoulShots;
import net.sf.l2j.gameserver.handler.itemhandlers.SpiritShot;
import net.sf.l2j.gameserver.handler.itemhandlers.SummonItems;
import net.sf.l2j.gameserver.handler.itemhandlers.WorldMap;

/**
 * This class manages handlers of items
 *
 * @version $Revision: 1.1.4.3 $ $Date: 2005/03/27 15:30:09 $
 */
public class ItemHandler
{
	//private final static Log _log = LogFactory.getLog(ItemHandler.class.getName());
    private static ItemHandler _instance;
    
    private Map<Integer, IItemHandler> _datatable;
    
    /**
     * Create ItemHandler if doesn't exist and returns ItemHandler
     * @return ItemHandler
     */
    public static ItemHandler getInstance()
    {
        if (_instance == null)
        {
            _instance = new ItemHandler();
        }
        return _instance;
    }
    
    /**
     * Returns the number of elements contained in datatable
     * @return int : Size of the datatable
     */
    public int size()
    {
        return _datatable.size();
    }
    
    /**
     * Constructor of ItemHandler
     */
    private ItemHandler()
    {
        _datatable = new TreeMap<Integer, IItemHandler>();
        this.registerItemHandler(new ScrollOfEscape());
        this.registerItemHandler(new ScrollOfResurrection());
        this.registerItemHandler(new SoulShots());
        this.registerItemHandler(new SpiritShot());
        this.registerItemHandler(new BeastSoulShot());
        this.registerItemHandler(new BeastSpiritShot());
        this.registerItemHandler(new BlessedSpiritShot());
        this.registerItemHandler(new ChestKey());        
        this.registerItemHandler(new WorldMap());
        this.registerItemHandler(new Potions());
        this.registerItemHandler(new Recipes());
        this.registerItemHandler(new RollingDice());
        this.registerItemHandler(new MysteryPotion());
        this.registerItemHandler(new EnchantScrolls());
        this.registerItemHandler(new Remedy());
        this.registerItemHandler(new Guide());
        this.registerItemHandler(new Scrolls());
        this.registerItemHandler(new CrystalCarol());
        this.registerItemHandler(new SoulCrystals());
        this.registerItemHandler(new SevenSignsRecord());
        this.registerItemHandler(new CharChangePotions());
        this.registerItemHandler(new Firework());
        this.registerItemHandler(new Seed());
        this.registerItemHandler(new Harvester());
        this.registerItemHandler(new MercTicket());
        this.registerItemHandler(new FishShots());
        this.registerItemHandler(new ExtractableItems());
        this.registerItemHandler(new SummonItems());      
        this.registerItemHandler(new EnergyStone());        
    }
    
    /**
     * Adds handler of item type in <I>datatable</I>.<BR><BR>
     * <B><I>Concept :</I></U><BR>
     * This handler is put in <I>datatable</I> Map &lt;Integer ; IItemHandler &gt; for each ID corresponding to an item type 
     * (existing in classes of package itemhandlers) sets as key of the Map. 
     * @param handler (IItemHandler)
     */
    public void registerItemHandler(IItemHandler handler)
    {
        // Get all ID corresponding to the item type of the handler
        int[] ids = handler.getItemIds();
        // Add handler for each ID found
        for (int i = 0; i < ids.length; i++)
        {
            _datatable.put(new Integer(ids[i]), handler);
        }
    }
    
    /**
     * Returns the handler of the item
     * @param itemId : int designating the itemID
     * @return IItemHandler
     */
    public IItemHandler getItemHandler(int itemId)
    {
        return _datatable.get(new Integer(itemId));
    }
}
