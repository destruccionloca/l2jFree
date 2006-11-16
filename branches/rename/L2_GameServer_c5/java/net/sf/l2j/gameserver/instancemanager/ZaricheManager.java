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
package net.sf.l2j.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.SkillTable;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.Earthquake;
import net.sf.l2j.gameserver.serverpackets.ExRedSky;
import net.sf.l2j.gameserver.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.serverpackets.ItemList;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.util.RandomIntGenerator;

/**
 * 
 * @author Micht
 */
public class ZaricheManager
{
    private static final Logger _log = Logger.getLogger(ZaricheManager.class.getName());

    // =========================================================
    private static ZaricheManager _Instance;

    public static final ZaricheManager getInstance()
    {
        if (_Instance == null)
        {
            System.out.println("Initializing ZaricheManager");
            _Instance = new ZaricheManager();
            _Instance.load();
        }
        return _Instance;
    }

    // =========================================================
    // Data Field
    private final int _zaricheItemId = 8190;
    private boolean _isZaricheDropped = false;
    private boolean _isZaricheActive = false;
    private ScheduledFuture _removeZaricheTask;

    private int _nbKills = 0;
    private long _endTime = 0;

    // Player datas
    private int _playerId = 0;
    private L2PcInstance _player = null;
    private L2ItemInstance _item = null;
    private int _playerKarma = 0;
    private int _playerPkKills = 0;

    private static long MAINTENANCE_INTERVAL;

    // =========================================================
    // Constructor
    public ZaricheManager()
    {
        MAINTENANCE_INTERVAL = Config.ZARICHE_DURATION_LOST * 12000; // 60 * 1000 / 5 = 12000
    }

