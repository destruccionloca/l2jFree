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


import java.util.List;
import java.util.concurrent.Future;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillLaunched;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.tools.geometry.Point3D;

/**
 * Handler for Radius/Fusion skill effects. Fusion skills = Radius skills mostly...
 * This class handles Radius-Buff skills, which are Symbol of... and massive area
 * damage skills, which are MDAM skills.
 * MDAM skills are (like land-mines) called upon when you enter the radius of the
 * casted skill.  
 * @author darki699
 */

public class EffectRadiusSkill
{
	
	private class RadiusSkill
	{
		private L2Spawn 	_effect;
		private L2Character _caster;
		private Point3D		_point;
		private long 		_lifeCycle;
		
		/**
		 * Constructor - receives the caster and the spell radius
		 * @param caster
		 * @param point
		 */
		RadiusSkill(L2Character caster , Point3D point)
		{
			_caster 	= caster;
			_point 		= point;
		}
		
		/**
		 * @return the x,y,z of this radius, contained inside a Point3D object
		 */
		public Point3D getLocation()
		{
			return _point;
		}
		
		/**
		 * @return the caster of this radius skill
		 */
		public L2Character getCaster()
		{
			return _caster;
		}
		
		/**
		 * Set the life time for this object. Time is received in seconds, converted to milliseconds
		 * and added to System.currentTimeMillis() so we know when to kill it
		 * @param time
		 */
		public void setLifeCycle(int time)
		{
			_lifeCycle 	= (time*1000) + System.currentTimeMillis();
            _caster.sendPacket(new SetupGauge(1, time*1000));
		}
		
		/**
		 * @return true if the life cycle is over and process should sigterm
		 */
		public boolean isLifeCycleOver()
		{
			return (System.currentTimeMillis() > _lifeCycle);
		}
		
		/**
		 * Destroys <b>this</b> instance of RadiusSkill
		 * @return <b>this</b> object
		 */
		public RadiusSkill destroyMe()
		{
			if (_caster != null)
			{
				_caster.sendPacket(new SetupGauge(1, 0));
			}
			_caster = null;
			_point 	= null;
			unspawnEffect();
			return this;
		}
		
		/**
		 * Initialize the radius skill effect  -  tnx for the tip Apocalipce =)
		 * @param id - the skill id
		 */
		public void setEffect(int id)
		{
			if (id > 400 && id < 500)
			{
				id = 13030;
			}
			else if (id > 1400 && id < 1500)
			{
				id = 13018;
			}
			else 
			{
				return;
			}
			spawnEffect(id);
		}
		
		/**
		 * Spawns the Radius skill effect
		 */
		private void spawnEffect(int id)
		{
			try
			{
				_effect = new L2Spawn(NpcTable.getInstance().getTemplate(id));
				_effect.setLocx(_point.getX());
				_effect.setLocy(_point.getY());
				_effect.setLocz(_point.getZ());
				_effect.setAmount(1);
				_effect.setRespawnDelay(1);
				SpawnTable.getInstance().addNewSpawn(_effect, false);
				_effect.init();
				_effect.getLastSpawn().getStatus().setCurrentHp(999999999);
				_effect.getLastSpawn().decayMe();
				_effect.getLastSpawn().spawnMe();
				_effect.getLastSpawn().setRadiusSkillsAffect(true);
			}
			catch(Throwable t){}			
		}
		
		/**
		 * Unspawns the Radius skill effect
		 */
		private void unspawnEffect()
		{
			if (_effect != null)
			{
				try
				{
					_effect.getLastSpawn().deleteMe();
					_effect.stopRespawn();
					SpawnTable.getInstance().deleteSpawn(_effect, false);
					_effect = null;
				}
				catch(Throwable t){}
			}
		}
	}
	
	private static FastMap<RadiusSkill , L2Skill> radiusSkillUsers;
	private static EffectRadiusSkill _instance;

	/**
	 * Initialize a empty radius-skill-queue, which holds the location of the skills
	 * and the trigger skill (the skill that calls the shot/triggered skill).
	 */
	public EffectRadiusSkill()
	{
		radiusSkillUsers = new FastMap<RadiusSkill , L2Skill>();
	}
	
