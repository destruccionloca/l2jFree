# Kamaloka Script by Psychokiller1888
# Psycho / L2jFree

import sys
from java.lang                                     import System
from com.l2jfree.gameserver.instancemanager        import InstanceManager
from com.l2jfree.gameserver.model                  import L2World
from com.l2jfree.gameserver.model.actor            import L2Character
from com.l2jfree.gameserver.model.actor            import L2Summon
from com.l2jfree.gameserver.model.entity           import Instance
from com.l2jfree.gameserver.model.quest            import State
from com.l2jfree.gameserver.model.quest            import QuestState
from com.l2jfree.gameserver.model.quest.jython     import QuestJython as JQuest
from com.l2jfree.gameserver.network.serverpackets  import SystemMessage

qn = "Kamaloka"

debug = False

#NPC
BATHIS    = 30332 #Gludio
LUCAS     = 30071 #Dion
GOSTA     = 30916 #Heine
MOUEN     = 30196 #Oren
VISHOTSKY = 31981 #Schuttgart
MATHIAS   = 31340 #Rune

MAX_DISTANCE = 500

GUARDS = [BATHIS,LUCAS,GOSTA,MOUEN,VISHOTSKY,MATHIAS]
BOSSES = [18554,18555,18558,18559,18562,18564,18566,18568,18571,18573,18577,29129,29132,29135,29138,29141,29144,29147]

MOBS = [22485,22486,22487,22488,22489,22490,22491,22492,22493,22494,22495,22496,22497,22498,22499,22500,22501,22502,22503,22504,22505,25616,25617,25618,25619,25620,25621,25622]

#KAMALOKA = LEVEL: [FILE, Reuse Delay, Boss, LvlMin, LvlMax, MaxPlayer, X, Y, Z, Usable]
KAMALOKA = {
23: ["Kamaloka-23.xml",86400,18554,18,28,6,-57109,-219871,-8117, False],
26: ["Kamaloka-26.xml",86400,18555,21,31,6,-55556,-206144,-8117, False],
29: ["Kamaloka-29.xml",86400,18555,24,34,9,-10864,-174897,-10947, False],
33: ["Kamaloka-33.xml",86400,18558,28,38,6,-55492,-206143,-8117, False],
36: ["Kamaloka-36.xml",86400,18559,31,41,6,-41257,-213143,-8117, False],
39: ["Kamaloka-39.xml",86400,18555,34,44,9,-10864,-174897,-10947, False],
43: ["Kamaloka-43.xml",86400,18562,38,48,6,-49802,-206141,-8117, False],
46: ["Kamaloka-46.xml",86400,18564,41,51,6,-41184,-213144,-8117, False],
49: ["Kamaloka-49.xml",86400,18555,44,54,9,-10864,-174897,-10947, False],
53: ["Kamaloka-53.xml",86400,18566,48,58,6,-41201,-219859,-8117, False],
56: ["Kamaloka-56.xml",86400,18568,51,61,6,-57102,-206143,-8117, False],
59: ["Kamaloka-59.xml",86400,18555,54,64,9,-10864,-174897,-10947, False],
63: ["Kamaloka-63.xml",86400,18571,58,68,6,-57116,-219857,-8117, False],
66: ["Kamaloka-66.xml",86400,18573,61,71,6,-41228,-219860,-8117, False],
69: ["Kamaloka-69.xml",86400,18555,64,74,9,-10864,-174897,-10947, False],
73: ["Kamaloka-73.xml",86400,18577,68,78,6,-55823,-212935,-8071, False],
78: ["Kamaloka-78.xml",86400,18555,73,85,9,-10864,-174897,-10947, False],
81: ["Kamaloka-81.xml",86400,18555,76,86,9,-10864,-174897,-10947, False]
}

#LABYRINTHMOBS = LEVEL: [[ROOM1],[ROOM2],[ROOM3],[ROOM4]]
LABYRINTHMOBS = {
29: [[22485],[22485,22486],[22497,25616],[29129]],
39: [[22488],[22488,22489],[22490,25617],[29132]],
49: [[22491],[22491,22492],[22493,25618],[29135]],
59: [[22494],[22494,22495],[22496,25619],[29138]],
69: [[22497],[22497,22498],[22499,25620],[29141]],
78: [[22500],[22500,22501],[22502,25621],[29144]],
81: [[22503],[22503,22504],[22505,25622],[29147]]
}

