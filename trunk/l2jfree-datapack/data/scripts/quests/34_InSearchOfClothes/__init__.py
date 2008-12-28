# Made by disKret
import sys
from com.l2jfree import Config
from com.l2jfree.tools.random import Rnd #Isn't this imported without reason? I don't see it in use =/
from com.l2jfree.gameserver.model.quest import State
from com.l2jfree.gameserver.model.quest import QuestState
from com.l2jfree.gameserver.model.quest.jython import QuestJython as JQuest

qn = "34_InSearchOfClothes"

#ITEM
SPINNERET  = 7528
SPIDERSILK = 1493
SUEDE      = 1866
THREAD     = 1868

#NEEDED
RALFORD_SPINNERET = 10
RADIA_SUEDE       = 3000
RADIA_THREAD      = 5000

#QUEST MONSTER
TRISALIM_SPIDER    = 20560
TRISALIM_TARANTULA = 20561

#NPC
RADIA   = 30088
VARAN   = 30294
RALFORD = 30165

#REWARD
MYSTERIOUS_CLOTH = 7076

class Quest (JQuest) :

 def __init__(self,id,name,descr):
     JQuest.__init__(self,id,name,descr)
     self.questItemIds = [SPINNERET,SPIDERSILK]

 def onEvent (self,event,st) :
   htmltext = event
   cond = st.getInt("cond")
   if event == "30088-1.htm" and cond == 0:
     st.set("cond","1")
     st.setState(State.STARTED)
     st.playSound("ItemSound.quest_accept")
   if event == "30294-1.htm" and cond == 1 :
     st.set("cond","2")
   if event == "30088-3.htm" and cond == 2:
     st.set("cond","3")
   if event == "30165-1.htm" and cond == 3:
     st.set("cond","4")
   if event == "30165-3.htm" and cond == 5:
     if st.getQuestItemsCount(SPINNERET) == RALFORD_SPINNERET :
       st.takeItems(SPINNERET,RALFORD_SPINNERET)
       st.giveItems(SPIDERSILK,1)
       st.set("cond","6")
     else :
       htmltext = "You don't have enough materials"
   if event == "30088-5.htm" and cond == 6 :
     if st.getQuestItemsCount(SUEDE) >= RADIA_SUEDE and st.getQuestItemsCount(THREAD) >= RADIA_THREAD and st.getQuestItemsCount(SPIDERSILK) == 1 :
       st.takeItems(SUEDE,RADIA_SUEDE)
       st.takeItems(THREAD,RADIA_THREAD)
       st.takeItems(SPIDERSILK,1)
       st.giveItems(MYSTERIOUS_CLOTH,1)
       st.playSound("ItemSound.quest_finish")
       st.exitQuest(1)
     else :
       htmltext = "You don't have enough materials"
   return htmltext

 def onTalk (self,npc,player):
   htmltext = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>"
   st = player.getQuestState(qn)
   if not st : return htmltext
   npcId = npc.getNpcId()
   id = st.getState()
   cond = st.getInt("cond")
   if npcId == RADIA and cond == 0 and st.getQuestItemsCount(MYSTERIOUS_CLOTH) == 0 :
     fwear=player.getQuestState("37_PleaseMakeMeFormalWear")
     if fwear :
       if fwear.get("cond") == "6" :
         htmltext = "30088-0.htm"
       else :
         st.exitQuest(1)
     else :
       st.exitQuest(1)
   elif id == State.STARTED :    
       if npcId == VARAN and cond == 1 :
         htmltext = "30294-0.htm"
       elif npcId == RADIA and cond == 2 :
         htmltext = "30088-2.htm"
       elif npcId == RALFORD and cond == 3 :
         htmltext = "30165-0.htm"
       elif npcId == RALFORD and cond == 5 :
         htmltext = "30165-2.htm"
       elif npcId == RADIA and cond == 6 :
          htmltext = "30088-4.htm"
   return htmltext

 def onKill(self,npc,player,isPet):
   partyMember = self.getRandomPartyMember(player,"4")
   if not partyMember : return

   st = partyMember.getQuestState(qn)
   if not st : return 
   if st.getState() != State.STARTED : return

   count = st.getQuestItemsCount(SPINNERET)
   if count < RALFORD_SPINNERET :
     st.giveItems(SPINNERET,int(1))
     if count == (RALFORD_SPINNERET - 1) :
       st.playSound("ItemSound.quest_middle")
       st.set("cond","5")
     else :
       st.playSound("ItemSound.quest_itemget")
   return

QUEST = Quest(34,qn,"In Search of Clothes")

QUEST.addStartNpc(RADIA)

QUEST.addTalkId(RADIA)
QUEST.addTalkId(RALFORD)
QUEST.addTalkId(VARAN)

QUEST.addKillId(TRISALIM_SPIDER)
QUEST.addKillId(TRISALIM_TARANTULA)
