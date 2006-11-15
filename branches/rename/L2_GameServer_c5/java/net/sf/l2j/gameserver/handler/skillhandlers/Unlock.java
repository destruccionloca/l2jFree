package net.sf.l2j.gameserver.handler.skillhandlers;

//import net.sf.l2j.gameserver.SkillTable;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2ChestInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;

public class Unlock implements ISkillHandler
{
    //private static Logger _log = Logger.getLogger(Unlock.class.getName()); 
    protected SkillType[] _skillIds = {SkillType.UNLOCK};

    public void useSkill(L2Character activeChar, L2Skill skill, @SuppressWarnings("unused") L2Object[] targets)
    {
    	 	L2Object[] targetList = skill.getTargetList(activeChar);
    
    		for (int index = 0; index < targetList.length; index++) 
    		{
                L2Object target = targetList[index];
                boolean success = Formulas.getInstance().calculateUnlockChance(skill);
    			
                if(target instanceof L2DoorInstance)
                {
                    L2DoorInstance  doortarget  = (L2DoorInstance) targetList[index];
                    
                    if (!doortarget.isUnlockable())
                    {
                         SystemMessage systemmessage = new SystemMessage(SystemMessage.S1_S2);
                         systemmessage.addString("You cannot unlock this door!");
                         activeChar.sendPacket(systemmessage);  
                         return;
                    }
                    
                    if (success && (doortarget.getOpen() == 1))
                    {
                        doortarget.openMe();
                        doortarget.onOpen();
                    }
                    else
                    {
                         SystemMessage systemmessage = new SystemMessage(SystemMessage.S1_S2);
                         systemmessage.addString("UnLock failed!");
                         activeChar.sendPacket(systemmessage);              
                    }
                }
                else if(target instanceof L2ChestInstance)
                {
                    L2ChestInstance chest = (L2ChestInstance)targetList[index];
                    if(success)
                    {
                        SystemMessage sm = new SystemMessage(614);
                        sm.addString("Unlocked!");
                        activeChar.sendPacket(sm); 
                        chest.dropReward(activeChar);
                        chest.doDie(activeChar);
                    }else
                    {
                        SystemMessage sm = new SystemMessage(614);
                        sm.addString("Unlock failed!");
                        activeChar.sendPacket(sm);
                        //L2Skill bigboom = SkillTable.getInstance().getInfo(4139, 1); //bigboom
                        //chest.callSkill(bigboom,activeChar);
                    }
                }
    		}
    }
    
    public SkillType[] getSkillIds() 
    { 
         return _skillIds; 
    }
}
