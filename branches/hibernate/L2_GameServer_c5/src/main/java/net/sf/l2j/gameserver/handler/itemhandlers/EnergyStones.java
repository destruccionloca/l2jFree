
package net.sf.l2j.gameserver.handler.itemhandlers;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Potion;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.EffectCharge;


public class EnergyStones implements IItemHandler
{
    private static int[] _itemIds = { 5589};
    int num_charges;   
    
    public void useItem(L2PlayableInstance playable, L2ItemInstance item)
    {
        if (!(playable instanceof L2PcInstance))
            return;
        L2PcInstance activeChar = (L2PcInstance)playable;
        
        L2Potion potion = new L2Potion(IdFactory.getInstance().getNextId());
        int itemId = item.getItemId();
          
        if (itemId == 5589)
        {
            if ((activeChar.getClassId().getId() == 2) || (activeChar.getClassId().getId() == 48) || (activeChar.getClassId().getId() == 88))
            {
                num_charges = (activeChar.getClassId().getId() == 2) ? activeChar.getSkillLevel(8) : activeChar.getSkillLevel(50);
                EffectCharge effect = (activeChar.getClassId().getId() == 2) ? (EffectCharge)activeChar.getEffect(8) : (EffectCharge)activeChar.getEffect(50);
                
                if (effect != null) {
                    if (effect.getLevel()  < num_charges)
                    {
                        MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2165, 1, 1, 0);
                        activeChar.sendPacket(MSU);
                        activeChar.broadcastPacket(MSU);
                        effect.num_charges++;
                        activeChar.updateEffectIcons();
                        potion.setCurrentMpPotion1(playable, itemId);
                        playable.destroyItem("Consume",item.getObjectId(),1,null,false);
                        SystemMessage sm = new SystemMessage(614);
                        sm.addString("Sonic charged.");
                        activeChar.sendPacket(sm);
                    }else{
                        SystemMessage sm2 = new SystemMessage(614);
                        sm2.addString("Maximum amount of Sonics charged.");
                        activeChar.sendPacket(sm2);   
                    }
                }
                
                /* TODO:
                 * else-case 
                 * First use of focusing sonic with stone 
                 * throughs Null-Pointer-Exception, because 
                 * EffectCharge object isn't created with stone. 
                 */
                
            }else{
                SystemMessage sm = new SystemMessage(614);
                sm.addString("Only Gladiators,Tyrants or Duelists can use Energy Stone.");
                activeChar.sendPacket(sm);                
            }
        }        
    }
    
    public int[] getItemIds()
    {
        return _itemIds;
    }
}