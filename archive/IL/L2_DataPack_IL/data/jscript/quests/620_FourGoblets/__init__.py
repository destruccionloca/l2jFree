import sys

from net.sf.l2j.gameserver.model.quest          import State
from net.sf.l2j.gameserver.model.quest          import QuestState
from net.sf.l2j.gameserver.model.quest.jython   import QuestJython as JQuest
from net.sf.l2j.gameserver.instancemanager      import FourSepulchersManager

qn = "620_FourGoblets"

#NPC
NAMELESS_SPIRIT = 31453

GHOST_OF_WIGOTH_1 = 31452
GHOST_OF_WIGOTH_2 = 31454

CONQ_SM = 31921
EMPER_SM = 31922
SAGES_SM = 31923
JUDGE_SM = 31924

GHOST_CHAMBERLAIN_1 = 31919
GHOST_CHAMBERLAIN_2 = 31920

#ITEMS
ENTRANCE_PASS = 7075
GRAVE_PASS = 7261
GOBLETS = [7256,7257,7258,7259]
SEALED_BOX = 7255

#REWARDS
ANTIQUE_BROOCH = 7262
REWARDS = [57,81,151,959,1895,2500,4040,4042,4043,5529,5545,5546]

class Quest (JQuest) :

  def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

  def onTalk (Self,npc,player) :
    st = player.getQuestState(qn)
    id = st.getState()

    if id == CREATED :
      st.set("cond","0")

    npcId = npc.getNpcId()

    if npcId == NAMELESS_SPIRIT:
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
        if st.getQuestItemsCount(GOBLETS[0]) >= 1 and st.getQuestItemsCount(GOBLETS[1]) >= 1 and st.getQuestItemsCount(GOBLETS[2]) >= 1 and st.getQuestItemsCount(GOBLETS[3]) >= 1 :
          htmltext = "31453-FOK.htm"
        else :
          htmltext = "31453-FNG.htm"
      elif int(st.get("cond")) == 2 :
        htmltext = "31453-TELE.htm"

    elif npcId == GHOST_OF_WIGOTH_1 :
      htmltext = "31452.htm"

    elif npcId == GHOST_OF_WIGOTH_2 :
      htmltext = "31454-1.htm"

    elif npcId == CONQ_SM :
      htmltext = "31921-E.htm"
    elif npcId == EMPER_SM :
      htmltext = "31922-E.htm"
    elif npcId == SAGES_SM :
      htmltext = "31923-E.htm"
    elif npcId == JUDGE_SM :
      htmltext = "31924-E.htm"

    elif npcId == GHOST_CHAMBERLAIN_1 :
      htmltext = "31919.htm"

    elif npcId == GHOST_CHAMBERLAIN_2 :
      htmltext = "31920.htm"

    return htmltext

  def onKill (self,npc,player,isPet) :
    st = player.getQuestState(qn)
    npcId = npc.getNpcId()
    if st:
      if int(st.get("cond")) == 1 or int(st.get("cond")) == 2 :
        if npcId in range(18120,18256) :
          if st.getRandom(100) < 30 :
            st.giveItems(SEALED_BOX,1)
            st.playSound("ItemSound.quest_itemget")
      return

  def onAdvEvent (self,event,npc,player) :
    st = player.getQuestState(qn)

    if event == "Enter" : 
      FourSepulchersManager.getInstance().tryEntry(npc,player)
      return

    if not st : return

    elif event == "11" :
      if st.getQuestItemsCount(SEALED_BOX) >= 1 :
        htmltext = "31454-1.htm"
        st.takeItems(SEALED_BOX,1)
        if st.getRandom(1000000) < 700000 :
          cnt = 1370 + st.getRandom(1374)
          st.giveItems(REWARDS[0],cnt)

        if st.getRandom(1000000) < 2 :
          st.giveItems(REWARDS[1],1)

        if st.getRandom(1000000) < 2 :
          st.giveItems(REWARDS[2],1)

        if st.getRandom(1000000) < 8 :
          st.giveItems(REWARDS[3],1)

        if st.getRandom(1000000) < 54858 :
          st.giveItems(REWARDS[4],1)

        if st.getRandom(1000000) < 2 :
          st.giveItems(REWARDS[5],1)

        if st.getRandom(1000000) < 3841 :
          st.giveItems(REWARDS[6],1)

        if st.getRandom(1000000) < 3201 :
          st.giveItems(REWARDS[7],1)

        if st.getRandom(1000000) < 6401 :
          st.giveItems(REWARDS[8],1)

        if st.getRandom(1000000) < 440 :
          st.giveItems(REWARDS[9],1)

        if st.getRandom(1000000) < 440 :
          st.giveItems(REWARDS[10],1)

        if st.getRandom(1000000) < 483 :
          st.giveItems(REWARDS[11],1)

      else :
        return "31454-NG.htm"

    elif event == "12" :
      if st.getQuestItemsCount(GOBLETS[0]) >= 1 and st.getQuestItemsCount(GOBLETS[1]) >= 1 and st.getQuestItemsCount(GOBLETS[2]) >= 1 and st.getQuestItemsCount(GOBLETS[3]) >= 1 :
        st.takeItems(GOBLETS[0],1)
        st.takeItems(GOBLETS[1],1)
        st.takeItems(GOBLETS[2],1)
        st.takeItems(GOBLETS[3],1)
        st.giveItems(ANTIQUE_BROOCH,1)
        st.set("cond","2")
        st.playSound("ItemSound.quest_finish")
        return "31453-22.htm"
      else :
        return "31453-FNG.htm"

    elif event == "13" :
      st.playSound("ItemSound.quest_accept")
      st.exitQuest(1)
      return "END.htm"

    elif event == "14" :
      st.playSound("ItemSound.quest_accept")
      return "CONTINUE.htm"

    # Ghost Chamberlain of Elmoreden: Teleport to 4th sepulcher
    elif event == "15" :
      if st.getQuestItemsCount(ANTIQUE_BROOCH) >= 1 :
        st.getPlayer().teleToLocation(178298,-84574,-7216)
        return
      elif st.getQuestItemsCount(GRAVE_PASS) >= 1 :
        st.takeItems(GRAVE_PASS,1)
        st.getPlayer().teleToLocation(178298,-84574,-7216)
        return
      else :
        return "NG.htm"

    # Ghost Chamberlain of Elmoreden: Teleport to Imperial Tomb entrance
    elif event == "16" :
      if st.getQuestItemsCount(ANTIQUE_BROOCH) >= 1 :
        st.getPlayer().teleToLocation(186942,-75602,-2834)
        return
      elif st.getQuestItemsCount(GRAVE_PASS) >= 1 :
        st.takeItems(GRAVE_PASS,1)
        st.getPlayer().teleToLocation(186942,-75602,-2834)
        return
      else :
        return "NG.htm"

    # Teleport to Pilgrims Temple
    elif event == "17" :
      if st.getQuestItemsCount(ANTIQUE_BROOCH) >= 1 :
        st.getPlayer().teleToLocation(169590,-90218,-2914)
        return
      elif st.getQuestItemsCount(GRAVE_PASS) >= 1 :
        st.takeItems(GRAVE_PASS,1)
        st.getPlayer().teleToLocation(169590,-90218,-2914)
        return
      else :
        return "NG.htm"


QUEST       = Quest(620,"620_FourGoblets","quest")
CREATED     = State('Start', QUEST)
STARTED     = State('Started', QUEST)
COMPLETED   = State('Completed', QUEST)

QUEST.setInitialState(CREATED)
QUEST.addStartNpc(NAMELESS_SPIRIT) 
QUEST.addTalkId(NAMELESS_SPIRIT) 

for npcTalkId in [GHOST_OF_WIGOTH_1,GHOST_OF_WIGOTH_2,CONQ_SM,EMPER_SM,SAGES_SM,JUDGE_SM,GHOST_CHAMBERLAIN_1,GHOST_CHAMBERLAIN_2] :
  QUEST.addTalkId(npcTalkId)

for npcStartId in [CONQ_SM,EMPER_SM,SAGES_SM,JUDGE_SM,GHOST_CHAMBERLAIN_1,GHOST_CHAMBERLAIN_2] : 
  QUEST.addStartNpc(npcStartId)

for npcKillId in range(18120,18256) :
  QUEST.addKillId(npcKillId)
