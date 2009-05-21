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
package net.sf.l2j.gameserver.model.actor.instance;

import java.util.concurrent.Future;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.PetLiveTime;
import net.sf.l2j.gameserver.network.serverpackets.SetSummonRemainTime;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class L2SummonInstance extends L2Summon
{
    final static Log _log = LogFactory.getLog(L2SummonInstance.class.getName());
    
    private float _expPenalty = 0; // exp decrease multiplier (i.e. 0.3 (= 30%) for shadow)
	private int _itemConsumeId;
	private int _itemConsumeCount;
	private int _itemConsumeSteps;
    private int _totalLifeTime;
    private int _timeLostIdle;
    private int _timeLostActive;
    private int _timeRemaining;
    private int _nextItemConsumeTime;
    public int lastShowntimeRemaining;  // Following FbiAgent's example to avoid sending useless packets

    private static final int SUMMON_LIFETIME_INTERVAL = 1200000; // 20 minutes
    
    private static Future<?> _summonLifeTask;
    private static Future<?> _summonConsumeTask;

    private static final int SUMMON_LIFETIME_REFRESH_INTERVAL = 30000; // 30 seconds
    
    private static int _lifeTime = SUMMON_LIFETIME_INTERVAL; // summon life time for life scale bar
    
	public L2SummonInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, L2Skill skill)
    {
		super(objectId, template, owner);
        setShowSummonAnimation(true);

        if (owner.getPet()!= null && owner.getPet().getTemplate().getNpcId() == template.getNpcId() )
            return;

    	// defaults
        _itemConsumeId = 0;
        _itemConsumeCount = 0;
        _itemConsumeSteps = 0;
        _totalLifeTime = 1200000; // 20 minutes
        _timeLostIdle = 1000;
        _timeLostActive = 1000;
        
        
    	if (skill != null) {
        	_itemConsumeId = skill.getItemConsumeIdOT();
        	_itemConsumeCount = skill.getItemConsumeOT();
            _itemConsumeSteps = skill.getItemConsumeSteps();
            _totalLifeTime = skill.getTotalLifeTime();
            _timeLostIdle = skill.getTimeLostIdle();
            _timeLostActive = skill.getTimeLostActive();
        }
    	
        _timeRemaining = _totalLifeTime;
        lastShowntimeRemaining = _totalLifeTime;
        
        if (_itemConsumeId == 0)
        	_nextItemConsumeTime = -1;	// do not consume
        else if (_itemConsumeSteps == 0)
        	_nextItemConsumeTime = -1;	// do not consume
        else
        	_nextItemConsumeTime = _totalLifeTime - _totalLifeTime/(_itemConsumeSteps+1);

    	// When no item consume is defined task only need to check when summon life time has ended.
    	// Otherwise have to destroy items from owner's inventory in order to let summon live.
        int delay = 1000;

       	if (_log.isDebugEnabled() && (_itemConsumeCount != 0))
       		_log.debug("L2SummonInstance: Item Consume ID: " + _itemConsumeId + ", Count: " + _itemConsumeCount + ", Rate: " + _itemConsumeSteps + " times.");
       	if (_log.isDebugEnabled())
       		_log.debug("L2SummonInstance: Task Delay " + (delay / 1000) + " seconds.");
       	
        _summonConsumeTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new SummonConsume(getOwner(), this), delay, delay);
        _summonLifeTask = ThreadPoolManager.getInstance().scheduleGeneral(new SummonLifetime(getOwner(), this), SUMMON_LIFETIME_REFRESH_INTERVAL);
    }
    
	@Override
	public final int getLevel()
	{
		return (getTemplate() != null ? getTemplate().getLevel() : 0);
	}
	
    @Override
    public int getSummonType()
	{
		return 1;
	}
	
	public void setExpPenalty(float expPenalty)
	{
        float ratePenalty = Config.ALT_GAME_SUMMON_PENALTY_RATE;
        _expPenalty = (expPenalty* ratePenalty);
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

    public int getNextItemConsumeTime()
    {
        return _nextItemConsumeTime;
    }    

    public int getTotalLifeTime()
    {
        return _totalLifeTime;
    }

    public int getTimeLostIdle()
    {
        return _timeLostIdle;
    }

    public int getTimeLostActive()
    {
        return _timeLostActive;
    }
    
    public int getTimeRemaining()
    {
        return _timeRemaining;
    }
    
    public void setNextItemConsumeTime(int value)
    {
        _nextItemConsumeTime = value;
    }    

    public void decNextItemConsumeTime(int value)
    {
        _nextItemConsumeTime -= value;
    }    

    public void decTimeRemaining(int value)
    {
    	_timeRemaining -= value;
    }

    public void addExpAndSp(int addToExp, int addToSp)
    {
        getOwner().addExpAndSp(addToExp, addToSp);
    }
    
    public void reduceCurrentHp(int damage, L2Character attacker)
    {
        super.reduceCurrentHp(damage, attacker);
        SystemMessage sm = new SystemMessage(SystemMessageId.SUMMON_RECEIVED_DAMAGE_S2_BY_S1);
        if (attacker instanceof L2NpcInstance)
        {
            sm.addNpcName(((L2NpcInstance)attacker).getTemplate().getNpcId());
        }
        else
        {
            sm.addString(attacker.getName());
        }
        sm.addNumber(damage);
        getOwner().sendPacket(sm);
    }
    
    @Override
    public boolean doDie(L2Character killer)
    {
        if (!super.doDie(killer))
            return false;

        if (_log.isDebugEnabled())
            _log.warn("L2SummonInstance: " + getTemplate().getName() + " (" + getOwner().getName() + ") has been killed.");

        if (_summonLifeTask != null)
        {
           _summonLifeTask.cancel(true);
           _summonLifeTask = null;
        }

        if (_summonConsumeTask != null)
        {
            _summonConsumeTask.cancel(true);
            _summonConsumeTask = null;
         }

        return true;
   }

    public void displayHitMessage(int damage, boolean crit, boolean miss) 
    {
        if (crit)
        {
            getOwner().sendPacket(new SystemMessage(SystemMessageId.CRITICAL_HIT_BY_SUMMONED_MOB));
        }
        
        if (miss)
        {
            getOwner().sendPacket(new SystemMessage(SystemMessageId.MISSED_TARGET));
        }
        else
        {
            SystemMessage sm = new SystemMessage(SystemMessageId.SUMMON_GAVE_DAMAGE_S1);
            sm.addNumber(damage);
            getOwner().sendPacket(sm);
        }
    }

    static class SummonConsume implements Runnable
    {
        private L2PcInstance _activeChar;
        private L2SummonInstance _summon;
        
        SummonConsume(L2PcInstance activeChar, L2SummonInstance newpet)
        {
            _activeChar = activeChar;
            _summon = newpet;
        }
        
        public void run()
        {
           	if (_log.isDebugEnabled())
    			_log.warn("L2SummonInstance: " + _summon.getTemplate().getName() + " (" + _activeChar.getName() + ") run task.");

			try
			{
				double oldTimeRemaining = _summon.getTimeRemaining();
				int maxTime = _summon.getTotalLifeTime();
				double newTimeRemaining;
				
				// if pet is attacking
				if (_summon.isAttackingNow())
				{
				    _summon.decTimeRemaining(_summon.getTimeLostActive());
				}
				else
				{
					_summon.decTimeRemaining(_summon.getTimeLostIdle());
				}
				newTimeRemaining = _summon.getTimeRemaining();
				// check if the summon's lifetime has ran out
				if (newTimeRemaining < 0 )
				{
	                _summon.unSummon(_activeChar);
				}
				// check if it is time to consume another item
				else if ( (newTimeRemaining <= _summon.getNextItemConsumeTime()) && (oldTimeRemaining > _summon.getNextItemConsumeTime()) )
				{
					_summon.decNextItemConsumeTime(maxTime/(_summon.getItemConsumeSteps()+1));
					
		            // check if owner has enought itemConsume, if requested
		            if (_summon.getItemConsumeCount() > 0
		                && _summon.getItemConsumeId() != 0
		                && !_summon.isDead()
		                && !_summon.destroyItemByItemId("Consume", _summon.getItemConsumeId(),
		                                                _summon.getItemConsumeCount(), _activeChar, true))
		            {
		                _summon.unSummon(_activeChar);
		            }
				}
				
				// prevent useless packet-sending when the difference isn't visible.
				if ((_summon.lastShowntimeRemaining - newTimeRemaining) > maxTime/352)
				{
					_summon.getOwner().sendPacket(new SetSummonRemainTime(maxTime,(int) newTimeRemaining));
					_summon.lastShowntimeRemaining = (int) newTimeRemaining;
				}
			}
            catch (Throwable e)  
    		{
            	if (_log.isDebugEnabled()) 
            		_log.warn("Summon of player [#"+_activeChar.getName()+"] has encountered item consumption errors: "+e);

            }
        }
    }

    static class SummonLifetime implements Runnable
    {
        private L2PcInstance _activeChar;
        private L2SummonInstance _summon;
        
        SummonLifetime(L2PcInstance activeChar, L2SummonInstance newpet)
        {
            _activeChar = activeChar;
            _summon = newpet;
        }
        
        public void run()
        {
        	
        	 if (!_summon.isDead())
        	 {
        		 _lifeTime -= SUMMON_LIFETIME_REFRESH_INTERVAL; 
        		 _activeChar.sendPacket(new PetLiveTime(_summon));
        		 _summonLifeTask = ThreadPoolManager.getInstance().scheduleGeneral(new SummonLifetime(_activeChar, _summon), SUMMON_LIFETIME_REFRESH_INTERVAL);
        	 }
        	 else
        	 {
        		 _lifeTime = 0;
        		 _activeChar.sendPacket(new PetLiveTime(_summon));
        	 }
        }
    }

    @Override
    public int getCurrentFed() { return _lifeTime; }
    
	@Override
	public int getMaxFed() { return SUMMON_LIFETIME_INTERVAL; }
        	
	@Override
	public void unSummon(L2PcInstance owner)
	{
       	if (_log.isDebugEnabled())
       		_log.warn("L2SummonInstance: " + getTemplate().getName() + " (" + owner.getName() + ") unsummoned.");

        if (_summonLifeTask != null) {
        	_summonLifeTask.cancel(true);
        	_summonLifeTask = null;
        }
        
        if (_summonConsumeTask != null) {
            _summonConsumeTask.cancel(true);
            _summonConsumeTask = null;
         }
        
        super.unSummon(owner);
	}

	@Override
	public boolean destroyItem(String process, int objectId, int count, L2Object reference, boolean sendMessage)
	{
		return getOwner().destroyItem(process, objectId, count, reference, sendMessage);
	}

	@Override
	public boolean destroyItemByItemId(String process, int itemId, int count, L2Object reference, boolean sendMessage)
	{
       	if (_log.isDebugEnabled())
			_log.warn("L2SummonInstance: " + getTemplate().getName() + " (" + getOwner().getName() + ") consume.");
	
		return getOwner().destroyItemByItemId(process, itemId, count, reference, sendMessage);
	}
    
    @Override
    public final void sendDamageMessage(L2Character target, int damage, boolean mcrit, boolean pcrit, boolean miss)
    {
    	if (miss) return;
        	
    	// Prevents the double spam of system messages, if the target is the owning player.
    	if (target.getObjectId() != getOwner().getObjectId())
    	{
    		if (pcrit || mcrit)
    			getOwner().sendPacket(new SystemMessage(SystemMessageId.CRITICAL_HIT_BY_SUMMONED_MOB));

    		SystemMessage sm = new SystemMessage(SystemMessageId.SUMMON_GAVE_DAMAGE_S1);
    		sm.addNumber(damage);
    		getOwner().sendPacket(sm);
        }
    }
}
