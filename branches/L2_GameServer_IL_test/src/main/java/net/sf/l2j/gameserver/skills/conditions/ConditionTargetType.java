/*
 * This program is free software; you can redistribute it and/or modify
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
package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.skills.Env;

public class ConditionTargetType extends Condition
{
    private final boolean _pc, _npc, _mob;
    
    public ConditionTargetType(boolean pc, boolean npc, boolean mob)
    {
        _pc = pc;  _npc = npc;  _mob = mob;
    }

    @Override
    public boolean testImpl(Env env)
    {
        L2Character target = (L2Character)env.player.getTarget();
        if(target == null) return false;
		
		boolean check = false;
		
        if (target instanceof L2PlayableInstance) check = _pc;
        if (target instanceof L2MonsterInstance) check = check || _mob;
		if (target instanceof L2NpcInstance && !((L2NpcInstance)target).isMob()) check = check || _npc;

        return check;
    }
}
