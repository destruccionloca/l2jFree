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
from com.l2jfree.gameserver.network.serverpackets import NpcSay
from com.l2jfree.gameserver.model                  import L2World
from com.l2jfree.tools.random import Rnd
from com.l2jfree.gameserver.datatables import ItemTable

import time

qn = "HellboundTown"
QUEST_RATE = 1

debug = False

#NPCs
KANAF       = 32346
PRISONER	= 32358
KEYHOLE		= 32343

#Mobs
AMASKARI	= 22449
KEYMASTER 	= 22361

AMASKARI_TEXT = [
'Slimebags death awaits you!','Little humans, what a suprise','First i kill you then the native you tried to free','Lord Beleth will not be pleased','Not you again'
]

LOCS = [
 	 [17409,250359,-1927]
	,[14083,253171,-2014]
	,[14222,254925,-2010]
	,[15138,253814,-2012]
	,[16122,250321,-1921]
]

		  
class PyObject:
	pass

def dropItem(npc,itemId,count,player):
	ditem = ItemTable.getInstance().createItem("Loot", itemId, count, player)
	ditem.dropMe(npc, npc.getX(), npc.getY(), npc.getZ()); 	
	
def openDoor(doorId,instanceId):
	for door in InstanceManager.getInstance().getInstance(instanceId).getDoors():
		if door.getDoorId() == doorId:
			door.openMe()

def checkCondition(player):
	if not player.getLevel() >= 78:
		player.sendPacket(SystemMessage.sendString("You must be level 78 or higher to enter this town."))
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
		return instanceId
	party = player.getParty()
	if party != None :
		channel = party.getCommandChannel()
		if channel != None :
			members = channel.getMembers().toArray()
		else:
			members = party.getPartyMembers().toArray()
	else:
		members = []
	#check for exising instances of party members or channel members
	for member in members :
		if member.getInstanceId()!= 0 :
			instanceId = member.getInstanceId()
	#exising instance	#exising instance
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

	def onAdvEvent (self,event,npc,player) :
		if event == "keySpawn1" or event == "keySpawn2":
			self.startQuestTimer("keySpawn2", 300000, None, None)
			loc = LOCS[Rnd.get(len(LOCS))]
			self.amaskari.teleToLocation(loc[0],loc[1],loc[2])
			if event == "keySpawn1"
				self.startQuestTimer("keySpawn2", 300000, None, None)
			else:
				self.startQuestTimer("keySpawn1", 300000, None, None)
			return
		if event == "freeprisoner":
			objId = npc.getObjectId()
			npc.broadcastPacket(NpcSay(objId, 0, npc.getNpcId(), "Thank you, i hope Amaskari wont notice!"))
			time.sleep(5)
			#npc.decayMe()
			player.increaseTrustLevel(500*QUEST_RATE)
			chance = Rnd.get(100)
			if chance <= 10:
				self.amaskari.teleToLocation(player.getX(),player.getY(),player.getZ())
				self.amaskari.setTarget(player)
				objId = self.amaskari.getObjectId()
				self.amaskari.broadcastPacket(NpcSay(objId, 0, self.amaskari.getNpcId(), AMASKARI_TEXT[Rnd.get(len(AMASKARI_TEXT))]))
			return
		return

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
			newNpc = self.addSpawn(AMASKARI,19496,253125,-2030,0,False,0,False, instanceId)
			self.amaskari = newNpc
			loc = LOCS[Rnd.get(len(LOCS))]
			newNpc = self.addSpawn(KEYMASTER,loc[0],loc[1],loc[2],0,False,0,False, instanceId)
			self.startQuestTimer("keySpawn1", 300000, None, None)
		if self.worlds.has_key(npc.getInstanceId()):
			world = self.worlds[npc.getInstanceId()]
		return
   
	def onKill(self,npc,player,isPet):
		npcId = npc.getNpcId()
		objId = npc.getObjectId()
		if npcId == KEYMASTER:
			chance = Rnd.get(100)
			if chance <= 75:
				npc.broadcastPacket(NpcSay(objId, 0, npc.getNpcId(), "Oh no my key............."))
				dropItem(npc,9714,1,player)
			else:
				npc.broadcastPacket(NpcSay(objId, 0, npc.getNpcId(), "You will never get my key!"))
		if self.worlds.has_key(npc.getInstanceId()):
			world = self.worlds[npc.getInstanceId()]
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
QUEST.addStartNpc(PRISONER)
QUEST.addTalkId(KANAF)
QUEST.addTalkId(PRISONER)
QUEST.addKillId(AMASKARI)
QUEST.addKillId(KEYMASTER)
