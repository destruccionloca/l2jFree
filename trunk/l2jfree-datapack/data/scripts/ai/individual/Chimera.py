import sys
from com.l2jfree.gameserver.model.quest import State
from com.l2jfree.gameserver.model.quest import QuestState
from com.l2jfree.gameserver.model.quest.jython import QuestJython as JQuest
from com.l2jfree.tools.random import Rnd
from com.l2jfree.gameserver.datatables				import ItemTable

reward = 9681
QUEST_RATE = 5

def dropItem(player,npc,itemId,count):
	ditem = ItemTable.getInstance().createItem("Loot", itemId, count, player)
	ditem.dropMe(player, npc.getX(), npc.getY(), npc.getZ()); 

class Chimera(JQuest):
	def __init__(self,id,name,descr):
		JQuest.__init__(self,id,name,descr)
	
	def onSpawn(self, npc):
		npc.setQuestDropable(False)

	def onKill (self,npc,player,isPet):
		item = player.getInventory().getItemByItemId(9672)
		if item:
			dropItem(player,npc,reward,QUEST_RATE,player)
		return
	
QUEST = Chimera(-1, "Chimera", "ai")
QUEST.addKillId(22349)
QUEST.addKillId(22350)
QUEST.addKillId(22351)
QUEST.addKillId(22352)

QUEST.addSpawnId(22349)
QUEST.addSpawnId(22350)
QUEST.addSpawnId(22351)
QUEST.addSpawnId(22352)
