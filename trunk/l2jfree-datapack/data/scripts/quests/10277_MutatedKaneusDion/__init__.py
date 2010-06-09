# Made by LiveTeam modified by Daehak for L2jFree

import sys
from com.l2jfree.gameserver.model.quest        import State
from com.l2jfree.gameserver.model.quest        import QuestState
from com.l2jfree.gameserver.model.quest.jython import QuestJython as JQuest
from com.l2jfree.tools.random                  import Rnd

qn = "10277_MutatedKaneusDion"

#NPCs
Lucas = 30071
Mirien = 30461
CrimsonHatuOtis = 18558
SeerFlouros = 18559

#items
Tissue1 = 13832
Tissue2 = 13833

class Quest (JQuest) :
    def __init__(self,id,name,descr):
        JQuest.__init__(self,id,name,descr)
        self.questItemIds = [Tissue1,Tissue2]

    def onAdvEvent (self,event,npc, player) :
        htmltext = event
        st = player.getQuestState(qn)
        if not st : return
        if event == "30071-03.htm" :
            st.set("cond","1")
            st.setState(State.STARTED)
            st.playSound("ItemSound.quest_accept")
        elif event == "30461-02.htm" :
            st.giveItems(57,20000)
            st.unset("cond")
            st.exitQuest(False)
            st.playSound("ItemSound.quest_finish")
        return htmltext

    def onTalk (self,npc,player):
        htmltext = "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>"
        st = player.getQuestState(qn)
        if not st : return htmltext
        npcId = npc.getNpcId()
        id = st.getState()
        cond = st.getInt("cond")
        if id == State.COMPLETED :
            if npcId == Lucas :
                htmltext = "30071-0a.htm"
        elif id == State.CREATED and npcId == Lucas:
            if player.getLevel() >= 28 :
                htmltext = "30071-01.htm"
            else :
                htmltext = "30071-00.htm"
        else :
            if npcId == Lucas :
                if cond == 1:
                   htmltext = "30071-04.htm"
                elif cond == 2:
                   htmltext = "30071-05.htm"
            elif npcId == Mirien:
                if cond == 2:
                   htmltext = "30461-01.htm"
                else :
                   htmltext = "30461-01a.htm"
        return htmltext

    def onKill(self,npc,player,isPet):
        party = player.getParty()
        if party :
            PartyQuestMembers = []
            for player1 in party.getPartyMembers().toArray() :
                st1 = player1.getQuestState(qn)
                if st1 :
                    if st1.getState() == State.STARTED and st1.getInt("cond") == 1 :
                        PartyQuestMembers.append(st1)
            if len(PartyQuestMembers) == 0 : return
            st = PartyQuestMembers[Rnd.get(len(PartyQuestMembers))]
            st.giveItems(Tissue1,1)
            st.giveItems(Tissue2,1)
            st.set("cond","2")
            st.playSound("ItemSound.quest_middle")
        else : # in case that party members disconnected or so
            st = player.getQuestState(qn)
            if not st : return
            if st.getState() == State.STARTED and st.getInt("cond") == 1:
                st.giveItems(Tissue1,1)
                st.giveItems(Tissue2,1)
                st.set("cond","2")
                st.playSound("ItemSound.quest_middle")
        return

QUEST       = Quest(10277,qn,"Mutated Kaneus")

QUEST.addStartNpc(Lucas)
QUEST.addTalkId(Lucas)
QUEST.addTalkId(Mirien)
QUEST.addKillId(CrimsonHatuOtis)
QUEST.addKillId(SeerFlouros)