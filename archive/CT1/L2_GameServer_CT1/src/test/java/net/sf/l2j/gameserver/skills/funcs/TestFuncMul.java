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
package net.sf.l2j.gameserver.skills.funcs;

import junit.framework.TestCase;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Stats;

public class TestFuncMul extends TestCase
{
    public void testFuncDivCalc()
    {
        FuncMul fa = new FuncMul(Stats.MAX_HP, 1, null, new LambdaConst(2));

        Env env = new Env();
        env.value = 1;
        fa.calc(env);
        assertEquals(2.0, env.value);
    }
}
