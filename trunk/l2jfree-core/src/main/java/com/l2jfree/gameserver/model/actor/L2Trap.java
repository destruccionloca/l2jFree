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
package com.l2jfree.gameserver.model.actor;

import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.knownlist.CharKnownList;
import com.l2jfree.gameserver.model.actor.knownlist.TrapKnownList;
import com.l2jfree.gameserver.model.actor.view.CharLikeView;
import com.l2jfree.gameserver.model.actor.view.TrapView;
import com.l2jfree.gameserver.network.serverpackets.AbstractNpcInfo;
import com.l2jfree.gameserver.network.serverpackets.SocialAction;
import com.l2jfree.gameserver.taskmanager.DecayTaskManager;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;

/**
 * @author nBd
 */
public class L2Trap extends L2Character
{
	protected static final int TICK = 1000; // 1s
	
	private boolean _isTriggered;
	private final L2Skill _skill;
	private final int _lifeTime;
	private int _timeRemaining;
	
	/**
	 * @param objectId
	 * @param template
	 */
	public L2Trap(int objectId, L2NpcTemplate template, int lifeTime, L2Skill skill)
	{
		super(objectId, template);
		setName(template.getName());
		getKnownList();
		getStat();
		getStatus();
		setIsInvul(false);
		
		_isTriggered = false;
		_skill = skill;
		_lifeTime = lifeTime == 0 ? 30000 : lifeTime;
		_timeRemaining = _lifeTime;
		
		ThreadPoolManager.getInstance().schedule(new TrapTask(), TICK);
	}
	
	@Override
	protected CharKnownList initKnownList()
	{
		return new TrapKnownList(this);
	}
	
	@Override
	public TrapKnownList getKnownList()
	{
		return (TrapKnownList)_knownList;
	}
	
	@Override
	protected CharLikeView initView()
	{
		return new TrapView(this);
	}
	
	@Override
	public TrapView getView()
	{
		return (TrapView)_view;
	}
	
	/**
	 * @see com.l2jfree.gameserver.model.actor.L2Object#onAction(com.l2jfree.gameserver.model.actor.instance.L2PcInstance)
	 */
	@Override
	public void onAction(L2PcInstance player, boolean interact)
	{
		// Aggression target lock effect
		if (!player.canChangeLockedTarget(this))
			return;
		
		player.setTarget(this);
	}
	
	@Override
	public int getMyTargetSelectedColor(L2PcInstance player)
	{
		return player.getLevel() - getLevel();
	}
	
	/**
	 *
	 *
	 */
	public void stopDecay()
	{
		DecayTaskManager.getInstance().cancelDecayTask(this);
	}
	
	/**
	 * @see com.l2jfree.gameserver.model.actor.L2Character#onDecay()
	 */
	@Override
	public void onDecay()
	{
		deleteMe();
	}
	
	/**
	 * @return
	 */
	public final int getNpcId()
	{
		return getTemplate().getNpcId();
	}
	
	/**
	 * @see com.l2jfree.gameserver.model.L2Object#isAutoAttackable(com.l2jfree.gameserver.model.actor.L2Character)
	 */
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return !canSee(attacker);
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
			return false;
		
		DecayTaskManager.getInstance().addDecayTask(this);
		return true;
	}
	
	/**
	 * @param owner
	 */
	public void deleteMe()
	{
		decayMe();
		getKnownList().removeAllKnownObjects();
	}
	
	/**
	 * @param owner
	 */
	public synchronized void unSummon()
	{
		if (isVisible() && !isDead())
		{
			if (getWorldRegion() != null)
				getWorldRegion().removeFromZones(this);
			
			deleteMe();
		}
	}
	
	/**
	 * @see com.l2jfree.gameserver.model.actor.L2Character#getLevel()
	 */
	@Override
	public int getLevel()
	{
		return getTemplate().getLevel();
	}
	
	/**
	 * @return
	 */
	public L2PcInstance getOwner()
	{
		return null;
	}
	
	@Override
	public L2PcInstance getActingPlayer()
	{
		return getOwner();
	}
	
	/**
	 * @see com.l2jfree.gameserver.model.actor.L2Character#getTemplate()
	 */
	@Override
	public L2NpcTemplate getTemplate()
	{
		return (L2NpcTemplate)super.getTemplate();
	}
	
	public int getKarma()
	{
		return 0;
	}
	
	public byte getPvpFlag()
	{
		return 0;
	}
	
	public L2Skill getSkill()
	{
		return _skill;
	}
	
	/**
	 * Checks is triggered
	 * 
	 * @return True if trap is triggered.
	 */
	public boolean isTriggered()
	{
		return _isTriggered;
	}
	
	/**
	 * Checks trap visibility
	 * 
	 * @param cha - checked character
	 * @return True if character can see trap
	 */
	public boolean canSee(L2Character cha)
	{
		return false;
	}
	
	/**
	 * Reveal trap to the detector (if possible)
	 * 
	 * @param detector
	 */
	public void setDetected(L2Character detector)
	{
		sendInfo(detector.getActingPlayer());
	}
	
	/**
	 * Check if target can trigger trap
	 * 
	 * @param target
	 * @return
	 */
	protected boolean checkTarget(L2Character target)
	{
		return L2Skill.checkForAreaOffensiveSkills(this, target, _skill, false);
	}
	
	private class TrapTask implements Runnable
	{
		public void run()
		{
			try
			{
				if (!_isTriggered)
				{
					_timeRemaining -= TICK;
					
					if (_timeRemaining < _lifeTime - 15000)
					{
						SocialAction sa = new SocialAction(getObjectId(), 2);
						broadcastPacket(sa);
					}
					
					if (_timeRemaining < 0)
					{
						switch (getSkill().getTargetType())
						{
							case TARGET_AURA:
							case TARGET_FRONT_AURA:
							case TARGET_BEHIND_AURA:
								trigger(L2Trap.this);
								break;
							default:
								unSummon();
						}
						return;
					}
					
					for (L2Character target : getKnownList().getKnownCharactersInRadius(_skill.getSkillRadius()))
					{
						if (!checkTarget(target))
							continue;
						
						trigger(target);
						return;
					}
					
					ThreadPoolManager.getInstance().schedule(new TrapTask(), TICK);
				}
			}
			catch (Exception e)
			{
				_log.warn("", e);
				unSummon();
			}
		}
	}
	
	/**
	 * Trigger trap
	 * 
	 * @param target
	 */
	public void trigger(L2Character target)
	{
		_isTriggered = true;
		broadcastFullInfoImpl();
		setTarget(target);
		ThreadPoolManager.getInstance().schedule(new TriggerTask(), 300);
	}
	
	private class TriggerTask implements Runnable
	{
		public void run()
		{
			try
			{
				doCast(_skill);
				ThreadPoolManager.getInstance().schedule(new UnsummonTask(), _skill.getHitTime() + 300);
			}
			catch (Exception e)
			{
				unSummon();
			}
		}
	}
	
	private class UnsummonTask implements Runnable
	{
		public void run()
		{
			unSummon();
		}
	}
	
	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		activeChar.sendPacket(new AbstractNpcInfo.TrapInfo(this));
	}
	
	@Override
	public void broadcastFullInfoImpl()
	{
		broadcastPacket(new AbstractNpcInfo.TrapInfo(this));
	}
}
