package net.sf.l2j.gameserver.model;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 * 
 *
 * @author  Luno
 */
public final class L2ArmorSet
{
    private final int chest;
    private final int legs;
    private final int head;
    private final int gloves;
    private final int feet;
    private final int skill_id;
    
    private final int shield;
    private final int shield_skill_id;
    
    private final int enchant6skill;
    
    public L2ArmorSet(int _chest, int _legs, int _head, int _gloves, int _feet, int _skill_id, int _shield, int _shield_skill_id, int _enchant6skill)
    {
        this.chest = _chest;
        this.legs  = _legs;
        this.head  = _head;
        this.gloves = _gloves;
        this.feet  = _feet;
        this.skill_id = _skill_id;
        
        this.shield = _shield;
        this.shield_skill_id = _shield_skill_id;
        
        this.enchant6skill = _enchant6skill;
    }
    /**
     * Checks if player have equiped all items from set (not checking shield)
     * @param player whose inventory is being checked
     * @return True if player equips whole set
     */
    public boolean containAll(L2PcInstance player)
    {
        Inventory inv = player.getInventory();
        
        L2ItemInstance legsItem   = inv.getPaperdollItem(Inventory.PAPERDOLL_LEGS);
        L2ItemInstance headItem   = inv.getPaperdollItem(Inventory.PAPERDOLL_HEAD);
        L2ItemInstance glovesItem = inv.getPaperdollItem(Inventory.PAPERDOLL_GLOVES);
        L2ItemInstance feetItem   = inv.getPaperdollItem(Inventory.PAPERDOLL_FEET);
        
        int _legs = 0;
        int _head = 0;
        int _gloves = 0;
        int _feet = 0;
        
        if(legsItem != null)   _legs = legsItem.getItemId();
        if(headItem != null)   _head = headItem.getItemId();
        if(glovesItem != null) _gloves = glovesItem.getItemId();
        if(feetItem != null)   _feet = feetItem.getItemId();
        
        return containAll(this.chest,_legs,_head,_gloves,_feet);
        
    }
    public boolean containAll(int _chest, int _legs, int _head, int _gloves, int _feet)
    {
        if(this.chest != 0 && this.chest != _chest)
            return false;
        if(this.legs != 0 && this.legs != _legs)
            return false;
        if(this.head != 0 && this.head != _head)
            return false;
        if(this.gloves != 0 && this.gloves != _gloves)
            return false;
        if(this.feet != 0 && this.feet != _feet)
            return false;
    
        return true;
    }
    public boolean containItem(int slot, int itemId)
    {
        switch(slot)
        {
        case Inventory.PAPERDOLL_CHEST:
            return chest == itemId;
        case Inventory.PAPERDOLL_LEGS:
            return legs == itemId;
        case Inventory.PAPERDOLL_HEAD:
            return head == itemId;
        case Inventory.PAPERDOLL_GLOVES:
            return gloves == itemId;
        case Inventory.PAPERDOLL_FEET:
            return feet == itemId;
        default:
            return false;
        }
    }
    public int getSkillId()
    {
        return skill_id;
    }
    public boolean containShield(L2PcInstance player)
    {
        Inventory inv = player.getInventory();
        
        L2ItemInstance shieldItem   = inv.getPaperdollItem(Inventory.PAPERDOLL_LHAND);
        if(shieldItem!= null && shieldItem.getItemId() == shield)
            return true;
    
        return false;
    }
    public boolean containShield(int shield_id)
    {
        if(shield == 0)
            return false;
        
        return shield == shield_id;
    }
    public int getShieldSkillId()
    {
        return shield_skill_id;
    }
    public int getEnchant6skillId()
    {
        return enchant6skill;
    }
    /**
     * Checks if all parts of set are enchanted to +6 or more
     * @param player
     * @return 
     */
    public boolean isEnchanted6(L2PcInstance player)
    {
         // Player don't have full set
        if(!containAll(player))
            return false;
        
        Inventory inv = player.getInventory();
        
        L2ItemInstance chestItem  = inv.getPaperdollItem(Inventory.PAPERDOLL_CHEST);
        L2ItemInstance legsItem   = inv.getPaperdollItem(Inventory.PAPERDOLL_LEGS);
        L2ItemInstance headItem   = inv.getPaperdollItem(Inventory.PAPERDOLL_HEAD);
        L2ItemInstance glovesItem = inv.getPaperdollItem(Inventory.PAPERDOLL_GLOVES);
        L2ItemInstance feetItem   = inv.getPaperdollItem(Inventory.PAPERDOLL_FEET);
        
        if(chestItem.getEnchantLevel() < 6)
            return false;
        if(this.legs != 0 && legsItem.getEnchantLevel() < 6)
            return false;
        if(this.gloves != 0 && glovesItem.getEnchantLevel() < 6)
            return false;
        if(this.head != 0 && headItem.getEnchantLevel() < 6)
            return false;
        if(this.feet != 0 && feetItem.getEnchantLevel() < 6)
            return false;
        
        return true;
    }
}