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
package net.sf.l2j.gameserver.skills;

import java.io.File;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.items.model.Item;
import net.sf.l2j.gameserver.templates.L2Armor;
import net.sf.l2j.gameserver.templates.L2ArmorType;
import net.sf.l2j.gameserver.templates.L2EtcItem;
import net.sf.l2j.gameserver.templates.L2EtcItemType;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.gameserver.templates.StatsSet;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author mkizub
 */
final class DocumentItem extends DocumentBase
{
    private Item _currentItem = null;
    private FastList<L2Item> _itemsInFile = new FastList<L2Item>();
    private FastMap<Integer, Item> _itemData = new FastMap<Integer, Item>();

    /**
     * @param armorData
     * @param f
     */
    public DocumentItem(FastMap<Integer, Item> pItemData, File file)
    {
        super(file);
        _itemData = pItemData;
    }

    /**
     * @param item
     */
    private void setCurrentItem(Item item)
    {
        _currentItem = item;
    }

    @Override
    protected StatsSet getStatsSet()
    {
        return _currentItem.set;
    }

    @Override
    protected String getTableValue(String name)
    {
        return _tables.get(name)[_currentItem.currentLevel];
    }

    @Override
    protected String getTableValue(String name, int idx)
    {
        return _tables.get(name)[idx - 1];
    }

    @Override
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
                        setCurrentItem(new Item());
                        parseItem(d);
                        if (_currentItem.item!=null)
                        	_itemsInFile.add(_currentItem.item);
                        resetTable();
                    }
                }
            }
            else if ("item".equalsIgnoreCase(n.getNodeName()))
            {
                setCurrentItem(new Item());
                parseItem(n);
                if (_currentItem.item!=null)
                	_itemsInFile.add(_currentItem.item);
            }
        }
    }

    protected void parseItem(Node n)
    {
        int itemId = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
        String itemName = n.getAttributes().getNamedItem("name").getNodeValue();

        if (!_itemData.containsKey(itemId)) 
        {
        	_log.fatal("Stats for item id "+itemId+" ignored !");
        	
        	return;
        }
        _currentItem.id = itemId;
        _currentItem.name = itemName;
        _currentItem.set = _itemData.get(_currentItem.id).set;
        _currentItem.type = _itemData.get(_currentItem.id).type;

        Node first = n.getFirstChild();
        for (n = first; n != null; n = n.getNextSibling())
        {
            if ("table".equalsIgnoreCase(n.getNodeName())) parseTable(n);
        }
        for (n = first; n != null; n = n.getNextSibling())
        {
            if ("set".equalsIgnoreCase(n.getNodeName()))
                parseBeanSet(n, _itemData.get(_currentItem.id).set, 1);
        }
        for (n = first; n != null; n = n.getNextSibling())
        {
            if ("for".equalsIgnoreCase(n.getNodeName()))
            {
                makeItem();
                parseTemplate(n, _currentItem.item);
            }
        }
    }

    private void makeItem()
    {
        if (_currentItem.item != null) return;
        if (_currentItem.type instanceof L2ArmorType) _currentItem.item = new L2Armor((L2ArmorType) _currentItem.type,_currentItem.set);
        else if (_currentItem.type instanceof L2WeaponType) _currentItem.item = new L2Weapon((L2WeaponType) _currentItem.type,_currentItem.set);
        else if (_currentItem.type instanceof L2EtcItemType) _currentItem.item = new L2EtcItem((L2EtcItemType) _currentItem.type,_currentItem.set);
        else throw new Error("Unknown item type " + _currentItem.type);
    }

    /**
     * @return
     */
    public FastList<L2Item> getItemList()
    {
        return _itemsInFile;
    }
}
