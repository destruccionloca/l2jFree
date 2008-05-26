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
package net.sf.l2j.gameserver.instancemanager;

import java.util.Calendar;

import javolution.util.FastList;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.ExDuelEnd;
import net.sf.l2j.gameserver.serverpackets.ExDuelReady;
import net.sf.l2j.gameserver.serverpackets.ExDuelStart;
import net.sf.l2j.gameserver.serverpackets.ServerBasePacket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/** 
 * @author tomciaaa
 * 
 */
public class DuelManager
{
    private final static Log _log = LogFactory.getLog(DuelManager.class.getName());
   
    public class EndDuel implements Runnable
    {
        private int _duelId;
        private boolean _isParty;
        public EndDuel(int duel, boolean party)
        {
            _duelId = duel;
            _isParty = party;
        }
        public void run()
        {
            try
            {
            Duel duel = getDuel(_duelId, _isParty);
            if (duel != null && duel.getTimeRemaining()> 0)
                ThreadPoolManager.getInstance().scheduleGeneral(this, 3000); //check if the duel should be ended every 3 seconds
            else if (duel != null)
                endDuel(_duelId, _isParty, 0);
            }
            catch(Exception e)
            {
                _log.error("error, running duel thread: "+e.getMessage(),e);
            }
        }
    } 
    private class Duel
    {
        private final L2PcInstance _player1;
        private final L2PcInstance _player2;
        private final int _Id;
        private final Calendar _endTime;
        public Duel(L2PcInstance pl1, L2PcInstance pl2, boolean isParty)
        {
            _Id = IdFactory.getInstance().getNextId();
            _player1 = pl1;
            _player2 = pl2;
            if (isParty)
            {
                _endTime = Calendar.getInstance();
                _endTime.setTimeInMillis(Calendar.getInstance().getTimeInMillis()+300000); //Party Vs Party duel lasts 5 minutes
                getPartyDuels().add(this);
            }
            else
            {
                _endTime = Calendar.getInstance();
                _endTime.setTimeInMillis(Calendar.getInstance().getTimeInMillis()+120000); //One on One duel lasts 2 minutes 
                getSoloDuels().add(this);
            }
        }
        
        public int getId()
        {
            return _Id;
        }
        public int getTimeRemaining()
        {
            return (int) (_endTime.getTimeInMillis() - Calendar.getInstance().getTimeInMillis());
        }
        public boolean stillGoingOn()
        {
            return (_player1.getDistanceSq(_player2) < 2000/*max distance after which the duel will be interrupted, totally custom*/
                    && getTimeRemaining()>0);
        }
        public L2PcInstance getPlayer(boolean first)
        {
            if (first) return _player1;
            else  return _player1;
        }
    }

    // =========================================================
    private static DuelManager _Instance;
    public static final DuelManager getInstance()
    {
        if (_Instance == null)
        {
            _log.info("Initializing DuelManager");
            _Instance = new DuelManager();
            _Duels = new FastList<Duel>();
            _PartyDuels = new FastList<Duel>();
        }
        return _Instance;
    }
    private static FastList<Duel> _Duels;
    private static FastList<Duel> _PartyDuels;


