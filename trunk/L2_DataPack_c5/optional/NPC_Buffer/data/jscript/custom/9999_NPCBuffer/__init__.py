import sys
from net.sf.l2j.gameserver.model.actor.instance import L2PcInstance
from java.util import Iterator
from net.sf.l2j.gameserver.datatables	import SkillTable
from net.sf.l2j			       import L2DatabaseFactory
from net.sf.l2j.gameserver.model.quest import State
from net.sf.l2j.gameserver.model.quest import QuestState
from net.sf.l2j.gameserver.model.quest.jython import QuestJython as JQuest

NPC=[7784,7788,7790,7786,7778,7780,7782,7774,7776,7800,7802,7798,12836,12837,12835,12834,12833,8158,8160,8156,8152,8150,8154,12895,12896,12897,12898]
ADENA_ID=57
QuestId     = 9999
QuestName   = "NPCBuffer"
QuestDesc   = "custom"
InitialHtml = "1.htm"

print "importing " + QuestDesc + ": " + str(QuestId) + ": " + QuestName + ": " + QuestName,

class Quest (JQuest) :

	def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)


	def onEvent(self,event,st):
		htmltext = event
		count=st.getQuestItemsCount(ADENA_ID)
		if count < 15000 :
			htmltext = "<html><head><body>You dont have 15000 Adena.</body></html>"
		else:
			st.takeItems(ADENA_ID,15000)
			st.getPlayer().setTarget(st.getPlayer())
			if event == "2":
				st.getPlayer().useMagic(SkillTable.getInstance().getInfo(4360,3),False,False)
				st.getPlayer().restoreHPMP()
				st.getPlayer().useMagic(SkillTable.getInstance().getInfo(4359,3),False,False)
				st.getPlayer().restoreHPMP()
				st.getPlayer().useMagic(SkillTable.getInstance().getInfo(4358,3),False,False)
				st.getPlayer().restoreHPMP()
				st.getPlayer().useMagic(SkillTable.getInstance().getInfo(4357,2),False,False)
				st.getPlayer().restoreHPMP()
				st.getPlayer().useMagic(SkillTable.getInstance().getInfo(4354,4),False,False)
				st.getPlayer().restoreHPMP()
				st.getPlayer().useMagic(SkillTable.getInstance().getInfo(4353,6),False,False)
				st.getPlayer().restoreHPMP()
				st.getPlayer().useMagic(SkillTable.getInstance().getInfo(4350,4),False,False)
				st.getPlayer().restoreHPMP()
				st.getPlayer().useMagic(SkillTable.getInstance().getInfo(4348,6),False,False)
				st.getPlayer().restoreHPMP()
				st.setState(COMPLETED)

			if event == "3":
				st.getPlayer().useMagic(SkillTable.getInstance().getInfo(4352,2),False,False)
				st.getPlayer().restoreHPMP()
				st.getPlayer().useMagic(SkillTable.getInstance().getInfo(4351,6),False,False)		
				st.getPlayer().restoreHPMP()
				st.getPlayer().useMagic(SkillTable.getInstance().getInfo(4355,3),False,False)
				st.getPlayer().restoreHPMP()
				st.getPlayer().useMagic(SkillTable.getInstance().getInfo(4356,3),False,False)
				st.getPlayer().restoreHPMP()
				st.setState(COMPLETED)

			if event == "4":
				st.getPlayer().useMagic(SkillTable.getInstance().getInfo(4346,4),False,False)
				st.getPlayer().restoreHPMP()
				st.getPlayer().useMagic(SkillTable.getInstance().getInfo(4342,2),False,False)
				st.getPlayer().restoreHPMP()
				st.getPlayer().useMagic(SkillTable.getInstance().getInfo(4343,3),False,False)
				st.getPlayer().restoreHPMP()
				st.getPlayer().useMagic(SkillTable.getInstance().getInfo(4344,3),False,False)
				st.getPlayer().restoreHPMP()
				st.getPlayer().useMagic(SkillTable.getInstance().getInfo(4345,3),False,False)
				st.getPlayer().restoreHPMP()
				st.getPlayer().useMagic(SkillTable.getInstance().getInfo(4349,2),False,False)
				st.getPlayer().restoreHPMP()
				st.getPlayer().useMagic(SkillTable.getInstance().getInfo(4347,6),False,False)
				st.getPlayer().restoreHPMP()
				st.setState(COMPLETED)
				
			if htmltext != event:
				st.setState(COMPLETED)
				st.exitQuest(1)
		return htmltext


        def onTalk (self,npc,st):
	   htmltext = "<html><head><body>I have nothing to say to you</body></html>"
	   st.setState(STARTED)
           return InitialHtml



QUEST       = Quest(QuestId,str(QuestId) + "_" + QuestName,QuestDesc)
CREATED=State('Start',QUEST)
STARTED=State('Started',QUEST)
COMPLETED=State('Completed',QUEST)


QUEST.setInitialState(CREATED)

for npcId in NPC:
 QUEST.addStartNpc(npcId)
 STARTED.addTalkId(npcId)

print "...done"



              
