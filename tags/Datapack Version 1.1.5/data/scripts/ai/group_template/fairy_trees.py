import sys

from com.l2jfree.gameserver.ai import CtrlIntention
from com.l2jfree.gameserver.datatables import SkillTable
from com.l2jfree.gameserver.model.quest import Quest as JQuest
from com.l2jfree.gameserver.network.serverpackets import NpcSay
from com.l2jfree.tools.random import Rnd

class trees(JQuest) :

    def __init__(self,id,name,descr):
        JQuest.__init__(self,id,name,descr)

    def onAdvEvent (self,event,npc,pc) :
        if npc:
           npc.deleteMe()
        return

    def onKill (self,npc,player,isPet):
        npcId = npc.getNpcId()
        if npcId in range(27185,27189) :
           for x in xrange(20):
               newNpc = self.addSpawn(27189,npc)
               killer = player
               if isPet :
                   killer = player.getPet()
               newNpc.setRunning()
               newNpc.addDamageHate(killer,0,999)
               newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, killer)
               self.startQuestTimer("despawn",300000, newNpc, None)
               if Rnd.get(2) :
                  skill = SkillTable.getInstance().getInfo(4243,1)
                  if skill != None :
                     skill.getEffects(newNpc, killer)
        return 


QUEST        = trees(-2,"fairy trees","ai")

for i in range(27185,27189):
    QUEST.addKillId(i)
QUEST.addSpawnId(27189)