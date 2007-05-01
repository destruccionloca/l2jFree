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
import net.sf.l2j.gameserver.network.SelectorThread;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.serverpackets.SocialAction;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.Util;

public class AdminTest implements IAdminCommandHandler
{
    private static final int REQUIRED_LEVEL = Config.GM_TEST;
    /*
    public static final String[] ADMIN_TEST_COMMANDS =
    {
        "admin_test", "admin_stats", "admin_skill_test", 
        "admin_st", "admin_mp", "admin_known"
    };
*/
    private static String[][] _adminCommands = {
    	{"admin_stats",                                       
    		
    		"Shows server performance statistics.",
    		"Usage: stats"
    	}, 				                       
    	{"admin_skill",                                 
    		
    		"Test skill animation.",
    		"Usage: skill skillID <level>",
    		"Options:",
    		"skillID - Id of skill animation of that you want to test",
    		"<level> - skill level, Default is 1",
     	},
    	{"admin_targets",                                 
    		
    		"List skill targets (only multiple targets supported).",
    		"Usage: targets skillID <level>",
    		"Options:",
    		"skillID - Id of skill, target list you want to see",
    		"<level> - skill level, Default is 1",
     	},
    	{"admin_msg",                                 
    		
    		"Test system message.",
    		"Usage: msg msgID",
    		"Options:",
    		"msgID - Id of client system message you want to see",
     	},
    	{"admin_social",                                 
    		
    		"Test social action or NPC animation.",
    		"Usage: social id",
    		"Options:",
    		"id - Id of social action you want to test",
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
        else if (cmd.equals("admin_skill"))
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
                	adminTestSkill(activeChar,skillId,skillLvl,skill.getHitTime());
                else
                	activeChar.sendMessage("Skill id "+skillId+" not found.");
        	}
            else
            	showAdminCommandHelp(activeChar,cmd);
        }
        else if (cmd.equals("admin_social"))
        {
        	int socId = -1;
       	
            try
            {
            	socId = Integer.parseInt(st.nextToken());
            }
            catch(Exception e)
            {
            }
            if (socId >= 0)
            	adminTestSocial(activeChar,socId);
            else
            	showAdminCommandHelp(activeChar,cmd);
        }
        else if (cmd.equals("admin_msg"))
        {
            int msgId = -1;

            try
            {
                msgId = Integer.parseInt(st.nextToken());
            }
            catch (Exception e)
            {
            }
            if (msgId >= 0)
            	activeChar.sendPacket(new SystemMessage(msgId));
            else
            	showAdminCommandHelp(activeChar,cmd);
        }
        else if (cmd.equals("admin_mp"))
        {
            if (Config.IO_TYPE == Config.IOType.nio)
            {
            	if (st.hasMoreTokens() && st.nextToken().equalsIgnoreCase("dump"))
            	{
            		SelectorThread.dumpPacketHistory();
                    activeChar.sendMessage("Packet history saved.");
            	} else
            	{
            		if (Config.TEST_CAPTUREPACKETS == false)
            			SelectorThread.startPacketMonitor();
            		else
            			SelectorThread.stopPacketMonitor();
            		activeChar.sendMessage("Packet monitor is "+(Config.TEST_CAPTUREPACKETS?"enabled":"disabled")+".");
            	}
            }
            else
            {
                activeChar.sendMessage("Packet monitor not supported.");
            }
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
     * Test skill animation
     * @param activeChar
     * @param skill
     */
    public void adminTestSkill(L2PcInstance activeChar,int skillId, int skilLvl, int hitTime)
    {
    	L2Object target;
    	
       	if (activeChar.getTarget() != null && (activeChar.getTarget() instanceof L2Character))
    		target = activeChar.getTarget();
    	else 
    		target = activeChar;
       	activeChar.sendMessage("S="+skillId+" Lv="+skilLvl+" Hit="+hitTime);
       	MagicSkillUser msu = new MagicSkillUser((L2Character)activeChar, (L2Character)target, skillId, skilLvl, hitTime, 1);
       	activeChar.broadcastPacket(msu);
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
    	for (int i=0; i < _adminCommands.length; i++)
    	{
    		if (command.equals(_adminCommands[i][0]))
    		{
    			for (int k=1; k < _adminCommands[i].length; k++)
    				activeChar.sendMessage(_adminCommands[i][k]);
    		}
    	}
    }
    
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IAdminCommandHandler#getAdminCommandList()
     */
    public String[] getAdminCommandList()
    {
    	String[] _adminCommandsOnly = new String[_adminCommands.length];
    	for (int i=0; i < _adminCommands.length; i++)
    	{
    		_adminCommandsOnly[i]=_adminCommands[i][0];
    	}
    	
        return _adminCommandsOnly;
    }

}
