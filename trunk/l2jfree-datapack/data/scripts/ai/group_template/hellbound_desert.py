# author evill33t
import sys
from com.l2jfree.gameserver.ai import CtrlIntention
from com.l2jfree.gameserver.model.quest.jython import QuestJython as JQuest
from com.l2jfree.gameserver.network.serverpackets import MagicSkillUse

#NPC BELETH_CLAN
JUNIOR_WATCHMAN = 22320
JUNIOR_SUMMONER = 22321
BLIND_HUNTSMAN  = 22324
BLIND_WATCHMAN  = 22325
ARCANE_SCOUT    = 22327
ARCANE_GUARDIAN = 22328
ARCANE_WATCHMAN = 22329

#NPC NATIVES_CLAN
SUBJUGATED_NATIVE = 22322
CHARMED_NATIVE    = 22323
NATIVE_SLAVE      = 32357
NATIVE_PRISONER   = 32358

class hellbound_desert(JQuest):
	def __init__(self,id,name,descr):
		#npcid,trustpoints+
		self.BelethClan ={
			JUNIOR_WATCHMAN:10,
			JUNIOR_SUMMONER:10,
			BLIND_HUNTSMAN:1,
			BLIND_WATCHMAN:1,
			ARCANE_SCOUT:3,
			ARCANE_GUARDIAN:3,
			ARCANE_WATCHMAN:3
			}
		#npcid,trustpoints-
		self.NativeClan ={
			SUBJUGATED_NATIVE:10,
			CHARMED_NATIVE:10,
			NATIVE_SLAVE:10,
			NATIVE_PRISONER:10
			}
		JQuest.__init__(self,id,name,descr)

	def onKill (self,npc,player,isPet):
		npcId = npc.getNpcId()
		if self.BelethClan.has_key(npcId) :
			player.increaseTrustLevel(self.BelethClan[npcId])
		if self.NativeClan.has_key(npcId) :
			player.decreaseTrustLevel(self.NativeClan[npcId])
		return 

QUEST = hellbound_desert(-1,"hellbound_desert","ai")
for i in QUEST.BelethClan.keys():
	QUEST.addKillId(i)
for i in QUEST.NativeClan.keys():
	QUEST.addKillId(i)