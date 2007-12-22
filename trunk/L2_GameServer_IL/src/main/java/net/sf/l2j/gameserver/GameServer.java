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

import java.net.InetAddress;
import java.util.Calendar;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.admin.AdminSrv;
import net.sf.l2j.gameserver.boat.service.BoatService;
import net.sf.l2j.gameserver.cache.CrestCache;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.datatables.ArmorSetsTable;
import net.sf.l2j.gameserver.datatables.AugmentationData;
import net.sf.l2j.gameserver.datatables.BuffTemplateTable;
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
import net.sf.l2j.gameserver.datatables.NobleSkillTable;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.NpcWalkerRoutesTable;
import net.sf.l2j.gameserver.datatables.PetDataTable;
import net.sf.l2j.gameserver.datatables.SkillSpellbookTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.datatables.SkillTreeTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.datatables.StaticObjects;
import net.sf.l2j.gameserver.datatables.SummonItemsData;
import net.sf.l2j.gameserver.datatables.TeleportLocationTable;
import net.sf.l2j.gameserver.datatables.TradeListTable;
import net.sf.l2j.gameserver.geoeditorcon.GeoEditorListener;
import net.sf.l2j.gameserver.handler.AdminCommandHandler;
import net.sf.l2j.gameserver.handler.ChatHandler;
import net.sf.l2j.gameserver.handler.ItemHandler;
import net.sf.l2j.gameserver.handler.SkillHandler;
import net.sf.l2j.gameserver.handler.UserCommandHandler;
import net.sf.l2j.gameserver.handler.VoicedCommandHandler;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.instancemanager.FrintezzaManager;
import net.sf.l2j.gameserver.instancemanager.AntharasManager;
import net.sf.l2j.gameserver.instancemanager.AuctionManager;
import net.sf.l2j.gameserver.instancemanager.BaiumManager;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.CastleManorManager;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.instancemanager.CoupleManager;
import net.sf.l2j.gameserver.instancemanager.CrownManager;
import net.sf.l2j.gameserver.instancemanager.CursedWeaponsManager;
import net.sf.l2j.gameserver.instancemanager.DayNightSpawnManager;
import net.sf.l2j.gameserver.instancemanager.DimensionalRiftManager;
import net.sf.l2j.gameserver.instancemanager.FactionManager;
import net.sf.l2j.gameserver.instancemanager.FactionQuestManager;
import net.sf.l2j.gameserver.instancemanager.FourSepulchersManager;
import net.sf.l2j.gameserver.instancemanager.IrcManager;
import net.sf.l2j.gameserver.instancemanager.ItemsOnGroundManager;
import net.sf.l2j.gameserver.instancemanager.MapRegionManager;
import net.sf.l2j.gameserver.instancemanager.MercTicketManager;
import net.sf.l2j.gameserver.instancemanager.PetitionManager;
import net.sf.l2j.gameserver.instancemanager.QuestManager;
import net.sf.l2j.gameserver.instancemanager.RaidBossSpawnManager;
import net.sf.l2j.gameserver.instancemanager.RaidPointsManager;
import net.sf.l2j.gameserver.instancemanager.SailrenManager;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
import net.sf.l2j.gameserver.instancemanager.TownManager;
import net.sf.l2j.gameserver.instancemanager.ValakasManager;
import net.sf.l2j.gameserver.instancemanager.VanHalterManager;
import net.sf.l2j.gameserver.instancemanager.ZoneManager;
import net.sf.l2j.gameserver.model.AutoChatHandler;
import net.sf.l2j.gameserver.model.AutoSpawnHandler;
import net.sf.l2j.gameserver.model.L2Manor;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.entity.Hero;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.L2GamePacketHandler;
import net.sf.l2j.gameserver.pathfinding.geonodes.GeoPathFinding;
import net.sf.l2j.gameserver.registry.IServiceRegistry;
import net.sf.l2j.gameserver.script.faenor.FaenorScriptEngine;
import net.sf.l2j.gameserver.skills.SkillsEngine;
import net.sf.l2j.gameserver.taskmanager.TaskManager;
import net.sf.l2j.gameserver.util.DynamicExtension;
import net.sf.l2j.gameserver.util.FloodProtector;
import net.sf.l2j.gameserver.util.PathCreator;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.status.Status;
import net.sf.l2j.tools.L2Registry;
import net.sf.l2j.tools.versionning.model.Version;
import net.sf.l2j.tools.versionning.service.VersionningService;
import net.sf.l2j.util.RandomIntGenerator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jserver.mmocore.network.SelectorServerConfig;
import com.l2jserver.mmocore.network.SelectorThread;

