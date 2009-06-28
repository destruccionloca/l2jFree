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
package com.l2jfree.gameserver.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

/**
 *
 * @author  KenM
 */
public abstract class L2Transformation
{
    protected final static Log _log = LogFactory.getLog(L2Transformation.class.getName());

    private final int _id;
    private final int _graphicalId;
    private final double _collisionRadius;
    private final double _collisionHeight;

    public static final int TRANSFORM_ZARICHE = 301;
    public static final int TRANSFORM_AKAMANAH = 302;

    protected boolean _canDoMeleeAttack = true;
    protected boolean _startFollowToCast = true;

    /**
     * 
     * @param id Internal id that server will use to associate this transformation 
     * @param graphicalId Client visible transformation id
     * @param duration Transformation duration in seconds
     * @param collisionRadius Collision Radius of the player while transformed
     * @param collisionHeight  Collision Height of the player while transformed
     */
    public L2Transformation(int id, int graphicalId, double collisionRadius, double collisionHeight)
    {
        _id = id;
        _graphicalId = graphicalId;
        _collisionRadius = collisionRadius;
        _collisionHeight = collisionHeight;
    }
    
    /**
     * 
     * @param id Internal id(will be used also as client graphical id) that server will use to associate this transformation 
     * @param duration Transformation duration in seconds
     * @param collisionRadius Collision Radius of the player while transformed
     * @param collisionHeight  Collision Height of the player while transformed
     */
    public L2Transformation(int id, double collisionRadius, double collisionHeight)
    {
        this(id, id, collisionRadius, collisionHeight);
    }
    
    /**
     * @return Returns the id.
     */
    public int getId()
    {
        return _id;
    }

    /**
     * @return Returns the graphicalId.
     */
    public int getGraphicalId()
    {
        return _graphicalId;
    }

    /**
     * @return Returns the collisionRadius.
     */
    public double getCollisionRadius(L2PcInstance player)
    {
        if (getId() >= 312 && getId() <= 318)
             return player.getBaseTemplate().getCollisionRadius();
        return _collisionRadius;
    }

    /**
     * @return Returns the collisionHeight.
     */
    public double getCollisionHeight(L2PcInstance player)
    {
        if (getId() >= 312 && getId() <= 318)
             return player.getBaseTemplate().getCollisionHeight();
        return _collisionHeight;
    }

    // Scriptable Events
    public abstract void onTransform(L2PcInstance player);
    
    public abstract void onUntransform(L2PcInstance player);

    public void addSkill(L2PcInstance player, int skillId, int skillLevel)
    {
        L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
        if (skill == null)
            _log.warn("Transformed skill "+skillId+" "+skillLevel+" not found!");
        else
            player.addSkill(skill, false);
    }

    public void removeSkill(L2PcInstance player, int skillId)
    {
        player.removeSkill(skillId);
    }

    // Override if necessary
    public void onLevelUp(L2PcInstance player)
    {
    }
    
    /**
     * Returns true if transformation can do melee attack
     */
    public boolean canDoMeleeAttack()
    {
    	return _canDoMeleeAttack;
    }
    
    /**
     * Returns true if transformation can start follow target when trying to cast an skill out of range
     */
    public boolean canStartFollowToCast()
    {
    	return _startFollowToCast;
    }

    /**
     * Returns true if the standard action buttons must be hidden while transformed. (most except Vanguard, Inquisitor etc.)
     */
	public boolean hidesActionButtons()
	{
		return true;
	}
}
