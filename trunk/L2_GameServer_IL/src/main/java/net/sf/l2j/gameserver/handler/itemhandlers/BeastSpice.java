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
package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2TrainedPetInstance;
import net.sf.l2j.gameserver.model.base.Experience;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.lib.Rnd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class ...
 * author wtfx
 * Weee's goes to Sami.
 */

public class BeastSpice implements IItemHandler
{
	final static Log _log = LogFactory.getLog(BeastSpice.class.getName());
	
	private static int[] _itemIds =
		{ 6643,6644 };

	public synchronized void useItem(L2PlayableInstance playable,
			L2ItemInstance item)
	{
		L2PcInstance activeChar;
		
		
		if (playable instanceof L2PcInstance)
			activeChar = (L2PcInstance) playable;
		else if (playable instanceof L2PetInstance)
			activeChar = ((L2PetInstance) playable).getOwner();
		else
			return;

		if (activeChar.isInOlympiadMode())
		{
			activeChar
					.sendPacket(new SystemMessage(
							SystemMessage.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
			return;
		}

		if (activeChar.isAllSkillsDisabled())
		{
			ActionFailed af = new ActionFailed();
			activeChar.sendPacket(af);
			return;
		}
		
		int itemId = item.getItemId();
		double prob  = 0.5;
		
		if (itemId == 6643 || itemId == 6644) 
		{
			
			L2Character target = null;
			
			if(activeChar.getTarget() instanceof L2Attackable)
				 target = (L2Character)activeChar.getTarget();
			

			
			if (target != null && !target.isDead())
	        {
				
				if (!activeChar.isInsideRadius(activeChar.getTarget().getX(),activeChar.getTarget().getY(),200,true))
				{
					activeChar.sendPacket(new SystemMessage(
							SystemMessage.TARGET_TOO_FAR));
					return;
				}

				    SetupGauge sg = new SetupGauge(SetupGauge.BLUE, 2000);
				    activeChar.sendPacket(sg);
				    
				    try 
        			{ 
        				Thread.sleep(2000); 
        			}
        			catch ( InterruptedException e ) {} 
				    
					String destid  = null;
					String mobid = null;
					int mobcmp =((L2Attackable)target).getTemplate().idTemplate;
					
					//kookaburra stage 1
					//kookaburra stage 2
					//kookaburra stage 3
					//kookaburra stage 4
					if(mobcmp != 21451 && mobcmp != 21452 &&mobcmp != 21460 &&
					   mobcmp != 21470 && mobcmp != 21471 && mobcmp != 21481 &&
					   mobcmp != 21489 && mobcmp != 21490 &&  mobcmp != 21498 ){
						activeChar
						.sendPacket(new SystemMessage(
								SystemMessage.TARGET_IS_INCORRECT));
						return;
					}
					
					if(Rnd.get() > 0.5){
						
						target.setTarget(activeChar);
						
						((L2Attackable) target).addDamage(activeChar,1);
						/*activeChar
						.sendPacket(new SystemMessage(
								SystemMessage.NOTHING_HAPPENED));*/
						playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
						return;
					}
					playable.setTarget(null);
					
					
						if(mobcmp == 21451){
							destid = "21452";
							mobid = "21451";
						}else if(mobcmp ==21452){
							destid = "21460";
							mobid = "21451";
						}else if(mobcmp ==21460){
							if(Rnd.get() < prob){
								destid = "21468";
								mobid = "21451";
							}else{
								target.decayMe();
								createPet(activeChar,16017,itemId);
								playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
								return;
							}
							
						}
						//buffalo stage 1
						//buffalo stage 2
						//buffalo stage 3
						//buffalo stage 4
						else if(mobcmp == 21470){
							destid = "21471";
							mobid = "21470";
						}else if(mobcmp == 21471){
							destid = "21481";
							mobid = "21470";
						}else if(mobcmp == 21481){
							
							if(Rnd.get() < prob){
								destid = "21487";
								mobid = "21470";
							}else{
								target.decayMe();
								createPet(activeChar,16013,itemId);
								playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
								return;
							}
						}
						//couger stage 1
						//couger stage 2
						//couger stage 3
						//couger stage 4
						else if(mobcmp == 21489){
							destid = "21490";
							mobid = "21489";
						}else if(mobcmp == 21490){
							destid = "21498";
							mobid = "21489";
						}else if(mobcmp == 21498){
			
							if(Rnd.get() < prob){
								destid = "21506";
								mobid = "21489";
							}else{
								target.decayMe();
								createPet(activeChar,16015,itemId);
								playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
								return;
							}
						}
						
						target.getPoly().setPolyInfo("npc",destid,mobid);
						//target.teleToLocation(target.getX(), target.getY(), target.getZ(), false);
						
						//poly ani!
						L2Skill skill = SkillTable.getInstance().getInfo(2036, 1);
				        MagicSkillUser msu = new MagicSkillUser(activeChar,target, 2036, 1, skill.getSkillTime(), 0);
				        activeChar.broadcastPacket(msu);
	
			            target.decayMe();
			            target.spawnMe(target.getX(),target.getY(),target.getZ());
			            playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
			            
			            //Lets Attack the one that used the thing on me!
			            target.setTarget(activeChar);
						
			            ((L2Attackable) target).addDamage(activeChar,1);
			             activeChar.setTarget(activeChar);
					}
	        }
	}

	
	public void createPet(L2PcInstance activeChar, int destp, int itemId){
			
		if(activeChar.getPet() != null){   
			if (activeChar.getPet() instanceof L2TrainedPetInstance)
				activeChar.getPet().unSummon(activeChar);
			else
				return;
		}
			   
			
			L2NpcTemplate summonTemplate = NpcTable.getInstance().getTemplate(destp);
	        L2TrainedPetInstance summon = new L2TrainedPetInstance(IdFactory.getInstance().getNextId(), summonTemplate, activeChar, null);
			
	        summon.setName(summonTemplate.name);
	        //summon.setTitle(activeChar.getName());
	       // summon.setExpPenalty(expPenalty);
	     
	        summon.getStat().setExp(Experience.LEVEL[(summon.getLevel() % Experience.LEVEL.length)]);
	        
			summon.getStatus().setCurrentHp(summon.getMaxHp());
			summon.getStatus().setCurrentMp(summon.getMaxMp());
			summon.setHeading(activeChar.getHeading());
	    	summon.setRunning();
	    	
	    	summon.setFoodId(itemId);
	    	
			activeChar.setPet(summon);
	    		
	    	L2World.getInstance().storeObject(summon);
	        summon.spawnMe(activeChar.getX()+50, activeChar.getY()+100, activeChar.getZ());
	    		
	    	summon.setFollowStatus(true);
	        summon.setShowSummonAnimation(false); // addVisibleObject created the info packets with summon animation
	                                              // if someone comes into range now, the animation shouldnt show any more
	        
	        
			summon.startFeed(false);
			
			
			
			 if(activeChar.isMageClass())
			    	summon.startRecharge();
			    else
			    	summon.startHeal();
			 
			 summon.doBuff();
		 
        //activeChar.sendPacket(new PetInfo(summon));
	}

	public int[] getItemIds()
	{
		return _itemIds;
	}
}