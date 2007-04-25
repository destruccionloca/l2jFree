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
package net.sf.l2j.gameserver.datatables;

import javolution.util.FastList;
import net.sf.l2j.gameserver.model.L2Skill;

/**
 *
 * @author -Nemesiss-
 */
public class NobleSkillTable
{
    private static NobleSkillTable _instance;
    private static FastList<L2Skill> _NobleSkills;
    private static final int[] _NobleSkillsIds = {325,326,327,1323,1324,1325,1326,1327};
    
    private NobleSkillTable()
    {
        _NobleSkills = new FastList<L2Skill>();
        for(int _skillId : _NobleSkillsIds)
        	_NobleSkills.add(SkillTable.getInstance().getInfo(_skillId, 1));
    }
    
    public static NobleSkillTable getInstance()
    {
        if (_instance == null)
            _instance = new NobleSkillTable();
        return _instance;
    }
    
    public FastList<L2Skill> GetNobleSkills()
    {
        return _NobleSkills;
    }
}
