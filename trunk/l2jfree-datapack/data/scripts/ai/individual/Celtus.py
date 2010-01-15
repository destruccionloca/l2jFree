# Author: Psycho(killer1888) / L2jFree

import sys
from com.l2jfree.gameserver.model.quest           import State
from com.l2jfree.gameserver.model.quest           import QuestState
from com.l2jfree.gameserver.model.quest.jython    import QuestJython as JQuest
from com.l2jfree.gameserver.network.serverpackets import InventoryUpdate
from com.l2jfree.gameserver.network.serverpackets import SystemMessage
from com.l2jfree.gameserver.network               import SystemMessageId
from com.l2jfree.tools.random                     import Rnd

class Celtus(JQuest):
	def __init__(self,id,name,descr):
		JQuest.__init__(self,id,name,descr)

	def onKill (self,npc,player,isPet):
		if npc.getQuestDropable() == True and Rnd.get <= 20:
			item = player.getInventory().addItem("Celtus", 9682, 1, player, None)
			iu = InventoryUpdate()
			iu.addItem(item)
			player.sendPacket(iu);
			sm = SystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2)
			sm.addItemName(item)
			sm.addNumber(1)
			player.sendPacket(sm)
		return

	def onSkillSee(self,npc,caster,skill,targets,isPet):
		skillId = skill.getId()
		if skillId != 2359:
			return
		if not npc in targets:
			return
		if npc.getStatus().getCurrentHp() <=  npc.getMaxHp() / 10:
			npc.setMagicBottled(True)
		else:
			npc.setQuestDropable(False)
			caster.sendPacket(SystemMessage(SystemMessageId.NOTHING_HAPPENED))
			return
		return

QUEST = Celtus(-1, "Celtus", "ai")
QUEST.addKillId(22353)
QUEST.addSpawnId(22353)
QUEST.addSkillSeeId(22353)
