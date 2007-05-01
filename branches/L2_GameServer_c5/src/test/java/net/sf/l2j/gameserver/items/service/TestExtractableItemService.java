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
