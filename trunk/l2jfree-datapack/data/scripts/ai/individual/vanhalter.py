# L2J_JP CREATE SANDMAN
import sys
from com.l2jfree.gameserver.model.quest import State
from com.l2jfree.gameserver.model.quest import QuestState
from com.l2jfree.gameserver.model.quest.jython import QuestJython as JQuest
from com.l2jfree.gameserver.instancemanager.grandbosses import VanHalterManager

#NPC
ANDREAS_VAN_HALTER = 29062
ANDREAS_CAPTAIN    = 22188

#TRIOL'S REVELATIONS
TRIOLS_1 = 32058
TRIOLS_2 = 32059
TRIOLS_3 = 32060
TRIOLS_4 = 32061
TRIOLS_5 = 32062
TRIOLS_6 = 32063
TRIOLS_7 = 32064
TRIOLS_8 = 32065
TRIOLS_9 = 32066

# Main Quest Code
class vanhalter(JQuest):

  def __init__(self,id,name,descr) : JQuest.__init__(self,id,name,descr)

  def onAttack (self,npc,player,damage,isPet,skill) :
    npcId = npc.getNpcId()
    if npcId == ANDREAS_VAN_HALTER :
      maxHp = npc.getMaxHp()
      curHp = npc.getStatus().getCurrentHp()
      if (curHp / maxHp) * 100 <= 20 :
        VanHalterManager.getInstance().callRoyalGuardHelper()

  def onKill (self,npc,player,isPet) :
    npcId = npc.getNpcId()
    if npcId == TRIOLS_1 or npcId == TRIOLS_2 or npcId == TRIOLS_3 or npcId == TRIOLS_4 or npcId == TRIOLS_5 or npcId == TRIOLS_6 or npcId == TRIOLS_7 or npcId == TRIOLS_8 or npcId == TRIOLS_9 :
      VanHalterManager.getInstance().removeBleeding(npcId)
      VanHalterManager.getInstance().checkTriolRevelationDestroy()
    if npcId == ANDREAS_CAPTAIN :
      VanHalterManager.getInstance().checkRoyalGuardCaptainDestroy()
    if npcId == ANDREAS_VAN_HALTER :
      VanHalterManager.getInstance().enterInterval()

# Quest class and state definition
QUEST = vanhalter(-1,"vanhalter","ai")

# Quest NPC starter initialization
# High Priestess van Halter
QUEST.addAttackId(ANDREAS_VAN_HALTER)
QUEST.addKillId(ANDREAS_VAN_HALTER)
# Andreas' Captain of the Royal Guard
QUEST.addKillId(ANDREAS_CAPTAIN)
# Triol's Revelation
QUEST.addKillId(TRIOLS_1)
QUEST.addKillId(TRIOLS_2)
QUEST.addKillId(TRIOLS_3)
QUEST.addKillId(TRIOLS_4)
QUEST.addKillId(TRIOLS_5)
QUEST.addKillId(TRIOLS_6)
QUEST.addKillId(TRIOLS_7)
QUEST.addKillId(TRIOLS_8)
QUEST.addKillId(TRIOLS_9)