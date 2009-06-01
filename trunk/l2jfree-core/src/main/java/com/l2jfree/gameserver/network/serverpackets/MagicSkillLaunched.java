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
package com.l2jfree.gameserver.network.serverpackets;

import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.L2Character;

public final class MagicSkillLaunched extends L2GameServerPacket
{
	private static final String _S__8E_MAGICSKILLLAUNCHED = "[S] 8E MagicSkillLaunched";
	
	private final int _charObjId;
	private final int _skillId;
	private final int _skillLevel;
	private final L2Object[] _targets;
	
	public MagicSkillLaunched(L2Character cha, int skillId, int skillLevel, L2Object... targets)
	{
		_charObjId = cha.getObjectId();
		_skillId = skillId;
		_skillLevel = skillLevel;
		_targets = targets;
	}
	
	public MagicSkillLaunched(L2Character cha, L2Skill skill, L2Object... targets)
	{
		this(cha, skill.getDisplayId(), skill.getLevel(), targets);
	}
	
	public MagicSkillLaunched(L2Character cha, int skillId, int skillLevel)
	{
		this(cha, skillId, skillLevel, cha.getTarget());
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0x54);
		writeD(_charObjId);
		writeD(_skillId);
		writeD(_skillLevel);
		writeD(_targets.length);
		
		if (_targets.length == 0)
			writeD(0);
		else
			for (L2Object target : _targets)
				writeD(target == null ? 0 : target.getObjectId());
	}
	
	@Override
	public String getType()
	{
		return _S__8E_MAGICSKILLLAUNCHED;
	}
}
