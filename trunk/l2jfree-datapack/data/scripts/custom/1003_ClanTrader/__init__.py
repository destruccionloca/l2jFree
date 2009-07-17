# Script by Psychokiller1888 / L2jFree
import sys
from com.l2jfree.gameserver.model.quest import State
from com.l2jfree.gameserver.model.quest import QuestState
from com.l2jfree.gameserver.model.quest.jython import QuestJython as JQuest

qn = "1003_ClanTrader"

class Quest (JQuest) :

    def __init__(self,id,name,descr):
        JQuest.__init__(self,id,name,descr)

    def onAdvEvent(self,event,npc,player):
        htmltext = event
        return htmltext

    def onFirstTalk(self,npc,player):
        npcId = npc.getNpcId()
        if npcId == 32024:
            if player.isClanLeader():
                htmltext = "32024.htm"
            else:
                htmltext = "32024-no.htm"
        elif npcId == 32025:
            if player.isClanLeader():
                htmltext = "32025.htm"
            else:
                htmltext = "32025-no.htm"
        return htmltext

QUEST = Quest(-1, qn, "custom")

QUEST.addTalkId(32024)
QUEST.addTalkId(32025)
QUEST.addFirstTalkId(32024)
QUEST.addFirstTalkId(32025)
