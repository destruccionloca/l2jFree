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

/**
 * 
 * @author FBIagent
 * 
 */

package net.sf.l2j.gameserver.items.dao.impl;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.items.dao.ExtractableItemsDAO;
import net.sf.l2j.gameserver.items.model.L2ExtractableItem;
import net.sf.l2j.gameserver.items.model.L2ExtractableProductItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 * 
 * Implementation of extractable items DAO by csv file.
 * A csv file is composed by line formated as follow : 
 * 
 * #ItemID;Production1,Quantity1,Chance1[;Production2,Quantity2,Chance2... ;ProductionN,QuantityN,ChanceN]
 * 
 * Line with bad format are ignored.
 * All lines are keeped in memory, there is no reloading of the files
 */
public class ExtractableItemsDAOCsv implements ExtractableItemsDAO
{   
    /**
     * Logger 
     */
    private static final Log _log = LogFactory.getLog(ExtractableItemsDAOCsv.class.getName());
    
    /**
     * Map for all L2ExtractableItem, identified by the item id
     */
    private Map<Integer, L2ExtractableItem> _items;
    
    /**
     * Constructor :
     * read the file and load all data in a private map
     */
    public ExtractableItemsDAOCsv()
    {   
        // o Local variable initialization
        // --------------------------------
        _items = new FastMap<Integer,L2ExtractableItem>();
        int lineCount = 0;
        Scanner s=null;

        try
        {
            s = new Scanner(new File(Config.DATAPACK_ROOT, "data/extractable_items.csv")); 
        }
        catch (Exception e)
        {
            _log.error("Extractable items data: Can not find '"+Config.DATAPACK_ROOT+"/data/extractable_items.csv'");
            return;
        }
        
        // o Read all lines and parse it
        // ------------------------------
        while (s.hasNextLine())
        {           
            lineCount++;
            
            String line = s.nextLine();
            
            // Ignore lines if line empty or starts with a comment
            // ---------------------------------------------------
            if (line.startsWith("#"))
                continue;
            else if (line.equals(""))
                continue;
            
            // Split line with a separator
            // ----------------------------
            String[] lineSplit = line.split(";");
            boolean ok = true;
            int itemID = 0;

            try
            {
                itemID = Integer.parseInt(lineSplit[0]);
            }
            catch (Exception e)
            {
                _log.error("Extractable items data: Error in line " + lineCount + " -> invalid item id or wrong seperator after item id!");
                _log.error("        " + line);
                ok = false;             
            }
            
            if (!ok)
                continue;
            // Initialize a list to store all product items
            // ----------------------------------------------
            List<L2ExtractableProductItem> product_temp = new FastList<L2ExtractableProductItem>(); 
                        
            for (int i=0;i<lineSplit.length-1;i++)
            {
                ok = true;

                String[] lineSplit2 = lineSplit[i+1].split(",");
                int production = 0,
                    amount = 0,
                    chance = 0;

                try
                {
                    production = Integer.parseInt(lineSplit2[0]);
                    amount = Integer.parseInt(lineSplit2[1]);
                    chance = Integer.parseInt(lineSplit2[2]);
                }
                catch (Exception e)
                {
                    _log.error("Extractable items data: Error in line " + lineCount + " -> incomplete/invalid production data or wrong seperator!");
                    _log.error("        " + line);
                    ok = false;
                }
                
                if (!ok)
                    continue;
                
                L2ExtractableProductItem product = new L2ExtractableProductItem(production,amount,chance);
                product_temp.add(product);              
            }
            
            // coherence check
            // -----------------
            int fullChances = 0;
            
            for (L2ExtractableProductItem Pi : product_temp)
            {
                fullChances += Pi.getChance();
            }
            
            if (fullChances > 100)
            {
                _log.error("Extractable items data: Error in line " + lineCount + " -> all chances together are more then 100!");
                _log.error("        " + line);
                continue;
            }
            // Store this extractable item
            L2ExtractableItem product = new L2ExtractableItem(itemID, product_temp);
            _items.put(itemID, product);            
        }
        
        s.close();
        _log.info("Extractable items data: Loaded " + _items.size() + " extractable items!");
    }
    
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.datatables.ExtractableItemsDAO#getExtractableItem(int)
     */
    public L2ExtractableItem getExtractableItem(int itemID)
    {
        return _items.get(itemID);
    }   
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.datatables.ExtractableItemsDAO#itemIDs()
     */
    public int[] itemIDs()
    {
        int size = _items.size();
        int[] result = new int[size];
        int i = 0;
        for (L2ExtractableItem ei : _items.values())
        {
            result[i] = ei.getItemId();
            i++;
        }
        return result;
    }
}
