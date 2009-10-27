import sys
from com.l2jfree.gameserver.model.quest import State
from com.l2jfree.gameserver.model.quest import QuestState
from com.l2jfree.gameserver.model.quest.jython import QuestJython as JQuest
from com.l2jfree.tools.random import Rnd
from com.l2jfree.gameserver.datatables				import ItemTable
from com.l2jfree.gameserver.network.serverpackets import CreatureSay

rewards = [9682]
QUEST_RATE = 5

def dropItem(npc,player,itemId,count):
	ditem = ItemTable.getInstance().createItem("Loot", itemId, count, player)
	ditem.dropMe(npc, npc.getX(), npc.getY(), npc.getZ()); 

def autochat(npc,text):
	if npc: npc.broadcastPacket(CreatureSay(npc.getObjectId(),0,npc.getName(),text))
	return

class Celtus(JQuest):
	def __init__(self,id,name,descr):
		JQuest.__init__(self,id,name,descr)
	
	def onSpawn(self, npc):
		npc.setQuestDropable(False)

	def onKill (self,npc,player,isPet):
		item = player.getInventory().getItemByItemId(9672)
		if item:
			dropid = Rnd.get(len(rewards))
			dropItem(npc,player,rewards[dropid],QUEST_RATE,player)
		return
		
QUEST = Celtus(-1, "Celtus", "ai")
QUEST.addKillId(22353)
QUEST.addSpawnId(22353)