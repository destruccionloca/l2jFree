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
package net.sf.l2j.gameserver.items.service;

import net.sf.l2j.gameserver.items.dao.ExtractableItemsDAO;
import net.sf.l2j.gameserver.items.model.L2ExtractableItem;


public class ExtractableItemsService
{
    private ExtractableItemsDAO __extractableItemsDAO =null;
    
    public void setExtractableItemsDAO (ExtractableItemsDAO dao)
    {
        __extractableItemsDAO = dao;
    }

    /**
     * @param itemID
     * @return
     * @see net.sf.l2j.gameserver.items.dao.ExtractableItemsDAO#getExtractableItem(int)
     */
    public L2ExtractableItem getExtractableItem(int itemID)
    {
        return __extractableItemsDAO.getExtractableItem(itemID);
    }

    /**
     * @return
     * @see net.sf.l2j.gameserver.items.dao.ExtractableItemsDAO#itemIDs()
     */
    public int[] itemIDs()
    {
        return __extractableItemsDAO.itemIDs();
    }

}