class PyObject:
	pass

def startFirstRoom(self,world):
	world.status = 1
	world.room1 = PyObject()
	world.room1.npclist = {}
	MOBLIST = LABYRINTHMOBS[world.level][0]
	newNpc = self.addSpawn(MOBLIST[0], -12238, -174914, -10951, 64774, False, 0, False, world.instanceId)
	world.room1.npclist[newNpc] = False
	newNpc = self.addSpawn(MOBLIST[0], -12202, -174796, -10951, 13295, False, 0, False, world.instanceId)
	world.room1.npclist[newNpc] = False
	newNpc = self.addSpawn(MOBLIST[0], -12167, -175120, -10951, 44797, False, 0, False, world.instanceId)
	world.room1.npclist[newNpc] = False
	newNpc = self.addSpawn(MOBLIST[0], -12551, -174894, -10951, 27219, False, 0, False, world.instanceId)
	world.room1.npclist[newNpc] = False
	newNpc = self.addSpawn(MOBLIST[0], -12348, -174733, -10951, 33299, False, 0, False, world.instanceId)
	world.room1.npclist[newNpc] = False
	newNpc = self.addSpawn(MOBLIST[0], -12140, -174532, -10951, 40687, False, 0, False, world.instanceId)
	world.room1.npclist[newNpc] = False

def startSecondRoom(self,world):
	world.status = 2
	world.room2 = PyObject()
	world.room2.npclist = {}
	MOBLIST = LABYRINTHMOBS[world.level][1]
	newNpc = self.addSpawn(MOBLIST[0], -14701, -174759, -10688, 31617, False, 0, False, world.instanceId)
	world.room2.npclist[newNpc] = False
	newNpc = self.addSpawn(MOBLIST[0], -14566, -175385, -10688, 45304, False, 0, False, world.instanceId)
	world.room2.npclist[newNpc] = False
	newNpc = self.addSpawn(MOBLIST[0], -14491, -175191, -10688, 48804, False, 0, False, world.instanceId)
	world.room2.npclist[newNpc] = False
	newNpc = self.addSpawn(MOBLIST[0], -14289, -174810, -10688, 56914, False, 0, False, world.instanceId)
	world.room2.npclist[newNpc] = False
	newNpc = self.addSpawn(MOBLIST[0], -14414, -174673, -10688, 22012, False, 0, False, world.instanceId)
	world.room2.npclist[newNpc] = False
	newNpc = self.addSpawn(MOBLIST[0], -14728, -175077, -10688, 44753, False, 0, False, world.instanceId)
	world.room2.npclist[newNpc] = False
	newNpc = self.addSpawn(MOBLIST[1], -14241, -174927, -10688, 9555, False, 0, False, world.instanceId)
	world.room2.npclist[newNpc] = False
	newNpc = self.addSpawn(MOBLIST[1], -14416, -175155, -10688, 62980, False, 0, False, world.instanceId)
	world.room2.npclist[newNpc] = False
	newNpc = self.addSpawn(MOBLIST[1], -14691, -174506, -10688, 24714, False, 0, False, world.instanceId)
	world.room2.npclist[newNpc] = False
	newNpc = self.addSpawn(MOBLIST[1], -14494, -174855, -10688, 54512, False, 0, False, world.instanceId)
	world.room2.npclist[newNpc] = False
	newNpc = self.addSpawn(MOBLIST[1], -14621, -174851, -10688, 56617, False, 0, False, world.instanceId)
	world.room2.npclist[newNpc] = False
	newNpc = self.addSpawn(MOBLIST[1], -14955, -174871, -10688, 27492, False, 0, False, world.instanceId)
	world.room2.npclist[newNpc] = False

