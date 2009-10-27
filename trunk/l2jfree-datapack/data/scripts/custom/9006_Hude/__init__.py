import sys
from com.l2jfree.gameserver.model.quest           import State
from com.l2jfree.gameserver.model.quest           import QuestState
from com.l2jfree.gameserver.model.quest.jython    import QuestJython as JQuest
from com.l2jfree.tools.random                     import Rnd
from com.l2jfree.gameserver.model.itemcontainer   import PcInventory
from com.l2jfree.gameserver.model                 import L2ItemInstance
from com.l2jfree.gameserver.network.serverpackets import InventoryUpdate
from com.l2jfree.gameserver.network.serverpackets import SystemMessage
from com.l2jfree.gameserver.network               import SystemMessageId

qn = "9006_Hude"

items = [9628,9629,9630]
badge = 9674

class Quest (JQuest) :

	def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)


	def onAdvEvent (self,event,npc,player) :
		if event == "advanced":
			item = player.getInventory().getItemByItemId(9851)
			if item and player.getTrustLevel()>=600000 and player.getTransformationId()==101:
				return "trade2.htm"
			return "no2.htm"
		if event == "advanced2":
			item = player.getInventory().getItemByItemId(9851)
			if item and player.getTrustLevel()>=1000000 and player.getTransformationId()==101:
				return "trade3.htm"
			return "no2.htm"
		if event == "advanced3":
			item = player.getInventory().getItemByItemId(9852)
			if item and player.getTrustLevel()>=1000000 and player.getTransformationId()==101:
				return "trade4.htm"
			return "no2.htm"
		if event == "tradeall":
			item = player.getInventory().getItemByItemId(badge)
			if item.getCount()>=10:
				for step in range(10,item.getCount(),10):
					player.destroyItemByItemId("Quest", badge, 10, player, True)
					i = Rnd.get(len(items))
					item = player.getInventory().addItem("Quest", items[i], 1, player, None)
					iu = InventoryUpdate()
					iu.addItem(item)
					player.sendPacket(iu);
					sm = SystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2)
					sm.addItemName(item)
					sm.addNumber(1)
					player.sendPacket(sm)
			return
		if event == "trade":
			item = player.getInventory().getItemByItemId(badge)
			if not item:
				player.sendPacket(SystemMessage.sendString("You must have 10 Darion Badges in your Inventory."))	
				return
			if item.getCount()<10:
				player.sendPacket(SystemMessage.sendString("You must have 10 Darion Badges in your Inventory."))	
				return
			player.destroyItemByItemId("Quest", badge, 10, player, True)
			i = Rnd.get(len(items))
			item = player.getInventory().addItem("Quest", items[i], 1, player, None)
			iu = InventoryUpdate()
			iu.addItem(item)
			player.sendPacket(iu);
			sm = SystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2)
			sm.addItemName(item)
			sm.addNumber(1)
			player.sendPacket(sm)
			return
	
	def onTalk (self,npc,player):
		item = player.getInventory().getItemByItemId(9850)
		if item and player.getTrustLevel()>=600000 and player.getTransformationId()==101:
			return "trade.htm"
		else:
			return "no.htm"
	
QUEST = Quest(9006,qn,"custom")

QUEST.addStartNpc(32298)
QUEST.addTalkId(32298)
