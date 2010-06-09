# Made by LiveTeam modified by Daehak for L2jFree

import sys
from com.l2jfree.gameserver.model.quest        import State
from com.l2jfree.gameserver.model.quest        import QuestState
from com.l2jfree.gameserver.model.quest.jython import QuestJython as JQuest
from com.l2jfree.tools.random                  import Rnd

qn = "10278_MutatedKaneusHein"

#NPCs
Gosta = 30916
Minevia = 30907
BladeOtis = 18562
WeirdBunei = 18564

#items
Tissue1 = 13834
Tissue2 = 13835

class Quest (JQuest) :
    def __init__(self,id,name,descr):
        JQuest.__init__(self,id,name,descr)
        self.questItemIds = [Tissue1,Tissue2]

    def onAdvEvent (self,event,npc, player) :
        htmltext = event
        st = player.getQuestState(qn)
        if not st : return
        if event == "30916-03.htm" :
            st.set("cond","1")
            st.setState(State.STARTED)
            st.playSound("ItemSound.quest_accept")
        elif event == "30907-02.htm" :
            st.giveItems(57,360000)
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
            if npcId == Gosta :
                htmltext = "30916-0a.htm"
        elif id == State.CREATED and npcId == Gosta:
            if player.getLevel() >= 38 :
                htmltext = "30916-01.htm"
            else :
                htmltext = "30916-00.htm"
        else :
            if npcId == Gosta :
                if cond == 1:
                   htmltext = "30916-04.htm"
                elif cond == 2:
                   htmltext = "30916-05.htm"
            elif npcId == Minevia:
                if cond == 2:
                   htmltext = "30907-01.htm"
                else :
                   htmltext = "30907-01a.htm"
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

QUEST       = Quest(10278,qn,"Mutated Kaneus")

QUEST.addStartNpc(Gosta)
QUEST.addTalkId(Gosta)
QUEST.addTalkId(Minevia)
QUEST.addKillId(BladeOtis)
QUEST.addKillId(WeirdBunei)