# Script by Psychokiller1888 / L2jFree

import sys
from com.l2jfree                               import Config
from com.l2jfree.gameserver.model.quest        import State
from com.l2jfree.gameserver.model.quest        import QuestState
from com.l2jfree.gameserver.model.quest.jython import QuestJython as JQuest


qn = "1003_DimensionalMerchants"

class Quest (JQuest) :

    def __init__(self,id,name,descr):
        JQuest.__init__(self,id,name,descr)

    def onAdvEvent(self,event,npc,player):
        htmltext = event
        return htmltext

    def onFirstTalk(self,npc,player):
        st = player.getQuestState(qn)
        if not st :
            st = self.newQuestState(player)
        if Config.ALT_ENABLE_DIMENSIONAL_MERCHANTS:
            htmltext = "32478.htm"
        else:
            htmltext = "32478-na.htm"
        return htmltext

QUEST = Quest(-1, qn, "custom")

QUEST.addStartNpc(32478)
QUEST.addTalkId(32478)
QUEST.addFirstTalkId(32478)
