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
package net.sf.l2j.gameserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.logging.LogManager;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.cache.CrestCache;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.datatables.ArmorSetsTable;
import net.sf.l2j.gameserver.datatables.BuffTemplateTable;
import net.sf.l2j.gameserver.datatables.CharNameTable;
import net.sf.l2j.gameserver.datatables.CharTemplateTable;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.datatables.DoorTable;
import net.sf.l2j.gameserver.datatables.EventDroplist;
import net.sf.l2j.gameserver.datatables.FishTable;
import net.sf.l2j.gameserver.datatables.GmListTable;
import net.sf.l2j.gameserver.datatables.HennaTable;
import net.sf.l2j.gameserver.datatables.HennaTreeTable;
import net.sf.l2j.gameserver.datatables.HeroSkillTable;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.datatables.LevelUpData;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.datatables.NobleSkillTable;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.PetDataTable;
import net.sf.l2j.gameserver.datatables.SkillSpellbookTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.datatables.SkillTreeTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.datatables.StaticObjects;
import net.sf.l2j.gameserver.datatables.SummonItemsData;
import net.sf.l2j.gameserver.datatables.TeleportLocationTable;
import net.sf.l2j.gameserver.datatables.TradeListTable;
import net.sf.l2j.gameserver.handler.AdminCommandHandler;
import net.sf.l2j.gameserver.handler.ItemHandler;
import net.sf.l2j.gameserver.handler.SkillHandler;
import net.sf.l2j.gameserver.handler.UserCommandHandler;
import net.sf.l2j.gameserver.handler.VoicedCommandHandler;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.instancemanager.CoupleManager;
import net.sf.l2j.gameserver.instancemanager.CrownManager;
import net.sf.l2j.gameserver.instancemanager.CursedWeaponsManager;
import net.sf.l2j.gameserver.instancemanager.DayNightSpawnManager;
import net.sf.l2j.gameserver.instancemanager.FactionManager;
import net.sf.l2j.gameserver.instancemanager.FactionQuestManager;
import net.sf.l2j.gameserver.instancemanager.ItemsOnGroundManager;
import net.sf.l2j.gameserver.instancemanager.Manager;
import net.sf.l2j.gameserver.instancemanager.RaidBossSpawnManager;
import net.sf.l2j.gameserver.model.AutoChatHandler;
import net.sf.l2j.gameserver.model.AutoSpawnHandler;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.entity.Hero;
import net.sf.l2j.gameserver.network.IOThread;
import net.sf.l2j.gameserver.script.faenor.FaenorScriptEngine;
import net.sf.l2j.gameserver.taskmanager.TaskManager;
import net.sf.l2j.gameserver.util.DynamicExtension;
import net.sf.l2j.status.Status;
import net.sf.l2j.tools.L2Registry;
import net.sf.l2j.tools.versionning.model.Version;
import net.sf.l2j.tools.versionning.service.VersionningService;
import net.sf.l2j.util.RandomIntGenerator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class ...
 *
 * @version $Revision: 1.29.2.15.2.19 $ $Date: 2005/04/05 19:41:23 $
 */
public class GameServer
{
    private static final Log _log = LogFactory.getLog(GameServer.class.getName());
    private final IOThread _gameThread;
    private final SkillTable _skillTable;
    private final ItemTable _itemTable;
    private final NpcTable _npcTable;
    private final HennaTable _hennaTable;
    private final IdFactory _idFactory;
    public static GameServer gameServer;

    private final ItemHandler _itemHandler;
    private final SkillHandler _skillHandler;
    private final AdminCommandHandler _adminCommandHandler;
    private final Shutdown _shutdownHandler;
    private final UserCommandHandler _userCommandHandler;
    private final VoicedCommandHandler _voicedCommandHandler;
    private final DoorTable _doorTable;
    private final SevenSigns _sevenSignsEngine;
    private final AutoSpawnHandler _autoSpawnHandler;
    private LoginServerThread _loginThread;
    
    public static Status statusServer;
    @SuppressWarnings("unused")
    private final ThreadPoolManager _threadpools;   

    public static final Calendar DateTimeServerStarted = Calendar.getInstance();
    
    public long getUsedMemoryMB()
    {
        return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1048576; // 1024 * 1024 = 1048576;
    }

    public IOThread getSelectorThread()
    {
        return _gameThread;
    }

