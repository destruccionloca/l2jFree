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

public final class BloodFiend extends QuestJython
{
	private static final String BLOOD_FIEND = "164_BloodFiend";

	// Quest NPCs
	private static final int CEL = 30149;

	// Quest items
	private static final int KIRUNAK_SKULL = 1044;

	// Quest monsters
	private static final int KIRUNAK = 27021;
	private static final String KIRUNAK_ATTACKED = "I shall taste your steaming blood!";
	private static final String KIRUNAK_KILLED = "Contract with Creamees is accomplished...";

	public BloodFiend(int questId, String name, String descr)
	{
		super(questId, name, descr);
		questItemIds = new int[] { KIRUNAK_SKULL };
		addStartNpc(CEL);
		addTalkId(CEL);
		addAttackId(KIRUNAK);
		addKillId(KIRUNAK);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState qs = player.getQuestState(BLOOD_FIEND);
		if (qs.isCompleted())
			return QUEST_DONE;
		else if ("1".equals(event))
		{
			qs.set(CONDITION, 1);
			qs.setState(State.STARTED);
			player.sendPacket(SND_ACCEPT);
			return "30149-04.htm";
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
			npc.broadcastPacket(new NpcSay(npc, KIRUNAK_ATTACKED));
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
		QuestState qs = quester.getQuestState(BLOOD_FIEND);
		if (qs == null || qs.getState() != State.STARTED || qs.getInt(CONDITION) != 1
				|| npc.getQuestAttackStatus() != ATTACK_SINGLE)
			return null;

		if (qs.getQuestItemsCount(KIRUNAK_SKULL) == 0)
		{
			npc.broadcastPacket(new NpcSay(npc, KIRUNAK_KILLED));
			qs.giveItems(KIRUNAK_SKULL, 1);
			quester.sendPacket(SND_MIDDLE);
			qs.set(CONDITION, 2);
		}

		return null;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance talker)
	{
		QuestState qs = talker.getQuestState(BLOOD_FIEND);
		if (qs == null)
			return NO_QUEST;
		else if (qs.isCompleted())
			return QUEST_DONE;

		int cond = qs.getInt(CONDITION);
		if (cond == 0)
		{
			// Can't risk more races to be added
			if (talker.getRace() == Race.Darkelf)// || talker.getRace() == Race.Kamael)
			{
				qs.exitQuest(true);
				return "30149-00.htm";
			}
			else if (talker.getLevel() < 21)
			{
				qs.exitQuest(true);
				return "30149-02.htm";
			}
			else
				return "30149-03.htm";
		}
		else
		{
			if (qs.getQuestItemsCount(KIRUNAK_SKULL) != 0)
			{
				// should we set cond to 0?
				qs.exitQuest(false);
				qs.rewardItems(PcInventory.ADENA_ID, 42130);
				qs.addExpAndSp(35637, 1854);
				talker.sendPacket(SND_FINISH);
				return "30149-06.htm";
			}
			else
				return "30149-05.htm";
		}
	}

	public static void main(String[] args)
	{
		new BloodFiend(164, BLOOD_FIEND, "Blood Fiend");
	}
}
