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

import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.instancemanager.CastleManager;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfree.gameserver.model.entity.Castle;
import com.l2jfree.gameserver.model.entity.Siege;

/**
 * @author Savormix
 * @since 2009-04-22
 */
public class L2SiegeDangerZone extends L2DamageZone
{
	private static final int[] ZONE_EFFECTS = { 4150, 4625 };
	private Siege _siege;

	@Override
	protected boolean checkDynamicConditions(L2Character character)
	{
		if (_siege == null || !_siege.getIsInProgress() || !(character instanceof L2PlayableInstance)
				|| !_siege.getAreTrapsOn())
				//|| _siege.checkIsDefender(((L2PlayableInstance) character).getActingPlayer().getClan()))
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

	public void upgrade(int... level)
	{
		L2Skill s;
		for (int i = 0; i < 2; i++)
		{
			if (level[0] > 0 || level[1] > 0)
			{
				s = SkillTable.getInstance().getInfo(ZONE_EFFECTS[i], level[i]);
				if (s != null) {
					_applyEnter = (L2Skill[])ArrayUtils.add(_applyEnter, s);
					_removeExit = ArrayUtils.add(_removeExit, ZONE_EFFECTS[i]);
				}
				else
					_log.warn("Upgrading siege danger zone: no such skill level - " + level);
			}
			else {
				_applyEnter = null;
				for (L2Character c : getCharactersInside())
					removeFromZone(c);
				_removeExit = null;
			}
		}
	}

	public boolean isUpgraded()
	{
		try { return _applyEnter[0].getLevel() > 0; }
		catch (NullPointerException npe) { return false; }
	}
}
