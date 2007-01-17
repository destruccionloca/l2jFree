/* This program is free software; you can redistribute it and/or modify
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
import net.sf.l2j.gameserver.SkillTable;
import net.sf.l2j.gameserver.NpcTable;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.Experience;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SummonInstance;
import net.sf.l2j.gameserver.model.actor.stat.PlayableStat;
import net.sf.l2j.gameserver.serverpackets.CharInfo;
import net.sf.l2j.gameserver.serverpackets.Earthquake;
import net.sf.l2j.gameserver.serverpackets.NpcInfo;
import net.sf.l2j.gameserver.serverpackets.PetInfo;
import net.sf.l2j.gameserver.serverpackets.SocialAction;
import net.sf.l2j.gameserver.serverpackets.StopMove;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.UserInfo;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
* This class handles following admin commands: 	- invis / invisible = make yourself invisible
* 												- vis / visible	= make yourself visible
* 
*/
public class AdminEffects implements IAdminCommandHandler
{
   //private final static Log _log = LogFactory.getLog(AdminDelete.class.getName());

   private static String[] _adminCommands = { "admin_invis", "admin_invisible", "admin_vis",
	   "admin_visible", "admin_earthquake", "admin_bighead", "admin_shrinkhead", "admin_gmspeed",  
	   "admin_unpara_all", "admin_para_all", "admin_unpara", "admin_para", "admin_polyself",
	   "admin_unpolyself", "admin_changename", "admin_clearteams", "admin_setteam_close",
       "admin_setteam", "admin_setlevel", "admin_sethero",
       "admin_create_adena", "admin_summon123"};

   private static final int REQUIRED_LEVEL = Config.GM_GODMODE;

