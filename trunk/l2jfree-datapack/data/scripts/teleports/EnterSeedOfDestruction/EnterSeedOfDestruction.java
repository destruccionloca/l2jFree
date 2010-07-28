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
package teleports.EnterSeedOfDestruction;

import com.l2jfree.gameserver.instancemanager.SeedOfDestructionManager;
import com.l2jfree.gameserver.model.actor.L2Npc;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.quest.Quest;


/**
 * @author Psycho(killer1888) / L2jfree
 */

public class EnterSeedOfDestruction extends Quest
{
	private final static int npcId = 32526;

	public EnterSeedOfDestruction(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(npcId);
		addFirstTalkId(npcId);
		addTalkId(npcId);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		return event+".htm";
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (player.getQuestState("EnterSeedOfDestruction") == null)
			newQuestState(player);

		String htmltext = "";
		long mode = SeedOfDestructionManager.getInstance().getState();
		if (mode == 1)
			htmltext = "32526-AttackMode.htm";
		else if (mode == 2)
			htmltext = "32526-AttackMode.htm";
		else
			htmltext = "32526-HuntingGroundMode.htm";

		return htmltext;
	}

	public static void main(String[] args)
	{
		new EnterSeedOfDestruction(-1, "EnterSeedOfDestruction", "teleports");
	}
}
