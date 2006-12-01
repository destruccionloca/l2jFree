# Maked by Mr. Have fun! Version 0.2
print "importing quests: 246: Possessor of a Precious Soul 3"
import sys
from net.sf.l2j.gameserver.model.quest import State
from net.sf.l2j.gameserver.model.quest import QuestState
from net.sf.l2j.gameserver.model.quest.jython import QuestJython as JQuest

CARADINE_LETTER_ID = 7678
RING_GOD_ID = 7591
NECK_GOD_ID = 7592
STAFF_GOD_ID = 7593
CARADINE_LETTER2_ID = 7679
BOX_ID = 7594

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onEvent (self,event,st) :
    htmltext = event
    if event=="1" :
        htmltext = "start.htm"
        st.set("cond","1")
        st.setState(STARTED)
        st.playSound("ItemSound.quest_accept")
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
   if npcId==8740 and int(st.get("cond"))==0 and int(st.get("onlyone"))==0 and st.getQuestItemsCount(CARADINE_LETTER_ID)==1 :
     if st.getPlayer().getSubLevel() >= 65 :
       htmltext = "8740-1.htm"
     else:
       htmltext = "8740-10.htm"
       st.exitQuest(1)
   elif npcId == 8740 and int(st.get("cond"))==0 and int(st.get("onlyone"))==1 :
      htmltext = "<html><head><body>This quest have already been completed.</body></html>"
   elif npcId==8741 and int(st.get("cond"))==1 and st.player.isSubClassActive() :
        htmltext = "8741-1.htm"
        st.set("cond","2")
   elif npcId==8741 and int(st.get("cond"))==3 and st.getQuestItemsCount(RING_GOD_ID)==1 and st.getQuestItemsCount(NECK_GOD_ID)==1 and st.player.isSubClassActive() :
        st.takeItems(RING_GOD_ID,1)
        st.takeItems(NECK_GOD_ID,1)
        htmltext = "8741-2.htm"
        st.set("cond","4")
   elif npcId==8741 and int(st.get("cond"))==5 and st.getQuestItemsCount(STAFF_GOD_ID)==1 and st.player.isSubClassActive() :
        htmltext = "8741-3.htm"
        st.takeItems(STAFF_GOD_ID,1)
        st.giveItems(BOX_ID,1)
        st.set("cond","6")
   elif npcId==7721 and int(st.get("cond"))==6 and st.player.isSubClassActive() :
        htmltext = "7721-1.htm"
        st.set("cond","0")
        st.giveItems(CARADINE_LETTER2_ID,1)
        st.set("onlyone","1")
        st.setState(COMPLETED) 
        st.playSound("ItemSound.quest_finish")
   return htmltext

 def onKill (self,npc,st):

   npcId = npc.getNpcId()
   if npcId==1541 :
    if int(st.get("cond"))==2 and st.getQuestItemsCount(RING_GOD_ID) < 1 and st.player.isSubClassActive():
      if st.getRandom(100) < 50 :
        st.giveItems(RING_GOD_ID,1)
        if st.getQuestItemsCount(NECK_GOD_ID) < 1 :
          st.playSound("ItemSound.quest_itemget")
        else:
          st.playSound("ItemSound.quest_middle")
          st.set("cond","3")
   if npcId==1544 :
    if int(st.get("cond"))==2 and st.getQuestItemsCount(NECK_GOD_ID) < 1 and st.player.isSubClassActive():
      if st.getRandom(100) < 50 :
        st.giveItems(NECK_GOD_ID,1)
        if st.getQuestItemsCount(RING_GOD_ID) < 1 :
          st.playSound("ItemSound.quest_itemget")
        else:
          st.playSound("ItemSound.quest_middle")
          st.set("cond","3")
   if npcId==10325 :
    if int(st.get("cond"))==4 and st.getQuestItemsCount(STAFF_GOD_ID) < 1 and st.player.isSubClassActive():
      st.giveItems(STAFF_GOD_ID,1)
      st.playSound("ItemSound.quest_middle")
      st.set("cond","5")
   return 

QUEST       = Quest(246,"246_PossessorOfPreciousSoul3","Possessor Of Precious Soul 3")
CREATED     = State('Start', QUEST)
STARTING     = State('Starting', QUEST)
STARTED     = State('Started', QUEST)
COMPLETED   = State('Completed', QUEST)


QUEST.setInitialState(CREATED)
QUEST.addStartNpc(8740)

STARTING.addTalkId(8740)

STARTED.addTalkId(8740)
STARTED.addTalkId(8741)
STARTED.addTalkId(7721)

STARTED.addKillId(1541)
STARTED.addKillId(1544)
STARTED.addKillId(10325)

STARTED.addQuestDrop(8741,BOX_ID,1)
STARTED.addQuestDrop(1541,RING_GOD_ID,1)
STARTED.addQuestDrop(1544,NECK_GOD_ID,1)
STARTED.addQuestDrop(10325,STAFF_GOD_ID,1)
STARTED.addQuestDrop(7721,CARADINE_LETTER2_ID,1)