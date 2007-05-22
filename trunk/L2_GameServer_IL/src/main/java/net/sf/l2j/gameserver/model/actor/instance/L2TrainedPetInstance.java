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
package net.sf.l2j.gameserver.model.actor.instance;

import java.util.concurrent.Future;

import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.lib.Rnd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class ...
 * author wtfx
 * Weee's goes to Sami.
 */

public class L2TrainedPetInstance extends L2Summon
{
	final static Log _log = LogFactory.getLog(L2TrainedPetInstance.class.getName());

    private float _expPenalty = 0; // exp decrease multiplier (i.e. 0.3 (= 30%) for shadow)
    private int _itemConsumeId;
    private int _itemConsumeCount;
    private int _itemConsumeSteps;
    private int _itemConsumeStepsElapsed;
    public int _foodId;
    
    private Future _summonLifeTask;

    private static final int SUMMON_LIFETIME_INTERVAL = 1200000; // 20 minutes

    private Future _feedTask;
    private Future _healTask;
    private Future _rechargeTask;
    private int _feedTime;
    private int _hpmpTime;
    protected boolean _feedMode;
    
    private int _curFed;
    
    public int getMaxFed() { return SUMMON_LIFETIME_INTERVAL; }
    public int getCurrentFed() { return _curFed; }
    public void setCurrentFed(int num) { _curFed = num > getMaxFed() ? getMaxFed() : num; }
    
