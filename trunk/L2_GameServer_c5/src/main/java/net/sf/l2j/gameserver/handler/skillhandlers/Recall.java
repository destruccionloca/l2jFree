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
package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.instancemanager.ZoneManager;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.ZoneType;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Recall implements ISkillHandler
{
    private final static Log _log = LogFactory.getLog(Recall.class.getName());
    protected SkillType[] _skillIds = {SkillType.RECALL};

    public void useSkill(@SuppressWarnings("unused") L2Character activeChar, @SuppressWarnings("unused") L2Skill skill, L2Object[] targets)
    {
        // [L2J_JP ADD SANDMAN]
        // <!--- Zaken skills - teleport PC --> or <!--- Zaken skills - teleport -->
        if(skill.getId() == 4216 || skill.getId() == 4222)
        {
            this.doZakenTeleport(targets);
            activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
            if(activeChar instanceof L2MonsterInstance)
                ((L2MonsterInstance)activeChar).clearAggroList();
            return;
        }

        if (activeChar instanceof L2PcInstance)
        {
            if (((L2PcInstance)activeChar).isInOlympiadMode())
            {
                ((L2PcInstance)activeChar).sendPacket(new SystemMessage(SystemMessage.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
                return;                
            }
        }
        
        try 
        {
            for (int index = 0; index < targets.length; index++)
            {
                if (!(targets[index] instanceof L2Character))
                    continue;

                L2Character target = (L2Character)targets[index];

                if (target instanceof L2PcInstance)
                {
                    L2PcInstance targetChar = (L2PcInstance)target;

					// [L2J_JP ADD]
					if(ZoneManager.getInstance().checkIfInZone(ZoneType.ZoneTypeEnum.NoEscape.toString(),targetChar)){
					    targetChar.sendPacket(SystemMessage.sendString("You can not escape from here."));
					    targetChar.sendPacket(new ActionFailed());
					    break;                   
					}

                    // Check to see if the current player target is in a festival.
                    if (targetChar.isFestivalParticipant()) {
                        targetChar.sendPacket(SystemMessage.sendString("You may not use an escape skill in a festival."));
                        continue;
                    }
                    
                    //Check to see if the current player target is in TvT , CTF or ViP events.
                    if (targetChar._inEventCTF || targetChar._inEventTvT || targetChar._inEventVIP) {
                        targetChar.sendPacket(SystemMessage.sendString("You may not use an escape skill in a Event."));
                        continue;
                    }
                    
                    // Check to see if player is in jail
                    if (targetChar.isInJail())
                    {
                        targetChar.sendPacket(SystemMessage.sendString("You can not escape from jail."));
                        continue;
                    }
                }
                  
                target.teleToLocation(MapRegionTable.TeleportWhereType.Town);
            }
        } catch (Throwable e) {
            _log.error(e.getMessage(),e);
        }
    }

    public SkillType[] getSkillIds()
    {
        return _skillIds;
    }
    // [L2J_JP ADD SANDMAN]
    protected void doZakenTeleport(L2Object[] targets)
    {
    	final int loc[][] = 
		{
			{54228,220136,-3496},
			{56315,220127,-3496},
			{56285,218078,-3496},
			{54238,218066,-3496},
			{55259,219107,-3496},
			{56295,218078,-3224},
			{56283,220133,-3224},
			{54241,220127,-3224},
			{54238,218077,-3224},
			{55268,219090,-3224},
			{56284,218078,-2952},
			{54252,220135,-2952},
			{54244,218095,-2952},
			{55270,219086,-2952}
		};
    	
    	int rndLoc = 0;
    	int rndX = 0;
    	int rndY = 0;
    	
        try
        {
            for (int index = 0; index < targets.length; index++)
            {
                if (!(targets[index] instanceof L2Character)) continue;

                L2Character target = (L2Character) targets[index];

                target.abortAttack();

                rndLoc = Rnd.get(14);
                rndX = Rnd.get(-400,400);
                rndY = Rnd.get(-400,400);

                target.teleToLocation(loc[rndLoc][0] + rndX,loc[rndLoc][1] + rndY,loc[rndLoc][2]);
            }
        }
        catch (Throwable e)
        {
            if (_log.isDebugEnabled())
            	e.printStackTrace();
        }
    }
}