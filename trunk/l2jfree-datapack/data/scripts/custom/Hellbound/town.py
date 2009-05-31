from com.l2jfree.gameserver.instancemanager        import InstanceManager
from com.l2jfree.gameserver.model.entity           import Instance
from com.l2jfree.gameserver.model.actor                  import L2Summon
from com.l2jfree.gameserver.model.quest            import State
from com.l2jfree.gameserver.model.quest            import QuestState
from com.l2jfree.gameserver.model.quest.jython     import QuestJython as JQuest
from com.l2jfree.gameserver.network.serverpackets  import CreatureSay
from com.l2jfree.gameserver.network.serverpackets  import MagicSkillUse
from com.l2jfree.gameserver.network.serverpackets  import SystemMessage
from com.l2jfree.tools.random                      import Rnd
from com.l2jfree.gameserver.model.itemcontainer import PcInventory
from com.l2jfree.gameserver.model import L2ItemInstance
from com.l2jfree.gameserver.network.serverpackets import InventoryUpdate
from com.l2jfree.gameserver.network.serverpackets import SystemMessage
from com.l2jfree.gameserver.network import SystemMessageId


qn = "HellboundTown"

debug = False

#NPCs
KANAF       = 32346

#Mobs
AMARAKIS = 22449

LOCS = [
 	 [22449,17409,250359,-1927]
	,[22449,14083,253171,-2014]
	,[22449,14222,254925,-2010]
	,[22449,15138,253814,-2012]
	,[22449,16122,250321,-1921]
	,[22449,14083,251808,-1939]
	,[22449,14088,250279,-1943]
	,[22449,13977,250893,-1940]
	,[22449,16671,254022,-2034]
	,[22449,15664,255596,-2015]
	,[22449,16321,256206,-2018]
	,[22449,19888,256210,-2091]
	,[22449,18953,251220,-2010]
	,[22449,21243,250300,-1980]
	,[22449,21409,251193,-2009]
]

		  
class PyObject:
	pass

def openDoor(doorId,instanceId):
	for door in InstanceManager.getInstance().getInstance(instanceId).getDoors():
		if door.getDoorId() == doorId:
			door.openMe()

def checkCondition(player):
	if not player.getLevel() >= 78:
		player.sendPacket(SystemMessage.sendString("You must be level 75 to enter this town."))
		return False
	party = player.getParty()
	if not party:
		player.sendPacket(SystemMessage.sendString("To attempt to enter the town by yourself would be suicide! You must enter with the rest of your party members."))	
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

def enterInstance(self,player,template,teleto):
	instanceId = 0
	if not checkCondition(player):
		return 0
	party = player.getParty()
	#check for exising instances of party members
	for partyMember in party.getPartyMembers().toArray():
		if partyMember.getInstanceId()!=0:
			instanceId = partyMember.getInstanceId()
			if debug: print "HellboundTown: found party member in instance:"+str(instanceId)
	#exising instance
	if instanceId != 0:
		foundworld = False
		for worldid in self.world_ids:
			if worldid == instanceId:
				foundworld = True
		if not foundworld:
			player.sendPacket(SystemMessage.sendString("Your Party Members are in another Instance."))	
			return 0
		teleto.instanceId = instanceId
		teleportplayer(self,player,teleto)
		return instanceId
	#new instance
	else:
		instanceId = InstanceManager.getInstance().createDynamicInstance(template)	
		if not self.worlds.has_key(instanceId):
			world = PyObject()			
			world.rewarded=[]
			world.instanceId = instanceId
			self.worlds[instanceId]=world
			self.world_ids.append(instanceId)
			print "HellboundTown: started " + template + " Instance: " +str(instanceId) + " created by player: " + str(player.getName()) 
		# teleports player
		teleto.instanceId = instanceId
		teleportplayer(self,player,teleto)
		return instanceId
	return instanceId

def exitInstance(player,tele):
	player.setInstanceId(0)
	player.teleToLocation(tele.x, tele.y, tele.z)
	pet = player.getPet()
	if pet != None :
		pet.setInstanceId(0)
		pet.teleToLocation(tele.x, tele.y, tele.z)
						
class HellboundTown(JQuest):
	def __init__(self,id,name,descr):
		JQuest.__init__(self,id,name,descr)
		self.worlds = {}
		self.world_ids = []

	def onTalk (self,npc,player):
		npcId = npc.getNpcId()
		if npcId == KANAF :
			tele = PyObject()
			tele.x = 13881
			tele.y = 255491
			tele.z = -2025
			instanceId = enterInstance(self, player, "HBTown.xml", tele)
			if not instanceId:
				return
			if instanceId == 0:
				return
		if self.worlds.has_key(npc.getInstanceId()):
			world = self.worlds[npc.getInstanceId()]
		return
   
	def onKill(self,npc,player,isPet):
		npcId = npc.getNpcId()
		if self.worlds.has_key(npc.getInstanceId()):
			world = self.worlds[npc.getInstanceId()]
			if npc.getNpcId()==AMARAKIS:
				pass
				#spawn keymaster
		return
		
	def onAttack(self,npc,player,damage,isPet, skill):
		npcId = npc.getNpcId()
		if self.worlds.has_key(npc.getInstanceId()):
			world = self.worlds[npc.getInstanceId()]
		return
	
	def onFirstTalk (self,npc,player):
		npcId = npc.getNpcId()
		if self.worlds.has_key(npc.getInstanceId()):
			world = self.worlds[npc.getInstanceId()]
		return ""

QUEST = HellboundTown(-1, qn, "HBT")
QUEST.addStartNpc(KANAF)
QUEST.addTalkId(KANAF)
QUEST.addKillId(AMARAKIS)
