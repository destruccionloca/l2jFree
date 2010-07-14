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
import com.l2jfree.gameserver.model.actor.L2Npc;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.tools.random.Rnd;

/**
 * @author Psycho(killer1888) / L2jFree
 */
public class StarStone extends L2AttackableAIScript
{
	private static final int[] NPC_IDS = {
		18684,18685,18686,18687,18688,18689,18690,18691,18692
	};

	public StarStone(int questId, String name, String descr)
	{
		super(questId, name, descr);
		registerMobs(NPC_IDS);
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		int npcId = npc.getNpcId();
		int itemId = 0;

		if (!contains(targets, npc))
		{
			return super.onSkillSee(npc, caster, skill, targets, isPet);
		}

		if (skill.getId() == 932)
		{
			switch (npcId)
			{
				case 18684:
				case 18685:
				case 18686:
					itemId = 14009;
					break;
				case 18687:
				case 18688:
				case 18689:
					itemId = 14010;
					break;
				case 18690:
				case 18691:
				case 18692:
					itemId = 14011;
					break;
				default:
					return null;
			}

			int amount = Rnd.get(0,2);

			if (amount > 0)
			{
				caster.addItem("Star Stone", itemId, amount, caster, true, true);
				caster.sendPacket(new SystemMessage(SystemMessageId.STARSTONE_COLLECTED));
			}
			else
				caster.sendPacket(new SystemMessage(SystemMessageId.STARSTONE_COLLECTION_FAILED));
			npc.doDie(caster);
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}

	public static void main(String[] args)
	{
		new StarStone(-1, "StarStone", "ai");
	}
}