    // =========================================================
    // Method - Private
    private final void load()
    {
        java.sql.Connection con = null;
        try
        {
            // Retrieve the L2PcInstance from the characters table of the database
            con = L2DatabaseFactory.getInstance().getConnection();
            
            PreparedStatement statement = con.prepareStatement("SELECT playerId, playerKarma, playerPkKills, nbKills, endTime FROM zariche");
            ResultSet rset = statement.executeQuery();

            if (rset.next())
            {
                _playerId      = rset.getInt("playerId");
                _playerKarma   = rset.getInt("playerKarma");
                _playerPkKills = rset.getInt("playerPkKills");
                _nbKills       = rset.getInt("nbKills");
                _endTime       = rset.getLong("endTime");
                
                _isZaricheActive = true;
            } else
            {
                _isZaricheActive = false;
                
                // Do an item check to be sure that Zariche isn't hold by someone
                statement = con.prepareStatement("SELECT owner_id FROM items WHERE item_id=?");
                statement.setInt(1, _zaricheItemId);
                rset = statement.executeQuery();

                if (rset.next())
                {
                    // A player has Zariche in his inventory ...
                    _playerId = rset.getInt("owner_id");
                    
                    // Delete the item
                    statement = con.prepareStatement("DELETE FROM items WHERE owner_id=? AND item_id=?");
                    statement.setInt(1, _playerId);
                    statement.setInt(2, _zaricheItemId);
                    if (statement.executeUpdate() != 1)
                    {
                        _log.warning("Error while deleting Zariche item from userId "+_playerId);
                    }
                    
                    // Delete the skill
                    statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=? AND skill_id=3603");
                    statement.setInt(1, _playerId);
                    if (statement.executeUpdate() != 1)
                    {
                        _log.warning("Error while deleting Zariche skill from userId "+_playerId);
                    }
                    
                    _playerId = 0;
                }
            }

            rset.close();
            statement.close();
        }
        catch (Exception e)
        {
            _log.warning("Could not restore Zariche data: " + e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
        
        if (_endTime > 0)
        {
            if (_endTime - System.currentTimeMillis() <= 0)
                endOfZaricheLife();
            else 
                _removeZaricheTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new RemoveZaricheTask(), MAINTENANCE_INTERVAL, MAINTENANCE_INTERVAL);
        }
    }
    
    private void cancelZaricheTask()
    {
        if (_removeZaricheTask != null)
        {
            _removeZaricheTask.cancel(true);
            _removeZaricheTask = null;
        }
    }

    private class RemoveZaricheTask implements Runnable
    {
        protected RemoveZaricheTask()
        {
        }

        public void run() 
        {
            if (System.currentTimeMillis() >= ZaricheManager.getInstance().getEndTime())
                ZaricheManager.getInstance().endOfZaricheLife();
        }
    }

    private void dropIt(L2Character killer)
    {
        dropIt(null, null, killer, false);
    }
    private void dropIt(L2Attackable attackable, L2PcInstance player)
    {
        dropIt(attackable, player, null, true);
    }
    private void dropIt(L2Attackable attackable, L2PcInstance player, L2Character killer, boolean fromMonster)
    {
        _isZaricheActive = false;
        
        if (fromMonster)
        {
            _item = attackable.DropItem(player, _zaricheItemId, 1);
            _item.setDropTime(0); // Prevent Zariche from being removed by ItemsAutoDestroy

            // RedSky and Earthquake
            ExRedSky packet = new ExRedSky(10);
            for (L2PcInstance aPlayer : L2World.getInstance().getAllPlayers())
                aPlayer.sendPacket(packet);
            Earthquake eq = new Earthquake(player.getX(), player.getY(), player.getZ(), 30, 12);
            player.broadcastPacket(eq);
        } else
        {
            _player.dropItem("DieDrop", _item, killer, true);
        
            //L2ItemInstance item = _player.getInventory().getItemByItemId(_zaricheItemId);
            //_player.getInventory().dropItem("DieDrop", item, _player, null);
            //_player.getInventory().getItemByItemId(_zaricheItemId).dropMe(_player, _player.getX(), _player.getY(), _player.getZ());
        }

        _isZaricheDropped = true;
        announce("Demonic Sword Zariche was dropped."); // in the Hot Spring region
    }
    
    private void announce(String txt)
    {
        for (L2PcInstance player : L2World.getInstance().getAllPlayers())
        {
            if (player == null) continue;
            
            player.sendMessage(txt);
        }
        _log.info(txt);
    }
    
    // =========================================================
    // Properties - Public
    public synchronized void checkDrop(L2Attackable attackable,
            L2PcInstance player)
    {
        if (_isZaricheDropped || _isZaricheActive)
            return;

        if ((RandomIntGenerator.getInstance().getRnd()*0.1) <= Config.ZARICHE_DROP_RATE)
        {
            // Drop the item
            dropIt(attackable, player);
            
            // Start the Zarich Life Task
            _endTime = System.currentTimeMillis() + Config.ZARICHE_DURATION * 60000;
            _removeZaricheTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new RemoveZaricheTask(), MAINTENANCE_INTERVAL, MAINTENANCE_INTERVAL);
        }
    }
    

    public void activateZariche(L2PcInstance player, L2ItemInstance item)
    {
        _isZaricheActive = true;
        
        // Player holding Zariche datas
        _player = player;
        _playerId = _player.getObjectId();
        _playerKarma = _player.getKarma();
        _playerPkKills = _player.getPkKills();
        saveData();

        // Change player stats
        _player.setZaricheEquiped(true);
        _player.setKarma(9000000);
        _player.setPkKills(0);
        if (_player.isInParty())
            _player.getParty().oustPartyMember(_player);

        // Add zariche skill
        L2Skill skill = SkillTable.getInstance().getInfo(3603, 1);
        _player.addSkill(skill);
        
        // Equip with Zariche
        _item = item;
        //L2ItemInstance[] items = 
        _player.getInventory().equipItemAndRecord(_item);
        SystemMessage sm = new SystemMessage(SystemMessage.S1_EQUIPPED);
        sm.addItemName(_item.getItemId());
        _player.sendPacket(sm);
        
        // Fully heal player
        _player.setCurrentHpMp(_player.getMaxHp(), _player.getMaxMp());
        _player.setCurrentCp(_player.getMaxCp());

        // Refresh inventory
        if (!Config.FORCE_INVENTORY_UPDATE)
        {
            InventoryUpdate iu = new InventoryUpdate();
            iu.addItem(_item);
            //iu.addItems(Arrays.asList(items));
            _player.sendPacket(iu);
        }
        else _player.sendPacket(new ItemList(_player, false));
        
        // Refresh player stats
        _player.broadcastUserInfo();
        
        announce("The owner of Demonic Sword Zariche has appeared."); //  in the Hot Spring Region
    }
    
    
    public void dropZariche(L2Character killer)
    {
        if (Rnd.get(100) <= Config.ZARICHE_DISAPEAR_CHANCE)
        {
            // Remove Zariche
            endOfZaricheLife();
        } else
        {
            // Reset player stats
            _player.setKarma(_playerKarma);
            _player.setPkKills(_playerPkKills);
            _player.setZaricheEquiped(false);
            _player.removeSkill(SkillTable.getInstance().getInfo(3603, _player.getSkillLevel(3603)));

            _player.abortAttack();
            
            // Unequip Zariche
            //_player.getInventory().unEquipItemInSlot(Inventory.PAPERDOLL_LRHAND);
            
            // Unequip & Drop Zariche
            dropIt(killer);
            
            _player.broadcastUserInfo();
        }
    }
    

