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
import com.l2jfree.gameserver.model.entity.Siege;

/**
 * A generic implementation; just that the zones would be loaded without errors
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
				|| !_siege.getIsDangerZoneOn())
				//|| _siege.checkIsDefender(((L2PlayableInstance) character).getActingPlayer().getClan()))
			return false;

		return super.checkDynamicConditions(character);
	}

	@Override
	protected void register() throws Exception
	{
		_siege = CastleManager.getInstance().getCastleById(getCastleId()).getSiege();
		CastleManager.getInstance().loadDangerZone(getCastleId(), this);
	}

	public void upgrade(int level)
	{
		L2Skill s;
		for (int id : ZONE_EFFECTS)
		{
			if (level > 0) {
				s = SkillTable.getInstance().getInfo(id, level);
				if (s != null) {
					_applyEnter = (L2Skill[])ArrayUtils.add(_applyEnter, s);
					_removeExit = ArrayUtils.add(_removeExit, id);
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

	public int getUpgradeLevel()
	{
		try { return _applyEnter[0].getLevel(); }
		catch (NullPointerException npe) { return 0; }
	}
}
