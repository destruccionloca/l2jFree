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

import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.instancemanager.CastleManager;
import com.l2jfree.gameserver.instancemanager.ClanHallManager;
import com.l2jfree.gameserver.instancemanager.FortManager;
import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.entity.AbstractSiege;
import com.l2jfree.gameserver.model.entity.Castle;
import com.l2jfree.gameserver.model.entity.ClanHall;
import com.l2jfree.gameserver.model.entity.Fort;
import com.l2jfree.gameserver.model.entity.Siegeable;

public abstract class SiegeableEntityZone extends L2Zone
{
	protected Siegeable<? extends AbstractSiege> _entity;
	
	protected Siegeable<? extends AbstractSiege> initSiegeableEntity() throws Exception
	{
		int givenIdCount = 0;
		if (getCastleId() > 0)
			givenIdCount++;
		if (getFortId() > 0)
			givenIdCount++;
		if (getClanhallId() > 0)
			givenIdCount++;
		
		if (givenIdCount > 1)
			throw new IllegalArgumentException("Invalid castleId: " + getCastleId() + ", fortId: " + getFortId() + " and clanhallId: " + getClanhallId());
		
		if (getCastleId() > 0)
			return initCastle();
		
		if (getFortId() > 0)
			return initFort();
		
		if (getClanhallId() > 0)
			return initHideout();
		
		throw new IllegalArgumentException("Invalid entity!");
	}
	
	protected Castle initCastle() throws Exception
	{
		final Castle castle = CastleManager.getInstance().getCastleById(getCastleId());
		
		if (castle == null)
			throw new IllegalArgumentException("Invalid castleId: " + getCastleId());
		
		return castle;
	}
	
	protected Fort initFort() throws Exception
	{
		final Fort fort = FortManager.getInstance().getFortById(getFortId());
		
		if (fort == null)
			throw new IllegalArgumentException("Invalid fortId: " + getFortId());
		
		return fort;
	}
	
	protected ClanHall initHideout() throws Exception
	{
		final ClanHall hideout = ClanHallManager.getInstance().getClanHallById(getClanhallId());
		
		if (hideout == null)
			throw new IllegalArgumentException("Invalid clanHallId: " + getFortId());
		
		return hideout;
	}
	
	@SuppressWarnings("unchecked")
	protected <T extends AbstractSiege> T getSiege()
	{
		return (T)_entity.getSiege();
	}
	
	protected boolean isSiegeInProgress()
	{
		if (_entity.getSiege() != null)
			return _entity.getSiege().getIsInProgress();
		else
			// TODO: Not siegeable, but rather contested differently
			return false;
	}
	
	public static final int DEATH_SYNDROME = 5660;
	
	protected final void applyDeathSyndrome(L2Character character)
	{
		// debuff participants only if they die inside siege zone
		if (character instanceof L2PcInstance && isSiegeInProgress())
		{
			int lvl;
			L2Effect effect = character.getFirstEffect(DEATH_SYNDROME);
			if (effect != null)
				lvl = Math.min(effect.getLevel() + 1, SkillTable.getInstance().getMaxLevel(DEATH_SYNDROME));
			else
				lvl = 1;
			
			L2Skill skill = SkillTable.getInstance().getInfo(DEATH_SYNDROME, lvl);
			if (skill != null)
				skill.getEffects(character, character);
		}
	}
}
