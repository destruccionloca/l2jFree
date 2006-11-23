# Maked by Tomciaaa Have fun!
print "importing quests: 611: Alliance with Varka Sillenos"
import sys
from net.sf.l2j.gameserver.model.quest import State
from net.sf.l2j.gameserver.model.quest import QuestState
from net.sf.l2j.gameserver.model.quest.jython import QuestJython as JQuest

#Items
MARK_OF_VARKA_ALLIANCE1_ID = 7221
MARK_OF_VARKA_ALLIANCE2_ID = 7222
MARK_OF_VARKA_ALLIANCE3_ID = 7223
MARK_OF_VARKA_ALLIANCE4_ID = 7224
MARK_OF_VARKA_ALLIANCE5_ID = 7225
KB_SOLDIER_ID = 7226
KB_CAPTAIN_ID = 7227
KB_GENERAL_ID = 7228
TOTEM_OF_VALOR_ID = 7229
TOTEM_OF_WISDOM_ID = 7230

#NPCs
NARAN_ID = 31378
GLOCER_ID = 31380
WH_ID = 31381
SOUL_ID = 31379
TRADER_ID = 31382
GK_ID = 31383
BOX_ID = 31561

#hunt for soldier
RAIDER_ID = 21327
FOOTMAN_ID = 21324
SCOUT_ID = 21328
WAR_HOUND_ID = 21325
SHAMAN_ID = 21329

#hunt for captain
SEER_ID = 21338
WARRIOR_ID = 21331
LIEUTENANT_ID = 21332
ELITE_SOLDIER_ID = 21335
MEDIUM_ID = 21334
COMMAND_ID = 21343
ELITE_GUARD_ID = 21344
WHITE_CAPTAIN_ID = 21336

#hunt for general
BATTALION_COMMANDER_SOLDIER_ID = 21340
GENERAL_ID = 21339
GREAT_SEER_ID = 21342
VARKA_PROPHET_ID = 21347
PROPHET_GUARD_ID = 21348
PROPHET_AIDE_ID = 21349
HEAD_SHAMAN_ID = 21345
HEAD_GUARDS_ID = 21346

