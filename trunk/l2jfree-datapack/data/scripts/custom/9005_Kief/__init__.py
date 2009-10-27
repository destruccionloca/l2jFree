import sys
from com.l2jfree.gameserver.model.quest import State
from com.l2jfree.gameserver.model.quest import QuestState
from com.l2jfree.gameserver.model.quest.jython import QuestJython as JQuest

qn = "9005_Kief"

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onTalk (self,npc,player):
    if player.getTrustLevel()>=300000:
        return "trade.htm"
    else:
        return "no.htm"



QUEST = Quest(9005,qn,"custom")

QUEST.addStartNpc(32354)
QUEST.addTalkId(32354)
