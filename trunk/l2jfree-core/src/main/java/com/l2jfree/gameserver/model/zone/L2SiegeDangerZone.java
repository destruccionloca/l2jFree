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
package com.l2jfree.gameserver.model.zone;

import org.apache.commons.lang.ArrayUtils;
import org.w3c.dom.Node;

import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.instancemanager.CastleManager;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Playable;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.entity.Castle;
import com.l2jfree.gameserver.model.entity.Siege;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Savormix
 * @since 2009-04-22
 */
public class L2SiegeDangerZone extends L2DamageZone
{
	private static final int DECREASE_SPEED_SKILL = 4625;
	
	private Siege _siege;
	private boolean _active;
	
	@Override
	protected void checkForDamage(L2Character character)
	{
		super.checkForDamage(character);
		
		if (getHPDamagePerSecond() > 0 && character instanceof L2Playable)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.C1_RECEIVED_DAMAGE_FROM_S2_THROUGH_FIRE_OF_MAGIC);
			sm.addCharName(character);
			sm.addNumber(getHPDamagePerSecond());
			character.getActingPlayer().sendPacket(sm);
		}
	}
	
	@Override
	protected boolean checkDynamicConditions(L2Character character)
	{
		if (_siege == null || !_siege.getIsInProgress())
			return false;
		
		if (!isActive())
			return false;
		
		// non-playables and during siege defenders not affected
		final L2PcInstance player = character.getActingPlayer();
		if (player == null || player.isInSiege() && player.getSiegeState() == L2PcInstance.SIEGE_STATE_DEFENDER)
			return false;
		
		return super.checkDynamicConditions(character);
	}
	
	@Override
	protected void register() throws Exception
	{
		Castle c = CastleManager.getInstance().getCastleById(getCastleId());
		_siege = c.getSiege();
		c.loadDangerZone(this);
	}
	
	@Override
	protected void parseSkills(Node n) throws Exception
	{
		super.parseSkills(n);
		
		final L2Skill s = SkillTable.getInstance().getInfo(DECREASE_SPEED_SKILL, 12);
		if (s != null)
		{
			if (_applyEnter == null)
				_applyEnter = new L2Skill[] { s };
			else
				_applyEnter = (L2Skill[])ArrayUtils.add(_applyEnter, s);
			
			_removeExit = ArrayUtils.add(_removeExit, DECREASE_SPEED_SKILL);
		}
		else
			_log.warn("Missing siege danger zone skill! " + DECREASE_SPEED_SKILL + " Lv 12");
	}
	
	/** Activates this zone. */
	public void activate()
	{
		_active = true;
		
		revalidateAllInZone();
	}
	
	/** Deactivates this zone. */
	public void deactivate()
	{
		_active = false;
		
		revalidateAllInZone();
	}
	
	public boolean isActive()
	{
		return _active;
	}
}
