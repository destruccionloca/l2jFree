import sys
from com.l2jfree.gameserver.model.quest import State
from com.l2jfree.gameserver.model.quest import QuestState
from com.l2jfree.gameserver.model.quest.jython import QuestJython as JQuest

class remnant(JQuest):
	def __init__(self,id,name,descr):
		JQuest.__init__(self,id,name,descr)

	def onKill (self,npc,player,isPet):
		return
		
	def onSpawn(self, npc):
		self.isSpawned = False
		npc.setKillable(False)
	
QUEST = remnant(-1, "remnant", "ai")
QUEST.addKillId(18463)
QUEST.addSpawnId(18463)
QUEST.addKillId(18464)
QUEST.addSpawnId(18464)