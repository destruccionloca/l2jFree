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
package net.sf.l2j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javolution.util.FastList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class containce global server configuration.<br>
 * It has static final fields initialized from configuration files.<br>
 * It's initialized at the very begin of startup, and later JIT will optimize
 * away debug/unused code.
 * 
 * @author mkizub
 */
public final class Config {

	protected static Log _log = LogFactory.getLog(Config.class.getName());
    /** Enable/disable assertions */
    public static boolean ASSERT;

    /** Enable/disable DEVELOPER TREATMENT  */
    public static boolean DEVELOPER;

    /** Set if this server is a test server used for development */
    public static boolean TEST_SERVER;

    /** Game Server ports */
    public static int PORT_GAME;
    /** Login Server port */
    public static int PORT_LOGIN;
    /** Number of trys of login before ban */
    public static int LOGIN_TRY_BEFORE_BAN;
    /** Hostname of the Game Server */
    public static String GAMESERVER_HOSTNAME;
    
    // Access to database
    /** Driver to access to database */
    public static String DATABASE_DRIVER;
    /** Path to access to database */
    public static String DATABASE_URL;
    /** Database login */ 
    public static String DATABASE_LOGIN;
    /** Database password */
    public static String DATABASE_PASSWORD;
    /** Maximum number of connections to the database */
    public static int DATABASE_MAX_CONNECTIONS;
    
    /** Maximum number of players allowed to play simultaneously on server */
    public static int   MAXIMUM_ONLINE_USERS;
    
    // Setting for serverList
    /** Displays [] in front of server name ? */
    public static boolean SERVER_LIST_BRACKET;
    /** Displays a clock next to the server name ? */
    public static boolean SERVER_LIST_CLOCK;
    /** Display test server in the list of servers ? */
    public static boolean SERVER_LIST_TESTSERVER;
    /** Set the server as gm only at startup ? */
    public static boolean SERVER_GMONLY;
    /** Safe mode will disable some feature during restart/shutdown to prevent exploit **/
    public static boolean    SAFE_REBOOT = false;    
    // Thread pools size
    /** Thread pool size effect */
    public static int THREAD_P_EFFECTS;
    /** Thread pool size general */
    public static int THREAD_P_GENERAL;
    /** Packet max thread */
    public static int GENERAL_PACKET_THREAD_CORE_SIZE;
    public static int URGENT_PACKET_THREAD_CORE_SIZE;
    /** General max thread */
    public static int GENERAL_THREAD_CORE_SIZE;
    /** AI max thread */
    public static int AI_MAX_THREAD;
    
    /* Show License at login */
    public static boolean SHOW_L2J_LICENSE;
    /* Show html window at login */
    public static boolean SHOW_HTML_WELCOME;
    /** Config for use chat filter **/
    public static boolean USE_SAY_FILTER;
    public static ArrayList<String> FILTER_LIST = new ArrayList<String>();
    
    /** Accept auto-loot ? */
    public static boolean AUTO_LOOT;
    public static boolean AUTO_LOOT_HERBS;

    /** Character name template */
    public static String CNAME_TEMPLATE;
    /** Pet name template */
    public static String PET_NAME_TEMPLATE;    
    /** Maximum number of characters per account */
    public static int MAX_CHARACTERS_NUMBER_PER_ACCOUNT;

    /** Global chat state */
    public static String  DEFAULT_GLOBAL_CHAT;
    /** Trade chat state */
    public static String  DEFAULT_TRADE_CHAT;
    /** For test servers - everybody has admin rights */
    public static boolean EVERYBODY_HAS_ADMIN_RIGHTS;
    /** Alternative game crafting */
    public static boolean ALT_GAME_CREATION;
    /** Alternative game crafting speed mutiplier - default 0 (fastest but still not instant) */
    public static double ALT_GAME_CREATION_SPEED;
    /** Alternative game crafting XP rate multiplier - default 1*/
    public static double ALT_GAME_CREATION_XP_RATE;
    /** Alternative game crafting SP rate multiplier - default 1*/
    public static double ALT_GAME_CREATION_SP_RATE;
    /**Alternative number of cumulated buff */
    public static int ALT_GAME_NUMBER_OF_CUMULATED_BUFF;
   
 	/** Alternative game skill learning */
    public static boolean ALT_GAME_SKILL_LEARN;
    /** Cancel attack bow by hit */
    public static boolean ALT_GAME_CANCEL_BOW;
    /** Cancel cast by hit */
    public static boolean ALT_GAME_CANCEL_CAST;
    
    /** Alternative game - use tiredness, instead of CP */
    public static boolean ALT_GAME_TIREDNESS;

    /** Alternative shield defence */
    public static boolean ALT_GAME_SHIELD_BLOCKS;
    
    /** Alternative Perfect shield defence rate */
    public static int ALT_PERFECT_SHLD_BLOCK;

    /** Alternative game mob ATTACK AI */
    public static boolean ALT_GAME_MOB_ATTACK_AI;
    
    /** Rate of Instant kill effect 2(CP no change ,HP =1,no kill)*/
    public static float ALT_INSTANT_KILL_EFFECT_2;
    
    /** Alternative success rate formulas for skills such root/sleep/stun */
    public static String ALT_GAME_SKILL_FORMULAS;
    /** Alternative damage for dagger skills VS heavy*/
    public static float ALT_DAGGER_DMG_VS_HEAVY;
    /** Alternative damage for dagger skills VS robe*/
    public static float ALT_DAGGER_DMG_VS_ROBE;
    /** Alternative damage for dagger skills VS light*/
    public static float ALT_DAGGER_DMG_VS_LIGHT;
    /** Alternative success rate formulas for skills such dagger/critical skills and blows */
     public static boolean ALT_DAGGER_FORMULA;
    /** Alternative config for next hit delay */
    public static float ALT_ATTACK_DELAY;
    /** Alternative success rate for dagger blow,MAX value 100 (100% rate) */
    public static int ALT_DAGGER_RATE;
    /** Alternative fail rate for dagger blow,MAX value 100 (100% rate) */
    public static int ALT_DAGGER_FAIL_RATE;
    /** Alternative increasement to success rate for dagger/critical skills if activeChar is Behind the target */
     public static int ALT_DAGGER_RATE_BEHIND;
    /** Alternative increasement to success rate for dagger/critical skills if activeChar is in Front of target */
    public static int ALT_DAGGER_RATE_FRONT;
    
    /** Alternative freight modes - Freights can be withdrawed from any village */
    public static boolean ALT_GAME_FREIGHTS;
    /** Alternative freight modes - Sets the price value for each freightened item */
    public static int ALT_GAME_FREIGHT_PRICE;

    /** Fast or slow multiply coefficient for skill hit time */
    public static float ALT_GAME_SKILL_HIT_RATE;

    /** Rate Common herbs */
    public static float   RATE_DROP_COMMON_HERBS;
    /** Rate MP/HP herbs */
    public static float   RATE_DROP_MP_HP_HERBS;
    /** Rate Common herbs */
    public static float   RATE_DROP_GREATER_HERBS;
    /** Rate Common herbs */
    public static float   RATE_DROP_SUPERIOR_HERBS;
    /** Rate Common herbs */
    public static float   RATE_DROP_SPECIAL_HERBS;

    /** Named mobs. Random spawning mobs with multiples of health and rewards. */
    /** Frequency of spawn */
    public static int CHAMPION_FREQUENCY;
    /** Hp multiplier */
    public static int CHAMPION_HP;
    /** Rewards multiplier */
    public static int CHAMPION_REWARDS;

    /** Alternative gameing - loss of XP on death */
    public static boolean ALT_GAME_DELEVEL;

    /** Alternative gameing - magic dmg failures */
    public static boolean ALT_GAME_MAGICFAILURES;

    /** Alternative gaming - player must be in a castle-owning clan or ally to sign up for Dawn. */
    public static boolean ALT_GAME_REQUIRE_CASTLE_DAWN;
    
    /** Alternative gaming - allow clan-based castle ownage check rather than ally-based. */
    public static boolean ALT_GAME_REQUIRE_CLAN_CASTLE;
    
    /** Alternative gaming - allow free teleporting around the world. */
    public static boolean ALT_GAME_FREE_TELEPORT;
    
    /** Alternative gaming - allow sub-class addition without quest completion. */
    public static boolean ALT_GAME_SUBCLASS_WITHOUT_QUESTS;
    
    /** View npc stats/drop by shift-cliking it for nongm-players */
    public static boolean ALT_GAME_VIEWNPC;    
    
    /** Minimum number of player to participate in SevenSigns Festival */
    public static int ALT_FESTIVAL_MIN_PLAYER;

    /** Maximum of player contrib during Festival */
    public static int ALT_MAXIMUM_PLAYER_CONTRIB;    
   
    /** Festival Manager start time. */
    public static long ALT_FESTIVAL_MANAGER_START;

    /** Festival Length */
    public static long ALT_FESTIVAL_LENGTH;

    /** Festival Cycle Length */
    public static long ALT_FESTIVAL_CYCLE_LENGTH;

    /** Festival First Spawn */
    public static long ALT_FESTIVAL_FIRST_SPAWN;

    /** Festival First Swarm */
    public static long ALT_FESTIVAL_FIRST_SWARM;

    /** Festival Second Spawn */
    public static long ALT_FESTIVAL_SECOND_SPAWN;

    /** Festival Second Swarm */
    public static long ALT_FESTIVAL_SECOND_SWARM;

    /** Festival Chest Spawn */
    public static long ALT_FESTIVAL_CHEST_SPAWN;
    
    /** Number of members needed to request a clan war */
    public static int ALT_CLAN_MEMBERS_FOR_WAR;

    /** Number of days before joining a new clan */
    public static int ALT_CLAN_JOIN_DAYS;
    /** Number of days before creating a new clan */
    public static int ALT_CLAN_CREATE_DAYS;

    /** Alternative gaming - all new characters always are newbies. */
    public static boolean ALT_GAME_NEW_CHAR_ALWAYS_IS_NEWBIE;
    
    /** Strict Hero Mode */
    public static boolean ALT_STRICT_HERO_SYSTEM;
    
    /** Untradeable Item List */
    public static String  NONTRADEABLE_ITEMS;
    public static List<Integer> LIST_NONTRADEABLE_ITEMS   = new FastList<Integer>();

    /** Olympiad Compitition Starting time */
    public static int ALT_OLY_START_TIME;
    
    /** Olympiad Compition Min */
    public static int ALT_OLY_MIN;
    
    /** Olympaid Comptetition Period */
    public static int ALT_OLY_CPERIOD;
    
    /** Olympiad Battle Period */
    public static int ALT_OLY_BATTLE;
    
    /** Olympiad Battle Wait */
    public static int ALT_OLY_BWAIT;
    
    /** Olympiad Inital Wait */
    public static int ALT_OLY_IWAIT;
    
    /** Olympaid Weekly Period */
    public static int ALT_OLY_WPERIOD;
    
    /** Olympaid Validation Period */
    public static int ALT_OLY_VPERIOD;
    
    /** Strict Seven Signs */
    public static boolean ALT_STRICT_SEVENSIGNS;
    
    /** Initial Lottery prize */
    public static int ALT_LOTTERY_PRIZE;
    
    /** Lottery Ticket Price */
    public static int ALT_LOTTERY_TICKET_PRICE;
    
    /** What part of jackpot amount should receive characters who pick 5 wining numbers */
    public static float ALT_LOTTERY_5_NUMBER_RATE;
    
    /** What part of jackpot amount should receive characters who pick 4 wining numbers */
    public static float ALT_LOTTERY_4_NUMBER_RATE;
    
    /** What part of jackpot amount should receive characters who pick 3 wining numbers */
    public static float ALT_LOTTERY_3_NUMBER_RATE;
    
    /** How much adena receive characters who pick two or less of the winning number */
    public static int ALT_LOTTERY_2_AND_1_NUMBER_PRIZE;

    /** Alt Settings for devs */
    public static boolean ALT_DEV_NO_QUESTS;
    public static boolean ALT_DEV_NO_SPAWNS;

    /** Enable Rate Hp  */
    public static boolean ENABLE_RATE_HP;

    /** Spell Book needed to learn skill */
    public static boolean SP_BOOK_NEEDED;
    /** Logging Chat Window */
    public static boolean LOG_CHAT;
    public static boolean LOG_ITEMS;
    
    public static boolean ALT_PRIVILEGES_ADMIN;
    public static boolean ALT_PRIVILEGES_SECURE_CHECK;
    public static int ALT_PRIVILEGES_DEFAULT_LEVEL;
        
    public static int ALT_BUFF_TIME;
    public static int ALT_DANCE_TIME;
    /** Config for limit attack speed */
    public static int MAX_PATK_SPEED;
    public static int MAX_MATK_SPEED;
    /** Config for damage multiplies */
    public static float ALT_PHYSICAL_DAMAGE_MULTI;
    public static float ALT_MAGICAL_DAMAGE_MULTI;
    public static float ALT_PHYSICAL_DAMAGE_MULTI_NPC;
    public static float ALT_MAGICAL_DAMAGE_MULTI_NPC;

    /** Config for URN temp fail */
    public static int ALT_URN_TEMP_FAIL;

    /** Buffer Hate **/
    public static int ALT_BUFFER_HATE;
    
    /** No exp cutoff */
    public static int ALT_DIFF_CUTOFF;
    
    /***************************************************************************
     * GM CONFIG General GM AccessLevel *
     **************************************************************************/
    public static int     GM_ACCESSLEVEL;
    /** General GM Minimal AccessLevel */
    public static int     GM_MIN;
    /** General GM AccessLevel to change announcements */
    public static int     GM_ANNOUNCE;
    /** General GM AccessLevel can /ban /unban */
    public static int     GM_BAN;
    /** General GM AccessLevel can /ban /unban for chat */
    public static int     GM_BAN_CHAT;
    /** General GM AccessLevel can /create_item and /gmshop */
    public static int     GM_CREATE_ITEM;
    /** General GM AccessLevel can enchant armor */
    public static int     GM_ENCHANT;
    /** General GM AccessLevel can /delete */
    public static int     GM_DELETE;
    /** General GM AccessLevel can /kick /disconnect */
    public static int     GM_KICK;
    /** General GM AccessLevel for access to GMMenu */
    public static int     GM_MENU;
    /** General GM AccessLevel to use god mode command */
    public static int     GM_GODMODE;
    /** General GM AccessLevel with character edit rights */
    public static int     GM_CHAR_EDIT;
    /** General GM AccessLevel with edit rights for other characters */
    public static int     GM_CHAR_EDIT_OTHER;
    /** General GM AccessLevel with character view rights */
    public static int     GM_CHAR_VIEW;
    /** General GM AccessLevel with NPC edit rights */
    public static int     GM_NPC_EDIT;
    public static int     GM_NPC_VIEW;
    /** General GM AccessLevel to teleport to any location */
    public static int     GM_TELEPORT;
    /** General GM AccessLevel to teleport character to any location */
    public static int     GM_TELEPORT_OTHER;
    /** General GM AccessLevel to restart server */
    public static int     GM_RESTART;
    /** General GM AccessLevel for MonsterRace */
    public static int     GM_MONSTERRACE;
    /** General GM AccessLevel to ride Wyvern */
    public static int     GM_RIDER;
    /** General GM AccessLevel to unstuck without 5min delay */
    public static int     GM_ESCAPE;
    /** General GM AccessLevel to resurect fixed after death */
    public static int     GM_FIXED;
    /** General GM AccessLevel to create Path Nodes */
    public static int     GM_CREATE_NODES;
    /** General GM AccessLevel to close/open Doors */
    public static int     GM_DOOR;
    /** General GM AccessLevel with Resurrection rights */
    public static int     GM_RES;
    /** General GM AccessLevel to attack in the peace zone */
    public static int     GM_PEACEATTACK;   
    /** General GM AccessLevel to heal */
    public static int     GM_HEAL;
    /** General GM AccessLevel to unblock IPs detected as hack IPs */
    public static int     GM_UNBLOCK;
    /** General GM AccessLevel to use Cache commands */
    public static int GM_CACHE;
    /** General GM AccessLevel to use test&st commands */
    public static int GM_TALK_BLOCK;
    public static int GM_TEST;
    /** Disable transaction on AccessLevel **/
    public static boolean GM_DISABLE_TRANSACTION;
    public static int GM_TRANSACTION_MIN;
    public static int GM_TRANSACTION_MAX;
    /** Minimum level to allow a GM giving damage */
    public static int     GM_CAN_GIVE_DAMAGE;
    /** Minimum level to don't give Exp/Sp in party */
    public static int     GM_DONT_TAKE_EXPSP;
    /** Minimum level to don't take aggro */
    public static int     GM_DONT_TAKE_AGGRO;    
    /** GM name color */
    public static boolean   GM_NAME_COLOR_ENABLED;
    public static boolean   GM_TITLE_COLOR_ENABLED;
    public static int       GM_NAME_COLOR;
    public static int       GM_TITLE_COLOR;
    public static int       ADMIN_NAME_COLOR;
    public static int       ADMIN_TITLE_COLOR;
    /** GM Announce at login */
    public static boolean SHOW_GM_LOGIN;
    public static boolean HIDE_GM_STATUS;
    public static boolean GM_STARTUP_INVISIBLE;
    public static boolean GM_STARTUP_SILENCE;
    public static boolean GM_STARTUP_AUTO_LIST;

