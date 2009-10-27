import sys
from com.l2jfree.gameserver.model.quest import State
from com.l2jfree.gameserver.model.quest import QuestState
from com.l2jfree.gameserver.model.quest.jython import QuestJython as JQuest
from com.l2jfree.gameserver.model.itemcontainer import Inventory
from com.l2jfree.gameserver.model import L2ItemInstance
from com.l2jfree.gameserver.network.serverpackets import InventoryUpdate
from com.l2jfree.gameserver.network.serverpackets import SystemMessage
from com.l2jfree.gameserver.network import SystemMessageId

class falk (JQuest):

  def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

  def onTalk (self,npc,player):
    if player.getTrustLevel()>=300000: 
        item = player.getInventory().getItemByItemId(9674);
        if item:
            if item.getCount<20:
                return "<html><body>Falk:<br>Not enough Darion's Badge!</body></html>"
            else:
                player.destroyItemByItemId("Quest", 9674, 20, player, True)
                item = player.getInventory().addItem("Quest", 9850, 1, player, None)
                iu = InventoryUpdate()
                iu.addItem(item)
                player.sendPacket(iu);
                sm = SystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2)
                sm.addItemName(item)
                sm.addNumber(1)
                player.sendPacket(sm)
                return
        else:
           return "<html><body>Falk:<br>Not enough Darion's Badge!</body></html>"
    else :
        return "<html><body>Falk:<br>You are not trustworthy enough!</body></html>"
    return

# Quest class and state definition
QUEST = falk(-1, "falk", "ai")
QUEST.addStartNpc(32297)
QUEST.addTalkId(32297)