    // =========================================================
    public void checkPlayer(L2PcInstance player)
    {
        if (player == null)
            return;
        
        if (player.getObjectId() == _playerId)
        {
            _player = player;
            _item = _player.getInventory().getItemByItemId(_zaricheItemId);
            _player.setZaricheEquiped(true);
        }
    }
    
    public void playerLogout()
    {
        _player = null;
    }
    
    public void endOfZaricheLife()
    {
        if (_isZaricheActive)
        {
            if (_player != null && _player.isOnline() == 1)
            {
                _log.info("Zariche being removed online." );
                _player.setKarma(_playerKarma);
                _player.setPkKills(_playerPkKills);
                _player.setZaricheEquiped(false);
                _player.removeSkill(SkillTable.getInstance().getInfo(3603, _player.getSkillLevel(3603)));
    
                _player.abortAttack();
                
                // Remove Zariche
                _player.getInventory().unEquipItemInBodySlotAndRecord(L2Item.SLOT_LR_HAND);
                /*
                L2ItemInstance[] unequiped = _player.getInventory().unEquipItemInBodySlotAndRecord(L2Item.SLOT_LR_HAND);
                InventoryUpdate iu = new InventoryUpdate();
                iu.addItems(Arrays.asList(unequiped));
                _player.sendPacket(iu);
                */
                
                // Destroy Zariche
                L2ItemInstance removedItem = _player.getInventory().destroyItemByItemId("Zariche", _zaricheItemId, 1, _player, null);
                if (!Config.FORCE_INVENTORY_UPDATE)
                {
                    InventoryUpdate iu = new InventoryUpdate();
                    if (removedItem.getCount() == 0) iu.addRemovedItem(removedItem);
                    else iu.addModifiedItem(removedItem);
            
                    _player.sendPacket(iu);
                }
                else _player.sendPacket(new ItemList(_player, true));   
                
                _player.broadcastUserInfo();
            } else
            {
                _log.info("Zariche being removed offline." );
                // Remove from Db
                java.sql.Connection con = null;
                try
                {
                    con = L2DatabaseFactory.getInstance().getConnection();
                    
                    // Delete the item
                    PreparedStatement statement = con.prepareStatement("DELETE FROM items WHERE owner_id=? AND item_id=?");
                    statement.setInt(1, _playerId);
                    statement.setInt(2, _zaricheItemId);
                    if (statement.executeUpdate() != 1)
                    {
                        _log.warning("Error while deleting Zariche item from userId "+_playerId);
                    }
                    
                    // Delete the skill
                    statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=? AND skill_id=3603");
                    statement.setInt(1, _playerId);
                    if (statement.executeUpdate() != 1)
                    {
                        _log.warning("Error while deleting Zariche skill from userId "+_playerId);
                    }
    
                    // Restore the karma
                    statement = con.prepareStatement("UPDATE characters SET karma=?, pkkills=? WHERE obj_id=?");
                    statement.setInt(1, _playerKarma);
                    statement.setInt(2, _playerPkKills);
                    statement.setInt(3, _playerId);
                    if (statement.executeUpdate() != 1)
                    {
                        _log.warning("Error while updating farma from userId "+_playerId);
                    }
    
                    // Delete infos from zariche table if any
                    statement = con.prepareStatement("DELETE FROM zariche");
                    statement.executeUpdate();
    
                    statement.close();
                }
                catch (Exception e)
                {
                    _log.warning("Could not delete Zariche: " + e);
                }
                finally
                {
                    try { con.close(); } catch (Exception e) {}
                }
            }
        } else
        {
            // Zariche is dropped on the ground
            if (_item != null)
            {
                _item.decayMe();
                L2World.getInstance().removeObject(_item);
                _log.info("Zariche item has been removed from World.");
            }
        }
        
        announce("Zariche has disapeared.");
        
        // Reset Zariche state
        cancelZaricheTask();
        _removeZaricheTask = null;
        _isZaricheActive = false;
        _isZaricheDropped = false;
        _endTime = 0;
        _player = null;
        _playerId = 0;
        _playerKarma = 0;
        _playerPkKills = 0;
        _item = null;
        _nbKills = 0;
    }
    