    public static boolean PETITIONING_ALLOWED;
    public static int MAX_PETITIONS_PER_PLAYER;
    public static int MAX_PETITIONS_PENDING;

    /** Rate control */
    public static float   RATE_XP;
    public static float   RATE_SP;
    public static float   RATE_PARTY_XP;
    public static float   RATE_PARTY_SP;
    public static float   RATE_QUESTS_REWARD;
    public static float   RATE_DROP_ADENA;
    public static float   RATE_CONSUMABLE_COST;
    public static float   RATE_CRAFT_COST;
    public static float   RATE_DROP_ITEMS;
    public static float   RATE_DROP_SPOIL;
    public static float   RATE_DROP_QUEST;
    public static float   RATE_KARMA_EXP_LOST;	
    public static float   RATE_SIEGE_GUARDS_PRICE;	
    /** Rate of boxes spawn */
    public static int     RATE_BOX_SPAWN;
    /*Alternative Xp/Sp rewards, if not 0, then calculated as 2^((mob.level-player.level) / coef)*/
    public static float   ALT_GAME_EXPONENT_XP;
    public static float   ALT_GAME_EXPONENT_SP;
    /** Config for spawn siege guard**/
    public static boolean SPAWN_SIEGE_GUARD; 

    /** Player can drop adena ? */
    public static boolean ALT_PLAYER_CAN_DROP_ADENA;     
    
    /** Player Drop Rate control */
    public static int   PLAYER_DROP_LIMIT;
    public static int   PLAYER_RATE_DROP;
    public static int   PLAYER_RATE_DROP_ITEM;
    public static int   PLAYER_RATE_DROP_EQUIP;    
    public static int   PLAYER_RATE_DROP_EQUIP_WEAPON;    
    public static int   PLAYER_RATE_DROP_ADENA;
    
	/** Pet Rates (Multipliers) */ 	
	public static float         PET_XP_RATE; 
 	public static int           PET_FOOD_RATE;

    /** Karma Drop Rate control */
    public static int   KARMA_DROP_LIMIT;
    public static int   KARMA_RATE_DROP;
    public static int   KARMA_RATE_DROP_ITEM;
    public static int   KARMA_RATE_DROP_EQUIP;    
    public static int   KARMA_RATE_DROP_EQUIP_WEAPON;    

    /** Time after which item will auto-destroy */
    public static int     AUTODESTROY_ITEM_AFTER;
    /** Auto destroy herb time */
    public static int     HERB_AUTO_DESTROY_TIME;
    
    public static boolean SAVE_DROPPED_ITEM;
    
    public static boolean DROP_OVER_MAX_CHANCE;
    public static boolean CATEGORIZE_DROPS;
    public static int CATEGORY2_DROP_LIMIT;
    public static int CATEGORY3_DROP_LIMIT;

    public static int     COORD_SYNCHRONIZE;
    
    public static int     DELETE_DAYS;
    

    /** Datapack root directory */
    public static File    DATAPACK_ROOT;

    /** Maximum range mobs can randomly go from spawn point */
    public static int MAX_DRIFT_RANGE;
    
    public static boolean ALLOWFISHING;
    /** Allow Manor system */
    public static boolean ALLOW_MANOR;

    /** Allow Geodata */
    public static boolean ALLOW_GEODATA;
    public static boolean ALLOW_GEODATA_WATER;
    public static int ALLOW_GEODATA_EXPIRATIONTIME;
    public static boolean ALLOW_GEODATA_CHECK_KNOWN;
    public static boolean ALLOW_GEODATA_DEBUG;
    
    /** Jail config **/
    public static boolean JAIL_IS_PVP;
    public static boolean JAIL_DISABLE_CHAT;
    
    public static String FISHINGMODE;

    // Allow L2Walker client
    public static enum L2WalkerAllowed
    {
        True,
        False,
        GM
    }

    public static L2WalkerAllowed ALLOW_L2WALKER_CLIENT;
    public static boolean       AUTOBAN_L2WALKER_ACC;
    public static int           L2WALKER_REVISION;

    public static boolean       ALLOW_DISCARDITEM;
    public static boolean       ALLOW_FREIGHT;
    public static boolean       ALLOW_WAREHOUSE;
    public static boolean 	    ALLOW_WEAR;
    public static int           WEAR_DELAY;
    public static int           WEAR_PRICE;    
    public static boolean 	    ALLOW_LOTTERY;
    public static boolean 	    ALLOW_RACE;
    public static boolean 	    ALLOW_WATER;
    public static boolean       ALLOW_RENTPET;
    public static boolean 	    ALLOW_BOAT;
    /** Allow cursed weapons ? */
    public static boolean        ALLOW_CURSED_WEAPONS;    
    
    public static int           PACKET_LIFETIME;

    // Pets
    public static int           WYVERN_SPEED;
    public static int           STRIDER_SPEED;
    public static boolean       ALLOW_WYVERN_UPGRADER;

    // protocol revision
    public static int           MIN_PROTOCOL_REVISION;
    public static int           MAX_PROTOCOL_REVISION;

    // random animation interval
    public static int           MIN_NPC_ANIMATION;
    public static int           MAX_NPC_ANIMATION;

    public static boolean       ACTIVATE_POSITION_RECORDER;
    public static boolean       USE_3D_MAP;

    // Community Board
    public static String        COMMUNITY_TYPE;
    public static String        BBS_DEFAULT;    
    public static boolean       SHOW_LEVEL_COMMUNITYBOARD;
    public static boolean       SHOW_STATUS_COMMUNITYBOARD;
    public static int           NAME_PAGE_SIZE_COMMUNITYBOARD;
    public static int           NAME_PER_ROW_COMMUNITYBOARD;

    /** Configuration files */
    /** Properties file for game server (connection and ingame) configurations */
    public static final String  CONFIGURATION_FILE          = "./config/server.properties";
    /** Properties file for game server options */
    public static final String  OPTIONS_FILE                = "./config/options.properties";
    /** Properties file for the ID factory */
    public static final String  ID_CONFIG_FILE				= "./config/idfactory.properties";
    public static final String  OTHER_CONFIG_FILE			= "./config/other.properties";
    /** Properties file for rates configurations */
    public static final String  RATES_CONFIG_FILE           = "./config/rates.properties";
    public static final String  ENCHANT_CONFIG_FILE         = "./config/enchant.properties";
    public static final String  ALT_SETTINGS_FILE			= "./config/altsettings.properties";
    public static final String  PVP_CONFIG_FILE				= "./config/pvp.properties";
    public static final String  GM_ACCESS_FILE				= "./config/GMAccess.properties";
    public static final String  TELNET_FILE					= "./config/telnet.properties";
    public static final String  VERSION_FILE				= "./config/l2j-version.properties";
    public static final String  SIEGE_CONFIGURATION_FILE	= "./config/siege.properties";
    public static final String  BANNED_IP_XML				= "./config/banned.xml";
    public static final String  HEXID_FILE					= "./config/hexid.txt";
    public static final String  COMMAND_PRIVILEGES_FILE     = "./config/command-privileges.properties";
    public static final String  SEVENSIGNS_FILE             = "./config/sevensigns.properties";    
    /** Properties file for externsions */
    public static final String EXTENSION_FILE               = "./config/extensions.properties";
    public static final String	SAY_FILTER_FILE				= "./config/sayfilter.txt";
    
    public static boolean       CHECK_KNOWN;
    
    public static int           GAME_SERVER_LOGIN_PORT;
    public static String        GAME_SERVER_LOGIN_HOST;
    public static String        INTERNAL_HOSTNAME;
    public static String        EXTERNAL_HOSTNAME;
    /** IO_Type */
    public static String        IO_TYPE;
    public static int           PATH_NODE_RADIUS;
    public static int           NEW_NODE_ID;
    public static int           SELECTED_NODE_ID;
    public static int           LINKED_NODE_ID;
    public static String        NEW_NODE_TYPE;
    public static boolean       FORCE_INVENTORY_UPDATE;
    public static boolean       ALLOW_GUARDS;
    public static boolean       SPAWN_CLASS_MASTER;
	public static boolean       ALLOW_CLASS_MASTER_1;
    public static boolean       ALLOW_CLASS_MASTER_2;
    public static boolean       ALLOW_CLASS_MASTER_3;
	public static int           IP_UPDATE_TIME;
    
    public static String        SERVER_VERSION;
    public static String        SERVER_BUILD_DATE;
    
    /** Show L2Monster level and aggro ? */
    public static boolean       SHOW_NPC_LVL;
    
    /** Zone Setting */
    public static int           ZONE_TOWN;
    
    /** Crafting Enabled? */
    public static boolean       IS_CRAFTING_ENABLED;
    
    /** Inventory slots limits */
    public static int           INVENTORY_MAXIMUM_NO_DWARF;
    public static int           INVENTORY_MAXIMUM_DWARF;
    public static int           INVENTORY_MAXIMUM_GM;
    
    /** Weight limit */
    public static int ADD_MAX_LOAD;
   
    /** Warehouse slots limits */
    public static int           WAREHOUSE_SLOTS_NO_DWARF;
    public static int           WAREHOUSE_SLOTS_DWARF;
    public static int           WAREHOUSE_SLOTS_CLAN;
    public static int           FREIGHT_SLOTS;
    
    /** Spoil Rates */
    public static boolean CAN_SPOIL_LOWER_LEVEL_MOBS;
    public static boolean CAN_DELEVEL_AND_SPOIL_MOBS;
    public static float   MAXIMUM_PLAYER_AND_MOB_LEVEL_DIFFERENCE;
    public static float   BASE_SPOIL_RATE;
    public static float   MINIMUM_SPOIL_RATE;
    public static float   SPOIL_LEVEL_DIFFERENCE_LIMIT;
    public static float   SPOIL_LEVEL_DIFFERENCE_MULTIPLIER;
    public static int     LAST_LEVEL_SPOIL_IS_LEARNED;
    
    /** Karma System Variables */
    public static int     KARMA_MIN_KARMA;
    public static int     KARMA_MAX_KARMA;
    public static int     KARMA_XP_DIVIDER;
    public static int     KARMA_LOST_BASE;
    public static boolean KARMA_DROP_GM;
    public static boolean KARMA_AWARD_PK_KILL;
    public static int     KARMA_PK_LIMIT;
    
    public static String  KARMA_NONDROPPABLE_PET_ITEMS;
    public static String  KARMA_NONDROPPABLE_ITEMS;
    public static List<Integer> KARMA_LIST_NONDROPPABLE_PET_ITEMS   = new FastList<Integer>();
    public static List<Integer> KARMA_LIST_NONDROPPABLE_ITEMS       = new FastList<Integer>();

    public static String  NONDROPPABLE_ITEMS;
    public static List<Integer> LIST_NONDROPPABLE_ITEMS       = new FastList<Integer>();

    public static String  PET_RENT_NPC;
    public static List<Integer> LIST_PET_RENT_NPC   = new FastList<Integer>();
    
    public static int PVP_TIME;    

    /** Karma Punishment */
    public static boolean ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE;
    public static boolean ALT_GAME_KARMA_PLAYER_CAN_SHOP;
    /** Allow player with karma to use GK ? */
    public static boolean ALT_GAME_KARMA_PLAYER_CAN_USE_GK;    
    public static boolean ALT_GAME_KARMA_PLAYER_CAN_TELEPORT;
    public static boolean ALT_GAME_KARMA_PLAYER_CAN_TRADE;
    public static boolean ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE;
    /** Config for Auto Learn Skills */
    public static boolean AUTO_LEARN_SKILLS;
    /** Disable Grade penalty */
    public static boolean GRADE_PENALTY;
    /** Clan leader name color */
    public static enum ClanLeaderColored
    {
        name,
        title
    }
    public static boolean   CLAN_LEADER_COLOR_ENABLED;
    public static ClanLeaderColored       CLAN_LEADER_COLORED;
    public static int       CLAN_LEADER_COLOR;
    public static int       CLAN_LEADER_COLOR_CLAN_LEVEL;

    /** Day/Night Status **/
    public static boolean DAY_STATUS_FORCE_CLIENT_UPDATE;
    public static int     DAY_STATUS_SUN_RISE_AT;
    public static int     DAY_STATUS_SUN_SET_AT;
    
    /**  
	 * Counting of amount of packets per minute  
	 */  
	public static boolean  COUNT_PACKETS           = false;
	public static boolean  DUMP_PACKET_COUNTS      = false;
    public static int      DUMP_INTERVAL_SECONDS   = 60;
    
	public static enum IdFactoryType
    {
	    Compaction,
        BitSet,
        Stack
    }
    
    /** ID Factory type */
    public static IdFactoryType IDFACTORY_TYPE;
    /** Check for bad ID ? */
    public static boolean BAD_ID_CHECKING;
	
    /** Enumeration for type of maps object */
    public static enum ObjectMapType
    {
        L2ObjectHashMap,
        WorldObjectMap
    }

    /** Enumeration for type of set object */
    public static enum ObjectSetType
    {
        L2ObjectHashSet,
        WorldObjectSet
    }

    /** Type of map object */
    public static ObjectMapType   MAP_TYPE;
    /** Type of set object */
    public static ObjectSetType   SET_TYPE;
    
    /**
     * Allow lesser effects to be canceled if stronger effects are used when effects of the same stack group are used.<br> 
     * New effects that are added will be canceled if they are of lesser priority to the old one.
     */
    public static boolean EFFECT_CANCELING;

    /** Auto-delete invalid quest data ? */
    public static boolean AUTODELETE_INVALID_QUEST_DATA;
    
    /** Chance that an item will succesfully be enchanted */
    public static int ENCHANT_CHANCE_WEAPON;
    public static int ENCHANT_CHANCE_ARMOR;
    public static int ENCHANT_CHANCE_JEWELRY;
    public static int ENCHANT_CHANCE_WEAPON_CRYSTAL;
    public static int ENCHANT_CHANCE_ARMOR_CRYSTAL;
    public static int ENCHANT_CHANCE_JEWELRY_CRYSTAL;
    public static int ENCHANT_CHANCE_WEAPON_BLESSED;
    public static int ENCHANT_CHANCE_ARMOR_BLESSED;
    public static int ENCHANT_CHANCE_JEWELRY_BLESSED;
    /** If an enchant fails - will the item break or only reset to 0? */
    public static boolean ENCHANT_BREAK_WEAPON;
    public static boolean ENCHANT_BREAK_ARMOR;
    public static boolean ENCHANT_BREAK_JEWELRY;
    public static boolean ENCHANT_BREAK_WEAPON_CRYSTAL;
    public static boolean ENCHANT_BREAK_ARMOR_CRYSTAL;
    public static boolean ENCHANT_BREAK_JEWELRY_CRYSTAL;
    public static boolean ENCHANT_BREAK_WEAPON_BLESSED;
    public static boolean ENCHANT_BREAK_ARMOR_BLESSED;
    public static boolean ENCHANT_BREAK_JEWELRY_BLESSED;
    
    /** Chance For Soul Crystal to Break **/
    public static int CHANCE_BREAK;
    /** Chance For Soul Crystal to Level **/
    public static int CHANCE_LEVEL;
    /** Enchant hero weapons? */
    public static boolean ENCHANT_HERO_WEAPONS;
    /* Dwarf enchant System? */
    public static boolean ENCHANT_DWARF_SYSTEM;
    /** Maximum level of enchantment */
    public static int ENCHANT_MAX_WEAPON;
    public static int ENCHANT_MAX_ARMOR;
    public static int ENCHANT_MAX_JEWELRY;
    /** maximum level of safe enchantment */
    public static int ENCHANT_SAFE_MAX;
    public static int ENCHANT_SAFE_MAX_FULL;
    
    // NPC regen multipliers
    public static double  HP_REGEN_MULTIPLIER;
    public static double  MP_REGEN_MULTIPLIER;
    public static double  CP_REGEN_MULTIPLIER;