#MONSTERS NOT TO HUNT
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
   if npcId == NARAN_ID and int(st.get("cond"))==0 and int(st.get("onlyone"))==0 :
        if st.getPlayer().getLevel()>70 :
          htmltext = "8371-1.htm"
          return htmltext
        else:
          htmltext = "8371-0.htm"
          st.exitQuest(1)
   if npcId == NARAN_ID and int(st.get("cond"))==1 and st.getQuestItemsCount(KB_SOLDIER_ID) > 99 :
       htmltext = "8371-2.htm"
       st.takeItems(KB_SOLDIER_ID, 100)
       st.giveItems(MARK_OF_VARKA_ALLIANCE1_ID,1)
       st.playSound("ItemSound.quest_middle")
       st.set("cond","2")
       st.player.setVarka(1)
       return htmltext
   if npcId == NARAN_ID and int(st.get("cond"))==2 and st.getQuestItemsCount(KB_SOLDIER_ID) > 199 and st.getQuestItemsCount(KB_CAPTAIN_ID) > 99 and st.getQuestItemsCount(MARK_OF_VARKA_ALLIANCE1_ID)==1 :
       htmltext = "8371-3.htm"
       st.takeItems(KB_SOLDIER_ID, 200)
       st.takeItems(KB_CAPTAIN_ID, 100)
       st.takeItems(MARK_OF_VARKA_ALLIANCE1_ID,1)
       st.giveItems(MARK_OF_VARKA_ALLIANCE2_ID,1)
       st.playSound("ItemSound.quest_middle")
       st.set("cond","3")
       st.player.setVarka(2)
       return htmltext
   if npcId == NARAN_ID and int(st.get("cond"))==3 and st.getQuestItemsCount(KB_SOLDIER_ID) > 299 and st.getQuestItemsCount(KB_CAPTAIN_ID) > 199 and st.getQuestItemsCount(KB_GENERAL_ID) > 99 and st.getQuestItemsCount(MARK_OF_VARKA_ALLIANCE2_ID)==1 :
       htmltext = "8371-4.htm"
       st.takeItems(KB_SOLDIER_ID, 300)
       st.takeItems(KB_CAPTAIN_ID, 200)
       st.takeItems(KB_GENERAL_ID, 100)
       st.takeItems(MARK_OF_VARKA_ALLIANCE2_ID,1)
       st.giveItems(MARK_OF_VARKA_ALLIANCE3_ID,1)
       st.playSound("ItemSound.quest_middle")
       st.set("cond","4")
       st.player.setVarka(3)
       return htmltext
   if npcId == NARAN_ID and int(st.get("cond"))==4  and st.getQuestItemsCount(KB_SOLDIER_ID) > 299 and st.getQuestItemsCount(KB_CAPTAIN_ID) > 299 and st.getQuestItemsCount(KB_GENERAL_ID) > 199 and st.getQuestItemsCount(TOTEM_OF_VALOR_ID) == 1 and st.getQuestItemsCount(MARK_OF_VARKA_ALLIANCE3_ID)==1 :
       htmltext = "8371-5.htm"
       st.takeItems(KB_SOLDIER_ID, 300)
       st.takeItems(KB_CAPTAIN_ID, 300)
       st.takeItems(KB_GENERAL_ID, 200)
       st.takeItems(TOTEM_OF_VALOR_ID, 1)
       st.takeItems(MARK_OF_VARKA_ALLIANCE3_ID,1)
       st.giveItems(MARK_OF_VARKA_ALLIANCE4_ID,1)
       st.playSound("ItemSound.quest_middle")
       st.set("cond","5")
       st.player.setVarka(4)
       return htmltext
   if npcId == NARAN_ID and int(st.get("cond"))==5 and st.getQuestItemsCount(KB_SOLDIER_ID) > 399 and st.getQuestItemsCount(KB_CAPTAIN_ID) > 399 and st.getQuestItemsCount(KB_GENERAL_ID) > 199 and st.getQuestItemsCount(TOTEM_OF_WISDOM_ID) == 1 and st.getQuestItemsCount(MARK_OF_VARKA_ALLIANCE4_ID)==1 :
       htmltext = "8371-6.htm"
       st.takeItems(KB_SOLDIER_ID, 400)
       st.takeItems(KB_CAPTAIN_ID, 400)
       st.takeItems(KB_GENERAL_ID, 200)
       st.takeItems(TOTEM_OF_WISDOM_ID, 1)
       st.takeItems(MARK_OF_VARKA_ALLIANCE4_ID,1)
       st.giveItems(MARK_OF_VARKA_ALLIANCE5_ID,1)
       st.playSound("ItemSound.quest_middle")
       st.set("cond","6")
       st.player.setVarka(5)
       return htmltext
   if npcId == NARAN_ID and int(st.get("cond"))==6 :
       htmltext = "8371-7.htm"
       return htmltext
   if npcId == GLOCER_ID and int(st.get("cond"))==6 :
       htmltext = "8371-8.htm"
       return htmltext
   if npcId == WH_ID and int(st.get("cond"))==6 :
       htmltext = "8371-9.htm"
       return htmltext
   if npcId == SOUL_ID and int(st.get("cond"))==6 :
       htmltext = "8371-10.htm"
       return htmltext
   if npcId == TRADER_ID and int(st.get("cond"))==6 :
       htmltext = "8371-11.htm"
       return htmltext
   if npcId == GK_ID and int(st.get("cond"))==6 :
       htmltext = "8371-12.htm"
       return htmltext
   if npcId == BOX_ID and int(st.get("cond"))==6 and st.getQuestItemsCount(5580)==1 and st.getQuestItemsCount(5581) < 1 and st.getQuestItemsCount(5582) < 1 :
       htmltext = "8371-13.htm"
       st.takeItems(5580,1)
       st.giveItems(5908,1)
       st.playSound("ItemSound.quest_middle")
       return htmltext
   if npcId == BOX_ID and int(st.get("cond"))==6 and st.getQuestItemsCount(5581)==1 and st.getQuestItemsCount(5580) < 1 and st.getQuestItemsCount(5582) < 1 :
       htmltext = "8371-13.htm"
       st.takeItems(5581,1)
       st.giveItems(5911,1)
       st.playSound("ItemSound.quest_middle")
       return htmltext
   if npcId == BOX_ID and int(st.get("cond"))==6 and st.getQuestItemsCount(5582)==1 and st.getQuestItemsCount(5581) < 1 and st.getQuestItemsCount(5580) < 1 :
       htmltext = "8371-13.htm"
       st.takeItems(5582,1)
       st.giveItems(5914,1)
       st.playSound("ItemSound.quest_middle")
       return htmltext
   if npcId == BOX_ID and int(st.get("cond"))==6 and st.getQuestItemsCount(5580)==1 and st.getQuestItemsCount(5581)>=1 and st.getQuestItemsCount(5582)>=1 :
       htmltext = "8371-14.htm"
       return htmltext
   if npcId == BOX_ID and int(st.get("cond"))==6 and st.getQuestItemsCount(5581)==1 and st.getQuestItemsCount(5580)>=1 and st.getQuestItemsCount(5582)>=1 :
       htmltext = "8371-14.htm"
       return htmltext
   if npcId == BOX_ID and int(st.get("cond"))==6 and st.getQuestItemsCount(5582)==1 and st.getQuestItemsCount(5581)>=1 and st.getQuestItemsCount(5580)>=1 :
       htmltext = "8371-14.htm"
       return htmltext
   if npcId == BOX_ID and int(st.get("cond"))==6 and st.getQuestItemsCount(5580)==1 and st.getQuestItemsCount(5581)>=1 or st.getQuestItemsCount(5582)>=1 :
       htmltext = "8371-14.htm"
       return htmltext
   if npcId == BOX_ID and int(st.get("cond"))==6 and st.getQuestItemsCount(5581)==1 and st.getQuestItemsCount(5580)>=1 or st.getQuestItemsCount(5582)>=1 :
       htmltext = "8371-14.htm"
       return htmltext
   if npcId == BOX_ID and int(st.get("cond"))==6 and st.getQuestItemsCount(5582)==1 and st.getQuestItemsCount(5581)>=1 or st.getQuestItemsCount(5580)>=1 :
       htmltext = "8371-14.htm"
       return htmltext
   if npcId == BOX_ID and int(st.get("cond"))==6 and st.getQuestItemsCount(5582) < 1 and st.getQuestItemsCount(5581) < 1 and st.getQuestItemsCount(5580) < 1 :
       htmltext = "<html><head><body><br>You have no Stage 12 Soul Crystals in your inventory!</body></html>"
       return htmltext
   return htmltext

 def onKill (Self,npc,st):

   npcId = npc.getNpcId()
   Faction = npc.getFactionId()
   if npcId==SEER_ID or npcId==WARRIOR_ID or npcId==LIEUTENANT_ID or npcId==ELITE_SOLDIER_ID or npcId==MEDIUM_ID or npcId==COMMAND_ID or npcId==ELITE_GUARD_ID or npcId==WHITE_CAPTAIN_ID :
    if int(st.get("cond"))>1 :
      if st.getRandom(100) < 50 :
        st.giveItems(KB_CAPTAIN_ID,1)
        st.playSound("ItemSound.quest_middle")
   elif npcId==RAIDER_ID or npcId==FOOTMAN_ID or npcId==SCOUT_ID or npcId==WAR_HOUND_ID or npcId==SHAMAN_ID :
    if int(st.get("cond"))>0 :
      if st.getRandom(100) < 50 :
        st.giveItems(KB_SOLDIER_ID,1)
        st.playSound("ItemSound.quest_middle")
   elif npcId==BATTALION_COMMANDER_SOLDIER_ID or npcId==GENERAL_ID or npcId==GREAT_SEER_ID or npcId==VARKA_PROPHET_ID or npcId==PROPHET_GUARD_ID or npcId==PROPHET_AIDE_ID or npcId==HEAD_SHAMAN_ID or npcId==HEAD_GUARDS_ID :
    if int(st.get("cond"))>2 :
      if st.getRandom(100) < 50 :
        st.giveItems(KB_SOLDIER_ID,1)
        st.playSound("ItemSound.quest_middle")
   elif npcId==VARKA_RECRUIT_ID or npcId==VARKA_FOOTMAN_ID or npcId==VARKA_SCOUT_ID or npcId==VARKA_HUNTER_ID or npcId==VARKA_SHAMAN_ID or npcId==VARKA_PRIEST_ID or npcId==VARKA_WARRIOR_ID or npcId==VARKA_MEDIUM_ID or npcId==VARKA_MAGUS_ID or npcId==VARKA_OFFICIER_ID or npcId==VARKA_COMMANDER_ID or npcId==VARKA_ELITE_GUARD_ID or npcId==VARKA_GREAT_MAGUS_ID or npcId==VARKA_GENERAL_ID or npcId==VARKA_GREAT_SEER_ID or npcId==VARKA_PROPHET_ID or npcId==VARKA_DISCIPLE_OF_PROPHET_ID or npcId==VARKA_PROPHET_GUARDS_ID or npcId==VARKA_HEAD_MAGUS_ID or npcId==VARKA_HEAD_GUARDS_ID :
    if int(st.get("cond"))>1 :
     st.set("cond", str(st.getInt("cond")-1))
     if int(st.get("cond")) == 2 :
      st.takeItems(MARK_OF_VARKA_ALLIANCE2_ID,1)
      st.giveItems(MARK_OF_VARKA_ALLIANCE1_ID,1)
     if int(st.get("cond")) == 3 :
      st.takeItems(MARK_OF_VARKA_ALLIANCE3_ID,1)
      st.giveItems(MARK_OF_VARKA_ALLIANCE2_ID,1)
     if int(st.get("cond")) == 4 :
      st.takeItems(MARK_OF_VARKA_ALLIANCE4_ID,1)
      st.giveItems(MARK_OF_VARKA_ALLIANCE3_ID,1)
     if int(st.get("cond")) == 5 :
      st.takeItems(MARK_OF_VARKA_ALLIANCE5_ID,1)
      st.giveItems(MARK_OF_VARKA_ALLIANCE4_ID,1)
   return 