public class GameServer
{
    private static final Log _log = LogFactory.getLog(GameServer.class.getName());	
    public static GameServer gameServer;
    private final IdFactory _idFactory;
    private final Shutdown _shutdownHandler;
    private final SelectorThread<L2GameClient> _selectorThread;
    private static Status _statusServer;
    public static final Calendar dateTimeServerStarted = Calendar.getInstance();
    private LoginServerThread _loginThread;
    
    public GameServer() throws Throwable
    {
        Config.load();
        Util.printSection("Database");
        L2DatabaseFactory.initInstance();
        Util.printSection("Preparations");
        new PathCreator();
        Util.printSection("World");
        RandomIntGenerator.getInstance();
        L2World.getInstance();
        Announcements.getInstance();
        _idFactory = IdFactory.getInstance();
        if (!_idFactory.isInitialized())
        {
            _log.fatal("Could not read object IDs from DB. Please Check Your Data.");
            throw new Exception("Could not initialize the ID factory");
        }
        _log.info("IdFactory: Free ObjectID's remaining: " + IdFactory.getInstance().size());
        ThreadPoolManager.getInstance();
        if (Config.GEODATA)
        {
            GeoData.getInstance();
            if ( _log.isDebugEnabled())_log.debug("GeoData initialized");
            
            if (Config.GEO_PATH_FINDING)
            {
                GeoPathFinding.getInstance();
                if ( _log.isDebugEnabled())_log.debug("GeoPathFinding initialized");
            }
        }
        MapRegionManager.getInstance();
        ZoneManager.getInstance();
        ClanHallManager.getInstance();
        DoorTable.getInstance();
        StaticObjects.getInstance();
        GameTimeController.getInstance();
        BoatService boatService =(BoatService)L2Registry.getBean(IServiceRegistry.BOAT);
        boatService.loadBoatDatas();
        TeleportLocationTable.getInstance();
        Util.printSection("Skills");
        SkillTreeTable.getInstance();
        SkillsEngine.getInstance();
        SkillTable.getInstance();
        NobleSkillTable.getInstance();
        _log.info("NobleSkills initialized");
        HeroSkillTable.getInstance();
        _log.info("HeroSkills initialized");
        Util.printSection("Items");
        ItemTable.getInstance();
        ArmorSetsTable.getInstance();
        AugmentationData.getInstance();
        if(Config.SP_BOOK_NEEDED)
            SkillSpellbookTable.getInstance();
        SummonItemsData.getInstance();
        if(Config.ALLOW_FISHING)
            FishTable.getInstance();
        ItemsOnGroundManager.getInstance();
        if (Config.AUTODESTROY_ITEM_AFTER > 0 || Config.HERB_AUTO_DESTROY_TIME > 0)
            ItemsAutoDestroy.getInstance();
        Util.printSection("Characters");
        CharTemplateTable.getInstance();
        LevelUpData.getInstance();
        HennaTable.getInstance();
        HennaTreeTable.getInstance();
        if(Config.ALLOW_WEDDING)
            CoupleManager.getInstance();
        CursedWeaponsManager.getInstance();
        ClanTable.getInstance();
        CrestCache.getInstance();
        Hero.getInstance();
        Util.printSection("NPCs");
        NpcTable.getInstance();
        HtmCache.getInstance();
        BuffTemplateTable.getInstance();
        if(Config.ALLOW_NPC_WALKERS)
        	NpcWalkerRoutesTable.getInstance().load();
        PetDataTable.getInstance().loadPetsData();
        Util.printSection("Spawns");
        SpawnTable.getInstance();
		DayNightSpawnManager.getInstance().notifyChangeMode();
        RaidBossSpawnManager.getInstance();
        RaidPointsManager.getInstance();
        AutoChatHandler.getInstance();
        AutoSpawnHandler.getInstance();
        Util.printSection("Castles and Towns");
        CastleManager.getInstance();
        CrownManager.getInstance();
        MercTicketManager.getInstance();
        TownManager.getInstance();
        SiegeManager.getInstance();
        Util.printSection("Economy");
        TradeListTable.getInstance();
        CastleManorManager.getInstance();
        L2Manor.getInstance();
        AuctionManager.getInstance();
        Util.printSection("SevenSigns");
        SevenSigns.getInstance();
        SevenSignsFestival.getInstance();
        Util.printSection("Olympiad");
        Olympiad.getInstance();
        Util.printSection("DimensionalRift");
        DimensionalRiftManager.getInstance();
        Util.printSection("FourSepulchers");
        FourSepulchersManager.getInstance().init();
        Util.printSection("Bosses");
        SailrenManager.getInstance().init();
        FrintezzaManager.getInstance().init();
        AntharasManager.getInstance().init();
        BaiumManager.getInstance().init();
        ValakasManager.getInstance().init();
        VanHalterManager.getInstance().init();
        Util.printSection("Quests");
        QuestManager.getInstance();
        Util.printSection("Events/ScriptEngine");
        EventDroplist.getInstance();
        FaenorScriptEngine.getInstance();
        Util.printSection("Extensions");
        if(Config.FACTION_ENABLED)
        {
            Util.printSection("Factions");
            FactionManager.getInstance();
            FactionQuestManager.getInstance();
        }
        try {
            DynamicExtension.getInstance();
        } catch (Exception ex) {
            _log.warn( "DynamicExtension could not be loaded and initialized", ex);
        }
        

        Util.printSection("Handlers");
        ItemHandler.getInstance();
        SkillHandler.getInstance();
        AdminCommandHandler.getInstance();
        UserCommandHandler.getInstance();
        VoicedCommandHandler.getInstance();
        ChatHandler.getInstance();

        Util.printSection("Misc");
        TaskManager.getInstance();
        GmListTable.getInstance();
        PetitionManager.getInstance();
        if(Config.ONLINE_PLAYERS_ANNOUNCE_INTERVAL > 0)
            OnlinePlayers.getInstance();
        FloodProtector.getInstance();
        
        _shutdownHandler = Shutdown.getInstance();
        Runtime.getRuntime().addShutdownHook(_shutdownHandler);

        System.gc();

        Util.printSection("ServerThreads");
        _loginThread = LoginServerThread.getInstance();
        _loginThread.start();
        
        SelectorServerConfig ssc = new SelectorServerConfig(InetAddress.getByName(Config.GAMESERVER_HOSTNAME), Config.PORT_GAME);
        L2GamePacketHandler gph = new L2GamePacketHandler();

        _selectorThread = new SelectorThread<L2GameClient>(ssc, gph, gph, gph);
        _selectorThread.openServerSocket();
        _selectorThread.start();

        if(Config.IRC_ENABLED)
            IrcManager.getInstance().getConnection().sendChan("GameServer Started");
        if ( Config.IS_TELNET_ENABLED ) 
        {
            _statusServer = new Status();
            _statusServer.start();
        }
        else 
            _log.info("Telnet server is currently disabled.");
        if ( Config.JMX_TCP_PORT != -1 ||  Config.JMX_HTTP_PORT != -1 )
            AdminSrv.getInstance().registerMbeans();

        if (Config.ACCEPT_GEOEDITOR_CONN)
            GeoEditorListener.getInstance();

        Util.printSection("l2jfree");
        _log.info("Revision: "+getVersionNumber());
        _log.info("Build date: "+getBuildDate());
        _log.info("Compiler version: "+getBuildJdk());
        _log.info("Maximum Numbers of Connected Players: " + Config.MAXIMUM_ONLINE_USERS);
        printMemUsage();
        
        Util.printSection("GameServerLog");
        if(Config.ENABLE_JYTHON_SHELL)
        {
            Util.printSection("JythonShell");
            Util.JythonShell();
        }
    }
    