	/**
	 * This should be a static instance, since one queue holds all radius skills.
	 */
	public static EffectRadiusSkill getInstance()
	{
		if (_instance == null)
		{
			_instance = new EffectRadiusSkill();
		}
		return _instance;
	}
	
	/**
	 * Override to add a Radius-Skill with no actual cast location to the radius-skill-queue.
	 * The function uses the radius around the activeChar, instead of a given x,y,z
	 * @param activeChar - L2Character the caster, radius is set around his cast location.
	 * @param skill - The trigger skill that starts the action
	 */
	public void addRadiusSkill(L2Character activeChar , L2Skill skill)
	{
		addRadiusSkill(activeChar.getX() , activeChar.getY() , activeChar.getZ() , activeChar , skill);
	}
	
	/**
	 * Adds a radius skill to the queue, using a given x,y,z for the center of the radius, the
	 * activeChar is the caster, and the skill with the radius.
	 * @param spellX - int x location 
	 * @param spellY - int y location
	 * @param spellZ - int z location
	 * @param activeChar - the L2Character caster
	 * @param skill - the trigger skill
	 */
	public void addRadiusSkill(int spellX , int spellY , int spellZ , L2Character activeChar , L2Skill skill)
    {
		// Place the skill in the given x,y,z coords, it's cast from that point.
		RadiusSkill radiusSkill = new RadiusSkill(activeChar , new Point3D(spellX , spellY, spellZ));
		radiusSkillUsers.put(radiusSkill, skill);
		
    	// Caster gets the initial skill effects.
    	L2Effect effects[] = skill.getEffects(activeChar, activeChar);
    	
    	// Set the time to remove this skill from the radius-skill-queue
		RemoveRadiusSkill removeRadiusSkill = new RemoveRadiusSkill(radiusSkill,skill); 
		Future task = ThreadPoolManager.getInstance().scheduleGeneral(removeRadiusSkill, effects[0].getTotalTaskTime()*1000);
		removeRadiusSkill.setTask(task);
		
		//Set the time in order to kill the object
		radiusSkill.setLifeCycle(effects[0].getTotalTaskTime());

		//Set the Radius of the Skill "Effect" - effect will be removed when this RadiusSkill instance is destroyed.
		radiusSkill.setEffect(skill.getId());

		// At this point we need to make a first seperation between BUFF radius skills, to MDAM radius skills.
		// This seperation is made in the triggered/shot skills and not the calling skill.
		// While the BUFF skills remove the trigger skill effect, the MDAM shot skills do not.   
		L2Skill tSkills[] = skill.getTriggeredSkills();
		
		if (tSkills != null)
		{
			if (tSkills[0].getSkillType() != SkillType.MDAM)
			{
				activeChar.removeEffect(effects[0]);
			}
		}
    }
    
    /**
     * Class removes a radius skill from the queue at a given time 
     * @author darki699
     */
	private class RemoveRadiusSkill implements Runnable
    {
    	Future 		_task 	= null;
    	RadiusSkill _radiusSkill;
    	L2Skill 	_skill;
    	
    	void setTask(Future task)
    	{
    		_task = task;
    	}
    	
    	RemoveRadiusSkill(RadiusSkill radiusSkill, L2Skill s)
    	{
    		_radiusSkill 	= radiusSkill;
    		_skill 			= s;
    	}
    	
    	public void run()
    	{
    		if (_task != null)
    		{
    			_task.cancel(true);
    			_task = null;
    		}
    		
    		removeRadiusSkill(_radiusSkill , _skill);
    	}
    }
    
    /**
     * Removes a radius skill from the radius-skill-queue, and destroys the RadiusSkill object
     */
	private void removeRadiusSkill(RadiusSkill _radiusSkill , L2Skill _skill)
	{
		try
		{
			FastMap.Entry<RadiusSkill, L2Skill> e = fetchEntry(_radiusSkill.getCaster() , _skill);
			if (e != null)
			{
	      	  	_radiusSkill.destroyMe();
	      	  	e.setValue(null);
			}

			radiusSkillUsers.remove(null);			
		}
		catch(Throwable t){}
	}
	
