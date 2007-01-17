# Maked by Tomciaaa Have fun!
print "importing quests: 605: Alliance with Ketra Orcs"
import sys
from net.sf.l2j.gameserver.model.quest import State
from net.sf.l2j.gameserver.model.quest import QuestState
from net.sf.l2j.gameserver.model.quest.jython import QuestJython as JQuest

#Items
MARK_OF_KETRA_ALLIANCE1_ID = 7211
MARK_OF_KETRA_ALLIANCE2_ID = 7212
MARK_OF_KETRA_ALLIANCE3_ID = 7213
MARK_OF_KETRA_ALLIANCE4_ID = 7214
MARK_OF_KETRA_ALLIANCE5_ID = 7215
VB_SOLDIER_ID = 7216
VB_CAPTAIN_ID = 7217
VB_GENERAL_ID = 7218
TOTEM_OF_VALOR_ID = 7219
TOTEM_OF_WISDOM_ID = 7220

#NPCs
WAHKAN_ID = 31371
GLOCER_ID = 31373
WH_ID = 31374
SOUL_ID = 31372
TRADER_ID = 31375
GK_ID = 31376
BOX_ID = 31559

#hunt for soldier
RECRUIT_ID = 21350
FOOTMAN_ID = 21351
SCOUT_ID = 21353
HUNTER_ID = 21354
SHAMAN_ID = 21355

#hunt for captain
PRIEST_ID = 21357
WARRIOR_ID = 21358
MEDIUM_ID = 21360
MAGUS_ID = 21361
OFFICIER_ID = 21362
COMMANDER_ID = 21369
ELITE_GUARD_ID = 21370

#hunt for general
GREAT_MAGUS_ID = 21365
GENERAL_ID = 21366
GREAT_SEER_ID = 21368
VARKA_PROPHET_ID = 21373
DISCIPLE_OF_PROPHET_ID = 21375
PROPHET_GUARDS_ID = 21374
HEAD_MAGUS_ID = 21371
HEAD_GUARDS_ID = 21372

