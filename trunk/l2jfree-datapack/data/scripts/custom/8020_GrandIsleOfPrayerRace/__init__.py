# By Psychokiller1888
import sys
from com.l2jfree.gameserver.ai                 import CtrlIntention
from com.l2jfree.gameserver.model.quest        import State
from com.l2jfree.gameserver.model.quest        import QuestState
from com.l2jfree.gameserver.model.quest.jython import QuestJython as JQuest
from com.l2jfree.tools.random                  import Rnd

qn = "8020_GrandIsleOfPrayerRace"

RIGNOS = 32349
GUARD  = 18367
TICKET = 10013
KEY    = 9694

def getPrison(player):
	prison = 0
	if (player.getX() <= 161078 and player.getX() >= 160006) and (player.getY() <= 185406 and player.getY() >= 184332):
		prison = 1
	elif (player.getX() <= 135462 and player.getX() >= 134398) and (player.getY() <= 172610 and player.getY() >= 171551):
		prison = 2
	elif (player.getX() <= 147120 and player.getX() >= 146054) and (player.getY() <= 152140 and player.getY() >= 151068):
		prison = 3
	elif (player.getX() <= 156135 and player.getX() >= 155072) and (player.getY() <= 160684 and player.getY() >= 159627):
		prison = 4
	return prison

class Quest (JQuest) :

	def __init__(self,id,name,descr):
		JQuest.__init__(self,id,name,descr)
		self.questItemIds = [TICKET]
		raceState = self.loadGlobalQuestVar("raceState")
		if raceState == "" or raceState == "1":
			self.saveGlobalQuestVar("raceState","0")

	def onAdvEvent(self,event,npc,player):
		st = player.getQuestState(qn)
		if not st: st = self.newQuestState(player)
		cond = st.getInt("cond")
		if event == "startRace":
			raceState = self.loadGlobalQuestVar("raceState")
			if int(raceState) == 0:
				st.set("cond","1")
				st.setState(State.STARTED)
				self.saveGlobalQuestVar("raceState","1")
				self.startQuestTimer("endRace", 1800000, None, player)
				st.playSound("ItemSound.quest_accept")
				htmltext = "32349-1.htm"
			else:
				htmltext = "32349-7.htm"
		if event == "endRace":
			st.exitQuest(1)
			self.saveGlobalQuestVar("raceState","0")
			htmltext = "32349-3.htm"
		if event == "cancelState":
			st.exitQuest(1)
			htmltext = "32349-9.htm"
		return htmltext

	def onFirstTalk (self,npc,player):
		npcId = npc.getNpcId()
		st = player.getQuestState(qn)
		if not st: st = self.newQuestState(player)
		state = st.getState()
		cond = st.getInt("cond")
		if npcId == RIGNOS:
			if state != State.STARTED:
				htmltext = "32349.htm"
			elif cond != 5 and state == State.STARTED:
				htmltext = "32349-8.htm"
			elif cond == 5 and state == State.STARTED:
				if st.getQuestItemsCount(TICKET) >= 4:
					if st.getQuestTimer("endRace"):
						st.getQuestTimer("endRace").cancel()
					self.saveGlobalQuestVar("raceState","0")
					st.takeItems(TICKET,-1)
					st.giveItems(KEY,4)
					st.playSound("ItemSound.quest_finish")
					st.exitQuest(1)
					htmltext = "32349-4.htm"
				else:
					htmltext = "32349-5.htm"
					st.exitQuest(1)
		return htmltext

	def onSpawn(self, npc):
		npc.setKillable(False)
	
	def onAttack(self,npc,player,damage,isPet,skill):
		st = player.getQuestState(qn)
		if not st: return
		npcId = npc.getNpcId()
		if npcId == GUARD:
			prison = getPrison(player)
			cond = st.getInt("cond")
			if cond == 1 and prison == 1:
				if Rnd.get(100) <= 3:
					st.giveItems(TICKET,1)
					npc.setKillable(True)
					npc.reduceCurrentHp(9999999,npc)
					st.set("cond","2")
					st.playSound("ItemSound.quest_itemget")
					htmltext = "32349-2.htm"
					return htmltext
			elif cond == 2 and prison == 2:
				if Rnd.get(100) <= 3:
					st.giveItems(TICKET,1)
					npc.setKillable(True)
					npc.reduceCurrentHp(9999999,npc)
					st.set("cond","3")
					st.playSound("ItemSound.quest_itemget")
					htmltext = "32349-2.htm"
					return htmltext
			elif cond == 3 and prison == 3:
				if Rnd.get(100) <= 3:
					st.giveItems(TICKET,1)
					npc.setKillable(True)
					npc.reduceCurrentHp(9999999,npc)
					st.set("cond","4")
					st.playSound("ItemSound.quest_itemget")
					htmltext = "32349-2.htm"
					return htmltext
			elif cond == 4 and prison == 4:
				if Rnd.get(100) <= 3:
					st.giveItems(TICKET,1)
					npc.setKillable(True)
					npc.reduceCurrentHp(9999999,npc)
					st.set("cond","5")
					st.playSound("ItemSound.quest_itemget")
					htmltext = "32349-6.htm"
					return htmltext
     
QUEST = Quest(8020,qn,"custom")

QUEST.addStartNpc(RIGNOS)
QUEST.addTalkId(RIGNOS)
QUEST.addFirstTalkId(RIGNOS)
QUEST.addAttackId(GUARD)
QUEST.addSpawnId(GUARD)