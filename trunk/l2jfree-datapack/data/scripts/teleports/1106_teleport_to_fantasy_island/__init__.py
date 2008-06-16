#
# Created by Shizzo
#
import sys

from com.l2jfree.gameserver.model.actor.instance import L2PcInstance
from com.l2jfree.gameserver.model.quest          import State
from com.l2jfree.gameserver.model.quest          import QuestState
from com.l2jfree.gameserver.model.quest.jython   import QuestJython as JQuest
qn = "1106_teleport_to_fantasy_island"

FANTASY_ISLAND_GK = 32378

TELEPORTERS = {
    30059:3,    # TRISHA
    30080:4,    # CLARISSA
    30177:6,    # VALENTIA
    30233:8,    # ESMERALDA
    30256:2,    # BELLA
    30320:1,    # RICHLIN
    30848:7,    # ELISA
    30899:5,    # FLAUEN
    31320:9,    # ILYANA
    31275:10,   # TATIANA
    30727:11,   # VERONA
    30836:12,   # MINERVA
    31964:13    # BILIA
}

RETURN_LOCS = [[-80826,149775,-3043],[-12672,122776,-3116],[15670,142983,-2705],[83400,147943,-3404], \
              [111409,219364,-3545],[82956,53162,-1495],[146331,25762,-2018],[116819,76994,-2714], \
              [43835,-47749,-792],[147930,-55281,-2728],[85335,16177,-3694],[105857,109763,-3202], \
              [87386,-143246,-1293]]

class Quest (JQuest) :

 def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

 def onTalk (self,npc,player):
   st = player.getQuestState(qn)
   npcId = npc.getNpcId()
   if not st: return
   ###################
   # Start Locations #
   ###################
   if TELEPORTERS.has_key(npcId) :
     st.getPlayer().teleToLocation(-59722,-57866,-2032)
     st.setState(State.STARTED)
     st.set("id",str(TELEPORTERS[npcId]))     
   ############################
   #     Fantasy Island       #
   ############################
   elif st.getState() == State.STARTED and npcId == FANTASY_ISLAND_GK:
     # back to start location
     return_id = st.getInt("id") - 1
     st.getPlayer().teleToLocation(RETURN_LOCS[return_id][0],RETURN_LOCS[return_id][1],RETURN_LOCS[return_id][2])
     st.exitQuest(1)
   return

QUEST       = Quest(1106,qn,"Teleports")

for npcId in TELEPORTERS.keys() :
    QUEST.addStartNpc(npcId)
    QUEST.addTalkId(npcId)

QUEST.addTalkId(FANTASY_ISLAND_GK)