	/**
     * Core of this class - For Fusion except MDAM fusion skills. 
     * Checks radius skill effects on characters.
     * Function decides if a radius skill should be removed, replaced, executed, etc...
     * @param activeChar - the character being checked.
     */
	public void checkRadiusSkills(L2Character activeChar)
    {
    	// No need to check null characters ;]
		if (activeChar == null)
    		return;
    	
		// L2PcInstances trigger the check on all OTHER L2Characters instances, this is done in order to save
		// CPU and memory usage.
    	if (activeChar instanceof L2PcInstance)
    	{
        	// If we check the L2PcInstance let's check the L2Characters it knows as well
    		for (L2Character knownChar : activeChar.getKnownList().getKnownCharacters())
    		{
        		// This IF is done to save us from deadlocks (x->y->x->y->x->... is bad) 
    			if (!(knownChar instanceof L2PcInstance)) checkRadiusSkills(knownChar);
    		}
    	}
    	
    	List<Integer> skillIds = new FastList<Integer>();
    	
    	refreshRadiusSkillList();
    	
    	// If the queue is empty we don't need to check all this. If the queue is not empty, let's do the math:
    	// We add to the skillIds list all the skills that affect this L2Character. if a skill should affect but doesn't
    	// We cast it here. All these skill IDs are saved in the list initiated above.
    	if (!radiusSkillUsers.isEmpty())
    	{
        	/* Check to add radius skills to this character */
    		for (FastMap.Entry<RadiusSkill, L2Skill> e = radiusSkillUsers.head(), end = radiusSkillUsers.tail(); (e = e.getNext()) != end;)
    		{
    	          
    			Point3D 	key 	= e.getKey().getLocation(); 
    	        L2Skill 	value 	= e.getValue();
    	          
    	          if (key == null || value == null)
    	        	  continue;
    	          
    	          // is the checked char inside the radius of the effect.
    	          if (activeChar.isInsideRadius(key.getX(), key.getY(), key.getZ(), value.getEffectRange(), true, false))
    	          {
    	        	  L2Skill skills[] = value.getTriggeredSkills();
    	        	  
    	        	  if (skills != null)
    	        	  {
    	        		  for (L2Skill skill : skills)
    	        		  {
    	        			  if (skill == null)
    	        				  continue;
    	        			  // Shouldn't check isOffensive() since there are debuffs as well...
    	        			  else if (skill.getSkillType() == SkillType.MDAM) 
    	        				  continue;
    	        			  
    	        			  boolean replace = true;
    	        			  for (L2Effect effectExist : activeChar.getAllEffects())
    	        			  {
    	        				  // No need to cast twice if the effect is already there.
    	        				  if (effectExist.getSkill() == skill)
    	        				  {
    	        					  replace = false;
    	        					  break;
    	        				  }
    	        			  }
    	        			  
    	        			  if (replace)
    	        			  {
   	        					  activeChar.doCast(skill);    	        					  
    	        			  }
    	        			  
    	        			  skillIds.add(skill.getId());
    	        		  }
    	        	  }
    	          }
    		}
    	}
    	
    	/* Skills to remove from the L2Character */
    	if (activeChar.getRadiusSkillsAffect() != null)
    	{
       		for (Integer id : activeChar.getRadiusSkillsAffect())
        	{
				//If the skill isn't contained inside the skillIds list, then this checked char shouldn't have it 
       			if (!skillIds.contains(id))
       			{
       				activeChar.stopSkillEffects(id.intValue());       				
       			}
       		}
       		//all invalid effects are already removed, we init the character's list of skill effects
       		activeChar.setRadiusSkillsAffect(null);
    	}

		// The list contains the new skills that affect this char, we need to save it.
    	if (!skillIds.isEmpty())
		{
			activeChar.setRadiusSkillsAffect(skillIds.toArray(new Integer[skillIds.size()]));
		}
    }
	
