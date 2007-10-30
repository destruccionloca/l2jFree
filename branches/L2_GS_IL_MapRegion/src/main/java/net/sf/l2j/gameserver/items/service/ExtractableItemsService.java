/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
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
