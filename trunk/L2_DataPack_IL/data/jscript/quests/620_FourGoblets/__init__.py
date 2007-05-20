import sys

from net.sf.l2j.gameserver.model.quest          import State
from net.sf.l2j.gameserver.model.quest          import QuestState
from net.sf.l2j.gameserver.model.quest.jython   import QuestJython as JQuest
from net.sf.l2j.gameserver.instancemanager      import FourSepulchersManager

qn = "620_FourGoblets"

#NPC
NPC_1 = 31453                           
NPC_2 = [31452,31454]                   
NPC_3 = [31921,31922,31923,31924]       
NPC_4 = 31919                           
NPC_5 = 31920                           


#ITEMS
ITEM_1 = [7075,7261]                    
ITEM_2 = [7256,7257,7258,7259]          
ITEM_3 = 7255                           
#7260   
#7261   

#REWARD
REWARD_1 = 7262                         
REWARD_2 = [57,81,151,959,1895,2500,4040,4042,4043,5529,5545,5546]  

class Quest (JQuest) :

  def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

  def onTalk (Self,npc,player) :
    st = player.getQuestState(qn)
    id = st.getState()

    if id == CREATED :
      st.set("cond","0")

    npcId = npc.getNpcId()

    if npcId == NPC_1:
      if int(st.get("cond")) == 0 :
        if st.getPlayer().getLevel() >= 74 :
          st.setState(STARTED)
          st.playSound("ItemSound.quest_accept")
          htmltext = "31453-SOK.htm"
          st.set("cond","1")
        else :
          htmltext = "31453-SNG.htm"
          st.exitQuest(1)
      elif int(st.get("cond")) == 1 :
        if st.getQuestItemsCount(ITEM_2[0]) >= 1 and st.getQuestItemsCount(ITEM_2[1]) >= 1 and st.getQuestItemsCount(ITEM_2[2]) >= 1 and st.getQuestItemsCount(ITEM_2[3]) >= 1 :
          htmltext = "31453-FOK.htm"
        else :
          htmltext = "31453-FNG.htm"
      elif int(st.get("cond")) == 2 :
        htmltext = "<html><body>not inplemented yet.</body></html>"

    elif npcId == NPC_2[0] :
      htmltext = "31452.htm"

    elif npcId == NPC_2[1] :
      htmltext = "31454-1.htm"

    elif npcId == NPC_3[0] :
      if FourSepulchersManager.getInstance().IsEntryTime() :
        if FourSepulchersManager.getInstance().IsEnableEntry(npcId,st.getPlayer()) :
          htmltext = "31921-OK.htm"
        else :
          htmltext = "31921-NG2.htm"
      else :
        htmltext = "31921-NG1.htm"

    elif npcId == NPC_3[1] :
      if FourSepulchersManager.getInstance().IsEntryTime() :
        if FourSepulchersManager.getInstance().IsEnableEntry(npcId,st.getPlayer()) :
          htmltext = "31922-OK.htm"
        else :
          htmltext = "31922-NG2.htm"
      else :
        htmltext = "31922-NG1.htm"

    elif npcId == NPC_3[2] :
      if FourSepulchersManager.getInstance().IsEntryTime() :
        if FourSepulchersManager.getInstance().IsEnableEntry(npcId,st.getPlayer()) :
          htmltext = "31923-OK.htm"
        else :
          htmltext = "31923-NG2.htm"
      else :
        htmltext = "31923-NG1.htm"

    elif npcId == NPC_3[3] :
      if FourSepulchersManager.getInstance().IsEntryTime() :
        if FourSepulchersManager.getInstance().IsEnableEntry(npcId,st.getPlayer()) :
          htmltext = "31924-OK.htm"
        else :
          htmltext = "31924-NG2.htm"
      else :
        htmltext = "31924-NG1.htm"

    elif npcId == NPC_4 :
      htmltext = "31919.htm"

    elif npcId == NPC_5 :
      htmltext = "31920.htm"

    return htmltext

  def onKill (self,npc,player) :
    st = player.getQuestState(qn)
    npcId = npc.getNpcId()
    if st:
      if int(st.get("cond")) == 1 or int(st.get("cond")) == 2 :
        if npcId in range(18120,18256) :
          if st.getRandom(100) < 30 :
            st.giveItems(ITEM_3,1)
            st.playSound("ItemSound.quest_itemget")
      return

  def onEvent (self,event,st) :
    if event == "11" :
      if st.getQuestItemsCount(ITEM_3) >= 1 :
        htmltext = "31454-1.htm"
        st.takeItems(ITEM_3,1)
        if st.getRandom(1000000) < 700000 :
          cnt = 1370 + st.getRandom(1374)
          st.giveItems(REWARD_2[0],cnt)

        if st.getRandom(1000000) < 2 :
          st.giveItems(REWARD_2[1],1)

        if st.getRandom(1000000) < 2 :
          st.giveItems(REWARD_2[2],1)

        if st.getRandom(1000000) < 8 :
          st.giveItems(REWARD_2[3],1)

        if st.getRandom(1000000) < 54858 :
          st.giveItems(REWARD_2[4],1)

        if st.getRandom(1000000) < 2 :
          st.giveItems(REWARD_2[5],1)

        if st.getRandom(1000000) < 3841 :
          st.giveItems(REWARD_2[6],1)

        if st.getRandom(1000000) < 3201 :
          st.giveItems(REWARD_2[7],1)

        if st.getRandom(1000000) < 6401 :
          st.giveItems(REWARD_2[8],1)

        if st.getRandom(1000000) < 440 :
          st.giveItems(REWARD_2[9],1)

        if st.getRandom(1000000) < 440 :
          st.giveItems(REWARD_2[10],1)

        if st.getRandom(1000000) < 483 :
          st.giveItems(REWARD_2[11],1)

      else :
        htmltext = "31454-NG.htm"

    elif event == "12" :
      st.takeItems(ITEM_2[0],1)
      st.takeItems(ITEM_2[1],1)
      st.takeItems(ITEM_2[2],1)
      st.takeItems(ITEM_2[3],1)
      st.giveItems(REWARD_1,1)
      st.set("cond","2")
      st.playSound("ItemSound.quest_finish")
      htmltext = "31453-22.htm"

    elif event == "13" :
      st.playSound("ItemSound.quest_accept")
      st.exitQuest(1)
      htmltext = "END.htm"

    elif event == "14" :
      st.playSound("ItemSound.quest_accept")
      htmltext = "CONTINUE.htm"

    elif event == "15" :
      if st.getQuestItemsCount(ITEM_1[1]) >= 1 :
        st.takeItems(ITEM_1[1],1)
        st.getPlayer().teleToLocation(178298,-84574,-7216)
        return
      elif st.getQuestItemsCount(REWARD_1) >= 1 :
        st.getPlayer().teleToLocation(178298,-84574,-7216)
        return
      else :
        htmltext = "NG.htm"

    elif event == "16" :
      if st.getQuestItemsCount(ITEM_1[1]) >= 1 :
        st.takeItems(ITEM_1[1],1)
        st.getPlayer().teleToLocation(186942,-75602,-2834)
        return
      elif st.getQuestItemsCount(REWARD_1) >= 1 :
        st.getPlayer().teleToLocation(186942,-75602,-2834)
        return
      else :
        htmltext = "NG.htm"

    elif event == "17" :
      if st.getQuestItemsCount(ITEM_1[1]) >= 1 :
        st.getPlayer().teleToLocation(169590,-90218,-2914)
        return
      elif st.getQuestItemsCount(REWARD_1) >= 1 :
        st.getPlayer().teleToLocation(169590,-90218,-2914)
        return
      else :
        htmltext = "NG.htm"

    return htmltext


QUEST       = Quest(620,"620_FourGoblets","Four Goblets")
CREATED     = State('Start', QUEST)
STARTED     = State('Started', QUEST)
COMPLETED   = State('Completed', QUEST)

QUEST.setInitialState(CREATED)
QUEST.addStartNpc(NPC_1) 

QUEST.addTalkId(NPC_1) 

for npcTalkId in [31452,31454,31921,31922,31923,31924,31919,31920] :
  QUEST.addTalkId(npcTalkId)

for npcKillId in range(18120,18256) :
  QUEST.addKillId(npcKillId)

print "importing quests: 620: Four Goblets" 
