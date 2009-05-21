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
