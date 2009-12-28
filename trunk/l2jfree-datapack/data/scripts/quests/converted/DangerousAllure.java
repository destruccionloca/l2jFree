package quests.converted;

import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.L2Npc;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.base.Race;
import com.l2jfree.gameserver.model.itemcontainer.PcInventory;
import com.l2jfree.gameserver.model.quest.QuestState;
import com.l2jfree.gameserver.model.quest.State;
import com.l2jfree.gameserver.model.quest.jython.QuestJython;
import com.l2jfree.gameserver.network.serverpackets.NpcSay;

/**
 * A quest restricted to dark elves.
 * @author savormix
 */
public final class DangerousAllure extends QuestJython
{
	private static final String DANGEROUS_ALLURE = "170_DangerousAllure";

	// Quest NPCs
	private static final int VELLIOR = 30305;

	// Quest items
	private static final int NIGHTMARE_CRYSTAL = 1046;

	// Quest monsters
	private static final int MERKENIS = 27022;
	private static final String MERKENIS_ATTACKED = "I shall put you in a never-ending nightmare!";
	private static final String MERKENIS_KILLED = "My soul is to Icarus...";

	public DangerousAllure(int questId, String name, String descr)
	{
		super(questId, name, descr);
		questItemIds = new int[] { NIGHTMARE_CRYSTAL };
		addStartNpc(VELLIOR);
		addTalkId(VELLIOR);
		addAttackId(MERKENIS);
		addKillId(MERKENIS);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState qs = player.getQuestState(DANGEROUS_ALLURE);
		if (qs.isCompleted())
			return QUEST_DONE;
		else if ("1".equals(event))
		{
			qs.set(CONDITION, 1);
			qs.setState(State.STARTED);
			player.sendPacket(SND_ACCEPT);
			return "30305-04.htm";
		}
		else
			return event;
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet,
			L2Skill skill)
	{
		switch (npc.getQuestAttackStatus())
		{
		case 0:
			npc.broadcastPacket(new NpcSay(npc, MERKENIS_ATTACKED));
			npc.setQuestAttackStatus(ATTACK_SINGLE);
			npc.setQuestFirstAttacker(attacker);
			break;
		case 1:
			if (attacker != npc.getQuestFirstAttacker())
				npc.setQuestAttackStatus(ATTACK_MULTIPLE);
			break;
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		L2PcInstance quester = npc.getQuestFirstAttacker();
		if (quester == null)
			return null;
		QuestState qs = quester.getQuestState(DANGEROUS_ALLURE);
		if (qs == null || qs.getState() != State.STARTED || qs.getInt(CONDITION) != 1
				|| npc.getQuestAttackStatus() != ATTACK_SINGLE)
			return null;

		if (qs.getQuestItemsCount(NIGHTMARE_CRYSTAL) == 0)
		{
			npc.broadcastPacket(new NpcSay(npc, MERKENIS_KILLED));
			qs.giveItems(NIGHTMARE_CRYSTAL, 1);
			quester.sendPacket(SND_MIDDLE);
			qs.set(CONDITION, 2);
		}

		return null;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance talker)
	{
		QuestState qs = talker.getQuestState(DANGEROUS_ALLURE);
		if (qs == null)
			return NO_QUEST;
		else if (qs.isCompleted())
			return QUEST_DONE;

		int cond = qs.getInt(CONDITION);
		if (cond == 0)
		{
			if (talker.getRace() != Race.Darkelf)
			{
				qs.exitQuest(true);
				return "30305-00.htm";
			}
			else if (talker.getLevel() < 21)
			{
				qs.exitQuest(true);
				return "30305-02.htm";
			}
			else
				return "30305-03.htm";
		}
		else
		{
			if (qs.getQuestItemsCount(NIGHTMARE_CRYSTAL) != 0)
			{
				// should we set cond to 0?
				qs.exitQuest(false);
				qs.rewardItems(PcInventory.ADENA_ID, 102680);
				qs.addExpAndSp(38607, 4018);
				talker.sendPacket(SND_FINISH);
				return "30305-06.htm";
			}
			else
				return "30305-05.htm";
		}
	}

	public static void main(String[] args)
	{
		new DangerousAllure(170, DANGEROUS_ALLURE, "Dangerous Allure");
	}
}
