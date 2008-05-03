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

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * This class describes a Recipe component (1 line of the recipe : Item-Quantity needed).<BR><BR>
 */
public class L2RecipeComponent
{
	/** The Identifier of the item  */
    private int _itemId;
	
	/** The item quantity needed  */
    private int _quantity;
    
	
	/**
	 * Constructor of L2RecipeComponent 
	 */
    public L2RecipeComponent(int itemId, int quantity)
    {
        _itemId = itemId;
        _quantity = quantity;
    }
    
	/**
	 * Return the Identifier of the RecipComponent
	 */
    public int getItemId()
    {
        return _itemId;
    }
    
	/**
	 * Return the Item quantity needed 
	 */
    public int getQuantity()
    {
        return _quantity;
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17,37).append(_itemId).toHashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final L2RecipeComponent other = (L2RecipeComponent) obj;
        if (_itemId != other._itemId)
            return false;
        return true;
    }
	
}