    public void saveData()
    {
        if (Config.DEBUG)
            System.out.println("ZaricheManager: Saving data to disk.");

        Connection con = null;
        PreparedStatement statement = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();

            statement = con.prepareStatement("DELETE FROM zariche");
            statement.executeUpdate();

            if (_isZaricheActive)
            {
                statement = con.prepareStatement("INSERT INTO zariche (playerId, playerKarma, playerPkKills, nbKills, endTime) VALUES (?, ?, ?, ?, ?)");
                statement.setInt(1, _playerId);
                statement.setInt(2, _playerKarma);
                statement.setInt(3, _playerPkKills);
                statement.setInt(4, _nbKills);
                statement.setLong(5, _endTime);
                statement.executeUpdate();
            }
            
            statement.close();
            con.close();
        }
        catch (SQLException e)
        {
            _log.severe("ZaricheManager: Failed to save data: " + e);
        }
        finally
        {
            try
            {
                statement.close();
            }
            catch (Exception e) {}
            try
            {
                con.close();
            }
            catch (Exception e) {}
        }
    }
    
    
    // =========================================================
    public void increaseKills()
    {
        _nbKills++;
        
        _player.setPkKills(_player.getPkKills() + 1);
        _player.broadcastUserInfo();
        
        if (_nbKills % Config.ZARICHE_STAGE_KILLS == 0 && _nbKills <= Config.ZARICHE_STAGE_KILLS*10)
        {
            L2Skill skill = SkillTable.getInstance().getInfo(3603, _nbKills/Config.ZARICHE_STAGE_KILLS);
            _player.addSkill(skill);
        }
        
        // Reduce time-to-live of Zariche
        _endTime -= Config.ZARICHE_DURATION_LOST * 60000;
    }
    
    public int getZaricheLevel()
    {
        if (_nbKills > Config.ZARICHE_STAGE_KILLS*10)
        {
            return 10;
        } else
        {
            return (_nbKills / Config.ZARICHE_STAGE_KILLS);
        }
    }
    
    public long getEndTime()
    {
        return _endTime;
    }
    
    public long getTimeLeft()
    {
        return _endTime - System.currentTimeMillis();
    }
    
    public int getNbKills()
    {
        return _nbKills;
    }

    public L2PcInstance getPlayer()
    {
        return _player;
    }
    public int getPlayerKarma()
    {
        return _playerKarma;
    }
    
    public boolean isZaricheActive()
    {
        return _isZaricheActive;
    }
    
    public boolean isZaricheDropped()
    {
        return _isZaricheDropped;
    }
    
    public void goTo(L2PcInstance player)
    {
        if (player == null) return;
        
        if (_isZaricheActive)
        {
            // Go to player holding Zariche
            player.teleToLocation(_player.getX(), _player.getY(), _player.getZ() + 20);
        } else if (_isZaricheDropped)
        {
            // Go to item on the groud
            player.teleToLocation(_item.getX(), _item.getY(), _item.getZ() + 20);
        } else
        {
            player.sendMessage("Zariche isn't in the World.");
        }
    }
}
