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

import com.l2jfree.Config;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2Skill.SkillType;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

/**
 * 
 *
 * sample 
 * 0000: 6d 0c 00 00 00 00 00 00 00 03 00 00 00 f3 03 00    m...............
 * 0010: 00 00 00 00 00 01 00 00 00 f4 03 00 00 00 00 00    ................
 * 0020: 00 01 00 00 00 10 04 00 00 00 00 00 00 01 00 00    ................
 * 0030: 00 2c 04 00 00 00 00 00 00 03 00 00 00 99 04 00    .,..............
 * 0040: 00 00 00 00 00 02 00 00 00 a0 04 00 00 00 00 00    ................
 * 0050: 00 01 00 00 00 c0 04 00 00 01 00 00 00 01 00 00    ................
 * 0060: 00 76 00 00 00 01 00 00 00 01 00 00 00 a3 00 00    .v..............
 * 0070: 00 01 00 00 00 01 00 00 00 c2 00 00 00 01 00 00    ................
 * 0080: 00 01 00 00 00 d6 00 00 00 01 00 00 00 01 00 00    ................
 * 0090: 00 f4 00 00 00
 * 
 * format   d (ddd)
 * 
 * @version $Revision: 1.3.2.1.2.5 $ $Date: 2005/03/27 15:29:39 $
 */
public class SkillList extends L2GameServerPacket
{
    private static final String _S__6D_SKILLLIST = "[S] 58 SkillList";
    private L2PcInstance _owner;

    public SkillList(L2PcInstance client)
    {
        _owner = client;
    }

    @Override
    protected final void writeImpl()
    {
        writeC(0x5f);
        writeD(_owner.getAllSkills().length);
        L2Skill templ[] = _owner.getAllSkills();
        for (int i = 0; i < templ.length; i++)
        {
            L2Skill temp = templ[i];
        	
			if (temp.getSkillType() == SkillType.NOTDONE)
			{
				switch (Config.SEND_NOTDONE_SKILLS)
				{
					case 2:
						if (_owner.isGM())
							break;
						break;
					case 1:
						continue;
				}
			}
        	
            writeD(temp.isPassive() ? 1 : 0);
            writeD(temp.getLevel());
            writeD(temp.getDisplayId());
            writeC(0x00); //transformation
        }
    }

    /* (non-Javadoc)
     * @see com.l2jfree.gameserver.serverpackets.ServerBasePacket#getType()
     */
    @Override
    public String getType()
    {
        return _S__6D_SKILLLIST;
    }
}
