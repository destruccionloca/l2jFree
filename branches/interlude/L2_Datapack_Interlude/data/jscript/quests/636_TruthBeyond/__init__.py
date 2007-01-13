# Maked by Polo Have fun! Fixed by BiTi
# v0.3
print "importing quests: 636: The Truth Beyond the Gate"
import sys
from net.sf.l2j.gameserver.model.quest import State
from net.sf.l2j.gameserver.model.quest import QuestState
from net.sf.l2j.gameserver.model.quest.jython import QuestJython as JQuest

#Npc
ELIYAH = 31329
FLAURON = 32010

#Items
MARK = 8064

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onEvent (self,event,st) :
    htmltext = event
    if htmltext == "31329-04.htm" :
        st.set("cond","1")
        st.setState(STARTED)
        st.playSound("ItemSound.quest_accept")
    elif htmltext == "32010-02.htm" :
        st.playSound("ItemSound.quest_finish")
        st.giveItems(MARK,1)
        st.setState(COMPLETED)
    elif event == "31329-01.htm" :
        st.exitQuest(1)
    return htmltext

 def onTalk (Self,npc,st):
   npcId = npc.getNpcId()
   htmltext = "<html><head><body>I have nothing to say to you</body></html>"
   id = st.getState()
   if id == CREATED :
     st.setState(STARTING)
     st.set("cond","0")
     st.set("onlyone","0")
     st.set("id","0")
   if npcId == ELIYAH and st.getInt("cond")==0 :
       if st.getPlayer().getLevel()>72 :
           htmltext = "31329-02.htm"
       else:
           htmltext = "31329-01.htm"
           st.exitQuest(1)
   elif npcId == FLAURON and st.getInt("cond")==1 :
       htmltext = "32010-01.htm"
       st.set("cond","2")
   return htmltext



QUEST       = Quest(636,"636_TruthBeyond","The Truth Beyond the Gate")
CREATED     = State('Start', QUEST)
STARTING     = State('Starting', QUEST)
STARTED     = State('Started', QUEST)
COMPLETED   = State('Completed', QUEST)

QUEST.setInitialState(CREATED)
QUEST.addStartNpc(ELIYAH)

STARTING.addTalkId(ELIYAH)

STARTED.addTalkId(ELIYAH)
STARTED.addTalkId(FLAURON)