#Monsters not to hunt
KETRA_RAIDER_ID = 21327
KETRA_FOOTMAN_ID = 21324
KETRA_SCOUT_ID = 21328
KETRA_WAR_HOUND_ID = 21325
KETRA_SHAMAN_ID = 21329
KETRA_SEER_ID = 21338
KETRA_WARRIOR_ID = 21331
KETRA_LIEUTENANT_ID = 21332
KETRA_ELITE_SOLDIER_ID = 21335
KETRA_MEDIUM_ID = 21334
KETRA_COMMAND_ID = 21343
KETRA_ELITE_GUARD_ID = 21344
KETRA_WHITE_CAPTAIN_ID = 21336
KETRA_BATTALION_COMMANDER_SOLDIER_ID = 21340
KETRA_GENERAL_ID = 21339
KETRA_GREAT_SEER_ID = 21342
KETRA_VARKA_PROPHET_ID = 21347
KETRA_PROPHET_GUARD_ID = 21348
KETRA_PROPHET_AIDE_ID = 21349
KETRA_HEAD_SHAMAN_ID = 21345
KETRA_HEAD_GUARDS_ID = 21346



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
   if npcId == WAHKAN_ID and str(st.get("cond"))=="0" and int(st.get("onlyone"))==0 :
        if st.getPlayer().getLevel()>70 :
          htmltext = "8371-1.htm"
          return htmltext
        else:
          htmltext = "8371-0.htm"
          st.exitQuest(1)
   if npcId == WAHKAN_ID and str(st.get("cond"))=="1" and st.getQuestItemsCount(VB_SOLDIER_ID) > 99 :
       htmltext = "8371-2.htm"
       st.takeItems(VB_SOLDIER_ID, 100)
       st.giveItems(MARK_OF_KETRA_ALLIANCE1_ID,1)
       st.playSound("ItemSound.quest_middle")
       st.set("cond","2")
       st.player.setKetra(1)
       return htmltext
   if npcId == WAHKAN_ID and str(st.get("cond"))=="2" and st.getQuestItemsCount(VB_SOLDIER_ID) > 199 and st.getQuestItemsCount(VB_CAPTAIN_ID) > 99 and st.getQuestItemsCount(MARK_OF_KETRA_ALLIANCE1_ID)==1 :
       htmltext = "8371-3.htm"
       st.takeItems(VB_SOLDIER_ID, 200)
       st.takeItems(VB_CAPTAIN_ID, 100)
       st.takeItems(MARK_OF_KETRA_ALLIANCE1_ID,1)
       st.giveItems(MARK_OF_KETRA_ALLIANCE2_ID,1)
       st.playSound("ItemSound.quest_middle")
       st.set("cond","3")
       st.player.setKetra(2)
       return htmltext
   if npcId == WAHKAN_ID and str(st.get("cond"))=="3" and st.getQuestItemsCount(VB_SOLDIER_ID) > 299 and st.getQuestItemsCount(VB_CAPTAIN_ID) > 199 and st.getQuestItemsCount(VB_GENERAL_ID) > 99 and st.getQuestItemsCount(MARK_OF_KETRA_ALLIANCE2_ID)==1 :
       htmltext = "8371-4.htm"
       st.takeItems(VB_SOLDIER_ID, 300)
       st.takeItems(VB_CAPTAIN_ID, 200)
       st.takeItems(VB_GENERAL_ID, 100)
       st.takeItems(MARK_OF_KETRA_ALLIANCE2_ID,1)
       st.giveItems(MARK_OF_KETRA_ALLIANCE3_ID,1)
       st.playSound("ItemSound.quest_middle")
       st.set("cond","4")
       st.player.setKetra(3)
       return htmltext
   if npcId == WAHKAN_ID and str(st.get("cond"))=="4"  and st.getQuestItemsCount(VB_SOLDIER_ID) > 299 and st.getQuestItemsCount(VB_CAPTAIN_ID) > 299 and st.getQuestItemsCount(VB_GENERAL_ID) > 199 and st.getQuestItemsCount(TOTEM_OF_VALOR_ID) == 1 and st.getQuestItemsCount(MARK_OF_KETRA_ALLIANCE3_ID)==1 :
       htmltext = "8371-5.htm"
       st.takeItems(VB_SOLDIER_ID, 300)
       st.takeItems(VB_CAPTAIN_ID, 300)
       st.takeItems(VB_GENERAL_ID, 200)
       st.takeItems(TOTEM_OF_VALOR_ID, 1)
       st.takeItems(MARK_OF_KETRA_ALLIANCE3_ID,1)
       st.giveItems(MARK_OF_KETRA_ALLIANCE4_ID,1)
       st.playSound("ItemSound.quest_middle")
       st.set("cond","5")
       st.player.setKetra(4)
       return htmltext
   if npcId == WAHKAN_ID and str(st.get("cond"))=="5" and st.getQuestItemsCount(VB_SOLDIER_ID) > 399 and st.getQuestItemsCount(VB_CAPTAIN_ID) > 399 and st.getQuestItemsCount(VB_GENERAL_ID) > 199 and st.getQuestItemsCount(TOTEM_OF_WISDOM_ID) == 1 and st.getQuestItemsCount(MARK_OF_KETRA_ALLIANCE4_ID)==1 :
       htmltext = "8371-6.htm"
       st.takeItems(VB_SOLDIER_ID, 400)
       st.takeItems(VB_CAPTAIN_ID, 400)
       st.takeItems(VB_GENERAL_ID, 200)
       st.takeItems(TOTEM_OF_WISDOM_ID,1)
       st.takeItems(MARK_OF_KETRA_ALLIANCE4_ID,1)
       st.giveItems(MARK_OF_KETRA_ALLIANCE5_ID,1)
       st.playSound("ItemSound.quest_middle")
       st.set("cond","6")
       st.player.setKetra(5)
       return htmltext
   if npcId == WAHKAN_ID and str(st.get("cond"))=="6":
       htmltext = "8371-7.htm"
       return htmltext
   if npcId == GLOCER_ID and str(st.get("cond"))=="6":
       htmltext = "8371-8.htm"
       return htmltext
   if npcId == WH_ID and str(st.get("cond"))=="6":
       htmltext = "8371-9.htm"
       return htmltext
   if npcId == SOUL_ID and str(st.get("cond"))=="6":
       htmltext = "8371-10.htm"
       return htmltext
   if npcId == TRADER_ID and str(st.get("cond"))=="6":
       htmltext = "8371-11.htm"
       return htmltext
   if npcId == GK_ID and str(st.get("cond"))=="6":
       htmltext = "8371-12.htm"
       return htmltext
   if npcId == BOX_ID and str(st.get("cond"))=="6" and st.getQuestItemsCount(5580)==1 and st.getQuestItemsCount(5581) < 1 and st.getQuestItemsCount(5582) < 1 :
       htmltext = "8371-13.htm"
       st.takeItems(5580,1)
       st.giveItems(5908,1)
       st.playSound("ItemSound.quest_middle")
       return htmltext
   if npcId == BOX_ID and str(st.get("cond"))=="6" and st.getQuestItemsCount(5581)==1 and st.getQuestItemsCount(5580) < 1 and st.getQuestItemsCount(5582) < 1 :
       htmltext = "8371-13.htm"
       st.takeItems(5581,1)
       st.giveItems(5911,1)
       st.playSound("ItemSound.quest_middle")
       return htmltext
   if npcId == BOX_ID and str(st.get("cond"))=="6" and st.getQuestItemsCount(5582)==1 and st.getQuestItemsCount(5581) < 1 and st.getQuestItemsCount(5580) < 1 :
       htmltext = "8371-13.htm"
       st.takeItems(5582,1)
       st.giveItems(5914,1)
       st.playSound("ItemSound.quest_middle")
       return htmltext
   if npcId == BOX_ID and str(st.get("cond"))=="6" and st.getQuestItemsCount(5580)==1 and st.getQuestItemsCount(5581)>=1 and st.getQuestItemsCount(5582)>=1 :
       htmltext = "8371-14.htm"
       return htmltext
   if npcId == BOX_ID and str(st.get("cond"))=="6" and st.getQuestItemsCount(5581)==1 and st.getQuestItemsCount(5580)>=1 and st.getQuestItemsCount(5582)>=1 :
       htmltext = "8371-14.htm"
       return htmltext
   if npcId == BOX_ID and str(st.get("cond"))=="6" and st.getQuestItemsCount(5582)==1 and st.getQuestItemsCount(5581)>=1 and st.getQuestItemsCount(5580)>=1 :
       htmltext = "8371-14.htm"
       return htmltext
   if npcId == BOX_ID and str(st.get("cond"))=="6" and st.getQuestItemsCount(5580)==1 and st.getQuestItemsCount(5581)>=1 or st.getQuestItemsCount(5582)>=1 :
       htmltext = "8371-14.htm"
       return htmltext
   if npcId == BOX_ID and str(st.get("cond"))=="6" and st.getQuestItemsCount(5581)==1 and st.getQuestItemsCount(5580)>=1 or st.getQuestItemsCount(5582)>=1 :
       htmltext = "8371-14.htm"
       return htmltext
   if npcId == BOX_ID and str(st.get("cond"))=="6" and st.getQuestItemsCount(5582)==1 and st.getQuestItemsCount(5581)>=1 or st.getQuestItemsCount(5580)>=1 :
       htmltext = "8371-14.htm"
       return htmltext
   if npcId == BOX_ID and str(st.get("cond"))=="6" and st.getQuestItemsCount(5582) < 1 and st.getQuestItemsCount(5581) < 1 and st.getQuestItemsCount(5580) < 1 :
       htmltext = "<html><head><body><br>You have no Stage 12 Soul Crystals in your inventory!</body></html>"
       return htmltext
   return htmltext

 def onKill (Self,npc,st):

   npcId = npc.getNpcId()
   Faction = npc.getFactionId()
   if npcId==PRIEST_ID or npcId==WARRIOR_ID or npcId==MEDIUM_ID or npcId==MAGUS_ID or npcId==OFFICIER_ID or npcId==COMMANDER_ID or npcId==ELITE_GUARD_ID :
    if int(st.get("cond"))>1 :
      if st.getRandom(100) < 50 :
        st.giveItems(VB_CAPTAIN_ID,1)
        st.playSound("ItemSound.quest_middle")
   elif npcId==RECRUIT_ID or npcId==FOOTMAN_ID or npcId==SCOUT_ID or npcId==HUNTER_ID or npcId==SHAMAN_ID :
    if int(st.get("cond"))>0 :
      if st.getRandom(100) < 50 :
        st.giveItems(VB_SOLDIER_ID,1)
        st.playSound("ItemSound.quest_middle")
   elif npcId==GREAT_MAGUS_ID or npcId==GENERAL_ID or npcId==GREAT_SEER_ID or npcId==VARKA_PROPHET_ID or npcId==DISCIPLE_OF_PROPHET_ID or npcId==PROPHET_GUARDS_ID or npcId==HEAD_MAGUS_ID or npcId==HEAD_GUARDS_ID :
    if int(st.get("cond"))>2 :
      if st.getRandom(100) < 50 :
        st.giveItems(VB_GENERAL_ID,1)
        st.playSound("ItemSound.quest_middle")
   elif npcId==KETRA_RAIDER_ID or npcId==KETRA_FOOTMAN_ID or npcId==KETRA_SCOUT_ID or npcId==KETRA_WAR_HOUND_ID or npcId==KETRA_SHAMAN_ID or npcId==KETRA_SEER_ID or npcId==KETRA_WARRIOR_ID or npcId==KETRA_LIEUTENANT_ID or npcId==KETRA_ELITE_SOLDIER_ID or npcId==KETRA_MEDIUM_ID or npcId==KETRA_COMMAND_ID or npcId==KETRA_ELITE_GUARD_ID or npcId==KETRA_WHITE_CAPTAIN_ID or npcId==KETRA_BATTALION_COMMANDER_SOLDIER_ID or npcId==KETRA_GENERAL_ID or npcId==KETRA_GREAT_SEER_ID or npcId==KETRA_VARKA_PROPHET_ID or npcId==KETRA_PROPHET_GUARD_ID or npcId==KETRA_PROPHET_AIDE_ID or npcId==KETRA_HEAD_SHAMAN_ID or npcId==KETRA_HEAD_GUARDS_ID : 
    if int(st.get("cond"))>1 :
     st.set("cond", str(st.getInt("cond")-1))
     if int(st.get("cond")) == 2 :
      st.takeItems(MARK_OF_KETRA_ALLIANCE2_ID,1)
      st.giveItems(MARK_OF_KETRA_ALLIANCE1_ID,1)
     if int(st.get("cond")) == 3 :
      st.takeItems(MARK_OF_KETRA_ALLIANCE3_ID,1)
      st.giveItems(MARK_OF_KETRA_ALLIANCE2_ID,1)
     if int(st.get("cond")) == 4 :
      st.takeItems(MARK_OF_KETRA_ALLIANCE4_ID,1)
      st.giveItems(MARK_OF_KETRA_ALLIANCE3_ID,1)
     if int(st.get("cond")) == 5 :
      st.takeItems(MARK_OF_KETRA_ALLIANCE5_ID,1)
      st.giveItems(MARK_OF_KETRA_ALLIANCE4_ID,1)
   return 

