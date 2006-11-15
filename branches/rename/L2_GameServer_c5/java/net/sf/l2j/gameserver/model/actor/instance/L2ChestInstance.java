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
package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.taskmanager.DecayTaskManager;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * 
 * @version $Revision: 1.0.0.0 $ $Date: 2006/06/16 $
 */
public final class L2ChestInstance extends L2Attackable
{
    public boolean isAutoAttackable(L2Character attacker) 
    {
        return false;
    }
    
    public L2ChestInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }

    public void doDie(L2Character killer, boolean haveToDrop) 
    {
        if (haveToDrop) 
        {
            // do item drop if haveToDrop = true
            super.doDie(killer);
        }
        else
        {
            DecayTaskManager.getInstance().addDecayTask(this);
            // Set target to null and cancel Attack or Cast
            setTarget(null);
            // Stop movement
            stopMove(null);
            // Stop HP/MP/CP Regeneration task
            getStatus().stopHpMpRegeneration();
            // Stop all active skills effects in progress on the chest
            stopAllEffects();
            // Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
            broadcastStatusUpdate();
            // Notify L2Character AI
            getAI().notifyEvent(CtrlEvent.EVT_DEAD, null);
            getAttackByList().clear();
        }
    }

    public void doDie(L2Character killer) 
    {
        // place conditions that check whenever your mob should drop or not
        doDie(killer, false);
    }
    
    public boolean isAggressive()
    {
        return false;
    }
    
    public void onSpawn()
    {
        super.OnSpawn();
    }   
    public void dropReward(L2Character player)
    {
        super.doItemDrop(player);
        doDie(player, false);
    }
    /*
    public void showChatWindow(L2PcInstance player, int val)
    {
        if(this.getCurrentHp()==0) 
        {
            player.setTarget(null);
            player.sendPacket(new ActionFailed());
            return;
        }
        
        String filename = "data/html/chests/" + getTemplate().npcId + ".htm";

        NpcHtmlMessage html = new NpcHtmlMessage(1);
        html.setFile(filename);

        html.replace("%objectId%", String.valueOf(getObjectId()));
        player.sendPacket(html);
    }*/
}