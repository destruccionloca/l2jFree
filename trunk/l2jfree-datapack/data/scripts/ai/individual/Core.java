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

import com.l2jfree.gameserver.model.actor.L2Attackable;
import com.l2jfree.gameserver.model.actor.L2Npc;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.serverpackets.NpcSay;
import com.l2jfree.gameserver.network.serverpackets.PlaySound;
import com.l2jfree.tools.random.Rnd;
import ai.group_template.L2AttackableAIScript;

/**
 * Core AI
 * @author DrLecter
 * Revised By Emperorc
 */
public class Core extends L2AttackableAIScript
{
	private static final int CORE = 29006;
	private static final int DEATH_KNIGHT = 29007;
	private static final int DOOM_WRAITH = 29008;
	//private static final int DICOR = 29009;
	//private static final int VALIDUS = 29010;
	private static final int SUSCEPTOR = 29011;
	//private static final int PERUM = 29012;
	//private static final int PREMO = 29013;

	private static boolean _FirstAttacked;
	private static boolean _isAlive;

	List<L2Attackable> Minions = new FastList<L2Attackable>();

	public Core(int id, String name, String descr)
	{
		super(id, name, descr);

		int[] mobs = {CORE, DEATH_KNIGHT, DOOM_WRAITH, SUSCEPTOR};
		registerMobs(mobs);

		_FirstAttacked = false;
		_isAlive = false;
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		if (npc.getNpcId() == CORE)
		{
			_isAlive = true;
			npc.broadcastPacket(new PlaySound(1, "BS01_A", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
			//Spawn minions
			for (int i = 0; i < 5;i++)
			{
				int x = 16800 + i * 360;
				Minions.add((L2Attackable) addSpawn(DEATH_KNIGHT, x, 110000, npc.getZ(), 280 + Rnd.get(40), false, 0));
				Minions.add((L2Attackable) addSpawn(DEATH_KNIGHT, x, 109000, npc.getZ(), 280 + Rnd.get(40), false, 0));
				int x2 = 16800 + i * 600;
				Minions.add((L2Attackable) addSpawn(DOOM_WRAITH, x2, 109300, npc.getZ(), 280 + Rnd.get(40), false, 0));
			}
			for (int i = 0; i < 4; i++)
			{
				int x = 16800 + i * 450;
				Minions.add((L2Attackable) addSpawn(SUSCEPTOR, x, 110300, npc.getZ(), 280 + Rnd.get(40), false, 0));
			}
		}
		return null;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("spawn_minion"))
		{
			Minions.add((L2Attackable) addSpawn(npc.getNpcId(), npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0));
		}
		else if (event.equalsIgnoreCase("despawn_minions"))
		{
			for (L2Attackable mob : Minions)
			{
				if (mob != null)
					mob.decayMe();
			}
			Minions.clear();
		}
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (npc.getNpcId() == CORE)
		{
			if (_FirstAttacked)
			{
				if (Rnd.get(100) == 0)
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), "Removing intruders."));
			}
			else
			{
				_FirstAttacked = true;
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), "A non-permitted target has been discovered."));
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), "Starting intruder removal system."));
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onKill (L2Npc npc, L2PcInstance killer, boolean isPet) 
	{
		int npcId = npc.getNpcId();
		if (npcId == CORE)
		{
			_isAlive = false;
			int objId = npc.getObjectId();
			npc.broadcastPacket(new PlaySound(1, "BS02_D", 1, objId, npc.getX(), npc.getY(), npc.getZ()));
			npc.broadcastPacket(new NpcSay(objId, 0, npcId, "A fatal error has occurred."));
			npc.broadcastPacket(new NpcSay(objId, 0, npcId, "System is being shut down..."));
			npc.broadcastPacket(new NpcSay(objId, 0, npcId, "......"));
			_FirstAttacked = false;
			addSpawn(31842, 16502, 110165, -6394, 0, false, 900000);
			addSpawn(31842, 18948, 110166, -6397 ,0, false, 900000);

			startQuestTimer("despawn_minions", 20000, null, null);
			cancelQuestTimers("spawn_minion");
		}
		else if (_isAlive && Minions.contains(npc))
		{
			Minions.remove(npc);
			startQuestTimer("spawn_minion",60000,npc,null);
		}
		return super.onKill(npc,killer,isPet);
	}

	public static void main(String[] args)
	{
		// now call the constructor (starts up the ai)
		new Core(-1, "core", "ai");
	}
}