	/**
	 * If the caster or a running Radius Effect is dead, offline, null or the effect finished it's life cycle
	 * and should be terminated, this function will terminate it.
	 */
	private void refreshRadiusSkillList()
	{
		if (radiusSkillUsers.isEmpty())
			return;
		
		boolean changed = false;
		for (FastMap.Entry<RadiusSkill, L2Skill> e = radiusSkillUsers.head(), end = radiusSkillUsers.tail(); (e = e.getNext()) != end;)
		{
	        L2Character	caster	= e.getKey().getCaster();
			boolean kill		= e.getKey().isLifeCycleOver();
	        
			if (caster == null)
				kill = true;
			else if (caster.isAlikeDead())
				kill = true;
			else if (caster instanceof L2PcInstance)
			{
				if (((L2PcInstance)caster).isOnline() == 0)
					kill = true;
			}
			
			if (kill)
	        {
	        	e.getKey().destroyMe();
	        	e.setValue(null);
	        	changed = true;
	        	continue;
	        }
	        
		}
		
		if (changed)
		{
			FastMap<RadiusSkill , L2Skill> tempMap = new FastMap<RadiusSkill , L2Skill>();
			for (FastMap.Entry<RadiusSkill, L2Skill> e = radiusSkillUsers.head(), end = radiusSkillUsers.tail(); (e = e.getNext()) != end;)
			{
				if (e.getValue() != null)
					tempMap.put(e.getKey(), e.getValue());
			}
			radiusSkillUsers.clear();
			radiusSkillUsers = tempMap;
		}
	}

	/**
	 * Controls the fusion animation skill cast on one target
	 * @param caster
	 * @param target
	 * @param triggerSkill - animation skill
	 * @param shotSkill - damage skill
	 */
	private void fusionMDAM(L2Character caster , L2Character target , L2Skill triggerSkill)
	{
		  L2Object[] targets = new L2Object[1];
		  targets[0] = (L2Object)target;
		  
		  //For animation purposes only
		  for (L2Character knownChar : target.getKnownList().getKnownPlayers().values())
		  {
			  target.broadcastPacket(new MagicSkillLaunched(knownChar, triggerSkill.getId(), triggerSkill.getLevel(), targets));
		  }

	}
	
	
	/**
     * Core of this class for Fusion Damage skills ONLY. 
     * Checks radius skill effects on characters.
     * Function decides if a radius skill should be removed, replaced, executed, etc...
     * @param caster - Only the caster may call this function, or the damage will be wrong
     */
	public void checkRadiusDamageSkills(L2Character caster , L2Skill skill)
    {
    	// No need to check null  ;]
		if (caster == null || skill == null)
    		return;

    	/* Check to match radius skills to this caster */
		FastMap.Entry<RadiusSkill, L2Skill> e = fetchEntry(caster , skill);
		
		if (e == null)
			return;

		Point3D key 	= e.getKey().getLocation(); 
		L2Skill	value 	= e.getValue();

		if (key == null || value == null)
      	  return;
   		
        for (L2Character activeChar : caster.getKnownList().getKnownCharacters())
   		{
   			if (activeChar == null)
   				continue;
	    	// is the checked char inside the radius of the effect.
   			else if (activeChar.isInsideRadius(key.getX(), key.getY(), key.getZ(), value.getEffectRange(), true, false))
	    	{
	    		if (skill.getSkillType() == SkillType.MDAM)
   	    		{
	    			fusionMDAM(caster , activeChar , value);
   	    		}
	    	}
   		}
        
        caster.doCast(skill);
    }
	
	/**
	 * returns a FastMap.Entry<RadiusSkill, L2Skill> of the Radius Skill in the radius-skill-queue
	 * if one exists. If no entry matching exists returns null
	 * @param caster the caster of the radius skill
	 * @param skill either the shot-skill or the triggered skill
	 * @return FastMap.Entry<RadiusSkill, L2Skill> containing the RadiusSkill and shot-skill
	 */
	private FastMap.Entry<RadiusSkill, L2Skill> fetchEntry(L2Character caster , L2Skill skill)
	{

		for (FastMap.Entry<RadiusSkill, L2Skill> e = radiusSkillUsers.head(), end = radiusSkillUsers.tail(); (e = e.getNext()) != end;)
		{
			if (e.getKey() == null || e.getValue() == null)
				continue;
			else if (e.getKey().getCaster() != caster)
				continue;
			else if (e.getValue() != skill)
			{
				if (e.getValue().getTriggeredSkills() != null)
				{
					L2Skill skills[] = e.getValue().getTriggeredSkills();
					for (L2Skill s : skills)
					{
						if (s == skill)
							return e;
					}
				}
				continue;
			}
	        
	        return e;
		}   
		return null;
	}
	
