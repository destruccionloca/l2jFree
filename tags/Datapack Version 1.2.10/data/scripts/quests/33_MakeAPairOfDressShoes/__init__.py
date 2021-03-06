# Made by disKret
import sys
from com.l2jfree import Config
from com.l2jfree.gameserver.model.quest import State
from com.l2jfree.gameserver.model.quest import QuestState
from com.l2jfree.gameserver.model.quest.jython import QuestJython as JQuest

qn = "33_MakeAPairOfDressShoes"

#NPC
WOODLEY = 30838
IAN     = 30164
LEIKAR  = 31520

#ITEM
ADENA           = 57
LEATHER         = 1882
THREAD          = 1868
DRESS_SHOES_BOX = 7113

#NEEDED
WOODLEY_LEATHER = 200
WOODLEY_THREAD  = 600
WOODLEY_PRICE   = 200000
IAN_PRICE       = 300000


class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onEvent (self,event,st) :
   htmltext = event
   if event == "30838-1.htm" :
     st.set("cond","1")
     st.setState(State.STARTED)
     st.playSound("ItemSound.quest_accept")
   if event == "31520-1.htm" :
     st.set("cond","2")
   if event == "30838-3.htm" :
     st.set("cond","3")
   if event == "30838-5.htm" :
     if st.getQuestItemsCount(LEATHER) >= WOODLEY_LEATHER and st.getQuestItemsCount(THREAD) >= WOODLEY_THREAD and st.getQuestItemsCount(ADENA) >= WOODLEY_PRICE :
       st.takeItems(LEATHER,WOODLEY_LEATHER)
       st.takeItems(THREAD,WOODLEY_THREAD)
       st.takeItems(ADENA,WOODLEY_PRICE)
       st.set("cond","4")
     else :
       htmltext = "You don't have enough materials"
   if event == "30164-1.htm" :
     if st.getQuestItemsCount(ADENA) >= IAN_PRICE :
       st.takeItems(ADENA,IAN_PRICE)
       st.set("cond","5")
     else :
       htmltext = "You don't have enough materials"
   if event == "30838-7.htm" :
     st.giveItems(DRESS_SHOES_BOX,1)
     st.playSound("ItemSound.quest_finish")
     st.exitQuest(1)
   return htmltext

 def onTalk (self,npc,player):
   htmltext = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>"
   st = player.getQuestState(qn)
   if not st : return htmltext

   npcId = npc.getNpcId()
   id = st.getState()
   if id == State.CREATED :
     st.set("cond","0")
   cond = st.getInt("cond")
   if npcId == WOODLEY and cond == 0 and st.getQuestItemsCount(DRESS_SHOES_BOX) == 0 :
     fwear=player.getQuestState("37_PleaseMakeMeFormalWear")
     if fwear :
       if fwear.get("cond") == "7" :
         htmltext = "30838-0.htm"
       else:
         st.exitQuest(1)
     else:
       st.exitQuest(1)
   elif id == State.STARTED :    
       if npcId == LEIKAR and cond == 1 :
         htmltext = "31520-0.htm"
       elif npcId == WOODLEY and cond == 2 :
         htmltext = "30838-2.htm"
       elif npcId == WOODLEY and cond == 3 :
         htmltext = "30838-4.htm"
       elif npcId == IAN and cond == 4 :
         htmltext = "30164-0.htm"
       elif npcId == WOODLEY and cond == 5 :
         htmltext = "30838-6.htm"
   return htmltext

QUEST       = Quest(33,qn,"Make A Pair Of Dress Shoes")


QUEST.addStartNpc(WOODLEY)

QUEST.addTalkId(WOODLEY)
QUEST.addTalkId(IAN)
QUEST.addTalkId(LEIKAR)
