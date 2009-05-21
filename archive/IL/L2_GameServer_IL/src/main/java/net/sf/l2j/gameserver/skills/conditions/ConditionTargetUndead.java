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
import net.sf.l2j.gameserver.model.actor.instance.L2SummonInstance;
import net.sf.l2j.gameserver.skills.Env;

public class ConditionTargetUndead extends Condition
{
    final boolean _isUndead;
    
    public ConditionTargetUndead(boolean isUndead)
    {
        _isUndead = isUndead;
    }

    @Override
    public boolean testImpl(Env env)
    {
        L2Character target = (L2Character)env.player.getTarget();

        if(target == null) return false;
        if (target instanceof L2MonsterInstance) return ((L2MonsterInstance)target).isUndead() == _isUndead;
        if (target instanceof L2SummonInstance) return ((L2SummonInstance)target).isUndead() == _isUndead;

        return false;
    }
}