def startThirdRoom(self,world):
	world.status = 3
	world.room3 = PyObject()
	world.room3.npclist = {}
	MOBLIST = LABYRINTHMOBS[world.level][2]
	newNpc = self.addSpawn(MOBLIST[0], -16956, -175034, -10425, 2555, False, 0, False, world.instanceId)
	world.room3.npclist[newNpc] = False
	newNpc = self.addSpawn(MOBLIST[0], -16748, -175008, -10425, 1294, False, 0, False, world.instanceId)
	world.room3.npclist[newNpc] = False
	newNpc = self.addSpawn(MOBLIST[0], -16712, -174800, -10425, 14596, False, 0, False, world.instanceId)
	world.room3.npclist[newNpc] = False
	newNpc = self.addSpawn(MOBLIST[0], -16872, -174747, -10425, 29431, False, 0, False, world.instanceId)
	world.room3.npclist[newNpc] = False
	newNpc = self.addSpawn(MOBLIST[0], -16988, -174837, -10425, 55547, False, 0, False, world.instanceId)
	world.room3.npclist[newNpc] = False
	newNpc = self.addSpawn(MOBLIST[0], -16976, -174914, -10425, 63797, False, 0, False, world.instanceId)
	world.room3.npclist[newNpc] = False
	newNpc = self.addSpawn(MOBLIST[0], -16757, -174979, -10425, 2598, False, 0, False, world.instanceId)
	world.room3.npclist[newNpc] = False
	newNpc = self.addSpawn(MOBLIST[0], -17014, -174730, -10425, 1838, False, 0, False, world.instanceId)
	world.room3.npclist[newNpc] = False
	newNpc = self.addSpawn(MOBLIST[1], -16861, -174899, -10425, 64650, False, 0, False, world.instanceId)
	world.room3.npclist[newNpc] = False

def startFourthRoom(self,world):
	world.status = 4
	world.room4 = PyObject()
	world.room4.npclist = {}
	MOBLIST = LABYRINTHMOBS[world.level][3]
	newNpc = self.addSpawn(MOBLIST[0], -20640, -174904, -9981, 912, False, 0, False, world.instanceId)
	world.room4.npclist[newNpc] = False

def checkKillProgress(npc,room):
	cont = True
	if room.npclist.has_key(npc):
		room.npclist[npc] = True
	for npc in room.npclist.keys():
		if room.npclist[npc] == False:
			cont = False
	return cont

def saveEntry(self,member) :
	currentTime = System.currentTimeMillis()/1000
	st = member.getQuestState(qn)
	if not st :
		st = self.newQuestState(member)
	st.set("LastEntry",str(currentTime))
	return

def checkDistance(player) :
	isTooFar = False
	party = player.getParty()
	if party:
		for partyMember in party.getPartyMembers().toArray():
			if abs(partyMember.getX() - player.getX()) > MAX_DISTANCE :
				isTooFar = True
				break;
			if abs(partyMember.getY() - player.getY()) > MAX_DISTANCE :
				isTooFar = True
				break;
			if abs(partyMember.getZ() - player.getZ()) > MAX_DISTANCE :
				isTooFar = True
				break;
	return isTooFar

def checkCondition(player,KamaInfo):
	currentTime = System.currentTimeMillis()/1000
	party = player.getParty()
	if not party:
		player.sendPacket(SystemMessage.sendString("You are not currently in a party, so you cannot enter."))
		return False
	# Check size of the party, max 6 for entering Kamaloka Hall of Abyss, 9 for Labyrinth
	if party and party.getMemberCount() > KamaInfo[5]:
		player.sendPacket(SystemMessage.sendString("Instance for max "+str(KamaInfo[5])+" players in party."))
		return False
	for partyMember in party.getPartyMembers().toArray():
		if partyMember.getLevel() < KamaInfo[3] or partyMember.getLevel() > KamaInfo[4]:
			player.sendPacket(SystemMessage.sendString("You and your party mates must be between level " + str(KamaInfo[3]) + " and level " + str(KamaInfo[4]) + " to enter this Kamaloka."))
			partyMember.sendPacket(SystemMessage.sendString("You must be between level " + str(KamaInfo[3]) + " and level " + str(KamaInfo[4]) + " to enter this Kamaloka."))
			return False
		st = partyMember.getQuestState(qn)
		if st:
			LastEntry = st.getInt("LastEntry")
			if currentTime < LastEntry + KamaInfo[1]:
				player.sendPacket(SystemMessage.sendString(player.getName()+" may not re-enter yet."))
				partyMember.sendPacket(SystemMessage.sendString(player.getName()+" may not re-enter yet."))
				return False
	return True

def teleportplayer(self,player,KamaInfo,instanceId):
	player.setInstanceId(instanceId)
	player.teleToLocation(KamaInfo[6], KamaInfo[7], KamaInfo[8])
	pet = player.getPet()
	if pet != None :
		pet.setInstanceId(instanceId)
		pet.teleToLocation(KamaInfo[6], KamaInfo[7], KamaInfo[8])
	return