QUEST       = Quest(605,"605_AllianceWithKetraOrcs","Alliance With The Ketra Orcs")
CREATED     = State('Start', QUEST)
STARTING     = State('Starting', QUEST)
STARTED     = State('Started', QUEST)
COMPLETED   = State('Completed', QUEST)


QUEST.setInitialState(CREATED)
QUEST.addStartNpc(WAHKAN_ID)

STARTING.addTalkId(WAHKAN_ID)

STARTED.addTalkId(WAHKAN_ID)
STARTED.addTalkId(GLOCER_ID)
STARTED.addTalkId(WH_ID)
STARTED.addTalkId(SOUL_ID)
STARTED.addTalkId(TRADER_ID)
STARTED.addTalkId(GK_ID)
STARTED.addTalkId(BOX_ID)

#hunt for soldier
STARTED.addKillId(RECRUIT_ID) 
STARTED.addKillId(FOOTMAN_ID)
STARTED.addKillId(SCOUT_ID)
STARTED.addKillId(HUNTER_ID)
STARTED.addKillId(SHAMAN_ID)

#hunt for captain
STARTED.addKillId(PRIEST_ID)
STARTED.addKillId(WARRIOR_ID)
STARTED.addKillId(MEDIUM_ID)
STARTED.addKillId(MAGUS_ID)
STARTED.addKillId(OFFICIER_ID)
STARTED.addKillId(COMMANDER_ID)
STARTED.addKillId(ELITE_GUARD_ID)

