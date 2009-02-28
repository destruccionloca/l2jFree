# Script by Psychokiller1888
import sys
from com.l2jfree.gameserver.instancemanager.grandbosses import BaylorManager
from com.l2jfree.gameserver.model.quest                 import State
from com.l2jfree.gameserver.model.quest                 import QuestState
from com.l2jfree.gameserver.model.quest.jython          import QuestJython as JQuest
from com.l2jfree.gameserver.instancemanager             import InstanceManager
from com.l2jfree.gameserver.model.entity                import Instance

ORACLE_GUIDE = 32279

class PyObject:
	pass


def exitInstance(player,teleto):
	player.setInstanceId(0)
	player.teleToLocation(teleto.x, teleto.y, teleto.z)
	pet = player.getPet()
	if pet != None :
		pet.setInstanceId(0)
		pet.teleToLocation(teleto.x, teleto.y, teleto.z)

def teleportplayer(player,teleto):
	player.teleToLocation(teleto.x, teleto.y, teleto.z)
	pet = player.getPet()
	if pet != None :
		pet.teleToLocation(teleto.x, teleto.y, teleto.z)
	return

class oracle5(JQuest):
	def __init__(self,id,name,descr):
		self.isSpawned = False
		JQuest.__init__(self,id,name,descr)

	def onAdvEvent (self,event,npc,player):
		if event == "out":
			tele = PyObject()
			tele.x = 149361
			tele.y = 172327
			tele.z = -945
			exitInstance(player,tele)
			return
		elif event == "meet":
			tele = PyObject()
			tele.x = 153586
			tele.y = 145934
			tele.z = -12589
			teleportplayer(player,tele)
			return
		return

	def onTalk (self,npc,player):
		npcId = npc.getNpcId()
		if npcId == ORACLE_GUIDE:
			BAYLOR_STATE = BaylorManager.getInstance().canIntoBaylorLair(player)
			if BAYLOR_STATE == 0 or BAYLOR_STATE == 4:
			    htmltext = "meetingOk.htm"
			else:
				htmltext = "teleportOut.htm"
		return htmltext





QUEST = oracle5(-1, "oracle5", "ai")
QUEST.addStartNpc(ORACLE_GUIDE)
QUEST.addTalkId(ORACLE_GUIDE)