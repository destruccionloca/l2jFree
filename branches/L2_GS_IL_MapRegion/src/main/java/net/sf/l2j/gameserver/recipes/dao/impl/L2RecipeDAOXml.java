/**
 * 
 */
package net.sf.l2j.gameserver.recipes.dao.impl;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.recipes.dao.IL2RecipeDAO;
import net.sf.l2j.gameserver.recipes.model.L2Recipe;
import net.sf.l2j.gameserver.recipes.model.L2RecipeComponent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Xml implementation for L2RecipeDAO This implementation read all recipes from
 * a xml file
 * 
 * @author G1ta0
 * 
 */
public class L2RecipeDAOXml implements IL2RecipeDAO
{
	/** Logger */
	private final static Log _log = LogFactory.getLog(L2RecipeDAOXml.class.getName());

	/** private map to store all recipes */
	private Map<Integer, L2Recipe> _lists;

	/**
	 * Constructor The constructor load the file and parse it
	 */
	public L2RecipeDAOXml()
	{
		_lists = new FastMap<Integer, L2Recipe>();

		Document doc = null;

		File recipesXml = new File(Config.DATAPACK_ROOT, "data/recipes.xml");

		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			doc = factory.newDocumentBuilder().parse(recipesXml);
		} catch (Exception e)
		{
			_log.error("Recipes: Error loading "+recipesXml.getAbsolutePath()+" !");
		}
		try
		{
			parseDocument(doc);
		} catch (Exception e)
		{
			_log.error("Recipes: Error while reading "+recipesXml.getAbsolutePath()+" !");
		}

		_log.info("Recipes: Loaded " + _lists.size() + " recipes.");
	}

	protected void parseDocument(Document doc)
	{
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("item".equalsIgnoreCase(d.getNodeName()))
					{
						L2Recipe recipe = parseEntry(d);
						if ( recipe != null )
						_lists.put(new Integer(_lists.size()), recipe);
					}
				}
			} else if ("item".equalsIgnoreCase(n.getNodeName()))
			{
				L2Recipe recipe = parseEntry(n);
				if ( recipe != null )
				_lists.put(new Integer(_lists.size()), recipe);
			}
		}
	}

	protected L2Recipe parseEntry(Node n)
	{
		int id = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
		String name = n.getAttributes().getNamedItem("name").getNodeValue();
		int recipeId = 0;
		int level = 0;
		int mpCost = 0;
		int successRate = 100;
		int productId = 0;
		int productQuantity = 0;
		boolean isDwarvenRecipe = true;
		List<L2RecipeComponent> recipePartList = new FastList<L2RecipeComponent>();

		Node first = n.getFirstChild();
		for (n = first; n != null; n = n.getNextSibling())
		{
			if ("ingredient".equalsIgnoreCase(n.getNodeName()))
			{
				int ingredientId = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
				int ingredientCount = Integer.parseInt(n.getAttributes().getNamedItem("count").getNodeValue());
				L2RecipeComponent rp = new L2RecipeComponent(ingredientId, ingredientCount);
				recipePartList.add(rp);
			} else if ("production".equalsIgnoreCase(n.getNodeName()))
			{
				productId = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
				productQuantity = Integer.parseInt(n.getAttributes().getNamedItem("count").getNodeValue());
			} else if ("recipe".equalsIgnoreCase(n.getNodeName()))
			{
				recipeId = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
				level = Integer.parseInt(n.getAttributes().getNamedItem("level").getNodeValue());
			} else if ("dwarven".equalsIgnoreCase(n.getNodeName()))
			{
				isDwarvenRecipe = true;
			} else if ("common".equalsIgnoreCase(n.getNodeName()))
			{
				isDwarvenRecipe = false;
			} else if ("mpCost".equalsIgnoreCase(n.getNodeName()))
			{
				mpCost = Integer.parseInt(n.getTextContent());
			} else if ("successRate".equalsIgnoreCase(n.getNodeName()))
			{
				successRate = Integer.parseInt(n.getTextContent());
			}
		}

		if ((productId > 0) && (productQuantity > 0) && (recipePartList.size() > 0))
		{
			L2Recipe recipe = new L2Recipe(id, level, recipeId, name, successRate, mpCost, productId, productQuantity,
					isDwarvenRecipe);

			for (L2RecipeComponent recipePart : recipePartList)
				recipe.addRecipe(recipePart);

			return recipe;
		} else
			return null;
	}

	/**
	 * Return a L2Recipe by its place in the list.
	 * 
	 * @see net.sf.l2j.gameserver.recipes.dao.IL2RecipeDAO#getRecipe(int)
	 * @return a L2Recipe
	 * @param listId
	 *            or null if it doesn't exist
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
	 * 
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
	 * 
	 * @return recipe list size
	 * @see net.sf.l2j.gameserver.recipes.dao.IL2RecipeDAO#getRecipesCount()
	 */
	public int getRecipesCount()
	{
		return _lists.size();
	}

	/**
	 * @return an array of all recipe ids
	 * @see net.sf.l2j.gameserver.recipes.dao.IL2RecipeDAO#getRecipeIds()
	 */
	public int[] getRecipeIds()
	{
		int[] recipeIds = new int[_lists.size()];
		int i = 0;
		for (Map.Entry<Integer, L2Recipe> e : _lists.entrySet())
		{
			recipeIds[i] = e.getValue().getRecipeId();
			i++;
		}
		return recipeIds;
	}
}
