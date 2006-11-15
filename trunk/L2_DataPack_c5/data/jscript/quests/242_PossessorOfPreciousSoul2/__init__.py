# Maked by Mr. Have fun! Version 0.2
print "importing quests: 242: Possessor of a Precious Soul 2"
import sys
from net.sf.l2j.gameserver.model.quest import State
from net.sf.l2j.gameserver.model.quest import QuestState
from net.sf.l2j.gameserver.model.quest.jython import QuestJython as JQuest

VIRGILS_LETTER_ID = 7677
BLONDE_STRAND_ID = 7590
SORCERY_INGREDIENT_ID = 7596
CARADINE_LETTER_ID = 7678
ORB_OF_BINDING_ID = 7595


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
     st.set("prog","0")
   if npcId==8742 and int(st.get("cond"))==0 and int(st.get("onlyone"))==0 and st.getQuestItemsCount(VIRGILS_LETTER_ID)==1 :
     if st.getPlayer().getSubLevel() >= 60 :
       htmltext = "8742-1.htm"
     else:
       htmltext = "8742-10.htm"
       st.exitQuest(1)
   elif npcId == 8742 and int(st.get("cond"))==0 and int(st.get("onlyone"))==1 :
      htmltext = "<html><head><body>This quest have already been completed.</body></html>"
   elif npcId==8743 and int(st.get("cond"))==1 and st.player.isSubClassActive() :
        htmltext = "8743-1.htm"
        st.set("cond","2")
   elif npcId==8744 and int(st.get("cond"))==2 and st.player.isSubClassActive() :
        htmltext = "8744-1.htm"
        st.set("cond","3")
   elif npcId==8751 and int(st.get("cond"))==3 and st.player.isSubClassActive() :
        htmltext = "8751-1.htm"
        st.set("cond","4")
   elif npcId==8752 and int(st.get("cond"))==4 and st.player.isSubClassActive() :
        st.giveItems(BLONDE_STRAND_ID,1)
        st.set("cond","5")
   elif npcId==8751 and int(st.get("cond"))==5 and st.getQuestItemsCount(BLONDE_STRAND_ID)==1 and st.player.isSubClassActive() :
        htmltext = "8751-2.htm"
        st.set("cond","6")
   elif npcId==7759 and int(st.get("cond"))==6 and st.getQuestItemsCount(BLONDE_STRAND_ID)==1 and st.player.isSubClassActive() :
        htmltext = "7759-1.htm"
        st.takeItems(BLONDE_STRAND_ID,1)
        st.set("cond","7")
   elif npcId==7738 and int(st.get("cond"))==7 and st.player.isSubClassActive() :
        htmltext = "7738-1.htm"
        st.giveItems(SORCERY_INGREDIENT_ID,1)
        st.set("cond","8")
   elif npcId==7759 and int(st.get("cond"))==8 and st.getQuestItemsCount(SORCERY_INGREDIENT_ID)==1 and st.player.isSubClassActive() :
        htmltext = "7759-2.htm"
        st.takeItems(SORCERY_INGREDIENT_ID,1)
        st.set("cond","9")
   elif npcId==8748 and int(st.get("cond"))==9 and st.getQuestItemsCount(ORB_OF_BINDING_ID) >=1 and st.player.isSubClassActive() :
        htmltext = "8748-1.htm"
        st.takeItems(ORB_OF_BINDING_ID,1)
        npc.reduceCurrentHp(9999999, npc)
        if st.getInt("prog") < 4 :
          st.set("prog", str(st.getInt("prog")+1))
        if st.getInt("prog")==4 :
          st.set("cond","10")
          st.playSound("ItemSound.quest_middle")
   elif npcId==8746 and int(st.get("cond"))==10 and st.player.isSubClassActive() :
        npc.reduceCurrentHp(9999999, npc)
        st.getPcSpawn().addSpawn(8747)
   elif npcId==8747 and int(st.get("cond"))==10 and st.player.isSubClassActive() :
        htmltext = "8747-1.htm"
        st.set("cond","11")
   elif npcId==8743 and int(st.get("cond"))==11 and st.player.isSubClassActive() :
        htmltext = "8743-2.htm"
        st.set("cond","0")
        st.giveItems(CARADINE_LETTER_ID,1)
        st.set("onlyone","1")
        st.setState(COMPLETED) 
        st.playSound("ItemSound.quest_finish")
   return htmltext

 def onKill (self,npc,st):

   npcId = npc.getNpcId()
   if npcId==5317 :
    if int(st.get("cond"))==9 and st.getQuestItemsCount(ORB_OF_BINDING_ID) < 4 and st.player.isSubClassActive() :
      st.giveItems(ORB_OF_BINDING_ID,1)
      if st.getQuestItemsCount(ORB_OF_BINDING_ID)<4 :
        st.playSound("ItemSound.quest_itemget")
      else:
        st.playSound("ItemSound.quest_middle")
   return 

QUEST       = Quest(242,"242_PossessorOfPreciousSoul2","Possessor Of Precious Soul 2")
CREATED     = State('Start', QUEST)
STARTING     = State('Starting', QUEST)
STARTED     = State('Started', QUEST)
COMPLETED   = State('Completed', QUEST)


QUEST.setInitialState(CREATED)
QUEST.addStartNpc(8742)

STARTING.addTalkId(8742)

STARTED.addTalkId(8742)
STARTED.addTalkId(8743)
STARTED.addTalkId(8751)
STARTED.addTalkId(8752)
STARTED.addTalkId(7759)
STARTED.addTalkId(7738)
STARTED.addTalkId(8744)
STARTED.addTalkId(8748)
STARTED.addTalkId(8747)
STARTED.addTalkId(8746)

STARTED.addKillId(5317)

STARTED.addQuestDrop(8752,BLONDE_STRAND_ID,1)
STARTED.addQuestDrop(7795,SORCERY_INGREDIENT_ID,1)
STARTED.addQuestDrop(5317,ORB_OF_BINDING_ID,1)