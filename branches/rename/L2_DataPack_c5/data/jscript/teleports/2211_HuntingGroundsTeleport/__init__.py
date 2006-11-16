# By Deniska Spectr
import sys
from net.sf.l2j.gameserver.model.actor.instance import L2PcInstance
from net.sf.l2j.gameserver.model.quest          import State
from net.sf.l2j.gameserver.model.quest          import QuestState
from net.sf.l2j.gameserver.model.quest.jython   import QuestJython as JQuest

GLUDIN_DAWN,GLUDIO_DAWN,DION_DAWN,GIRAN_DAWN,HEINE_DAWN,OREN_DAWN,ADEN_DAWN,\
GLUDIN_DUSK,GLUDIO_DUSK,DION_DUSK,GIRAN_DUSK,HEINE_DUSK,OREN_DUSK,ADEN_DUSK = range(31078,31092)
HW_DAWN,HW_DUSK = range(31168,31170)
GODDARD_DAWN,GODDARD_DUSK,RUNE_DAWN,RUNE_DUSK = range(31692,31696)

class Quest (JQuest) :

 def __init__(self, id, name, descr): JQuest.__init__(self, id, name, descr)

 def onTalk (Self, npc, st):
    npcId = npc.getNpcId()
    if npcId in [GLUDIN_DAWN,GLUDIN_DUSK] :
          htmltext = "hg_gludin.htm"
    elif npcId in [GLUDIO_DAWN,GLUDIO_DUSK] :
          htmltext = "hg_gludio.htm"
    elif npcId in [DION_DAWN,DION_DUSK] :
          htmltext = "hg_dion.htm"
    elif npcId in [GIRAN_DAWN,GIRAN_DUSK] :
          htmltext = "hg_giran.htm"
    elif npcId in [OREN_DAWN,OREN_DUSK] :
          htmltext = "hg_oren.htm"
    elif npcId in [ADEN_DAWN,ADEN_DUSK] :
          htmltext = "hg_aden.htm"
    elif npcId in [HEINE_DAWN,HEINE_DUSK] :
          htmltext = "hg_heine.htm"
    elif npcId in [HW_DAWN,HW_DUSK] :
          htmltext = "hg_hw.htm"
    elif npcId in [GODDARD_DAWN,GODDARD_DUSK] :
          htmltext = "hg_goddard.htm"
    elif npcId in [RUNE_DAWN,RUNE_DUSK] :
          htmltext = "hg_rune.htm"
    else:
    	  htmltext = "hg_wrong.htm"
    st.exitQuest(1)
    return htmltext

QUEST    = Quest(2211, "2211_HuntingGroundsTeleport", "Teleports")
CREATED    = State('Start', QUEST)

QUEST.setInitialState(CREATED)

for i in range(31078,31092)+range(31168,31170)+range(31692,31696) :
   QUEST.addStartNpc(i)
   CREATED.addTalkId(i)

print "importing teleport data: 2211_HuntingGroundsTeleport"
