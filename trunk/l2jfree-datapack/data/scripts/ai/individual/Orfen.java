/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ai.individual;

import java.util.List;

import javolution.util.FastList;

import ai.group_template.L2AttackableAIScript;

import com.l2jfree.gameserver.ai.CtrlIntention;
import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2Spawn;
import com.l2jfree.gameserver.model.actor.L2Attackable;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Npc;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.serverpackets.NpcSay;
import com.l2jfree.gameserver.network.serverpackets.PlaySound;
import com.l2jfree.tools.random.Rnd;

/**
 * Orfen AI
 * @author Emperorc
 *
 */
public class Orfen extends L2AttackableAIScript
{
	private static final int[][] Pos = {{43728,17220,-4342},
		{55024,17368,-5412},{53504,21248,-5486},{53248,24576,-5262}};

	private static final String[] Text = {"PLAYERNAME, stop kidding yourthis about your own powerlessness!",
			"PLAYERNAME, I'll make you feel what true fear is!",
			"You're really stupid to have challenged me. PLAYERNAME! Get ready!",
			"PLAYERNAME, do you think that's going to work?!"};

	private static final int ORFEN = 29014;
	//private static final int RAIKEL = 29015;
	private static final int RAIKEL_LEOS = 29016;
	//private static final int RIBA = 29017;
	private static final int RIBA_IREN = 29018;
	
	private static boolean _IsTeleported;
	private static boolean _isAlive;
	private static List<L2Attackable> _Minions = new FastList<L2Attackable>();

	public Orfen(int id, String name, String descr)
	{
		super(id,name,descr);
		int[] mobs = {ORFEN, RAIKEL_LEOS, RIBA_IREN};
		registerMobs(mobs);
		_IsTeleported = true;
		_isAlive = false;
	}

