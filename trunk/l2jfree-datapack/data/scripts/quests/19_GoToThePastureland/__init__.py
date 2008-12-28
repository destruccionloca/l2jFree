# Made by disKret
import sys
from com.l2jfree import Config
from com.l2jfree.gameserver.model.quest import State
from com.l2jfree.gameserver.model.quest import QuestState
from com.l2jfree.gameserver.model.quest.jython import QuestJython as JQuest

qn = "19_GoToThePastureland"

#QUEST LEVEL
QLVL = 63

#NPC
VLADIMIR = 31302
TUNATUN  = 31537

#ITEMS
BEAST_MEAT = 7547

#REWARDS
ADENA_ID     = 57
ADENA_REWARD = 50000
EXP          = 136766
SP           = 12688


class Quest (JQuest) :

 def __init__(self,id,name,descr):
     JQuest.__init__(self,id,name,descr)
     self.questItemIds = [BEAST_MEAT]

 def onEvent (self,event,st) :
   htmltext = event
   if event == "31302-1.htm" :
     st.giveItems(BEAST_MEAT,1)
     st.set("cond","1")
     st.setState(State.STARTED)
     st.playSound("ItemSound.quest_accept")
   if event == "31537-1.htm" :
     st.takeItems(BEAST_MEAT,1)
     st.rewardItems(ADENA_ID,ADENA_REWARD)
     st.addExpAndSp(EXP,SP)
     st.unset("cond")
     st.exitQuest(False)
     st.playSound("ItemSound.quest_finish")
   return htmltext

 def onTalk (self,npc,player):
   htmltext = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>"
   st = player.getQuestState(qn)
   if not st : return htmltext

   npcId = npc.getNpcId()
   id = st.getState()
   cond = st.getInt("cond")
   if npcId == VLADIMIR :
     if cond == 0 :
       if id == State.COMPLETED :
         htmltext = "<html><body>This quest has already been completed.</body></html>"
       elif player.getLevel() >= QLVL :
         htmltext = "31302-0.htm"
       else:
         htmltext = "<html><body>Quest for characters level 63 or above.</body></html>"
         st.exitQuest(1)
     else :
       htmltext = "31302-2.htm"
   elif id == State.STARTED :
       htmltext = "31537-0.htm"
   return htmltext

QUEST       = Quest(19,qn,"Go To The Pastureland")

QUEST.addStartNpc(VLADIMIR)

QUEST.addTalkId(VLADIMIR)
QUEST.addTalkId(TUNATUN)
