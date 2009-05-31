import sys
from com.l2jfree.gameserver.model.quest import State
from com.l2jfree.gameserver.model.quest import QuestState
from com.l2jfree.gameserver.model.quest.jython import QuestJython as JQuest
from com.l2jfree.tools.random import Rnd
from com.l2jfree.gameserver.datatables				import ItemTable
from com.l2jfree.gameserver.datatables import DoorTable

class Captain(JQuest):
	def __init__(self,id,name,descr):
		JQuest.__init__(self,id,name,descr)
	
	def onAdvEvent (self,event,npc,player):
		if event == "close_timer":
			_doorTable = DoorTable.getInstance()
			_doorTable.getDoor(20250001).closeMe()
			return
		
	def onKill (self,npc,player,isPet):
		_doorTable = DoorTable.getInstance()
		_doorTable.getDoor(20250001).openMe()
		self.startQuestTimer("close_timer",20000,npc,None)
		return
	
QUEST = Captain(-1, "Captain", "ai")
QUEST.addKillId(18466)
