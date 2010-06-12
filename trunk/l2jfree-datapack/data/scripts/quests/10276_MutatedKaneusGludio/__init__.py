# Made by LiveTeam modified by Daehak for L2jFree

import sys
from com.l2jfree.gameserver.model.quest        import State
from com.l2jfree.gameserver.model.quest        import QuestState
from com.l2jfree.gameserver.model.quest.jython import QuestJython as JQuest
from com.l2jfree.tools.random                  import Rnd

qn = "10276_MutatedKaneusGludio"

#NPCs
Bathis = 30332
Rohmer = 30344
TomlanKamos = 18554
OlAriosh = 18555

#items
Tissue1 = 13830
Tissue2 = 13831

class Quest (JQuest) :
    def __init__(self,id,name,descr):
        JQuest.__init__(self,id,name,descr)
        self.questItemIds = [Tissue1,Tissue2]

    def onAdvEvent (self,event,npc, player) :
        htmltext = event
        st = player.getQuestState(qn)
        if not st : return
        if event == "30332-03.htm" :
            st.set("cond","1")
            st.setState(State.STARTED)
            st.playSound("ItemSound.quest_accept")
        elif event == "30344-02.htm" :
            st.giveItems(57,8500)
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
            if npcId == Bathis :
                htmltext = "30332-0a.htm"
        elif id == State.CREATED and npcId == Bathis:
            if player.getLevel() >= 18 :
                htmltext = "30332-01.htm"
            else :
                htmltext = "30332-00.htm"
        else :
            if npcId == Bathis :
                if cond == 1:
                   htmltext = "30332-04.htm"
                elif cond == 2:
                   htmltext = "30332-05.htm"
            elif npcId == Rohmer:
                if cond == 2:
                   htmltext = "30344-01.htm"
                else :
                   htmltext = "30344-01a.htm"
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

QUEST       = Quest(10276,qn,"Mutated Kaneus")

QUEST.addStartNpc(Bathis)
QUEST.addTalkId(Bathis)
QUEST.addTalkId(Rohmer)
QUEST.addKillId(TomlanKamos)
QUEST.addKillId(OlAriosh)