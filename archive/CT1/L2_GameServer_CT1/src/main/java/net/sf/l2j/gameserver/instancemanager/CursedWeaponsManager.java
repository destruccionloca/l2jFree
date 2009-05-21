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
package net.sf.l2j.gameserver.instancemanager;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.CursedWeapon;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Boss;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2FeedableBeastInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2FestivalMonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2GuardInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2RiftInvaderInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SiegeGuardInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * 
 * @author Micht
 */
public class CursedWeaponsManager
{
    private static final Log _log = LogFactory.getLog(CursedWeaponsManager.class.getName());

    // =========================================================
    private static CursedWeaponsManager _instance;

    public static final CursedWeaponsManager getInstance()
    {
        if (_instance == null)
        {
            _instance = new CursedWeaponsManager();
            _instance.load();
        }
        return _instance;
    }

    // =========================================================
    // Data Field
    private FastMap<Integer, CursedWeapon> _cursedWeapons;

    // =========================================================
    // Constructor
    public CursedWeaponsManager()
    {
        _cursedWeapons = new FastMap<Integer, CursedWeapon>();
    }

    // =========================================================
    // Method - Private
    public final void reload()
    {
        _cursedWeapons = new FastMap<Integer, CursedWeapon>();
        load();
    }
    private final void load()
    {
        java.sql.Connection con = null;
        
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setIgnoringComments(true);
            
            File file = new File(Config.DATAPACK_ROOT+"/data/cursedWeapons.xml");
            if (!file.exists())
                throw new IOException();
            
            Document doc = factory.newDocumentBuilder().parse(file);

            for (Node n=doc.getFirstChild(); n != null; n = n.getNextSibling())
            {
                if ("list".equalsIgnoreCase(n.getNodeName()))
                {
                    for (Node d=n.getFirstChild(); d != null; d = d.getNextSibling())
                    {
                        if ("item".equalsIgnoreCase(d.getNodeName()))
                        {
                            NamedNodeMap attrs = d.getAttributes();
                            int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
                            int skillId = Integer.parseInt(attrs.getNamedItem("skillId").getNodeValue());
                            String name = attrs.getNamedItem("name").getNodeValue();
                            
                            CursedWeapon cw = new CursedWeapon(id, skillId, name);

                            int val;
                            for (Node cd=d.getFirstChild(); cd != null; cd = cd.getNextSibling())
                            {
                                if ("dropRate".equalsIgnoreCase(cd.getNodeName()))
                                {
                                    attrs = cd.getAttributes();
                                    val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
                                    cw.setDropRate(val);
                                } else if ("duration".equalsIgnoreCase(cd.getNodeName()))
                                {
                                    attrs = cd.getAttributes();
                                    val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
                                    cw.setDuration(val);
                                } else if ("durationLost".equalsIgnoreCase(cd.getNodeName()))
                                {
                                    attrs = cd.getAttributes();
                                    val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
                                    cw.setDurationLost(val);
                                } else if ("disapearChance".equalsIgnoreCase(cd.getNodeName()))
                                {
                                    attrs = cd.getAttributes();
                                    val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
                                    cw.setDisapearChance(val);
                                } else if ("stageKills".equalsIgnoreCase(cd.getNodeName()))
                                {
                                    attrs = cd.getAttributes();
                                    val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
                                    cw.setStageKills(val);
                                } else if ("transformId".equalsIgnoreCase(cd.getNodeName()))
                                {
                                    attrs = cd.getAttributes();
                                    val = Integer.parseInt(attrs.getNamedItem("val").getNodeValue());
                                    cw.setTransformId(val);
                                }
                            }
                            
                            // Store cursed weapon
                            _cursedWeapons.put(id, cw);
                        }
                    }
                }
            }
            
            // Retrieve the L2PcInstance from the characters table of the database
            con = L2DatabaseFactory.getInstance().getConnection(con);
            PreparedStatement statement;
            ResultSet rset;
            
            if(Config.ALLOW_CURSED_WEAPONS)
            {
            statement = con.prepareStatement("SELECT itemId, playerId, playerKarma, playerPkKills, nbKills, endTime FROM cursed_weapons");
            rset = statement.executeQuery();

            while (rset.next())
            {
                int itemId        = rset.getInt("itemId");
                int playerId      = rset.getInt("playerId");
                int playerKarma   = rset.getInt("playerKarma");
                int playerPkKills = rset.getInt("playerPkKills");
                int nbKills       = rset.getInt("nbKills");
                long endTime      = rset.getLong("endTime");
                
                CursedWeapon cw = _cursedWeapons.get(itemId);
                cw.setPlayerId(playerId);
                cw.setPlayerKarma(playerKarma);
                cw.setPlayerPkKills(playerPkKills);
                cw.setNbKills(nbKills);
                cw.setEndTime(endTime);
                cw.reActivate();
            }

            rset.close();
            statement.close();
            }
            else
            {
                statement = con.prepareStatement("TRUNCATE TABLE cursed_weapons");
                rset = statement.executeQuery();
                rset.close();
                statement.close();
            }
            con.close();
            
            // Retrieve the L2PcInstance from the characters table of the database
            con = L2DatabaseFactory.getInstance().getConnection(con);

            for (CursedWeapon cw : _cursedWeapons.values())
            {
                if (cw.isActivated()) continue;

                // Do an item check to be sure that the cursed weapon isn't hold by someone
                int itemId = cw.getItemId();
                try
                {
                    statement = con.prepareStatement("SELECT owner_id FROM items WHERE item_id=?");
                    statement.setInt(1, itemId);
                    rset = statement.executeQuery();
        
                    if (rset.next())
                    {
                        // A player has the cursed weapon in his inventory ...
                        int playerId = rset.getInt("owner_id");
                        _log.info("PROBLEM : Player "+playerId+" owns the cursed weapon "+itemId+" but he shouldn't.");
                        
                        // Delete the item
                        statement = con.prepareStatement("DELETE FROM items WHERE owner_id=? AND item_id=?");
                        statement.setInt(1, playerId);
                        statement.setInt(2, itemId);
                        if (statement.executeUpdate() != 1)
                        {
                            _log.warn("Error while deleting cursed weapon "+itemId+" from userId "+playerId);
                        }
                        statement.close();
                        
                        // Delete the skill
                        /*
                        statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=? AND skill_id=");
                        statement.setInt(1, playerId);
                        statement.setInt(2, cw.getSkillId());
                        if (statement.executeUpdate() != 1)
                        {
                            _log.warn("Error while deleting cursed weapon "+itemId+" skill from userId "+playerId);
                        }
                        */
						// Restore the player's old karma and pk count
		    			statement = con.prepareStatement("UPDATE characters SET karma=?, pkkills=? WHERE obj_Id=?");
		    			statement.setInt(1, cw.getPlayerKarma());
		    			statement.setInt(2, cw.getPlayerPkKills());
		    			statement.setInt(3, playerId);
		    			if (statement.executeUpdate() != 1)
		    			{
		    				_log.warn("Error while updating karma & pkkills for userId "+cw.getPlayerId());
		    			}
		    			// clean up the cursedweapons table.
		    			removeFromDb(itemId);                        
                    }
                } catch (SQLException sqlE)
                {}
				// close the statement to avoid multiply prepared statement errors in following iterations.
    			try { con.close(); } catch (Exception e) {}                
            }
        }
        catch (Exception e)
        {
            _log.warn("Could not load CursedWeapons data: " + e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }

        _log.info("CursedWeaponsManager: loaded "+_cursedWeapons.size() + " cursed weapon(s).");
    }
    
