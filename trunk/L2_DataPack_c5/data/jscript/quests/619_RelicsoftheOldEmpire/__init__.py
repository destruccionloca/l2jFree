import sys

from net.sf.l2j.gameserver.model.quest import State
from net.sf.l2j.gameserver.model.quest import QuestState
from net.sf.l2j.gameserver.model.quest.jython import QuestJython as JQuest

qn = "619_RelicsoftheOldEmpire"

#NPC
NPC_1   =   31538   #Ghost of Adventurer
NPC_2   =   31454   #Ghost of Wigoth

#ITEMS
ITEM_1  =   7254    #Broken Relic Part
ITEM_2  =   7075    #Entrance Pass to the Sepulcher

#REWARD
REWORD      =   [6881,6883,6885,6887,6891,6893,6895,6897,6899,7580]
#6881   #Recipe: Forgotten Blade (60%)
#6883   #Recipe: Basalt Battlehammer (60%)
#6885   #Recipe: Imperial Staff (60%)
#6887   #Recipe: Angel Slayer (60%)
#6891   #Recipe: Dragon Hunter Axe (60%)
#6893   #Recipe: Saint Spear (60%)
#6895   #Recipe: Demon Splinter (60%)
#6897   #Recipe: Heavens Divider (60%)
#6899   #Recipe: Arcana Mace (60%)
#7580   #Recipe: Draconic Bow (60%)

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onEvent (self,event,st) :
   if event == "1" :
     st.setState(STARTED)
     st.playSound("ItemSound.quest_accept")
     htmltext = "31538-3.htm"
     st.set("cond","1")
   elif event == "2" :
     htmltext = "31538-4.htm"
     st.playSound("ItemSound.quest_finish")
     st.exitQuest(1)
   elif event == "3" :
     htmltext = "31538-7.htm"
     st.takeItems(ITEM_1,1000)
     rewardNo = st.getRandom(10)
     st.giveItems(REWORD[rewardNo],1)
     st.playSound("ItemSound.quest_finish")
   elif event == "10" :
     htmltext = "31538-7.htm"
     st.takeItems(ITEM_1,1000)
     st.giveItems(6881,1)
     st.playSound("ItemSound.quest_finish")
   elif event == "11" :
     htmltext = "31538-7.htm"
     st.takeItems(ITEM_1,1000)
     st.giveItems(6683,1)
     st.playSound("ItemSound.quest_finish")
   elif event == "12" :
     htmltext = "31538-7.htm"
     st.takeItems(ITEM_1,1000)
     st.giveItems(6885,1)
     st.playSound("ItemSound.quest_finish")
   elif event == "13" :
     htmltext = "31538-7.htm"
     st.takeItems(ITEM_1,1000)
     st.giveItems(6887,1)
     st.playSound("ItemSound.quest_finish")
   elif event == "14" :
     htmltext = "31538-7.htm"
     st.takeItems(ITEM_1,1000)
     st.giveItems(7580,1)
     st.playSound("ItemSound.quest_finish")
   elif event == "15" :
     htmltext = "31538-7.htm"
     st.takeItems(ITEM_1,1000)
     st.giveItems(6891,1)
     st.playSound("ItemSound.quest_finish")
   elif event == "16" :
     htmltext = "31538-7.htm"
     st.takeItems(ITEM_1,1000)
     st.giveItems(6893,1)
     st.playSound("ItemSound.quest_finish")
   elif event == "17" :
     htmltext = "31538-7.htm"
     st.takeItems(ITEM_1,1000)
     st.giveItems(6895,1)
     st.playSound("ItemSound.quest_finish")
   elif event == "18" :
     htmltext = "31538-7.htm"
     st.takeItems(ITEM_1,1000)
     st.giveItems(6897,1)
     st.playSound("ItemSound.quest_finish")
   elif event == "19" :
     htmltext = "31538-7.htm"
     st.takeItems(ITEM_1,1000)
     st.giveItems(6899,1)
     st.playSound("ItemSound.quest_finish")
   return htmltext

 def onTalk (Self,npc,player) :
   st = player.getQuestState(qn)
   id = st.getState()
   if id == CREATED :
     st.set("cond","0")
   npcId = npc.getNpcId()
   if int(st.get("cond")) == 0 :
     if st.getPlayer().getLevel() >= 74 :
       htmltext = "31538-1.htm"
     else :
       htmltext = "31538-2.htm"
       st.exitQuest(1)
   elif int(st.get("cond")) == 1 :
     if npcId == NPC_1 :
       if st.getQuestItemsCount(ITEM_1) >= 1000 :
         htmltext = "31538-5.htm"
       else :
         htmltext = "31538-6.htm"
     elif npcId == NPC_2 :
       if st.getQuestItemsCount(ITEM_1) >= 1000 :
         htmltext = "31454-1.htm"
       else :
         htmltext = "31454-2.htm"
   return htmltext

 def onKill (self,npc,player):
   st = player.getQuestState(qn)
   npcId = npc.getNpcId()
   if st:
       if int(st.get("cond")) == 1 and (npcId in range(21396,21434) or npcId in range(21798,21800) or npcId in range(18120,18256)) :
         if st.getRandom(100) < 25 :
           st.giveItems(ITEM_1,1)
           st.playSound("ItemSound.quest_itemget")
         elif st.getRandom(100) < 5 :
           st.giveItems(ITEM_2,1)
           st.playSound("ItemSound.quest_itemget")
   return


QUEST       = Quest(619,qn,"Relics of the Old Empire") 
CREATED     = State('Start', QUEST)
STARTED     = State('Started', QUEST)
COMPLETED   = State('Completed', QUEST)

QUEST.setInitialState(CREATED)
QUEST.addStartNpc(NPC_1) 

QUEST.addTalkId(NPC_1) 
QUEST.addTalkId(NPC_2) 

for npcKillId in range(21396,21434) :
  QUEST.addKillId(npcKillId)

for npcKillId in range(21798,21800) :
  QUEST.addKillId(npcKillId)

for npcKillId in range(18120,18256) :
  QUEST.addKillId(npcKillId)

print "importing quests: 619: Relics of the Old Empire" 
