# L2J_JP CREATE SANDMAN
import sys
from com.l2jfree.gameserver.model.quest import State
from com.l2jfree.gameserver.model.quest import QuestState
from com.l2jfree.gameserver.model.quest.jython import QuestJython as JQuest
from com.l2jfree.gameserver.instancemanager.grandbosses import ValakasManager

# Main Quest Code
class valakas(JQuest):

  def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

  def onTalk (self,npc,player):
    st = player.getQuestState("valakas")
    if not st : return "<html><body>You are either not carrying out your quest or don't meet the criteria.</body></html>"
    npcId = npc.getNpcId()
    if npcId == 31385 :    # Heart of Volcano
      if st.getInt("ok"):
        if ValakasManager.getInstance().isEnableEnterToLair():
          ValakasManager.getInstance().setValakasSpawnTask()
          st.player.teleToLocation(203940,-111840,66)
          return
        else:
          st.exitQuest(1)
          return "<html><body>Heart of Volcano:<br><br>Valakas is already awake!<br>You may not enter the Lair of Valakas.<br></body></html>"
      else:
        st.exitQuest(1)
        return "Conditions are not right to enter to Lair of Valakas."
    elif npcId == 31540 :    # Klein
      if ValakasManager.getInstance().isEnableEnterToLair():
        if st.getQuestItemsCount(7267) > 0 :    # Check Floating Stone
          st.takeItems(7267,1)
          player.teleToLocation(183831,-115457,-3296)
          st.set("ok","1")
        else :
          st.exitQuest(1)
          return "<html><body>Klein:<br>You do not have the Floating Stone. Go get one and then come back to me.</body></html>"
      else:
        st.exitQuest(1)
        return "<html><body>Klein:<br><br>Valakas is already awake!<br>You may not enter the Lair of Valakas.<br></body></html>"
      return

  def onKill (self,npc,player,isPet):
    st = player.getQuestState("valakas")
    #give the valakas slayer circlet to ALL PARTY MEMBERS who help kill valakas,
    party = player.getParty()
    if party :
       for partyMember in party.getPartyMembers().toArray() :
           pst = partyMember.getQuestState("valakas")
           if pst :
               if pst.getQuestItemsCount(8567) < 1 :
                   pst.giveItems(8567,1)
                   pst.exitQuest(1)
    else :
       pst = player.getQuestState("valakas")
       if pst :
           if pst.getQuestItemsCount(8567) < 1 :
               pst.giveItems(8567,1)
               pst.exitQuest(1)
    ValakasManager.getInstance().setCubeSpawn()
    if not st: return
    st.exitQuest(1)

# Quest class and state definition
QUEST = valakas(-1,"valakas","ai")

# Quest NPC starter initialization
QUEST.addStartNpc(31540)
QUEST.addStartNpc(31385)
QUEST.addTalkId(31540)
QUEST.addTalkId(31385)
QUEST.addKillId(29028)