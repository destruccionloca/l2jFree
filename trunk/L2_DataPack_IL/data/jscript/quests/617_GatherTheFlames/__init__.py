# Created by Umbra
import sys
from net.sf.l2j.gameserver.model.quest import State
from net.sf.l2j.gameserver.model.quest import QuestState
from net.sf.l2j.gameserver.model.quest.jython import QuestJython as JQuest

qn = "617_GatherTheFlames"

TORCH = 7264
VULCAN = 31539
MOBS = [ 21652,21653,21654,21655,21656,21657,21376,21377,21378,21379,21380,21381,21382,21383,21384,21385,21386,21387,21388,21389,21390,21391,21392,21393,21394,21395 ]
REWARDS = [ 6881,6883,6885,6887,6889,6891,6893,6895,6897,6899,7580 ]

class Quest (JQuest) :

    def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

    def onEvent (self,event,st) :
        htmltext = event
        torches = st.getQuestItemsCount(TORCH)
        if event == "31539-03.htm" :
            if st.getPlayer().getLevel() >= 74 :
                if st.get("cond") == "0" :
                    st.set("cond","1")
                    st.setState(STARTED)
                    st.playSound("ItemSound.quest_accept")
            else :
                htmltext = "31539-02.htm"
                st.exitQuest(1)
        elif event == "31539-07.htm" :
            if torches >= 1000 :
                st.takeItems(TORCH,1000)
                st.giveItems(REWARDS[st.getRandom(len(REWARDS))],1)
            else :
               htmltext = "31539-06.htm"
        elif event == "31539-08.htm" :
            st.takeItems(TORCH,-1)
            st.exitQuest(1)
        return htmltext

    def onTalk (self,npc,player):
        htmltext = "<html><head><body>I have nothing to say you</body></html>"
        st = player.getQuestState(qn)
        if not st :
            return htmltext
       
        npcId = npc.getNpcId()
        id = st.getState()
        if id == CREATED :
            st.set("cond","0")
            htmltext = "31539-01.htm"
        elif st.get("cond") == "1" :
            if st.getQuestItemsCount(TORCH) >= 1000 :
                htmltext = "31539-04.htm"
            else :
                htmltext = "31539-05.htm"
        return htmltext

    def onKill (self,npc,player):
        partyMember = self.getRandomPartyMemberState(player, STARTED)
        if not partyMember :
            return
        st = partyMember.getQuestState(qn)
        if st :
          if st.getState() == STARTED :
            npcId = npc.getNpcId()
            if npcId in MOBS :
                st.giveItems(TORCH,1)
                torches = st.getQuestItemsCount(TORCH)
                if torches == 1000 :
                    st.playSound("ItemSound.quest_middle")
                else :
                    st.playSound("ItemSound.quest_itemget")
        return

QUEST       = Quest(617, qn, "Gather the Flames")
CREATED     = State('Start', QUEST)
STARTED     = State('Started', QUEST)

QUEST.setInitialState(CREATED)
QUEST.addStartNpc(VULCAN)
QUEST.addTalkId(VULCAN)

for mobId in MOBS :
  QUEST.addKillId(mobId)
  STARTED.addQuestDrop(VULCAN,TORCH,1)

print "importing quests: 617: Gather the Flames"