    public static void printMemUsage()
    {
        Util.printSection("Memory");
        for (String line : getMemUsage())
                _log.info(line);
    }
    
    public static String[] getMemUsage()
    {
        double maxMem = ((long)(Runtime.getRuntime().maxMemory() / 1024)); // maxMemory is the upper limit the jvm can use
        double allocatedMem = ((long)(Runtime.getRuntime().totalMemory() / 1024)); //totalMemory the size of the current allocation pool
        double nonAllocatedMem = maxMem - allocatedMem; //non allocated memory till jvm limit
        double cachedMem = ((long)(Runtime.getRuntime().freeMemory() / 1024)); // freeMemory the unused memory in the allocation pool
        double usedMem = allocatedMem - cachedMem; // really used memory
        double useableMem = maxMem - usedMem; //allocated, but non-used and non-allocated memory
        return new String[] {
                " - AllowedMemory: " +((int)(maxMem))+" KB",
                "Allocated: "+((int)(allocatedMem))+" KB ("+(((double)(Math.round(allocatedMem/maxMem*1000000)))/10000)+"%)",
                "Non-Allocated: "+((int)(nonAllocatedMem))+" KB ("+(((double)(Math.round(nonAllocatedMem/maxMem*1000000)))/10000)+"%)",
                "- AllocatedMemory: "+((int)(allocatedMem))+" KB",
                "Used: "+((int)(usedMem))+" KB ("+(((double)(Math.round(usedMem/maxMem*1000000)))/10000)+"%)",
                "Unused (cached): "+((int)(cachedMem))+" KB ("+(((double)(Math.round(cachedMem/maxMem*1000000)))/10000)+"%)",
                "- UseableMemory: "+((int)(useableMem))+" KB ("+(((double)(Math.round(useableMem/maxMem*1000000)))/10000)+"%)"
        };
    }
        
