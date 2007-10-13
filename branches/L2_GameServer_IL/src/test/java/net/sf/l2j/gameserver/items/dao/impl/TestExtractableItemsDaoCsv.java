package net.sf.l2j.gameserver.items.dao.impl;

import java.io.File;
import java.util.Arrays;

import junit.framework.TestCase;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.items.model.L2ExtractableItem;

public class TestExtractableItemsDaoCsv extends TestCase
{
    
    public void testLoadDataWithValidFile ()
    {
        Config.DATAPACK_ROOT = new File (getClass().getResource(".").getFile().replace("%20", " ")); 
        
        ExtractableItemsDAOCsv extractableItemsDAOCsv = new ExtractableItemsDAOCsv();
        
        assertEquals(7, extractableItemsDAOCsv.itemIDs().length);
    }
    
    public void testLoadDataWithFileNotFound ()
    {
        Config.DATAPACK_ROOT = new File (System.getProperty("user.home")); 
        
        ExtractableItemsDAOCsv extractableItemsDAOCsv = new ExtractableItemsDAOCsv();
        
        assertEquals(0, extractableItemsDAOCsv.itemIDs().length);
    }    

    public void testGetExtractableItemById ()
    {
        Config.DATAPACK_ROOT = new File (getClass().getResource(".").getFile().replace("%20", " ")); 
        ExtractableItemsDAOCsv extractableItemsDAOCsv = new ExtractableItemsDAOCsv();
        assertEquals(7, extractableItemsDAOCsv.itemIDs().length);
        
        L2ExtractableItem l2ExtractableItem = extractableItemsDAOCsv.getExtractableItem(7629);
        assertNotNull(l2ExtractableItem);
        L2ExtractableItem l2ExtractableItemNull = extractableItemsDAOCsv.getExtractableItem(1);
        assertNull(l2ExtractableItemNull);
        
    }   

    public void testGetItemIds ()
    {
        Config.DATAPACK_ROOT = new File (getClass().getResource(".").getFile().replace("%20", " ")); 
        ExtractableItemsDAOCsv extractableItemsDAOCsv = new ExtractableItemsDAOCsv();
        
        int[] itemIds = extractableItemsDAOCsv.itemIDs();
        assertNotNull(itemIds);
        assertEquals(7, itemIds.length);
        Arrays.sort(itemIds);
        assertTrue(Arrays.binarySearch(itemIds,7633)>=0);
        assertTrue(Arrays.binarySearch(itemIds,5)<0);
    }        
    
}