    /**
     * Initiate all managers, singletons, load data 
     * launch main thread to handle client packets
     * 
     * TODO : let singleton be loaded by spring
     * @throws Throwable
     */
    public GameServer() throws Throwable
    {
        // Local Constants
        // ----------------
        final String LOG_FOLDER = "log"; // Name of folder for log file
        final String LOG_FOLDER_GAME="game";
        // Create log folders
        // -------------------
        File logFolder = new File(Config.DATAPACK_ROOT, LOG_FOLDER); 
        logFolder.mkdir();
        File logFolderGame = new File(logFolder, LOG_FOLDER_GAME); 
        logFolderGame.mkdir();
        
        // o Initialize configuration
        // ------------------
        Config.load();
        
        // Create input stream for log file
        // or store file data into memory
        // -------------------------------
        InputStream is =  new FileInputStream(new File("./config/logging.properties")); 
        LogManager.getLogManager().readConfiguration(is);
        is.close();        
        
        // o Init database factory 
        // TODO : will be replaced by L2Registry.load one day...
        // -----------------------------------------------------
        L2DatabaseFactory.initInstance();

        // o Print used memory
        // --------------------
        if ( _log.isDebugEnabled())_log.debug("used mem:" + getUsedMemoryMB()+"MB" );

        
        // o Initialize the Id factory
        // the type of id factory is modifiable by configuration
        // -----------------------------------------------------
        _idFactory = IdFactory.getInstance();
        if (!_idFactory.isInitialized())
        {
            _log.fatal("Could not read object IDs from DB. Please Check Your Data.");
            throw new Exception("Could not initialize the ID factory");
        }
        
        // o Launch pool of threads
        // -----------------------
        _threadpools = ThreadPoolManager.getInstance();
        if ( _log.isDebugEnabled())_log.debug("ClientScheduler initialized");
        
        // o Initialize folders
        // -------------------
        new File(Config.DATAPACK_ROOT, "data/clans").mkdirs();
        new File(Config.DATAPACK_ROOT, "data/crests").mkdirs();
        new File("pathnode").mkdirs();

        // o start game time control early
        // ------------------------------
        GameTimeController.getInstance();
        if ( _log.isDebugEnabled())_log.debug("TimeController initialized");

        // o Initialize a singleton (unusefull...)
        // TODO rewrite the CharNameTable, no need to 
        // have a singleton and its methods are misplaced
        // ----------------------------------------------
        CharNameTable.getInstance();
        
        // o Load datapack items
        // ---------------------
        _itemTable = ItemTable.getInstance();
        if (!_itemTable.isInitialized())
        {
            _log.fatal("Items not initialized.");
            throw new Exception("Could not initialize the item table");
        }
        
        // o Load items used for summoning
        // ------------------------------
        SummonItemsData.getInstance();
        
        // o Load buylist 
        // --------------
        TradeListTable.getInstance();
        _skillTable = SkillTable.getInstance();
        if (!_skillTable.isInitialized())
        {
            _log.fatal("Skills not initialized.");
            throw new Exception("Could not initialize the skill table");
        }
        
        // o Initialize skill tree from dp
        // --------------------------------
        SkillTreeTable.getInstance();
        if ( _log.isDebugEnabled())_log.debug("SkillTreeTable initialized");
        // o Initialize armor sets from dp
        // --------------------------------
        ArmorSetsTable.getInstance();
        if ( _log.isDebugEnabled())_log.debug("ArmorSetsTable initialized");
        // o Load fish table
        // --------------------------------
        FishTable.getInstance();
        if ( _log.isDebugEnabled())_log.debug("FishTable initialized");
        // o Load skill spellbook table
        // --------------------------------
        SkillSpellbookTable.getInstance();
        if ( _log.isDebugEnabled())_log.debug("SkillSpellbookTable initialized");
        // o Load char templates table
        // --------------------------------
        CharTemplateTable.getInstance();
        if ( _log.isDebugEnabled())_log.debug("ChatTemplateTable initialized");
        // o Load noble skills table
        // --------------------------------
        NobleSkillTable.getInstance();
        if ( _log.isDebugEnabled())_log.debug("NobleSkillTable initialized");
        // o Load heroes
        // -------------
        HeroSkillTable.getInstance();
        if ( _log.isDebugEnabled())_log.debug("HeroSkillTable initialized");        
        // o Initialize Cache for Html and crest
        // TODO : use ehcache for those cache
        // ------------------------------------
        HtmCache.getInstance();
        CrestCache.getInstance();
        
        // o Load npcs
        // ------------
        _npcTable = NpcTable.getInstance();
        if (!_npcTable.isInitialized())
        {
            _log.fatal("Npc Table not initialized.");
            throw new Exception("Could not initialize the npc table");
        }
        
        // o Load henna
        // ------------
        _hennaTable = HennaTable.getInstance();
        
        if (!_hennaTable.isInitialized())
        {
           throw new Exception("Could not initialize the Henna Table");
        }
        
        // o Load henna relation 
        // ----------------------
        HennaTreeTable.getInstance();
        
        if (!_hennaTable.isInitialized())
        {
           throw new Exception("Could not initialize the Henna Tree Table");
        }
        
        // o Load referential tables 
        // ------------------------
        BuffTemplateTable.getInstance();
        if ( _log.isDebugEnabled())_log.debug("BuffTemplateTable initialized");
        GeoData.getInstance();
        if ( _log.isDebugEnabled())_log.debug("GeoData initialized");
        TeleportLocationTable.getInstance();
        if ( _log.isDebugEnabled())_log.debug("TeleportLocationTable initialized");
        LevelUpData.getInstance();
        if ( _log.isDebugEnabled())_log.debug("LevelUpData initialized");
        L2World.getInstance();
        if ( _log.isDebugEnabled())_log.debug("World initialized");
        RandomIntGenerator.getInstance();
        if ( _log.isDebugEnabled())_log.debug("RandomIntGenerator initialized");
        AutoChatHandler.getInstance();
        if ( _log.isDebugEnabled())_log.debug("AutoChatHandler initialized");
        SpawnTable.getInstance();
        if ( _log.isDebugEnabled())_log.debug("SpawnTable initialized");
        RaidBossSpawnManager.getInstance();
        if ( _log.isDebugEnabled())_log.debug("Day/Night SpawnMode initialized");
        DayNightSpawnManager.getInstance().notifyChangeMode();
        if ( _log.isDebugEnabled())_log.debug("RaidBossSpawnManager initialized");
        Announcements.getInstance();
        if ( _log.isDebugEnabled())_log.debug("Announcments initialized");
        MapRegionTable.getInstance();
        if ( _log.isDebugEnabled())_log.debug("MapRegionTable initialized");
        EventDroplist.getInstance();
        if ( _log.isDebugEnabled())_log.debug("EventDroplist initialized");        
        ItemsOnGroundManager.getInstance();
        if ( _log.isDebugEnabled())_log.debug("ItemsOnGroundManager initialized");        
        
        if (Config.AUTODESTROY_ITEM_AFTER > 0 || Config.HERB_AUTO_DESTROY_TIME > 0)
        {
            ItemsAutoDestroy.getInstance();
            if ( _log.isDebugEnabled())_log.debug("ItemsAutoDestroy initialized");
        }
        
        // o Initialize monster race
        // TODO : nothing to initialize...
        // ------------------------
        MonsterRace.getInstance();
        if ( _log.isDebugEnabled())_log.debug("MonsterRace initialized");
        
        // o Load doors and static objects from dp
        // ---------------------------------------
        _doorTable = DoorTable.getInstance();
        StaticObjects.getInstance();
        
        // o Load seven signs engine
        // ------------------------
        _sevenSignsEngine = SevenSigns.getInstance();
        SevenSignsFestival.getInstance();
        // o Load random spawn
        // -------------------
        _autoSpawnHandler = AutoSpawnHandler.getInstance();
        _log.info("AutoSpawnHandler: Loaded " + _autoSpawnHandler.size() + " handlers in total.");

        // o Spawn the Orators/Preachers if in the Seal Validation period.
        // -------------------------------------------------------------
        _sevenSignsEngine.spawnSevenSignsNPC();

        Olympiad.getInstance();
        if ( _log.isDebugEnabled())_log.debug("Olympiad initialized");
        Hero.getInstance();
        if ( _log.isDebugEnabled())_log.debug("Heroes initialized");
        FaenorScriptEngine.getInstance();
        if ( _log.isDebugEnabled())_log.debug("ScriptEngine initialized");
        CursedWeaponsManager.getInstance();
        if ( _log.isDebugEnabled())_log.debug("CursedWeapons initialized");
        CrownManager.getInstance();
        if ( _log.isDebugEnabled())_log.debug("CrownManager initialized");

        // o Couple manager
        // -----------------
        if(Config.ALLOW_WEDDING)
        {
            CoupleManager.getInstance();
            if ( _log.isDebugEnabled())_log.debug("CoupleManager initialized");
        }

        // o Faction manager
        // -----------------
        if(Config.FACTION_ENABLED)
        {
            FactionManager.getInstance();
            if ( _log.isDebugEnabled())_log.debug("FactionManager initialized");
            FactionQuestManager.getInstance();
            if ( _log.isDebugEnabled())_log.debug("FactionQuestManager initialized");
        }
        
        // o Start to announce online players number
        // ---------------------------------------
        if(Config.ONLINE_PLAYERS_ANNOUNCE_INTERVAL > 0)
        	OnlinePlayers.getInstance();        

        // o Initialize item handlers
        // --------------------------
        _itemHandler = ItemHandler.getInstance();
        _log.info("ItemHandler: Loaded " + _itemHandler.size() + " handlers.");

        // o Initialize skills handlers
        // --------------------------
        _skillHandler = SkillHandler.getInstance();
        _log.info("SkillHandler: Loaded " + _skillHandler.size() + " handlers.");

        // o Initialize admin commands handlers
        // --------------------------------------
        _adminCommandHandler = AdminCommandHandler.getInstance();
        _log.info("AdminCommandHandler: Loaded " + _adminCommandHandler.size() + " handlers.");

        // o Initialize user commands handlers
        // --------------------------------------
        _userCommandHandler = UserCommandHandler.getInstance();
        _log.info("UserCommandHandler: Loaded " + _userCommandHandler.size() + " handlers.");

        // o Initialize voiced commands handlers
        // --------------------------------------
        _voicedCommandHandler = VoicedCommandHandler.getInstance();
        _log.info("VoicedCommandHandler: Loaded " + _voicedCommandHandler.size() + " handlers.");

        TaskManager.getInstance();
        if ( _log.isDebugEnabled())_log.debug("TaskManager initialized");
        
        GmListTable.getInstance();
        if ( _log.isDebugEnabled())_log.debug("GmListTable initialized");

        // read pet stats from db
        PetDataTable.getInstance().loadPetsData(); 
        if ( _log.isDebugEnabled())_log.debug("PetData initialized");
        
        // Initialize managers
        // -------------------
        Manager.loadAll();
        
        // o Register a shutdown hook
        // ---------------------------
        _shutdownHandler = Shutdown.getInstance();
        Runtime.getRuntime().addShutdownHook(_shutdownHandler);
        
        // o Open doors
        // ----------
        _doorTable.openDoors ();
        
        // o Load clans
        // -------------
        ClanTable.getInstance();
        if ( _log.isDebugEnabled())_log.debug("Clans initialized");
        
        // Print id factory infos
        // ----------------------
        _log.info("############ GENERAL INFORMATION ###########");
        _log.info("IdFactory: Free ObjectID's remaining: " + IdFactory.getInstance().size());

        // o Initialize the dynamic extension loader
        // -----------------------------------------
        try {
            DynamicExtension.getInstance();
        } catch (Exception ex) {
            _log.warn( "DynamicExtension could not be loaded and initialized", ex);
        }
        
        // o Call system garbage collector
        // ------------------------------
        System.gc();
        
        // o Print memory infos
        // maxMemory is the upper limit the jvm can use, totalMemory the size of the current allocation pool, freeMemory the unused memory in the allocation pool
        long freeMem = (Runtime.getRuntime().maxMemory()-Runtime.getRuntime().totalMemory()+Runtime.getRuntime().freeMemory()) / 1048576; // 1024 * 1024 = 1048576;
        long totalMem = Runtime.getRuntime().maxMemory() / 1048576;
        _log.info("GameServer Started, free memory "+freeMem+" Mb of "+totalMem+" Mb");
        
        // o Start main threads
        // --------------------
        _loginThread = LoginServerThread.getInstance();
        _loginThread.start();
        
        _gameThread = IOThread.getInstance();
        _gameThread.start();
        
        // Print general information
        // --------------------------
        _log.info("Maximum Numbers of Connected Players: " + Config.MAXIMUM_ONLINE_USERS);

        // o Print versions
        // -----------------
        VersionningService versionningService = (VersionningService)L2Registry.getBean("VersionningService");
        Version version = versionningService.getVersion();
        if (version!= null)
        {
            _log.info("L2JFree Server Version:    "+version.getRevisionNumber());
            _log.info("L2JFree Jdk used for compilation:    "+version.getBuildJdk());
            // TODO : add build date to plugin maven build number 
            //_log.info("L2JFree Server Build Date: "+Config.SERVER_BUILD_DATE);
        }
        
        // o Enable telnet server
        // -----------------------
        if ( Config.IS_TELNET_ENABLED ) {
            statusServer = new Status();
            statusServer.start();
        }
        else {
            _log.info("Telnet server is currently disabled.");
        }
        _log.info("################################################");
    }
    
    /**
     * Instantiate the gameserver
     * Main class of the server
     * 
     * @param args (empty)
     * @throws Throwable
     */
    public static void main(String[] args) throws Throwable
    {
        gameServer = new GameServer();
    }
}
