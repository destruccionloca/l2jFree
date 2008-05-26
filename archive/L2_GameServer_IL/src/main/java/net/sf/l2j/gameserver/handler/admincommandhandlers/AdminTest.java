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
package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.serverpackets.SocialAction;
import net.sf.l2j.gameserver.util.Util;

public class AdminTest implements IAdminCommandHandler
{
    private static final int REQUIRED_LEVEL = Config.GM_TEST;
    /*
    private static final String[] ADMIN_TEST_COMMANDS =
    {
        "admin_test", "admin_stats", "admin_skill_test", 
        "admin_st", "admin_mp", "admin_known"
    };
*/
    private static final String[][] ADMIN_COMMANDS = {
    	{"admin_stats",
    		
    		"Shows server performance statistics.",
    		"Usage: stats"
    	},
    	{"admin_docast",
    		
    		"Test skill animation on target.",
    		"Usage: //docast <skill id> <skill level> <skill time>",
    		"Options:",
    		"skill id - Id of skill animation that you want to test",
    		"skill level - skill level of the skill you want to display",
    		"skill time - the duration of the casting animation"
    	},
    	{"admin_docastself",
    		
    		"Test skill animation on oneself.",
    		"Usage: //docastself <skill id> <skill level> <skill time>",
    		"Options:",
    		"skill id - Id of skill animation that you want to test",
    		"skill level - skill level of the skill you want to display",
    		"skill time - the duration of the casting animation"
    	},
    	{"admin_targets",
    		
    		"List skill targets (only multiple targets supported).",
    		"Usage: targets skillID <level>",
    		"Options:",
    		"skillID - Id of skill, target list you want to see",
    		"<level> - skill level, Default is 1",
    	},
    	{"admin_mp",
    		
    		"Enable/disable client-server packets monitor.",
    		"Usage: mp |dump",
    		"Options:",
    		"dump - dump currently cuptured packets",
    	},
    	{"admin_known",
    		
    		"Enable/disable knownlist ingame debug messages.",
    		"Usage: knownlist",
    	},
    	{"admin_heading",
    		
    		"Show usefull info about target heading and angle.",
    		"Usage: heading",
    	}
    };

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IAdminCommandHandler#useAdminCommand(java.lang.String, net.sf.l2j.gameserver.model.L2PcInstance)
     */
    public boolean useAdminCommand(String command, L2PcInstance activeChar)
    {
        if (!Config.ALT_PRIVILEGES_ADMIN)
            if (activeChar.getAccessLevel() < REQUIRED_LEVEL) return false;
        
        StringTokenizer st = new StringTokenizer(command, " ");
        
        String cmd = st.nextToken();  // get command
        
        if (cmd.equals("admin_stats"))
        {
            for (String line : ThreadPoolManager.getInstance().getStats())
            {
                activeChar.sendMessage(line);
            }
        }
        else if (cmd.equals("admin_docast") || cmd.equals("admin_docastself"))
        {
            L2Object obj = activeChar.getTarget();
            L2Character caster = null;
            if (obj == null || !(obj instanceof L2Character))
            {
                caster = activeChar;
            }
            else
            {
                caster = (L2Character)obj;
            }

            int skillId = 0, skillLevel = 0, skillTime = 0;
            try
            {
                skillId = Integer.parseInt(st.nextToken());
                skillLevel = Integer.parseInt(st.nextToken());
                skillTime = Integer.parseInt(st.nextToken());
            }
            catch(Exception e)
            {
                activeChar.sendMessage("Usage: //docast <skill id> <skill level> <skill time>");
                return false;
            }
            L2Character target = null;
            if (caster.getTarget() == null || !(caster.getTarget() instanceof L2Character) || cmd.equals("admin_docastself"))
            {
                target = caster;
            }
            else
                target = (L2Character)caster.getTarget();

            caster.broadcastPacket(new MagicSkillUser(caster, target, skillId, skillLevel, skillTime, 0));
            activeChar.sendMessage("Did a cast for skill: "+skillId+", level: "+skillLevel);
        }
        else if (cmd.equals("admin_mp"))
        {
            activeChar.sendMessage("Packet monitor not supported.");
        }
        else if (cmd.equals("admin_known"))
        {
            Config.TEST_KNOWNLIST = !Config.TEST_KNOWNLIST;
            activeChar.sendMessage("Knownlist debug is "+(Config.TEST_KNOWNLIST?"enabled":"disabled")+".");
        }
        else if (cmd.equals("admin_targets"))
        {
        	L2Skill skill;
        	int skillId = 0;
        	int skillLvl = 1;
        	
        	try
            {
            	skillId = Integer.parseInt(st.nextToken());
            	if (st.hasMoreTokens())
            		skillLvl = Integer.parseInt(st.nextToken());  
            }
            catch(Exception e)
            {
            }

            if (skillId > 0)
        	{
                int skillLvlMax = SkillTable.getInstance().getMaxLevel(skillId, 1);
                
                if (skillLvl > skillLvlMax)
                	skillLvl = skillLvlMax;
            	
                skill = SkillTable.getInstance().getInfo(skillId, skillLvl);
                if (skill != null)
                {
                	L2Object[] targetList = skill.getTargetList(activeChar);
            		
            		if (targetList.length > 0)
            		{
            			activeChar.sendMessage("Targets list fo skill "+skill.getName()+":");
                		
                		for (L2Object target : targetList)
                		{
                   			if (target instanceof L2NpcInstance)
                                activeChar.sendMessage("NPC: "+((L2NpcInstance) target).getName());
                   			if (target instanceof L2PcInstance)
                                activeChar.sendMessage("PC : "+((L2PcInstance) target).getName());
                   			if (target instanceof L2Summon)
                                activeChar.sendMessage("PET: "+((L2Summon) target).getName());
                		}
                                 
                		activeChar.sendMessage("Total targets: "+targetList.length);
            		}
            		else
            		{
            			activeChar.sendMessage("Targets list fo skill "+skill.getName()+" is empty.");
            		}
                }
                else
                	activeChar.sendMessage("Skill id "+skillId+" not found.");
        	}
            else
            	showAdminCommandHelp(activeChar,cmd);
        }
        if (cmd.equals("admin_heading"))
        {
            L2Object objTarget = activeChar.getTarget();
            
            if (objTarget != null && objTarget instanceof L2Character)
            {
            	double angleChar, angleTarget, angleDiff, maxAngleDiff = 45;
            	
                L2Character charTarget = (L2Character) objTarget;
                
                angleChar = Util.calculateAngleFrom(charTarget, activeChar);
                angleTarget = Util.convertHeadingToDegree(charTarget.getHeading());
                angleDiff = angleChar - angleTarget;
                
                activeChar.sendMessage("Target heading " + charTarget.getHeading() + ".");
                activeChar.sendMessage("Your heading " + activeChar.getHeading() + ".");
                activeChar.sendMessage("Target angle " + angleTarget + ".");
                activeChar.sendMessage("Your angle " + angleChar + ".");
                activeChar.sendMessage("Angle difference before correction " + angleDiff + ".");
                
                if (angleDiff <= -360 + maxAngleDiff) angleDiff += 360;
                if (angleDiff >= 360 - maxAngleDiff) angleDiff -= 360;
                
                activeChar.sendMessage("Angle difference after correction " + angleDiff + ".");
                activeChar.sendMessage("Is Behind ? " + activeChar.isBehindTarget() + ".");      
            }
            else
            	showAdminCommandHelp(activeChar,cmd);
        }
        return true;
    }

