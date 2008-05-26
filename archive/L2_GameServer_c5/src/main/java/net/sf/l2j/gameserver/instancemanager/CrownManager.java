package net.sf.l2j.gameserver.instancemanager;

import net.sf.l2j.gameserver.datatables.CrownTable;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** 
 * @author evill33t
 * 
 */
public class CrownManager
{
    private static final Log _log = LogFactory.getLog(CrownManager.class.getName());
    private static CrownManager _Instance;
    public static final CrownManager getInstance()
    {
        if (_Instance == null)
        {
            _log.info("Initializing CrownManager");
            _Instance = new CrownManager();
        }
        return _Instance;
    }    
    
    public void checkCrowns(L2PcInstance cha)
    {
        // check for crowns
        L2Clan activeCharClan  = cha.getClan();
        if(activeCharClan!=null) // character has clan ?
        {
            Castle activeCharCastle= CastleManager.getInstance().getCastleByOwner(cha.getClan());
            if(activeCharCastle!=null) // clan has castle ?
            {
                for (L2ClanMember member : activeCharClan.getMembers())
                {
                    if(member.getObjectId() == cha.getObjectId()) // find member 
                    {
                        int CrownId = CrownTable.getCrownId(activeCharCastle.getCastleId()); // get crown id
        
                        if(activeCharClan.getLeader().getObjectId()==member.getObjectId()) // if leader give lord crown and normal crown
                        {
                            if(cha.getInventory().getItemByItemId(6841)==null) // check if character already has a crown in inventory
                            {
                                cha.getInventory().addItem("Crown",6841,1,member.getPlayerInstance(),null); // give lord crown
                                cha.getInventory().updateDatabase(); // update database
                            }
                            if(cha.getInventory().getItemByItemId(CrownId)==null) // check if character already has a crown in inventory
                            {
                                if(CrownId!=0) // check for crown id
                                    cha.getInventory().addItem("Crown",CrownId,1,member.getPlayerInstance(),null); // give castle crown
                                    cha.getInventory().updateDatabase(); // update database
                            }
                        }
                        else // no leader give normal crown
                        {
                            if(cha.getInventory().getItemByItemId(CrownId)==null) // check for crown id in inventory
                            {
                                if(CrownId!=0)
                                {
                                    cha.getInventory().addItem("Crown",CrownId,1,member.getPlayerInstance(),null); // give crown
                                    cha.getInventory().updateDatabase(); // update database
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}