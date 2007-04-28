package net.sf.l2j.gameserver.recipes.dao.impl;

import java.io.File;
import java.util.Arrays;

import junit.framework.TestCase;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.recipes.model.L2Recipe;
import net.sf.l2j.gameserver.recipes.model.L2RecipeComponent;

public class TestL2RecipeDaoCsv extends TestCase
{
    
    public void testLoadDataWithValidFile ()
    {
        Config.DATAPACK_ROOT = new File (getClass().getResource(".").getFile().replace("%20", " ")); 
        
        L2RecipeDAOCsv l2RecipeDAOCsv = new L2RecipeDAOCsv();
        
        assertEquals(4, l2RecipeDAOCsv.getRecipesCount());
    }
    
    public void testLoadDataWithFileNotFound ()
    {
        Config.DATAPACK_ROOT = new File (System.getProperty("user.home")); 
        
        L2RecipeDAOCsv l2RecipeDAOCsv = new L2RecipeDAOCsv();
        
        assertEquals(0, l2RecipeDAOCsv.getRecipesCount());
    }    

    public void testGetRecipeByListId ()
    {
        Config.DATAPACK_ROOT = new File (getClass().getResource(".").getFile().replace("%20", " ")); 
        L2RecipeDAOCsv l2RecipeDAOCsv = new L2RecipeDAOCsv();
        assertEquals(4, l2RecipeDAOCsv.getRecipesCount());
        
        L2Recipe l2Recipe = l2RecipeDAOCsv.getRecipe(1);
        assertNotNull(l2Recipe);
        assertEquals("mk_broad_sword", l2Recipe.getRecipeName());
        L2Recipe l2RecipeNull = l2RecipeDAOCsv.getRecipe(12);
        assertNull(l2RecipeNull);
        
    }   
    
    public void testGetRecipeByRecId ()
    {
        Config.DATAPACK_ROOT = new File (getClass().getResource(".").getFile().replace("%20", " ")); 
        L2RecipeDAOCsv l2RecipeDAOCsv = new L2RecipeDAOCsv();
        assertEquals(4, l2RecipeDAOCsv.getRecipesCount());
        
        L2Recipe l2Recipe = l2RecipeDAOCsv.getRecipeById(1);
        assertNotNull(l2Recipe);
        assertEquals("mk_wooden_arrow", l2Recipe.getRecipeName());
        L2Recipe l2RecipeNull = l2RecipeDAOCsv.getRecipeById(12);
        assertNull(l2RecipeNull);
    }
    
    public void testGetRecipeByItemId ()
    {
        Config.DATAPACK_ROOT = new File (getClass().getResource(".").getFile().replace("%20", " ")); 
        L2RecipeDAOCsv l2RecipeDAOCsv = new L2RecipeDAOCsv();
        assertEquals(4, l2RecipeDAOCsv.getRecipesCount());
        
        L2Recipe l2Recipe = l2RecipeDAOCsv.getRecipeByItemId(1666);
        assertNotNull(l2Recipe);
        assertEquals("mk_wooden_arrow", l2Recipe.getRecipeName());
        assertTrue ( l2Recipe.isDwarvenRecipe());
        assertEquals(500, l2Recipe.getCount());
        assertEquals(4, l2Recipe.getLevel());
        assertEquals(30, l2Recipe.getMpCost());
        assertEquals(100, l2Recipe.getSuccessRate());
        L2RecipeComponent [] components = l2Recipe.getRecipeComponents();
        assertEquals(2, components.length);
        
        L2Recipe l2RecipeNull = l2RecipeDAOCsv.getRecipeById(3000);
        assertNull(l2RecipeNull);
    }     
    
    public void testGetRecipeIds ()
    {
        Config.DATAPACK_ROOT = new File (getClass().getResource(".").getFile().replace("%20", " ")); 
        L2RecipeDAOCsv l2RecipeDAOCsv = new L2RecipeDAOCsv();
        assertEquals(4, l2RecipeDAOCsv.getRecipesCount());
        
        int[] recipeIds = l2RecipeDAOCsv.getRecipeIds();
        assertNotNull(recipeIds);
        assertEquals(4, recipeIds.length);
        Arrays.sort(recipeIds);
        assertTrue(Arrays.binarySearch(recipeIds,1666)>=0);
        assertTrue(Arrays.binarySearch(recipeIds,5)<0);
    }        
    
}
