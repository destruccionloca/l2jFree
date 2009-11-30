# Psycho(killer1888) / L2jFree
import sys
from com.l2jfree.gameserver.model.quest        import State
from com.l2jfree.gameserver.model.quest        import QuestState
from com.l2jfree.gameserver.model.quest.jython import QuestJython as JQuest
from java.text                                 import SimpleDateFormat
from java.util                                 import GregorianCalendar

qn = "1108_Hellbound_WarpGate"

WARPGATES  = [32314,32315,32316,32317,32318,32319]
WARPACTIVE = 100000
ENERGYFROMMINORBOSSES = False

KECHI      = 25532
DARNEL     = 25531
TEARS      = 25534
BAYLOR     = 29099

def getDate(self):
    calendar = GregorianCalendar()
    date = calendar.getTime()
    dayFormat = SimpleDateFormat("dd")
    monthFormat = SimpleDateFormat("MM")
    yearFormat = SimpleDateFormat("yyyy")
    DAY = int(dayFormat.format(date))
    MONTH = int(monthFormat.format(date))
    YEAR = int(yearFormat.format(date))
    if MONTH < 10:
        TEMP1 = "%d0%d" % (YEAR, MONTH)
    else:
        TEMP1 = "%d%d" % (YEAR, MONTH)
    if DAY < 10:
        CURRENTDATE = "%d0%d" % (TEMP1, DAY)
    else:
        CURRENTDATE = "%d%d" % (TEMP1, DAY)
    return CURRENTDATE

def checkWarpGate (self):
    warpEnergy = self.loadGlobalQuestVar("WarpGateEnergy")
    return int(warpEnergy)

def updateWarpGate (self,points,increase):
    warpEnergy = self.loadGlobalQuestVar("WarpGateEnergy")
    if increase:
        tmp = int(warpEnergy) + points
    else:
        tmp = int(warpEnergy) - points
    if tmp < 0:
        tmp = 0
    if tmp > 400000:
        tmp = 400000
    self.saveGlobalQuestVar("WarpGateEnergy",str(tmp))
    return tmp

class Quest(JQuest):

    def __init__(self,id,name,descr):
        JQuest.__init__(self,id,name,descr)
        warpEnergy = self.loadGlobalQuestVar("WarpGateEnergy")
        if warpEnergy == "":
            self.saveGlobalQuestVar("WarpGateEnergy","0")
            warpEnergy = 0
        print "WarpGates for Hellbound: " + str(warpEnergy) + " energy"
        currentDate = getDate(self)
        lastControl = self.loadGlobalQuestVar("WarpGateControlled")
        if lastControl == "":
            self.saveGlobalQuestVar("WarpGateControlled",str(currentDate))
        else:
            if int(currentDate) > int(lastControl):
                multiple = int(currentDate) - int(lastControl)
                points = 10000 * multiple
                self.saveGlobalQuestVar("WarpGateControlled",str(currentDate))
                updateWarpGate(self,points,False)
        longQuest = self.loadGlobalQuestVar("BloodyHotQuest")
        if longQuest == "":
            self.saveGlobalQuestVar("BloodyHotQuest","0")
            longQuest = 0
        self.bloodyHot = int(longQuest)

    def onTalk (self,npc,player):
        st = player.getQuestState(qn)
        if not st: return
        npcId = npc.getNpcId()
        if self.bloodyHot == 0:
            st1 = st.getPlayer().getQuestState("133_ThatsBloodyHot")
            if st1:
                if st1.getState() == State.COMPLETED:
                    updateWarpGate(self,100000,True)
                    self.saveGlobalQuestVar("BloodyHotQuest","1")
                    player.teleToLocation(-11095, 236440, -3232)
                    htmltext = ""
                else:
                    htmltext = "cant-port.htm"
            else:
                htmltext = "cant-port.htm"
        else:
            if checkWarpGate(self) >= WARPACTIVE:
                st2 = st.getPlayer().getQuestState("130_PathToHellbound")
                if st2:
                    if st2.getState() == State.COMPLETED:
                        player.teleToLocation(-11095, 236440, -3232)
                        htmltext = ""
                    else:
                        htmltext = "cant-port.htm"
                else:
                    htmltext = "cant-port.htm"
            else:
                htmltext = "cant-port.htm"
        st.exitQuest(1)
        return htmltext

    def onKill(self,npc,player,isPet):
        npcId = npc.getNpcId()
        if npcId == BAYLOR:
            if self.bloodyHot == 1:
                updateWarpGate(self,80000,True)
        if ENERGYFROMMINORBOSSES:
            if npcId == TEARS or npcId == KECHI or npcId == DARNEL:
                updateWarpGate(self,10000,True)
        return

QUEST = Quest(1108,qn,"Teleports")

for npcId in WARPGATES :
    QUEST.addStartNpc(npcId)
    QUEST.addTalkId(npcId)

QUEST.addKillId(BAYLOR)
QUEST.addKillId(TEARS)
QUEST.addKillId(KECHI)
QUEST.addKillId(DARNEL)