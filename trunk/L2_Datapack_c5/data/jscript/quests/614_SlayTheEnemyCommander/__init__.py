# Maked by Tomciaaa Have fun!
print "importing quests: 614: Slay the enemy commander"
import sys
from net.sf.l2j.gameserver.model.quest import State
from net.sf.l2j.gameserver.model.quest import QuestState
from net.sf.l2j.gameserver.model.quest.jython import QuestJython as JQuest

#Items
HEAD_OF_TAYR_ID = 7241
TOTEM_OF_WISDOM_ID = 7230
MARK_OF_VARKA_ALLIANCE4_ID = 7224

#NPCs
ASHAS_VARKA_DURAI_ID = 31377

#RB to hunt
TAYR_ID = 25302

#MOSTERS NOT TO HUNT
VARKA_RECRUIT_ID = 21350
VARKA_FOOTMAN_ID = 21351
VARKA_SCOUT_ID = 21353
VARKA_HUNTER_ID = 21354
VARKA_SHAMAN_ID = 21355
VARKA_PRIEST_ID = 21356
VARKA_WARRIOR_ID = 21358
VARKA_MEDIUM_ID = 21360
VARKA_MAGUS_ID = 21361
VARKA_OFFICIER_ID = 21362
VARKA_COMMANDER_ID = 21369
VARKA_ELITE_GUARD_ID = 21370
VARKA_GREAT_MAGUS_ID = 21365
VARKA_GENERAL_ID = 21366
VARKA_GREAT_SEER_ID = 21368
VARKA_PROPHET_ID = 21373
VARKA_DISCIPLE_OF_PROPHET_ID = 21375
VARKA_PROPHET_GUARDS_ID = 21374
VARKA_HEAD_MAGUS_ID = 21371
VARKA_HEAD_GUARDS_ID = 21372



class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onEvent (self,event,st) :
    htmltext = event
    if event == "1" :
        st.set("cond","1")
        st.set("id","0")
        st.setState(STARTED)
        st.playSound("ItemSound.quest_accept")
        htmltext = "start.htm"
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
   if npcId == ASHAS_VARKA_DURAI_ID and int(st.get("cond"))==0 and int(st.get("onlyone"))==0 :
        if st.getPlayer().getLevel()>70 and st.getQuestItemsCount(MARK_OF_VARKA_ALLIANCE4_ID)==1 :
          htmltext = "8371-1.htm"
        else:
          htmltext = "8371-3.htm"
          return htmltext
          st.exitQuest(1)
   if npcId == ASHAS_VARKA_DURAI_ID and int(st.get("cond"))==2 and st.getQuestItemsCount(HEAD_OF_TAYR_ID)==1 :
       htmltext = "8371-2.htm"
       st.takeItems(HEAD_OF_TAYR_ID,1)
       st.giveItems(TOTEM_OF_VALOR_ID,1)
       st.setState(COMPLETED) 
       st.playSound("ItemSound.quest_finish")
       return htmltext
   if npcId == ASHAS_VARKA_DURAI_ID and int(st.get("cond"))>=1 and st.getQuestItemsCount(HEAD_OF_TAYR_ID) < 1 :
       htmltext = "8371-4.htm"
       return htmltext
   return htmltext

 def onKill (Self,npc,st):

   npcId = npc.getNpcId()
   Faction = npc.getFactionId()
   if npcId==TAYR_ID :
    if int(st.get("cond"))==1 :
      st.giveItems(HEAD_OF_TAYR_ID,1)
      st.set("cond","2")
      st.playSound("ItemSound.quest_middle")
   elif npcId==VARKA_RECRUIT_ID or npcId==VARKA_FOOTMAN_ID or npcId==VARKA_SCOUT_ID or npcId==VARKA_HUNTER_ID or npcId==VARKA_SHAMAN_ID or npcId==VARKA_PRIEST_ID or npcId==VARKA_WARRIOR_ID or npcId==VARKA_MEDIUM_ID or npcId==VARKA_MAGUS_ID or npcId==VARKA_OFFICIER_ID or npcId==VARKA_COMMANDER_ID or npcId==VARKA_ELITE_GUARD_ID or npcId==VARKA_GREAT_MAGUS_ID or npcId==VARKA_GENERAL_ID or npcId==VARKA_GREAT_SEER_ID or npcId==VARKA_PROPHET_ID or npcId==VARKA_DISCIPLE_OF_PROPHET_ID or npcId==VARKA_PROPHET_GUARDS_ID or npcId==VARKA_HEAD_MAGUS_ID or npcId==VARKA_HEAD_GUARDS_ID :
    if int(st.get("cond"))>=1 :
     st.set("cond","0")
     st.setState(STARTING)
   elif npcId==VARKA_RECRUIT_ID or npcId==VARKA_FOOTMAN_ID or npcId==VARKA_SCOUT_ID or npcId==VARKA_HUNTER_ID or npcId==VARKA_SHAMAN_ID or npcId==VARKA_PRIEST_ID or npcId==VARKA_WARRIOR_ID or npcId==VARKA_MEDIUM_ID or npcId==VARKA_MAGUS_ID or npcId==VARKA_OFFICIER_ID or npcId==VARKA_COMMANDER_ID or npcId==VARKA_ELITE_GUARD_ID or npcId==VARKA_GREAT_MAGUS_ID or npcId==VARKA_GENERAL_ID or npcId==VARKA_GREAT_SEER_ID or npcId==VARKA_PROPHET_ID or npcId==VARKA_DISCIPLE_OF_PROPHET_ID or npcId==VARKA_PROPHET_GUARDS_ID or npcId==VARKA_HEAD_MAGUS_ID or npcId==VARKA_HEAD_GUARDS_ID and st.getQuestItemsCount(HEAD_OF_TAYR_ID)==1 :
    if int(st.get("cond"))>=1 :
     st.set("cond","0")
     st.setState(STARTING)
     st.takeItems(HEAD_OF_TAYR_ID,1)
   return 

