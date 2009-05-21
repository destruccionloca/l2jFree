/* This program is free software; you can redistribute it and/or modify */
package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.CoupleManager;
import net.sf.l2j.gameserver.instancemanager.JailManager;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2FriendList;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.serverpackets.ConfirmDlg;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.Broadcast;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** 
 * @author evill33t
 * 
 */
public class Wedding implements IVoicedCommandHandler
{
    protected static Log _log = LogFactory.getLog(Wedding.class);
    private static String[] _voicedCommands = { "divorce", "engage", "gotolove" };

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IUserCommandHandler#useUserCommand(int, net.sf.l2j.gameserver.model.L2PcInstance)
     */
    public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
    {
        if(command.startsWith("engage"))
            return Engage(activeChar);
        else if(command.startsWith("divorce"))
            return Divorce(activeChar);
        else if(command.startsWith("gotolove"))
            return GoToLove(activeChar);
        return false;
    }
    
    public boolean Divorce(L2PcInstance activeChar)
    {
        if(activeChar.getPartnerId()==0)
            return false;

        int _partnerId = activeChar.getPartnerId();
        int _coupleId = activeChar.getCoupleId();
        int AdenaAmount = 0;
        
        if(activeChar.isMaried())
        {
            activeChar.sendMessage("You are now divorced."); 

            AdenaAmount = (activeChar.getAdena()/100)*Config.WEDDING_DIVORCE_COSTS;
            activeChar.getInventory().reduceAdena("Wedding", AdenaAmount, activeChar, null);
            
        }
        else
            activeChar.sendMessage("You have broken up as a couple.");
       
        L2PcInstance partner;
        partner = (L2PcInstance)L2World.getInstance().findObject(_partnerId);
        
        if (partner != null)
        {
            partner.setPartnerId(0);
            if(partner.isMaried())
                partner.sendMessage("Your fiance has decided to divorce from you.");
            else
                partner.sendMessage("Your fiance has decided to disengage.");

            // give adena
            if(AdenaAmount>0)
                partner.addAdena("WEDDING", AdenaAmount, null, false);
        }
        
        CoupleManager.getInstance().deleteCouple(_coupleId);
        return true;
    }

    public boolean Engage(L2PcInstance activeChar)
    {
        // check target
        if (activeChar.getTarget()==null)
        {
            activeChar.sendMessage("You have no one targeted.");
            return false;
        }
        
        // check if target is a l2pcinstance
        if (!(activeChar.getTarget() instanceof L2PcInstance))
        {
            activeChar.sendMessage("You can only ask another player for engagement");

            return false;
        }
        
        // check if player is already engaged
        if (activeChar.getPartnerId()!=0)
        {
            activeChar.sendMessage("You are already engaged.");
            if(Config.WEDDING_PUNISH_INFIDELITY)
            {
                activeChar.startAbnormalEffect((short)0x2000); // give player a Big Head
                // lets recycle the sevensigns debuffs
                int skillId;
    
                int skillLevel = 1;
                
                if (activeChar.getLevel() > 40)
                    skillLevel = 2;
                
                if(activeChar.isMageClass())
                    skillId = 4361;
                else
                    skillId = 4362;
                
                L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
                
                if (activeChar.getEffect(skill) == null)
                {
                    skill.getEffects(activeChar, activeChar);
                    SystemMessage sm = new SystemMessage(SystemMessage.YOU_FEEL_S1_EFFECT);
                    sm.addSkillName(skillId);
                    activeChar.sendPacket(sm);
                }
            }   
            return false;
        }

        L2PcInstance ptarget = (L2PcInstance)activeChar.getTarget();
        
        // check if player target himself
        if(ptarget.getObjectId()==activeChar.getObjectId())
        {
            activeChar.sendMessage("Going out with yourself?");
            return false;
        }

        if(ptarget.isMaried())
        {
            activeChar.sendMessage("Already married.");
            return false;
        }

        if(ptarget.getPartnerId()!=0)
        {
            activeChar.sendMessage("Already engaged.");
            return false;
        }

        if(ptarget.isEngageRequest())
        {
            activeChar.sendMessage("Already asked by someone else.");
            return false;
        }

        if(ptarget.getPartnerId()!=0)
        {
            activeChar.sendMessage("Is already engaged with someone else.");
            return false;
        }
        
        
        if (ptarget.getAppearance().getSex()==activeChar.getAppearance().getSex() && !Config.WEDDING_SAMESEX)
        {
            activeChar.sendMessage("You can't ask someone of the same sex for engagement.");
            return false;
        }
        
        if(!L2FriendList.isInFriendList(activeChar, ptarget))
        {
            activeChar.sendMessage("The player you want to ask is not on your friend list.");
            return false;
        }
        
        ptarget.setEngageRequest(true, activeChar.getObjectId());        
        //ptarget.sendMessage("Player "+activeChar.getName()+" wants to engage with you.");
        ptarget.sendPacket(new ConfirmDlg(614,activeChar.getName()+" asking you to engage. Do you want to start a new relationship?"));
        return true;
    }
    
