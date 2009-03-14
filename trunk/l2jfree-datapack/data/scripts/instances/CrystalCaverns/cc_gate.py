# By Evil33t

import sys
from com.l2jfree.gameserver.model.quest import State
from com.l2jfree.gameserver.model.quest import QuestState
from com.l2jfree.gameserver.model.quest.jython import QuestJython as JQuest
from com.l2jfree.gameserver.instancemanager import InstanceManager
from com.l2jfree.gameserver.model.entity import Instance
from com.l2jfree.gameserver.datatables import ItemTable
from com.l2jfree.tools.random import Rnd

GK1 = 22275
GK2 = 22277

def dropItem(npc,itemId,count):
	ditem = ItemTable.getInstance().createItem("Loot", itemId, count, None)
	ditem.dropMe(npc, npc.getX(), npc.getY(), npc.getZ()); 

class cc_gate(JQuest):
	def __init__(self,id,name,descr):
		JQuest.__init__(self,id,name,descr)

	def onKill (self,npc,player,isPet):
		instance = InstanceManager.getInstance().getInstance(npc.getInstanceId())
		npcId = npc.getNpcId()
		if npc.getNpcId() == GK1:
			dropItem(npc,9698,1)
		if npc.getNpcId() == GK2:
			dropItem(npc,9699,1)
		for nnpc in instance.getNpcs():
			if nnpc:
				if nnpc.getNpcId() == GK1 and nnpc.getNpcId() != npc.getNpcId():
					nnpc.decayMe()
				if nnpc.getNpcId() == GK2 and nnpc.getNpcId() != npc.getNpcId():
					nnpc.decayMe()
		return

QUEST = cc_gate(-1, "cc_gate", "ai")
QUEST.addKillId(GK1)
QUEST.addKillId(GK2)