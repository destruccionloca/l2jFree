# Made by Psychokiller1888

import sys
from com.l2jfree.gameserver.model.quest        import State
from com.l2jfree.gameserver.model.quest        import QuestState
from com.l2jfree.gameserver.model.quest.jython import QuestJython as JQuest

qn = "8005_Talismans"

KNIGHT_EPAULETTES = 9912
TALISMANS = [9956,9923,9959,9922,9957,9958,9955,9926,9930,9924,9932,9920,9927,9914,9919,9921,9915,10142,9951,10158,9950,10141,9954,9952,9953,9918,9931,9928,9917,9963,9964,9960,9966,9962,9961,9965,9938,9940,9935,9937,9936,9947,9944,9943,9942,9939,9945,9946,9949,9948,9933,9941,9934]

class Quest (JQuest) :

    def __init__(self, id, name, descr): 
        JQuest.__init__(self, id, name, descr)

    def onAdvEvent (self,event,npc,player):
        htmltext = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>"
        st = player.getQuestState(qn)
        if not st : return htmltext
        htmltext = event
        if event == "getTalisman":
            if st.getQuestItemsCount(KNIGHT_EPAULETTES) >= 10 :
                length = len(TALISMANS)
                pos = st.getRandom(length)
                talisman = TALISMANS[pos]
                st.takeItems(KNIGHT_EPAULETTES, 10)
                st.giveItems(talisman, 1)
                htmltext = "<html><body>Very well. Here you go!</body></html>"
            else :
                npcId = npc.getNpcId()
                if npcId >= 35648 and npcId <= 35656:
                    htmltext = st.showHtmlFile("no-KE.htm").replace("%LINKBACK%", "castlemagician/magician.htm")
                else:
                    htmltext = "no-KE-fort.htm"
            st.exitQuest(1)
        return htmltext

    def onTalk(self, npc, player):
        st = player.getQuestState(qn)
        if not st :
            st = self.newQuestState(player)
        htmltext = "talismans.htm"
        return htmltext

QUEST = Quest(-1, qn, "custom")

for npc in [35648,35649,35650,35651,35652,35653,35654,35655,35656,35662,35694,35731,35763,35800,35831,35863,35900,35932,35970,36007,36039,36114,36145,36177,36215,36253,36290,36322,36360]:
    QUEST.addStartNpc(npc)
    QUEST.addTalkId(npc)