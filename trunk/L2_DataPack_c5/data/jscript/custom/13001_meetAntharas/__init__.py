import sys

from net.sf.l2j.gameserver.model.actor.instance import  L2PcInstance
from net.sf.l2j.gameserver.model.quest          import  State
from net.sf.l2j.gameserver.model.quest          import  QuestState
from net.sf.l2j.gameserver.model.quest.jython   import  QuestJython as JQuest
from net.sf.l2j.gameserver.instancemanager      import  BossActionTaskManager

qn = "13001_meetAntharas"
PORTAL_STONE    = 3865
HEART           = 13001
class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onTalk (self,npc,player):
     st = player.getQuestState(qn) 
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
                return "1.htm"
        else:       
            st.exitQuest(1)
            return "2.htm"

QUEST       = Quest(13001,"13001_meetAntharas","custom")
CREATED     = State('Start',QUEST)

QUEST.setInitialState(CREATED)

QUEST.addTalkId(HEART)
CREATED.addTalkId(HEART)

print "importing custom data: 13001_meetAntharas"