    /**
     * Test social action or NPC animation
     * @param activeChar
     * @param skill
     */
    public void adminTestSocial(L2PcInstance activeChar,int socId)
    {
    	L2Object target;
    	
       	if (activeChar.getTarget() != null && (activeChar.getTarget() instanceof L2Character))
    		target = activeChar.getTarget();
    	else 
    		target = activeChar;
        
       	SocialAction sa = new SocialAction(target.getObjectId(), socId);
       	((L2Character)target).broadcastPacket(sa);
    }
    
    /**
     * Show tips about command usage and syntax. 
     * @param command admin command name
     */    
    private void showAdminCommandHelp(L2PcInstance activeChar, String command)
    {
    	for (String[] element : ADMIN_COMMANDS) {
    		if (command.equals(element[0]))
    		{
    			for (int k=1; k < element.length; k++)
    				activeChar.sendMessage(element[k]);
    		}
    	}
    }
    
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IAdminCommandHandler#getAdminCommandList()
     */
    public String[] getAdminCommandList()
    {
    	String[] _adminCommandsOnly = new String[ADMIN_COMMANDS.length];
    	for (int i=0; i < ADMIN_COMMANDS.length; i++)
    	{
    		_adminCommandsOnly[i] = ADMIN_COMMANDS[i][0];
    	}
    	
        return _adminCommandsOnly;
    }
}