   public boolean useAdminCommand(String command, L2PcInstance activeChar)
   {
       if (!Config.ALT_PRIVILEGES_ADMIN)
       {
           if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM()))
               return false;
       }
       
       if (command.equals("admin_invis")||command.equals("admin_invisible"))
       {
    	   activeChar.setInvisible();
    	   activeChar.broadcastUserInfo();
                      
           activeChar.decayMe();
           activeChar.spawnMe();
          
           //activeChar.teleToLocation(activeChar.getX(), activeChar.getY(), activeChar.getZ());
       }
       if (command.equals("admin_vis")||command.equals("admin_visible"))
       {
    	   activeChar.setVisible();
    	   activeChar.broadcastUserInfo();
       }
       if (command.startsWith("admin_earthquake"))
       {
           try
           {
        	   String val = command.substring(17);
               StringTokenizer st = new StringTokenizer(val);
               String val1 = st.nextToken();
               int intensity = Integer.parseInt(val1);
               String val2 = st.nextToken();
               int duration = Integer.parseInt(val2);
        	   Earthquake eq = new Earthquake(activeChar.getX(), activeChar.getY(), activeChar.getZ(), 
        			   						  intensity, duration);
               activeChar.broadcastPacket(eq);
           }
           catch (Exception e)
           {  }
       }
       if (command.equals("admin_para"))
       {
    	   String type = "1";
           StringTokenizer st = new StringTokenizer(command);
           try
           {
               st.nextToken();
               type = st.nextToken();
           }
           catch(Exception e){}
           try
           {
       		L2Object target = activeChar.getTarget();
       		L2Character player = null;
	    		if (target instanceof L2Character) {
	    			player = (L2Character)target;
	    			
	    			if (type.equals("1"))
	    			   player.startAbnormalEffect((short)0x0400);
	    			else
	    				player.startAbnormalEffect((short)0x0800);
	            	player.setIsParalyzed(true);
	            	
	            	StopMove sm = new StopMove(player);
	            	player.sendPacket(sm);
	            	player.broadcastPacket(sm);
	    		}
           }
           catch (Exception e)
           {
           }
       }
       if (command.equals("admin_unpara"))
       {
           try
           {
          		L2Object target = activeChar.getTarget();
           		L2Character player = null;
    	    		if (target instanceof L2Character) {
    	    			player = (L2Character)target;
    	    			   player.stopAbnormalEffect((short)0x0400);
    	    			   player.setIsParalyzed(false);
    	    		}
           }
           catch (Exception e)
           {
           }
       }
       if (command.startsWith("admin_para_all"))
       {
           try
           {
               for (L2PcInstance player : activeChar.getKnownList().getKnownPlayers().values())
               {
            	   if (!player.isGM())
            	   {
                	   player.startAbnormalEffect((short)0x0400);
                	   player.setIsParalyzed(true);
                	   
                	   StopMove sm = new StopMove(player);
   	            	   player.sendPacket(sm);
   	            	   player.broadcastPacket(sm);
            	   }
               }
           }
           catch (Exception e)
           {
           }
       }
       if (command.startsWith("admin_unpara_all"))
       {
           try
           {
               for (L2PcInstance player : activeChar.getKnownList().getKnownPlayers().values())
               {
            	   player.stopAbnormalEffect((short)0x0400);
            	   player.setIsParalyzed(false);
               }
           }
           catch (Exception e)
           {
           }
       }
       if (command.startsWith("admin_bighead"))
       {
           try
           {   
               L2Object target = activeChar.getTarget();
               L2Character player = null;
                if (target instanceof L2Character) {
                  player = (L2Character)target;
                  player.startAbnormalEffect((short)0x2000);
                }
           }
           catch (Exception e)
           {
           }
       }
       if (command.startsWith("admin_shrinkhead"))
       {
           try
           {
                 L2Object target = activeChar.getTarget();
                   L2Character player = null;
                    if (target instanceof L2Character) {
                      player = (L2Character)target;
                      player.stopAbnormalEffect((short)0x2000);
                    }
           }
           catch (Exception e)
           {
           }
       }
       if (command.startsWith("admin_gmspeed"))
       {
          int val;
           try {
              val = Integer.parseInt(command.substring(14));
              boolean sendMessage = activeChar.getEffect(7029) != null;

              activeChar.stopEffect(7029);
              if (val == 0 && sendMessage) {
                  SystemMessage sm = new SystemMessage(SystemMessage.EFFECT_S1_DISAPPEARED);
                  sm.addSkillName(7029);
                  activeChar.sendPacket(sm);
              }
              else if ((val >= 1) && (val <= 4)) {
                  L2Skill gmSpeedSkill = SkillTable.getInstance().getInfo(7029, val);

                  activeChar.doCast(gmSpeedSkill);
              }
          }
          catch (Exception e) {
               SystemMessage sm = new SystemMessage(614);

              sm.addString("Use //gmspeed value = [0...4].");
              activeChar.sendPacket(sm);
          }
          finally {
              activeChar.updateEffectIcons();
          }
       }
       if (command.startsWith("admin_polyself"))
       {
       	StringTokenizer st = new StringTokenizer(command);
        try
        {
            st.nextToken();
            String id = st.nextToken();
        	activeChar.getPoly().setPolyInfo("npc", id);
        	activeChar.teleToLocation(activeChar.getX(), activeChar.getY(), activeChar.getZ(), false);
        	CharInfo info1 = new CharInfo(activeChar);
        	activeChar.broadcastPacket(info1);
		    UserInfo info2 = new UserInfo(activeChar);
		    activeChar.sendPacket(info2);
        }
        catch(Exception e){}
       }
       if (command.startsWith("admin_unpolyself"))
       {
        	activeChar.getPoly().setPolyInfo(null, "1");
        	activeChar.decayMe();
        	activeChar.spawnMe(activeChar.getX(),activeChar.getY(),activeChar.getZ());
        	CharInfo info1 = new CharInfo(activeChar);
        	activeChar.broadcastPacket(info1);
		    UserInfo info2 = new UserInfo(activeChar);
		    activeChar.sendPacket(info2);
       }
       if (command.startsWith("admin_changename"))
       {
           try
           {
        	   String name = command.substring(17);
        	   String oldName = "null";
        	   try
               {
              		L2Object target = activeChar.getTarget();
               		L2Character player = null;
        	    		if (target instanceof L2Character) {
        	    			player = (L2Character)target;
        	    			oldName = player.getName();
        	    		}
        	    		else if (target == null)
        	    		{
        	    			player = activeChar;
        	    			oldName = activeChar.getName();
        	    		}
        	    		if (player instanceof L2PcInstance)
        	    			L2World.getInstance().removeFromAllPlayers((L2PcInstance)player);
        	    		player.setName(name);
        	    		if (player instanceof L2PcInstance)
        	    			L2World.getInstance().addVisibleObject(player, null, null);
        	    		
        	    		if (player instanceof L2PcInstance)
        	    		{
        	        	CharInfo info1 = new CharInfo((L2PcInstance)player);
        	        	player.broadcastPacket(info1);
        			    UserInfo info2 = new UserInfo((L2PcInstance)player);
        			    player.sendPacket(info2);
        	    		}
        	    		else if(player instanceof L2NpcInstance)
        	    		{
        	    			NpcInfo info1 = new NpcInfo((L2NpcInstance)player, null);
            	        	player.broadcastPacket(info1);
        	    		}
        			    
        			    SystemMessage smA = new SystemMessage(SystemMessage.S1_S2);
        				smA.addString("Changed name from "+ oldName +" to "+ name +".");		
        				activeChar.sendPacket(smA);
               }
               catch (Exception e)
               {
               }
           }
           catch (StringIndexOutOfBoundsException e)
           { }
       }
       else if (command.equals("admin_clear_teams"))
       {
           try
           {
               for (L2PcInstance player : activeChar.getKnownList().getKnownPlayers().values())
               {
                   player.setTeam(0);
                   player.broadcastUserInfo();
               }
           }
           catch (Exception e)
           {
           }
       }
       else if (command.startsWith("admin_setteam_close"))
       {
           String val = command.substring(20);
           int teamVal = Integer.parseInt(val);
           try
           {
               for (L2PcInstance player : activeChar.getKnownList().getKnownPlayers().values())
               {
                   if (activeChar.isInsideRadius(player, 400, false, true))
                   {
                       player.setTeam(0);
                       if (teamVal != 0)
                       {
                           SystemMessage sm = new SystemMessage(614);
                           sm.addString("You have joined team " + teamVal);
                           player.sendPacket(sm);
                       }
                       player.broadcastUserInfo();
                   }
               }
           }
           catch (Exception e)
           {
           }
       }
       else if (command.startsWith("admin_setteam"))
       {
           String val = command.substring(14);
           int teamVal = Integer.parseInt(val);
           L2Object target = activeChar.getTarget();
           L2PcInstance player = null;
           if (target instanceof L2PcInstance) {
               player = (L2PcInstance)target;
           } else {
               return false;
           }
           player.setTeam(teamVal);
           if (teamVal != 0)
           {
           SystemMessage sm = new SystemMessage(614);
           sm.addString("You have joined team " + teamVal);
           player.sendPacket(sm);
           }
           player.broadcastUserInfo();
       }
       else if (command.startsWith("admin_setlevel"))
       {
		StringTokenizer st = new StringTokenizer(command);
        try
        {
            st.nextToken();
		    int newlvl = Integer.parseInt(st.nextToken());
		    if (newlvl >= 1 && newlvl <= 78)
            {
		    	L2Object target = activeChar.getTarget();
	            //L2Character player = null;

                //L2Object target = (L2PlayableInstance)activeChar.getTarget();
                L2Character player = null;

                if (target instanceof L2Character)
                    player = (L2Character)activeChar.getTarget();
                else
                    player = activeChar;

                PlayableStat ps = (PlayableStat)player.getStat();
                ps.addExp(ps.getExpForLevel(newlvl)-ps.getExp());
                player.broadcastPacket(new SocialAction(player.getObjectId(), 15));

		SystemMessage sm = new SystemMessage(614);
		SystemMessage smAdmin = new SystemMessage(614);

		sm.addString(activeChar.getName() + " has set your level to " + player.getLevel());
		smAdmin.addString("You have set " + player.getName() + " to level " + player.getLevel());

		player.sendPacket(sm);
		activeChar.sendPacket(smAdmin);

            } else {
                SystemMessage smAdmin = new SystemMessage(614);
                smAdmin.addString("Wrong paramater [1.."+80+"].");
                activeChar.sendPacket(smAdmin);
            }
        }
        catch (Exception e)
        {
			SystemMessage smAdmin = new SystemMessage(614);
			smAdmin.addString("Error when set level");
			activeChar.sendPacket(smAdmin);
        }
       }
       else if (command.startsWith("admin_sethero"))
       {
			L2Object target = activeChar.getTarget();
			L2PcInstance player = null;
    	    SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);		
			if (target instanceof L2PcInstance)
				player = (L2PcInstance)target;
			else
     	      	player = activeChar;
		
			if (player.isHero())
			{
				player.setHero(false);
				sm.addString("You are not longer a hero");				
			}
			else
			{
                player.broadcastPacket(new SocialAction(player.getObjectId(), 16));
				player.setHero(true);
				sm.addString("You are now a hero");
			}
		    sm.addString("Admin changed your hero status");
		    player.sendPacket(sm);
    	   	player.broadcastUserInfo();
       }
       else if (command.startsWith("admin_summon123"))
       {
		StringTokenizer st = new StringTokenizer(command);
		try
		{
		st.nextToken();
	        int npcId = Integer.parseInt(st.nextToken());
	
	        if (activeChar.getPet() != null)
	           return false;
	
	        float expPenalty = 0.f;
	
	        L2NpcTemplate summonTemplate = NpcTable.getInstance().getTemplate(npcId);
            L2SummonInstance summon = new L2SummonInstance(IdFactory.getInstance().getNextId(), summonTemplate, activeChar, null);
			
	        summon.setTitle(activeChar.getName());
	        summon.setExpPenalty(expPenalty);
	        if (summon.getLevel() >= Experience.LEVEL.length)
	        {
	            summon.getStat().setExp(Experience.LEVEL[Experience.LEVEL.length - 1]);
	        }
	        else
	        {
	            summon.getStat().setExp(Experience.LEVEL[(summon.getLevel() % Experience.LEVEL.length)]);
	        }
			summon.setCurrentHp(summon.getMaxHp());
			summon.setCurrentMp(summon.getMaxMp());
			summon.setHeading(activeChar.getHeading());
			summon.setRunning();
			activeChar.setPet(summon);
	    		
	    	L2World.getInstance().storeObject(summon);
	        summon.spawnMe(activeChar.getX()+50, activeChar.getY()+100, activeChar.getZ());
	    		
	    	summon.setFollowStatus(true);
	        summon.setShowSummonAnimation(false);
	        activeChar.sendPacket(new PetInfo(summon));
		}
        catch(Exception e){}
       }
       else if (command.startsWith("admin_create_adena"))
       {
		   	StringTokenizer st = new StringTokenizer(command);
		   	try
		   	{
		   	st.nextToken();
		   	int numval = Integer.parseInt(st.nextToken());
		
		   	L2Object target = activeChar.getTarget();
		   	L2PcInstance player = null;
		
		   	if (target instanceof L2PcInstance)
		   		player = (L2PcInstance)target;
		   	else
		        	      	player = activeChar;
		
		   	player.getInventory().addItem("admin_create_adena", 57, numval, activeChar, activeChar);
		
		   	SystemMessage sm = new SystemMessage(614);
		   	sm.addString(activeChar.getName() + " has given " + numval  +" Adena in your inventory.");
		   	player.sendPacket(sm);
		
		   	SystemMessage smAdmin = new SystemMessage(614);
		   	smAdmin.addString("You have given " + numval + " Adena to " + player.getName());
		   	activeChar.sendPacket(smAdmin);				
		   	}
		   	catch (StringIndexOutOfBoundsException e)
		   	{
		   	SystemMessage sm = new SystemMessage(614);
		   	sm.addString("Error while creating Adena.");
		   	activeChar.sendPacket(sm);
	   	}
       }
       
       return true;
   }

   public String[] getAdminCommandList()
   {
       return _adminCommands;
   }

   private boolean checkLevel(int level)
   {
       return (level >= REQUIRED_LEVEL);
   }

}
