import sys
from net.sf.l2j.gameserver.model.quest import State
from net.sf.l2j.gameserver.model.quest import QuestState
from net.sf.l2j.gameserver.model.quest.jython import QuestJython as JQuest

SMITHS = [7283,7298,7300,7317,7458,7471,7526,7527,7536,7621,7678,7688,7846,7898,8002,8044,8271,8274,8316,8539,8583,8626,8668]

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onEvent (self,event,st) :
    htmltext = event

# BW Gloves*BW Gloves Heavy
    if event == "1":
        if st.getQuestItemsCount(5718) >= 1:
            st.takeItems(5718,1)
            st.giveItems(2487,1)
            htmltext = "Item has been succesfully unsealed."
        else:
             htmltext = "You do not have enough materials."

# BW Gloves*BW Gloves Light
    if event == "2":
        if st.getQuestItemsCount(5719) >= 1:
            st.takeItems(5719,1)
            st.giveItems(2487,1)
            htmltext = "Item has been succesfully unsealed."
        else:
             htmltext = "You do not have enough materials."

# BW Gloves*BW Gloves Robe
    if event == "3":
        if st.getQuestItemsCount(5720) >= 1:
            st.takeItems(5720,1)
            st.giveItems(2487,1)
            htmltext = "Item has been succesfully unsealed."
        else:
             htmltext = "You do not have enough materials."

# Doom Gloves*Doom Gloves Heavy
    if event == "4":
        if st.getQuestItemsCount(5722) >= 1:
            st.takeItems(5722,1)
            st.giveItems(2475,1)
            htmltext = "Item has been succesfully unsealed."
        else:
             htmltext = "You do not have enough materials."

# Doom Gloves*Doom Gloves Light
    if event == "5":
         if st.getQuestItemsCount(5723) >= 1:
            st.takeItems(5723,1)
            st.giveItems(2475,1)
            htmltext = "Item has been succesfully unsealed."
         else:
             htmltext = "You do not have enough materials."

# Doom Gloves*Doom Gloves Robe
    if event == "6":
         if st.getQuestItemsCount(5724) >= 1:
            st.takeItems(5724,1)
            st.giveItems(2475,1)
            htmltext = "Item has been succesfully unsealed."
         else:
             htmltext = "You do not have enough materials."

# Zubei Gauntlets*Zubei Gauntlets Heavy
    if event == "7":
         if st.getQuestItemsCount(5710) >= 1:
            st.takeItems(5710,1)
            st.giveItems(612,1)
            htmltext = "Item has been succesfully unsealed."
         else:
             htmltext = "You do not have enough materials."

# Zubei Gauntlets*Zubei Gauntlets Light
    if event == "8":
         if st.getQuestItemsCount(5711) >= 1:
            st.takeItems(5711,1)
            st.giveItems(612,1)
            htmltext = "Item has been succesfully unsealed."
         else:
             htmltext = "You do not have enough materials."

# Zubei Gauntlets*Zubei Gauntlets Robe
    if event == "9":
         if st.getQuestItemsCount(5712) >= 1:
            st.takeItems(5712,1)
            st.giveItems(612,1)
            htmltext = "Item has been succesfully unsealed."
         else:
             htmltext = "You do not have enough materials."

# Avadon Gloves*Avadon Gloves Heavy
    if event == "10":
         if st.getQuestItemsCount(5714) >= 1:
            st.takeItems(5714,1)
            st.giveItems(2464,1)
            htmltext = "Item has been succesfully unsealed."
         else:
             htmltext = "You do not have enough materials."

# Avadon Gloves*Avadon Gloves Light
    if event == "11":
         if st.getQuestItemsCount(5715) >= 1:
            st.takeItems(5715,1)
            st.giveItems(2464,1)
            htmltext = "Item has been succesfully unsealed."
         else:
             htmltext = "You do not have enough materials."

# Avadon Gloves*Avadon Gloves Robe
    if event == "12":
         if st.getQuestItemsCount(5716) >= 1:
            st.takeItems(5716,1)
            st.giveItems(2464,1)
            htmltext = "Item has been succesfully unsealed."
         else:
             htmltext = "You do not have enough materials."