#hunt for general
STARTED.addKillId(GREAT_MAGUS_ID)
STARTED.addKillId(GENERAL_ID)
STARTED.addKillId(GREAT_SEER_ID)
STARTED.addKillId(VARKA_PROPHET_ID)
STARTED.addKillId(DISCIPLE_OF_PROPHET_ID)
STARTED.addKillId(PROPHET_GUARDS_ID)
STARTED.addKillId(HEAD_MAGUS_ID)
STARTED.addKillId(HEAD_GUARDS_ID)

#MOSTER NOT TO KILL
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



#hunt for soldier
STARTED.addQuestDrop(RECRUIT_ID,VB_SOLDIER_ID,1) 
STARTED.addQuestDrop(FOOTMAN_ID,VB_SOLDIER_ID,1)
STARTED.addQuestDrop(SCOUT_ID,VB_SOLDIER_ID,1)
STARTED.addQuestDrop(HUNTER_ID,VB_SOLDIER_ID,1)
STARTED.addQuestDrop(SHAMAN_ID,VB_SOLDIER_ID,1)

#hunt for captain
STARTED.addQuestDrop(PRIEST_ID,VB_CAPTAIN_ID,1)
STARTED.addQuestDrop(WARRIOR_ID,VB_CAPTAIN_ID,1)
STARTED.addQuestDrop(MEDIUM_ID,VB_CAPTAIN_ID,1)
STARTED.addQuestDrop(MAGUS_ID,VB_CAPTAIN_ID,1)
STARTED.addQuestDrop(OFFICIER_ID,VB_CAPTAIN_ID,1)
STARTED.addQuestDrop(COMMANDER_ID,VB_CAPTAIN_ID,1)
STARTED.addQuestDrop(ELITE_GUARD_ID,VB_CAPTAIN_ID,1)

