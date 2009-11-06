# Author: Psycho(killer1888) / L2jFree
import sys
from com.l2jfree.gameserver.ai 						import CtrlIntention
from com.l2jfree 									import L2DatabaseFactory
from com.l2jfree.gameserver.network.serverpackets 	import NpcSay
from com.l2jfree.gameserver.model.quest 			import State
from com.l2jfree.gameserver.model.quest 			import QuestState
from com.l2jfree.gameserver.model.quest.jython 		import QuestJython as JQuest
from com.l2jfree.gameserver.model.actor.instance    import L2MonsterInstance

debug = False

def getHellbountPoints(self):
	con = L2DatabaseFactory.getInstance().getConnection(None)
	offline = con.prepareStatement("SELECT value FROM quest_global_data WHERE quest_name = 'Hellbound' AND var = 'HellboundPoints'")
	rs = offline.executeQuery()
	if rs :
		rs.next()
		try :
			actualPoints = rs.getInt("value")
			con.close()
		except :
			actualPoints = 0
	return int(actualPoints)

def setPoints(self,value):
	con = L2DatabaseFactory.getInstance().getConnection(None)
	offline = con.prepareStatement("UPDATE quest_global_data SET value = ? WHERE quest_name = 'Hellbound' AND var = 'HellboundPoints'")
	offline.setInt(1, value)
	try :
		offline.executeUpdate()
		offline.close()
		con.close()
	except :
		try : con.close()
		except : pass
	return

def cancelTimers(self,npc):
	if self.getQuestTimer("CheckIfSafe",npc,None):
		self.getQuestTimer("CheckIfSafe",npc,None).cancel()
	if self.getQuestTimer("CallKillers",npc,None):
		self.getQuestTimer("CallKillers",npc,None).cancel()
	return

class QuarrySlave(JQuest):
	def __init__(self,id,name,descr):
		JQuest.__init__(self,id,name,descr)

	def onAdvEvent (self,event,npc, player) :
		if event == "CheckIfSafe":
			if npc.isDead():
				cancelTimers(self,npc)
				points = getHellbountPoints(self)
				points -= 30
				if debug:
					print "Points: "+str(points)
				setPoints(self,points)
				return
			if (npc.getX() >= -5967 and npc.getX() <= -4163) and (npc.getY() >= 251137 and npc.getY() <= 251970) and (npc.getZ() >= -3400 and npc.getZ() <= -3100):
				points = getHellbountPoints(self)
				points += 30
				if debug:
					print "Points: "+str(points)
				setPoints(self,points)
				npc.broadcastPacket(NpcSay(npc.getObjectId(),0,npc.getNpcId(),"Thank you, you saved me! I'll remember you my whole life!"))
				npc.decayMe()
				cancelTimers(self,npc)
			else:
				if debug:
					print "Not on position. Currently: X: " +str(npc.getX())+ " Y: " +str(npc.getY())+ " Z: " +str(npc.getZ())
				self.startQuestTimer("CheckIfSafe",10000,npc,None)
		elif event == "CallKillers":
			if npc.isDead():
				cancelTimers(self,npc)
				points = getHellbountPoints(self)
				points -= 30
				if debug:
					print "Points: "+str(points)
				setPoints(self,points)
				return
			for object in npc.getKnownList().getKnownObjects().values():
				if object != None:
					if isinstance(object, L2MonsterInstance):
						objectId = object.getNpcId()
						if objectId in [22347,22344,22346]:
							object.setTarget(npc)
							object.addDamageHate(npc, 0, 999)
							object.setIsRunning(True)
							object.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK)
			self.startQuestTimer("CallKillers",1000,npc,None)
		return

	def onTalk (self,npc,player):
		npcId = npc.getNpcId()
		trust = getHellbountPoints(self)
		if npcId == 32299 and trust >= 1030000 and trust <= 1060000:
			npc.setTarget(player);
			npc.setRunning()
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, player)
			self.startQuestTimer("CheckIfSafe",10000,npc,None)
			self.startQuestTimer("CallKillers",1000,npc,None)
		return
	
QUEST = QuarrySlave(-1, "QuarrySlave", "custom")
QUEST.addStartNpc(32299)
QUEST.addTalkId(32299)
QUEST.addKillId(32299)
