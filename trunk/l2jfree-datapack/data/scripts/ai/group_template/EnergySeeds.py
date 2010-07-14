# Author: Psycho(killer1888) / L2jFree

import sys
from com.l2jfree.gameserver.instancemanager             import SeedOfDestructionManager
from com.l2jfree.gameserver.model.quest.jython          import QuestJython as JQuest
from com.l2jfree.gameserver.network.serverpackets       import SystemMessage
from com.l2jfree.gameserver.network                     import SystemMessageId
from com.l2jfree.tools.random                           import Rnd

STONES = {
18678 : 14016,
18679 : 14015,
18680 : 14017,
18681 : 14018,
18682 : 14020,
18683 : 14019}

SKILL = 5780

NPCS = [18678,18679,18680,18681,18682,18683]

class EnergySeeds(JQuest) :

    def __init__(self, id, name, descr) :
        JQuest.__init__(self, id, name, descr)

    def onSpawn(self,npc):
        npc.setIsNoRndWalk(True)
        return

    def onAdvEvent(self,event,npc,player):
        npcId = npc.getNpcId();
        if event == "respawn":
            self.addSpawn(NPCS[Rnd.get(len(NPCS))], npc.getX(), npc.getY(), npc.getZ(), 0 , False, 0)
        return

    def onSkillSee(self,npc,caster,skill,targets,isPet):
        npcId = npc.getNpcId()
        skillId = skill.getId()
        if npcId in NPCS:
            if SeedOfDestructionManager.getInstance().getState() != 3:
                return
            if not npc in targets:
                return
            if skillId == SKILL:
                itemId = STONES[npcId]
                chance = Rnd.get(100)
                if chance > 40:
                    caster.addItem("Energy Seed", itemId, Rnd.get(1,2), caster, True, True)
                    sm = SystemMessage(SystemMessageId.STARSTONE_COLLECTED)
                else:
                    sm = SystemMessage(SystemMessageId.STARSTONE_COLLECTION_FAILED)
                self.startQuestTimer("respawn", Rnd.get(60000,7200000), npc, None)
                npc.decayMe()
                caster.sendPacket(sm)
        return

QUEST = EnergySeeds(-1, "EnergySeeds", "ai")

for npc in NPCS:
    QUEST.addSpawnId(npc)
    QUEST.addSkillSeeId(npc)