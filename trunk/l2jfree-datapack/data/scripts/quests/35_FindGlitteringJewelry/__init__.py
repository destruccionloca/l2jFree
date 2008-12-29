# Made by disKret
import sys
from com.l2jfree import Config
from com.l2jfree.tools.random import Rnd
from com.l2jfree.gameserver.model.quest import State
from com.l2jfree.gameserver.model.quest import QuestState
from com.l2jfree.gameserver.model.quest.jython import QuestJython as JQuest

qn = "35_FindGlitteringJewelry"

#REWARD
JEWEL_BOX = 7077

#ITEM
ROUGH_JEWEL   = 7162
ORIHARUKON    = 1893
SILVER_NUGGET = 1873
THONS         = 4044

#NEEDED
ELLIE_JEWEL       = 10
ELLIE_ORIHARUKON  = 5
ELLIE_SILV_NUGGET = 500
ELLIE_THONS       = 150

#NPC
ELLIE  = 30091
FELTON = 30879

#MOB
ALLIGATOR = 20135

class Quest (JQuest) :

 def __init__(self,id,name,descr):
     JQuest.__init__(self,id,name,descr)
     self.questItemIds = [ROUGH_JEWEL]

 def onEvent (self,event,st) :
   htmltext = event
   cond = st.getInt("cond")
   if event == "30091-1.htm" and cond == 0 :
     st.set("cond","1")
     st.setState(State.STARTED)
     st.playSound("ItemSound.quest_accept")
   if event == "30879-1.htm" and cond == 1:
     st.set("cond","2")
   if event == "30091-3.htm" and cond == 3:
     st.takeItems(ROUGH_JEWEL,ELLIE_JEWEL)
     st.set("cond","4")
   if event == "30091-5.htm" and cond == 4:
     if st.getQuestItemsCount(ORIHARUKON) >= ELLIE_ORIHARUKON and st.getQuestItemsCount(SILVER_NUGGET) >= ELLIE_SILV_NUGGET and st.getQuestItemsCount(THONS) >= ELLIE_THONS :
       st.takeItems(ORIHARUKON,ELLIE_ORIHARUKON)
       st.takeItems(SILVER_NUGGET,ELLIE_SILV_NUGGET)
       st.takeItems(THONS,ELLIE_THONS)
       st.giveItems(JEWEL_BOX,1)
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
   cond = st.getInt("cond")
   id = st.getState()
   if npcId == ELLIE and cond == 0 and st.getQuestItemsCount(JEWEL_BOX) == 0 :
     fwear=player.getQuestState("37_PleaseMakeMeFormalWear")
     if not fwear is None :
       if fwear.get("cond") == "6" :
         htmltext = "30091-0.htm"
         return htmltext
       else:
         htmltext = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>" 
         st.exitQuest(1)
     else:
       htmltext = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>" 
       st.exitQuest(1)
   elif npcId == FELTON and cond == 1 :
     htmltext = "30879-0.htm"
   elif id == State.STARTED :  
       if npcId == ELLIE and st.getQuestItemsCount(ROUGH_JEWEL) == ELLIE_JEWEL :
         htmltext = "30091-2.htm"
       elif npcId == ELLIE and cond == 4 and st.getQuestItemsCount(ORIHARUKON) >= ELLIE_ORIHARUKON and st.getQuestItemsCount(SILVER_NUGGET) >= ELLIE_SILV_NUGGET and st.getQuestItemsCount(THONS) >= ELLIE_THONS :
         htmltext = "30091-4.htm"
   return htmltext


 def onKill(self,npc,player,isPet):
   partyMember1 = self.getRandomPartyMember(player,"1")
   partyMember2 = self.getRandomPartyMember(player,"2")
   partyMember = partyMember1 # initialize
   if not partyMember1 and not partyMember2: return
   elif not partyMember2 : partyMember = partyMember1
   elif not partyMember1 : partyMember = partyMember2
   else :
       if Rnd.get(2): partyMember = partyMember2
   
   if not partyMember : return
   st = partyMember.getQuestState(qn)
   if not st : return 
   if st.getState() != State.STARTED : return   
   count = st.getQuestItemsCount(ROUGH_JEWEL)
   if count < ELLIE_JEWEL :
     st.giveItems(ROUGH_JEWEL,int(1))
     if count == (ELLIE_JEWEL - 1) :
       st.playSound("ItemSound.quest_middle")
       st.set("cond","3")
     else:
       st.playSound("ItemSound.quest_itemget")
   return

QUEST       = Quest(35,qn,"Find Glittering Jewelry")

QUEST.addStartNpc(ELLIE)

QUEST.addTalkId(ELLIE)
QUEST.addTalkId(FELTON)

QUEST.addKillId(ALLIGATOR)
