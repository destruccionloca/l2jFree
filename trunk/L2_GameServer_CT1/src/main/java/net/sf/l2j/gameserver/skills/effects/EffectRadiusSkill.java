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
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.tools.geometry.Point3D;

/**
 * 
 * @author darki699
 */

public class EffectRadiusSkill
{
	private static FastMap<Point3D , L2Skill> radiusSkillUsers;
	private static EffectRadiusSkill _instance;

	public EffectRadiusSkill()
	{
		radiusSkillUsers = new FastMap<Point3D , L2Skill>();
	}
	
	public static EffectRadiusSkill getInstance()
	{
		if (_instance == null)
		{
			_instance = new EffectRadiusSkill();
		}
		return _instance;
	}
	
	public void addRadiusSkill(L2Character activeChar , L2Skill skill)
    {
		Point3D point = new Point3D(activeChar.getX() , activeChar.getY(), activeChar.getZ());
    	radiusSkillUsers.put(point, skill);
		
    	L2Effect effects[] = skill.getEffects(activeChar, activeChar);
    	
		RemoveRadiusSkill removeRadiusSkill = new RemoveRadiusSkill(point,skill); 
		Future task = ThreadPoolManager.getInstance().scheduleGeneral(removeRadiusSkill, effects[0].getTotalTaskTime()*1000);
		removeRadiusSkill.setTask(task);
		
		activeChar.removeEffect(effects[0]);
    }
    
    private class RemoveRadiusSkill implements Runnable
    {
    	Future 	_task 	= null;
    	Point3D _point;
    	L2Skill _skill;
    	
    	void setTask(Future task)
    	{
    		_task = task;
    	}
    	
    	RemoveRadiusSkill(Point3D p, L2Skill s)
    	{
    		_point = p;
    		_skill = s;
    	}
    	
    	public void run()
    	{
    		if (_task != null)
    		{
    			_task.cancel(true);
    			_task = null;
    		}
    		
    		for (FastMap.Entry<Point3D, L2Skill> e = radiusSkillUsers.head(), end = radiusSkillUsers.tail(); (e = e.getNext()) != end;)
    		{
    	          Point3D key 	= e.getKey(); 
    	          L2Skill value = e.getValue();

    	          if (key.equals(_point) && value.equals(_skill))
    	          {
    	        	  e.setValue(null);
    	        	  break;
    	          }
    		}
    		radiusSkillUsers.remove(null);
    	}
    }
    
    public void checkRadiusSkills(L2Character activeChar)
    {
    	List<Integer> skillIds = new FastList<Integer>();
    	
    	if (!radiusSkillUsers.isEmpty())
    	{
        	/* Check to add radius skills to this character */
    		for (FastMap.Entry<Point3D, L2Skill> e = radiusSkillUsers.head(), end = radiusSkillUsers.tail(); (e = e.getNext()) != end;)
    		{
    	          
    			Point3D key 	= e.getKey(); 
    	        L2Skill value 	= e.getValue();
    	          
    	          if (key == null || value == null)
    	        	  continue;
    	          
    	          if (activeChar.isInsideRadius(key.getX(), key.getY(), key.getZ(), value.getEffectRange(), true, false))
    	          {
    	        	  L2Skill skills[] = value.getTriggeredSkills();
    	        	  
    	        	  if (skills != null)
    	        	  {
    	        		  for (L2Skill skill : skills)
    	        		  {
    	        			  if (skill == null)
    	        				  continue;
    	        			  
    	        			  boolean replace = true;
    	        			  for (L2Effect effectExist : activeChar.getAllEffects())
    	        			  {
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
				if (!skillIds.contains(id))
       			{
       				activeChar.stopSkillEffects(id.intValue());       				
       			}
       		}
     
       		activeChar.setRadiusSkillsAffect(null);
    	}

		if (!skillIds.isEmpty())
		{
			activeChar.setRadiusSkillsAffect(skillIds.toArray(new Integer[skillIds.size()]));
		}
    }	
}