def enterInstance(self,player,KamaInfo,level):
	instanceId = 0
	template = KamaInfo[0]
	reuse = KamaInfo[1]
	if checkDistance(player):
		player.sendPacket(SystemMessage.sendString("Please regroup your party before joining Kamaloka."))
		return 0
	if not checkCondition(player,KamaInfo):
		return 0
	party = player.getParty()
	# Check for existing instances of party members
	for partyMember in party.getPartyMembers().toArray():
		if partyMember.getInstanceId() != 0:
			player.sendPacket(SystemMessage.sendString("One of your party member is already in an instance"))
			return 0
	# New instance
	instanceId = InstanceManager.getInstance().createDynamicInstance(template)
	if not self.worlds.has_key(instanceId):
		world = PyObject()
		world.instanceId = instanceId
		self.worlds[instanceId]=world
		self.world_ids.append(instanceId)
		if level in [29,39,49,59,69,78,81]:
			world.level = level
			startFirstRoom(self,world)
		if debug:
			print "Kamaloka: started " + template + " Instance: " +str(instanceId) + " created by player: " + str(player.getName())
	# Teleport players
	for partyMember in party.getPartyMembers().toArray():
		partyMember.stopAllEffects()
		partyMember.clearSouls()
		partyMember.clearCharges()
		teleportplayer(self,partyMember,KamaInfo,instanceId)
	return instanceId

class Kamaloka(JQuest):
	def __init__(self,id,name,descr):
		JQuest.__init__(self,id,name,descr)
		self.worlds = {}
		self.world_ids = []
		
	def onAdvEvent (self,event,npc,player):
		st = player.getQuestState(qn)
		if not st:
			st = self.newQuestState(player)
		level = int(event[3:])
		KamaInfo = KAMALOKA[level]
		if not KamaInfo[9]:
			player.sendPacket(SystemMessage.sendString("This Kamaloka has been disabled by an Admin"))
			return
		if player.getInstanceId() != 0:
			player.sendPacket(SystemMessage.sendString("You are already in an instance..."))
			return
		instanceId = enterInstance(self,player,KamaInfo,level)
		if not instanceId:
			return
		if instanceId == 0:
			return
		return
		
	def onTalk (self,npc,player):
		st = player.getQuestState(qn)
		npcId = npc.getNpcId()
		if not st:
			st.setState(State.STARTED)
		if npcId == BATHIS:
			htmltext = "start-bathis.htm"
		if npcId == LUCAS:
			htmltext = "start-lucas.htm"
		if npcId == GOSTA:
			htmltext = "start-gosta.htm"
		if npcId == MOUEN:
			htmltext = "start-mouen.htm"
		if npcId == VISHOTSKY:
			htmltext = "start-vishotsky.htm"
		if npcId == MATHIAS:
			htmltext = "start-mathias.htm"
		return htmltext

	def onKill(self,npc,player,isPet):
		npcId = npc.getNpcId()
		if npcId in BOSSES:
			playerList = InstanceManager.getInstance().getInstance(player.getInstanceId()).getPlayers()
			for member in playerList.toArray():
				member = L2World.getInstance().findPlayer(member)
				saveEntry(self,member)
				member.sendPacket(SystemMessage.sendString("You will be moved out of Kamaloka in 5 minutes"))
			if player.getInstanceId() != 0:
				instance = InstanceManager.getInstance().getInstance(npc.getInstanceId())
				if instance != None:
					instance.setDuration(300000)
		else:
			if self.worlds.has_key(npc.getInstanceId()):
				world = self.worlds[npc.getInstanceId()]
				if world.status == 1:
					if checkKillProgress(npc,world.room1):
						startSecondRoom(self,world)
				elif world.status == 2:
					if checkKillProgress(npc,world.room2):
						startThirdRoom(self,world)
				elif world.status == 3:
					if checkKillProgress(npc,world.room3):
						startFourthRoom(self,world)
		return

QUEST = Kamaloka(-1, qn, "instances")

for npc in GUARDS :
	QUEST.addStartNpc(npc)
	QUEST.addTalkId(npc)

for boss in BOSSES :
	QUEST.addKillId(boss)

for mob in MOBS :
	QUEST.addKillId(mob)