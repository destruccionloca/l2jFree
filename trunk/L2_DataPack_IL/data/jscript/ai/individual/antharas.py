import sys
from net.sf.l2j.gameserver.model.quest import State
from net.sf.l2j.gameserver.model.quest import QuestState
from net.sf.l2j.gameserver.model.quest.jython import QuestJython as JQuest
from net.sf.l2j.gameserver.instancemanager import BossActionTaskManager

PORTAL_STONE    = 3865
HEART           = 13001

# Boss: Antharas
class antharas(JQuest):

  def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

  def onTalk (self,npc,player):
    st = player.getQuestState("antharas")
    npcId = npc.getNpcId()
    if npcId == HEART:
      if BossActionTaskManager.getInstance().CanIntoAntharasLair():
        if st.getQuestItemsCount(PORTAL_STONE) >= 1:
          st.takeItems(PORTAL_STONE,1)
          BossActionTaskManager.getInstance().SetAntharasSpawnTask()
          BossActionTaskManager.getInstance().AddPlayerToAntharasLair(st.player)
          st.player.teleToLocation(173826,115333,-7708)
          st.exitQuest(1)
          return
        else:
          st.exitQuest(1)
          return '<html><body>Heart of Muscai:<br><br>You do not have the proper stones needed for teleport.<br>It is for the teleport where does 1 stone to you need.<br></body></html>'
      else:       
        st.exitQuest(1)
        return '<html><body>Heart of Muscai:<br><br>Antharas has already awoke!<br>You are not possible to enter into Lair of Antharas.<br></body></html>'

# Quest class and state definition
QUEST       = antharas(-1, "antharas", "ai")
CREATED     = State('Start', QUEST)

# Quest initialization
QUEST.setInitialState(CREATED)
# Quest NPC starter initialization
QUEST.addStartNpc(HEART)
QUEST.addTalkId(HEART)

print "AI: individuals: Antharas...loaded!"
