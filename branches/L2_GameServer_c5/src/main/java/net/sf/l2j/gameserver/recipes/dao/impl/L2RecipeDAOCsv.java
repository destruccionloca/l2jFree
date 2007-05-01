package net.sf.l2j.gameserver.recipes.dao.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.recipes.dao.IL2RecipeDAO;
import net.sf.l2j.gameserver.recipes.model.L2Recipe;
import net.sf.l2j.gameserver.recipes.model.L2RecipeComponent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Csv implementation for L2RecipeDAO
 * This implementation read all recipes in a csv files 
 * 
 * Example : 
 * dwarven;mk_wooden_arrow;1;1666;1;[1864(4)],[1869(2)];17;500;[1666(1)],[57(5400000)];30;100;
 *
 */
public class L2RecipeDAOCsv implements IL2RecipeDAO
{
    /** Logger */
    private final static Log _log = LogFactory.getLog(L2RecipeDAOCsv.class.getName());
    
    /** private map to store all recipes */
    private Map<Integer, L2Recipe> _lists;
    
    /**
     * Constructor 
     * The constructor load the file and parse it 
     */
    public L2RecipeDAOCsv()
    {
        _lists = new FastMap<Integer, L2Recipe>();
        String line = null;
        LineNumberReader lnr = null;
        
        try
        {
            File recipesData = new File(Config.DATAPACK_ROOT, "data/recipes.csv");
            lnr = new LineNumberReader(new BufferedReader(new FileReader(recipesData)));
            
            while ((line = lnr.readLine()) != null)
            {
                if (line.trim().length() == 0 || line.startsWith("#"))
                    continue;
                
                parseLine(line);
                
            }
            _log.info("RecipeController: Loaded " + _lists.size() + " Recipes.");
        }
        catch (Exception e)
        {
            if (lnr != null)

                _log.warn( "error while creating recipe controller in linenr: "
                         + lnr.getLineNumber(), e);

            else

                _log.warn("No recipes were found in data folder " + Config.DATAPACK_ROOT );

        }
        finally
        {
            try
            {
                lnr.close();
            }
            catch (Exception e)
            {
            }
        }
    }    

    /**
     * Return a L2Recipe by its place in the list.
     * 
     * @see net.sf.l2j.gameserver.recipes.dao.IL2RecipeDAO#getRecipe(int)
     * @return a L2Recipe
     * @param listId or null if it doesn't exist
     */
    public L2Recipe getRecipe(int listId)
    {
        return _lists.get(listId);
    }

    /**
     * 
     * @param recId
     * @return L2Recipe for the the given recipe id or null if not found
     * @see net.sf.l2j.gameserver.recipes.dao.IL2RecipeDAO#getRecipeById(int)
     */
    public L2Recipe getRecipeById(int recId)
    {
        for (int i = 0; i < _lists.size(); i++)
        {
            L2Recipe find = _lists.get(new Integer(i));
            if (find.getId() == recId)
            {
                return find;
            }
        }
        return null;
    }

    /** 
     * Retrieve the recipe for the given item id
     * @param itemId
     * @return L2Recipe for this itemId or null if not found
     * 
     * @see net.sf.l2j.gameserver.recipes.dao.IL2RecipeDAO#getRecipeByItemId(int)
     */
    public L2Recipe getRecipeByItemId(int itemId)
    {
        for (int i = 0; i < _lists.size(); i++)
        {
            L2Recipe find = _lists.get(new Integer(i));
            if (find.getRecipeId() == itemId)
            {
                return find;
            }
        }
        return null;
    }
    
    /**
     * Return the recipe list size
     * @return recipe list size
     * @see net.sf.l2j.gameserver.recipes.dao.IL2RecipeDAO#getRecipesCount()
     */
    public int getRecipesCount()
    {
        return _lists.size();
    }

    
    /**
     * 
     * @param line
     */
    private void parseLine(String line)
    {
        try
        {
            StringTokenizer st = new StringTokenizer(line, ";");
            List<L2RecipeComponent> recipePartList = new FastList<L2RecipeComponent>();
            
            //we use common/dwarf for easy reading of the recipes.csv file 
            String recipeTypeString = st.nextToken();
            
            // now parse the string into a boolean 
            boolean isDwarvenRecipe;
            
            if (recipeTypeString.equalsIgnoreCase("dwarven")) isDwarvenRecipe = true;
            else if (recipeTypeString.equalsIgnoreCase("common")) isDwarvenRecipe = false;
            else
            { //prints a helpfull message 
                _log.warn("Error parsing recipes.csv, unknown recipe type " + recipeTypeString);
                return;
            }
            
            String recipeName = st.nextToken();
            int id = Integer.parseInt(st.nextToken());
            int recipeId = Integer.parseInt(st.nextToken());
            int level = Integer.parseInt(st.nextToken());
            
            //material
            StringTokenizer st2 = new StringTokenizer(st.nextToken(), "[],");
            while (st2.hasMoreTokens())
            {
                StringTokenizer st3 = new StringTokenizer(st2.nextToken(), "()");
                int rpItemId = Integer.parseInt(st3.nextToken());
                int quantity = Integer.parseInt(st3.nextToken());
                L2RecipeComponent rp = new L2RecipeComponent(rpItemId, quantity);
                recipePartList.add(rp);
            }
            
            int itemId = Integer.parseInt(st.nextToken());
            int count = Integer.parseInt(st.nextToken());
            
            //npc fee
            /*String notdoneyet = */st.nextToken();
            
            int mpCost = Integer.parseInt(st.nextToken());
            int successRate = Integer.parseInt(st.nextToken());
            
            L2Recipe recipeList = new L2Recipe(id, level, recipeId, recipeName, successRate,
                                                       mpCost, itemId, count, isDwarvenRecipe);
            for (L2RecipeComponent recipePart : recipePartList)
            {
                recipeList.addRecipe(recipePart);
            }
            _lists.put(new Integer(_lists.size()), recipeList);
        }
        catch (Exception e)
        {
            _log.error("Exception in RecipeController.parseList() - " + e);
        }
    }
    
    /**
     * @return an array of all recipe ids
     * @see net.sf.l2j.gameserver.recipes.dao.IL2RecipeDAO#getRecipeIds()
     */
    public int[] getRecipeIds()
    {
        int[] recipeIds = new int[_lists.size()]; 
        int i=0;
        for(Map.Entry<Integer,L2Recipe> e: _lists.entrySet()) 
        {
            recipeIds[i] = e.getValue().getRecipeId();
            i++;
        }
        return recipeIds;
    }

}
