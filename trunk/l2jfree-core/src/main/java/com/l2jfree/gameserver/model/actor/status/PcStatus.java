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
package com.l2jfree.gameserver.model.actor.status;

import com.l2jfree.Config;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2Summon;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfree.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jfree.gameserver.model.entity.Duel;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.skills.Stats;
import com.l2jfree.gameserver.util.Util;

public class PcStatus extends PlayableStatus
{
    // =========================================================
    // Data Field
    
    // =========================================================
    // Constructor
    public PcStatus(L2PcInstance activeChar)
    {
        super(activeChar);
    }

    // =========================================================
    // Method - Public
    @Override
    public final void reduceHp(double value, L2Character attacker, boolean awake, boolean isDOT)
    {
        double realValue = value;
        L2PcInstance cha = getActiveChar();
        if ((cha.isInvul() && cha != attacker) || cha.isDead() || cha.isPetrified())
            return;

        if (cha.isInDuel())
        {
            if (attacker instanceof L2PcInstance)
            {
                L2PcInstance pcInst = (L2PcInstance)attacker;
                if (pcInst.isGM() && pcInst.getAccessLevel() < Config.GM_CAN_GIVE_DAMAGE)
                    return;

                // the duel is finishing - players do not receive damage
                if (cha.getDuelState() == Duel.DUELSTATE_DEAD)
                    return;
                else if (cha.getDuelState() == Duel.DUELSTATE_WINNER)
                    return;

                // cancel duel if player got hit by another player, that is not part of the duel
                if (pcInst.getDuelId() != cha.getDuelId())
                    cha.setDuelState(Duel.DUELSTATE_INTERRUPTED);
            }
            else if (!(attacker instanceof L2SummonInstance))
            {
                // if attacked by a non L2PcInstance & non L2SummonInstance the duel gets canceled
                getActiveChar().setDuelState(Duel.DUELSTATE_INTERRUPTED);
            }
        }

        if (attacker != null && attacker != getActiveChar())
        {
            // Check and calculate transfered damage
            L2Summon summon = getActiveChar().getPet();

            if (summon != null && summon instanceof L2SummonInstance && Util.checkIfInRange(900, getActiveChar(), summon, true))
            {
                int tDmg = (int)value * (int)getActiveChar().getStat().calcStat(Stats.TRANSFER_DAMAGE_PERCENT, 0, null, null) /100;
                
                // Only transfer dmg up to current HP, it should not be killed
                if (summon.getStatus().getCurrentHp() < tDmg) tDmg = (int)summon.getStatus().getCurrentHp() - 1;
                if (tDmg > 0)
                {
                    summon.reduceCurrentHp(tDmg, attacker, null);
                    value -= tDmg;
                    realValue = value;
                }
            }

            if (attacker instanceof L2PlayableInstance)
            {
                if (getCurrentCp() >= value)
                {
                    setCurrentCp(getCurrentCp() - value);   // Set Cp to diff of Cp vs value
                    value = 0;                              // No need to subtract anything from Hp
                }
                else
                {
                    value -= getCurrentCp();                // Get diff from value vs Cp; will apply diff to Hp
                    setCurrentCp(0);                        // Set Cp to 0
                }
            }
        }

        super.reduceHp(value, attacker, awake, isDOT);

        if (!getActiveChar().isDead() && getActiveChar().isSitting()) 
            getActiveChar().standUp();
        
        if (getActiveChar().isFakeDeath()) 
            getActiveChar().stopFakeDeath(null);

        if (attacker != null && attacker != getActiveChar() && realValue > 0)
        {
            // Send a System Message to the L2PcInstance
            SystemMessage smsg = new SystemMessage(SystemMessageId.S1_RECEIVED_DAMAGE_OF_S3_FROM_S2);
            smsg.addString(getActiveChar().getName());
            smsg.addCharName(attacker);
            smsg.addNumber((int)realValue);
            getActiveChar().sendPacket(smsg);
        }
    }

    // =========================================================
    // Method - Private

    // =========================================================
    // Property - Public
    @Override
    public L2PcInstance getActiveChar() { return (L2PcInstance)super.getActiveChar(); }
}
