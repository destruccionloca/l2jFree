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
package com.l2jfree.gameserver.model.actor.knownlist;

import java.util.Map;

import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfree.gameserver.util.Util;
import com.l2jfree.util.L2Collections;
import com.l2jfree.util.SingletonMap;



public class CharKnownList extends ObjectKnownList
{
    // =========================================================
    // Data Field
    private Map<Integer, L2PcInstance> _knownPlayers;
    
    // =========================================================
    // Constructor
    public CharKnownList(L2Character activeChar)
    {
        super(activeChar);
    }

    // =========================================================
    // Method - Public
    @Override
    public boolean addKnownObject(L2Object object, L2Character dropper)
    {
        if (!super.addKnownObject(object, dropper))
            return false;

        if (object instanceof L2PcInstance)
            getKnownPlayers().put(object.getObjectId(), (L2PcInstance)object);
        
        return true;
    }

    /**
     * Return True if the L2PcInstance is in _knownPlayer of the L2Character.<BR><BR>
     * @param player The L2PcInstance to search in _knownPlayer
     */
    public final boolean knowsThePlayer(L2PcInstance player) { return getActiveChar() == player || getKnownPlayers().containsKey(player.getObjectId()); }
    
    /** Remove all L2Object from _knownObjects and _knownPlayer of the L2Character then cancel Attak or Cast and notify AI. */
    @Override
    public void removeAllKnownObjects()
    {
        super.removeAllKnownObjects();
        getKnownPlayers().clear();

        // Set _target of the L2Character to null
        // Cancel Attack or Cast
        getActiveChar().setTarget(null);

        // Cancel AI Task
        if (getActiveChar().hasAI())
            getActiveChar().setAI(null);
    }
    
    @Override
    public boolean removeKnownObject(L2Object object)
    {
        if (!super.removeKnownObject(object))
            return false;

        if (object instanceof L2PcInstance)
            getKnownPlayers().remove(object.getObjectId());
        
        // If object is targeted by the L2Character, cancel Attack or Cast
        if (object == getActiveChar().getTarget())
            getActiveChar().setTarget(null);

        return true;
    }

    // =========================================================
    // Method - Private

    // =========================================================
    // Property - Public

    public L2Character getActiveChar() { return (L2Character)_activeObject; }
    
    @Override
    public int getDistanceToForgetObject(L2Object object) { return 0; }

    @Override
    public int getDistanceToWatchObject(L2Object object) { return 0; }

	public Iterable<L2Character> getKnownCharacters()
	{
		return L2Collections.filteredIterable(L2Character.class, getKnownObjects().values());
	}
	
	public Iterable<L2Character> getKnownCharactersInRadius(final int radius)
	{
		return L2Collections.filteredIterable(L2Character.class, getKnownObjects().values(), new L2Collections.Filter<L2Character>() {
			public boolean accept(L2Character obj)
			{
				if (!Util.checkIfInRange(radius, getActiveChar(), obj, true))
					return false;
				
				return obj instanceof L2PlayableInstance || obj instanceof L2NpcInstance;
			}
		});
	}
	
    public final Map<Integer, L2PcInstance> getKnownPlayers()
    {
        if (_knownPlayers == null) _knownPlayers = new SingletonMap<Integer, L2PcInstance>().setShared();
        return _knownPlayers;
    }

	public final Iterable<L2PcInstance> getKnownPlayersInRadius(final int radius)
	{
		return L2Collections.filteredIterable(L2PcInstance.class, getKnownPlayers().values(), new L2Collections.Filter<L2PcInstance>() {
			public boolean accept(L2PcInstance player)
			{
				return Util.checkIfInRange(radius, getActiveChar(), player, true);
			}
		});
	}
}
