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
package net.sf.l2j.gameserver.items.service;

import java.io.File;

import junit.framework.TestCase;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.items.dao.impl.ExtractableItemsDAOCsv;
import net.sf.l2j.gameserver.items.model.L2ExtractableItem;

public class TestExtractableItemService extends TestCase
{
    public void testGetExtractableItemById ()
    {
        Config.DATAPACK_ROOT = new File (getClass().getResource(".").getFile().replace("%20", " ")); 
        
        ExtractableItemsService extractableItemsService = new ExtractableItemsService();
        extractableItemsService.setExtractableItemsDAO(new ExtractableItemsDAOCsv());
        
        L2ExtractableItem l2ExtractableItem = extractableItemsService.getExtractableItem(7629);
        assertNotNull(l2ExtractableItem);
        assertEquals (7629,l2ExtractableItem.getItemId());
    }
    
    public void testgetItemIds ()
    {
        Config.DATAPACK_ROOT = new File (getClass().getResource(".").getFile().replace("%20", " ")); 
        
        ExtractableItemsService extractableItemsService = new ExtractableItemsService();
        extractableItemsService.setExtractableItemsDAO(new ExtractableItemsDAOCsv());
        
        int[] items = extractableItemsService.itemIDs();
        assertNotNull(items);
        assertEquals (7,items.length);
    }    
}
