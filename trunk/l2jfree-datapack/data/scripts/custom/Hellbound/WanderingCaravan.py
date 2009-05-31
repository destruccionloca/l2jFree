# By Psychokiller1888

import sys
from com.l2jfree.gameserver.model.quest           import State
from com.l2jfree.gameserver.model.quest           import QuestState
from com.l2jfree.gameserver.model.quest.jython    import QuestJython as JQuest
from com.l2jfree.gameserver.model.itemcontainer   import PcInventory
from com.l2jfree.gameserver.model                 import L2ItemInstance
from com.l2jfree.gameserver.network.serverpackets import InventoryUpdate
from com.l2jfree.gameserver.network.serverpackets import SystemMessage
from com.l2jfree.gameserver.network               import SystemMessageId

WANDERING_CARAVAN = 22339
BASIC_CERTIFICATE = 9850
STANDARD_CERTIFICATE = 9851
MARK_BETRAYAL = 9676

class WanderingCaravan(JQuest):
	def __init__(self,id,name,descr):
		JQuest.__init__(self,id,name,descr)

	def onKill (self,npc,player,isPet):
		npcId = npc.getNpcId()
		if npcId == WANDERING_CARAVAN:
			bcertificate = player.getInventory().getItemByItemId(BASIC_CERTIFICATE)
			scertificate = player.getInventory().getItemByItemId(STANDARD_CERTIFICATE)
			if bcertificate and not scertificate:
				item = player.getInventory().addItem("Quest", MARK_BETRAYAL, 1, player, None)
				iu = InventoryUpdate()
				iu.addItem(item)
				player.sendPacket(iu);
				sm = SystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2)
				sm.addItemName(item)
				sm.addNumber(1)
				player.sendPacket(sm)
		return
	
QUEST = WanderingCaravan(-1, "WanderingCaravan", "custom")
QUEST.addKillId(22339)