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
package net.sf.l2j.gameserver.skills.effects;

import java.util.Vector;

import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2TrapInstance;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Manages Trap Effects
 * Keeps a L2TrapInstance Queue to detect or remove a trap instance
 * @author Darki699
 */
public final class EffectTrap
{
	protected static 	Log							_log = LogFactory.getLog(EffectTrap.class.getName());
	private static 		Vector <L2TrapInstance> 	_trapQueue;
	private static 		EffectTrap					_instance;

	/**
	 * Initialize the trap queue
	 */
	public EffectTrap()
	{
		_trapQueue = new Vector <L2TrapInstance>();
	}

	/**
	 * Returns this instance and creates the instance if it doesn't exist yet.
	 * @return
	 */
	public static EffectTrap getInstance()
	{
		if (_instance == null)
		{
			_instance = new EffectTrap();
		}

		return _instance;
	}

	/**
	 * Check if the activeChar L2Character is standing close to a trap, or maybe should activate it
	 * @param activeChar
	 * @param checkKnownListAsWell - if set to true, checks the knownList as well
	 */
	public void checkTraps(L2Character activeChar , boolean checkKnownListAsWell)
	{
		if (activeChar == null)
    		return;

    	if (checkKnownListAsWell)
    	{
        	// Let's check the L2Characters it knows as well
    		for (L2Character knownChar : activeChar.getKnownList().getKnownCharacters())
    		{
        		// This "false" is done to save us from deadlocks (x->y->x->y->x->... is bad)
    			checkTraps(knownChar , false);
    		}
    	}

    	for (L2TrapInstance trap : _trapQueue)
    	{
    		L2Character trapInstance = trap.getTrapObject();

    		if (trapInstance == null)
    			continue;

    		else if (trapInstance.isInsideRadius(activeChar, 1000, true, false))
    		{
    			trapInstance.getKnownList().addKnownObject(activeChar);
    		}
    	}
	}

	/**
	 * Receives a targeted trap and checks if it really a trap, and what is the max level of trap that can
	 * be removed. If this is a trap and maxLevelToRemove > trap's level removes it
	 * @param trapObject
	 * @param maxLevelToRemove
	 * @return true if trap was removed
	 */
	public boolean removeOneTrap(L2Object trapObject , int maxLevelToRemove)
	{
		L2Character maybeTrapNpc = ((L2NpcInstance)trapObject);
		L2TrapInstance theTrap = null;

		for (L2TrapInstance trap : _trapQueue)
		{
			if (trap.getTrapObject().equals(maybeTrapNpc) && trap.getTrapLevel() < maxLevelToRemove)
			{
				theTrap = trap;
				break;
			}
		}

		if (theTrap != null)
		{
			theTrap.eraseMe();
			return true;
		}
		return false;
	}

	/**
	 * Removes all traps in the area, with a maxLevelToRemove > trap level condition
	 * @param x
	 * @param y
	 * @param z
	 * @param radius
	 * @param maxLevelToRemove
	 * @return true if a trap was removed
	 */
	public boolean removeTrap(int x , int y , int z , int radius , int maxLevelToRemove)
	{
		Vector<L2TrapInstance> trapList = getTrapsInZone(x , y , z , radius);
		boolean returnValue = false;

		for (L2TrapInstance trap : trapList)
		{
			if (_trapQueue.contains(trap) && trap.getTrapLevel() < maxLevelToRemove)
			{
				trap.eraseMe();
				_trapQueue.remove(trap);
				returnValue = true;
			}
		}
		trapList.clear();

		return returnValue;
	}

	/**
	 * Detects all traps in the area, with a maxLevelToDetect > trap level condition
	 * @param x
	 * @param y
	 * @param z
	 * @param radius
	 * @param maxLevelToDetect
	 * @return
	 */
	public boolean detectTrap(int x , int y , int z, int radius , int maxLevelToDetect)
	{
		Vector<L2TrapInstance> trapList = getTrapsInZone(x , y , z , radius);
		boolean returnValue = false;

		for (L2TrapInstance trap : trapList)
		{
			if (_trapQueue.contains(trap) && trap.getTrapLevel() < maxLevelToDetect)
			{
				L2Character trapObject = trap.getTrapObject();
				if (trapObject != null)
				{
					trapObject.decayMe();
					trapObject.setIsVisible(true);
					trap.setIsDetected();
					trapObject.spawnMe();
					((L2NpcInstance)trapObject).setUnTargetable(false);
					returnValue = true;
				}
			}
		}
		trapList.clear();

		return returnValue;
	}

	/**
	 * Adds a trap to the L2World
	 * @param caster - L2Character that summoned the trap
	 * @param triggerSkill - that summoned the trap
	 */
	public void addTrap(L2Character caster , L2Skill triggerSkill)
	{
		try
		{
			L2TrapInstance thisTrap = new L2TrapInstance(IdFactory.getInstance().getNextId() , NpcTable.getInstance().getTemplate(13037));
			thisTrap.onSpawn(caster, triggerSkill);
			_trapQueue.add(thisTrap);
		}
		catch(Throwable t)
		{
			_log.warn("Could not add a trap: " + t);
		}
	}

	/**
	 * Returns all the L2World traps in a Vector
	 * @return Vector of L2TrapInstance(s)
	 */
	public Vector<L2TrapInstance> getTrapQueue()
	{
		return _trapQueue;
	}

	/**
	 * Returns all traps in the given area
	 * @param x
	 * @param y
	 * @param z
	 * @param radius
	 * @return Vector of L2TrapInstance(s)
	 */
	private Vector<L2TrapInstance> getTrapsInZone(int x , int y , int z, int radius)
	{
		Vector<L2TrapInstance> trapList = new Vector<L2TrapInstance>();
		for (L2TrapInstance t : _trapQueue)
		{
			L2Character trap = t.getTrapObject();
			if (trap != null)
			{
				if (trap.isInsideRadius(x, y, z, radius, true, false))	trapList.add(t);
			}
		}
		return trapList;
	}

	public void delete(L2TrapInstance trap)
	{
		if (_trapQueue.contains(trap.getTrapObject()))
			_trapQueue.remove(trap.getTrapObject());

		if (_trapQueue.contains(trap))
			_trapQueue.remove(trap);

		try
		{
			((L2NpcInstance)(trap.getTrapObject())).deleteMe();
			if (((L2NpcInstance)(trap.getTrapObject())).getSpawn() != null)
			{
				((L2NpcInstance)(trap.getTrapObject())).getSpawn().stopRespawn();
				SpawnTable.getInstance().deleteSpawn(((L2NpcInstance)(trap.getTrapObject())).getSpawn(), false);
			}
			trap.deleteMe();
			if (trap.getSpawn() != null)
			{
				trap.getSpawn().stopRespawn();
				SpawnTable.getInstance().deleteSpawn(trap.getSpawn(), false);
			}
		}
		catch(Throwable t){}
	}
}