# Maked by Mr. Have fun! Version 0.2
print "importing quests: 241: Possessor of a Precious Soul"
import sys
from net.sf.l2j.gameserver.model.quest import State
from net.sf.l2j.gameserver.model.quest import QuestState
from net.sf.l2j.gameserver.model.quest.jython import QuestJython as JQuest

FEATHER_OF_GABRIELLE_ID = 3852
LEGEND_OF_SEVENTEEN_ID = 7587
FORGOTTEN_SONG_ECHO_ID = 7589
MALRUK_SUCCUBUS_CLAW_ID = 7597
FADED_POETRY_BOOK_ID = 7588
VIRGILS_LETTER_ID = 7677
RAHORAKTI_MEDICINE_ID = 7599
CRIMSON_MOSS_ID = 7598
LUNARGENT_ID = 6029
HELLFIRE_OIL_ID = 6033

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onEvent (self,event,st) :
    htmltext = event
    if event=="1" :
        htmltext = "8739-04.htm"
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
   if npcId==8739 and int(st.get("cond"))==0 and int(st.get("onlyone"))==0 :
     if st.getPlayer().getSubLevel() >= 50 :
       htmltext = "8739-03.htm"
     else:
       htmltext = "7461-02.htm"
       st.exitQuest(1)
   elif npcId == 8739 and int(st.get("cond"))==0 and int(st.get("onlyone"))==1 :
      htmltext = "<html><head><body>This quest have already been completed.</body></html>"
   elif npcId==7753 and int(st.get("cond"))==1 and st.player.isSubClassActive() :
        htmltext = "7753-1.htm"
        st.giveItems(FEATHER_OF_GABRIELLE_ID,1)
        st.set("cond","2")
   elif npcId==7754 and int(st.get("cond"))==2 and st.getQuestItemsCount(FEATHER_OF_GABRIELLE_ID)==1 and st.player.isSubClassActive() :
        htmltext = "7754-1.htm"
        st.takeItems(FEATHER_OF_GABRIELLE_ID,1)
        st.set("cond","3")
   elif npcId==8739 and int(st.get("cond"))==4 and st.getQuestItemsCount(LEGEND_OF_SEVENTEEN_ID)==1 and st.player.isSubClassActive() :
        htmltext = "8739-6.htm"
        st.takeItems(LEGEND_OF_SEVENTEEN_ID,1)
        st.set("cond","5")
   elif npcId==8042 and int(st.get("cond"))==5 and st.player.isSubClassActive() :
        htmltext = "8042-1.htm"
        st.set("cond","6")
   elif npcId==8042 and int(st.get("cond"))==7 and st.getQuestItemsCount(MALRUK_SUCCUBUS_CLAW_ID)==10 and st.player.isSubClassActive()  :
        htmltext = "8042-2.htm"
        st.giveItems(FORGOTTEN_SONG_ECHO_ID,1)
        st.takeItems(MALRUK_SUCCUBUS_CLAW_ID,10)
        st.set("cond","8")
   elif npcId==8739 and int(st.get("cond"))==8 and st.getQuestItemsCount(FORGOTTEN_SONG_ECHO_ID)==1 and st.player.isSubClassActive() :
        htmltext = "8739-05.htm"
        st.takeItems(FORGOTTEN_SONG_ECHO_ID,1)
        st.set("cond","9")
   elif npcId==7692 and int(st.get("cond"))==9 and st.player.isSubClassActive() :
        htmltext = "7692-1.htm"
        st.giveItems(FADED_POETRY_BOOK_ID,1)
        st.set("cond","10")
   elif npcId==8739 and int(st.get("cond"))==10 and st.getQuestItemsCount(FADED_POETRY_BOOK_ID)==1 and st.player.isSubClassActive() :
        htmltext = "8739-06.htm"
        st.takeItems(FADED_POETRY_BOOK_ID,1)
        st.set("cond","11")
   elif npcId==8742 and int(st.get("cond"))==11 and st.player.isSubClassActive() :
        htmltext = "8742-1.htm"
        st.set("cond","12")
   elif npcId==8744 and int(st.get("cond"))==12 and st.player.isSubClassActive() :
        htmltext = "8744-1.htm"
        st.set("cond","13")
   elif npcId==8336 and int(st.get("cond"))==13 and st.player.isSubClassActive() :
        htmltext = "8336-1.htm"
        st.set("cond","14")
   elif npcId==8336 and int(st.get("cond"))==15 and st.getQuestItemsCount(CRIMSON_MOSS_ID)==5 and st.player.isSubClassActive() :
        htmltext = "8336-2.htm"
        st.takeItems(CRIMSON_MOSS_ID,5)
        st.giveItems(RAHORAKTI_MEDICINE_ID,1)
        st.set("cond","16")
   elif npcId==8743 and int(st.get("cond"))==16 and st.getQuestItemsCount(RAHORAKTI_MEDICINE_ID)==1 and st.player.isSubClassActive() :
        htmltext = "8743-1.htm"
        st.takeItems(RAHORAKTI_MEDICINE_ID,1)
        st.set("cond","17")
   elif npcId==8742 and int(st.get("cond"))==17 and st.player.isSubClassActive() :
        htmltext = "8742-2.htm"
        st.set("cond","18")
   elif npcId==8740 and int(st.get("cond"))==18 and st.player.isSubClassActive() :
        htmltext = "8740-1.htm"
        st.set("cond","19")
   elif npcId==8272 and int(st.get("cond"))==19 :
        if st.getQuestItemsCount(LUNARGENT_ID)==5 and st.getQuestItemsCount(HELLFIRE_OIL_ID)==1 and st.player.isSubClassActive() :
          st.takeItems(LUNARGENT_ID,5)
          st.takeItems(HELLFIRE_OIL_ID,1)
          htmltext = "8272-2.htm"
          st.set("cond","21")
        else:
          htmltext = "<html><head><body>You need to bring me 5 lunargents and 1 hellfire oil. Magic Trader Wesley at the Ivory Tower will tell you how to obtain these materials <br>Now off you go.</body></html>"
          st.set("cond","20")
   elif npcId==8272 and int(st.get("cond"))==20 and st.player.isSubClassActive() : 
        if st.getQuestItemsCount(LUNARGENT_ID)==5 and st.getQuestItemsCount(HELLFIRE_OIL_ID)==1 :
          htmltext = "8272-2.htm"
          st.set("cond","21")
          st.takeItems(LUNARGENT_ID,5)
          st.takeItems(HELLFIRE_OIL_ID,1)
        else:
          htmltext = "<html><head><body>Didn't you hear me? You need to bring me 5 lunargents and 1 hellfire oil. Magic Trader Wesley at the Ivory Tower will tell you how to obtain these materials <br>Now off you go.</body></html>"
   elif npcId==8740 and int(st.get("cond"))==21 and st.player.isSubClassActive() :
        htmltext = "8740-2.htm"
        st.set("cond","0")
        st.giveItems(VIRGILS_LETTER_ID,1)
        st.set("onlyone","1")
        st.setState(COMPLETED) 
        st.playSound("ItemSound.quest_finish")
   return htmltext

 def onKill (self,npc,st):

   npcId = npc.getNpcId()
   if npcId==5113 :
    if int(st.get("cond"))==3 and st.getQuestItemsCount(LEGEND_OF_SEVENTEEN_ID)<1 and st.player.isSubClassActive() :
      st.giveItems(LEGEND_OF_SEVENTEEN_ID,1)
      st.playSound("ItemSound.quest_middle")
      st.set("cond","4")
   elif npcId==244 :
    if int(st.get("cond"))==6 and st.getQuestItemsCount(MALRUK_SUCCUBUS_CLAW_ID)<10 and st.player.isSubClassActive() :
      if st.getRandom(100) < 10 :
        st.giveItems(MALRUK_SUCCUBUS_CLAW_ID,1)
        if st.getQuestItemsCount(MALRUK_SUCCUBUS_CLAW_ID) < 10 :
          st.playSound("ItemSound.quest_itemget")
        else:
          st.playSound("ItemSound.quest_middle")
          st.set("cond","7")
   elif npcId==245 :
    if int(st.get("cond"))==6 and st.getQuestItemsCount(MALRUK_SUCCUBUS_CLAW_ID)<10 and st.player.isSubClassActive() :
      if st.getRandom(100) < 10 :
        st.giveItems(MALRUK_SUCCUBUS_CLAW_ID,1)
        if st.getQuestItemsCount(MALRUK_SUCCUBUS_CLAW_ID) < 10 :
          st.playSound("ItemSound.quest_itemget")
        else:
          st.playSound("ItemSound.quest_middle")
          st.set("cond","7")
   elif npcId==283 :
    if int(st.get("cond"))==6 and st.getQuestItemsCount(MALRUK_SUCCUBUS_CLAW_ID)<10 and st.player.isSubClassActive() :
      if st.getRandom(100) < 10 :
        st.giveItems(MALRUK_SUCCUBUS_CLAW_ID,1)
        if st.getQuestItemsCount(MALRUK_SUCCUBUS_CLAW_ID) < 10 :
          st.playSound("ItemSound.quest_itemget")
        else:
          st.playSound("ItemSound.quest_middle")
          st.set("cond","7")
   elif npcId==284 :
    if int(st.get("cond"))==6 and st.getQuestItemsCount(MALRUK_SUCCUBUS_CLAW_ID)<10 and st.player.isSubClassActive() :
      if st.getRandom(100) < 10 :
        st.giveItems(MALRUK_SUCCUBUS_CLAW_ID,1)
        if st.getQuestItemsCount(MALRUK_SUCCUBUS_CLAW_ID) < 10 :
          st.playSound("ItemSound.quest_itemget")
        else:
          st.playSound("ItemSound.quest_middle")
          st.set("cond","7")
   elif npcId==1511 :
    if int(st.get("cond"))==14 and st.getQuestItemsCount(CRIMSON_MOSS_ID)<5 and st.player.isSubClassActive() :
     if st.getRandom(100) < 10 :
        st.giveItems(CRIMSON_MOSS_ID,1)
        if st.getQuestItemsCount(CRIMSON_MOSS_ID) < 5 :
          st.playSound("ItemSound.quest_itemget")
        else:
          st.playSound("ItemSound.quest_middle")
          st.set("cond","15")
   elif npcId==1512 :
    if int(st.get("cond"))==14 and st.getQuestItemsCount(CRIMSON_MOSS_ID)<5 and st.player.isSubClassActive() :
     if st.getRandom(100) < 10 :
        st.giveItems(CRIMSON_MOSS_ID,1)
        if st.getQuestItemsCount(CRIMSON_MOSS_ID) < 5 :
          st.playSound("ItemSound.quest_itemget")
        else:
          st.playSound("ItemSound.quest_middle")
          st.set("cond","15")
   return