QUEST       = Quest(614,"614_SlayTheEnemyCommander","Slay the enemy commander")
CREATED     = State('Start', QUEST)
STARTING     = State('Starting', QUEST)
STARTED     = State('Started', QUEST)
COMPLETED   = State('Completed', QUEST)


QUEST.setInitialState(CREATED)
QUEST.addStartNpc(ASHAS_VARKA_DURAI_ID)

STARTING.addTalkId(ASHAS_VARKA_DURAI_ID)

STARTED.addTalkId(ASHAS_VARKA_DURAI_ID)

#hunt for soldier
STARTED.addKillId(TAYR_ID) 

#MONSTERS NOT TO HUNT
STARTING.addKillId(VARKA_RECRUIT_ID) 
STARTING.addKillId(VARKA_FOOTMAN_ID)
STARTING.addKillId(VARKA_SCOUT_ID)
STARTING.addKillId(VARKA_HUNTER_ID)
STARTING.addKillId(VARKA_SHAMAN_ID)
STARTING.addKillId(VARKA_PRIEST_ID)
STARTING.addKillId(VARKA_WARRIOR_ID)
STARTING.addKillId(VARKA_MEDIUM_ID)
STARTING.addKillId(VARKA_MAGUS_ID)
STARTING.addKillId(VARKA_OFFICIER_ID)
STARTING.addKillId(VARKA_COMMANDER_ID)
STARTING.addKillId(VARKA_ELITE_GUARD_ID)
STARTING.addKillId(VARKA_GREAT_MAGUS_ID)
STARTING.addKillId(VARKA_GENERAL_ID)
STARTING.addKillId(VARKA_GREAT_SEER_ID)
STARTING.addKillId(VARKA_PROPHET_ID)
STARTING.addKillId(VARKA_DISCIPLE_OF_PROPHET_ID)
STARTING.addKillId(VARKA_PROPHET_GUARDS_ID)
STARTING.addKillId(VARKA_HEAD_MAGUS_ID)
STARTING.addKillId(VARKA_HEAD_GUARDS_ID)
STARTED.addKillId(VARKA_RECRUIT_ID) 
STARTED.addKillId(VARKA_FOOTMAN_ID)
STARTED.addKillId(VARKA_SCOUT_ID)
STARTED.addKillId(VARKA_HUNTER_ID)
STARTED.addKillId(VARKA_SHAMAN_ID)
STARTED.addKillId(VARKA_PRIEST_ID)
STARTED.addKillId(VARKA_WARRIOR_ID)
STARTED.addKillId(VARKA_MEDIUM_ID)
STARTED.addKillId(VARKA_MAGUS_ID)
STARTED.addKillId(VARKA_OFFICIER_ID)
STARTED.addKillId(VARKA_COMMANDER_ID)
STARTED.addKillId(VARKA_ELITE_GUARD_ID)
STARTED.addKillId(VARKA_GREAT_MAGUS_ID)
STARTED.addKillId(VARKA_GENERAL_ID)
STARTED.addKillId(VARKA_GREAT_SEER_ID)
STARTED.addKillId(VARKA_PROPHET_ID)
STARTED.addKillId(VARKA_DISCIPLE_OF_PROPHET_ID)
STARTED.addKillId(VARKA_PROPHET_GUARDS_ID)
STARTED.addKillId(VARKA_HEAD_MAGUS_ID)
STARTED.addKillId(VARKA_HEAD_GUARDS_ID)

#hunt for soldier
STARTED.addQuestDrop(TAYR_ID,HEAD_OF_TAYR_ID,1) 
STARTED.addQuestDrop(ASHAS_VARKA_DURAI_ID,TOTEM_OF_WISDOM_ID,1) 