# BW Boots*BW Boots Heavy
    if event == "13":
        if st.getQuestItemsCount(5734) >= 1:
            st.takeItems(5734,1)
            st.giveItems(2439,1)
            htmltext = "Item has been succesfully unsealed."
        else:
             htmltext = "You do not have enough materials."

# BW Boots*BW Boots Light
    if event == "14":
        if st.getQuestItemsCount(5735) >= 1:
            st.takeItems(5735,1)
            st.giveItems(2439,1)
            htmltext = "Item has been succesfully unsealed."
        else:
             htmltext = "You do not have enough materials."

# BW Boots*BW Boots Robe
    if event == "15":
        if st.getQuestItemsCount(5736) >= 1:
            st.takeItems(5736,1)
            st.giveItems(2439,1)
            htmltext = "Item has been succesfully unsealed."
        else:
             htmltext = "You do not have enough materials."

# Doom Boots*Doom Boots Heavy
    if event == "16":
        if st.getQuestItemsCount(5738) >= 1:
            st.takeItems(5738,1)
            st.giveItems(601,1)
            htmltext = "Item has been succesfully unsealed."
        else:
             htmltext = "You do not have enough materials."

# Doom Boots*Doom Boots Light
    if event == "17":
         if st.getQuestItemsCount(5739) >= 1:
            st.takeItems(5739,1)
            st.giveItems(601,1)
            htmltext = "Item has been succesfully unsealed."
         else:
             htmltext = "You do not have enough materials."

# Doom Boots*Doom Boots Robe
    if event == "18":
         if st.getQuestItemsCount(5740) >= 1:
            st.takeItems(5740,1)
            st.giveItems(601,1)
            htmltext = "Item has been succesfully unsealed."
         else:
             htmltext = "You do not have enough materials."

# Zubei Boots*Zubei Boots Heavy
    if event == "19":
         if st.getQuestItemsCount(5726) >= 1:
            st.takeItems(5726,1)
            st.giveItems(554,1)
            htmltext = "Item has been succesfully unsealed."
         else:
             htmltext = "You do not have enough materials."

# Zubei Boots*Zubei Boots Light
    if event == "20":
         if st.getQuestItemsCount(5727) >= 1:
            st.takeItems(5727,1)
            st.giveItems(554,1)
            htmltext = "Item has been succesfully unsealed."
         else:
             htmltext = "You do not have enough materials."

# Zubei Boots*Zubei Boots Robe
    if event == "21":
         if st.getQuestItemsCount(5728) >= 1:
            st.takeItems(5728,1)
            st.giveItems(554,1)
            htmltext = "Item has been succesfully unsealed."
         else:
             htmltext = "You do not have enough materials."

# Avadon Boots*Avadon Boots Heavy
    if event == "22":
         if st.getQuestItemsCount(5730) >= 1:
            st.takeItems(5730,1)
            st.giveItems(600,1)
            htmltext = "Item has been succesfully unsealed."
         else:
             htmltext = "You do not have enough materials."

# Avadon Boots*Avadon Boots Light
    if event == "23":
         if st.getQuestItemsCount(5731) >= 1:
            st.takeItems(5731,1)
            st.giveItems(600,1)
            htmltext = "Item has been succesfully unsealed."
         else:
             htmltext = "You do not have enough materials."

# Avadon Boots*Avadon Boots Robe
    if event == "24":
         if st.getQuestItemsCount(5732) >= 1:
            st.takeItems(5732,1)
            st.giveItems(600,1)
            htmltext = "Item has been succesfully unsealed."
         else:
             htmltext = "You do not have enough materials."

    if event == "0":
      htmltext = "Trade has been canceled."
    
    if htmltext != event:
      st.setState(COMPLETED)
      st.exitQuest(1)

    return htmltext

 def onTalk (Self,npc,st):

   npcId = npc.getNpcId()
   htmltext = "<html><head><body>I have nothing to say to you.</body></html>"
   st.set("cond","0")
   st.setState(STARTED)
   return "1.htm"

QUEST       = Quest(1003,"1003_reseal","Blacksmith")
CREATED     = State('Start',     QUEST)
STARTED     = State('Started',   QUEST)
COMPLETED   = State('Completed', QUEST)

QUEST.setInitialState(CREATED)

for npcId in SMITHS :
    QUEST.addStartNpc(npcId)
    STARTED.addTalkId(npcId)
    
print "importing blacksmith data: 1003_reseal"
