package net.sf.l2j.gameserver.items.model;

import net.sf.l2j.gameserver.model.L2ItemInstance;

/**
 * 
 * FIXME : to rename and factorize with other item model entities
 * 
 * Class explanation:
 * For item counting or checking purposes. When you don't want to modify inventory 
 * class contains itemId, quantity, ownerId, referencePrice, but not objectId 
 */
public class TempItem
{ // no object id stored, this will be only "list" of items with it's owner
    private int _itemId;
    private int _quantity;
    private int _ownerId;
    private int _referencePrice;
    private String _itemName;
    
    /**
     * @param item
     * @param quantity of that item
     */
    public TempItem(L2ItemInstance item, int quantity)
    {
        super();
        _itemId = item.getItemId();
        _quantity = quantity;
        _ownerId = item.getOwnerId();
        _itemName = item.getItem().getName();
        _referencePrice = item.getReferencePrice();
    }
    
    /**
     * @return Returns the quantity.
     */
    public int getQuantity()
    {
        return _quantity;
    }
    
    /**
     * @param quantity The quantity to set.
     */
    public void setQuantity(int quantity)
    {
        _quantity = quantity;
    }
    
    public int getReferencePrice()
    {
        return _referencePrice;
    }
    
    /**
     * @return Returns the itemId.
     */
    public int getItemId()
    {
        return _itemId;
    }
    
    /**
     * @return Returns the ownerId.
     */
    public int getOwnerId()
    {
        return _ownerId;
    }
    
    /**
     * @return Returns the itemName.
     */
    public String getItemName()
    {
        return _itemName;
    }
}
