import sys
from net.sf.l2j.gameserver.model.quest import State
from net.sf.l2j.gameserver.model.quest import QuestState
from net.sf.l2j.gameserver.model.quest.jython import QuestJython as JQuest
from net.sf.l2j.gameserver.instancemanager import BossActionTaskManager

qn = "31385_meetValakas" 

# Main Quest Code
class Quest (JQuest):

  def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

  def onEvent (self,event,st):
    return

  def onTalk (self,npc,player): 
    st = player.getQuestState(qn)       
    npcId = npc.getNpcId()
    if npcId == 31385 :
      if st.getInt("ok"):
        if BossActionTaskManager.getInstance().CanIntoValakasLair():
            BossActionTaskManager.getInstance().SetValakasSpawnTask()
            BossActionTaskManager.getInstance().AddPlayerToValakasLair(st.player)
            st.player.teleToLocation(203940,-111840,66)
            st.exitQuest(1)
            return
      else:
        st.exitQuest(1)
        return "Conditions are not right to enter to Lair of Valakas."
    elif npcId == 31540 :
      if st.getQuestItemsCount(7267) > 0 :
        st.takeItems(7267,1)
        st.getPlayer().teleToLocation(183831,-115457,-3296)
        st.set("ok","1")
      else :
        return '<html><head><body>-</body></html>'
    return

# Quest class and state definition
QUEST       = Quest(31385,"31385_meetValakas","custom")
CREATED     = State('Start',QUEST)
COMPLETED   = State('Completed',   QUEST)

# Quest initialization
QUEST.setInitialState(CREATED)
# Quest NPC starter initialization
QUEST.addStartNpc(31540)
QUEST.addStartNpc(31385)
QUEST.addTalkId(31540)
QUEST.addTalkId(31385)

print "importing custom data: 31385_meetValakas"
