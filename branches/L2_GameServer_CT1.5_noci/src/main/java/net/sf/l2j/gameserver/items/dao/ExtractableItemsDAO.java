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
package net.sf.l2j.gameserver.items.dao;

import net.sf.l2j.gameserver.items.model.L2ExtractableItem;

/**
 * Interface for manipulation of Extractable Items
 * 
 * An extractable item is ....
 */
public interface ExtractableItemsDAO
{

    /**
     * Retrieve the extractable item by item id
     * @param itemID
     * @return a L2ExtractableItem or null if not found
     */
    public abstract L2ExtractableItem getExtractableItem(int itemID);
    
    /**
     * Return all extractable item Ids 
     * @return array of items ids
     */
    public abstract int[] itemIDs();

}