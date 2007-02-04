# Made by tomciaaa Have fun!
import sys
from net.sf.l2j.gameserver.model.quest import State
from net.sf.l2j.gameserver.model.quest import QuestState
from net.sf.l2j.gameserver.model.quest.jython import QuestJython as JQuest

#NPCs 
PINTER = 30298

#MOBs
MAILLE_SCOUT = 20920
MAILLE_GUARD = 20921
KING_OF_ARANEID = 20927

#ITEMS 
LIZARDMEN_BLOOD         = 8062
LEG_OF_KING_ARANEID     = 8063
 
#REWARD 
HELM	        =       7850
ARMOR	        =       7851
GAUNTLETS	=	7852
SABATON	        =       7853
BRIGANDINE	=	7854
LEATHER_GLOVER	=	7855
BOOTS	        =       7856
AKETON	        =       7857
PADDED_GLOVES	=	7858
SANDALS	        =       7859
 
class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onEvent (self,event,st) :
   htmltext = event
   if event == "start.htm" :
     st.set("cond","1")
     st.setState(STARTED)
     st.playSound("ItemSound.quest_accept")
#   elif event == "robe.htm" :
#     htmltext = "robe.htm"
#   elif event == "light.htm" :
#     htmltext = "light.htm"
#   elif event == "heavy.htm" :
#     htmltext = "heavy.htm"
   elif event == "getlight.htm" :
     if st.getQuestItemsCount(LIZARDMEN_BLOOD) >= 10 :
         st.set("cond","5");
         st.set("armor_type","light")
         st.takeItems(LIZARDMEN_BLOOD,10)
         st.playSound("ItemSound.quest_middle")
   elif event == "getrobe.htm" :
     if st.getQuestItemsCount(LIZARDMEN_BLOOD) >= 10 :
         st.set("cond","3")
         st.set("armor_type","robe")
         st.takeItems(LIZARDMEN_BLOOD,10)
         st.playSound("ItemSound.quest_middle")     
   elif event == "getheavy.htm" :
     if st.getQuestItemsCount(LIZARDMEN_BLOOD) >= 10 :
         st.set("cond","4");
         st.set("armor_type","heavy")
         st.takeItems(LIZARDMEN_BLOOD,10)
         st.playSound("ItemSound.quest_middle") 
     
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
 
   cond    = st.getInt("cond") 
   onlyone = st.getInt("onlyone")
   armor_type = st.get("armor_type")
 
   if npcId == PINTER and cond == 0 and st.getPlayer().getPledgeType() < 0 : 
     if onlyone == 1 : 
       htmltext = "<html><head><body>This quest have already been completed.</body></html>" 
     elif st.getPlayer().getLevel() >= 19 and not st.getPlayer().getApprentice() == "" : 
       htmltext = "30517-01.htm"
     else: 
       htmltext = "30517-02.htm" 
       st.exitQuest(1) 
   elif npcId == PINTER and cond == 1 : 
     htmltext = "30517-03.htm" 
   elif npcId == PINTER and cond == 2 :
     htmltext = "30517-04.htm"
   elif npcId == PINTER and cond == 6 :
     htmltext = "30517-07.htm"
     st.set("cond", "7")
   elif npcId == PINTER and cond == 8 : 
     htmltext = "30517-08.htm"
     st.takeItems(LEG_OF_KING_ARANEID,8)
     if armor_type == "heavy" :
         st.giveItems(HELM,1) 
         st.giveItems(BRIGANDINE,1)
         st.giveItems(LEATHER_GLOVER,1)
         st.giveItems(BOOTS,1)
     elif armor_type == "light" :
         st.giveItems(HELM,1) 
         st.giveItems(ARMOR,1)
         st.giveItems(GAUNTLETS,1)
         st.giveItems(SABATON,1)
     else :
         st.giveItems(HELM,1) 
         st.giveItems(AKETON,1)
         st.giveItems(PADDED_GLOVES,1)
         st.giveItems(SANDALS,1)
     st.set("cond","0") 
     st.set("onlyone","1") 
     st.setState(COMPLETED) 
     st.playSound("ItemSound.quest_finish")
   elif npcId == PINTER and cond == 0 and not st.getPlayer().getApprentice() == "" :
     if int(st.getFriendsQuestState(st.getPlayer().getApprentice(),"cond")) == 3:
        if st.getQuestItemsCount(C_CRYSTAL) >= 922 :
            st.takeItems(D_CRYSTAL,922)
            htmltext = "30517-06.htm"
            st.setFriendsQuestState(st.getPlayer().getApprentice(),"cond",6)
        else :
            htmltext = "<html><head><body>You have to bring me at least 922 crystals to pay for your apprentice's Armor.</body></html>"
        st.exitQuest(1)
     elif int(st.getFriendsQuestState(st.getPlayer().getApprentice(),"cond")) == 4 or int(st.getFriendsQuestState(st.getPlayer().getApprentice(),"cond")) == 5 :
        if st.getQuestItemsCount(C_CRYSTAL) >= 771 :
            st.takeItems(D_CRYSTAL,771)
            htmltext = "30517-06.htm"
            st.setFriendsQuestState(st.getPlayer().getApprentice(),"cond",6)
        else :
            htmltext = "<html><head><body>You have to bring me at least 711 crystals to pay for your apprentice's Armor.</body></html>"
        st.exitQuest(1)
   return htmltext

 def onKill (Self,npc,st):

    npcId = npc.getNpcId()
    cond    = st.getInt("cond")

    if npcId == MAILLE_SCOUT or npcId == MAILLE_GUARD and cond == 1 :
        if st.getRandom(100) < 70 :
            st.giveItems(LIZARDMEN_BLOOD,1)
            if st.getQuestItemsCount(LIZARDMEN_BLOOD) == 10 :
                st.set("cond","2")
                st.playSound("ItemSound.quest_middle")
            else :
                st.playSound("ItemSound.quest_itemget")
    elif npcd == KING_OF_ARANEID and cond == 7 and st.getPlayer().getDistanceToPlayer(st.getPlayer().getApprentice()) <= 2000: #check if sponsor is near the apprentice (value is totally custom)
        st.giveItems(LEG_OF_KING_ARANEID,1)
        if st.getQuestItemsCount(LEG_OF_KING_ARANEID) == 8 :
                st.set("cond","8")
                st.playSound("ItemSound.quest_middle")
        else :
                st.playSound("ItemSound.quest_itemget")
    return htmltext
     

