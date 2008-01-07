package net.sf.l2j.gameserver.recipes.model;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class TestL2RecipeComponent extends TestCase
{
    /**
     * test that equals works
     *
     */
    public void testEquals ()
    {
        L2RecipeComponent recipeComponentRef = new L2RecipeComponent (1,2);
        
        L2RecipeComponent recipeComponentOther = new L2RecipeComponent (1,2);
        
        assertEquals(recipeComponentRef, recipeComponentOther);
    }
    
    /**
     * test hashcode method
     *
     */
    public void testHashCode ()
    {
        Map<L2RecipeComponent, Integer> mapTest = new HashMap<L2RecipeComponent, Integer>();
        L2RecipeComponent recipeComponentRef = new L2RecipeComponent (1,2);
        
        mapTest.put(recipeComponentRef, 1 );
        
        L2RecipeComponent recipeComponentOther = new L2RecipeComponent (1,2);
        
        assertTrue (mapTest.containsKey(recipeComponentOther));
        
    }
}