QUEST       = Quest(611,"611_AllianceWithVarkaSillenos","Alliance With The Varka Sillenos")
CREATED     = State('Start', QUEST)
STARTING     = State('Starting', QUEST)
STARTED     = State('Started', QUEST)
COMPLETED   = State('Completed', QUEST)


QUEST.setInitialState(CREATED)
QUEST.addStartNpc(NARAN_ID)

STARTING.addTalkId(NARAN_ID)

STARTED.addTalkId(NARAN_ID)
STARTED.addTalkId(GLOCER_ID)
STARTED.addTalkId(WH_ID)
STARTED.addTalkId(SOUL_ID)
STARTED.addTalkId(TRADER_ID)
STARTED.addTalkId(GK_ID)
STARTED.addTalkId(BOX_ID)

#hunt for soldier
STARTED.addKillId(RAIDER_ID) 
STARTED.addKillId(FOOTMAN_ID)
STARTED.addKillId(SCOUT_ID)
STARTED.addKillId(WAR_HOUND_ID)
STARTED.addKillId(SHAMAN_ID)

#hunt for captain
STARTED.addKillId(SEER_ID)
STARTED.addKillId(WARRIOR_ID)
STARTED.addKillId(LIEUTENANT_ID)
STARTED.addKillId(ELITE_SOLDIER_ID)
STARTED.addKillId(MEDIUM_ID)
STARTED.addKillId(COMMAND_ID)
STARTED.addKillId(ELITE_GUARD_ID)