QUEST     = Quest(118,"188_ToLeadAndBeLed","To Lead And Be Led") 
CREATED   = State('Start',     QUEST) 
STARTING  = State('Starting',  QUEST) 
STARTED   = State('Started',   QUEST) 
COMPLETED = State('Completed', QUEST) 

QUEST.setInitialState(CREATED)
QUEST.addStartNpc(PINTER) 

STARTING.addTalkId(PINTER) 

STARTED.addTalkId(PINTER)

STARTED.addKillId(KING_OF_ARANEID)
STARTED.addKillId(MAILLE_SCOUT)
STARTED.addKillId(MAILLE_GUARD)

STARTED.addQuestDrop(PINTER,HELM,1) 
STARTED.addQuestDrop(PINTER,GAUNTLETS,1) 
STARTED.addQuestDrop(PINTER,ARMOR,1) 
STARTED.addQuestDrop(PINTER,SABATON,1) 
STARTED.addQuestDrop(PINTER,BRIGANDINE,1) 
STARTED.addQuestDrop(PINTER,LEATHER_GLOVER,1)
STARTED.addQuestDrop(PINTER,PADDED_GLOVES,1)
STARTED.addQuestDrop(PINTER,AKETON,1)
STARTED.addQuestDrop(PINTER,BOOTS,1)
STARTED.addQuestDrop(PINTER,SANDALS,1)

STARTED.addQuestDrop(MAILLE_SCOUT,LIZARDMEN_BLOOD,1)
STARTED.addQuestDrop(MAILLE_GUARD,LIZARDMEN_BLOOD,1)
STARTED.addQuestDrop(KING_OF_ARANEID,LEG_OF_KING_ARANEID,1)

print "importing quests: 188: To Lead And Be Led" 
