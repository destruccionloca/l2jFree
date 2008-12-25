# Made by disKret
import sys
from com.l2jfree import Config
from com.l2jfree.gameserver.model.quest import State
from com.l2jfree.gameserver.model.quest import QuestState
from com.l2jfree.gameserver.model.quest.jython import QuestJython as JQuest

qn = "15_SweetWhisper"

#QUEST LEVEL
QLVL = 60

#NPC
VLADIMIR      = 31302
HIERARCH      = 31517
M_NECROMANCER = 31518

#REWARDS
EXP = 350531
SP  = 28204

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onEvent (self,event,st) :
   htmltext = event
   cond = st.getInt("cond")
   if event == "31302-1.htm" :
     st.set("cond","1")
     st.setState(State.STARTED)
     st.playSound("ItemSound.quest_accept")
   if event == "31518-1.htm" :
     if cond == 1 :
       st.set("cond","2")
   if event == "31517-1.htm" :
     if cond == 2 :
       st.addExpAndSp(EXP,SP)
       st.set("cond","0")
       st.playSound("ItemSound.quest_finish")
       st.exitQuest(False)
   return htmltext

 def onTalk (self,npc,player):
   htmltext = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>"
   st = player.getQuestState(qn)
   if not st : return htmltext

   npcId = npc.getNpcId()
   cond = st.getInt("cond")
   id = st.getState()
   if id == State.CREATED :
     st.set("cond","0")
   if npcId == VLADIMIR and st.getInt("cond") == 0 :
     if id == State.COMPLETED :
       htmltext = "<html><body>This quest has already been completed.</body></html>"
       return htmltext
     elif player.getLevel() >= QLVL :
       htmltext = "31302-0.htm"
       return htmltext
     else:
       htmltext = "31302-0a.htm"
       st.exitQuest(1)
   if npcId == VLADIMIR and cond == 1 :
       htmltext = "31302-1a.htm"
   if id == State.STARTED :
       if npcId == M_NECROMANCER and cond == 1 :
         htmltext = "31518-0.htm"
       elif npcId == M_NECROMANCER and cond == 2 :
         htmltext = "31518-1a.htm"
       elif npcId == HIERARCH and cond == 2 :
         htmltext = "31517-0.htm"
   return htmltext

QUEST       = Quest(15,qn,"Sweet Whisper")


QUEST.addStartNpc(VLADIMIR)

QUEST.addTalkId(VLADIMIR)
QUEST.addTalkId(HIERARCH)
QUEST.addTalkId(M_NECROMANCER)
