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
package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Sephiroth
 */
public class CurseDivinity implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS = { SkillType.CURSE_DIV };
    
    	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
    	{
      	if (activeChar.isAlikeDead())
            	return;
		
		double totalDamage = 0;
		L2Character _Effected = null;
		
		for (L2Object element : targets)
		{
			_Effected = (L2Character)element;

	      	boolean ss = false;
      	  	boolean bss = false;

	        	L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
      	  
	        	if (weaponInst != null)
	      	{
      	      	if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
	      	      {
      	          		bss = true;
            	    		weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
				}
	            
      	      	else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
            		{
	            		ss = true;
      	          		weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
            		}
			
				L2Skill curseDivinity = SkillTable.getInstance().getInfo(L2Skill.SKILL_CURSE_DIVINITY, activeChar.getSkillLevel(L2Skill.SKILL_CURSE_DIVINITY));	
	
				L2Effect[] effects = _Effected.getAllEffects();
				for (L2Effect e : effects)
				{
					if (e.getSkill().getSkillType()==SkillType.BUFF)
					{
						if (e.getSkill().getTargetType()==L2Skill.SkillTargetType.TARGET_ONE || e.getSkill().getTargetType()==L2Skill.SkillTargetType.TARGET_PARTY || e.getSkill().getTargetType()==L2Skill.SkillTargetType.TARGET_PARTY_MEMBER || e.getSkill().getTargetType()==L2Skill.SkillTargetType.TARGET_ALLY)
							totalDamage = totalDamage + (int) Formulas.getInstance().calcMagicDam(activeChar, (L2Character)element, curseDivinity, false, ss, bss);
					}
				}
				activeChar.sendDamageMessage(_Effected, (int)totalDamage, false, false, false);
				_Effected.reduceCurrentHp(totalDamage, activeChar);
			}
		}
	}
    
	public SkillType[] getSkillIds()
	{
      	return SKILL_IDS;
	}
}

