# Created by L2Emu Team
import sys
from com.l2jfree.gameserver.model.quest        import State
from com.l2jfree.gameserver.model.quest        import QuestState
from com.l2jfree.gameserver.model.quest.jython import QuestJython as JQuest

qn = "8004_PurchaseBracelet"

Angel_Bracelet = 10320
Devil_Bracelet = 10326

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onEvent(self,event,st):
    htmltext = event
    if event == "Little_Angel" :
      if st.getQuestItemsCount(6471) >= 20 and st.getQuestItemsCount(5094) >= 50 and st.getQuestItemsCount(8160) >= 4 and st.getQuestItemsCount(8166) >= 5 and st.getQuestItemsCount(8169) >= 5 and st.getQuestItemsCount(8163) >= 3 and st.getQuestItemsCount(57) >= 7500000 :
        st.takeItems(6471,20)
        st.takeItems(5094,50)
        st.takeItems(8160,4)
        st.takeItems(8166,5)
        st.takeItems(8169,5)
        st.takeItems(8163,3)
        st.takeItems(57,7500000)
        st.giveItems(Angel_Bracelet,1)
        st.exitQuest(1)
      else :
        htmltext = "30098-no.htm"
        st.exitQuest(1)
    if event == "Little_Devil" :
      if st.getQuestItemsCount(6471) >= 20 and st.getQuestItemsCount(5094) >= 50 and st.getQuestItemsCount(8160) >= 4 and st.getQuestItemsCount(8166) >= 5 and st.getQuestItemsCount(8169) >= 5 and st.getQuestItemsCount(8163) >= 3 and st.getQuestItemsCount(57) >= 7500000 :
        st.takeItems(6471,20)
        st.takeItems(5094,50)
        st.takeItems(8160,4)
        st.takeItems(8166,5)
        st.takeItems(8169,5)
        st.takeItems(8163,3)
        st.takeItems(57,7500000)
        st.giveItems(Devil_Bracelet,1)
        st.exitQuest(1)
      else :
        htmltext = "30098-no.htm"
        st.exitQuest(1)
    return htmltext

 def onTalk(self,npc,player):
    htmltext = ""
    st = player.getQuestState(qn)
    if not st :
      st = self.newQuestState(player)
    htmltext = "30098.htm"
    return htmltext

QUEST = Quest(8004,qn,"custom")

QUEST.addStartNpc(30098)

QUEST.addTalkId(30098)