    // Player regen multipliers
    public static double  PLAYER_HP_REGEN_MULTIPLIER;
    public static double  PLAYER_MP_REGEN_MULTIPLIER;
    public static double  PLAYER_CP_REGEN_MULTIPLIER;
    
    // Raid Boss multipliers
    /** Multiplier for Raid boss HP regeneration */ 
    public static double   RAID_HP_REGEN_MULTIPLIER;
    /** Mulitplier for Raid boss MP regeneration */
    public static double   RAID_MP_REGEN_MULTIPLIER;
    /** Multiplier for Raid boss defense multiplier */
    public static double   RAID_DEFENCE_MULTIPLIER;
    
    /** Amount of adenas when starting a new character */
    public static int STARTING_ADENA;
    
    /** Deep Blue Mobs' Drop Rules Enabled */
    public static boolean DEEPBLUE_DROP_RULES;
    public static int     UNSTUCK_INTERVAL;
    
    /** Is telnet enabled ? */
    public static boolean IS_TELNET_ENABLED;
    
    /** Player Protection control */
    public static int   PLAYER_SPAWN_PROTECTION;

    /** Define Party XP cutoff point method - Possible values: level and percentage */
    public static String  PARTY_XP_CUTOFF_METHOD;
    /** Define the cutoff point value for the "level" method */
    public static int PARTY_XP_CUTOFF_LEVEL;
    /** Define the cutoff point value for the "percentage" method */
    public static double  PARTY_XP_CUTOFF_PERCENT;
    /** Range of Members to get exp/drops **/
    public static int PARTY_RANGE; 
    
    /** Percent CP is restore on respawn */
    public static double  RESPAWN_RESTORE_CP;
    /** Percent HP is restore on respawn */
    public static double  RESPAWN_RESTORE_HP;
    /** Percent MP is restore on respawn */
    public static double  RESPAWN_RESTORE_MP;
 	/** Allow randomizing of the respawn point in towns. */ 
 	public static boolean RESPAWN_RANDOM_ENABLED; 
 	/** The maximum offset from the base respawn point to allow. */ 
 	public static int RESPAWN_RANDOM_MAX_OFFSET;
 	
    /** Maximum number of available slots for pvt stores (sell/buy) - Dwarves */
    public static int  MAX_PVTSTORE_SLOTS_DWARF;
    /** Maximum number of available slots for pvt stores (sell/buy) - Others */
    public static int  MAX_PVTSTORE_SLOTS_OTHER;
    
    /** Store skills cooltime on char exit/relogin */
    public static boolean STORE_SKILL_COOLTIME;
	
    /** Default punishment for illegal actions */
    public static int DEFAULT_PUNISH;
    /** Parameter for default punishment */
    public static int DEFAULT_PUNISH_PARAM;    
    
    /** Hexadecimal ID of the game server */
	public static byte[] HEX_ID;
    /** Accept alternate ID for server ? */
	public static boolean ACCEPT_ALTERNATE_ID;
    /** ID for request to the server */
	public static int REQUEST_ID;
	public static boolean RESERVE_HOST_ON_LOGIN = false;
    
    public static int MINIMUM_UPDATE_DISTANCE;
    public static int KNOWNLIST_FORGET_DELAY;
    public static int MINIMUN_UPDATE_TIME;
    
    public static boolean ANNOUNCE_MAMMON_SPAWN;
    public static boolean LAZY_CACHE;
    
    public static boolean GM_NAME_COLOUR_ENABLED;
    public static int GM_NAME_COLOUR;
    /** Place an aura around the GM ? */
    public static boolean GM_HERO_AURA;
    /** Set the GM invulnerable at startup ? */
    public static boolean GM_STARTUP_INVULNERABLE;
    
    public static boolean	BYPASS_VALIDATION;
    public static boolean GMAUDIT;
    
	public static boolean FLOOD_PROTECTION;
	public static int FAST_CONNECTION_LIMIT;
	public static int NORMAL_CONNECTION_TIME;
	public static int FAST_CONNECTION_TIME;
	public static int MAX_CONNECTION_PER_IP;
    
    public static boolean GAMEGUARD_ENFORCE;
    public static boolean GAMEGUARD_PROHIBITACTION;    
    
    /** Show Online Players announce */
    public static boolean ONLINE_PLAYERS_AT_STARTUP;
    public static int  ONLINE_PLAYERS_ANNOUNCE_INTERVAL; 
    
    /** Recipebook limits */
    public static int DWARF_RECIPE_LIMIT;

    public static int COMMON_RECIPE_LIMIT;  

    /** Grid Options */
    public static boolean GRIDS_ALWAYS_ON;
    public static int GRID_NEIGHBOR_TURNON_TIME;
    public static int GRID_NEIGHBOR_TURNOFF_TIME;    
    /** Extension Options */
    public static String TVT_EVEN_TEAMS;
    public static String CTF_EVEN_TEAMS;
    
    /** Clan Hall function related configs*/
    public static long CH_TELE_FEE_RATIO;
    public static int CH_TELE1_FEE;
    public static int CH_TELE2_FEE;
    public static int CH_TELE3_FEE;
    public static long CH_ITEM_FEE_RATIO;
    public static int CH_ITEM1_FEE;
    public static int CH_ITEM2_FEE;
    public static int CH_ITEM3_FEE;
    public static long CH_MPREG_FEE_RATIO;
    public static int CH_MPREG1_FEE;
    public static int CH_MPREG2_FEE;
    public static int CH_MPREG3_FEE;
    public static long CH_HPREG_FEE_RATIO;
    public static int CH_HPREG1_FEE;
    public static int CH_HPREG2_FEE;
    public static int CH_HPREG3_FEE;
    public static int CH_HPREG4_FEE;
    public static long CH_EXPREG_FEE_RATIO;
    public static int CH_EXPREG1_FEE;
    public static int CH_EXPREG2_FEE;
    public static int CH_EXPREG3_FEE;
    public static int CH_EXPREG4_FEE;
    public static long CH_SUPPORT_FEE_RATIO;
    public static int CH_SUPPORT1_FEE;
    public static int CH_SUPPORT2_FEE;
    public static int CH_SUPPORT3_FEE;
    public static int CH_SUPPORT4_FEE;
    public static int CH_SUPPORT5_FEE;
    
