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

#NPC
BATHIS    = 30332 #Gludio
LUCAS     = 30071 #Dion
GOSTA     = 30916 #Heine
MOUEN     = 30196 #Oren
VISHOTSKY = 31981 #Schuttgart
MATHIAS   = 31340 #Rune

MAX_DISTANCE = 500

GUARDS = [BATHIS,LUCAS,GOSTA,MOUEN,VISHOTSKY,MATHIAS]
BOSSES = [18554,18555,18558,18559,18562,18564,18566,18568,18571,18573,18577]

#KAMALOKA = [FILE, Reuse Delay, Boss, LvlMin, LvlMax, X, Y, Z]
KAMALOKA = {
23: ["Kamaloka-23.xml",86400,18554,18,28,-57109,-219871,-8117],
26: ["Kamaloka-26.xml",86400,18555,21,31,-55556,-206144,-8117],
33: ["Kamaloka-33.xml",86400,18558,28,38,-55492,-206143,-8117],
36: ["Kamaloka-36.xml",86400,18559,31,41,-41257,-213143,-8117],
43: ["Kamaloka-43.xml",86400,18562,38,48,-49802,-206141,-8117],
46: ["Kamaloka-46.xml",86400,18564,41,51,-41184,-213144,-8117],
53: ["Kamaloka-53.xml",86400,18566,48,58,-41201,-219859,-8117],
56: ["Kamaloka-56.xml",86400,18568,51,61,-57102,-206143,-8117],
63: ["Kamaloka-63.xml",86400,18571,58,68,-57116,-219857,-8117],
66: ["Kamaloka-66.xml",86400,18573,61,71,-41228,-219860,-8117],
73: ["Kamaloka-73.xml",86400,18577,68,78,-55823,-212935,-8071]
}


class PyObject:
	pass

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

def checkCondition(player,reuse,minLevel,maxLevel):
	currentTime = System.currentTimeMillis()/1000
	party = player.getParty()
	if not party:
		player.sendPacket(SystemMessage.sendString("You must be in a party with at least one other person."))
		return False
	# Check size of the party, max 6 for entering Kamaloka
	if party and party.getMemberCount() > 6:
		player.sendPacket(SystemMessage.sendString("Instance for max 6 players in party."))
		return False
	for partyMember in party.getPartyMembers().toArray():
		if partyMember.getLevel() < minLevel or partyMember.getLevel() > maxLevel:
			player.sendPacket(SystemMessage.sendString("You and your party mates must be between level " + str(minLevel) + " and level " + str(maxLevel) + " to enter this Kamaloka."))
			partyMember.sendPacket(SystemMessage.sendString("You must be between level " + str(minLevel) + " and level " + str(maxLevel) + " to enter this Kamaloka."))
			return False
		st = partyMember.getQuestState(qn)
		if st:
			LastEntry = st.getInt("LastEntry")
			if currentTime < LastEntry + reuse:
				player.sendPacket(SystemMessage.sendString("One of your party member still has to wait for re-access Kamaloka"))
				partyMember.sendPacket(SystemMessage.sendString("You have to wait at least 24 hours between each time you enter Kamaloka"))
				return False
	return True

def teleportplayer(self,player,teleto):
	player.setInstanceId(teleto.instanceId)
	player.teleToLocation(teleto.x, teleto.y, teleto.z)
	pet = player.getPet()
	if pet != None :
		pet.setInstanceId(teleto.instanceId)
		pet.teleToLocation(teleto.x, teleto.y, teleto.z)
	return

def enterInstance(self,player,teleto,KamaInfo):
	instanceId = 0
	template = KamaInfo[0]
	reuse = KamaInfo[1]
	minLevel = KamaInfo[3]
	maxLevel = KamaInfo[4]
	if checkDistance(player):
		player.sendPacket(SystemMessage.sendString("Please regroup your party before joining Kamaloka."))
		return 0
	if not checkCondition(player,reuse,minLevel,maxLevel):
		return 0
	party = player.getParty()
	# Check for existing instances of party members
	for partyMember in party.getPartyMembers().toArray():
		if partyMember.getInstanceId() != 0:
			instanceId = partyMember.getInstanceId()
	# New instance
	instanceId = InstanceManager.getInstance().createDynamicInstance(template)
	if not self.worlds.has_key(instanceId):
		world = PyObject()
		world.instanceId = instanceId
		self.worlds[instanceId]=world
		self.world_ids.append(instanceId)
		print "Kamaloka: started " + template + " Instance: " +str(instanceId) + " created by player: " + str(player.getName())
	# Teleport players
	teleto.instanceId = instanceId
	for partyMember in party.getPartyMembers().toArray():
		partyMember.stopAllEffects()
		partyMember.clearSouls()
		partyMember.clearCharges()
		teleportplayer(self,partyMember,teleto)
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
		level = event[3:]
		KamaInfo = KAMALOKA[int(level)]
		tele = PyObject()
		tele.x = KamaInfo[5]
		tele.y = KamaInfo[6]
		tele.z = KamaInfo[7]
		instanceId = enterInstance(self,player,tele,KamaInfo)
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
		playerList = InstanceManager.getInstance().getInstance(player.getInstanceId()).getPlayers()
		for member in playerList.toArray():
			member = L2World.getInstance().findPlayer(member)
			saveEntry(self,member)
			member.sendPacket(SystemMessage.sendString("You will be moved out of Kamaloka in 5 minutes"))
		instance = InstanceManager.getInstance().getInstance(npc.getInstanceId())
		instance.setDuration(300000)
		return

QUEST = Kamaloka(-1, qn, "instances")

for npc in GUARDS :
	QUEST.addStartNpc(npc)
	QUEST.addTalkId(npc)

for bosses in BOSSES :
	QUEST.addKillId(bosses)