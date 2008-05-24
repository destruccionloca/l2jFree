# By L2J_JP SANDMAN
import sys
from com.l2jfree.gameserver.model.quest import State
from com.l2jfree.gameserver.model.quest import QuestState
from com.l2jfree.gameserver.model.quest.jython import QuestJython as JQuest

qn = "654_JourneytoaSettlement"

#NPC
SPIRIT      = 31453     #Nameless Spirit

#TARGET
TARGET_1    = 21294     #Canyon Antelope
TARGET_2    = 21295     #Canyon Antelope Slave

#ITEM
ITEM        = 8072      #Antelope Skin

#REWARD
SCROLL      = 8073      #Frintezza's Magic Force Field Removal Scroll

class Quest (JQuest) :

  def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

  def onEvent (self,event,st) :
    htmltext = event
    if event == "31453-2.htm" :
      st.set("cond","1")
      st.setState(State.STARTED)
      st.playSound("ItemSound.quest_accept")
    if event == "31453-3.htm" :
      st.set("cond","2")
    elif event == "31453-5.htm" :
      st.giveItems(SCROLL,1)
      st.takeItems(ITEM,1)
      st.setState(State.COMPLETED)
      st.playSound("ItemSound.quest_finish")
      st.exitQuest(1)
    return htmltext

  def onTalk (Self,npc,player):
    st = player.getQuestState(qn)
    htmltext = "<html><body>Quest <font color=\"LEVEL\">Last Imperial Prince</font> is not accomplished or the condition is not suitable.</body></html>"
    if not st: return htmltext

    id = st.getState()
    if id == State.CREATED :
      st.set("cond","0")

    #confirm that quest can be executed.
    preQn = "119_LastImperialPrince"
    preSt = player.getQuestState(preQn)
    if not preSt: return htmltext
    preId = preSt.getState()
    if player.getLevel() < 74 :
      htmltext = "<html><body>Quest for characters level 74 and above.</body></html>"
      st.exitQuest(1)
      return htmltext
    elif preId != State.COMPLETED :
      htmltext = "<html><body>Quest <font color=\"LEVEL\">Last Imperial Prince</font> is not accomplished or the condition is not suitable.</body></html>"
      st.exitQuest(1)
      return htmltext

    cond = st.getInt("cond")
    npcId = npc.getNpcId()

    if npcId == SPIRIT :
      if cond == 0 :
        return "31453-1.htm"
      if cond == 1 :
        return "31453-2.htm"
      elif cond == 3 :
        return "31453-4.htm"
      else :
        htmltext = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>"
        return htmltext

  def onKill (self,npc,player,isPet) :
    st = player.getQuestState(qn)
    if not st: return
    npcId = npc.getNpcId()
    if int(st.getInt("cond")) == 2 and (npcId in range(TARGET_1,TARGET_2)) :
      if st.getRandom(100) < 5 :
        st.set("cond","3")
        st.giveItems(ITEM,1)
        st.playSound("ItemSound.quest_middle")
        return


QUEST = Quest(654,qn,"Journey to a Settlement")
QUEST.addStartNpc(SPIRIT)

QUEST.addTalkId(SPIRIT)
QUEST.addKillId(TARGET_1)
QUEST.addKillId(TARGET_2)