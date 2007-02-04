# Maked by BiTi! v0.2
print "importing quests: 637: Through the Gate Once More"
import sys
from net.sf.l2j.gameserver.model.quest import State
from net.sf.l2j.gameserver.model.quest import QuestState
from net.sf.l2j.gameserver.model.quest.jython import QuestJython as JQuest

#Npc
FLAURON = 32010

#Monsters to hunt
BONEANIMATOR = 21565
SKULLANIMATOR = 21566
BONESLAYER = 21567

#Items
NECROHEART = 8066
MARK = 8067
VISITORSMARK = 8064

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onEvent (self,event,st) :
    htmltext = event
    if htmltext == "32010-04.htm" :
        st.set("cond","1")
        st.setState(STARTED)
        st.takeItems(VISITORSMARK,1)
        st.playSound("ItemSound.quest_accept")
    elif event == "32010-01.htm" :
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
   if npcId == FLAURON and st.getInt("cond")==0 :
       if st.getPlayer().getLevel()>72 and st.getQuestItemsCount(VISITORSMARK)==1:
           htmltext = "32010-02.htm"
       else:
           htmltext = "32010-01.htm"
           st.exitQuest(1)
   elif npcId == FLAURON and st.getInt("cond")==2 and st.getQuestItemsCount(NECROHEART)==10:
       htmltext = "32010-05.htm"
       st.takeItems(NECROHEART,10)
       st.giveItems(MARK,1)
       st.setState(COMPLETED)
       st.playSound("ItemSound.quest_finish")
   return htmltext

 def onKill (self,npc,st):

   npcId = npc.getNpcId()
   if npcId == 21565 :
        st.set("id","0")
        if st.getInt("cond")==1:
          if st.getRandom(10)<4 and st.getQuestItemsCount(NECROHEART)<10 :
            st.giveItems(NECROHEART,1)
            if st.getQuestItemsCount(NECROHEART) == 10 :
              st.playSound("ItemSound.quest_middle")
              st.set("cond","2")
            else:
              st.playSound("ItemSound.quest_itemget")
   elif npcId == 21566 :
        st.set("id","0")
        if st.getInt("cond")==1:
          if st.getRandom(10)<4 and st.getQuestItemsCount(NECROHEART)<10 :
            st.giveItems(NECROHEART,1)
            if st.getQuestItemsCount(NECROHEART) == 10 :
              st.playSound("ItemSound.quest_middle")
              st.set("cond","2")
            else:
              st.playSound("ItemSound.quest_itemget")
   elif npcId == 21567 :
        st.set("id","0")
        if st.getInt("cond")==1:
          if st.getRandom(10)<4 and st.getQuestItemsCount(NECROHEART)<10 :
            st.giveItems(NECROHEART,1)
            if st.getQuestItemsCount(NECROHEART) == 10 :
              st.playSound("ItemSound.quest_middle")
              st.set("cond","2")
            else:
              st.playSound("ItemSound.quest_itemget")
   return

QUEST       = Quest(637,"637_ThroughOnceMore","Through the Gate Once More")
CREATED     = State('Start', QUEST)
STARTING     = State('Starting', QUEST)
STARTED     = State('Started', QUEST)
COMPLETED   = State('Completed', QUEST)

QUEST.setInitialState(CREATED)
QUEST.addStartNpc(FLAURON)

STARTING.addTalkId(FLAURON)

STARTED.addTalkId(FLAURON)

STARTED.addKillId(21565)
STARTED.addKillId(21566)
STARTED.addKillId(21567)

STARTED.addQuestDrop(21565,NECROHEART,1)
STARTED.addQuestDrop(21566,NECROHEART,1)
STARTED.addQuestDrop(21567,NECROHEART,1)