	/**
	 * Returns a target list for the caster's radius skill
	 * @param caster - the caster
	 * @param skill - the fusion skill
	 * @param onlyFirst - returns only the first target if true
	 * @return array of L2Object targets
	 */
	public L2Object[] getTargetList(L2Character caster, L2Skill skill, boolean onlyFirst)
	{
		FastMap.Entry<RadiusSkill, L2Skill> e = fetchEntry(caster , skill);
		
		if (e == null)
		{
			return null;
		}
		else if (e.getKey() == null || e.getValue() == null)
		{
			return null;
		}
		
		int 	x 		= e.getKey().getLocation().getX(),
				y 		= e.getKey().getLocation().getY(),
				z 		= e.getKey().getLocation().getZ(),
				radius 	= e.getValue().getEffectRange();
		
		FastList<L2Character> targetList = new FastList<L2Character>();
		
		for (L2Character target : caster.getKnownList().getKnownCharacters())
		{
			if (target == null)
				continue;
			else if (target.isInsideRadius(x, y, z, radius, true, false))
			{
				targetList.add(target);
				if (onlyFirst)
				{
					break;
				}
			}
		}
		
		if (!targetList.isEmpty())
		{
			return targetList.toArray(new L2Character[targetList.size()]);
		}
		return null;
	}
	
	
	/**
	 * Sets the cast target of the caster = new target is the closest character to the pointed target
	 * @param caster
	 * @param x
	 * @param y
	 * @param z
	 * @param radius
	 */
	public void setInitialTarget(L2Character caster , int x , int y , int z , int radius)
	{
		L2Character selectedTarget = null;
		int closest = -1;
		
		for (L2Character target : caster.getKnownList().getKnownCharacters())
		{
			if (target == null)
				continue;
			else if (target.isInsideRadius(x, y, z, radius, true, false))
			{
				int close = Math.abs(target.getX()-x) + Math.abs(target.getY()-y);
				if (close < closest || closest < 0)
				{
					closest = close;
					selectedTarget = target;
				}
			}
		}
		
		if (selectedTarget != null)
		{
			setTarget(caster , selectedTarget);
		}
	}
	
	/**
	 * Sets the activeChar's target 
	 * @param activeChar
	 * @param target
	 */
	private void setTarget(L2Character activeChar , L2Character target)
	{
		// set the target again on the players that targeted this _caster
		activeChar.setTarget(target);
		MyTargetSelected my = new MyTargetSelected(target.getObjectId(), activeChar.getLevel() - target.getLevel());
		activeChar.sendPacket(my);

		// Send a Server->Client packet StatusUpdate of the L2NpcInstance to the L2PcInstance to update its HP bar
		StatusUpdate su = new StatusUpdate(target.getObjectId());
		su.addAttribute(StatusUpdate.CUR_HP, (int) target.getStatus().getCurrentHp());
		su.addAttribute(StatusUpdate.MAX_HP, target.getMaxHp());
		activeChar.sendPacket(su);
		target.setAttackingChar(activeChar);
	}
	
	/**
	 * Checks if the activeChar has a active radius skill, if so and only if the radius skill has an 
	 * abnormal affect on the caster, removes the radius skill and effect from the caster 
	 * @param activeChar - the caster
	 * @return boolean true if the radius skill was removed, false if not.
	 */	
	public boolean abortRadiusSkill(L2Character activeChar)
	{
		if (activeChar == null || radiusSkillUsers.isEmpty())
			return false;
		
		// Can only abort Radius Skills in control of the caster, such as those that immoblize
		for (FastMap.Entry<RadiusSkill, L2Skill> e = radiusSkillUsers.head(), end = radiusSkillUsers.tail(); (e = e.getNext()) != end;)
		{
			if (e.getKey() == null || e.getValue() == null)
				continue;
			else if (e.getKey().getCaster() != activeChar)
				continue;
			else if (e.getValue() != null)
			{
				for (L2Effect effect : activeChar.getAllEffects())
				{
					if (effect.getSkill() == e.getValue())
					{
						effect.exit();
						removeRadiusSkill(e.getKey(), e.getValue());
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	
}