    // =========================================================
    // Properties - Public
    public synchronized void checkDrop(L2Attackable attackable, L2PcInstance player)
    {
        if(Config.ALLOW_CURSED_WEAPONS)
        {
            if (attackable instanceof L2SiegeGuardInstance
               || attackable instanceof L2RiftInvaderInstance
               || attackable instanceof L2FestivalMonsterInstance
               || attackable instanceof L2GuardInstance
               || attackable instanceof L2Boss
               || attackable instanceof L2FeedableBeastInstance
               )
                  return;

            for (CursedWeapon cw : _cursedWeapons.values())
            {
                if (cw.isActive()) continue;

                if (cw.checkDrop(attackable, player)) break;
            }
        }
    }
    
    public void activate(L2PcInstance player, L2ItemInstance item)
    {
        if(Config.ALLOW_CURSED_WEAPONS)
        {
        	CursedWeapon cw = _cursedWeapons.get(item.getItemId());
        
        	if (player.isCursedWeaponEquipped()) // cannot own 2 cursed swords
        	{
        		CursedWeapon cw2 = _cursedWeapons.get(player.getCursedWeaponEquippedId());

        		cw2.setNbKills(cw2.getStageKills()-1);
	        		cw2.increaseKills();
				
				// erase the newly obtained cursed weapon
				cw.setPlayer(player);  // NECESSARY in order to find which inventory the weapon is in!
				cw.endOfLife();        // expire the weapon and clean up.
        	}
        	else cw.activate(player, item);
        }
    }
    
