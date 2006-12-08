/* This program is free software; you can redistribute it and/or modify */
package net.sf.l2j.gameserver.handler.usercommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.handler.IUserCommandHandler;
import net.sf.l2j.gameserver.instancemanager.JailManager;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.Broadcast;

import org.apache.log4j.Logger;

/**
 * 
 *
 */
public class GoToLove implements IUserCommandHandler
{
    private static Logger _log = Logger.getLogger(Escape.class);
    private static final int[] COMMAND_IDS = { 103 };

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IUserCommandHandler#useUserCommand(int, net.sf.l2j.gameserver.model.L2PcInstance)
     */
    public boolean useUserCommand(@SuppressWarnings("unused") int id, L2PcInstance activeChar)
    {   
        if(!activeChar.isMaried())
        {
            activeChar.sendMessage("You're not Maried.");
            return false;
        }
        
        if(activeChar.getPartnerId()==0)
        {
            activeChar.sendMessage("Couldnt find your Partner in Database - Inform a Gamemaster.");
            _log.error("Maried but couldnt find parter for "+activeChar.getName());
            return false;
        }
        
        if (activeChar.isCastingNow() || activeChar.isMovementDisabled() || activeChar.isMuted() || activeChar.isAlikeDead() ||
                activeChar.isInOlympiadMode()) 
            return false;

        // Check if player is inside jail.
        if (JailManager.getInstance().checkIfInZone(activeChar))
        {
            activeChar.sendMessage("You're in JAIL, you can't go to your Love.");
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

        L2PcInstance partner;
        partner = (L2PcInstance)L2World.getInstance().findObject(activeChar.getPartnerId());
        if(partner ==null)
        {
            activeChar.sendPacket(SystemMessage.sendString("Youre Partner is not online."));
            return false;
        }
        
        int teleportTimer = Config.WEDDING_TELEPORT_INTERVAL*1000;
        
        activeChar.sendMessage("After " + teleportTimer/60000 + " min. you will be teleported to your Love.");
        
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
    public int[] getUserCommandList()
    {
        return COMMAND_IDS;
    }
}