#hunt for general
STARTED.addKillId(BATTALION_COMMANDER_SOLDIER_ID)
STARTED.addKillId(GENERAL_ID)
STARTED.addKillId(GREAT_SEER_ID)
STARTED.addKillId(VARKA_PROPHET_ID)
STARTED.addKillId(PROPHET_GUARD_ID)
STARTED.addKillId(PROPHET_AIDE_ID)
STARTED.addKillId(HEAD_SHAMAN_ID)
STARTED.addKillId(HEAD_GUARDS_ID)

#MONSTERS NOT TO HUNT
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
STARTED.addQuestDrop(RAIDER_ID,KB_SOLDIER_ID,1) 
STARTED.addQuestDrop(FOOTMAN_ID,KB_SOLDIER_ID,1)
STARTED.addQuestDrop(SCOUT_ID,KB_SOLDIER_ID,1)
STARTED.addQuestDrop(WAR_HOUND_ID,KB_SOLDIER_ID,1)
STARTED.addQuestDrop(SHAMAN_ID,KB_SOLDIER_ID,1)

#hunt for captain
STARTED.addQuestDrop(SEER_ID,KB_CAPTAIN_ID,1)
STARTED.addQuestDrop(WARRIOR_ID,KB_CAPTAIN_ID,1)
STARTED.addQuestDrop(LIEUTENANT_ID,KB_CAPTAIN_ID,1)
STARTED.addQuestDrop(ELITE_SOLDIER_ID,KB_CAPTAIN_ID,1)
STARTED.addQuestDrop(MEDIUM_ID,KB_CAPTAIN_ID,1)
STARTED.addQuestDrop(COMMAND_ID,KB_CAPTAIN_ID,1)
STARTED.addQuestDrop(ELITE_GUARD_ID,KB_CAPTAIN_ID,1)
STARTED.addQuestDrop(WHITE_CAPTAIN_ID,KB_CAPTAIN_ID,1)

