# Maked by Tomciaaa Have fun!
print "importing quests: 607: Prove your courage"
import sys
from net.sf.l2j.gameserver.model.quest import State
from net.sf.l2j.gameserver.model.quest import QuestState
from net.sf.l2j.gameserver.model.quest.jython import QuestJython as JQuest

#Items
HEAD_OF_SHADITH_ID = 7235
TOTEM_OF_VALOR_ID = 7219
MARK_OF_KETRA_ALLIANCE3_ID = 7213

#NPCs
KADAN_ZU_KETRA_ID = 8370

#RB to hunt
SHADITH_ID = 10309

#Monsters not to hunt
KETRA_RAIDER_ID = 1327
KETRA_FOOTMAN_ID = 1324
KETRA_SCOUT_ID = 1328
KETRA_WAR_HOUND_ID = 1325
KETRA_SHAMAN_ID = 1329
KETRA_SEER_ID = 1338
KETRA_WARRIOR_ID = 1331
KETRA_LIEUTENANT_ID = 1332
KETRA_ELITE_SOLDIER_ID = 1335
KETRA_MEDIUM_ID = 1334
KETRA_COMMAND_ID = 1343
KETRA_ELITE_GUARD_ID = 1344
KETRA_WHITE_CAPTAIN_ID = 1336
KETRA_BATTALION_COMMANDER_SOLDIER_ID = 1340
KETRA_GENERAL_ID = 1339
KETRA_GREAT_SEER_ID = 1342
KETRA_VARKA_PROPHET_ID = 1347
KETRA_PROPHET_GUARD_ID = 1348
KETRA_PROPHET_AIDE_ID = 1349
KETRA_HEAD_SHAMAN_ID = 1345
KETRA_HEAD_GUARDS_ID = 1346



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
   if npcId == KADAN_ZU_KETRA_ID and int(st.get("cond"))==0 and int(st.get("onlyone"))==0 :
        if st.getPlayer().getLevel()>70 and st.getQuestItemsCount(MARK_OF_KETRA_ALLIANCE3_ID)==1 :
          htmltext = "8371-1.htm"
        else:
          htmltext = "8371-3.htm"
          return htmltext
          st.exitQuest(1)
   if npcId == KADAN_ZU_KETRA_ID and int(st.get("cond"))==2 and st.getQuestItemsCount(HEAD_OF_SHADITH_ID)==1 :
       htmltext = "8371-2.htm"
       st.takeItems(HEAD_OF_SHADITH_ID, 1)
       st.giveItems(TOTEM_OF_VALOR_ID,1)
       st.setState(COMPLETED) 
       st.playSound("ItemSound.quest_finish")
       return htmltext
   if npcId == KADAN_ZU_KETRA_ID and int(st.get("cond"))>=1 and st.getQuestItemsCount(HEAD_OF_SHADITH_ID) < 1 :
       htmltext = "8371-4.htm"
       return htmltext
   return htmltext

 def onKill (Self,npc,st):

   npcId = npc.getNpcId()
   Faction = npc.getFactionId()
   if npcId==SHADITH_ID :
    if int(st.get("cond"))==1 :
      st.giveItems(HEAD_OF_SHADITH_ID,1)
      st.set("cond","2")
      st.playSound("ItemSound.quest_middle")
   elif npcId==KETRA_RAIDER_ID or npcId==KETRA_FOOTMAN_ID or npcId==KETRA_SCOUT_ID or npcId==KETRA_WAR_HOUND_ID or npcId==KETRA_SHAMAN_ID or npcId==KETRA_SEER_ID or npcId==KETRA_WARRIOR_ID or npcId==KETRA_LIEUTENANT_ID or npcId==KETRA_ELITE_SOLDIER_ID or npcId==KETRA_MEDIUM_ID or npcId==KETRA_COMMAND_ID or npcId==KETRA_ELITE_GUARD_ID or npcId==KETRA_WHITE_CAPTAIN_ID or npcId==KETRA_BATTALION_COMMANDER_SOLDIER_ID or npcId==KETRA_GENERAL_ID or npcId==KETRA_GREAT_SEER_ID or npcId==KETRA_VARKA_PROPHET_ID or npcId==KETRA_PROPHET_GUARD_ID or npcId==KETRA_PROPHET_AIDE_ID or npcId==KETRA_HEAD_SHAMAN_ID or npcId==KETRA_HEAD_GUARDS_ID : 
    if int(st.get("cond"))>=1 :
     st.set("cond","0")
     st.setState(STARTING)
   elif npcId==KETRA_RAIDER_ID or npcId==KETRA_FOOTMAN_ID or npcId==KETRA_SCOUT_ID or npcId==KETRA_WAR_HOUND_ID or npcId==KETRA_SHAMAN_ID or npcId==KETRA_SEER_ID or npcId==KETRA_WARRIOR_ID or npcId==KETRA_LIEUTENANT_ID or npcId==KETRA_ELITE_SOLDIER_ID or npcId==KETRA_MEDIUM_ID or npcId==KETRA_COMMAND_ID or npcId==KETRA_ELITE_GUARD_ID or npcId==KETRA_WHITE_CAPTAIN_ID or npcId==KETRA_BATTALION_COMMANDER_SOLDIER_ID or npcId==KETRA_GENERAL_ID or npcId==KETRA_GREAT_SEER_ID or npcId==KETRA_VARKA_PROPHET_ID or npcId==KETRA_PROPHET_GUARD_ID or npcId==KETRA_PROPHET_AIDE_ID or npcId==KETRA_HEAD_SHAMAN_ID or npcId==KETRA_HEAD_GUARDS_ID and st.getQuestItemsCount(HEAD_OF_SHADITH_ID)==1 : 
    if int(st.get("cond"))>=1 :
     st.set("cond","0")
     st.setState(STARTING)
     st.takeItems(HEAD_OF_SHADITH_ID,1)
   return 

