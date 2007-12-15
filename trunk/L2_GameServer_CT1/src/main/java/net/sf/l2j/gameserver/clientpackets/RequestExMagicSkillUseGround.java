/* This program is free software; you can redistribute it and/or modify
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

package net.sf.l2j.gameserver.clientpackets;

import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Fromat:(ch) dddddc
 * @author  -Wooden-
 */
public final class RequestExMagicSkillUseGround extends L2GameClientPacket
{
	private static final String _C__D0_2F_REQUESTEXMAGICSKILLUSEGROUND = "[C] D0:2F RequestExMagicSkillUseGround";
	private final static Log _log = LogFactory.getLog(RequestExMagicSkillUseGround.class.getName());

	private int _x;
	private int _y;
	private int _z;
	private int _skillId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;

	@Override
	protected void readImpl()
	{
		_x	= readD();
		_y	= readD();
		_z	= readD();
		_skillId		= readD();
		_ctrlPressed	= readD() != 0;
		_shiftPressed	= readC() != 0;
	}

	/**
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
		//TODO: implementation missing
		System.out.println("C6: RequestExMagicSkillUseGround. x: "+_x+" y: "+_y+" z: "+_z+" skill: "+_skillId+" crtl: "+_ctrlPressed+" shift: "+_shiftPressed);
		
		L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		int level = activeChar.getSkillLevel(_skillId);
		
		if (level <= 0 || activeChar.isOutOfControl())
		{
			activeChar.sendPacket(new ActionFailed());
			return;
		}
		
		L2Skill skill = SkillTable.getInstance().getInfo(_skillId, level);
		
		if (skill == null || skill.getSkillType() == SkillType.NOTDONE)
		{
			activeChar.sendPacket(new ActionFailed());
			return;
		}
		
		// TODO: Handler for that skills... 
		activeChar.sendPacket(new ActionFailed());
	}

	/**
	 * @see net.sf.l2j.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__D0_2F_REQUESTEXMAGICSKILLUSEGROUND;
	}
}