	public static void load()
	{
			_log.info("loading gameserver config");
		    try {
		        Properties serverSettings    = new Properties();
				InputStream is               = new FileInputStream(new File(CONFIGURATION_FILE));
				serverSettings.load(is);
				is.close();
				
				GAME_SERVER_LOGIN_HOST  = serverSettings.getProperty("LoginHost","127.0.0.1");
				GAME_SERVER_LOGIN_PORT  = Integer.parseInt(serverSettings.getProperty("LoginPort","9013"));
				
				REQUEST_ID              = Integer.parseInt(serverSettings.getProperty("RequestServerID","0"));
				ACCEPT_ALTERNATE_ID     = Boolean.parseBoolean(serverSettings.getProperty("AcceptAlternateID","True"));
				
	            PORT_GAME               = Integer.parseInt(serverSettings.getProperty("GameserverPort", "7777"));
	            PORT_LOGIN              = Integer.parseInt(serverSettings.getProperty("LoginserverPort", "2106"));
	            CNAME_TEMPLATE 		    = serverSettings.getProperty("CnameTemplate", ".*");
                PET_NAME_TEMPLATE       = serverSettings.getProperty("PetNameTemplate", ".*");
                
                MAX_CHARACTERS_NUMBER_PER_ACCOUNT = Integer.parseInt(serverSettings.getProperty("CharMaxNumber", "0"));
	            LOGIN_TRY_BEFORE_BAN    = Integer.parseInt(serverSettings.getProperty("LoginTryBeforeBan", "10"));
	            GAMESERVER_HOSTNAME     = serverSettings.getProperty("GameserverHostname");

                DEFAULT_GLOBAL_CHAT          = serverSettings.getProperty("GlobalChat", "ON");
                DEFAULT_TRADE_CHAT           = serverSettings.getProperty("TradeChat", "ON");
	
				DATAPACK_ROOT    = new File(serverSettings.getProperty("DatapackRoot", ".")).getCanonicalFile();
	
	            MIN_PROTOCOL_REVISION   = Integer.parseInt(serverSettings.getProperty("MinProtocolRevision", "694"));
	            MAX_PROTOCOL_REVISION   = Integer.parseInt(serverSettings.getProperty("MaxProtocolRevision", "709"));
	            
	            if (MIN_PROTOCOL_REVISION > MAX_PROTOCOL_REVISION)
	            {
	            	throw new Error("MinProtocolRevision is bigger than MaxProtocolRevision in server configuration file.");
	            }
	            
                INTERNAL_HOSTNAME   = serverSettings.getProperty("InternalHostname", "*");
	            EXTERNAL_HOSTNAME   = serverSettings.getProperty("ExternalHostname", "*");
                
                IO_TYPE                 = serverSettings.getProperty("IOType", "nio");
	            
	            MAXIMUM_ONLINE_USERS        = Integer.parseInt(serverSettings.getProperty("MaximumOnlineUsers", "100"));
                
	            DATABASE_DRIVER             = serverSettings.getProperty("Driver", "com.mysql.jdbc.Driver");
	            DATABASE_URL                = serverSettings.getProperty("URL", "jdbc:mysql://localhost/l2jdb");
	            DATABASE_LOGIN              = serverSettings.getProperty("Login", "root");
	            DATABASE_PASSWORD           = serverSettings.getProperty("Password", "");
	            DATABASE_MAX_CONNECTIONS    = Integer.parseInt(serverSettings.getProperty("MaximumDbConnections", "10"));
                
                SAFE_REBOOT  = Boolean.valueOf(serverSettings.getProperty("SafeReboot", "False"));
	        }
	        catch (Exception e)
	        {
	            _log.error(e.getMessage(),e);
	            throw new Error("Failed to Load "+CONFIGURATION_FILE+" File.");
	        }

            try 
            {
                Properties optionsSettings    = new Properties();
                InputStream is               = new FileInputStream(new File(OPTIONS_FILE));
                optionsSettings.load(is);
                is.close();

                EVERYBODY_HAS_ADMIN_RIGHTS      = Boolean.parseBoolean(optionsSettings.getProperty("EverybodyHasAdminRights", "false"));
                             
                ASSERT                          = Boolean.parseBoolean(optionsSettings.getProperty("Assert", "false"));
                DEVELOPER                       = Boolean.parseBoolean(optionsSettings.getProperty("Developer", "false"));
                TEST_SERVER                     = Boolean.parseBoolean(optionsSettings.getProperty("TestServer", "false"));
                SERVER_LIST_TESTSERVER          = Boolean.parseBoolean(optionsSettings.getProperty("TestServer", "false"));
                             
                SERVER_LIST_BRACKET             = Boolean.valueOf(optionsSettings.getProperty("ServerListBrackets", "false"));
                SERVER_LIST_CLOCK               = Boolean.valueOf(optionsSettings.getProperty("ServerListClock", "false"));
                SERVER_GMONLY                   = Boolean.valueOf(optionsSettings.getProperty("ServerGMOnly", "false"));
                
                AUTODESTROY_ITEM_AFTER          = Integer.parseInt(optionsSettings.getProperty("AutoDestroyDroppedItemAfter", "0"));
                HERB_AUTO_DESTROY_TIME          = Integer.parseInt(optionsSettings.getProperty("AutoDestroyHerbTime","15"))*1000;
                SAVE_DROPPED_ITEM              = Boolean.valueOf(optionsSettings.getProperty("SaveDroppedItem", "false"));
                
                DROP_OVER_MAX_CHANCE            = Boolean.valueOf(optionsSettings.getProperty("DropOverMaxChance", "true")); 
                CATEGORIZE_DROPS                = Boolean.valueOf(optionsSettings.getProperty("CategorizeDrops", "true"));
                CATEGORY2_DROP_LIMIT            = Integer.parseInt(optionsSettings.getProperty("Category2DropLimit", "3"));
                CATEGORY3_DROP_LIMIT            = Integer.parseInt(optionsSettings.getProperty("Category3DropLimit", "3"));
             
                COORD_SYNCHRONIZE               = Integer.parseInt(optionsSettings.getProperty("CoordSynchronize", "-1"));
             
                ALLOW_WAREHOUSE                 = Boolean.valueOf(optionsSettings.getProperty("AllowWarehouse", "True"));
                ALLOW_FREIGHT                   = Boolean.valueOf(optionsSettings.getProperty("AllowFreight", "True"));
                ALLOW_WEAR                      = Boolean.valueOf(optionsSettings.getProperty("AllowWear", "False"));
                WEAR_DELAY                      = Integer.parseInt(optionsSettings.getProperty("WearDelay", "5"));
                WEAR_PRICE                      = Integer.parseInt(optionsSettings.getProperty("WearPrice", "10"));
                ALLOW_LOTTERY                   = Boolean.valueOf(optionsSettings.getProperty("AllowLottery", "False"));
                ALLOW_RACE                      = Boolean.valueOf(optionsSettings.getProperty("AllowRace", "False"));
                ALLOW_WATER                     = Boolean.valueOf(optionsSettings.getProperty("AllowWater", "False"));
                ALLOW_RENTPET                   = Boolean.valueOf(optionsSettings.getProperty("AllowRentPet", "False"));
                ALLOW_DISCARDITEM               = Boolean.valueOf(optionsSettings.getProperty("AllowDiscardItem", "True"));
                ALLOWFISHING                    = Boolean.valueOf(optionsSettings.getProperty("AllowFishing", "False"));
                ALLOW_MANOR                     = Boolean.valueOf(optionsSettings.getProperty("AllowManor", "False"));
                ALLOW_GEODATA                   = Boolean.valueOf(optionsSettings.getProperty("AllowGeodata", "False"));
                ALLOW_GEODATA_WATER             = Boolean.valueOf(optionsSettings.getProperty("AllowGeodataWater", "False"));
                ALLOW_GEODATA_EXPIRATIONTIME    = Integer.parseInt(optionsSettings.getProperty("AllowGeodata_ExpirationTime", "9000000"));
                ALLOW_GEODATA_CHECK_KNOWN       = Boolean.valueOf(optionsSettings.getProperty("AllowGeodataCheckKnown", "False"));
                ALLOW_GEODATA_DEBUG             = Boolean.valueOf(optionsSettings.getProperty("AllowGeodataDebug", "False"));
                ALLOW_BOAT                      = Boolean.valueOf(optionsSettings.getProperty("AllowBoat", "False"));
                ALLOW_CURSED_WEAPONS            = Boolean.valueOf(optionsSettings.getProperty("AllowCursedWeapons", "False"));
                FISHINGMODE                     = optionsSettings.getProperty("FishingMode", "water");                
               
                ALLOW_L2WALKER_CLIENT           = L2WalkerAllowed.valueOf(optionsSettings.getProperty("AllowL2Walker", "False"));
                L2WALKER_REVISION               = Integer.parseInt(optionsSettings.getProperty("L2WalkerRevision", "537"));
                AUTOBAN_L2WALKER_ACC            = Boolean.valueOf(optionsSettings.getProperty("AutobanL2WalkerAcc", "False"));
               
                ACTIVATE_POSITION_RECORDER      = Boolean.valueOf(optionsSettings.getProperty("ActivatePositionRecorder", "False"));
            
                DEFAULT_GLOBAL_CHAT             = optionsSettings.getProperty("GlobalChat", "ON");
                DEFAULT_TRADE_CHAT              = optionsSettings.getProperty("TradeChat", "ON");
            
                LOG_CHAT                        = Boolean.valueOf(optionsSettings.getProperty("LogChat", "false"));
                LOG_ITEMS                       = Boolean.valueOf(optionsSettings.getProperty("LogItems", "false"));
                             
                GMAUDIT                         = Boolean.valueOf(optionsSettings.getProperty("GMAudit", "False"));

                COMMUNITY_TYPE                  = optionsSettings.getProperty("CommunityType", "old");
                BBS_DEFAULT                     = optionsSettings.getProperty("BBSDefault", "_bbshome");
                SHOW_LEVEL_COMMUNITYBOARD       = Boolean.valueOf(optionsSettings.getProperty("ShowLevelOnCommunityBoard", "False"));
                SHOW_STATUS_COMMUNITYBOARD      = Boolean.valueOf(optionsSettings.getProperty("ShowStatusOnCommunityBoard", "True"));
                NAME_PAGE_SIZE_COMMUNITYBOARD   = Integer.parseInt(optionsSettings.getProperty("NamePageSizeOnCommunityBoard", "50"));
                NAME_PER_ROW_COMMUNITYBOARD     = Integer.parseInt(optionsSettings.getProperty("NamePerRowOnCommunityBoard", "5"));
                             
                ZONE_TOWN                       = Integer.parseInt(optionsSettings.getProperty("ZoneTown", "0"));
                             
                MAX_DRIFT_RANGE                 = Integer.parseInt(optionsSettings.getProperty("MaxDriftRange", "300"));

                MIN_NPC_ANIMATION               = Integer.parseInt(optionsSettings.getProperty("MinNPCAnimation", "0"));
                MAX_NPC_ANIMATION               = Integer.parseInt(optionsSettings.getProperty("MaxNPCAnimation", "0"));
                             
                SHOW_NPC_LVL                    = Boolean.valueOf(optionsSettings.getProperty("ShowNpcLevel", "False"));

                FORCE_INVENTORY_UPDATE          = Boolean.valueOf(optionsSettings.getProperty("ForceInventoryUpdate", "False"));

                AUTODELETE_INVALID_QUEST_DATA   = Boolean.valueOf(optionsSettings.getProperty("AutoDeleteInvalidQuestData", "False"));
                             
                DAY_STATUS_SUN_RISE_AT          = Integer.parseInt(optionsSettings.getProperty("DayStatusSunRiseAt", "6"));
                DAY_STATUS_SUN_SET_AT           = Integer.parseInt(optionsSettings.getProperty("DayStatusSunSetAt", "18"));
                DAY_STATUS_FORCE_CLIENT_UPDATE  = Boolean.valueOf(optionsSettings.getProperty("DayStatusForceClientUpdate", "True"));

                THREAD_P_EFFECTS                = Integer.parseInt(optionsSettings.getProperty("ThreadPoolSizeEffects", "6"));
                THREAD_P_GENERAL                = Integer.parseInt(optionsSettings.getProperty("ThreadPoolSizeGeneral", "15"));
                GENERAL_PACKET_THREAD_CORE_SIZE = Integer.parseInt(optionsSettings.getProperty("GeneralPacketThreadCoreSize", "4"));
                URGENT_PACKET_THREAD_CORE_SIZE  =Integer.parseInt(optionsSettings.getProperty("UrgentPacketThreadCoreSize", "2"));
                GENERAL_THREAD_CORE_SIZE        = Integer.parseInt(optionsSettings.getProperty("GeneralThreadCoreSize", "4"));
                AI_MAX_THREAD                   = Integer.parseInt(optionsSettings.getProperty("AiMaxThread", "10"));
                             
                DELETE_DAYS                     = Integer.parseInt(optionsSettings.getProperty("DeleteCharAfterDays", "7"));
                             
                DEFAULT_PUNISH                  = Integer.parseInt(optionsSettings.getProperty("DefaultPunish", "2"));
                DEFAULT_PUNISH_PARAM            = Integer.parseInt(optionsSettings.getProperty("DefaultPunishParam", "0"));

                LAZY_CACHE                      = Boolean.valueOf(optionsSettings.getProperty("LazyCache", "False"));

                PACKET_LIFETIME                 = Integer.parseInt(optionsSettings.getProperty("PacketLifeTime", "0"));
                             
                BYPASS_VALIDATION               = Boolean.valueOf(optionsSettings.getProperty("BypassValidation", "False"));
                             
                GAMEGUARD_ENFORCE               = Boolean.valueOf(optionsSettings.getProperty("GameGuardEnforce", "False"));
                GAMEGUARD_PROHIBITACTION        = Boolean.valueOf(optionsSettings.getProperty("GameGuardProhibitAction", "False"));
                GRIDS_ALWAYS_ON                 = Boolean.parseBoolean(optionsSettings.getProperty("GridsAlwaysOn", "False"));
                GRID_NEIGHBOR_TURNON_TIME       = Integer.parseInt(optionsSettings.getProperty("GridNeighborTurnOnTime", "30"));
                GRID_NEIGHBOR_TURNOFF_TIME      = Integer.parseInt(optionsSettings.getProperty("GridNeighborTurnOffTime", "300"));    
                                 
                
                SHOW_L2J_LICENSE                = Boolean.parseBoolean(optionsSettings.getProperty("ShowL2JLicense", "false"));
                SHOW_HTML_WELCOME               = Boolean.parseBoolean(optionsSettings.getProperty("ShowHTMLWelcome", "false"));
                USE_SAY_FILTER                  = Boolean.parseBoolean(optionsSettings.getProperty("UseSayFilter", "false"));
                if(USE_SAY_FILTER){
                    try{
                    LineNumberReader lnr = null;
                    File say_filter = new File(SAY_FILTER_FILE);
                    lnr = new LineNumberReader(new BufferedReader(new FileReader(say_filter)));
                    String line = null;
                    while ((line = lnr.readLine()) != null){
                        if (line.trim().length() == 0 || line.startsWith("#"))
                        {
                            continue;
                        }
                        FILTER_LIST.add(line);
                    }
                    _log.info("Say Filter: Loaded " + FILTER_LIST.size() + " words");
                    }catch (FileNotFoundException e)
                    {
                        _log.warn("sayfilter.txt is missing in config folder");
                    }
                    catch (Exception e)
                    {
                        _log.warn("error loading say filter: " + e);
                    }
                }
                ONLINE_PLAYERS_AT_STARTUP = Boolean.parseBoolean(optionsSettings.getProperty("ShowOnlinePlayersAtStartup","True"));
                ONLINE_PLAYERS_ANNOUNCE_INTERVAL = Integer.parseInt(optionsSettings.getProperty("OnlinePlayersAnnounceInterval","900000"));
                
                // ---------------------------------------------------
                // Configuration values not found in config files
                // ---------------------------------------------------
                
                USE_3D_MAP                      = Boolean.valueOf(optionsSettings.getProperty("Use3DMap", "False"));

                PATH_NODE_RADIUS                = Integer.parseInt(optionsSettings.getProperty("PathNodeRadius", "50"));
                NEW_NODE_ID                     = Integer.parseInt(optionsSettings.getProperty("NewNodeId", "7952"));
                SELECTED_NODE_ID                = Integer.parseInt(optionsSettings.getProperty("NewNodeId", "7952"));
                LINKED_NODE_ID                  = Integer.parseInt(optionsSettings.getProperty("NewNodeId", "7952"));
                NEW_NODE_TYPE                   = optionsSettings.getProperty("NewNodeType", "npc");

                COUNT_PACKETS                   = Boolean.valueOf(optionsSettings.getProperty("CountPacket", "false"));  
                DUMP_PACKET_COUNTS              = Boolean.valueOf(optionsSettings.getProperty("DumpPacketCounts", "false"));
                DUMP_INTERVAL_SECONDS           = Integer.parseInt(optionsSettings.getProperty("PacketDumpInterval", "60"));
                
                MINIMUM_UPDATE_DISTANCE         = Integer.parseInt(optionsSettings.getProperty("MaximumUpdateDistance", "50"));
                MINIMUN_UPDATE_TIME             = Integer.parseInt(optionsSettings.getProperty("MinimumUpdateTime", "500"));
                CHECK_KNOWN                     = Boolean.valueOf(optionsSettings.getProperty("CheckKnownList", "false"));
                KNOWNLIST_FORGET_DELAY          = Integer.parseInt(optionsSettings.getProperty("KnownListForgetDelay", "10000"));
            }
            catch (Exception e)
            {
                _log.error(e.getMessage(),e);
                throw new Error("Failed to Load "+OPTIONS_FILE+" File.");
            }
	        /*
	         * Load L2J Version Properties file (if exists)
	         */
	        try
	        {
	            Properties serverVersion    = new Properties();
	            InputStream is              = new FileInputStream(new File(VERSION_FILE));  
	            serverVersion.load(is);
	            is.close();
	            
	            SERVER_VERSION      = serverVersion.getProperty("version", "Unsupported Custom Version.");
	            SERVER_BUILD_DATE   = serverVersion.getProperty("builddate", "Undefined Date.");
	        }
	        catch (Exception e)
	        {
	            //Ignore Properties file if it doesnt exist
	            SERVER_VERSION      = "Unsupported Custom Version.";
	            SERVER_BUILD_DATE   = "Undefined Date.";
	        }
	        
	        // telnet
	        try
	        {
	            Properties telnetSettings   = new Properties();
	            InputStream is              = new FileInputStream(new File(TELNET_FILE));  
	            telnetSettings.load(is);
	            is.close();
	            
	            IS_TELNET_ENABLED   = Boolean.valueOf(telnetSettings.getProperty("EnableTelnet", "false"));
	        }
	        catch (Exception e)
	        {
                _log.error(e);
	            throw new Error("Failed to Load "+TELNET_FILE+" File.");
	        }
	        
	        // id factory
	        try
	        {
	            Properties idSettings   = new Properties();
	            InputStream is          = new FileInputStream(new File(ID_CONFIG_FILE));
	            idSettings.load(is);
	            is.close();
	            
	            MAP_TYPE        = ObjectMapType.valueOf(idSettings.getProperty("L2Map", "WorldObjectMap"));
	            SET_TYPE        = ObjectSetType.valueOf(idSettings.getProperty("L2Set", "WorldObjectSet"));
	            IDFACTORY_TYPE  = IdFactoryType.valueOf(idSettings.getProperty("IDFactory", "Compaction"));
	            BAD_ID_CHECKING = Boolean.valueOf(idSettings.getProperty("BadIdChecking", "True"));
	        }
	        catch (Exception e)
	        {
                _log.error(e);
	            throw new Error("Failed to Load "+ID_CONFIG_FILE+" File.");
	        }
	        
	        // other
	        try
	        {
	            Properties otherSettings    = new Properties();
	            InputStream is              = new FileInputStream(new File(OTHER_CONFIG_FILE));
	            otherSettings.load(is);
	            is.close();
	            
	            DEEPBLUE_DROP_RULES = Boolean.parseBoolean(otherSettings.getProperty("UseDeepBlueDropRules", "True"));
	            ALLOW_GUARDS        = Boolean.valueOf(otherSettings.getProperty("AllowGuards", "False"));
	            EFFECT_CANCELING    = Boolean.valueOf(otherSettings.getProperty("CancelLesserEffect", "True"));
	            WYVERN_SPEED        = Integer.parseInt(otherSettings.getProperty("WyvernSpeed", "100"));         
	            STRIDER_SPEED       = Integer.parseInt(otherSettings.getProperty("StriderSpeed", "80"));
	            ALLOW_WYVERN_UPGRADER     = Boolean.valueOf(otherSettings.getProperty("AllowWyvernUpgrader", "False"));
	            
	            /* Inventory slots limits */
                INVENTORY_MAXIMUM_NO_DWARF  = Integer.parseInt(otherSettings.getProperty("MaximumSlotsForNoDwarf", "80"));
                INVENTORY_MAXIMUM_DWARF  = Integer.parseInt(otherSettings.getProperty("MaximumSlotsForDwarf", "100"));
	            INVENTORY_MAXIMUM_GM    = Integer.parseInt(otherSettings.getProperty("MaximumSlotsForGMPlayer", "250"));
              
                /* Config weight limit */
                ADD_MAX_LOAD = Integer.parseInt(otherSettings.getProperty("AddWeightLimit", "0"));
                
                /* Inventory slots limits */
                WAREHOUSE_SLOTS_NO_DWARF = Integer.parseInt(otherSettings.getProperty("MaximumWarehouseSlotsForNoDwarf", "100"));
                WAREHOUSE_SLOTS_DWARF = Integer.parseInt(otherSettings.getProperty("MaximumWarehouseSlotsForDwarf", "120"));
                WAREHOUSE_SLOTS_CLAN = Integer.parseInt(otherSettings.getProperty("MaximumWarehouseSlotsForClan", "150"));
                FREIGHT_SLOTS       = Integer.parseInt(otherSettings.getProperty("MaximumFreightSlots", "20"));
	            
                /* if different from 100 (ie 100%) heal rate is modified acordingly */
	            HP_REGEN_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("HpRegenMultiplier", "100"));
	            MP_REGEN_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("MpRegenMultiplier", "100"));
	            PLAYER_HP_REGEN_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("PlayerHpRegenMultiplier", "100"));
	            PLAYER_MP_REGEN_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("PlayerMpRegenMultiplier", "100"));
	            PLAYER_CP_REGEN_MULTIPLIER = Double.parseDouble(otherSettings.getProperty("PlayerCpRegenMultiplier", "100"));

                RAID_HP_REGEN_MULTIPLIER  = Double.parseDouble(otherSettings.getProperty("RaidHpRegenMultiplier", "500"));    
                RAID_MP_REGEN_MULTIPLIER  = Double.parseDouble(otherSettings.getProperty("RaidMpRegenMultiplier", "500"));    
                RAID_DEFENCE_MULTIPLIER  = Double.parseDouble(otherSettings.getProperty("RaidDefenceMultiplier", "500")) /100;    
	            
	            STARTING_ADENA      = Integer.parseInt(otherSettings.getProperty("StartingAdena", "100"));
	            UNSTUCK_INTERVAL    = Integer.parseInt(otherSettings.getProperty("UnstuckInterval", "300"));

                /* Player protection after teleport or login */
                PLAYER_SPAWN_PROTECTION = Integer.parseInt(otherSettings.getProperty("PlayerSpawnProtection", "0"));
	            
	            /* Defines some Party XP related values */
	            PARTY_XP_CUTOFF_METHOD  = otherSettings.getProperty("PartyXpCutoffMethod", "percentage");
	            PARTY_XP_CUTOFF_PERCENT = Double.parseDouble(otherSettings.getProperty("PartyXpCutoffPercent", "3."));
                PARTY_RANGE             = Integer.parseInt(otherSettings.getProperty("PartyRange", "1000"));
	            PARTY_XP_CUTOFF_LEVEL   = Integer.parseInt(otherSettings.getProperty("PartyXpCutoffLevel", "30"));
	            
	            /* Amount of HP, MP, and CP is restored */
	            RESPAWN_RESTORE_CP = Double.parseDouble(otherSettings.getProperty("RespawnRestoreCP", "0")) / 100;
	            RESPAWN_RESTORE_HP = Double.parseDouble(otherSettings.getProperty("RespawnRestoreHP", "70")) / 100;
	            RESPAWN_RESTORE_MP = Double.parseDouble(otherSettings.getProperty("RespawnRestoreMP", "70")) / 100;
	            
	         	RESPAWN_RANDOM_ENABLED = Boolean.parseBoolean(otherSettings.getProperty("RespawnRandomInTown", "False")); 
	         	RESPAWN_RANDOM_MAX_OFFSET = Integer.parseInt(otherSettings.getProperty("RespawnRandomMaxOffset", "50")); 
	            
	            /* Maximum number of available slots for pvt stores */
	            MAX_PVTSTORE_SLOTS_DWARF = Integer.parseInt(otherSettings.getProperty("MaxPvtStoreSlotsDwarf", "5"));
	            MAX_PVTSTORE_SLOTS_OTHER = Integer.parseInt(otherSettings.getProperty("MaxPvtStoreSlotsOther", "4"));
	            
	            STORE_SKILL_COOLTIME = Boolean.parseBoolean(otherSettings.getProperty("StoreSkillCooltime", "true"));
                
	            PET_RENT_NPC =  otherSettings.getProperty("ListPetRentNpc", "30827");
	            LIST_PET_RENT_NPC = new FastList<Integer>();
	            for (String id : PET_RENT_NPC.split(",")) {
	                LIST_PET_RENT_NPC.add(Integer.parseInt(id));
	            }
	            NONDROPPABLE_ITEMS        = otherSettings.getProperty("ListOfNonDroppableItems", "1147,425,1146,461,10,2368,7,6,2370,2369,5598");
	            
	            LIST_NONDROPPABLE_ITEMS = new FastList<Integer>();
	            for (String id : NONDROPPABLE_ITEMS.split(",")) {
	                LIST_NONDROPPABLE_ITEMS.add(Integer.parseInt(id));
	            }
                
	            ANNOUNCE_MAMMON_SPAWN = Boolean.parseBoolean(otherSettings.getProperty("AnnounceMammonSpawn", "True"));
                
                ALT_PRIVILEGES_ADMIN = Boolean.parseBoolean(otherSettings.getProperty("AltPrivilegesAdmin", "False"));
                ALT_PRIVILEGES_SECURE_CHECK = Boolean.parseBoolean(otherSettings.getProperty("AltPrivilegesSecureCheck", "True"));
                ALT_PRIVILEGES_DEFAULT_LEVEL = Integer.parseInt(otherSettings.getProperty("AltPrivilegesDefaultLevel", "100"));

                PETITIONING_ALLOWED = Boolean.parseBoolean(otherSettings.getProperty("PetitioningAllowed", "True"));
                MAX_PETITIONS_PER_PLAYER = Integer.parseInt(otherSettings.getProperty("MaxPetitionsPerPlayer", "5"));
                MAX_PETITIONS_PENDING = Integer.parseInt(otherSettings.getProperty("MaxPetitionsPending", "25"));                
                
                JAIL_IS_PVP       = Boolean.valueOf(otherSettings.getProperty("JailIsPvp", "True"));
                JAIL_DISABLE_CHAT = Boolean.valueOf(otherSettings.getProperty("JailDisableChat", "True"));
                
                GM_NAME_COLOUR_ENABLED = Boolean.parseBoolean(otherSettings.getProperty("GMNameColourEnabled", "False"));
                GM_NAME_COLOUR = Integer.decode("0x" + otherSettings.getProperty("GMNameColour", "00FF00"));
                
	        }
	        catch (Exception e)
	        {
                _log.error(e);
	            throw new Error("Failed to Load "+OTHER_CONFIG_FILE+" File.");
	        }
	        
	        // rates
	        try
	        {
                Properties ratesSettings    = new Properties();
                InputStream is              = new FileInputStream(new File(RATES_CONFIG_FILE));
                ratesSettings.load(is);
	            is.close();
	            
                RATE_XP                         = Float.parseFloat(ratesSettings.getProperty("RateXp", "1."));
                RATE_SP                         = Float.parseFloat(ratesSettings.getProperty("RateSp", "1."));
                RATE_PARTY_XP                   = Float.parseFloat(ratesSettings.getProperty("RatePartyXp", "1."));
                RATE_PARTY_SP                   = Float.parseFloat(ratesSettings.getProperty("RatePartySp", "1."));
                RATE_QUESTS_REWARD              = Float.parseFloat(ratesSettings.getProperty("RateQuestsReward", "1."));
                RATE_DROP_ADENA                 = Float.parseFloat(ratesSettings.getProperty("RateDropAdena", "1."));
                RATE_CONSUMABLE_COST            = Float.parseFloat(ratesSettings.getProperty("RateConsumableCost", "1."));
                RATE_CRAFT_COST                 = Float.parseFloat(ratesSettings.getProperty("RateCraftCost","1."));
                RATE_DROP_ITEMS                 = Float.parseFloat(ratesSettings.getProperty("RateDropItems", "1."));
                RATE_DROP_SPOIL                 = Float.parseFloat(ratesSettings.getProperty("RateDropSpoil", "1."));
                RATE_BOX_SPAWN                  = Integer.parseInt(ratesSettings.getProperty("RateBoxSpawn","20"));
                RATE_DROP_QUEST                 = Float.parseFloat(ratesSettings.getProperty("RateDropQuest", "1."));
                RATE_KARMA_EXP_LOST             = Float.parseFloat(ratesSettings.getProperty("RateKarmaExpLost", "1."));    
                RATE_SIEGE_GUARDS_PRICE         = Float.parseFloat(ratesSettings.getProperty("RateSiegeGuardsPrice", "1."));

                RATE_DROP_COMMON_HERBS          = Float.parseFloat(ratesSettings.getProperty("RateCommonHerbs", "15."));
                RATE_DROP_MP_HP_HERBS           = Float.parseFloat(ratesSettings.getProperty("RateHpMpHerbs", "10."));                
                RATE_DROP_GREATER_HERBS         = Float.parseFloat(ratesSettings.getProperty("RateGreaterHerbs", "4."));
                RATE_DROP_SUPERIOR_HERBS        = Float.parseFloat(ratesSettings.getProperty("RateSuperiorHerbs", "0.8"))*10;                
                RATE_DROP_SPECIAL_HERBS         = Float.parseFloat(ratesSettings.getProperty("RateSpecialHerbs", "0.2"))*10;
                
                PLAYER_DROP_LIMIT               = Integer.parseInt(ratesSettings.getProperty("PlayerDropLimit", "3"));
                PLAYER_RATE_DROP                = Integer.parseInt(ratesSettings.getProperty("PlayerRateDrop", "5"));
                PLAYER_RATE_DROP_ITEM           = Integer.parseInt(ratesSettings.getProperty("PlayerRateDropItem", "70"));
                PLAYER_RATE_DROP_EQUIP          = Integer.parseInt(ratesSettings.getProperty("PlayerRateDropEquip", "25"));
                PLAYER_RATE_DROP_EQUIP_WEAPON   = Integer.parseInt(ratesSettings.getProperty("PlayerRateDropEquipWeapon", "5"));

                PET_XP_RATE                     = Float.parseFloat(ratesSettings.getProperty("PetXpRate", "1."));
                PET_FOOD_RATE                   = Integer.parseInt(ratesSettings.getProperty("PetFoodRate", "1")); 

                KARMA_DROP_LIMIT                = Integer.parseInt(ratesSettings.getProperty("KarmaDropLimit", "10"));
                KARMA_RATE_DROP                 = Integer.parseInt(ratesSettings.getProperty("KarmaRateDrop", "70"));
                KARMA_RATE_DROP_ITEM            = Integer.parseInt(ratesSettings.getProperty("KarmaRateDropItem", "50"));
                KARMA_RATE_DROP_EQUIP           = Integer.parseInt(ratesSettings.getProperty("KarmaRateDropEquip", "40"));
                KARMA_RATE_DROP_EQUIP_WEAPON    = Integer.parseInt(ratesSettings.getProperty("KarmaRateDropEquipWeapon", "10"));
               
               CAN_SPOIL_LOWER_LEVEL_MOBS              = Boolean.parseBoolean(ratesSettings.getProperty("CanSpoilLowerLevelMobs", "false"));
               CAN_DELEVEL_AND_SPOIL_MOBS              = Boolean.parseBoolean(ratesSettings.getProperty("CanDelevelToSpoil", "true"));                       
               MAXIMUM_PLAYER_AND_MOB_LEVEL_DIFFERENCE = Float.parseFloat(ratesSettings.getProperty("MaximumPlayerAndMobLevelDifference", "9."));
               BASE_SPOIL_RATE                         = Float.parseFloat(ratesSettings.getProperty("BasePercentChanceOfSpoilSuccess", "40."));
               MINIMUM_SPOIL_RATE                      = Float.parseFloat(ratesSettings.getProperty("MinimumPercentChanceOfSpoilSuccess", "3."));
               SPOIL_LEVEL_DIFFERENCE_LIMIT            = Float.parseFloat(ratesSettings.getProperty("SpoilLevelDifferenceLimit", "5."));
               SPOIL_LEVEL_DIFFERENCE_MULTIPLIER       = Float.parseFloat(ratesSettings.getProperty("SpoilLevelMultiplier", "7."));
               LAST_LEVEL_SPOIL_IS_LEARNED             = Integer.parseInt(ratesSettings.getProperty("LastLevelSpoilIsLearned", "72"));
	        }
	        catch (Exception e) {
                _log.error(e);
                throw new Error("Failed to Load "+RATES_CONFIG_FILE+" File.");
	        }
            
            // enchants
            try
            {
                Properties enchantSettings  = new Properties();
                InputStream is              = new FileInputStream(new File(ENCHANT_CONFIG_FILE));
                enchantSettings.load(is);
                is.close();
                
                /* chance to enchant an item normal scroll*/
                ENCHANT_CHANCE_WEAPON  = Integer.parseInt(enchantSettings.getProperty("EnchantChanceWeapon", "65"));
                ENCHANT_CHANCE_ARMOR  = Integer.parseInt(enchantSettings.getProperty("EnchantChanceArmor", "65"));
                ENCHANT_CHANCE_JEWELRY  = Integer.parseInt(enchantSettings.getProperty("EnchantChanceJewelry", "65"));
                /* item may break normal scroll*/
                ENCHANT_BREAK_WEAPON  = Boolean.parseBoolean(enchantSettings.getProperty("EnchantBreakWeapon", "True"));
                ENCHANT_BREAK_ARMOR  = Boolean.parseBoolean(enchantSettings.getProperty("EnchantBreakArmor", "True"));
                ENCHANT_BREAK_JEWELRY  = Boolean.parseBoolean(enchantSettings.getProperty("EnchantBreakJewelry", "True"));
                /* chance to enchant an item crystal scroll */
                ENCHANT_CHANCE_WEAPON_CRYSTAL  = Integer.parseInt(enchantSettings.getProperty("EnchantChanceWeaponCrystal", "75"));
                ENCHANT_CHANCE_ARMOR_CRYSTAL  = Integer.parseInt(enchantSettings.getProperty("EnchantChanceArmorCrystal", "75"));
                ENCHANT_CHANCE_JEWELRY_CRYSTAL  = Integer.parseInt(enchantSettings.getProperty("EnchantChanceJewelryCrystal", "75"));
                /* item may break crystal scroll */
                ENCHANT_BREAK_WEAPON_CRYSTAL  = Boolean.parseBoolean(enchantSettings.getProperty("EnchantBreakWeaponCrystal", "True"));
                ENCHANT_BREAK_ARMOR_CRYSTAL  = Boolean.parseBoolean(enchantSettings.getProperty("EnchantBreakArmorCrystal", "True"));
                ENCHANT_BREAK_JEWELRY_CRYSTAL  = Boolean.parseBoolean(enchantSettings.getProperty("EnchantBreakJewelryCrystal", "True"));
                /* chance to enchant an item blessed scroll */
                ENCHANT_CHANCE_WEAPON_BLESSED  = Integer.parseInt(enchantSettings.getProperty("EnchantChanceWeaponBlessed", "65"));
                ENCHANT_CHANCE_ARMOR_BLESSED  = Integer.parseInt(enchantSettings.getProperty("EnchantChanceArmorBlessed", "65"));
                ENCHANT_CHANCE_JEWELRY_BLESSED  = Integer.parseInt(enchantSettings.getProperty("EnchantChanceJewelryBlessed", "65"));
                /* item may break blessed scroll */
                ENCHANT_BREAK_WEAPON_BLESSED  = Boolean.parseBoolean(enchantSettings.getProperty("EnchantBreakWeaponBlessed", "False"));
                ENCHANT_BREAK_ARMOR_BLESSED  = Boolean.parseBoolean(enchantSettings.getProperty("EnchantBreakArmorBlessed", "False"));
                ENCHANT_BREAK_JEWELRY_BLESSED  = Boolean.parseBoolean(enchantSettings.getProperty("EnchantBreakJewelryBlessed", "True"));
                /* enchat hero weapons? */
                ENCHANT_HERO_WEAPONS  = Boolean.parseBoolean(enchantSettings.getProperty("EnchantHeroWeapons", "False"));
                /* enchant dwarf system */
                ENCHANT_DWARF_SYSTEM  = Boolean.parseBoolean(enchantSettings.getProperty("EnchantDwarfSystem", "False"));
                /* limit on enchant */
                ENCHANT_MAX_WEAPON = Integer.parseInt(enchantSettings.getProperty("EnchantMaxWeapon", "255"));
                ENCHANT_MAX_ARMOR = Integer.parseInt(enchantSettings.getProperty("EnchantMaxArmor", "255"));
                ENCHANT_MAX_JEWELRY = Integer.parseInt(enchantSettings.getProperty("EnchantMaxJewelry", "255"));
                /* limit of safe enchant */
                ENCHANT_SAFE_MAX = Integer.parseInt(enchantSettings.getProperty("EnchantSafeMax", "3"));
                ENCHANT_SAFE_MAX_FULL = Integer.parseInt(enchantSettings.getProperty("EnchantSafeMaxFull", "4"));   
            }
            catch (Exception e) {
                e.printStackTrace();
                throw new Error("Failed to Load "+ENCHANT_CONFIG_FILE+" File.");
            }
	        
	        // alternative settings
	        try
	        {
	            Properties altSettings  = new Properties();
	            InputStream is          = new FileInputStream(new File(ALT_SETTINGS_FILE));  
	            altSettings.load(is);
	            is.close();
	            
	            ALT_GAME_TIREDNESS      = Boolean.parseBoolean(altSettings.getProperty("AltGameTiredness", "false"));
	            ALT_GAME_CREATION       = Boolean.parseBoolean(altSettings.getProperty("AltGameCreation", "false"));
	            ALT_GAME_CREATION_SPEED = Double.parseDouble(altSettings.getProperty("AltGameCreationSpeed", "1"));
	            ALT_GAME_CREATION_XP_RATE=Double.parseDouble(altSettings.getProperty("AltGameCreationRateXp", "1"));
                ALT_GAME_CREATION_SP_RATE=Double.parseDouble(altSettings.getProperty("AltGameCreationRateSp", "1"));
                ALT_GAME_NUMBER_OF_CUMULATED_BUFF= Integer.parseInt(altSettings.getProperty("AltNbCumulatedBuff", "24"));
	            ALT_GAME_SKILL_LEARN    = Boolean.parseBoolean(altSettings.getProperty("AltGameSkillLearn", "false"));
	            ALT_GAME_CANCEL_BOW     = altSettings.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("bow") || altSettings.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("all");
	            ALT_GAME_CANCEL_CAST    = altSettings.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("cast") || altSettings.getProperty("AltGameCancelByHit", "Cast").equalsIgnoreCase("all");
	            ALT_GAME_SHIELD_BLOCKS  = Boolean.parseBoolean(altSettings.getProperty("AltShieldBlocks", "false"));
                ALT_PERFECT_SHLD_BLOCK  = Integer.parseInt(altSettings.getProperty("AltPerfectShieldBlockRate", "10"));
                ALT_GAME_DELEVEL        = Boolean.parseBoolean(altSettings.getProperty("Delevel", "true"));
	            ALT_GAME_MAGICFAILURES  = Boolean.parseBoolean(altSettings.getProperty("MagicFailures", "false"));
	            ALT_GAME_MOB_ATTACK_AI  = Boolean.parseBoolean(altSettings.getProperty("AltGameMobAttackAI", "false"));
	            ALT_GAME_SKILL_FORMULAS = altSettings.getProperty("AltGameSkillFormulas", "none");
                ALT_INSTANT_KILL_EFFECT_2 = Float.parseFloat(altSettings.getProperty("InstantKillEffect2", "2"));
                ALT_DAGGER_DMG_VS_HEAVY = Float.parseFloat(altSettings.getProperty("DaggerVSHeavy", "2.50"));
                ALT_DAGGER_DMG_VS_ROBE  = Float.parseFloat(altSettings.getProperty("DaggerVSRobe", "2.00"));
                ALT_DAGGER_DMG_VS_LIGHT = Float.parseFloat(altSettings.getProperty("DaggerVSLight", "1.80"));
                ALT_DAGGER_FORMULA      = Boolean.parseBoolean(altSettings.getProperty("AltGameDaggerFormula", "false"));
                ALT_DAGGER_RATE         = Integer.parseInt(altSettings.getProperty("AltCancelRate", "85"));
                ALT_DAGGER_FAIL_RATE    = Integer.parseInt(altSettings.getProperty("AltFailRate", "15"));
                ALT_DAGGER_RATE_BEHIND  = Integer.parseInt(altSettings.getProperty("AltSuccessRateBehind", "20"));
                ALT_DAGGER_RATE_FRONT   = Integer.parseInt(altSettings.getProperty("AltSuccessRateFront", "5"));
                ALT_ATTACK_DELAY        = Float.parseFloat(altSettings.getProperty("AltAttackDelay", "1.00"));
                ALT_GAME_EXPONENT_XP    = Float.parseFloat(altSettings.getProperty("AltGameExponentXp", "0."));
	            ALT_GAME_EXPONENT_SP    = Float.parseFloat(altSettings.getProperty("AltGameExponentSp", "0."));
	            SPAWN_CLASS_MASTER      = Boolean.valueOf(altSettings.getProperty("SpawnClassMaster", "False"));
                ALLOW_CLASS_MASTER_1    = Boolean.valueOf(altSettings.getProperty("AllowClassMaster1", "False"));
                ALLOW_CLASS_MASTER_2    = Boolean.valueOf(altSettings.getProperty("AllowClassMaster2", "False"));
                ALLOW_CLASS_MASTER_3    = Boolean.valueOf(altSettings.getProperty("AllowClassMaster3", "False"));
	            ALT_GAME_FREIGHTS       = Boolean.parseBoolean(altSettings.getProperty("AltGameFreights", "false"));
	            ALT_GAME_FREIGHT_PRICE  = Integer.parseInt(altSettings.getProperty("AltGameFreightPrice", "1000"));
	            ALT_GAME_SKILL_HIT_RATE = Float.parseFloat(altSettings.getProperty("AltGameSkillHitRate", "1."));
                CHANCE_BREAK            = Integer.parseInt(altSettings.getProperty("ChanceToBreak", "10"));
                CHANCE_LEVEL            = Integer.parseInt(altSettings.getProperty("ChanceToLevel", "32"));
                CHAMPION_FREQUENCY      = Integer.parseInt(altSettings.getProperty("ChampionFrequency", "0"));
                CHAMPION_HP             = Integer.parseInt(altSettings.getProperty("ChampionHp", "7"));
                CHAMPION_REWARDS        = Integer.parseInt(altSettings.getProperty("ChampionRewards", "8"));
	            ENABLE_RATE_HP          = Boolean.parseBoolean(altSettings.getProperty("EnableRateHp", "false"));
	            IS_CRAFTING_ENABLED     = Boolean.parseBoolean(altSettings.getProperty("CraftingEnabled", "true"));
	            SP_BOOK_NEEDED          = Boolean.parseBoolean(altSettings.getProperty("SpBookNeeded", "true"));
	            AUTO_LOOT               = altSettings.getProperty("AutoLoot").equalsIgnoreCase("True");
                AUTO_LOOT_HERBS         = altSettings.getProperty("AutoLootHerbs").equalsIgnoreCase("True");
                ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE    = Boolean.valueOf(altSettings.getProperty("AltKarmaPlayerCanBeKilledInPeaceZone", "false"));
                ALT_GAME_KARMA_PLAYER_CAN_SHOP                      = Boolean.valueOf(altSettings.getProperty("AltKarmaPlayerCanShop", "true"));
                ALT_GAME_KARMA_PLAYER_CAN_USE_GK                    = Boolean.valueOf(altSettings.getProperty("AltKarmaPlayerCanUseGK", "false"));
                ALT_GAME_KARMA_PLAYER_CAN_TELEPORT                  = Boolean.valueOf(altSettings.getProperty("AltKarmaPlayerCanTeleport", "true"));
                ALT_GAME_KARMA_PLAYER_CAN_TRADE                     = Boolean.valueOf(altSettings.getProperty("AltKarmaPlayerCanTrade", "true"));
                ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE             = Boolean.valueOf(altSettings.getProperty("AltKarmaPlayerCanUseWareHouse", "true"));
                CH_TELE_FEE_RATIO                                   = Long.valueOf(altSettings.getProperty("ClanHallTeleportFunctionFeeRation", "86400000"));
                CH_TELE1_FEE                                        = Integer.valueOf(altSettings.getProperty("ClanHallTeleportFunctionFeeLvl1", "86400000"));
                CH_TELE2_FEE                                        = Integer.valueOf(altSettings.getProperty("ClanHallTeleportFunctionFeeLvl2", "86400000"));
                CH_TELE3_FEE                                        = Integer.valueOf(altSettings.getProperty("ClanHallTeleportFunctionFeeLvl3", "86400000"));
                CH_SUPPORT_FEE_RATIO                                = Long.valueOf(altSettings.getProperty("ClanHallSupportFunctionFeeRation", "86400000"));
                CH_SUPPORT1_FEE                                     = Integer.valueOf(altSettings.getProperty("ClanHallSupportFeeLvl1", "86400000"));
                CH_SUPPORT2_FEE                                     = Integer.valueOf(altSettings.getProperty("ClanHallSupportFeeLvl2", "86400000"));
                CH_SUPPORT3_FEE                                     = Integer.valueOf(altSettings.getProperty("ClanHallSupportFeeLvl3", "86400000"));
                CH_SUPPORT4_FEE                                     = Integer.valueOf(altSettings.getProperty("ClanHallSupportFeeLvl4", "86400000"));
                CH_SUPPORT5_FEE                                     = Integer.valueOf(altSettings.getProperty("ClanHallSupportFeeLvl5", "86400000"));
                CH_MPREG_FEE_RATIO                                  = Long.valueOf(altSettings.getProperty("ClanHallMpRegenerationFunctionFeeRation", "86400000"));
                CH_MPREG1_FEE                                       = Integer.valueOf(altSettings.getProperty("ClanHallMpRegenerationFeeLvl1", "86400000"));
                CH_MPREG2_FEE                                       = Integer.valueOf(altSettings.getProperty("ClanHallMpRegenerationFeeLvl2", "86400000"));
                CH_MPREG3_FEE                                       = Integer.valueOf(altSettings.getProperty("ClanHallMpRegenerationFeeLvl3", "86400000"));
                CH_HPREG_FEE_RATIO                                  = Long.valueOf(altSettings.getProperty("ClanHallHpRegenerationFunctionFeeRation", "86400000"));
                CH_HPREG1_FEE                                       = Integer.valueOf(altSettings.getProperty("ClanHallHpRegenerationFeeLvl1", "86400000"));
                CH_HPREG2_FEE                                       = Integer.valueOf(altSettings.getProperty("ClanHallHpRegenerationFeeLvl2", "86400000"));
                CH_HPREG3_FEE                                       = Integer.valueOf(altSettings.getProperty("ClanHallHpRegenerationFeeLvl3", "86400000"));
                CH_EXPREG_FEE_RATIO                                 = Long.valueOf(altSettings.getProperty("ClanHallExpRegenerationFunctionFeeRation", "86400000"));
                CH_EXPREG1_FEE                                      = Integer.valueOf(altSettings.getProperty("ClanHallExpRegenerationFeeLvl1", "86400000"));
                CH_EXPREG2_FEE                                      = Integer.valueOf(altSettings.getProperty("ClanHallExpRegenerationFeeLvl2", "86400000"));
                CH_EXPREG3_FEE                                      = Integer.valueOf(altSettings.getProperty("ClanHallExpRegenerationFeeLvl3", "86400000"));
                CH_ITEM_FEE_RATIO                                   = Long.valueOf(altSettings.getProperty("ClanHallItemCreationFunctionFeeRation", "86400000"));
                CH_ITEM1_FEE                                        = Integer.valueOf(altSettings.getProperty("ClanHallItemCreationFunctionFeeLvl1", "86400000"));
                CH_ITEM2_FEE                                        = Integer.valueOf(altSettings.getProperty("ClanHallItemCreationFunctionFeeLvl2", "86400000"));
                CH_ITEM3_FEE                                        = Integer.valueOf(altSettings.getProperty("ClanHallItemCreationFunctionFeeLvl3", "86400000"));
                ALT_GAME_FREE_TELEPORT                              = Boolean.parseBoolean(altSettings.getProperty("AltFreeTeleporting", "False"));
                ALT_GAME_SUBCLASS_WITHOUT_QUESTS                    = Boolean.parseBoolean(altSettings.getProperty("AltSubClassWithoutQuests", "False"));
                ALT_GAME_VIEWNPC                    				= Boolean.parseBoolean(altSettings.getProperty("AltGameViewNpc", "False"));
                ALT_GAME_NEW_CHAR_ALWAYS_IS_NEWBIE                  = Boolean.parseBoolean(altSettings.getProperty("AltNewCharAlwaysIsNewbie", "False"));
                DWARF_RECIPE_LIMIT                                  = Integer.parseInt(altSettings.getProperty("DwarfRecipeLimit","50"));
                COMMON_RECIPE_LIMIT                                 = Integer.parseInt(altSettings.getProperty("CommonRecipeLimit","50"));
                ALT_CLAN_MEMBERS_FOR_WAR                            = Integer.parseInt(altSettings.getProperty("AltClanMembersForWar", "15"));
                ALT_CLAN_JOIN_DAYS                                  = Integer.parseInt(altSettings.getProperty("DaysBeforeJoinAClan", "5"));
                ALT_CLAN_CREATE_DAYS                                = Integer.parseInt(altSettings.getProperty("DaysBeforeCreateAClan", "10"));                
                
                ALT_STRICT_HERO_SYSTEM                              = Boolean.parseBoolean(altSettings.getProperty("StrictHeroSystem", "True"));

                NONTRADEABLE_ITEMS                                  = altSettings.getProperty("UnTradeableItemList","6834,6835,6836,6837,6838,6839,6840,6841,4425");
                
                LIST_NONTRADEABLE_ITEMS = new FastList<Integer>();
                for (String id : NONTRADEABLE_ITEMS.split(",")) {
                    LIST_NONTRADEABLE_ITEMS.add(Integer.parseInt(id));
                }
                
                ALT_OLY_START_TIME                                  = Integer.parseInt(altSettings.getProperty("AltOlyStartTime", "20"));
                ALT_OLY_MIN                                         = Integer.parseInt(altSettings.getProperty("AltOlyMin","00"));
                ALT_OLY_CPERIOD                                     = Integer.parseInt(altSettings.getProperty("AltOlyPeriod","14100000"));
                ALT_OLY_BATTLE                                      = Integer.parseInt(altSettings.getProperty("AltOlyBattle","180000"));
                ALT_OLY_BWAIT                                       = Integer.parseInt(altSettings.getProperty("AltOlyBWait","600000"));
                ALT_OLY_IWAIT                                       = Integer.parseInt(altSettings.getProperty("AltOlyPwait","300000"));
                ALT_OLY_WPERIOD                                     = Integer.parseInt(altSettings.getProperty("AltOlyWperiod","604800000"));
                ALT_OLY_VPERIOD                                     = Integer.parseInt(altSettings.getProperty("AltOlyVperiod","86400000"));

                ALT_LOTTERY_PRIZE                                   = Integer.parseInt(altSettings.getProperty("AltLotteryPrize","50000"));
                ALT_LOTTERY_TICKET_PRICE                            = Integer.parseInt(altSettings.getProperty("AltLotteryTicketPrice","2000"));
                ALT_LOTTERY_5_NUMBER_RATE                           = Float.parseFloat(altSettings.getProperty("AltLottery5NumberRate","0.6"));
                ALT_LOTTERY_4_NUMBER_RATE                           = Float.parseFloat(altSettings.getProperty("AltLottery4NumberRate","0.2"));
                ALT_LOTTERY_3_NUMBER_RATE                           = Float.parseFloat(altSettings.getProperty("AltLottery3NumberRate","0.2"));
                ALT_LOTTERY_2_AND_1_NUMBER_PRIZE                    = Integer.parseInt(altSettings.getProperty("AltLottery2and1NumberPrize","200"));

                ALT_DEV_NO_QUESTS                                   = Boolean.parseBoolean(altSettings.getProperty("AltDevNoQuests", "False"));
                ALT_DEV_NO_SPAWNS                                   = Boolean.parseBoolean(altSettings.getProperty("AltDevNoSpawns", "False"));

                ALT_STRICT_SEVENSIGNS                               = Boolean.parseBoolean(altSettings.getProperty("StrictSevenSigns", "True"));
                
                CLAN_LEADER_COLOR_ENABLED     			       		= Boolean.parseBoolean(altSettings.getProperty("ClanLeaderColorEnabled", "True"));
                CLAN_LEADER_COLORED                  				= ClanLeaderColored.valueOf(altSettings.getProperty("ClanLeaderColored", "name"));
                CLAN_LEADER_COLOR                                   = Integer.decode("0x" + altSettings.getProperty("ClanLeaderColor", "00FFFF"));
                CLAN_LEADER_COLOR_CLAN_LEVEL                        = Integer.parseInt(altSettings.getProperty("ClanLeaderColorAtClanLevel", "1"));
                ALT_BUFF_TIME                                       = Integer.parseInt(altSettings.getProperty("AltBuffTime", "1"));
                ALT_DANCE_TIME                                      = Integer.parseInt(altSettings.getProperty("AltDanceTime", "1"));
	            SPAWN_SIEGE_GUARD 									= Boolean.parseBoolean(altSettings.getProperty("SpawnSiegeGuard", "true"));
	            AUTO_LEARN_SKILLS 									= Boolean.parseBoolean(altSettings.getProperty("AutoLearnSkills", "false"));
	            MAX_PATK_SPEED 										= Integer.parseInt(altSettings.getProperty("MaxPAtkSpeed", "0"));
                MAX_MATK_SPEED                                      = Integer.parseInt(altSettings.getProperty("MaxMAtkSpeed", "0"));
                ALT_PHYSICAL_DAMAGE_MULTI                           = Float.parseFloat(altSettings.getProperty("AltPDamage", "1.0"));
                ALT_PHYSICAL_DAMAGE_MULTI_NPC                       = Float.parseFloat(altSettings.getProperty("AltPDamageNpc", "1.0"));
                ALT_MAGICAL_DAMAGE_MULTI                            = Float.parseFloat(altSettings.getProperty("AltMDamage", "1.00"));
                ALT_MAGICAL_DAMAGE_MULTI_NPC                        = Float.parseFloat(altSettings.getProperty("AltMDamageNpc", "1.00"));
                ALT_BUFFER_HATE                                     = Integer.parseInt(altSettings.getProperty("BufferHate", "4"));                
	            GRADE_PENALTY										= Boolean.parseBoolean(altSettings.getProperty("GradePenalty", "true"));
                ALT_URN_TEMP_FAIL                                   = Integer.parseInt(altSettings.getProperty("UrnTempFail", "10"));
                ALT_DIFF_CUTOFF                                     = Integer.parseInt(altSettings.getProperty("MobPCExpCutoff", "-10"));
	        }
	        catch (Exception e)
	        {
                _log.error(e);
	            throw new Error("Failed to Load "+ALT_SETTINGS_FILE+" File.");
	        }
            
           // Seven Signs Config
            try
            {
                Properties SevenSettings  = new Properties();
                InputStream is            = new FileInputStream(new File(SEVENSIGNS_FILE));  
                SevenSettings.load(is);
                is.close();
                
                ALT_GAME_REQUIRE_CASTLE_DAWN    = Boolean.parseBoolean(SevenSettings.getProperty("AltRequireCastleForDawn", "False"));
                ALT_GAME_REQUIRE_CLAN_CASTLE    = Boolean.parseBoolean(SevenSettings.getProperty("AltRequireClanCastle", "False"));
                ALT_FESTIVAL_MIN_PLAYER         = Integer.parseInt(SevenSettings.getProperty("AltFestivalMinPlayer", "5"));
                ALT_MAXIMUM_PLAYER_CONTRIB      = Integer.parseInt(SevenSettings.getProperty("AltMaxPlayerContrib", "1000000"));
                ALT_FESTIVAL_MANAGER_START      = Long.parseLong(SevenSettings.getProperty("AltFestivalManagerStart", "120000"));
                ALT_FESTIVAL_LENGTH             = Long.parseLong(SevenSettings.getProperty("AltFestivalLength", "1080000"));
                ALT_FESTIVAL_CYCLE_LENGTH       = Long.parseLong(SevenSettings.getProperty("AltFestivalCycleLength", "2280000"));
                ALT_FESTIVAL_FIRST_SPAWN        = Long.parseLong(SevenSettings.getProperty("AltFestivalFirstSpawn", "120000"));
                ALT_FESTIVAL_FIRST_SWARM        = Long.parseLong(SevenSettings.getProperty("AltFestivalFirstSwarm", "300000"));
                ALT_FESTIVAL_SECOND_SPAWN       = Long.parseLong(SevenSettings.getProperty("AltFestivalSecondSpawn", "540000"));
                ALT_FESTIVAL_SECOND_SWARM       = Long.parseLong(SevenSettings.getProperty("AltFestivalSecondSwarm", "720000"));
                ALT_FESTIVAL_CHEST_SPAWN        = Long.parseLong(SevenSettings.getProperty("AltFestivalChestSpawn", "900000"));
            }
            catch (Exception e)
            {
                _log.error(e);
                throw new Error("Failed to Load "+SEVENSIGNS_FILE+" File.");
            }
	        
	        // pvp config
	        try
	        {
	            Properties pvpSettings      = new Properties();
	            InputStream is              = new FileInputStream(new File(PVP_CONFIG_FILE));  
	            pvpSettings.load(is);
	            is.close();
	            
	            /* KARMA SYSTEM */
	            KARMA_MIN_KARMA     = Integer.parseInt(pvpSettings.getProperty("MinKarma", "240"));
	            KARMA_MAX_KARMA     = Integer.parseInt(pvpSettings.getProperty("MaxKarma", "10000"));
	            KARMA_XP_DIVIDER    = Integer.parseInt(pvpSettings.getProperty("XPDivider", "260"));
	            KARMA_LOST_BASE     = Integer.parseInt(pvpSettings.getProperty("BaseKarmaLost", "0"));
	            
	            KARMA_DROP_GM               = Boolean.parseBoolean(pvpSettings.getProperty("CanGMDropEquipment", "false"));
	            KARMA_AWARD_PK_KILL         = Boolean.parseBoolean(pvpSettings.getProperty("AwardPKKillPVPPoint", "true"));
	            
	            KARMA_PK_LIMIT                      = Integer.parseInt(pvpSettings.getProperty("MinimumPKRequiredToDrop", "5"));
	            
                KARMA_NONDROPPABLE_PET_ITEMS    = pvpSettings.getProperty("ListOfPetItems", "2375,3500,3501,3502,4422,4423,4424,4425,6648,6649,6650");          
	            KARMA_NONDROPPABLE_ITEMS        = pvpSettings.getProperty("ListOfNonDroppableItems", "57,1147,425,1146,461,10,2368,7,6,2370,2369");
	            
	            KARMA_LIST_NONDROPPABLE_PET_ITEMS = new FastList<Integer>();
	            for (String id : KARMA_NONDROPPABLE_PET_ITEMS.split(",")) {
	                KARMA_LIST_NONDROPPABLE_PET_ITEMS.add(Integer.parseInt(id));
	            }
	            
	            KARMA_LIST_NONDROPPABLE_ITEMS = new FastList<Integer>();
	            for (String id : KARMA_NONDROPPABLE_ITEMS.split(",")) {
	                KARMA_LIST_NONDROPPABLE_ITEMS.add(Integer.parseInt(id));
	            }
	            
	            PVP_TIME = Integer.parseInt(pvpSettings.getProperty("PvPTime", "15000"));
                ALT_PLAYER_CAN_DROP_ADENA= Boolean.parseBoolean(pvpSettings.getProperty("PlayerCanDropAdena", "false"));
                PLAYER_RATE_DROP_ADENA  = Integer.parseInt(pvpSettings.getProperty("PlayerRateDropAdena", "1"));                 
	        }
	        catch (Exception e)
	        {
                _log.error(e);
	            throw new Error("Failed to Load "+PVP_CONFIG_FILE+" File.");
	        }
	        
	        // access levels
	        try
	        {
	            Properties gmSettings   = new Properties();
	            InputStream is          = new FileInputStream(new File(GM_ACCESS_FILE));  
	            gmSettings.load(is);
	            is.close();               
	            
	            GM_ACCESSLEVEL  = Integer.parseInt(gmSettings.getProperty("GMAccessLevel", "100"));
	            GM_MIN          = Integer.parseInt(gmSettings.getProperty("GMMinLevel", "100"));
	            GM_ANNOUNCE     = Integer.parseInt(gmSettings.getProperty("GMCanAnnounce", "100"));
	            GM_BAN          = Integer.parseInt(gmSettings.getProperty("GMCanBan", "100"));
	            GM_BAN_CHAT     = Integer.parseInt(gmSettings.getProperty("GMCanBanChat", "100"));
	            GM_CREATE_ITEM  = Integer.parseInt(gmSettings.getProperty("GMCanShop", "100"));
	            GM_DELETE       = Integer.parseInt(gmSettings.getProperty("GMCanDelete", "100"));
	            GM_KICK         = Integer.parseInt(gmSettings.getProperty("GMCanKick", "100"));
	            GM_MENU         = Integer.parseInt(gmSettings.getProperty("GMMenu", "100"));
	            GM_GODMODE      = Integer.parseInt(gmSettings.getProperty("GMGodMode", "100"));
	            GM_CHAR_EDIT    = Integer.parseInt(gmSettings.getProperty("GMCanEditChar", "100"));
	            GM_CHAR_EDIT_OTHER    = Integer.parseInt(gmSettings.getProperty("GMCanEditCharOther", "100"));
	            GM_CHAR_VIEW    = Integer.parseInt(gmSettings.getProperty("GMCanViewChar", "100"));
	            GM_NPC_EDIT     = Integer.parseInt(gmSettings.getProperty("GMCanEditNPC", "100"));
	            GM_NPC_VIEW     = Integer.parseInt(gmSettings.getProperty("GMCanViewNPC", "100"));
	            GM_TELEPORT     = Integer.parseInt(gmSettings.getProperty("GMCanTeleport", "100"));
	            GM_TELEPORT_OTHER     = Integer.parseInt(gmSettings.getProperty("GMCanTeleportOther", "100"));
	            GM_RESTART      = Integer.parseInt(gmSettings.getProperty("GMCanRestart", "100"));
	            GM_MONSTERRACE  = Integer.parseInt(gmSettings.getProperty("GMMonsterRace", "100"));
	            GM_RIDER        = Integer.parseInt(gmSettings.getProperty("GMRider", "100"));
	            GM_ESCAPE       = Integer.parseInt(gmSettings.getProperty("GMFastUnstuck", "100"));
	            GM_FIXED        = Integer.parseInt(gmSettings.getProperty("GMResurectFixed", "100"));
	            GM_CREATE_NODES = Integer.parseInt(gmSettings.getProperty("GMCreateNodes", "100"));
                GM_DOOR         = Integer.parseInt(gmSettings.getProperty("GMDoor", "100"));
	            GM_RES          = Integer.parseInt(gmSettings.getProperty("GMRes", "100"));
	            GM_PEACEATTACK  = Integer.parseInt(gmSettings.getProperty("GMPeaceAttack", "100"));
	            GM_HEAL         = Integer.parseInt(gmSettings.getProperty("GMHeal", "100"));
	            GM_ENCHANT      = Integer.parseInt(gmSettings.getProperty("GMEnchant", "100"));
	            GM_UNBLOCK      = Integer.parseInt(gmSettings.getProperty("GMUnblock", "100"));
                GM_CACHE        = Integer.parseInt(gmSettings.getProperty("GMCache", "100"));
                GM_TALK_BLOCK   = Integer.parseInt(gmSettings.getProperty("GMTalkBlock", "100"));
                GM_TEST         = Integer.parseInt(gmSettings.getProperty("GMTest", "100"));
                GM_STARTUP_AUTO_LIST = Boolean.parseBoolean(gmSettings.getProperty("GMStartupAutoList", "True"));
                GM_HERO_AURA 	= Boolean.parseBoolean(gmSettings.getProperty("GMHeroAura", "True"));
                GM_STARTUP_INVULNERABLE = Boolean.parseBoolean(gmSettings.getProperty("GMStartupInvulnerable", "True"));
                GMAUDIT 		= Boolean.valueOf(gmSettings.getProperty("GMAudit", "False"));
                
                String gmTrans = gmSettings.getProperty("GMDisableTransaction", "False");
                
                if (!gmTrans.equalsIgnoreCase("false"))
                {
                    String[] params = gmTrans.split(",");
                    GM_DISABLE_TRANSACTION = true;
                    GM_TRANSACTION_MIN = Integer.parseInt(params[0]);
                    GM_TRANSACTION_MAX = Integer.parseInt(params[1]);
                }
                else
                {
                    GM_DISABLE_TRANSACTION = false; 
                }
                GM_CAN_GIVE_DAMAGE = Integer.parseInt(gmSettings.getProperty("GMCanGiveDamage", "90"));
                GM_DONT_TAKE_AGGRO = Integer.parseInt(gmSettings.getProperty("GMDontTakeAggro", "90"));
                GM_DONT_TAKE_EXPSP = Integer.parseInt(gmSettings.getProperty("GMDontGiveExpSp", "90"));
                
                GM_NAME_COLOR_ENABLED  = Boolean.parseBoolean(gmSettings.getProperty("GMNameColorEnabled", "True"));
                GM_NAME_COLOR_ENABLED  = Boolean.parseBoolean(gmSettings.getProperty("GMTitleColorEnabled", "True"));
                GM_NAME_COLOR          = Integer.decode("0x" + gmSettings.getProperty("GMNameColor", "00FF00"));
                GM_TITLE_COLOR         = Integer.decode("0x" + gmSettings.getProperty("GMTitleColor", "00FF00"));
                ADMIN_NAME_COLOR       = Integer.decode("0x" + gmSettings.getProperty("AdminNameColor", "00FF00"));
                ADMIN_TITLE_COLOR      = Integer.decode("0x" + gmSettings.getProperty("AdminTitleColor", "00FF00"));
	            SHOW_GM_LOGIN 	       = Boolean.parseBoolean(gmSettings.getProperty("ShowGMLogin", "false"));
	            HIDE_GM_STATUS	       = Boolean.parseBoolean(gmSettings.getProperty("HideGMStatus", "false")); 
                GM_STARTUP_INVISIBLE   = Boolean.parseBoolean(gmSettings.getProperty("GMStartupInvisible", "True"));
                GM_STARTUP_SILENCE     = Boolean.parseBoolean(gmSettings.getProperty("GMStartupSilence", "True"));
                
	        }
	        catch (Exception e)
	        {
                _log.error(e);
	            throw new Error("Failed to Load "+GM_ACCESS_FILE+" File.");
	        }
	        
	        try
	        {
	            Properties Settings   = new Properties();
	            InputStream is          = new FileInputStream(HEXID_FILE);  
	            Settings.load(is);
	            is.close();  
	            HEX_ID = new BigInteger(Settings.getProperty("HexID"), 16).toByteArray();
	        }
	        catch (Exception e)
	        {
	        	_log.warn("Could not load HexID file ("+HEXID_FILE+"). Hopefully login will give us one.");
	        }
   
	        /** Extensions Config */
	        try
	        {
	        	Properties extensionSettings = new Properties();
	        	InputStream is = new FileInputStream(new File(EXTENSION_FILE));

	        	extensionSettings.load(is);
	        	is.close();
	        	TVT_EVEN_TEAMS = extensionSettings.getProperty("TvTEvenTeams", "BALANCE");
	        	CTF_EVEN_TEAMS = extensionSettings.getProperty("CTFEvenTeams", "BALANCE");
	        }
	        catch (Exception e)
	        {
	        	//e.printStackTrace();
	        	//throw new Error("Failed to Load " + EXTENSION_FILE + " File.");
	        }
	}
	
        /**
     * Set a new value to a game parameter from the admin console.
     * @param pName (String) : name of the parameter to change
     * @param pValue (String) : new value of the parameter
     * @return boolean : true if modification has been made
     * @link useAdminCommand
         */
    public static boolean setParameterValue(String pName, String pValue)
    {
        // Server settings
        if (pName.equalsIgnoreCase("RateXp")) RATE_XP = Float.parseFloat(pValue);
        else if (pName.equalsIgnoreCase("RateSp")) RATE_SP = Float.parseFloat(pValue);
        else if (pName.equalsIgnoreCase("RatePartyXp")) RATE_PARTY_XP = Float.parseFloat(pValue);
        else if (pName.equalsIgnoreCase("RatePartySp")) RATE_PARTY_SP = Float.parseFloat(pValue);
        else if (pName.equalsIgnoreCase("RateQuestsReward")) RATE_QUESTS_REWARD = Float.parseFloat(pValue);
        else if (pName.equalsIgnoreCase("RateDropAdena")) RATE_DROP_ADENA = Float.parseFloat(pValue);
        else if (pName.equalsIgnoreCase("RateConsumableCost")) RATE_CONSUMABLE_COST = Float.parseFloat(pValue);
        else if (pName.equalsIgnoreCase("RateDropItems")) RATE_DROP_ITEMS = Float.parseFloat(pValue);
        else if (pName.equalsIgnoreCase("RateDropSpoil")) RATE_DROP_SPOIL = Float.parseFloat(pValue);
        else if (pName.equalsIgnoreCase("RateBoxSpawn")) RATE_BOX_SPAWN = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("RateDropQuest")) RATE_DROP_QUEST = Float.parseFloat(pValue);
        else if (pName.equalsIgnoreCase("RateKarmaExpLost")) RATE_KARMA_EXP_LOST = Float.parseFloat(pValue);
        else if (pName.equalsIgnoreCase("RateSiegeGuardsPrice")) RATE_SIEGE_GUARDS_PRICE = Float.parseFloat(pValue);

        else if (pName.equalsIgnoreCase("PlayerDropLimit")) PLAYER_DROP_LIMIT = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("PlayerRateDrop")) PLAYER_RATE_DROP = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("PlayerRateDropItem")) PLAYER_RATE_DROP_ITEM = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("PlayerRateDropEquip")) PLAYER_RATE_DROP_EQUIP = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("PlayerRateDropEquipWeapon")) PLAYER_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(pValue);

        else if (pName.equalsIgnoreCase("KarmaDropLimit")) KARMA_DROP_LIMIT = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("KarmaRateDrop")) KARMA_RATE_DROP = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("KarmaRateDropItem")) KARMA_RATE_DROP_ITEM = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("KarmaRateDropEquip")) KARMA_RATE_DROP_EQUIP = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("KarmaRateDropEquipWeapon")) KARMA_RATE_DROP_EQUIP_WEAPON = Integer.parseInt(pValue);

        else if (pName.equalsIgnoreCase("AutoDestroyDroppedItemAfter")) AUTODESTROY_ITEM_AFTER = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("SaveDroppedItem")) SAVE_DROPPED_ITEM = Boolean.valueOf(pValue);
        //else if (pName.equalsIgnoreCase("CategoryDropSystem")) CATEGORY_DROP_SYSTEM = CategoryDropSystem.valueOf(pValue);
        else if (pName.equalsIgnoreCase("CoordSynchronize")) COORD_SYNCHRONIZE = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("DeleteCharAfterDays")) DELETE_DAYS = Integer.parseInt(pValue);

        else if (pName.equalsIgnoreCase("ChanceToBreak")) CHANCE_BREAK = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("ChanceToLevel")) CHANCE_LEVEL = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("AllowDiscardItem")) ALLOW_DISCARDITEM = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("ChampionFrequency")) CHAMPION_FREQUENCY = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("ChampionHp")) CHAMPION_HP = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("ChampionRewards")) CHAMPION_REWARDS = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("AllowFreight")) ALLOW_FREIGHT = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AllowWarehouse")) ALLOW_WAREHOUSE = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AllowWear")) ALLOW_WEAR = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("WearDelay")) WEAR_DELAY = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("WearPrice")) WEAR_PRICE = Integer.parseInt(pValue);        
        else if (pName.equalsIgnoreCase("AllowWater")) ALLOW_WATER = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AllowRentPet")) ALLOW_RENTPET = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("CommunityType")) COMMUNITY_TYPE = pValue;
        else if (pName.equalsIgnoreCase("BBSDefault")) BBS_DEFAULT = pValue;        
        else if (pName.equalsIgnoreCase("AllowBoat")) ALLOW_BOAT = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AllowCursedWeapons")) ALLOW_CURSED_WEAPONS = Boolean.valueOf(pValue);

        else if (pName.equalsIgnoreCase("ShowLevelOnCommunityBoard")) SHOW_LEVEL_COMMUNITYBOARD = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("ShowStatusOnCommunityBoard")) SHOW_STATUS_COMMUNITYBOARD = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("NamePageSizeOnCommunityBoard")) NAME_PAGE_SIZE_COMMUNITYBOARD = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("NamePerRowOnCommunityBoard")) NAME_PER_ROW_COMMUNITYBOARD = Integer.parseInt(pValue);

        else if (pName.equalsIgnoreCase("ShowNpcLevel")) SHOW_NPC_LVL = Boolean.valueOf(pValue);
        
        else if (pName.equalsIgnoreCase("ForceInventoryUpdate")) FORCE_INVENTORY_UPDATE = Boolean.valueOf(pValue);

        else if (pName.equalsIgnoreCase("AutoDeleteInvalidQuestData")) AUTODELETE_INVALID_QUEST_DATA = Boolean.valueOf(pValue);

        else if (pName.equalsIgnoreCase("MaximumOnlineUsers")) MAXIMUM_ONLINE_USERS = Integer.parseInt(pValue);

        else if (pName.equalsIgnoreCase("ZoneTown")) ZONE_TOWN = Integer.parseInt(pValue);

        else if (pName.equalsIgnoreCase("DayStatusForceClientUpdate")) DAY_STATUS_FORCE_CLIENT_UPDATE = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("DayStatusSunRiseAt")) DAY_STATUS_SUN_RISE_AT = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("DayStatusSunSetAt")) DAY_STATUS_SUN_SET_AT = Integer.parseInt(pValue);

        else if (pName.equalsIgnoreCase("MaximumUpdateDistance")) MINIMUM_UPDATE_DISTANCE = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("MinimumUpdateTime")) MINIMUN_UPDATE_TIME = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("CheckKnownList")) CHECK_KNOWN = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("KnownListForgetDelay")) KNOWNLIST_FORGET_DELAY = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("ShowGMLogin")) SHOW_GM_LOGIN = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("HideGMStatus")) HIDE_GM_STATUS = Boolean.valueOf(pValue);

        // Other settings
        else if (pName.equalsIgnoreCase("UseDeepBlueDropRules")) DEEPBLUE_DROP_RULES = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AllowGuards")) ALLOW_GUARDS = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("CancelLesserEffect")) EFFECT_CANCELING = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("WyvernSpeed")) WYVERN_SPEED = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("StriderSpeed")) STRIDER_SPEED = Integer.parseInt(pValue);

        else if (pName.equalsIgnoreCase("MaximumSlotsForNoDwarf")) INVENTORY_MAXIMUM_NO_DWARF = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("MaximumSlotsForDwarf")) INVENTORY_MAXIMUM_DWARF = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("MaximumSlotsForGMPlayer")) INVENTORY_MAXIMUM_GM = Integer.parseInt(pValue);

        else if (pName.equalsIgnoreCase("MaximumWarehouseSlotsForNoDwarf")) WAREHOUSE_SLOTS_NO_DWARF = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("MaximumWarehouseSlotsForDwarf")) WAREHOUSE_SLOTS_DWARF = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("MaximumWarehouseSlotsForClan")) WAREHOUSE_SLOTS_CLAN = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("MaximumFreightSlots")) FREIGHT_SLOTS = Integer.parseInt(pValue);

        else if (pName.equalsIgnoreCase("EnchantChanceWeapon")) ENCHANT_CHANCE_WEAPON = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("EnchantChanceArmor")) ENCHANT_CHANCE_ARMOR = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("EnchantBreakWeapon")) ENCHANT_BREAK_WEAPON = Boolean.parseBoolean(pValue);
        else if (pName.equalsIgnoreCase("EnchantBreakArmor")) ENCHANT_BREAK_ARMOR = Boolean.parseBoolean(pValue);
        else if (pName.equalsIgnoreCase("EnchantChanceWeaponCrystal")) ENCHANT_CHANCE_WEAPON_CRYSTAL = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("EnchantChanceArmorCrystal")) ENCHANT_CHANCE_ARMOR_CRYSTAL = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("EnchantBreakWeaponCrystal")) ENCHANT_BREAK_WEAPON_CRYSTAL = Boolean.parseBoolean(pValue);
        else if (pName.equalsIgnoreCase("EnchantBreakArmorCrystal")) ENCHANT_BREAK_ARMOR_CRYSTAL = Boolean.parseBoolean(pValue);
        else if (pName.equalsIgnoreCase("EnchantChanceWeaponBlessed")) ENCHANT_CHANCE_WEAPON_BLESSED = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("EnchantChanceArmorBlessed")) ENCHANT_CHANCE_ARMOR_BLESSED = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("EnchantBreakWeaponBlessed")) ENCHANT_BREAK_WEAPON_BLESSED = Boolean.parseBoolean(pValue);
        else if (pName.equalsIgnoreCase("EnchantBreakArmorBlessed")) ENCHANT_BREAK_ARMOR_BLESSED = Boolean.parseBoolean(pValue);
        else if (pName.equalsIgnoreCase("EnchantMaxWeapon")) ENCHANT_MAX_WEAPON = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("EnchantMaxArmor")) ENCHANT_MAX_ARMOR = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("EnchantSafeMax")) ENCHANT_SAFE_MAX = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("EnchantSafeMaxFull")) ENCHANT_SAFE_MAX_FULL = Integer.parseInt(pValue);

        else if (pName.equalsIgnoreCase("HpRegenMultiplier")) HP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
        else if (pName.equalsIgnoreCase("MpRegenMultiplier")) MP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
        
        else if (pName.equalsIgnoreCase("PlayerHpRegenMultiplier")) PLAYER_HP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
        else if (pName.equalsIgnoreCase("PlayerMpRegenMultiplier")) PLAYER_MP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
        else if (pName.equalsIgnoreCase("PlayerCpRegenMultiplier")) PLAYER_CP_REGEN_MULTIPLIER = Double.parseDouble(pValue);

        else if (pName.equalsIgnoreCase("RaidHpRegenMultiplier")) RAID_HP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
        else if (pName.equalsIgnoreCase("RaidMpRegenMultiplier")) RAID_MP_REGEN_MULTIPLIER = Double.parseDouble(pValue);
        else if (pName.equalsIgnoreCase("RaidDefenceMultiplier")) RAID_DEFENCE_MULTIPLIER = Double.parseDouble(pValue) /100;

        else if (pName.equalsIgnoreCase("StartingAdena")) STARTING_ADENA = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("UnstuckInterval")) UNSTUCK_INTERVAL = Integer.parseInt(pValue);

        else if (pName.equalsIgnoreCase("PlayerSpawnProtection")) PLAYER_SPAWN_PROTECTION = Integer.parseInt(pValue);

        else if (pName.equalsIgnoreCase("PartyXpCutoffMethod")) PARTY_XP_CUTOFF_METHOD = pValue;
        else if (pName.equalsIgnoreCase("PartyXpCutoffPercent")) PARTY_XP_CUTOFF_PERCENT = Double.parseDouble(pValue);
        else if (pName.equalsIgnoreCase("PartyXpCutoffLevel")) PARTY_XP_CUTOFF_LEVEL = Integer.parseInt(pValue);

        else if (pName.equalsIgnoreCase("RespawnRestoreCP")) RESPAWN_RESTORE_CP = Double.parseDouble(pValue) / 100;
        else if (pName.equalsIgnoreCase("RespawnRestoreHP")) RESPAWN_RESTORE_HP = Double.parseDouble(pValue) / 100;
        else if (pName.equalsIgnoreCase("RespawnRestoreMP")) RESPAWN_RESTORE_MP = Double.parseDouble(pValue) / 100;

        else if (pName.equalsIgnoreCase("MaxPvtStoreSlotsDwarf")) MAX_PVTSTORE_SLOTS_DWARF = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("MaxPvtStoreSlotsOther")) MAX_PVTSTORE_SLOTS_OTHER = Integer.parseInt(pValue);

        else if (pName.equalsIgnoreCase("StoreSkillCooltime")) STORE_SKILL_COOLTIME = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AnnounceMammonSpawn")) ANNOUNCE_MAMMON_SPAWN = Boolean.valueOf(pValue);
        
        // Spoil settings
        else if (pName.equalsIgnoreCase("CanSpoilLowerLevelMobs")) CAN_SPOIL_LOWER_LEVEL_MOBS = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("CanDelevelToSpoil")) CAN_DELEVEL_AND_SPOIL_MOBS = Boolean.valueOf(pValue);

        else if (pName.equalsIgnoreCase("MaximumPlayerAndMobLevelDifference")) MAXIMUM_PLAYER_AND_MOB_LEVEL_DIFFERENCE = Float.parseFloat(pValue);
        else if (pName.equalsIgnoreCase("BasePercentChanceOfSpoilSuccess")) BASE_SPOIL_RATE = Float.parseFloat(pValue);
        else if (pName.equalsIgnoreCase("MinimumPercentChanceOfSpoilSuccess")) MINIMUM_SPOIL_RATE = Float.parseFloat(pValue);
        else if (pName.equalsIgnoreCase("SpoilLevelDifferenceLimit")) SPOIL_LEVEL_DIFFERENCE_LIMIT = Float.parseFloat(pValue);
        else if (pName.equalsIgnoreCase("SpoilLevelMultiplier")) SPOIL_LEVEL_DIFFERENCE_MULTIPLIER = Float.parseFloat(pValue);

        else if (pName.equalsIgnoreCase("LastLevelSpoilIsLearned")) LAST_LEVEL_SPOIL_IS_LEARNED = Integer.parseInt(pValue);

        // Alternative settings
        else if (pName.equalsIgnoreCase("AltGameTiredness")) ALT_GAME_TIREDNESS = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AltGameCreation")) ALT_GAME_CREATION = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AltGameCreationSpeed")) ALT_GAME_CREATION_SPEED = Double.parseDouble(pValue);
        else if (pName.equalsIgnoreCase("AltGameCreationXpRate")) ALT_GAME_CREATION_XP_RATE = Double.parseDouble(pValue);
        else if (pName.equalsIgnoreCase("AltGameCreationSpRate")) ALT_GAME_CREATION_SP_RATE = Double.parseDouble(pValue); 
        else if (pName.equalsIgnoreCase("AltGameSkillLearn")) ALT_GAME_SKILL_LEARN = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AltNbCumulatedBuff")) ALT_GAME_NUMBER_OF_CUMULATED_BUFF = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("AltBuffTime")) ALT_BUFF_TIME = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("AltSuccessRate")) ALT_DAGGER_RATE = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("DaggerVSRobe")) ALT_DAGGER_DMG_VS_ROBE = Float.parseFloat(pValue);
        else if (pName.equalsIgnoreCase("DaggerVSLight")) ALT_DAGGER_DMG_VS_LIGHT = Float.parseFloat(pValue);
        else if (pName.equalsIgnoreCase("InstantKillEffect2")) ALT_INSTANT_KILL_EFFECT_2 = Float.parseFloat(pValue);
        else if (pName.equalsIgnoreCase("DaggerVSHeavy")) ALT_DAGGER_DMG_VS_HEAVY = Float.parseFloat(pValue);
        else if (pName.equalsIgnoreCase("AltAttackDelay")) ALT_ATTACK_DELAY = Float.parseFloat(pValue);
        else if (pName.equalsIgnoreCase("AltFailRate")) ALT_DAGGER_FAIL_RATE = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("AltBehindRate")) ALT_DAGGER_RATE_BEHIND = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("AltFrontRate")) ALT_DAGGER_RATE_FRONT = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("AltDanceTime")) ALT_DANCE_TIME = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("MaxPAtkSpeed")) MAX_PATK_SPEED = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("MaxMAtkSpeed")) MAX_MATK_SPEED = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("GradePenalty")) GRADE_PENALTY = Boolean.valueOf(pValue);

        else if (pName.equalsIgnoreCase("AltGameCancelByHit"))
        {
            ALT_GAME_CANCEL_BOW     = pValue.equalsIgnoreCase("bow") || pValue.equalsIgnoreCase("all");
            ALT_GAME_CANCEL_CAST    = pValue.equalsIgnoreCase("cast") || pValue.equalsIgnoreCase("all");
        }

        else if (pName.equalsIgnoreCase("AltShieldBlocks")) ALT_GAME_SHIELD_BLOCKS = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AltPerfectShieldBlockRate")) ALT_PERFECT_SHLD_BLOCK = Integer.parseInt(pValue); 
        else if (pName.equalsIgnoreCase("Delevel")) ALT_GAME_DELEVEL = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("MagicFailures")) ALT_GAME_MAGICFAILURES = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AltGameMobAttackAI")) ALT_GAME_MOB_ATTACK_AI = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AltGameSkillFormulas")) ALT_GAME_SKILL_FORMULAS = pValue;

        else if (pName.equalsIgnoreCase("AltGameExponentXp")) ALT_GAME_EXPONENT_XP = Float.parseFloat(pValue);
        else if (pName.equalsIgnoreCase("AltGameExponentSp")) ALT_GAME_EXPONENT_SP = Float.parseFloat(pValue);

        else if (pName.equalsIgnoreCase("AllowClassMaster1")) ALLOW_CLASS_MASTER_1 = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AllowClassMaster2")) ALLOW_CLASS_MASTER_2 = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AllowClassMaster3")) ALLOW_CLASS_MASTER_3 = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AltGameFreights")) ALT_GAME_FREIGHTS = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AltGameFreightPrice")) ALT_GAME_FREIGHT_PRICE = Integer.parseInt(pValue);

        else if (pName.equalsIgnoreCase("AltGameSkillHitRate")) ALT_GAME_SKILL_HIT_RATE = Float.parseFloat(pValue);
        else if (pName.equalsIgnoreCase("EnableRateHp")) ENABLE_RATE_HP = Boolean.valueOf(pValue);

        else if (pName.equalsIgnoreCase("CraftingEnabled")) IS_CRAFTING_ENABLED = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("SpBookNeeded")) SP_BOOK_NEEDED = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AutoLoot")) AUTO_LOOT = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AutoLootHerbs")) AUTO_LOOT_HERBS = Boolean.valueOf(pValue);

        else if (pName.equalsIgnoreCase("AltKarmaPlayerCanBeKilledInPeaceZone")) ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AltKarmaPlayerCanShop")) ALT_GAME_KARMA_PLAYER_CAN_SHOP = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AltKarmaPlayerCanUseGK")) ALT_GAME_KARMA_PLAYER_CAN_USE_GK = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AltKarmaPlayerCanTeleport")) ALT_GAME_KARMA_PLAYER_CAN_TELEPORT = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AltKarmaPlayerCanTrade")) ALT_GAME_KARMA_PLAYER_CAN_TRADE = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AltKarmaPlayerCanUseWareHouse")) ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AltRequireCastleForDawn")) ALT_GAME_REQUIRE_CASTLE_DAWN = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AltRequireClanCastle")) ALT_GAME_REQUIRE_CLAN_CASTLE = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AltFreeTeleporting")) ALT_GAME_FREE_TELEPORT = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AltSubClassWithoutQuests")) ALT_GAME_SUBCLASS_WITHOUT_QUESTS = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AltNewCharAlwaysIsNewbie")) ALT_GAME_NEW_CHAR_ALWAYS_IS_NEWBIE = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("DwarfRecipeLimit")) DWARF_RECIPE_LIMIT = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("CommonRecipeLimit")) COMMON_RECIPE_LIMIT = Integer.parseInt(pValue);        

        // PvP settings
        else if (pName.equalsIgnoreCase("MinKarma")) KARMA_MIN_KARMA = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("MaxKarma")) KARMA_MAX_KARMA = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("XPDivider")) KARMA_XP_DIVIDER = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("BaseKarmaLost")) KARMA_LOST_BASE = Integer.parseInt(pValue);

        else if (pName.equalsIgnoreCase("CanGMDropEquipment")) KARMA_DROP_GM = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("AwardPKKillPVPPoint")) KARMA_AWARD_PK_KILL = Boolean.valueOf(pValue);
        else if (pName.equalsIgnoreCase("MinimumPKRequiredToDrop")) KARMA_PK_LIMIT = Integer.parseInt(pValue);
        
        else if (pName.equalsIgnoreCase("PvPTime")) PVP_TIME = Integer.parseInt(pValue);
        else if (pName.equalsIgnoreCase("GlobalChat")) DEFAULT_GLOBAL_CHAT = pValue;
        else if (pName.equalsIgnoreCase("TradeChat"))  DEFAULT_TRADE_CHAT = pValue;
        
        else if (pName.equalsIgnoreCase("TvTEvenTeams"))  TVT_EVEN_TEAMS = pValue;
        else if (pName.equalsIgnoreCase("CTFEvenTeams"))  CTF_EVEN_TEAMS = pValue;
        else return false;
        return true;
    }
    
    /**
     * Allow the player to use L2Walker ?
     * @param player (L2PcInstance) : Player trying to use L2Walker
     * @return boolean : true if (L2Walker allowed as a general rule) or (L2Walker client allowed for GM and 
     *                   player is a GM)
     */
    public static boolean allowL2Walker(L2PcInstance player)
    {
        return (ALLOW_L2WALKER_CLIENT == L2WalkerAllowed.True ||
                (ALLOW_L2WALKER_CLIENT == L2WalkerAllowed.GM && player != null && player.isGM()));
    }
	
	// it has no instancies
	private Config() {}

	/**
     * Save hexadecimal ID of the server in the properties file.
	 * @param string (String) : hexadecimal ID of the server to store
     * @see HEXID_FILE
     * @see saveHexid(String string, String fileName)
     * @link LoginServerThread
	 */
	public static void saveHexid(String string)
	{
		saveHexid(string,HEXID_FILE);
	}
	
	/**
     * Save hexadecimal ID of the server in the properties file.
     * @param string (String) : hexadecimal ID of the server to store
     * @param fileName (String) : name of the properties file
	 */
	public static void saveHexid(String string, String fileName)
	{
		try
        {
            Properties hexSetting    = new Properties();
            File file = new File(fileName);
            //Create a new empty file only if it doesn't exist
            file.createNewFile();
            OutputStream out = new FileOutputStream(file);
            hexSetting.setProperty("HexID",string);
			hexSetting.store(out,"the hexID to auth into login");
			out.close();
        }
        catch (Exception e)
        {
            _log.warn("Failed to save hex id to "+fileName+" File.");
            e.printStackTrace();
        }
	}
	
}
