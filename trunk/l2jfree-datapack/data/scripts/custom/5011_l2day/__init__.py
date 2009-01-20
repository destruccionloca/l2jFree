# ----------------------------------------------------------------------------------
#	        Created by DJ_WEST. (C) Reallite Labs (www.reallite.cs2.ru)  1.11.2005
#	        Official idea - NCSoft. Version: 1.0
# ----------------------------------------------------------------------------------

# Settings
qn = "5011_l2day"
QuestId     = 5011
QuestName   = "l2day"
QuestDesc   = "custom"
InitialHtml = "1.htm"

# NPCs
BERYL_THE_CAT = 31774

# REWARDS : L2day Scrolls
GUIDANCE       = 3926
DEATH_WHISPER  = 3927
FOCUS          = 3928
GREATER_ACUMEN = 3929
EMPOWER        = 3932
WWALK          = 3934
SHIELD         = 3935
BSOE           = 3958
BRES           = 3959

# QUEST ITEMS : Letters to form words
A  = 3875
C  = 3876
E  = 3877
F  = 3878
G  = 3879
H  = 3880
I  = 3881
L  = 3882
N  = 3883
O  = 3884
R  = 3885
S  = 3886
T  = 3887
II = 3888

# Items - Format [name, giveItemId, giveItemQty, giveItem1Id, giveItem1Qty, giveItem2Id, giveItem2Qty, giveItem3Id, giveItem3Qty, giveItem4Id, giveItem4Qty, takeItem1Id, takeItem1Qty, takeItem2Id, takeItem2Qty, takeItem3Id, takeItem3Qty, takeItem4Id, takeItem4Qty, takeItem5Id, takeItem5Qty, takeItem6Id, takeItem6Qty, takeItem7Id, takeItem7Qty, takeItem8Id, takeItem8Qty, takeItem9Id, takeItem9Qty]
Items       = [
["LINEAGEII", BRES, 3, BSOE, 3, GREATER_ACUMEN, 3, EMPOWER, 3, L, 1, I, 1, N, 1, E, 2, A, 1, G, 1, E, 1, II, 1, C, 0],
["NCSOFT", BSOE, 1, BRES, 1, GUIDANCE, 1, DEATH_WHISPER, 1, N, 1, C, 1, S, 1, O, 1, F, 1, T, 1, A, 0, E, 0, G, 0],
["CHRONICLE", WWALK, 2, BRES, 2, BSOE, 2, FOCUS, 2, C, 2, H, 1, R, 1, O, 1, N, 1, I, 1, C, 1, L, 1, E, 1]
]

import sys
from com.l2jfree.gameserver.model.quest import State
from com.l2jfree.gameserver.model.quest import QuestState
from com.l2jfree.gameserver.model.quest.jython import QuestJython as JQuest

# doRequestedEvent
def do_RequestedEvent(event, st, giveItem1Id, giveItem1Qty, giveItem2Id, giveItem2Qty, giveItem3Id, giveItem3Qty, giveItem4Id, giveItem4Qty, takeItem1Id, takeItem1Qty, takeItem2Id, takeItem2Qty, takeItem3Id, takeItem3Qty, takeItem4Id, takeItem4Qty, takeItem5Id, takeItem5Qty, takeItem6Id, takeItem6Qty, takeItem7Id, takeItem7Qty, takeItem8Id, takeItem8Qty, takeItem9Id, takeItem9Qty) :
    if st.getQuestItemsCount(takeItem1Id) >= takeItem1Qty and st.getQuestItemsCount(takeItem2Id) >= takeItem2Qty and st.getQuestItemsCount(takeItem3Id) >= takeItem3Qty and st.getQuestItemsCount(takeItem4Id) >= takeItem4Qty and st.getQuestItemsCount(takeItem5Id) >= takeItem5Qty and st.getQuestItemsCount(takeItem6Id) >= takeItem6Qty and st.getQuestItemsCount(takeItem7Id) >= takeItem7Qty and st.getQuestItemsCount(takeItem8Id) >= takeItem8Qty and st.getQuestItemsCount(takeItem9Id) >= takeItem9Qty :
        st.takeItems(takeItem1Id, takeItem1Qty)
        st.takeItems(takeItem2Id, takeItem2Qty)
        st.takeItems(takeItem3Id, takeItem3Qty)
        st.takeItems(takeItem4Id, takeItem4Qty)
        st.takeItems(takeItem5Id, takeItem5Qty)
        st.takeItems(takeItem6Id, takeItem6Qty)
        st.takeItems(takeItem7Id, takeItem7Qty)
        st.takeItems(takeItem8Id, takeItem8Qty)
        st.takeItems(takeItem9Id, takeItem9Qty)
        st.giveItems(giveItem1Id, giveItem1Qty)
        st.giveItems(giveItem2Id, giveItem2Qty)
        st.giveItems(giveItem3Id, giveItem3Qty)
        st.giveItems(giveItem4Id, giveItem4Qty)
        return "2.htm"
    else :
        return "You do not have enough materials."

# main code
class Quest (JQuest) :

 def __init__(self,id,name,descr):
     JQuest.__init__(self,id,name,descr)

 def onEvent (self,event,st) :
    htmltext = event

    if event == "0":
        return InitialHtml

    for item in Items:
        if event == str(item[1]):
            htmltext = do_RequestedEvent(event, st, item[1], item[2], item[3], item[4], item[5], item[6], item[7], item[8], item[9], item[10], item[11], item[12], item[13], item[14], item[15], item[16], item[17], item[18], item[19], item[20], item[21], item[22], item[23], item[24], item[25], item[26])
    
    if htmltext != event:
      st.exitQuest(False) 
      st.exitQuest(1)

    return htmltext

 def onTalk (Self,npcId,player):
   st = player.getQuestState(qn)
   htmltext = "<html><body>I have nothing to say to you.</body></html>"
   st.set("cond","0")
   st.setState(State.STARTED)
   return InitialHtml

# Quest class and state definition
QUEST       = Quest(QuestId,str(QuestId) + "_" + QuestName,QuestDesc)

QUEST.addStartNpc(BERYL_THE_CAT)

QUEST.addTalkId(BERYL_THE_CAT)