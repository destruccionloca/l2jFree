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

import java.sql.PreparedStatement;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.datatables.CrownTable;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.L2ItemInstance;
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
    private static CrownManager _instance;
    public static final CrownManager getInstance()
    {
        if (_instance == null)
        {
            _log.info("Initializing CrownManager");
            _instance = new CrownManager();
        }
        return _instance;
    }    
    
    public void removeCrowns(L2Clan clan)
    {
    	for(L2ClanMember member: clan.getMembers())
    	{
    		if(member.isOnline())
    		{
				for(L2ItemInstance item : member.getPlayerInstance().getInventory().getItems())
				{
					if(CrownTable.getCrownList().contains(item.getItemId()))
					{
						member.getPlayerInstance().destroyItem("Removing Crown", item, member.getPlayerInstance(), true);
						member.getPlayerInstance().getInventory().updateDatabase();
					}
				}
    		}
    		else
    		{
				Integer PlayerId = member.getObjectId();
				
    			for(Integer CrownId : CrownTable.getCrownList())
    			{
                    java.sql.Connection con = null;
                    try
                    {
                        con = L2DatabaseFactory.getInstance().getConnection(con);
                        PreparedStatement statement = con.prepareStatement("delete from items where owner_id = ? and item_id = ?");
                        statement.setInt(1, PlayerId);
                        statement.setInt(2, CrownId);
                        statement.execute();
                        statement.close();
                    }
                    catch (Exception e) {} 
                    finally {try { con.close(); } catch (Exception e) {}}

    			}
    		}
    	}
    }
    
    public void giveCrowns(L2Clan clan,Integer CastleId)
    {
    	if(CastleManager.getInstance().getCastle(clan)!=null)
    	{
	        for (L2ClanMember member : clan.getMembers())
	        {
	        	if(member.isOnline())
	        	{
		            int CrownId = CrownTable.getCrownId(CastleId); // get crown id
		
		            if(clan.getLeader().getObjectId()==member.getObjectId()) // if leader give lord crown and normal crown
		            {
		                member.getPlayerInstance().getInventory().addItem("Crown",6841,1,member.getPlayerInstance(),null); // give lord crown
		                member.getPlayerInstance().getInventory().updateDatabase(); // update database
		            }
	                if(CrownId!=0) // give normal crown
	                {
	                    member.getPlayerInstance().getInventory().addItem("Crown",CrownId,1,member.getPlayerInstance(),null); // give crown
	                    member.getPlayerInstance().getInventory().updateDatabase(); // update database
	                }
	        	}
	        }
    	}
    }
    
    public void checkCrowns(L2PcInstance cha)
    {
        // check for crowns
        L2Clan activeCharClan  = cha.getClan();
        if(activeCharClan!=null) // character has clan ?
        {
            Castle activeCharCastle= CastleManager.getInstance().getCastle(cha.getClan());
            if(activeCharCastle!=null) // clan has castle ?
            {
            	int CrownId = CrownTable.getCrownId(CastleManager.getInstance().getCastle(cha.getClan()).getCastleId()); // get crown id
            	
				if(activeCharClan.getLeader().getObjectId()==cha.getObjectId()) // if leader give lord crown and normal crown
				{
				    if(cha.getInventory().getItemByItemId(6841)==null) // check if character already has a crown in inventory
				    {
				        cha.getInventory().addItem("Crown",6841,1,cha,null); // give lord crown
				        cha.getInventory().updateDatabase(); // update database
				    }
				}
			    if(cha.getInventory().getItemByItemId(CrownId)==null) // check for crown id in inventory
			    {
			        if(CrownId!=0)
			        {
			            cha.getInventory().addItem("Crown",CrownId,1,cha,null); // give crown
			            cha.getInventory().updateDatabase(); // update database
			        }
			    }
            }
        }
    }
}