    public L2TrainedPetInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, L2Skill skill)
    {
        super(objectId, template, owner);
        setShowSummonAnimation(true);
        _hpmpTime = 30000;
        /*_itemConsumeId = 0;
        _itemConsumeCount = 0;
        _itemConsumeSteps = 0;
        _itemConsumeStepsElapsed = 0;*/

        //Life Task Not Needed
        /*if (skill != null)
        {
            _itemConsumeId = skill.getItemConsumeIdOT();
            _itemConsumeCount = skill.getItemConsumeOT();
            if (skill.getItemConsumeTime() > 0)
                _itemConsumeSteps = (SUMMON_LIFETIME_INTERVAL / skill.getItemConsumeTime()) - 1;
        }

        // When no item consume is defined task only need to check when summon life time has ended.
        // Otherwise have to destroy items from owner's inventory in order to let summon live.
        int delay = SUMMON_LIFETIME_INTERVAL / (_itemConsumeSteps  +1);

        if (Config.DEBUG && (_itemConsumeCount != 0))
            _log.warning("L2SummonInstance: Item Consume ID: "+  _itemConsumeId + ", Count: "
                + _itemConsumeCount + ", Rate: "+  _itemConsumeSteps + " times.");
        if (Config.DEBUG) _log.warning("L2SummonInstance: Task Delay "  +(delay / 1000)+  " seconds.");

        _summonLifeTask = ThreadPoolManager.getInstance()
        	.scheduleGeneralAtFixedRate(new SummonLifetime(getOwner(), this), delay, delay);*/
    }

    public final int getLevel()
    {
        return (getTemplate() != null ? getTemplate().level : 0);
    }

    public int getSummonType()
    {
        return 1;
    }

    public void setExpPenalty(float expPenalty)
    {
        _expPenalty = expPenalty;
    }

    public float getExpPenalty()
    {
        return _expPenalty;
    }

    public int getItemConsumeCount()
    {
        return _itemConsumeCount;
    }

    public int getItemConsumeId()
    {
        return _itemConsumeId;
    }

    public int getItemConsumeSteps()
    {
        return _itemConsumeSteps;
    }

    public void incItemConsumeStepsElapsed()
    {
        _itemConsumeStepsElapsed++;
    }

    public int getItemConsumeStepsElapsed()
    {
        return _itemConsumeStepsElapsed;
    }

    public int getFoodId()
    {
    	return _foodId;
    }
    
    public void setFoodId(int foodid)
    {
    	_foodId=foodid;
    }
    
    public void addExpAndSp(int addToExp, int addToSp)
    {
        getOwner().addExpAndSp(addToExp, addToSp);
    }

    public void reduceCurrentHp(int damage, L2Character attacker)
    {
       System.out.println("Reduciendo Hp");
    	
    	super.reduceCurrentHp(damage, attacker);
        getStatus().reduceHp(damage, attacker, true);
        
        /*SystemMessage sm = new SystemMessage(SystemMessage.SUMMON_RECEIVED_DAMAGE_OF_S2_BY_S1);
        if (attacker instanceof L2NpcInstance)
        {
            sm.addNpcName(((L2NpcInstance) attacker).getTemplate().npcId);
        }
        else
        {
            sm.addString(attacker.getName());
        }
        sm.addNumber(damage);
        getOwner().sendPacket(sm);*/
    }

    
    
    public synchronized void doDie(L2Character killer)
    {
        if (_log.isDebugEnabled())
            _log.warn("L2SummonInstance: " + getTemplate().name+  " (" + getOwner().getName()
                + ") has been killed.");

        /*if (_summonLifeTask != null)
        {
            _summonLifeTask.cancel(true);
            _summonLifeTask = null;
        }*/

        this.stopFeed();
        this.stopHeal();
        this.stopRecharge();
        super.doDie(killer);
    }

    public void onAction(L2PcInstance player)
    {
    	if (player == getOwner() && player.getTarget() == this)
        {
            //player.sendPacket(new PetStatusShow(this));
            player.sendPacket(new ActionFailed());
       }
        else
        {
            if (_log.isDebugEnabled()) _log.info("new target selected:"+getObjectId());
            player.setTarget(this);
            MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
            player.sendPacket(my);
        }
    }

   /* static class SummonLifetime implements Runnable
    {
        private L2PcInstance _activeChar;
        private L2TrainedPetInstance _summon;

        SummonLifetime(L2PcInstance activeChar, L2TrainedPetInstance newpet)
        {
            _activeChar = activeChar;
            _summon = newpet;
        }

        public void run()
        {
            if (Config.DEBUG)
                log.warning("L2SummonInstance: " + _summon.getTemplate().name  +" ("
                   +  _activeChar.getName()+  ") run task.");

            // check if life time of summon is ended
            if (_summon.getItemConsumeStepsElapsed() >= _summon.getItemConsumeSteps())
            {
                _summon.unSummon(_activeChar);
            }
            // check if owner has enought itemConsume, if requested
            else if (_summon.getItemConsumeCount() > 0
                && _summon.getItemConsumeId() != 0
                && !_summon.isDead()
                && !_summon.destroyItemByItemId("Consume", _summon.getItemConsumeId(),
                                                _summon.getItemConsumeCount(), _activeChar, true))
            {
                _summon.unSummon(_activeChar);
            }

            _summon.incItemConsumeStepsElapsed();
        }
    }*/

    public void unSummon(L2PcInstance owner)
    {
        if (_log.isDebugEnabled())
            _log.warn("L2SummonInstance: " + getTemplate().name + " (" + owner.getName()
               +  ") unsummoned.");

        if (_summonLifeTask != null)
        {
            _summonLifeTask.cancel(true);
            _summonLifeTask = null;
        }
        
        this.stopFeed();
        this.stopHeal();
        this.stopRecharge();
        super.unSummon(owner);
    }

    public boolean destroyItem(String process, int objectId, int count, L2Object reference,
                               boolean sendMessage)
    {
        return getOwner().destroyItem(process, objectId, count, reference, sendMessage);
    }

    public boolean destroyItemByItemId(String process, int itemId, int count, L2Object reference,
                                       boolean sendMessage)
    {
        if (_log.isDebugEnabled())
            _log.warn("L2SummonInstance: "+  getTemplate().name + " (" + getOwner().getName()
               +  ") consume.");

        return getOwner().destroyItemByItemId(process, itemId, count, reference, sendMessage);
    }
    
    public synchronized void stopHeal()
	{
		if (_healTask != null)
		{
			_healTask.cancel(false);
			_healTask = null;
			//if (Config.DEBUG) _logPet.fine("Pet [#"getObjectId()"] feed task stop");
		}
	}
	
	public synchronized void startHeal()
	{
		// stop feeding task if its active
		
		stopHeal();
	    if (!isDead())
        {
	    	try{
	    	_healTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new Heal(50,this), _hpmpTime,_hpmpTime);
	    	}catch(Exception ee){
	    		System.out.println(ee);
	    	}
	    }
	}
    
	public synchronized void stopRecharge()
	{
		if (_rechargeTask != null)
		{
			_rechargeTask.cancel(false);
			_rechargeTask = null;
			//if (Config.DEBUG) _logPet.fine("Pet [#"getObjectId()"] feed task stop");
		}
	}
	
	public synchronized void startRecharge()
	{
		// stop feeding task if its active
		
		stopHeal();
	    if (!isDead())
        {
	    	try{
	    	_rechargeTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new Recharge(50,this), _hpmpTime,_hpmpTime);
	    	}catch(Exception ee){
	    		System.out.println(ee);
	    	}
	    }
	}
     
    public synchronized void stopFeed()
	{
		if (_feedTask != null)
		{
			_feedTask.cancel(false);
			_feedTask = null;
			//if (Config.DEBUG) _logPet.fine("Pet [#"getObjectId()"] feed task stop");
		}
	}
	
	public synchronized void startFeed( boolean battleFeed )
	{
		// stop feeding task if its active
		
		stopFeed();
	    if (!isDead())
        {
	    	if (battleFeed)
	        {
	    		_feedMode = true;
	    		_feedTime = 60000;
	        }
	    	else
	    	{
	    		_feedMode = false;
	    		_feedTime = 60000;
	    	}
	    	//  pet feed time must be different than 0. Changing time to bypass divide by 0
	    	_feedTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new FeedTask(), _feedTime, _feedTime);
	    }
	}
    
    
    private class FeedTask implements Runnable
	{
		public void run()
		{
			try
			{
				// if pet is attacking
				if (isAttackingNow())
				{
					// if its not already on battleFeed mode
					if (!_feedMode)
						startFeed(true); //switching to battle feed
				}
				else if (_feedMode) // if its on battleFeed mode
					startFeed(false); // normal feed
				
				int foodId = getFoodId();

				L2ItemInstance food = null;
				food = getOwner().getInventory().getItemByItemId(foodId);
                
				if (food != null)
				{
					if (destroyItem("Feed", food.getObjectId(), 1, null, false)) 
					{
						
						if (getOwner() != null)
						{
							SystemMessage sm = new SystemMessage(SystemMessage.PET_TOOK_S1_BECAUSE_HE_WAS_HUNGRY);
							sm.addItemName(foodId);
							getOwner().sendPacket(sm);
						}
					}else{
						unSummon(getOwner());
						getOwner().sendMessage("Your pet is too hungry to stay summoned.");
					}
				}else{
					unSummon(getOwner());
					getOwner().sendMessage("Your pet is too hungry to stay summoned.");
				}
                
				broadcastStatusUpdate();
			}
            catch (Throwable e) 
            {
            	//if (Config.DEBUG) 
            	//	_logPet.warning("Pet [#"getObjectId()"] a feed task error has occurred: "e);
            }
        }
    }

    private class Heal implements Runnable
    {
        private int _chance;
        L2Character _caster;

        Heal(int chance, L2Character caster)
        {
            _chance = chance;
            _caster = caster;
            // run task
        }

        public void run()
        {
        	//System.out.println("Shall i?");
        	
            if (getOwner().isDead())
            {
            	unSummon(getOwner());
                return;
            }
            
            if (Rnd.get(1, 100) < _chance)
            {
            	L2Character target;
            	target = null;
                       
            	if (getOwner().getStatus().getCurrentHp() < getOwner().getMaxHp()) 
            		target = getOwner();
                        
            	if (target != null)
            	{
                        	
            		getAI().stopFollow();
            		//setFollowStatus(false);
                        	
            		L2Skill skill = SkillTable.getInstance().getInfo(1217,18);
                        	
            		setTarget(target);
            		//getOwner().sendMessage("Ill Heal You Master!.");
            		//doCast(skill);
            		useMagic(skill,true,true);
                        
            		while(isCastingNow()) 
            		{ 
            			try 
            			{ 
            				Thread.sleep(200); 
            			}
            			catch ( InterruptedException e ) {} 
            		}
                           	
            		try
            		{
            			//setFollowStatus(true);
            			getAI().startFollow(target,100);
                           	    
            		} catch(Exception ee)
            		{
            			System.out.println(ee);
            		}
            	}
            }
        }
    }
    
    private class Recharge implements Runnable
    {
        private int _chance;
        L2Character _caster;

        Recharge(int chance, L2Character caster)
        {
            _chance = chance;
            _caster = caster;
            // run task
        }

        public void run()
        {
        	
        	if (getOwner().isDead())
            {
        		unSummon(getOwner());
        		return;
            }
        	
        	if (Rnd.get(1, 100) < _chance) 
        	{
        		L2Character target;
        		target = null;
                        
        		if (getOwner().getStatus().getCurrentMp() < getOwner().getMaxMp()) 
        			target = getOwner();
                        
        		if (target != null)
        		{
        			getAI().stopFollow();
        			//setFollowStatus(false);
                        		 
        			L2Skill skill = SkillTable.getInstance().getInfo(1013,10);
                        	
        			setTarget(target);
        			//getOwner().sendMessage("Ill Recharge You Master!.");
        			//doCast(skill);
        			useMagic(skill,true,true);
        			while(isCastingNow())
        			{ 
               			try 
            			{ 
            				Thread.sleep(200); 
            			}
            			catch ( InterruptedException e ) {} 
        			}
                                      	 
        			try
        			{
        				//setFollowStatus(true);
        				getAI().startFollow(target,100);
        			} catch(Exception ee) {
        				System.out.println(ee);
        			}
        		}
        	}
        }        
    }   
    
    public void doBuff(){
    
    	
    	/*Trained Animals buff and Heal you. Each Class is different. Mage classes will get MP regen when the Fighter Class with get HP healing.

    	Buffs are different as well.

    	Fighter Class gets
    	Haste Level 2 1086 -2
    	Bless the Body Level 5 1045-5
    	Vampyric Rage Level 3 1268-3
    	Guidance Level 3 1240-3
    	Regeneration Level 3 1044-3
    	Mage Class gets (don't know the levels)
    	Bless the Soul -1048-5
    	Acumen  1085-3
    	Wind Walk 1204-2
    	Empower 1059-3
    	Concentration 1078-5*/
    	
    	//getOwner().sendMessage("Ill Buff You Master!.");
    	int nBuff = Rnd.get(2,3);
    	  
    	int skillf [][]={{1086, 2},{1045, 5},{1268, 3},{1240, 3},{1044, 3}};
    	
    	int skillm [][]={{1048, 5},{1085, 3},{1204, 2},{1059, 3},{1078, 5}};
    	//System.out.println(nBuff);
    	L2Skill skill;
    	for(int i = 0;i<nBuff;i++)
    	{
    		//System.out.println("buff s="(i1));
    	
    		if (getOwner().isMageClass() == true)
    		{
    			int n = Rnd.get(0,4);
    			
    			while(skillm[n][0]==0)
    			{
    				n = Rnd.get(0,4);	
    			}
    			skill = SkillTable.getInstance().getInfo(skillm[n][0],skillm[n][1]);
    			skillm[n][0] = 0;	
    		}
    		else
    		{
    			int n = Rnd.get(0,4);
    			
    			while(skillf[n][0]==0)
    			{
    				n = Rnd.get(0,4);	
    			}
    			
    		    skill = SkillTable.getInstance().getInfo(skillf[n][0],skillf[n][1]);
    			skillf[n][0] = 0;	
    		}
    	   	  
    		setTarget(getOwner());
    	   	   
    		//System.out.println(skill.getName());
    		   
    		//doCast(skill);
    		useMagic(skill,true,true);
    		while(this.isCastingNow()){;}
    	}
    	
    	//System.out.println("Ill follow you Master!");
    	try
    	{
    		//setFollowStatus(true);
    		getAI().startFollow(getOwner(),100);
    	}
    	catch (Exception ee) {
    		System.out.println(ee);
    	}
    }
}
