# Made by Edge
import sys
from com.l2jfree import Config
from com.l2jfree.gameserver.model.quest import State
from com.l2jfree.gameserver.model.quest import QuestState
from com.l2jfree.gameserver.model.quest.jython import QuestJython as JQuest

qn = "646_SignsOfRevolt"

#NPC
TORRANT = 32016
#Item
CURSED_DOLL = 8087

class Quest (JQuest) :
 def __init__(self,id,name,descr):
    JQuest.__init__(self,id,name,descr)
    self.questItemIds = [CURSED_DOLL]

 def onTalk (self,npc,player):
   st = player.getQuestState(qn)
   if st :
      # Quest is no longer available
      st.unset("cond")
      st.exitQuest(1);
   return "32016-00.htm"

QUEST       = Quest(646, qn, "Signs of Revolt")

QUEST.addStartNpc(TORRANT)
QUEST.addTalkId(TORRANT)
