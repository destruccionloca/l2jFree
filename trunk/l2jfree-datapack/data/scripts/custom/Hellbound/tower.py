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

qn = "HellboundBaseTower"
QUEST_RATE = 1

debug = False

#NPCs
KEYHOLE		= 32343

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
		player.sendPacket(SystemMessage.sendString("You must be level 78 or higher to enter the tower."))
		return False
	party = player.getParty()
	if not party:
		player.sendPacket(SystemMessage.sendString("To attempt to enter the tower by yourself would be suicide! You must enter with the rest of your party members."))	
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
		if member.getInstanceId()!= 0 and member.getInstanceId() != player.getInstanceId():
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
		item = player.getInventory().getItemByItemId(9714)
		if item:
			player.destroyItemByItemId("Quest", 9714, 1, player, True)
			instanceId = InstanceManager.getInstance().createDynamicInstance(template)	
			if not self.worlds.has_key(instanceId):
				world = PyObject()			
				world.rewarded=[]
				world.instanceId = instanceId
				self.worlds[instanceId]=world
				self.world_ids.append(instanceId)
				print "HellboundBaseTower: started " + template + " Instance: " +str(instanceId) + " created by player: " + str(player.getName()) 
			# teleports player
			teleto.instanceId = instanceId
			teleportplayer(self,player,teleto)
		else:
			player.sendPacket(SystemMessage.sendString("You need the key of the Keymaster to enter!."))	
		return instanceId
	return instanceId
	
def exitInstance(player,tele):
	player.setInstanceId(0)
	player.teleToLocation(tele.x, tele.y, tele.z)
	pet = player.getPet()
	if pet != None :
		pet.setInstanceId(0)
		pet.teleToLocation(tele.x, tele.y, tele.z)
						
class HellboundBaseTower(JQuest):
	def __init__(self,id,name,descr):
		JQuest.__init__(self,id,name,descr)
		self.worlds = {}
		self.world_ids = []

	def onAdvEvent (self,event,npc,player) :
		return

	def onTalk (self,npc,player):
		npcId = npc.getNpcId()
		if npcId == KEYHOLE :
			tele = PyObject()
			tele.x = 16255
			tele.y = 282445
			tele.z = -9704
			instanceId = enterInstance(self, player, "BaseTower.xml", tele)
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

QUEST = HellboundBaseTower(-1, qn, "HellboundBaseTower")
QUEST.addStartNpc(KEYHOLE)
QUEST.addTalkId(KEYHOLE)