QUEST       = Quest(241,"241_PossessorOfPreciousSoul","Possessor Of Precious Soul")
CREATED     = State('Start', QUEST)
STARTING     = State('Starting', QUEST)
STARTED     = State('Started', QUEST)
COMPLETED   = State('Completed', QUEST)


QUEST.setInitialState(CREATED)
QUEST.addStartNpc(8739)

STARTING.addTalkId(8739)

STARTED.addTalkId(8739)
STARTED.addTalkId(7753)
STARTED.addTalkId(7754)
STARTED.addTalkId(8042)
STARTED.addTalkId(7692)
STARTED.addTalkId(8742)
STARTED.addTalkId(8744)
STARTED.addTalkId(8336)
STARTED.addTalkId(8743)
STARTED.addTalkId(8740)
STARTED.addTalkId(8272)

STARTED.addKillId(5113)
STARTED.addKillId(244)
STARTED.addKillId(245)
STARTED.addKillId(283)
STARTED.addKillId(284)
STARTED.addKillId(1511)
STARTED.addKillId(1512)

STARTED.addQuestDrop(7753,FEATHER_OF_GABRIELLE_ID,1)
STARTED.addQuestDrop(5113,LEGEND_OF_SEVENTEEN_ID,1)
STARTED.addQuestDrop(244,MALRUK_SUCCUBUS_CLAW_ID,1)
STARTED.addQuestDrop(245,MALRUK_SUCCUBUS_CLAW_ID,1)
STARTED.addQuestDrop(283,MALRUK_SUCCUBUS_CLAW_ID,1)
STARTED.addQuestDrop(284,MALRUK_SUCCUBUS_CLAW_ID,1)
STARTED.addQuestDrop(8042,FORGOTTEN_SONG_ECHO_ID,1)
STARTED.addQuestDrop(7692,FADED_POETRY_BOOK_ID,1)
STARTED.addQuestDrop(8336,RAHORAKTI_MEDICINE_ID,1)
STARTED.addQuestDrop(1511,CRIMSON_MOSS_ID,1)
STARTED.addQuestDrop(1512,CRIMSON_MOSS_ID,1)
STARTED.addQuestDrop(8740,VIRGILS_LETTER_ID,1)
