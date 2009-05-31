import sys
from com.l2jfree.gameserver.ai import CtrlIntention
from com.l2jfree.gameserver.model.quest.jython import QuestJython as JQuest
from com.l2jfree.gameserver.network.serverpackets import NpcSay

class monastery(JQuest) :

  def __init__(self, id, name, descr) :
    JQuest.__init__(self, id, name, descr)

  def onSpawn(self, npc) :
    objId = npc.getObjectId()
    for player in npc.getKnownList().getKnownPlayers().values() :
      if player.isInsideRadius(npc, 500, False, False) :
        if player.getActiveWeaponItem() :
          npc.broadcastPacket(NpcSay(objId, 0, npc.getNpcId(), "You cannot carry a weapon without authorization!"))
          npc.addDamageHate(player, 0, 999)
          npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player)
        else :
          npc.getAggroListRP().remove(player)
          npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, None, None)
    return

  def onAggroRangeEnter(self, npc, player, isPet) :
    objId = npc.getObjectId()
    for player in npc.getKnownList().getKnownPlayers().values() :
      if player.isInsideRadius(npc, 500, False, False) :
        if player.getActiveWeaponItem() :
          npc.broadcastPacket(NpcSay(objId, 0, npc.getNpcId(), "You cannot carry a weapon without authorization!"))
          npc.addDamageHate(player, 0, 999)
          npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player)
        else :
          npc.getAggroListRP().remove(player)
          npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, None, None)
    return

QUEST = monastery(-1, "monastery", "ai")

for i in range(22124,22128):
    QUEST.addSpawnId(i)
    QUEST.addAggroRangeEnterId(i)