    public void drop(int itemId, L2Character killer)
    {
        CursedWeapon cw = _cursedWeapons.get(itemId);
        
        cw.dropIt(killer);
    }
    
    public void increaseKills(int itemId)
    {
        CursedWeapon cw = _cursedWeapons.get(itemId);
        
        cw.increaseKills();
    }
    
    public int getLevel(int itemId)
    {
        CursedWeapon cw = _cursedWeapons.get(itemId);
        
        return cw.getLevel();
    }
    
    
    public static void announce(SystemMessage sm)
    {
        for (L2PcInstance player : L2World.getInstance().getAllPlayers())
        {
            if (player == null) continue;
            
            player.sendPacket(sm);
        }
        if (_log.isDebugEnabled())
            _log.info("MessageID: "+sm.getMessageID());
    }
    
    public void checkPlayer(L2PcInstance player)
    {
        if (player == null)
            return;

        for (CursedWeapon cw : _cursedWeapons.values())
        {
            if (cw.isActivated() && player.getObjectId() == cw.getPlayerId())
            {
                cw.setPlayer(player);
                cw.setItem(player.getInventory().getItemByItemId(cw.getItemId()));
                cw.giveSkill();
                player.setCursedWeaponEquippedId(cw.getItemId());
                
                SystemMessage sm = new SystemMessage(SystemMessageId.S2_MINUTE_OF_USAGE_TIME_ARE_LEFT_FOR_S1);
                sm.addString(cw.getName());
                //sm.addItemName(cw.getItemId());
                sm.addNumber((int)((cw.getEndTime() - System.currentTimeMillis()) / 60000));
                player.sendPacket(sm);
            }
        }
    }

    public static void removeFromDb(int itemId)
    {
        Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection(con);

            // Delete datas
            PreparedStatement statement = con.prepareStatement("DELETE FROM cursed_weapons WHERE itemId = ?");
            statement.setInt(1, itemId);
            statement.executeUpdate();
            
            statement.close();
            con.close();
        }
        catch (SQLException e)
        {
            _log.fatal("CursedWeaponsManager: Failed to remove data: " + e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    
    public void saveData()
    {
        for (CursedWeapon cw : _cursedWeapons.values())
        {
            cw.saveData();
        }
    }
    
    
    // =========================================================
    public boolean isCursed(int itemId)
    {
        return _cursedWeapons.containsKey(itemId);
    }
    
    public Collection<CursedWeapon> getCursedWeapons()
    {
        return _cursedWeapons.values();
    }
    
    public Set<Integer> getCursedWeaponsIds()
    {
        return _cursedWeapons.keySet();
    }

    public CursedWeapon getCursedWeapon(int itemId)
    {
        return _cursedWeapons.get(itemId);
    }
    
    public void givePassive(int itemId)
    {
       try { _cursedWeapons.get(itemId).giveSkill(); } catch (Exception e) {/***/}
    }
}
