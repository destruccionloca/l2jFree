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
package com.l2jfree.gameserver.instancemanager.grandbosses;

import javolution.util.FastList;

import com.l2jfree.Config;
import com.l2jfree.gameserver.datatables.NpcTable;
import com.l2jfree.gameserver.model.L2CharPosition;
import com.l2jfree.gameserver.model.L2Spawn;
import com.l2jfree.gameserver.model.actor.L2Npc;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;

/** 
 * @author hex1r0
 **/
public class QueenAntManager
{
	private final L2CharPosition _queenAntPos 		= new L2CharPosition(-21610, 181594, -5734, 0);
	private final L2CharPosition _queenAntLarvaPos 	= new L2CharPosition(-21600, 179482, -5846, 0);
	
	private final L2NpcTemplate _larvaTemplate = NpcTable.getInstance().getTemplate(29002);
	private final L2NpcTemplate _nurseTemplate = NpcTable.getInstance().getTemplate(29003);
	
	private FastList <L2Spawn> 	_nurseSpawns 	= new FastList<L2Spawn>();
	private FastList <L2Npc> 	_nurses 		= new FastList<L2Npc>(); 
	
	private L2Spawn 			_larvaSpawn 	= null;
	private L2Npc 				_larva 			= null;
	
	private L2Npc 				_queenAnt 		= null;
	
	public QueenAntManager()
	{
		_larvaSpawn = new L2Spawn(_larvaTemplate);
		_larvaSpawn.setAmount(1);
		_larvaSpawn.setLocx(_queenAntLarvaPos.x);
		_larvaSpawn.setLocy(_queenAntLarvaPos.y);
		_larvaSpawn.setLocz(_queenAntLarvaPos.z);
		_larvaSpawn.setHeading(_queenAntLarvaPos.heading);
		_larvaSpawn.stopRespawn();

		int radius = 400;
		for (int i = 0; i < 4; i++)
		{
			int x = _queenAntPos.x + (int) (radius * Math.cos(i * 1.407));
			int y = _queenAntPos.y + (int) (radius * Math.sin(i * 1.407));

			L2Spawn nurseSpawn = new L2Spawn(_nurseTemplate);
			nurseSpawn.setAmount(1);
			nurseSpawn.setLocx(x);
			nurseSpawn.setLocy(y);
			nurseSpawn.setLocz(_queenAntPos.z);
			nurseSpawn.setHeading(_queenAntPos.heading);
			nurseSpawn.setRespawnDelay(Config.NURSEANT_RESPAWN_DELAY);
			_nurseSpawns.add(nurseSpawn);
		}
	}
	
	public void init(L2Npc queen)
	{
		QueenAntManager.getInstance().setQueenAntInstance(queen);
		QueenAntManager.getInstance().spawnNurses();
		QueenAntManager.getInstance().spawnLarva();
	}
	
	public void setQueenAntInstance(L2Npc npc)
	{
		_queenAnt = npc;
	}
	
	public void spawnNurses()
	{	
		for (L2Spawn spawn : _nurseSpawns)
		{
			spawn.startRespawn();
			_nurses.add(spawn.doSpawn());
		}
	}
	
	public void deleteNurses()
	{
		for (L2Npc n : _nurses)
		{
			n.getSpawn().stopRespawn();
			n.deleteMe();
		}
		_nurses.clear();
	}
	
	public void spawnLarva()
	{
		_larva = _larvaSpawn.doSpawn();
	}

	public void deleteLarva()
	{
		_larva.getSpawn().stopRespawn();
		_larva.deleteMe();
	}
	
	public L2Npc getQueenAntInstance()
	{
		return _queenAnt;
	}
	
	public L2Npc getLarvaInstance()
	{
		return _larva;
	}
	
	private static final class SingletonHolder
	{
		private static final QueenAntManager INSTANCE = new QueenAntManager();
	}

	public static QueenAntManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
}
