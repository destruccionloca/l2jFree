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
package ai.group_template;

import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.quest.Quest;
import com.l2jfree.gameserver.model.quest.jython.QuestJython;

/**
 * 
 * Overarching Superclass for all mob AI
 * @author Fulminus
 *
 */
public class L2AttackableAIScript extends QuestJython
{
	/**
	 * This is used to register all monsters contained in mobs for a particular script
	 * @param mobs
	 */
	public void registerMobs(int[] mobs)
	{
		for (int id : mobs)
		{
			this.addEventId(id, Quest.QuestEventType.ON_ATTACK);
			this.addEventId(id, Quest.QuestEventType.ON_KILL);
			this.addEventId(id, Quest.QuestEventType.ON_SPAWN);
			this.addEventId(id, Quest.QuestEventType.ON_SPELL_FINISHED);
			this.addEventId(id, Quest.QuestEventType.ON_SKILL_SEE);
			this.addEventId(id, Quest.QuestEventType.ON_FACTION_CALL);
			this.addEventId(id, Quest.QuestEventType.ON_AGGRO_RANGE_ENTER);
		}
	}

	/**
	 * This is used simply for convenience of replacing
	 * jython 'element in list' boolean method.
	 */
	public static <T> boolean contains(T[] array, T obj)
	{
		for (int i = 0; i < array.length; i++)
		{
			if (array[i] == obj)
			{
				return true;
			}
		}
		return false;
	}

	public static boolean contains(int[] array, int obj)
	{
		for (int i = 0; i < array.length; i++)
		{
			if (array[i] == obj)
			{
				return true;
			}
		}
		return false;
	}

	public L2AttackableAIScript(int questId, String name, String descr)
	{
		super(questId, name, descr);
	}

	public static void main(String[] args)
	{
	}

	public String onAdvEvent (String event, L2NpcInstance npc, L2PcInstance player)
	{
		return null;
	}
	
	public String onSpellFinished(L2NpcInstance npc, L2PcInstance player, L2Skill skill)
	{
		return null;
	}

	public String onSkillSee (L2NpcInstance npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet) 
	{
		return null;
	}

	public String onFactionCall (L2NpcInstance npc, L2NpcInstance caller, L2PcInstance attacker, boolean isPet) 
	{
		return null;
	}

	public String onAggroRangeEnter (L2NpcInstance npc, L2PcInstance player, boolean isPet) 
	{
		return null; 
	}

	public String onSpawn (L2NpcInstance npc) 
	{
		return null; 
	}

	public String onAttack (L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		return null;
	}

	public String onKill (L2NpcInstance npc, L2PcInstance killer, boolean isPet) 
	{
		return null; 
	}
}