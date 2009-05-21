package net.sf.l2j.gameserver.recipes.dao.impl;

import java.io.File;
import java.util.Arrays;

import junit.framework.TestCase;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.recipes.model.L2Recipe;
import net.sf.l2j.gameserver.recipes.model.L2RecipeComponent;

public class TestL2RecipeDaoXml extends TestCase
{
    
    public void testLoadDataWithValidFile ()
    {
        Config.DATAPACK_ROOT = new File (getClass().getResource(".").getFile().replace("%20", " ")); 
        
        L2RecipeDAOXml l2RecipeDAOXml = new L2RecipeDAOXml();
        
        assertEquals(4, l2RecipeDAOXml.getRecipesCount());
    }
    
    public void testLoadDataWithFileNotFound ()
    {
        Config.DATAPACK_ROOT = new File (System.getProperty("user.home")); 
        
        L2RecipeDAOXml l2RecipeDAOXml = new L2RecipeDAOXml();
        
        assertEquals(0, l2RecipeDAOXml.getRecipesCount());
    }    

    public void testGetRecipeByListId ()
    {
        Config.DATAPACK_ROOT = new File (getClass().getResource(".").getFile().replace("%20", " ")); 
        L2RecipeDAOXml l2RecipeDAOXml = new L2RecipeDAOXml();
        assertEquals(4, l2RecipeDAOXml.getRecipesCount());
        
        L2Recipe l2Recipe = l2RecipeDAOXml.getRecipe(1);
        assertNotNull(l2Recipe);
        assertEquals("mk_broad_sword", l2Recipe.getRecipeName());
        L2Recipe l2RecipeNull = l2RecipeDAOXml.getRecipe(12);
        assertNull(l2RecipeNull);
        
    }   
    
    public void testGetRecipeByRecId ()
    {
        Config.DATAPACK_ROOT = new File (getClass().getResource(".").getFile().replace("%20", " ")); 
        L2RecipeDAOXml l2RecipeDAOXml = new L2RecipeDAOXml();
        assertEquals(4, l2RecipeDAOXml.getRecipesCount());
        
        L2Recipe l2Recipe = l2RecipeDAOXml.getRecipeById(1);
        assertNotNull(l2Recipe);
        assertEquals("mk_wooden_arrow", l2Recipe.getRecipeName());
        L2Recipe l2RecipeNull = l2RecipeDAOXml.getRecipeById(12);
        assertNull(l2RecipeNull);
    }
    
    public void testGetRecipeByItemId ()
    {
        Config.DATAPACK_ROOT = new File (getClass().getResource(".").getFile().replace("%20", " ")); 
        L2RecipeDAOXml l2RecipeDAOXml = new L2RecipeDAOXml();
        assertEquals(4, l2RecipeDAOXml.getRecipesCount());
        
        L2Recipe l2Recipe = l2RecipeDAOXml.getRecipeByItemId(1666);
        assertNotNull(l2Recipe);
        assertEquals("mk_wooden_arrow", l2Recipe.getRecipeName());
        assertTrue ( l2Recipe.isDwarvenRecipe());
        assertEquals(500, l2Recipe.getCount());
        assertEquals(1, l2Recipe.getLevel());
        assertEquals(30, l2Recipe.getMpCost());
        assertEquals(100, l2Recipe.getSuccessRate());
        L2RecipeComponent [] components = l2Recipe.getRecipeComponents();
        assertEquals(2, components.length);
        
        L2Recipe l2RecipeNull = l2RecipeDAOXml.getRecipeById(3000);
        assertNull(l2RecipeNull);
    }     
    
    public void testGetRecipeIds ()
    {
        Config.DATAPACK_ROOT = new File (getClass().getResource(".").getFile().replace("%20", " ")); 
        L2RecipeDAOXml l2RecipeDAOXml = new L2RecipeDAOXml();
        assertEquals(4, l2RecipeDAOXml.getRecipesCount());
        
        int[] recipeIds = l2RecipeDAOXml.getRecipeIds();
        assertNotNull(recipeIds);
        assertEquals(4, recipeIds.length);
        Arrays.sort(recipeIds);
        assertTrue(Arrays.binarySearch(recipeIds,1666)>=0);
        assertTrue(Arrays.binarySearch(recipeIds,5)<0);
    }        

}