    /**
     * @return the revision number 
     */
    public static String getVersionNumber()
    {
        VersionningService versionningService = (VersionningService)L2Registry.getBean(IServiceRegistry.VERSIONNING);
        Version version = versionningService.getVersion();
        if ( version != null )
            return version.getRevisionNumber();
        return "-1";
    }
    
    public static String getBuildDate()
    {
            VersionningService versionningService = (VersionningService)L2Registry.getBean(IServiceRegistry.VERSIONNING);
            Version version = versionningService.getVersion();
            if ( version != null )
                    return version.getBuildDate();
            return "-1";
    }
    
    /**
     * @return the build jdk
     */
    public static String getBuildJdk()
    {
        VersionningService versionningService = (VersionningService)L2Registry.getBean(IServiceRegistry.VERSIONNING);
        Version version = versionningService.getVersion();
        if ( version != null )
            return version.getBuildJdk();
        return "-1";
    }    

    public SelectorThread<L2GameClient> getSelectorThread()
    {
        return _selectorThread;
    }
    
    public long getUsedMemoryMB()
    {
        return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1048576; // 1024 * 1024 = 1048576;
    }
    
    public static void main(String[] args) throws Throwable
    {
        System.setProperty("python.cachedir", "../cachedir");
        gameServer = new GameServer();
    }
}
