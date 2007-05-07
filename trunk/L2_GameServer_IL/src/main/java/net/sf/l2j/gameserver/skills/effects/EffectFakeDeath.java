/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Env;
/**
 * @author mkizub
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
final class EffectFakeDeath extends L2Effect {

	public EffectFakeDeath(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	public EffectType getEffectType()
	{
		return EffectType.FAKE_DEATH;
	}
	
	/** Notify started */
	public void onStart() {
		getEffected().startFakeDeath();
     }
	
	/** Notify exited */
	public void onExit() {
		getEffected().stopFakeDeath(this);
	}
	
    public boolean onActionTime()
    {
		if(getEffected().isDead())
			return false;
        
        /*for (L2Object obj : getEffected().getKnownList().getKnownCharacters())
        {
            
            if ((obj != null) &&
                    (obj instanceof L2MonsterInstance 
                            || obj instanceof L2SiegeGuardInstance 
                            || obj instanceof L2GuardInstance
                            ))
                continue;
            
            if (((L2NpcInstance)obj).getTarget() == getEffected() && (!((L2NpcInstance)obj) instanceof L2DoorInstance) && ((L2NpcInstance)obj).getTarget() != null && !((L2NpcInstance)obj).isDead())
            {
                ((L2NpcInstance)obj).setTarget(null);
                ((L2NpcInstance)obj).getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, // Move Npc to Spawn Location
                                         new L2CharPosition(((L2NpcInstance)obj).getSpawn().getLocx(), ((L2NpcInstance)obj).getSpawn().getLocy(), ((L2NpcInstance)obj).getSpawn().getLocz(),0));
            }
        }
        
        if (!((L2PcInstance)obj).isDead() && ((L2PcInstance)obj) != null && ((L2PcInstance)obj) != getEffected() && ((L2PcInstance)obj).isInsideRadius(getEffected(),130,true,false))  //check if PC you Train is Close to you
        { 
            if (((L2PcInstance)obj).isMoving() && (NPC.getTemplate().aggroRange > 0) && (Rnd.get(100) < 75)); //If PC is moving give a chance to move agrro mobs on him.
            {
                NPC.setTarget(((L2PcInstance)obj));
                NPC.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK); //Train him!
            }
        }*/
        
        double manaDam = calc();
		
		if(manaDam > getEffected().getCurrentMp())
		{
			if(getSkill().isToggle())
			{
				SystemMessage sm = new SystemMessage(140);
				getEffected().sendPacket(sm);
				return false;
			}
		}
		
		getEffected().reduceCurrentMp(manaDam);
		return true;
    }
}