    public void createDuel(L2PcInstance plyr1,L2PcInstance plyr2, boolean party)  //TODO: find out if this script with larger parties won't give the callanger an advantage
    {
        if(plyr1!=null && plyr2!=null)
        {
            if (plyr1.isDuelling() ==0 && plyr2.isDuelling() == 0) //TODO: find out if we should replenish cp/hp/mp b4 duel starts
            {
                ExDuelReady ready = new ExDuelReady();
                ExDuelStart start = new ExDuelStart();
                if (party)
                {
                    Duel duel = new Duel(plyr1,plyr2,true);
                    
                    for (L2PcInstance pm : plyr2.getParty().getPartyMembers()) //======I know this looks stupid, but without this clalanged party wouldn't see enemies list 
                    {
                        pm.setDuelling(duel.getId());
                        pm.setTeam(2);
                        pm.sendPacket(ready);
                        pm.sendPacket(start);
                    }
                    for (L2PcInstance pm : plyr1.getParty().getPartyMembers())
                    {
                        pm.setDuelling(duel.getId());
                        pm.setTeam(1);
                        pm.sendPacket(ready);
                        pm.sendPacket(start);
                        pm.broadcastUserInfo();
                        pm.broadcastStatusUpdate();
                    }
                    for (L2PcInstance pm : plyr2.getParty().getPartyMembers())
                    {
                        pm.broadcastUserInfo();
                        pm.broadcastStatusUpdate();
                    }
                    ThreadPoolManager.getInstance().scheduleGeneral(new EndDuel(duel.getId(), true), 3000);
                }
                else
                {
                    Duel duel = new Duel(plyr1,plyr2,false);
                    //==============================================
                    plyr1.setDuelling(duel.getId());
                    plyr1.setTeam(1);
                    plyr2.setDuelling(duel.getId());
                    plyr2.setTeam(2);
                    //===================== We send duel start packets here, so that players may see
                    plyr1.sendPacket(ready);
                    plyr1.sendPacket(start);
                    plyr2.sendPacket(ready);
                    plyr2.sendPacket(start);
                    //=====================================================
                    plyr1.broadcastUserInfo();
                    plyr1.broadcastStatusUpdate();
                    plyr2.broadcastUserInfo();
                    plyr2.broadcastStatusUpdate();
                    //=======================================
                    ThreadPoolManager.getInstance().scheduleGeneral(new EndDuel(duel.getId(), false), 3000);
                }
            }
        }
    }
    public void MaybeEndDuel(L2PcInstance player)
    {
        if (player.isDuelling()>0)
            if (player.getParty()!= null)
            {
                if (player.getParty().getDefeatedPartyMembers()==player.getParty().getMemberCount())
                    endDuel(player.isDuelling(),true, player.getTeam());
            }
            else
                endDuel(player.isDuelling(),false, player.getTeam());
    }
    public FastList<Duel> getPartyDuels()
    {
        return _PartyDuels;
    }
    public FastList<Duel> getSoloDuels()
    {
        return _Duels;
    }
    public void endDuel(int duelId, boolean party, int looser)
    {
        if (duelId==0)
            return;
        Duel duel = getDuel(duelId, party);
        _Duels.remove(duel);
        L2PcInstance player = duel.getPlayer(true);
        if (player.getParty()!=null)
        {
            for (L2PcInstance pm : player.getParty().getPartyMembers())
            {
                pm.setDuelling(0);
                pm.setCurrentCp(pm.getMaxCp());
                pm.setCurrentHp(pm.getMaxHp()/2);
                pm.setCurrentMp(pm.getMaxMp()/2);
                pm.setTeam(0);
                pm.sendPacket(new ExDuelEnd());
                pm.setIsParalyzed(false);
                if (looser==2)
                {
                    pm.setPvpKills(pm.getPvpKills());
                    pm.sendMessage("You have won the duel!");
                }
                pm.broadcastUserInfo();
            }
        }
        else
        {
            player.setDuelling(0);
            player.setCurrentCp(player.getMaxCp());
            player.setCurrentHp(player.getMaxHp()/2);
            player.setCurrentMp(player.getMaxMp()/2);
            player.setTeam(0);
            player.sendPacket(new ExDuelEnd());
            player.setIsParalyzed(false);
            if (looser==2)
            {
                player.setPvpKills(player.getPvpKills());
                player.sendMessage("You have won the duel!");
            }
            player.broadcastUserInfo();
        }
        player = duel.getPlayer(false);
        if (player.getParty()!=null)
        {
            for (L2PcInstance pm : player.getParty().getPartyMembers())
            {
                pm.setDuelling(0);
                pm.setCurrentCp(pm.getMaxCp());
                pm.setCurrentHp(pm.getMaxHp()/2);
                pm.setCurrentMp(pm.getMaxMp()/2);
                pm.setTeam(0);
                pm.sendPacket(new ExDuelEnd());
                pm.setIsParalyzed(false);
                if (looser==1)
                {
                    pm.setPvpKills(pm.getPvpKills());
                    pm.sendMessage("You have won the duel!");
                }
                pm.broadcastUserInfo();
            }
        }
        else
        {
            player.setDuelling(0);
            player.setCurrentCp(player.getMaxCp());
            player.setCurrentHp(player.getMaxHp()/2);
            player.setCurrentMp(player.getMaxMp()/2);
            player.setTeam(0);
            player.sendPacket(new ExDuelEnd());
            player.setIsParalyzed(false);
            if (looser==1)
            {
                player.setPvpKills(player.getPvpKills());
                player.sendMessage("You have won the duel!");
            }
            player.broadcastUserInfo();
        }
    }
    public Duel getDuel(int duelId, boolean party)
    {
        int index = getDuelIndex(duelId, party);
        if (party)
        {
            if (index >= 0) return _PartyDuels.get(index);
        }
            else if (index >= 0) return _Duels.get(index);
        return null;
    }
    public void broadcastToOpponents(int duelId, L2PcInstance player, ServerBasePacket msg)
    {
        Duel duel = getDuel(duelId, player.getParty()!=null);
        if (duel.getPlayer(player.getTeam()==2).getParty()!=null)
            for (L2PcInstance op : duel.getPlayer(player.getTeam()==2).getParty().getPartyMembers())
                op.sendPacket(msg);
        else duel.getPlayer(player.getTeam()==2).sendPacket(msg);
    }
    private int getDuelIndex(int id, boolean party)
    {
        Duel duel;
        if (party && _PartyDuels.size()>0)
        {
            for (int i = 0; i < _PartyDuels.size(); i++)
            {
                duel = _PartyDuels.get(i);
                if (duel != null && duel.getId() == id) return i;
            }
        }
        else
        {
            for (int i = 0; i < _Duels.size(); i++)
            {
                duel = _Duels.get(i);
                if (duel != null && duel.getId() == id) return i;
            }
        }
        return -1;
    }
}