QUEST       = Quest(607,"607_ProveYourCourage","Prove your courage")
CREATED     = State('Start', QUEST)
STARTING     = State('Starting', QUEST)
STARTED     = State('Started', QUEST)
COMPLETED   = State('Completed', QUEST)


QUEST.setInitialState(CREATED)
QUEST.addStartNpc(KADAN_ZU_KETRA_ID)

STARTING.addTalkId(KADAN_ZU_KETRA_ID)

STARTED.addTalkId(KADAN_ZU_KETRA_ID)

STARTED.addKillId(SHADITH_ID) 

#MOSTER NOT TO KILL
STARTING.addKillId(KETRA_RAIDER_ID) 
STARTING.addKillId(KETRA_FOOTMAN_ID)
STARTING.addKillId(KETRA_SCOUT_ID)
STARTING.addKillId(KETRA_WAR_HOUND_ID)
STARTING.addKillId(KETRA_SHAMAN_ID)
STARTING.addKillId(KETRA_SEER_ID)
STARTING.addKillId(KETRA_WARRIOR_ID)
STARTING.addKillId(KETRA_LIEUTENANT_ID)
STARTING.addKillId(KETRA_ELITE_SOLDIER_ID)
STARTING.addKillId(KETRA_MEDIUM_ID)
STARTING.addKillId(KETRA_COMMAND_ID)
STARTING.addKillId(KETRA_ELITE_GUARD_ID)
STARTING.addKillId(KETRA_BATTALION_COMMANDER_SOLDIER_ID)
STARTING.addKillId(KETRA_GENERAL_ID)
STARTING.addKillId(KETRA_GREAT_SEER_ID)
STARTING.addKillId(KETRA_VARKA_PROPHET_ID)
STARTING.addKillId(KETRA_PROPHET_GUARD_ID)
STARTING.addKillId(KETRA_PROPHET_AIDE_ID)
STARTING.addKillId(KETRA_HEAD_SHAMAN_ID)
STARTING.addKillId(KETRA_HEAD_GUARDS_ID)
STARTED.addKillId(KETRA_RAIDER_ID) 
STARTED.addKillId(KETRA_FOOTMAN_ID)
STARTED.addKillId(KETRA_SCOUT_ID)
STARTED.addKillId(KETRA_WAR_HOUND_ID)
STARTED.addKillId(KETRA_SHAMAN_ID)
STARTED.addKillId(KETRA_SEER_ID)
STARTED.addKillId(KETRA_WARRIOR_ID)
STARTED.addKillId(KETRA_LIEUTENANT_ID)
STARTED.addKillId(KETRA_ELITE_SOLDIER_ID)
STARTED.addKillId(KETRA_MEDIUM_ID)
STARTED.addKillId(KETRA_COMMAND_ID)
STARTED.addKillId(KETRA_ELITE_GUARD_ID)
STARTED.addKillId(KETRA_BATTALION_COMMANDER_SOLDIER_ID)
STARTED.addKillId(KETRA_GENERAL_ID)
STARTED.addKillId(KETRA_GREAT_SEER_ID)
STARTED.addKillId(KETRA_VARKA_PROPHET_ID)
STARTED.addKillId(KETRA_PROPHET_GUARD_ID)
STARTED.addKillId(KETRA_PROPHET_AIDE_ID)
STARTED.addKillId(KETRA_HEAD_SHAMAN_ID)
STARTED.addKillId(KETRA_HEAD_GUARDS_ID)

STARTED.addQuestDrop(SHADITH_ID,HEAD_OF_SHADITH_ID,1) 
STARTED.addQuestDrop(KADAN_ZU_KETRA_ID,TOTEM_OF_VALOR_ID,1) 
