# version 0.1
# by Fulminus
# L2J_JP EDIT SANDMAN

import sys
from net.sf.l2j.gameserver.model.quest import State
from net.sf.l2j.gameserver.model.quest import QuestState
from net.sf.l2j.gameserver.model.quest.jython import QuestJython as JQuest
from net.sf.l2j.gameserver.instancemanager import BaiumManager

# Boss: Baium
class baium (JQuest):

  def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

  def onTalk (self,npc,player):
    st = player.getQuestState("baium")
    if not st : return "<html><body>You are either not carrying out your quest or don't meet the criteria.</body></html>"
    npcId = npc.getNpcId()
    if npcId == 29025 :
      if st.getInt("ok"):
        if not npc.isBusy():
           npc.onBypassFeedback(player,"wake_baium")
           npc.setBusy(True)
           npc.setBusyMessage("Attending another player's request")
      else:
        st.exitQuest(1)
        return "Conditions are not right to wake up Baium"
    elif npcId == 31862 :
      if st.getQuestItemsCount(4295) : # bloody fabric
        st.takeItems(4295,1)
        player.teleToLocation(113100,14500,10077)
        BaiumManager.getInstance().addPlayerToLair(player)
        st.set("ok","1")
      else :
        return '<html><body>Angelic Vortex:<br>You do not have enough items</body></html>'
    return

  def onKill(self,npc,player,isPet):
    st = player.getQuestState("baium")
    BaiumManager.getInstance().setCubeSpawn()
    if not st: return
    st.exitQuest(1)

# Quest class and state definition
QUEST       = baium(-1, "baium", "ai")
CREATED     = State('Start', QUEST)

# Quest initialization
QUEST.setInitialState(CREATED)
# Quest NPC starter initialization
QUEST.addStartNpc(29025)
QUEST.addStartNpc(31862)
QUEST.addTalkId(29025)
QUEST.addTalkId(31862)
QUEST.addKillId(29020)