    public boolean GoToLove(L2PcInstance activeChar)
    {   
        if(!activeChar.isMaried())
        {
            activeChar.sendMessage("You're not married."); 
            return false;
        }
        
        if(activeChar.getPartnerId()==0)
        {
            activeChar.sendMessage("Couldnt find your fiance in Database - Inform a Gamemaster.");
            _log.error("Married but couldnt find parter for "+activeChar.getName());
            return false;
        }
        
        if (activeChar.isCastingNow() || activeChar.isMovementDisabled() || activeChar.isMuted() || activeChar.isAlikeDead() ||
                activeChar.isInOlympiadMode() || activeChar._inEventCTF || activeChar._inEventTvT || activeChar._inEventDM)  
            return false;

        // Check if player is inside jail.
        if (JailManager.getInstance().checkIfInZone(activeChar))
        {
            activeChar.sendMessage("You're in JAIL, you can't go to your loved one."); 
            return false;
        }
 
        // Check to see if the player is in a festival.
        if (activeChar.isFestivalParticipant()) 
        {
            activeChar.sendPacket(SystemMessage.sendString("You may not use an escape command in a festival."));
            return false;
        }
        
        // Check to see if player is in jail
        if (activeChar.isInJail())
        {
            activeChar.sendPacket(SystemMessage.sendString("You can not escape from jail."));
            return false;
        }
        
        // Check if player is in Siege
        L2Clan activeCharClan  = activeChar.getClan();
        if(activeCharClan!=null) // character has clan ?
        {
            Castle activeCharCastle= CastleManager.getInstance().getCastleByOwner(activeChar.getClan());
            if(activeCharCastle!=null) // clan has castle ?
            {
                if (activeCharCastle.getSiege().getIsInProgress())
                {
                    activeChar.sendMessage("You in Siege now and you cant go to your partner!");
                    return false;
                }
            }
        }

        L2PcInstance partner;
        partner = (L2PcInstance)L2World.getInstance().findObject(activeChar.getPartnerId());
        if(partner ==null)
        {
            activeChar.sendPacket(SystemMessage.sendString("Your fiance is not online."));
            return false;
        }
        else if(partner.isInJail()){
            activeChar.sendPacket(SystemMessage.sendString("Your fiance is in Jail."));
            return false;
        }
        else if(partner._inEventCTF || partner._inEventTvT || partner._inEventDM){
            activeChar.sendPacket(SystemMessage.sendString("Your fiance is in Event now."));
            return false;
        }
        else if(partner.isInOlympiadMode()){
            activeChar.sendPacket(SystemMessage.sendString("Your fiance is in Olympiad now."));
            return false;
        }   

        // Check if partner is in Siege
        L2Clan partnerClan  = partner.getClan();
        if(partnerClan!=null) // character has clan ?
        {
            Castle partnerCastle= CastleManager.getInstance().getCastleByOwner(partner.getClan());
            if(partnerCastle!=null) // clan has castle ?
            {
                if (partnerCastle.getSiege().getIsInProgress())
                {
                    activeChar.sendMessage("You partner is in Siege you cant go to him!");
                    return false;
                }
            }
        }
        
        int teleportTimer = Config.WEDDING_TELEPORT_INTERVAL*1000;
        
        activeChar.sendMessage("After " + teleportTimer/60000 + " min. you will be teleported to your fiance.");
        activeChar.getInventory().reduceAdena("Wedding", Config.WEDDING_TELEPORT_PRICE, activeChar, null);
        
        activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
        //SoE Animation section
        activeChar.setTarget(activeChar);
        activeChar.disableAllSkills();

        MagicSkillUser msk = new MagicSkillUser(activeChar, 1050, 1, teleportTimer, 0);
        Broadcast.toSelfAndKnownPlayersInRadius(activeChar, msk, 810000/*900*/);
        SetupGauge sg = new SetupGauge(0, teleportTimer);
        activeChar.sendPacket(sg);
        //End SoE Animation section

        EscapeFinalizer ef = new EscapeFinalizer(activeChar,partner.getX(),partner.getY(),partner.getZ());
        // continue execution later
        activeChar.setSkillCast(ThreadPoolManager.getInstance().scheduleGeneral(ef, teleportTimer));
        activeChar.setSkillCastEndTime(10+GameTimeController.getGameTicks()+teleportTimer/GameTimeController.MILLIS_IN_TICK);
        
        return true;
    }

    static class EscapeFinalizer implements Runnable
    {
        private L2PcInstance _activeChar;
        private int _partnerx;
        private int _partnery;
        private int _partnerz;
        
        EscapeFinalizer(L2PcInstance activeChar,int x,int y,int z)
        {
            _activeChar = activeChar;
            this._partnerx=x;
            this._partnery=y;
            this._partnerz=z;
        }
        
        public void run()
        {
            if (_activeChar.isDead()) 
                return; 
            
            _activeChar.setIsIn7sDungeon(false);
            
            _activeChar.enableAllSkills();
            
            try 
            {
                _activeChar.teleToLocation(_partnerx, _partnery, _partnerz);
            } catch (Throwable e) { _log.error(e.getMessage(),e); }
        }
    }
    
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IUserCommandHandler#getUserCommandList()
     */
    public String[] getVoicedCommandList()
    {
        return _voicedCommands;
    }
}
