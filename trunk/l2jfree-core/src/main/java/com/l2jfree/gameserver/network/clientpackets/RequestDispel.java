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
package com.l2jfree.gameserver.network.clientpackets;

import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.templates.skills.L2EffectType;

/** 
 * @author evill33t/crion
 */
public class RequestDispel extends L2GameClientPacket
{
	private static final String	_C__D0_78_REQUESTDISPEL	= "[C] D0 4E RequestDispel";

	private int					_skillId;
	private int					_skillLevel;

	@Override
	protected void readImpl()
	{
		_skillId = readD();
		_skillLevel = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance cha = getClient().getActiveChar();
		if (cha == null) return;

		L2Skill s;
		for (L2Effect e : cha.getAllEffects())
		{
			s = e.getSkill();
			if (s.getId() == _skillId && s.getLevel() == _skillLevel)
			{
				if (!s.isDance() && !s.isSong() && !s.isDebuff() &&
						e.getEffectType() != L2EffectType.TRANSFORMATION &&
						e.getEffectType() != L2EffectType.ENVIRONMENT)
					e.exit();
			}
		}
		s = null;
		sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public String getType()
	{
		return _C__D0_78_REQUESTDISPEL;
	}
}
