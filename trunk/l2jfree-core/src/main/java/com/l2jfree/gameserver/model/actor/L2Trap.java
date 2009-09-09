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

import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.view.CharLikeView;
import com.l2jfree.gameserver.model.actor.view.TrapView;
import com.l2jfree.gameserver.network.serverpackets.AbstractNpcInfo;
import com.l2jfree.gameserver.taskmanager.DecayTaskManager;
import com.l2jfree.gameserver.templates.chars.L2CharTemplate;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;

/**
 *
 * @author nBd
 */
public class L2Trap extends L2Character
{
	private final L2PcInstance _owner;
	/**
	 * @param objectId
	 * @param template
	 */
	public L2Trap(int objectId, L2CharTemplate template, L2PcInstance owner)
	{
		super(objectId, template);
		getKnownList();
		getStat();
		getStatus();
		_owner = owner;
		getPosition().setXYZInvisible(owner.getX(), owner.getY(), owner.getZ());
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
	 *
	 * @see com.l2jfree.gameserver.model.actor.L2Character#onSpawn()
	 */
	@Override
	public void onSpawn()
	{
		super.onSpawn();
	}

	/**
	 *
	 * @see com.l2jfree.gameserver.model.actor.L2Object#onAction(com.l2jfree.gameserver.model.actor.instance.L2PcInstance)
	 */
	@Override
	public void onAction(L2PcInstance player)
	{
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
	 *
	 * @see com.l2jfree.gameserver.model.actor.L2Character#onDecay()
	 */
	@Override
	public void onDecay()
	{
		deleteMe(_owner);
	}

	/**
	 *
	 * @return
	 */
	public final int getNpcId()
	{
		return getTemplate().getNpcId();
	}

	/**
	 *
	 * @see com.l2jfree.gameserver.model.L2Object#isAutoAttackable(com.l2jfree.gameserver.model.actor.L2Character)
	 */
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return _owner.isAutoAttackable(attacker);
	}

	/**
	 *
	 * @param owner
	 */
	public void deleteMe(L2PcInstance owner)
	{
		decayMe();
		getKnownList().removeAllKnownObjects();
		owner.setTrap(null);
	}

	/**
	 *
	 * @param owner
	 */
	public synchronized void unSummon(L2PcInstance owner)
	{
		if (isVisible() && !isDead())
		{
			if (getWorldRegion() != null)
				getWorldRegion().removeFromZones(this);
			owner.setTrap(null);
			decayMe();
			getKnownList().removeAllKnownObjects();
		}
	}

	/**
	 *
	 * @see com.l2jfree.gameserver.model.actor.L2Character#getLevel()
	 */
	@Override
	public int getLevel()
	{
		return getTemplate().getLevel();
	}

	/**
	 *
	 * @return
	 */
	public final L2PcInstance getOwner()
	{
		return _owner;
	}

	@Override
	public L2PcInstance getActingPlayer()
	{
		return _owner;
	}

	/**
	 *
	 * @see com.l2jfree.gameserver.model.actor.L2Character#getTemplate()
	 */
	@Override
	public L2NpcTemplate getTemplate()
	{
		return (L2NpcTemplate) super.getTemplate();
	}

	/**
	 *
	 * @return
	 */
	public boolean isDetected()
	{
		return false;
	}

	/**
	 *
	 *
	 */
	public void setDetected()
	{
		// Do nothing
	}
	
	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		activeChar.sendPacket(new AbstractNpcInfo.TrapInfo(this));
	}
	
	@Override
	public void broadcastFullInfoImpl()
	{
	}
}
