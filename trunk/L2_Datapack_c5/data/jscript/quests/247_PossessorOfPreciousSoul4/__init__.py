# Maked by Mr. Have fun! Version 0.2
print "importing quests: 247: Possessor of a Precious Soul 4"
import sys
from net.sf.l2j.gameserver.model.quest import State
from net.sf.l2j.gameserver.model.quest import QuestState
from net.sf.l2j.gameserver.model.quest.jython import QuestJython as JQuest

CARADINE_LETTER2_ID = 7679
NOBLESSE_ID = 7694

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onEvent (self,event,st) :
    htmltext = event
    if event=="1" :
        htmltext = "start.htm"
        st.set("cond","1")
        st.setState(STARTED)
        st.playSound("ItemSound.quest_accept")
    if event=="2" :
        htmltext = "start.htm"
        st.player.teleToLocation(143283,44055,-3049)
        st.set("cond","2")
    return htmltext


 def onTalk (Self,npc,st):

   npcId = npc.getNpcId()
   htmltext = "<html><head><body>I have nothing to say you</body></html>"
   id = st.getState()
   if id == CREATED :
     st.setState(STARTING)
     st.set("cond","0")
     st.set("onlyone","0")
     st.set("id","0")
   if npcId==8740 and int(st.get("cond"))==0 and int(st.get("onlyone"))==0 : 
     if st.getQuestItemsCount(NOBLESSE_ID)==1 :
       htmltext = "<html><head><body>CHEATER!!!!!!!!!!!!!!!</body></html>"
     elif st.getQuestItemsCount(CARADINE_LETTER2_ID)==1 :
       if st.getPlayer().getSubLevel() >= 75 :
         htmltext = "8740-1.htm"
         st.takeItems(CARADINE_LETTER2_ID,1)
       else:
         htmltext = "8740-10.htm"
         st.exitQuest(1)
     else:
         htmltext = "8740-10.htm"
         st.exitQuest(1)
   elif npcId == 8740 and int(st.get("cond"))==0 and int(st.get("onlyone"))==1 :
      htmltext = "<html><head><body>This quest have already been completed.</body></html>"
   elif npcId==8740 and int(st.get("cond"))==1 and st.player.isSubClassActive() :
        htmltext = "8740-2.htm"
   elif npcId==8745 and int(st.get("cond"))==2 and st.player.isSubClassActive() :
        htmltext = "8745-1.htm"
        st.set("cond","0")
        st.player.setNoble(1)
        st.giveItems(NOBLESSE_ID,1)
        st.set("onlyone","1")
        st.setState(COMPLETED) 
        st.playSound("ItemSound.quest_finish")
   return htmltext

QUEST       = Quest(247,"247_PossessorOfPreciousSoul4","Possessor Of Precious Soul 4")
CREATED     = State('Start', QUEST)
STARTING     = State('Starting', QUEST)
STARTED     = State('Started', QUEST)
COMPLETED   = State('Completed', QUEST)


QUEST.setInitialState(CREATED)
QUEST.addStartNpc(8740)

STARTING.addTalkId(8740)

STARTED.addTalkId(8740)
STARTED.addTalkId(8745)

STARTED.addQuestDrop(8745,NOBLESSE_ID,1)