#hunt for general
STARTED.addQuestDrop(GREAT_MAGUS_ID,VB_GENERAL_ID,1)
STARTED.addQuestDrop(GENERAL_ID,VB_GENERAL_ID,1)
STARTED.addQuestDrop(GREAT_SEER_ID,VB_GENERAL_ID,1)
STARTED.addQuestDrop(VARKA_PROPHET_ID,VB_GENERAL_ID,1)
STARTED.addQuestDrop(DISCIPLE_OF_PROPHET_ID,VB_GENERAL_ID,1)
STARTED.addQuestDrop(PROPHET_GUARDS_ID,VB_GENERAL_ID,1)
STARTED.addQuestDrop(HEAD_MAGUS_ID,VB_GENERAL_ID,1)
STARTED.addQuestDrop(HEAD_GUARDS_ID,VB_GENERAL_ID,1)

STARTED.addQuestDrop(WAHKAN_ID,MARK_OF_KETRA_ALLIANCE1_ID,1)
STARTED.addQuestDrop(WAHKAN_ID,MARK_OF_KETRA_ALLIANCE2_ID,1)
STARTED.addQuestDrop(WAHKAN_ID,MARK_OF_KETRA_ALLIANCE3_ID,1)
STARTED.addQuestDrop(WAHKAN_ID,MARK_OF_KETRA_ALLIANCE4_ID,1)
STARTED.addQuestDrop(WAHKAN_ID,MARK_OF_KETRA_ALLIANCE5_ID,1)
