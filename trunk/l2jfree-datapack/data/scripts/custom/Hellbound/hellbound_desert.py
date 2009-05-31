# author evill33t
import sys
from com.l2jfree.gameserver.ai import CtrlIntention
from com.l2jfree.gameserver.model.quest.jython import QuestJython as JQuest
from com.l2jfree.gameserver.network.serverpackets import MagicSkillUse

QUEST_RATE = 500

class hellbound_desert(JQuest):
	def __init__(self,id,name,descr):
		#npcid,trustpoints+
		self.BelethClan ={
			22320:10,
			22321:10,
			22324:1,
			22325:1,
			22327:3,
			22328:3,
			22329:3,
			22339:3,
			22422:3,
			22330:3,
			22343:3,
			18463:10,
			18464:10,
			22346:5,
			33247:3,
			22344:3,
			22345:3,
			22326:100,
			25536:100
			}
		#npcid,trustpoints-
		self.NativeClan ={
			22322:10,
			22323:10,
			32357:10,
			32358:10,
			32299:10
			}
		JQuest.__init__(self,id,name,descr)

	def onKill (self,npc,player,isPet):
		npcId = npc.getNpcId()
		if self.BelethClan.has_key(npcId) :
			player.increaseTrustLevel(self.BelethClan[npcId]*QUEST_RATE)
		if self.NativeClan.has_key(npcId) :
			player.decreaseTrustLevel(self.NativeClan[npcId]*QUEST_RATE)
		return 

QUEST = hellbound_desert(-1,"hellbound_desert","ai")
for i in QUEST.BelethClan.keys():
	QUEST.addKillId(i)
for i in QUEST.NativeClan.keys():
	QUEST.addKillId(i)