	public void setSpawnPoint(L2Npc npc,int index)
	{
		((L2Attackable) npc).clearAggroList();
		npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
		L2Spawn spawn = npc.getSpawn();
		spawn.setLocx(Pos[index][0]);
		spawn.setLocy(Pos[index][1]);
		spawn.setLocz(Pos[index][2]);
		npc.teleToLocation(Pos[index][0],Pos[index][1],Pos[index][2]);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		if (npc.getNpcId() == ORFEN)
		{
			_isAlive = true;
			npc.broadcastPacket(new PlaySound(1, "BS01_A", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
			startQuestTimer("check_orfen_pos", 10000, npc, null, true);
			//Spawn minions
			int x = npc.getX();
			int y = npc.getY();
			_Minions.add((L2Attackable) addSpawn(RAIKEL_LEOS, x + 100, y + 100, npc.getZ(), 0, false, 0));
			_Minions.add((L2Attackable) addSpawn(RAIKEL_LEOS, x + 100, y - 100, npc.getZ(), 0, false, 0));
			_Minions.add((L2Attackable) addSpawn(RAIKEL_LEOS, x - 100, y + 100, npc.getZ(), 0, false, 0));
			_Minions.add((L2Attackable) addSpawn(RAIKEL_LEOS, x - 100, y - 100, npc.getZ(), 0, false, 0));
			startQuestTimer("check_minion_loc", 10000, npc, null, true);
		}
		return null;
	}

	@Override
	public String onAdvEvent (String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("check_orfen_pos"))
		{
			if ((_IsTeleported && npc.getStatus().getCurrentHp() > npc.getMaxHp() * 0.95))
			{
				setSpawnPoint(npc, Rnd.get(3) + 1);
				_IsTeleported = false;
			}
		}
		else if (event.equalsIgnoreCase("check_minion_loc"))
		{
			for (L2Attackable mob : _Minions)
			{
				if (mob != null && !npc.isInsideRadius(mob, 3000, false, false))
				{
					mob.teleToLocation(npc.getX(), npc.getY(), npc.getZ());
					((L2Attackable) npc).clearAggroList();
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
				}
			}
		}
		else if (event.equalsIgnoreCase("despawn_minions"))
		{
			for (L2Attackable mob : _Minions)
			{
				if (mob != null)
					mob.decayMe();
			}
			_Minions.clear();
		}
		else if (event.equalsIgnoreCase("spawn_minion"))
			_Minions.add((L2Attackable) addSpawn(RAIKEL_LEOS, npc.getX(), npc.getY(), npc.getZ(), 0, false, 0));
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onSkillSee (L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet) 
	{
		if (npc.getNpcId() == ORFEN)
		{
			L2Character originalCaster = isPet ? caster.getPet(): caster;
			if (skill.getAggroPoints() > 0 && Rnd.get(5) == 0 && npc.isInsideRadius(originalCaster, 1000, false, false))
			{
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), Text[Rnd.get(4)].replace("PLAYERNAME",caster.getName().toString())));
				originalCaster.teleToLocation(npc.getX(), npc.getY(), npc.getZ());
				npc.setTarget(originalCaster);
				npc.doCast(SkillTable.getInstance().getInfo(4064, 1));
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}

	@Override
	public String onFactionCall (L2Npc npc, L2Npc caller, L2PcInstance attacker, boolean isPet) 
	{
		if (caller == null || npc == null)
			return super.onFactionCall(npc, caller, attacker, isPet);
		int npcId = npc.getNpcId();
		int callerId = caller.getNpcId();
		if (npcId == RAIKEL_LEOS && Rnd.get(20) == 0)
		{
			npc.setTarget(attacker);
			npc.doCast(SkillTable.getInstance().getInfo(4067,4));
		}
		else if (npcId == RIBA_IREN)
		{
			int chance = 1;
			if (callerId == ORFEN)
				chance = 9;
			if (callerId != RIBA_IREN && caller.getStatus().getCurrentHp() < (caller.getMaxHp() / 2) && Rnd.get(10) < chance)
			{
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
				npc.setTarget(caller);
				npc.doCast(SkillTable.getInstance().getInfo(4516, 1));
			}
		}
		return super.onFactionCall(npc, caller, attacker, isPet);
	}

	@Override
	public String onAttack (L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if (npcId == ORFEN)
		{
			if ((npc.getStatus().getCurrentHp() - damage) < (npc.getMaxHp() / 2) && !_IsTeleported)
			{
				setSpawnPoint(npc, 0);
				_IsTeleported = true;
			}
			else if (npc.isInsideRadius(attacker, 1000, false, false) && !npc.isInsideRadius(attacker, 300, false, false) && Rnd.get(10) == 0)
			{
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npcId, Text[Rnd.get(3)].replace("PLAYERNAME",attacker.getName().toString())));
				attacker.teleToLocation(npc.getX(), npc.getY(), npc.getZ());
				npc.setTarget(attacker);
				npc.doCast(SkillTable.getInstance().getInfo(4064,1));
			}
		}
		else if (npcId == RIBA_IREN)
		{
			if ((npc.getStatus().getCurrentHp() - damage) < (npc.getMaxHp() / 2))
			{
				npc.setTarget(attacker);
				npc.doCast(SkillTable.getInstance().getInfo(4516,1));
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onKill (L2Npc npc, L2PcInstance killer, boolean isPet) 
	{
		if (npc.getNpcId() == ORFEN)
		{
			_isAlive = false;
			npc.broadcastPacket(new PlaySound(1, "BS02_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
			cancelQuestTimer("check_minion_loc",npc,null);
			cancelQuestTimer("check_orfen_pos",npc,null);
			cancelQuestTimers("spawn_minion");
			startQuestTimer("despawn_minions",20000,null,null);
		}
		else if (_isAlive && npc.getNpcId() == RAIKEL_LEOS)
		{
			_Minions.remove(npc);
			startQuestTimer("spawn_minion", 360000, npc, null);
		}
		return super.onKill(npc,killer,isPet);
	}

	public static void main(String[] args)
	{
		// Quest class and state definition
		new Orfen(-1,"orfen","ai");
	}
}