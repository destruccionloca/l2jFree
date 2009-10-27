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
    if player.isTransformed() and player.getTranformationId()==101:
        item = player.getInventory().getItemByItemId(9674);
        if item:
            if item.getCount<5:
                return "<html><body>Bernarde:<br>Not enough Darion's Badge!</body></html>"
            else:
                player.destroyItemByItemId("Quest", 9674, 5, player, True)
                item = player.getInventory().addItem("Quest", 9673, 1, player, None)
                iu = InventoryUpdate()
                iu.addItem(item)
                player.sendPacket(iu);
                sm = SystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2)
                sm.addItemName(item)
                sm.addNumber(1)
                player.sendPacket(sm)
                return
        else:
            return "<html><body>Bernarde:<br>Not enough Darion's Badge!</body></html>"
    else :
        return "<html><body>Bernarde:<br>You are not trustworthy enough!</body></html>"
    return

# Quest class and state definition
QUEST = falk(-1, "bernarde", "ai")
QUEST.addStartNpc(32300)
QUEST.addTalkId(32300)