#hunt for general
STARTED.addQuestDrop(BATTALION_COMMANDER_SOLDIER_ID,KB_GENERAL_ID,1)
STARTED.addQuestDrop(GENERAL_ID,KB_GENERAL_ID,1)
STARTED.addQuestDrop(GREAT_SEER_ID,KB_GENERAL_ID,1)
STARTED.addQuestDrop(VARKA_PROPHET_ID,KB_GENERAL_ID,1)
STARTED.addQuestDrop(PROPHET_GUARD_ID,KB_GENERAL_ID,1)
STARTED.addQuestDrop(PROPHET_AIDE_ID,KB_GENERAL_ID,1)
STARTED.addQuestDrop(HEAD_SHAMAN_ID,KB_GENERAL_ID,1)
STARTED.addQuestDrop(HEAD_GUARDS_ID,KB_GENERAL_ID,1)

STARTED.addQuestDrop(NARAN_ID,MARK_OF_VARKA_ALLIANCE1_ID,1)
STARTED.addQuestDrop(NARAN_ID,MARK_OF_VARKA_ALLIANCE2_ID,1)
STARTED.addQuestDrop(NARAN_ID,MARK_OF_VARKA_ALLIANCE3_ID,1)
STARTED.addQuestDrop(NARAN_ID,MARK_OF_VARKA_ALLIANCE4_ID,1)
STARTED.addQuestDrop(NARAN_ID,MARK_OF_VARKA_ALLIANCE5_ID,1)
