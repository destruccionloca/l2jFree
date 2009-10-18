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
package com.l2jfree.gameserver.model.actor.instance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javolution.util.FastList;

import org.apache.commons.lang.ArrayUtils;

import com.l2jfree.Config;
import com.l2jfree.L2DatabaseFactory;
import com.l2jfree.gameserver.Announcements;
import com.l2jfree.gameserver.GameServer;
import com.l2jfree.gameserver.GameTimeController;
import com.l2jfree.gameserver.ItemsAutoDestroy;
import com.l2jfree.gameserver.LoginServerThread;
import com.l2jfree.gameserver.RecipeController;
import com.l2jfree.gameserver.SevenSigns;
import com.l2jfree.gameserver.SevenSignsFestival;
import com.l2jfree.gameserver.Shutdown;
import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.Shutdown.DisableType;
import com.l2jfree.gameserver.ai.CtrlIntention;
import com.l2jfree.gameserver.ai.L2CharacterAI;
import com.l2jfree.gameserver.ai.L2PlayerAI;
import com.l2jfree.gameserver.ai.L2SummonAI;
import com.l2jfree.gameserver.cache.HtmCache;
import com.l2jfree.gameserver.cache.WarehouseCacheManager;
import com.l2jfree.gameserver.communitybbs.Manager.ForumsBBSManager;
import com.l2jfree.gameserver.communitybbs.Manager.RegionBBSManager;
import com.l2jfree.gameserver.communitybbs.Manager.RegionBBSManager.PlayerStateOnCommunity;
import com.l2jfree.gameserver.communitybbs.bb.Forum;
import com.l2jfree.gameserver.datatables.CharNameTable;
import com.l2jfree.gameserver.datatables.CharTemplateTable;
import com.l2jfree.gameserver.datatables.ClanTable;
import com.l2jfree.gameserver.datatables.DoorTable;
import com.l2jfree.gameserver.datatables.FishTable;
import com.l2jfree.gameserver.datatables.GmListTable;
import com.l2jfree.gameserver.datatables.HennaTable;
import com.l2jfree.gameserver.datatables.HeroSkillTable;
import com.l2jfree.gameserver.datatables.ItemTable;
import com.l2jfree.gameserver.datatables.NobleSkillTable;
import com.l2jfree.gameserver.datatables.NpcTable;
import com.l2jfree.gameserver.datatables.PetDataTable;
import com.l2jfree.gameserver.datatables.RecordTable;
import com.l2jfree.gameserver.datatables.ShotTable;
import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.datatables.SkillTreeTable;
import com.l2jfree.gameserver.geodata.GeoData;
import com.l2jfree.gameserver.handler.ItemHandler;
import com.l2jfree.gameserver.handler.SkillHandler;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminEditChar;
import com.l2jfree.gameserver.handler.skillhandlers.SummonFriend;
import com.l2jfree.gameserver.handler.skillhandlers.TakeCastle;
import com.l2jfree.gameserver.handler.skillhandlers.TakeFort;
import com.l2jfree.gameserver.instancemanager.CastleManager;
import com.l2jfree.gameserver.instancemanager.CoupleManager;
import com.l2jfree.gameserver.instancemanager.CursedWeaponsManager;
import com.l2jfree.gameserver.instancemanager.DimensionalRiftManager;
import com.l2jfree.gameserver.instancemanager.DuelManager;
import com.l2jfree.gameserver.instancemanager.FactionManager;
import com.l2jfree.gameserver.instancemanager.FortManager;
import com.l2jfree.gameserver.instancemanager.FortSiegeManager;
import com.l2jfree.gameserver.instancemanager.FourSepulchersManager;
import com.l2jfree.gameserver.instancemanager.QuestManager;
import com.l2jfree.gameserver.instancemanager.RecommendationManager;
import com.l2jfree.gameserver.instancemanager.SiegeManager;
import com.l2jfree.gameserver.instancemanager.ZoneManager;
import com.l2jfree.gameserver.instancemanager.grandbosses.AntharasManager;
import com.l2jfree.gameserver.instancemanager.grandbosses.BaiumManager;
import com.l2jfree.gameserver.instancemanager.grandbosses.BaylorManager;
import com.l2jfree.gameserver.instancemanager.grandbosses.FrintezzaManager;
import com.l2jfree.gameserver.instancemanager.grandbosses.SailrenManager;
import com.l2jfree.gameserver.instancemanager.grandbosses.ValakasManager;
import com.l2jfree.gameserver.instancemanager.grandbosses.VanHalterManager;
import com.l2jfree.gameserver.instancemanager.lastimperialtomb.LastImperialTombManager;
import com.l2jfree.gameserver.instancemanager.leaderboards.ArenaManager;
import com.l2jfree.gameserver.model.BlockList;
import com.l2jfree.gameserver.model.CursedWeapon;
import com.l2jfree.gameserver.model.Elementals;
import com.l2jfree.gameserver.model.FishData;
import com.l2jfree.gameserver.model.L2Clan;
import com.l2jfree.gameserver.model.L2ClanMember;
import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.model.L2Fishing;
import com.l2jfree.gameserver.model.L2FriendList;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2Macro;
import com.l2jfree.gameserver.model.L2ManufactureList;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2Party;
import com.l2jfree.gameserver.model.L2PetData;
import com.l2jfree.gameserver.model.L2Radar;
import com.l2jfree.gameserver.model.L2RecipeList;
import com.l2jfree.gameserver.model.L2Request;
import com.l2jfree.gameserver.model.L2ShortCut;
import com.l2jfree.gameserver.model.L2SiegeClan;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2SkillLearn;
import com.l2jfree.gameserver.model.L2Transformation;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.L2WorldRegion;
import com.l2jfree.gameserver.model.MacroList;
import com.l2jfree.gameserver.model.ShortCuts;
import com.l2jfree.gameserver.model.TradeList;
import com.l2jfree.gameserver.model.L2Skill.SkillTargetType;
import com.l2jfree.gameserver.model.actor.L2Attackable;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Decoy;
import com.l2jfree.gameserver.model.actor.L2Npc;
import com.l2jfree.gameserver.model.actor.L2Playable;
import com.l2jfree.gameserver.model.actor.L2SiegeGuard;
import com.l2jfree.gameserver.model.actor.L2Summon;
import com.l2jfree.gameserver.model.actor.L2Trap;
import com.l2jfree.gameserver.model.actor.appearance.PcAppearance;
import com.l2jfree.gameserver.model.actor.knownlist.CharKnownList;
import com.l2jfree.gameserver.model.actor.knownlist.PcKnownList;
import com.l2jfree.gameserver.model.actor.reference.ClearableReference;
import com.l2jfree.gameserver.model.actor.reference.ImmutableReference;
import com.l2jfree.gameserver.model.actor.shot.CharShots;
import com.l2jfree.gameserver.model.actor.shot.PcShots;
import com.l2jfree.gameserver.model.actor.stat.CharStat;
import com.l2jfree.gameserver.model.actor.stat.PcStat;
import com.l2jfree.gameserver.model.actor.status.CharStatus;
import com.l2jfree.gameserver.model.actor.status.PcStatus;
import com.l2jfree.gameserver.model.actor.view.CharLikeView;
import com.l2jfree.gameserver.model.actor.view.PcView;
import com.l2jfree.gameserver.model.base.ClassId;
import com.l2jfree.gameserver.model.base.ClassLevel;
import com.l2jfree.gameserver.model.base.Experience;
import com.l2jfree.gameserver.model.base.Race;
import com.l2jfree.gameserver.model.base.SubClass;
import com.l2jfree.gameserver.model.entity.Castle;
import com.l2jfree.gameserver.model.entity.Duel;
import com.l2jfree.gameserver.model.entity.Fort;
import com.l2jfree.gameserver.model.entity.FortSiege;
import com.l2jfree.gameserver.model.entity.GrandBossState;
import com.l2jfree.gameserver.model.entity.Siege;
import com.l2jfree.gameserver.model.entity.events.AbstractFunEventPlayerInfo;
import com.l2jfree.gameserver.model.entity.events.AutomatedTvT;
import com.l2jfree.gameserver.model.entity.events.CTF;
import com.l2jfree.gameserver.model.entity.events.DM;
import com.l2jfree.gameserver.model.entity.events.TvT;
import com.l2jfree.gameserver.model.entity.events.VIP;
import com.l2jfree.gameserver.model.entity.events.CTF.CTFPlayerInfo;
import com.l2jfree.gameserver.model.entity.events.DM.DMPlayerInfo;
import com.l2jfree.gameserver.model.entity.events.TvT.TvTPlayerInfo;
import com.l2jfree.gameserver.model.entity.events.VIP.VIPPlayerInfo;
import com.l2jfree.gameserver.model.entity.faction.FactionMember;
import com.l2jfree.gameserver.model.itemcontainer.Inventory;
import com.l2jfree.gameserver.model.itemcontainer.ItemContainer;
import com.l2jfree.gameserver.model.itemcontainer.PcFreight;
import com.l2jfree.gameserver.model.itemcontainer.PcInventory;
import com.l2jfree.gameserver.model.itemcontainer.PcWarehouse;
import com.l2jfree.gameserver.model.itemcontainer.PetInventory;
import com.l2jfree.gameserver.model.mapregion.TeleportWhereType;
import com.l2jfree.gameserver.model.olympiad.Olympiad;
import com.l2jfree.gameserver.model.quest.Quest;
import com.l2jfree.gameserver.model.quest.QuestState;
import com.l2jfree.gameserver.model.quest.State;
import com.l2jfree.gameserver.model.restriction.AvailableRestriction;
import com.l2jfree.gameserver.model.restriction.ObjectRestrictions;
import com.l2jfree.gameserver.model.restriction.global.DuelRestriction;
import com.l2jfree.gameserver.model.restriction.global.GlobalRestrictions;
import com.l2jfree.gameserver.model.zone.L2JailZone;
import com.l2jfree.gameserver.model.zone.L2Zone;
import com.l2jfree.gameserver.network.Disconnection;
import com.l2jfree.gameserver.network.InvalidPacketException;
import com.l2jfree.gameserver.network.L2GameClient;
import com.l2jfree.gameserver.network.SystemChatChannelId;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.AbstractNpcInfo;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.CameraMode;
import com.l2jfree.gameserver.network.serverpackets.ChangeWaitType;
import com.l2jfree.gameserver.network.serverpackets.CharInfo;
import com.l2jfree.gameserver.network.serverpackets.ConfirmDlg;
import com.l2jfree.gameserver.network.serverpackets.EtcStatusUpdate;
import com.l2jfree.gameserver.network.serverpackets.ExBasicActionList;
import com.l2jfree.gameserver.network.serverpackets.ExDuelUpdateUserInfo;
import com.l2jfree.gameserver.network.serverpackets.ExFishingEnd;
import com.l2jfree.gameserver.network.serverpackets.ExFishingStart;
import com.l2jfree.gameserver.network.serverpackets.ExGetBookMarkInfoPacket;
import com.l2jfree.gameserver.network.serverpackets.ExGetOnAirShip;
import com.l2jfree.gameserver.network.serverpackets.ExOlympiadMode;
import com.l2jfree.gameserver.network.serverpackets.ExOlympiadUserInfo;
import com.l2jfree.gameserver.network.serverpackets.ExPrivateStoreSetWholeMsg;
import com.l2jfree.gameserver.network.serverpackets.ExSetCompassZoneCode;
import com.l2jfree.gameserver.network.serverpackets.ExSpawnEmitter;
import com.l2jfree.gameserver.network.serverpackets.ExStorageMaxCount;
import com.l2jfree.gameserver.network.serverpackets.FriendList;
import com.l2jfree.gameserver.network.serverpackets.GMHide;
import com.l2jfree.gameserver.network.serverpackets.GameGuardQuery;
import com.l2jfree.gameserver.network.serverpackets.GetOnVehicle;
import com.l2jfree.gameserver.network.serverpackets.HennaInfo;
import com.l2jfree.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfree.gameserver.network.serverpackets.ItemList;
import com.l2jfree.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jfree.gameserver.network.serverpackets.MagicEffectIcons;
import com.l2jfree.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jfree.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jfree.gameserver.network.serverpackets.NicknameChanged;
import com.l2jfree.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfree.gameserver.network.serverpackets.ObservationMode;
import com.l2jfree.gameserver.network.serverpackets.ObservationReturn;
import com.l2jfree.gameserver.network.serverpackets.PartyMemberPosition;
import com.l2jfree.gameserver.network.serverpackets.PartySmallWindowUpdate;
import com.l2jfree.gameserver.network.serverpackets.PartySpelled;
import com.l2jfree.gameserver.network.serverpackets.PetInventoryUpdate;
import com.l2jfree.gameserver.network.serverpackets.PledgeShowMemberListDelete;
import com.l2jfree.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import com.l2jfree.gameserver.network.serverpackets.PledgeSkillList;
import com.l2jfree.gameserver.network.serverpackets.PrivateStoreListBuy;
import com.l2jfree.gameserver.network.serverpackets.PrivateStoreListSell;
import com.l2jfree.gameserver.network.serverpackets.PrivateStoreManageListBuy;
import com.l2jfree.gameserver.network.serverpackets.PrivateStoreManageListSell;
import com.l2jfree.gameserver.network.serverpackets.PrivateStoreMsgBuy;
import com.l2jfree.gameserver.network.serverpackets.PrivateStoreMsgSell;
import com.l2jfree.gameserver.network.serverpackets.QuestList;
import com.l2jfree.gameserver.network.serverpackets.RecipeShopMsg;
import com.l2jfree.gameserver.network.serverpackets.RecipeShopSellList;
import com.l2jfree.gameserver.network.serverpackets.RelationChanged;
import com.l2jfree.gameserver.network.serverpackets.Ride;
import com.l2jfree.gameserver.network.serverpackets.SetupGauge;
import com.l2jfree.gameserver.network.serverpackets.ShortBuffStatusUpdate;
import com.l2jfree.gameserver.network.serverpackets.ShortCutInit;
import com.l2jfree.gameserver.network.serverpackets.SkillCoolTime;
import com.l2jfree.gameserver.network.serverpackets.SkillList;
import com.l2jfree.gameserver.network.serverpackets.Snoop;
import com.l2jfree.gameserver.network.serverpackets.SocialAction;
import com.l2jfree.gameserver.network.serverpackets.SpecialCamera;
import com.l2jfree.gameserver.network.serverpackets.StaticPacket;
import com.l2jfree.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfree.gameserver.network.serverpackets.StopMove;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.network.serverpackets.TargetSelected;
import com.l2jfree.gameserver.network.serverpackets.TargetUnselected;
import com.l2jfree.gameserver.network.serverpackets.TradeDone;
import com.l2jfree.gameserver.network.serverpackets.TradeOtherDone;
import com.l2jfree.gameserver.network.serverpackets.TradeStart;
import com.l2jfree.gameserver.network.serverpackets.UserInfo;
import com.l2jfree.gameserver.network.serverpackets.ValidateLocation;
import com.l2jfree.gameserver.network.serverpackets.EffectInfoPacket.EffectInfoPacketList;
import com.l2jfree.gameserver.skills.Env;
import com.l2jfree.gameserver.skills.Formulas;
import com.l2jfree.gameserver.skills.Stats;
import com.l2jfree.gameserver.skills.conditions.ConditionGameTime;
import com.l2jfree.gameserver.skills.conditions.ConditionPlayerHp;
import com.l2jfree.gameserver.skills.funcs.Func;
import com.l2jfree.gameserver.skills.l2skills.L2SkillSummon;
import com.l2jfree.gameserver.taskmanager.AbstractIterativePeriodicTaskManager;
import com.l2jfree.gameserver.taskmanager.AttackStanceTaskManager;
import com.l2jfree.gameserver.taskmanager.LeakTaskManager;
import com.l2jfree.gameserver.taskmanager.SQLQueue;
import com.l2jfree.gameserver.taskmanager.PacketBroadcaster.BroadcastMode;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;
import com.l2jfree.gameserver.templates.chars.L2PcTemplate;
import com.l2jfree.gameserver.templates.item.L2Armor;
import com.l2jfree.gameserver.templates.item.L2ArmorType;
import com.l2jfree.gameserver.templates.item.L2EtcItemType;
import com.l2jfree.gameserver.templates.item.L2Henna;
import com.l2jfree.gameserver.templates.item.L2Item;
import com.l2jfree.gameserver.templates.item.L2Weapon;
import com.l2jfree.gameserver.templates.item.L2WeaponType;
import com.l2jfree.gameserver.templates.skills.L2EffectType;
import com.l2jfree.gameserver.templates.skills.L2SkillType;
import com.l2jfree.gameserver.util.Broadcast;
import com.l2jfree.gameserver.util.FloodProtector;
import com.l2jfree.gameserver.util.Util;
import com.l2jfree.lang.L2Math;
import com.l2jfree.sql.SQLQuery;
import com.l2jfree.tools.geometry.Point3D;
import com.l2jfree.tools.random.Rnd;
import com.l2jfree.util.L2Arrays;
import com.l2jfree.util.L2Collections;
import com.l2jfree.util.LinkedBunch;
import com.l2jfree.util.SingletonList;
import com.l2jfree.util.SingletonMap;

/**
 * This class represents all player characters in the world.
 * There is always a client-thread connected to this (except if a player-store is activated upon logout).<BR><BR>
 *
 * @version $Revision: 1.66.2.41.2.33 $ $Date: 2005/04/11 10:06:09 $
 */
public final class L2PcInstance extends L2Playable
{
	@SuppressWarnings("hiding")
	public static final L2PcInstance[] EMPTY_ARRAY = new L2PcInstance[0];

	// Character Skill SQL String Definitions:
	private static final String	RESTORE_SKILLS_FOR_CHAR			= "SELECT skill_id,skill_level FROM character_skills WHERE charId=? AND class_index=?";
	private static final String	ADD_NEW_SKILL					= "INSERT INTO character_skills (charId,skill_id,skill_level,class_index) VALUES (?,?,?,?)";
	private static final String	UPDATE_CHARACTER_SKILL_LEVEL	= "UPDATE character_skills SET skill_level=? WHERE skill_id=? AND charId=? AND class_index=?";
	private static final String	DELETE_SKILL_FROM_CHAR			= "DELETE FROM character_skills WHERE skill_id=? AND charId=? AND class_index=?";
	private static final String	DELETE_CHAR_SKILLS				= "DELETE FROM character_skills WHERE charId=? AND class_index=?";

	// Character Skill Save SQL String Definitions:
	private static final String	ADD_SKILL_SAVE					= "REPLACE INTO character_skills_save (charId,skill_id,skill_level,effect_count,effect_cur_time,reuse_delay,systime,restore_type,class_index,buff_index) VALUES (?,?,?,?,?,?,?,?,?,?)";
	private static final String	RESTORE_SKILL_SAVE				= "SELECT skill_id,skill_level,effect_count,effect_cur_time,reuse_delay,systime FROM character_skills_save WHERE charId=? AND class_index=? AND restore_type=? ORDER BY buff_index ASC";
	private static final String	DELETE_SKILL_SAVE				= "DELETE FROM character_skills_save WHERE charId=? AND class_index=?";

	// Character Character SQL String Definitions:
	private static final String	UPDATE_CHARACTER				= "UPDATE characters SET level=?,maxHp=?,curHp=?,maxCp=?,curCp=?,maxMp=?,curMp=?,face=?,hairStyle=?,hairColor=?,sex=?,heading=?,x=?,y=?,z=?,exp=?,expBeforeDeath=?,sp=?,karma=?,fame=?,pvpkills=?,pkkills=?,clanid=?,race=?,classid=?,deletetime=?,title=?,accesslevel=?,online=?,isin7sdungeon=?,clan_privs=?,wantspeace=?,base_class=?,onlinetime=?,in_jail=?,jail_timer=?,newbie=?,nobless=?,pledge_rank=?,subpledge=?,lvl_joined_academy=?,apprentice=?,sponsor=?,varka_ketra_ally=?,clan_join_expiry_time=?,clan_create_expiry_time=?,banchat_timer=?,char_name=?,death_penalty_level=?,trust_level=?,vitality_points=?,bookmarkslot=? WHERE charId=?";
	private static final String	RESTORE_CHARACTER				= "SELECT account_name, charId, char_name, level, maxHp, curHp, maxCp, curCp, maxMp, curMp, face, hairStyle, hairColor, sex, heading, x, y, z, exp, expBeforeDeath, sp, karma, fame, pvpkills, pkkills, clanid, race, classid, deletetime, cancraft, title, accesslevel, online, char_slot, lastAccess, clan_privs, wantspeace, base_class, onlinetime, isin7sdungeon, in_jail, jail_timer, banchat_timer, newbie, nobless, pledge_rank, subpledge, lvl_joined_academy, apprentice, sponsor, varka_ketra_ally, clan_join_expiry_time,clan_create_expiry_time,charViP,death_penalty_level,trust_level,vitality_points,bookmarkslot FROM characters WHERE charId=?";

	// Character Subclass SQL String Definitions:
	private static final String	RESTORE_CHAR_SUBCLASSES			= "SELECT class_id,exp,sp,level,class_index FROM character_subclasses WHERE charId=? ORDER BY class_index ASC";
	private static final String	ADD_CHAR_SUBCLASS				= "INSERT INTO character_subclasses (charId,class_id,exp,sp,level,class_index) VALUES (?,?,?,?,?,?)";
	private static final String	UPDATE_CHAR_SUBCLASS			= "UPDATE character_subclasses SET exp=?,sp=?,level=?,class_id=? WHERE charId=? AND class_index =?";
	private static final String	DELETE_CHAR_SUBCLASS			= "DELETE FROM character_subclasses WHERE charId=? AND class_index=?";

	// Character Henna SQL String Definitions:
	private static final String	RESTORE_CHAR_HENNAS				= "SELECT slot,symbol_id FROM character_hennas WHERE charId=? AND class_index=?";
	private static final String	ADD_CHAR_HENNA					= "INSERT INTO character_hennas (charId,symbol_id,slot,class_index) VALUES (?,?,?,?)";
	private static final String	DELETE_CHAR_HENNA				= "DELETE FROM character_hennas WHERE charId=? AND slot=? AND class_index=?";
	private static final String	DELETE_CHAR_HENNAS				= "DELETE FROM character_hennas WHERE charId=? AND class_index=?";

	// Character Shortcut SQL String Definitions:
	private static final String	DELETE_CHAR_SHORTCUTS			= "DELETE FROM character_shortcuts WHERE charId=? AND class_index=?";

	// Character Transformation SQL String Definitions:
	private static final String	SELECT_CHAR_TRANSFORM			= "SELECT transform_id FROM characters WHERE charId=?";
	private static final String	UPDATE_CHAR_TRANSFORM			= "UPDATE characters SET transform_id=? WHERE charId=?";

	// Character Teleport Bookmark:
	private static final String INSERT_TP_BOOKMARK 				= "INSERT INTO character_tpbookmark (charId,Id,x,y,z,icon,tag,name) values (?,?,?,?,?,?,?,?)";
	private static final String UPDATE_TP_BOOKMARK 				= "UPDATE character_tpbookmark SET icon=?,tag=?,name=? where charId=? AND Id=?";
	private static final String RESTORE_TP_BOOKMARK 			= "SELECT Id,x,y,z,icon,tag,name FROM character_tpbookmark WHERE charId=?";
	private static final String DELETE_TP_BOOKMARK 				= "DELETE FROM character_tpbookmark WHERE charId=? AND Id=?";

	// Subclass certification
	public static final String  STORE_CHAR_CERTIFICATION		= "INSERT INTO character_subclass_certification (charId,class_index,certif_level) VALUES (?,?,?)";
	public static final String  UPDATE_CHAR_CERTIFICATION		= "UPDATE character_subclass_certification SET certif_level=? WHERE charId=? AND class_index=?";
	private static final String	DELETE_CHAR_CERTIFICATION		= "DELETE FROM character_subclass_certification WHERE charId=?";
	private static final String	GET_CHAR_CERTIFICATION			= "SELECT certif_level FROM character_subclass_certification WHERE charId=? AND class_index=?";

	public static final int		REQUEST_TIMEOUT					= 15;

	public static final int		STORE_PRIVATE_NONE				= 0;
	public static final int		STORE_PRIVATE_SELL				= 1;
	public static final int		STORE_PRIVATE_BUY				= 3;
	public static final int		STORE_PRIVATE_MANUFACTURE		= 5;
	public static final int		STORE_PRIVATE_PACKAGE_SELL		= 8;

	/** The table containing all minimum level needed for each Expertise (None, D, C, B, A, S, S80, S84)*/
	private static final int[]	EXPERTISE_LEVELS				=
																{
			SkillTreeTable.getInstance().getExpertiseLevel(0), // NONE
			SkillTreeTable.getInstance().getExpertiseLevel(1), // D
			SkillTreeTable.getInstance().getExpertiseLevel(2), // C
			SkillTreeTable.getInstance().getExpertiseLevel(3), // B
			SkillTreeTable.getInstance().getExpertiseLevel(4), // A
			SkillTreeTable.getInstance().getExpertiseLevel(5), // S
			SkillTreeTable.getInstance().getExpertiseLevel(6), // S80
			SkillTreeTable.getInstance().getExpertiseLevel(7)  //S84
																};

	private static final int[] COMMON_CRAFT_LEVELS = { 5, 20, 28, 36, 43, 49, 55, 62 };
	
	public class AIAccessor extends L2Character.AIAccessor
	{
		protected AIAccessor()
		{
		}
		
		public L2PcInstance getPlayer()
		{
			return L2PcInstance.this;
		}
		
		public void doPickupItem(L2Object object)
		{
			L2PcInstance.this.doPickupItem(object);
		}
		
		public void doInteract(L2Character target)
		{
			L2PcInstance.this.doInteract(target);
		}
		
		@Override
		public void doAttack(L2Character target)
		{
			if (getAppearance().isInvisible())
				getEffects().stopEffects(L2EffectType.HIDE);
			
			super.doAttack(target);
			
			// Cancel the recent fake-death protection instantly if the player attacks or casts spells
			setRecentFakeDeath(false);
			
			if (isSilentMoving())
				getEffects().stopEffects(L2EffectType.SILENT_MOVE);
		}
		
		@Override
		public void doCast(L2Skill skill)
		{
			if (getAppearance().isInvisible())
				getEffects().stopEffects(L2EffectType.HIDE);
			
			super.doCast(skill);
			
			// Cancel the recent fake-death protection instantly if the player attacks or casts spells
			setRecentFakeDeath(false);
			if (skill == null)
				return;
			if (!skill.isOffensive())
				return;
			
			if (isSilentMoving())
				getEffects().stopEffects(L2EffectType.SILENT_MOVE);
			
			switch (skill.getTargetType())
			{
				case TARGET_GROUND:
					return;
				default:
				{
					for (L2CubicInstance cubic : getCubics().values())
						if (cubic.getId() != L2CubicInstance.LIFE_CUBIC)
							cubic.doAction();
				}
					break;
			}
		}
	}

	private L2GameClient					_client;

	private final PcAppearance				_appearance;

	/** Sitting down and Standing up fix */
	private long							_lastSitStandRequest	= 0;

	/** The Identifier of the L2PcInstance */
	private int								_charId					= 0x00030b7a;

	/** The Experience of the L2PcInstance before the last Death Penalty */
	private long							_expBeforeDeath;

	/** The Karma of the L2PcInstance (if higher than 0, the name of the L2PcInstance appears in red) */
	private int								_karma;

	/** The number of player killed during a PvP (the player killed was PvP Flagged) */
	private int								_pvpKills;

	/** The PK counter of the L2PcInstance (= Number of non PvP Flagged player killed) */
	private int								_pkKills;

	/** The Siege state of the L2PcInstance */
	private byte							_siegeState				= 0;
	private boolean							_isInSiege				= false;

	private int								_lastCompassZone;																	// The last compass zone update send to the client

	private boolean							_isIn7sDungeon			= false;

	public int 								_bookmarkslot = 0; // The Teleport Bookmark Slot
	public final List<TeleportBookmark> tpbookmark = new SingletonList<TeleportBookmark>();

	private int								_subPledgeType			= 0;

	/** L2PcInstance's pledge rank*/
	private int								_pledgeRank;

	/** Level at which the player joined the clan as an accedemy member*/
	private int								_lvlJoinedAcademy		= 0;

	/** The random number of the L2PcInstance */
	//private static final Random _rnd = new Random();
	private int								_curWeightPenalty		= 0;

	private long							_deleteTimer;
	private PcInventory						_inventory;
	private PcWarehouse						_warehouse;
	private PcFreight						_freight;
	private List<PcFreight>					_depositedFreight;

	/** True if the L2PcInstance is sitting */
	private boolean							_waitTypeSitting;

	/** True if the L2PcInstance is using the relax skill */
	private boolean							_relax;

	/** True if the L2PcInstance is in a boat */
	private boolean							_inBoat;
	/** AirShip */
	private L2AirShipInstance 				_airShip;
	private boolean 						_inAirShip;
	private Point3D 						_inAirShipPosition;

	public ScheduledFuture<?>				_taskforfish;

	/** Last NPC Id talked on a quest */
	private int								_questNpcObject			= 0;

	/** Bitmask used to keep track of one-time/newbie quest rewards */
	private int								_newbie;

	/** The table containing all Quests began by the L2PcInstance */
	private final Map<String, QuestState>			_quests					= new SingletonMap<String, QuestState>();

	/** The list containing all shortCuts of this L2PcInstance */
	private ShortCuts						_shortCuts;

	/** The list containing all macroses of this L2PcInstance */
	private MacroList						_macroses;

	private TradeList						_activeTradeList;
	private ItemContainer					_activeWarehouse;
	private L2ManufactureList				_createList;
	private TradeList						_sellList;
	private TradeList						_buyList;

	private L2PcInstance[] _snoopers = L2PcInstance.EMPTY_ARRAY; // List of GMs snooping this player
	private L2PcInstance[] _snoopedPlayers = L2PcInstance.EMPTY_ARRAY; // List of players being snooped by this GM

	/** The Private Store type of the L2PcInstance (STORE_PRIVATE_NONE=0, STORE_PRIVATE_SELL=1, sellmanage=2, STORE_PRIVATE_BUY=3, buymanage=4, STORE_PRIVATE_MANUFACTURE=5) */
	private int								_privatestore;
	private ClassId							_skillLearningClassId;

	// Hennas
	private final L2Henna[]					_henna					= new L2Henna[3];
	private int								_hennaSTR;
	private int								_hennaINT;
	private int								_hennaDEX;
	private int								_hennaMEN;
	private int								_hennaWIT;
	private int								_hennaCON;

	private boolean							_isRidingStrider		= false;
	private boolean							_isRidingRedStrider		= false;
	private boolean							_isRidingHorse			= false;
	private boolean 						_isFlyingMounted 		= false;

	/** The L2Summon of the L2PcInstance */
	private L2Summon						_summon					= null;
	/** The L2Decoy of the L2PcInstance */
	private L2Decoy							_decoy					= null;
	/** The L2Trap of the L2PcInstance */
	private L2Trap							_trap					= null;
	/** The L2Agathion of the L2PcInstance */
	private int								_agathionId				= 0;
	// Apparently, a L2PcInstance CAN have both a summon AND a tamed beast at the same time!!
	private L2TamedBeastInstance			_tamedBeast				= null;

	// Client radar
	private L2Radar							_radar;

	// These values are only stored temporarily
	private boolean							_partyMatchingAutomaticRegistration;
	private boolean							_partyMatchingShowLevel;
	private boolean							_partyMatchingShowClass;
	private String							_partyMatchingMemo;

	private L2Party							_party;
	// Clan related attributes

	/** The Clan Identifier of the L2PcInstance */
	private int								_clanId;

	/** The Clan object of the L2PcInstance */
	private L2Clan							_clan;

	/** Apprentice and Sponsor IDs */
	private int								_apprentice				= 0;
	private int								_sponsor				= 0;

	private long							_clanJoinExpiryTime;
	private long							_clanCreateExpiryTime;

	private long							_onlineTime;
	private long							_onlineBeginTime;

	// GM Stuff
	private boolean							_isGm;
	private int								_accessLevel;

	private boolean							_messageRefusal			= false;													// Message refusal mode
	private boolean							_dietMode				= false;													// Ignore weight penalty
	private boolean							_tradeRefusal			= false;													// Trade refusal
	private boolean							_exchangeRefusal		= false;													// Exchange refusal

	// This is needed to find the inviting player for Party response
	// There can only be one active party request at once
	private L2PcInstance					_activeRequester;
	private long							_requestExpireTime		= 0;
	private L2Request						_request;
	private L2ItemInstance					_arrowItem;
	private L2ItemInstance					_boltItem;

	// Used for protection after teleport
	private long							_protectEndTime			= 0;

	// Protects a char from agro mobs when getting up from fake death
	private long							_recentFakeDeathEndTime	= 0;

	/** The fists L2Weapon of the L2PcInstance (used when no weapon is equipped) */
	private L2Weapon						_fistsWeaponItem;

	private long							_uptime;
	private final String							_accountName;

	private Map<Integer, String>			_chars;

	/** The table containing all L2RecipeList of the L2PcInstance */
	private final Map<Integer, L2RecipeList>		_dwarvenRecipeBook		= new SingletonMap<Integer, L2RecipeList>();
	private final Map<Integer, L2RecipeList>		_commonRecipeBook		= new SingletonMap<Integer, L2RecipeList>();

	private int								_mountType;
	private int								_mountNpcId;
	private int 							_mountLevel;

	/** The current higher Expertise of the L2PcInstance (None=0, D=1, C=2, B=3, A=4, S=5, S80=6, S84=7)*/
	private int								_expertiseIndex;																	// Index in EXPERTISE_LEVELS
	private int								_expertisePenalty		= 0;

	private boolean							_isEnchanting			= false;
	private L2ItemInstance					_activeEnchantItem		= null;
	private L2ItemInstance					_activeEnchantSupportItem = null;
	private L2ItemInstance					_activeEnchantAttrItem	= null;
	private long							_activeEnchantTimestamp = 0;

	public static final byte ONLINE_STATE_LOADED = 0;
	public static final byte ONLINE_STATE_ONLINE = 1;
	public static final byte ONLINE_STATE_DELETED = 2;

	private byte _isOnline = ONLINE_STATE_LOADED;

	protected boolean						_inventoryDisabled		= false;

	protected Map<Integer, L2CubicInstance>	_cubics					= new SingletonMap<Integer, L2CubicInstance>().setShared();

	/** The L2NpcInstance corresponding to the last Folk wich one the player talked. */
	private L2Npc							_lastFolkNpc			= null;

	private int								_clanPrivileges			= 0;

	/** L2PcInstance's pledge class (knight, Baron, etc.)*/
	private int								_pledgeClass			= 0;

	/** Location before entering Observer Mode */
	private int								_obsX;
	private int								_obsY;
	private int								_obsZ;
	private boolean							_observerMode			= false;

	/** Total amount of damage dealt during a olympiad fight */
	private int								_olyDamage				= 0;

	/** TvT Instanced Engine parameters */
	public int								_originalNameColorTvTi, _originalKarmaTvTi, _countTvTiKills = 0, _countTvTITeamKills = 0;
	public boolean							_inEventTvTi			= false, _isSitForcedTvTi = false, _joiningTvTi = false;

	public int								_telemode				= 0;

	/** new loto ticket **/
	private final int								_loto[]					= new int[5];
	//public static int _loto_nums[] = {0,1,2,3,4,5,6,7,8,9,};
	/** new race ticket **/
	private final int								_race[]					= new int[2];

	private BlockList						_blockList;
	private L2FriendList					_friendList;

	private boolean							_fishing				= false;
	private int								_fishx					= 0;
	private int								_fishy					= 0;
	private int								_fishz					= 0;

	private final List<Integer>					_transformAllowedSkills				= new SingletonList<Integer>();

	private int								_team					= 0;
	private int								_wantsPeace				= 0;

	// Death Penalty Buff Level
	private int								_deathPenaltyBuffLevel	= 0;

	// Self resurrect during siege
	private boolean							_charmOfCourage			= false;

	private int								_trustlevel				= 0;

	private boolean							_hero					= false;
	private boolean							_noble					= false;
	private boolean							_inOlympiadMode			= false;
	private boolean							_olympiadStart			= false;
	private int								_olympiadGameId			= -1;
	private int								_olympiadSide			= -1;
	private int								_olympiadOpponentId		= 0;
	public int								olyBuff					= 0;

	/** Duel */
	private int								_duelState				= Duel.DUELSTATE_NODUEL;
	private boolean							_isInDuel				= false;
	private int								_duelId					= 0;
	private int								_noDuelReason			= 0;

	/** ally with ketra or varka related vars*/
	private int								_alliedVarkaKetra		= 0;

	/**
	 * IMO we don't need it, as we have FIFO packet execution.
	 */
	private final ReentrantLock _subclassLock = new ReentrantLock();
	/** The list of sub-classes this character has. */
	private Map<Integer, SubClass>			_subClasses;
	protected int							_baseClass;
	protected int							_activeClass;
	protected int							_classIndex				= 0;

	/** data for mounted pets */
	private int								_controlItemId;
	private L2PetData						_data;
	private int								_curFeed;
	protected Future<?>						_mountFeedTask;
	private ScheduledFuture<?>				_dismountTask;

	private long							_lastAccess;
	private int								_boatId;

	private ScheduledFuture<?>				_taskRentPet;
	private ScheduledFuture<?>				_taskWater;
	private L2BoatInstance					_boat;
	private Point3D							_inBoatPosition;

	/** Bypass validations */
	private List<String> _validBypass;
	private List<String> _validBypass2;

	private Forum							_forumMail;
	private Forum							_forumMemo;

	private L2Fishing						_fishCombat;

	/** Stored from last ValidatePosition **/
	private Point3D							_lastServerPosition;

	/** Previous coordinate sent to party in ValidatePosition **/
	private Point3D							_lastPartyPosition;

	/** The number of evaluation points obtained by this player */
	private int								_evalPoints;

	/** The number of evaluations this player can give */
	private int								_evaluations;

	/** List of players this player already evaluated */
	private final List<Integer>					_evaluated				= new SingletonList<Integer>();

	private boolean							_inCrystallize;

	private boolean							_inCraftMode;

	/** Current skill in use. Note that L2Character has _lastSkillCast, but this has the button presses */
	private SkillDat						_currentSkill;
	private SkillDat						_currentPetSkill;
	/** Skills queued because a skill is already in progress */
	private SkillDat						_queuedSkill;

	/** Store object used to summon the strider you are mounting **/
	private int								_mountObjectID			= 0;

	/** character VIP **/
	private boolean							_charViP				= false;

	private boolean							_inJail					= false;
	private long							_jailTimer				= 0;

	private boolean							_maried					= false;
	private int								_partnerId				= 0;
	private int								_coupleId				= 0;
	private boolean							_engagerequest			= false;
	private int								_engageid				= 0;
	private boolean							_maryrequest			= false;
	private boolean							_maryaccepted			= false;

	private int								_clientRevision			= 0;

	private FactionMember					_faction;

	/* Flag to disable equipment/skills while wearing formal wear **/
	private boolean							_IsWearingFormalWear	= false;

	private L2Transformation				_transformation;

	private int								_transformationId		= 0;

	private L2StaticObjectInstance			_throne;

	// Absorbed Souls
	private int								_souls					= 0;
	private ScheduledFuture<?>				_soulTask				= null;
	private int								_lastSoulConsume		= 0;

	// Force charges
	private int								_charges				= 0;
	private ScheduledFuture<?>				_chargeTask				= null;

	// WorldPosition used by TARGET_SIGNET_GROUND
	private Point3D							_currentSkillWorldPosition;

	public int								_fame = 0;					// The Fame of this L2PcInstance
	private ScheduledFuture<?>				_fameTask;

	/** Vitality Level of this L2PcInstance */
	private double 							_vitalityPoints = 1.0;
	private int								_vitalityLevel = 0;
	private ScheduledFuture<?>				_vitalityTask;

	// Id of the afro hair cut
	private int								_afroId					= 0;

	// Secret doors in Emerald
	private int								_openedSecretDoor		 = 0;
	private int								_positionWhenSecretDoorX = 0;
	private int								_positionWhenSecretDoorY = 0;
	private int								_positionWhenSecretDoorZ = 0;

	private class VitalityTask implements Runnable
	{
		private L2PcInstance	_player	= null;

		public VitalityTask(L2PcInstance activeChar)
		{
			_player = activeChar;
		}

		public void run()
		{
			if (_player == null)
				return;

			double vitalityAdd = Config.RATE_RECOVERY_VITALITY_TOWN_ZONE;
			if (_player.isInsideZone(L2Zone.FLAG_TOWN) && _player.getVitalityPoints() < 20000.0)
				_player.addVitalityPoints(vitalityAdd, true);
		}
	}

	/** Skill casting information (used to queue when several skills are cast in a short time) **/
	public class SkillDat
	{
		private final L2Skill	_skill;
		private final boolean	_ctrlPressed;
		private final boolean	_shiftPressed;

		protected SkillDat(L2Skill skill, boolean ctrlPressed, boolean shiftPressed)
		{
			_skill = skill;
			_ctrlPressed = ctrlPressed;
			_shiftPressed = shiftPressed;
		}

		public boolean isCtrlPressed()
		{
			return _ctrlPressed;
		}

		public boolean isShiftPressed()
		{
			return _shiftPressed;
		}

		public L2Skill getSkill()
		{
			return _skill;
		}

		public int getSkillId()
		{
			return (getSkill() != null) ? getSkill().getId() : -1;
		}
	}

	@Override
	public final boolean isAllSkillsDisabled()
	{
		return super.isAllSkillsDisabled() || isTryingToSitOrStandup();
	}

	@Override
	public final boolean isAttackingDisabled()
	{
		return super.isAttackingDisabled() || isTryingToSitOrStandup();
	}

	/** ShortBuff clearing Task */
	private ScheduledFuture<?>	_shortBuffTask	= null;

	private final class ShortBuffTask implements Runnable
	{
		public void run()
		{
			setShortBuffTaskSkillId(0);
			sendPacket(new ShortBuffStatusUpdate(0, 0, 0));
		}
	}

	/**
	 * Create a new L2PcInstance and add it in the characters table of the database.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Create a new L2PcInstance with an account name </li>
	 * <li>Set the name, the Hair Style, the Hair Color and  the Face type of the L2PcInstance</li>
	 * <li>Add the player in the characters table of the database</li><BR><BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 * @param template The L2PcTemplate to apply to the L2PcInstance
	 * @param accountName The name of the L2PcInstance
	 * @param name The name of the L2PcInstance
	 * @param hairStyle The hair style Identifier of the L2PcInstance
	 * @param hairColor The hair color Identifier of the L2PcInstance
	 * @param face The face type Identifier of the L2PcInstance
	 *
	 * @return The L2PcInstance added to the database or null
	 *
	 */
	public static L2PcInstance create(int objectId, L2PcTemplate template, String accountName, String name, byte hairStyle, byte hairColor, byte face,
			boolean sex)
	{
		// Create a new L2PcInstance with an account name
		PcAppearance app = new PcAppearance(face, hairColor, hairStyle, sex);
		L2PcInstance player = new L2PcInstance(objectId, template, accountName, app);

		// Set the name of the L2PcInstance
		player.setName(name);

		// Set the base class ID to that of the actual class ID.
		player.setBaseClass(player.getClassId());

		// Kept for backwards compabitility.
		player.setNewbie(1);

		// Add the player in the characters table of the database
		boolean ok = player.createDb();

		if (!ok)
			return null;

		return player;
	}

	public String getAccountName()
	{
		if (getClient() == null)
			return _accountName;
		return getClient().getAccountName();
	}

	public int getRelation(L2PcInstance target)
	{
		int result = 0;

		if (getClan() != null)
			result |= RelationChanged.RELATION_CLAN_MEMBER;

		if (isClanLeader())
			result |= RelationChanged.RELATION_LEADER;

		L2Party party = getParty();
		if (party != null && party == target.getParty())
		{
			result |= RelationChanged.RELATION_HAS_PARTY;

			switch (party.getPartyMembers().indexOf(this))
			{
				case 0:
					result |= RelationChanged.RELATION_PARTYLEADER; // 0x10
					break;
				case 1:
					result |= RelationChanged.RELATION_PARTY4; // 0x8
					break;
				case 2:
					result |= RelationChanged.RELATION_PARTY3+RelationChanged.RELATION_PARTY2+RelationChanged.RELATION_PARTY1; // 0x7
					break;
				case 3:
					result |= RelationChanged.RELATION_PARTY3+RelationChanged.RELATION_PARTY2; // 0x6
					break;
				case 4:
					result |= RelationChanged.RELATION_PARTY3+RelationChanged.RELATION_PARTY1; // 0x5
					break;
				case 5:
					result |= RelationChanged.RELATION_PARTY3; // 0x4
					break;
				case 6:
					result |= RelationChanged.RELATION_PARTY2+RelationChanged.RELATION_PARTY1; // 0x3
					break;
				case 7:
					result |= RelationChanged.RELATION_PARTY2; // 0x2
					break;
				case 8:
					result |= RelationChanged.RELATION_PARTY1; // 0x1
					break;
			}
		}

		if (getSiegeState() != 0)
		{
			result |= RelationChanged.RELATION_INSIEGE;
			if (getSiegeState() != target.getSiegeState())
				result |= RelationChanged.RELATION_ENEMY;
			else
				result |= RelationChanged.RELATION_ALLY;
			if (getSiegeState() == 1)
				result |= RelationChanged.RELATION_ATTACKER;
		}

		if (getClan() != null && target.getClan() != null)
		{
			if (target.getSubPledgeType() != L2Clan.SUBUNIT_ACADEMY && getSubPledgeType() != L2Clan.SUBUNIT_ACADEMY && target.getClan().isAtWarWith(getClan().getClanId()))
			{
				result |= RelationChanged.RELATION_1SIDED_WAR;
				if (getClan().isAtWarWith(target.getClan().getClanId()))
					result |= RelationChanged.RELATION_MUTUAL_WAR;
			}
		}
		return result;
	}

	public Map<Integer, String> getAccountChars()
	{
		if (_chars == null)
		{
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				
				// Retrieve the name and ID of the other characters assigned to this account.
				PreparedStatement statement = con.prepareStatement("SELECT charId, char_name FROM characters WHERE account_name=? AND charId<>?");
				statement.setString(1, getAccountName());
				statement.setInt(2, getObjectId());
				ResultSet rset = statement.executeQuery();
				
				while (rset.next())
				{
					if (_chars == null)
						_chars = new HashMap<Integer, String>();
					
					_chars.put(rset.getInt("charId"), rset.getString("char_name"));
				}
				
				rset.close();
				statement.close();
			}
			catch (SQLException e)
			{
				_log.warn("", e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
			
			if (_chars == null)
				_chars = L2Collections.emptyMap();
		}
		
		return _chars;
	}

	private void initPcStatusUpdateValues()
	{
		_cpUpdateInterval = getMaxCp() / 352.0;
		_cpUpdateIncCheck = getMaxCp();
		_cpUpdateDecCheck = getMaxCp() - _cpUpdateInterval;
		_mpUpdateInterval = getMaxMp() / 352.0;
		_mpUpdateIncCheck = getMaxMp();
		_mpUpdateDecCheck = getMaxMp() - _mpUpdateInterval;
	}

	/**
	 * Constructor of L2PcInstance (use L2Character constructor).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Call the L2Character constructor to create an empty _skills slot and copy basic Calculator set to this L2PcInstance </li>
	 * <li>Set the name of the L2PcInstance</li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method SET the level of the L2PcInstance to 1</B></FONT><BR><BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 * @param template The L2PcTemplate to apply to the L2PcInstance
	 * @param accountName The name of the account including this L2PcInstance
	 *
	 */
	private L2PcInstance(int objectId, L2PcTemplate template, String accountName, PcAppearance app)
	{
		super(objectId, template);
		getKnownList(); // Init knownlist
		getStat(); // Init stats
		getStatus(); // Init status
		super.initCharStatusUpdateValues();
		initPcStatusUpdateValues();

		_accountName = accountName;
		app.setOwner(this);
		_appearance = app;

		// Create an AI
		getAI();

		// Retrieve from the database all items of this L2PcInstance and add them to _inventory
		getInventory().restore();
		if (!Config.WAREHOUSE_CACHE)
			getWarehouse();
		getFreight();

		if (Config.ENABLE_VITALITY)
			startVitalityTask();
	}

	@Override
	protected CharKnownList initKnownList()
	{
		return new PcKnownList(this);
	}

	@Override
	public final PcKnownList getKnownList()
	{
		return (PcKnownList)_knownList;
	}

	@Override
	protected CharLikeView initView()
	{
		return new PcView(this);
	}

	@Override
	public PcView getView()
	{
		return (PcView)_view;
	}

	@Override
	protected CharStat initStat()
	{
		return new PcStat(this);
	}

	@Override
	public final PcStat getStat()
	{
		return (PcStat)_stat;
	}

	@Override
	protected CharStatus initStatus()
	{
		return new PcStatus(this);
	}

	@Override
	public final PcStatus getStatus()
	{
		return (PcStatus)_status;
	}

	public final PcAppearance getAppearance()
	{
		return _appearance;
	}
	
	@Override
	public void setTitle(String value)
	{
		if (Config.FACTION_ENABLED)
		{
			if (FactionManager.getInstance().getFactionTitles().contains(value.toLowerCase()) && !value.isEmpty())
			{
				sendMessage("Title protected by Faction System");
				return;
			}
		}
		
		if (value.length() > 16)
			value = value.substring(0, 15);
		
		super.setTitle(value);
	}

	/**
	 * Return the base L2PcTemplate link to the L2PcInstance.<BR><BR>
	 */
	public final L2PcTemplate getBaseTemplate()
	{
		return CharTemplateTable.getInstance().getTemplate(_baseClass);
	}

	/** Return the L2PcTemplate link to the L2PcInstance. */
	@Override
	public final L2PcTemplate getTemplate()
	{
		return (L2PcTemplate) super.getTemplate();
	}

	public void setTemplate(ClassId newclass)
	{
		super.setTemplate(CharTemplateTable.getInstance().getTemplate(newclass));
	}

	@Override
	protected L2CharacterAI initAI()
	{
		return new L2PlayerAI(new L2PcInstance.AIAccessor());
	}

	/** Return the Level of the L2PcInstance. */
	@Override
	public final int getLevel()
	{
		return getStat().getLevel();
	}

	/**
	 * Return the _newbie rewards state of the L2PcInstance.<BR><BR>
	 */
	public int getNewbie()
	{
		return _newbie;
	}

	/**
	 * Set the _newbie rewards state of the L2PcInstance.<BR><BR>
	 *
	 * @param newbieRewards The Identifier of the _newbie state<BR><BR>
	 *
	 */
	public void setNewbie(int newbieRewards)
	{
		_newbie = newbieRewards;
	}

	public void setBaseClass(int baseClass)
	{
		_baseClass = baseClass;
	}

	public void setBaseClass(ClassId classId)
	{
		_baseClass = classId.ordinal();
	}

	public boolean isInStoreMode()
	{
		return (getPrivateStoreType() > 0);
	}

	public boolean isInCraftMode()
	{
		return _inCraftMode;
	}

	public void isInCraftMode(boolean b)
	{
		_inCraftMode = b;
	}

	/**
	 * Return a table containing all Common L2Recipe of the L2PcInstance.<BR><BR>
	 */
	public L2RecipeList[] getCommonRecipeBook()
	{
		return _commonRecipeBook.values().toArray(new L2RecipeList[_commonRecipeBook.values().size()]);
	}

	/**
	 * Return a table containing all Dwarf L2Recipe of the L2PcInstance.<BR><BR>
	 */
	public L2RecipeList[] getDwarvenRecipeBook()
	{
		return _dwarvenRecipeBook.values().toArray(new L2RecipeList[_dwarvenRecipeBook.values().size()]);
	}

	/**
	 * Add a new L2Recipe to the table _commonrecipebook containing all L2Recipe of the L2PcInstance <BR><BR>
	 *
	 * @param recipe The L2RecipeList to add to the _recipebook
	 * @param saveToDb true to save infos into the DB
	 */
	public void registerCommonRecipeList(L2RecipeList recipe, boolean saveToDb)
	{
		_commonRecipeBook.put(recipe.getId(), recipe);

		if (saveToDb)
			insertNewRecipeData(recipe.getId(), false);
	}

	/**
	 * Add a new L2Recipe to the table _recipebook containing all L2Recipe of the L2PcInstance <BR><BR>
	 *
	 * @param recipe The L2Recipe to add to the _recipebook
	 * @param saveToDb true to save infos into the DB
	 */
	public void registerDwarvenRecipeList(L2RecipeList recipe, boolean saveToDb)
	{
		_dwarvenRecipeBook.put(recipe.getId(), recipe);

		if (saveToDb)
			insertNewRecipeData(recipe.getId(), true);
	}

	/**
	 * @param recipeId The Identifier of the L2Recipe to check in the player's recipe books
	 *
	 * @return
	 * <b>TRUE</b> if player has the recipe on Common or Dwarven Recipe book else returns <b>FALSE</b>
	 */
	public boolean hasRecipeList(int recipeId)
	{
		if (_dwarvenRecipeBook.containsKey(recipeId))
			return true;
		else return _commonRecipeBook.containsKey(recipeId);
	}

	/**
	 * Tries to remove a L2Recipe from the table _DwarvenRecipeBook or from table _CommonRecipeBook, those table contain all L2Recipe of the L2PcInstance <BR><BR>
	 *
	 * @param recipeId The Identifier of the L2Recipe to remove from the _recipebook
	 *
	 */
	public void unregisterRecipeList(int recipeId)
	{
		if (_dwarvenRecipeBook.remove(recipeId) != null)
			deleteRecipeData(recipeId, true);
		else if (_commonRecipeBook.remove(recipeId) != null)
			deleteRecipeData(recipeId, false);
		else
			_log.warn("Attempted to remove unknown RecipeList: " + recipeId);

		L2ShortCut[] allShortCuts = getAllShortCuts();
		for (L2ShortCut sc : allShortCuts)
		{
			if (sc != null && sc.getId() == recipeId && sc.getType() == L2ShortCut.TYPE_RECIPE)
				deleteShortCut(sc.getSlot(), sc.getPage());
		}
	}

	private void insertNewRecipeData(int recipeId, boolean isDwarf)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO character_recipebook (charId, id, classIndex, type) values(?,?,?,?)");
			statement.setInt(1, getObjectId());
			statement.setInt(2,recipeId);
			statement.setInt(3, isDwarf ? _classIndex : 0);
			statement.setInt(4, isDwarf ? 1 : 0);
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.error("SQL exception while inserting recipe: "+recipeId+" from character "+getObjectId(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	private void deleteRecipeData(int recipeId, boolean isDwarf)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM character_recipebook WHERE charId=? AND id=? AND classIndex=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, recipeId);
			statement.setInt(3, isDwarf ? _classIndex : 0);
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.error("SQL exception while deleting recipe: "+recipeId+" from character "+getObjectId(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * Returns the Id for the last talked quest NPC.<BR><BR>
	 */
	public int getLastQuestNpcObject()
	{
		return _questNpcObject;
	}

	public void setLastQuestNpcObject(int npcId)
	{
		_questNpcObject = npcId;
	}

	/**
	 * Return the QuestState object corresponding to the quest name.<BR><BR>
	 *
	 * @param quest The name of the quest
	 *
	 */
	public QuestState getQuestState(String quest)
	{
		return _quests.get(quest);
	}

	/**
	 * Add a QuestState to the table _quest containing all quests began by the L2PcInstance.<BR><BR>
	 *
	 * @param qs The QuestState to add to _quest
	 *
	 */
	public void setQuestState(QuestState qs)
	{
		_quests.put(qs.getQuestName(), qs);
	}

	/**
	 * Remove a QuestState from the table _quest containing all quests began by the L2PcInstance.<BR><BR>
	 *
	 * @param quest The name of the quest
	 *
	 */
	public void delQuestState(String quest)
	{
		_quests.remove(quest);
	}

	private QuestState[] addToQuestStateArray(QuestState[] questStateArray, QuestState state)
	{
		int len = questStateArray.length;
		QuestState[] tmp = new QuestState[len + 1];
        System.arraycopy(questStateArray, 0, tmp, 0, len);
		tmp[len] = state;
		return tmp;
	}

	/**
	 * Return a table containing all Quest in progress from the table _quests.<BR><BR>
	 */
	public Quest[] getAllActiveQuests()
	{
		LinkedBunch<Quest> quests = new LinkedBunch<Quest>();

		for (QuestState qs : _quests.values())
		{
			if (qs == null)
				continue;

			int questId = qs.getQuest().getQuestIntId();
			if ((questId > 19999) || (questId < 1))
				continue;

			if (!qs.isStarted() && !Config.DEVELOPER)
				continue;

			quests.add(qs.getQuest());
		}

		return quests.moveToArray(new Quest[quests.size()]);
	}

	/**
	 * Return a table containing all QuestState to modify after a L2Attackable killing.<BR><BR>
	 *
	 * @param npc The Identifier of the L2Attackable attacked
	 *
	 */
	public QuestState[] getQuestsForAttacks(L2NpcInstance npc)
	{
		// Create a QuestState table that will contain all QuestState to modify
		QuestState[] states = null;

		// Go through the QuestState of the L2PcInstance quests
		for (Quest quest : npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_ATTACK))
		{
			// Check if the Identifier of the L2Attackable attck is needed for the current quest
			if (getQuestState(quest.getName()) != null)
			{
				// Copy the current L2PcInstance QuestState in the QuestState table
				if (states == null)
					states = new QuestState[]
					{ getQuestState(quest.getName()) };
				else
					states = addToQuestStateArray(states, getQuestState(quest.getName()));
			}
		}

		// Return a table containing all QuestState to modify
		return states;
	}

	/**
	 * Return a table containing all QuestState to modify after a L2Attackable killing.<BR><BR>
	 *
	 * @param npc The Identifier of the L2Attackable killed
	 *
	 */
	public QuestState[] getQuestsForKills(L2NpcInstance npc)
	{
		// Create a QuestState table that will contain all QuestState to modify
		QuestState[] states = null;

		// Go through the QuestState of the L2PcInstance quests
		for (Quest quest : npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_KILL))
		{
			// Check if the Identifier of the L2Attackable killed is needed for the current quest
			if (getQuestState(quest.getName()) != null)
			{
				// Copy the current L2PcInstance QuestState in the QuestState table
				if (states == null)
					states = new QuestState[]
					{ getQuestState(quest.getName()) };
				else
					states = addToQuestStateArray(states, getQuestState(quest.getName()));
			}
		}

		// Return a table containing all QuestState to modify
		return states;
	}

	/**
	 * Return a table containing all QuestState from the table _quests in which the L2PcInstance must talk to the NPC.<BR><BR>
	 *
	 * @param npcId The Identifier of the NPC
	 *
	 */
	public QuestState[] getQuestsForTalk(int npcId)
	{
		// Create a QuestState table that will contain all QuestState to modify
		QuestState[] states = null;

		// Go through the QuestState of the L2PcInstance quests
		Quest[] quests = NpcTable.getInstance().getTemplate(npcId).getEventQuests(Quest.QuestEventType.ON_TALK);
		if (quests != null)
		{
			for (Quest quest : quests)
			{
				// Copy the current L2PcInstance QuestState in the QuestState table
				if (quest != null)
				{
					// Copy the current L2PcInstance QuestState in the QuestState table
					if (getQuestState(quest.getName()) != null)
					{
						if (states == null)
							states = new QuestState[]
							{ getQuestState(quest.getName()) };
						else
							states = addToQuestStateArray(states, getQuestState(quest.getName()));
					}
				}
			}
		}

		// Return a table containing all QuestState to modify
		return states;
	}

	public QuestState processQuestEvent(String quest, String event)
	{
		QuestState retval = null;
		if (event == null)
			event = "";
		if (!_quests.containsKey(quest))
			return retval;
		QuestState qs = getQuestState(quest);
		if (qs == null && event.length() == 0)
			return retval;
		if (qs == null)
		{
			Quest q = QuestManager.getInstance().getQuest(quest);
			if (q == null)
				return retval;
			qs = q.newQuestState(this);
		}
		if (qs != null)
		{
			if (getLastQuestNpcObject() > 0)
			{
				L2Object object = getKnownList().getKnownObject(getLastQuestNpcObject());
				if (object instanceof L2Npc && isInsideRadius(object, L2Npc.INTERACTION_DISTANCE, false, false))
				{
					L2Npc npc = (L2Npc) object;
					QuestState[] states = getQuestsForTalk(npc.getNpcId());

					if (states != null)
					{
						for (QuestState state : states)
						{
							if ((state.getQuest().getQuestIntId() == qs.getQuest().getQuestIntId()) && !qs.isCompleted())
							{
								if (qs.getQuest().notifyEvent(event, npc, this))
									showQuestWindow(quest, State.getStateName(qs.getState()));

								retval = qs;
							}
						}
						sendPacket(new QuestList(this));
					}
				}
			}
		}

		return retval;
	}

	/**
	 * FIXME: move this from L2PcInstance, there is no reason to have this here
	 * @param questId
	 * @param stateId
	 */
	private void showQuestWindow(String questId, String stateId)
	{
		String path = "data/scripts/quests/" + questId + "/" + stateId + ".htm";
		String content = HtmCache.getInstance().getHtm(path);

		if (content != null)
		{
			if (_log.isDebugEnabled())
				_log.debug("Showing quest window for quest " + questId + " state " + stateId + " html path: " + path);

			NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
			npcReply.setHtml(content);
			sendPacket(npcReply);
		}

		sendPacket(ActionFailed.STATIC_PACKET);
	}

	/** List of all QuestState instance that needs to be notified of this L2PcInstance's or its pet's death */
	private final List<QuestState> _NotifyQuestOfDeathList = new SingletonList<QuestState>();

	/**
	 * Add QuestState instance that is to be notified of L2PcInstance's death.<BR>
	 * <BR>
	 *
	 * @param qs The QuestState that subscribe to this event
	 */
	public void addNotifyQuestOfDeath(QuestState qs)
	{
		if (qs == null || _NotifyQuestOfDeathList.contains(qs))
			return;

		_NotifyQuestOfDeathList.add(qs);
	}

	/**
	 * Remove QuestState instance that is to be notified of L2PcInstance's death.<BR>
	 * <BR>
	 *
	 * @param qs The QuestState that subscribe to this event
	 */
	public void removeNotifyQuestOfDeath(QuestState qs)
	{
		if (qs == null || !_NotifyQuestOfDeathList.contains(qs))
			return;

		_NotifyQuestOfDeathList.remove(qs);
	}

	/**
	 * Return a list of QuestStates which registered for notify of death of this L2PcInstance.<BR>
	 * <BR>
	 */
	public final List<QuestState> getNotifyQuestOfDeath()
	{
		return _NotifyQuestOfDeathList;
	}

	private ShortCuts getShortCuts()
	{
		if (_shortCuts == null)
			_shortCuts = new ShortCuts(this);

		return _shortCuts;
	}

	/**
	 * Return a table containing all L2ShortCut of the L2PcInstance.<BR><BR>
	 */
	public L2ShortCut[] getAllShortCuts()
	{
		return getShortCuts().getAllShortCuts();
	}

	/**
	 * Add a L2shortCut to the L2PcInstance shortCuts<BR><BR>
	 */
	public void registerShortCut(L2ShortCut shortcut)
	{
		getShortCuts().registerShortCut(shortcut);
	}

	/**
	 * Delete the L2ShortCut corresponding to the position (page-slot) from the L2PcInstance shortCuts.<BR><BR>
	 */
	public void deleteShortCut(int slot, int page)
	{
		getShortCuts().deleteShortCut(slot, page);
	}

	/**
	 * Add a L2Macro to the L2PcInstance macroses<BR><BR>
	 */
	public void registerMacro(L2Macro macro)
	{
		getMacroses().registerMacro(macro);
	}

	/**
	 * Delete the L2Macro corresponding to the Identifier from the L2PcInstance macroses.<BR><BR>
	 */
	public void deleteMacro(int id)
	{
		getMacroses().deleteMacro(id);
	}

	/**
	 * Return all L2Macro of the L2PcInstance.<BR><BR>
	 */
	public MacroList getMacroses()
	{
		if (_macroses == null)
			_macroses = new MacroList(this);

		return _macroses;
	}

	/**
	 * Set the siege state of the L2PcInstance.<BR><BR>
	 * 1 = attacker, 2 = defender, 0 = not involved
	 */
	public void setSiegeState(byte siegeState)
	{
		_siegeState = siegeState;
		broadcastRelationChanged();
	}

	/**
	 * Get the siege state of the L2PcInstance.<BR><BR>
	 * 1 = attacker, 2 = defender, 0 = not involved
	 */
	public byte getSiegeState()
	{
		return _siegeState;
	}

	@Override
	public boolean revalidateZone(boolean force)
	{
		if (!super.revalidateZone(force))
			return false;

		if (Config.ALLOW_WATER)
			checkWaterState();

		if (isInsideZone(L2Zone.FLAG_SIEGE))
		{
			setLastCompassZone(ExSetCompassZoneCode.SIEGEWARZONE2);
		}
		else if (isInsideZone(L2Zone.FLAG_PVP))
		{
			setLastCompassZone(ExSetCompassZoneCode.PVPZONE);
		}
		else if (isIn7sDungeon())
		{
			setLastCompassZone(ExSetCompassZoneCode.SEVENSIGNSZONE);
		}
		else if (isInsideZone(L2Zone.FLAG_PEACE))
		{
			setLastCompassZone(ExSetCompassZoneCode.PEACEZONE);
		}
		else
		{
			if (_lastCompassZone == ExSetCompassZoneCode.SIEGEWARZONE2)
				updatePvPStatus();

			setLastCompassZone(ExSetCompassZoneCode.GENERALZONE);
		}

		return true;
	}

	private void setLastCompassZone(int newCompassZone)
	{
		if (_lastCompassZone == newCompassZone)
			return;

		_lastCompassZone = newCompassZone;
		sendPacket(new ExSetCompassZoneCode(newCompassZone));
	}

	/**
	 * Return True if the L2PcInstance can Craft Dwarven Recipes.<BR><BR>
	 */
	public boolean hasDwarvenCraft()
	{
		return getSkillLevel(L2Skill.SKILL_CREATE_DWARVEN) >= 1;
	}

	public int getDwarvenCraft()
	{
		return getSkillLevel(L2Skill.SKILL_CREATE_DWARVEN);
	}

	/**
	 * Return True if the L2PcInstance can Craft Dwarven Recipes.<BR><BR>
	 */
	public boolean hasCommonCraft()
	{
		return getSkillLevel(L2Skill.SKILL_CREATE_COMMON) >= 1;
	}

	public int getCommonCraft()
	{
		return getSkillLevel(L2Skill.SKILL_CREATE_COMMON);
	}

	/**
	 * Return the PK counter of the L2PcInstance.<BR><BR>
	 */
	public int getPkKills()
	{
		return _pkKills;
	}

	/**
	 * Set the PK counter of the L2PcInstance.<BR><BR>
	 */
	public void setPkKills(int pkKills)
	{
		_pkKills = pkKills;
	}

	/**
	 * Return the _deleteTimer of the L2PcInstance.<BR><BR>
	 */
	public long getDeleteTimer()
	{
		return _deleteTimer;
	}

	/**
	 * Set the _deleteTimer of the L2PcInstance.<BR><BR>
	 */
	public void setDeleteTimer(long deleteTimer)
	{
		_deleteTimer = deleteTimer;
	}

	/**
	 * Return the current weight of the L2PcInstance.<BR><BR>
	 */
	@Override
	public int getCurrentLoad()
	{
		return getInventory().getTotalWeight();
	}

	/** @return the number of evaluation points obtained by player. */
	public int getEvalPoints()
	{
		return _evalPoints;
	}

	/**
	 * Set the number of evaluation points obtained by player.
	 * @param value Evaluation point count
	 */
	public void setEvalPoints(int value)
	{
		_evalPoints = value;
	}

	/** @return the number of evaluations this player can give away. */
	public int getEvaluations()
	{
		return _evaluations;
	}

	/**
	 * Set the number of available evaluations.
	 * @param value New available evaluation count
	 */
	public void setEvaluationCount(int value)
	{
		_evaluations = value;
	}

	/**
	 * Add a player that has been evaluated by this player.
	 * @param charId evaluated player's ID
	 */
	public void addEvalRestriction(int charId)
	{
		_evaluated.add(charId);
	}

	/**	Removes all session evaluation restrictions for this player. */
	public void cleanEvalRestrictions()
	{
		_evaluated.clear();
	}

	/**
	 * @param target Player being evaluated
	 * @return whether this player hasn't evaluated the given player
	 */
	public boolean canEvaluate(L2PcInstance target)
	{
		return !_evaluated.contains(target.getObjectId());
	}

	/**
	 * Set the exp of the L2PcInstance before a death
	 * @param exp
	 */
	public void setExpBeforeDeath(long exp)
	{
		_expBeforeDeath = exp;
	}

	public long getExpBeforeDeath()
	{
		return _expBeforeDeath;
	}

	/**
	 * Return the Karma of the L2PcInstance.<BR><BR>
	 */
	public int getKarma()
	{
		return _karma;
	}

	/**
	 * Set the Karma of the L2PcInstance and send a Server->Client packet StatusUpdate (broadcast).<BR><BR>
	 */
	public void setKarma(int karma)
	{
		if (karma < 0)
			karma = 0;
		if (_karma == 0 && karma > 0)
		{
			for (L2Object object : getKnownList().getKnownObjects().values())
			{
				if (!(object instanceof L2GuardInstance))
					continue;

				if (((L2GuardInstance) object).getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
					((L2GuardInstance) object).getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
			}
		}
		else if (_karma > 0 && karma == 0)
		{
			// Send a Server->Client StatusUpdate packet with Karma and PvP Flag to the L2PcInstance and all L2PcInstance to inform (broadcast)
			setKarmaFlag(0);
		}
		_karma = karma;
		broadcastKarma();
		RegionBBSManager.changeCommunityBoard(this, PlayerStateOnCommunity.KARMA_OWNER);
	}

	/**
	 * Return the max weight that the L2PcInstance can load.<BR><BR>
	 */
	@Override
	public int getMaxLoad()
	{
		return (int)(calcStat(Stats.MAX_LOAD, 69000, this, null) * Config.ALT_WEIGHT_LIMIT);
	}

	public int getExpertisePenalty()
	{
		return _expertisePenalty;
	}

	@Override
	public int getWeightPenalty()
	{
		return _curWeightPenalty;
	}

	@Override
	public void setWeightPenalty(int value)
	{
		_curWeightPenalty = value;
	}

	public void refreshExpertisePenalty()
	{
		if (Config.ALT_GRADE_PENALTY)
		{
			int newPenalty = 0;

			for (L2ItemInstance item : getInventory().getItems())
			{
				if (item.isEquipped() && item.getItem() != null)
				{
					int crystaltype = item.getItem().getCrystalType();

					if (crystaltype > newPenalty)
						newPenalty = crystaltype;
				}
			}

			newPenalty = newPenalty - getExpertiseIndex();
			if (newPenalty <= 0)
				newPenalty = 0;

			if (getExpertisePenalty() != newPenalty || hasSkill(4267) != (newPenalty > 0))
			{
				_expertisePenalty = newPenalty;

				if (newPenalty > 0)
					super.addSkill(SkillTable.getInstance().getInfo(4267, 1));
				else
					super.removeSkill(getKnownSkill(4267));

				sendEtcStatusUpdate();
			}
		}
	}

	/**
	 * Return the the PvP Kills of the L2PcInstance (Number of player killed during a PvP).<BR><BR>
	 */
	public int getPvpKills()
	{
		return _pvpKills;
	}

	/**
	 * Set the the PvP Kills of the L2PcInstance (Number of player killed during a PvP).<BR><BR>
	 */
	public void setPvpKills(int pvpKills)
	{
		_pvpKills = pvpKills;
	}

	/**
	 * Return the ClassId object of the L2PcInstance contained in L2PcTemplate.<BR><BR>
	 */
	public ClassId getClassId()
	{
		return getTemplate().getClassId();
	}

	public void academyCheck(int Id)
	{
		if ((getSubPledgeType() == -1 || getLvlJoinedAcademy() != 0) && _clan != null && ClassId.values()[Id].getLevel() == ClassLevel.Third)
		{
			if (getLvlJoinedAcademy() <= 16)
				_clan.setReputationScore(_clan.getReputationScore() + Config.JOIN_ACADEMY_MAX_REP_SCORE, true);
			else if (getLvlJoinedAcademy() >= 39)
				_clan.setReputationScore(_clan.getReputationScore() + Config.JOIN_ACADEMY_MIN_REP_SCORE, true);
			else
				_clan.setReputationScore(_clan.getReputationScore() + (Config.JOIN_ACADEMY_MAX_REP_SCORE - (getLvlJoinedAcademy() - 16) * 20), true);
			setLvlJoinedAcademy(0);

			// Oust pledge member from the academy, cuz he has finished his 2nd class transfer
			SystemMessage msg = new SystemMessage(SystemMessageId.CLAN_MEMBER_S1_EXPELLED);
			msg.addString(getName());
			_clan.broadcastToOnlineMembers(msg);
			_clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(getName()));

			_clan.removeClanMember(getObjectId(), 0);
			sendPacket(SystemMessageId.ACADEMY_MEMBERSHIP_TERMINATED);
			// Receive graduation gift
			getInventory().addItem("Gift", 8181, 1, this, null); // Give academy circlet
		}
	}

	/**
	 * Set the template of the L2PcInstance.<BR><BR>
	 *
	 * @param Id The Identifier of the L2PcTemplate to set to the L2PcInstance
	 *
	 */
	public void setClassId(int Id)
	{
		if (!_subclassLock.tryLock())
			return;
		
		try
		{
			academyCheck(Id);
			
			if (isSubClassActive())
			{
				getSubClasses().get(_classIndex).setClassId(Id);
			}
			setClassTemplate(Id);
			
			setTarget(this);
			// Animation: Production - Clan / Transfer
			MagicSkillUse msu = new MagicSkillUse(this, this, 5103, 1, 1196, 0);
			broadcastPacket(msu);
			
			// Update class icon in party and clan
			broadcastClassIcon();
			
			rewardSkills();
		}
		finally
		{
			_subclassLock.unlock();
		}
	}

	public void checkSSMatch(L2ItemInstance equipped, L2ItemInstance unequipped)
	{
		if (unequipped == null || unequipped.getItem().getType2() != L2Item.TYPE2_WEAPON)
			return;

		// Keep shots with S80/84 weapons
		if (equipped != null && equipped.getItem().getCrystalGrade() == unequipped.getItem().getCrystalGrade())
			return;

		for (L2ItemInstance item : getInventory().getItems())
			if (ShotTable.isPcShot(item.getItemId()))
				if (item.getItem().getCrystalType() == unequipped.getItem().getCrystalType())
					getShots().removeAutoSoulShot(item.getItemId());
	}

	public void useEquippableItem(L2ItemInstance item, boolean abortAttack)
	{
		// Equip or unEquip
		L2ItemInstance[] items = null;
		final boolean isEquiped = item.isEquipped();
		final int oldInvLimit = getInventoryLimit();
		SystemMessage sm = null;
		L2ItemInstance old = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LRHAND);
		if (old == null)
			old = getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);

		checkSSMatch(item, old);

		int bodyPart = item.getItem().getBodyPart();
		if (isEquiped)
		{
			if (item.getEnchantLevel() > 0)
			{
				sm = new SystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
				sm.addNumber(item.getEnchantLevel());
				sm.addItemName(item);
			}
			else
			{
				sm = new SystemMessage(SystemMessageId.S1_DISARMED);
				sm.addItemName(item);
			}
			sendPacket(sm);

			switch (bodyPart)
			{
				case L2Item.SLOT_L_EAR:
				case L2Item.SLOT_LR_EAR:
				case L2Item.SLOT_L_FINGER:
				case L2Item.SLOT_LR_FINGER:
				case L2Item.SLOT_DECO:
				{
					getInventory().setPaperdollItem(item.getLocationSlot(), null);
					sendPacket(new ItemList(this, false));
				}
			}

			// We cant unequip talisman by body slot
			if (bodyPart == L2Item.SLOT_DECO)
				items = getInventory().unEquipItemInSlotAndRecord(item.getLocationSlot());
			else
				items = getInventory().unEquipItemInBodySlotAndRecord(bodyPart);
		}
		else
		{
			L2ItemInstance tempItem = getInventory().getPaperdollItemByL2ItemId(bodyPart);

			// Check if the item replaces a wear-item
			if (tempItem != null && tempItem.isWear())
			{
				// Dont allow an item to replace a wear-item
				return;
			}
			else if (bodyPart == L2Item.SLOT_LR_HAND)
			{
				// This may not remove left OR right hand equipment
				tempItem = getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
				if (tempItem != null && tempItem.isWear()) return;

				tempItem = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
				if (tempItem != null && tempItem.isWear()) return;
			}
			else if (bodyPart == L2Item.SLOT_FULL_ARMOR)
			{
				// This may not remove chest or leggins
				tempItem = getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
				if (tempItem != null && tempItem.isWear()) return;

				tempItem = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS);
				if (tempItem != null && tempItem.isWear()) return;
			}

			if (item.getEnchantLevel() > 0)
			{
				sm = new SystemMessage(SystemMessageId.S1_S2_EQUIPPED);
				sm.addNumber(item.getEnchantLevel());
				sm.addItemName(item);
			}
			else
			{
				sm = new SystemMessage(SystemMessageId.S1_EQUIPPED);
				sm.addItemName(item);
			}
			sendPacket(sm);

			//must be sent before InventoryUpdate
			if ((bodyPart & L2Item.SLOT_HEAD) > 0 || (bodyPart & L2Item.SLOT_NECK) > 0
					|| (bodyPart & L2Item.SLOT_L_EAR) > 0 || (bodyPart & L2Item.SLOT_R_EAR) > 0
					|| (bodyPart & L2Item.SLOT_L_FINGER) > 0 || (bodyPart & L2Item.SLOT_R_FINGER) > 0
					|| (bodyPart & L2Item.SLOT_R_BRACELET) > 0 || (bodyPart & L2Item.SLOT_L_BRACELET) > 0
					||(bodyPart & L2Item.SLOT_DECO) > 0)
				sendPacket(new UserInfo(this));

			items = getInventory().equipItemAndRecord(item);

			// Consume mana - will start a task if required; returns if item is not a shadow item
			item.decreaseMana(false);
		}
		sm = null;

		refreshExpertisePenalty();

		InventoryUpdate iu = new InventoryUpdate();
		iu.addEquipItems(items);
		sendPacket(iu);
		if (abortAttack)
			abortAttack();

		broadcastUserInfo();

		if (getInventoryLimit() != oldInvLimit)
			sendPacket(new ExStorageMaxCount(this));
	}

	/** Return the Experience of the L2PcInstance. */
	public long getExp()
	{
		return getStat().getExp();
	}

	public void setActiveEnchantItem(L2ItemInstance scroll)
	{
		// If we dont have a Enchant Item, we are not enchanting.
		if (scroll == null)
		{
			setActiveEnchantSupportItem(null);
			setActiveEnchantTimestamp(0);
			setIsEnchanting(false);
		}

		_activeEnchantItem = scroll;
	}

	public L2ItemInstance getActiveEnchantItem()
	{
		return _activeEnchantItem;
	}

	public void setActiveEnchantSupportItem(L2ItemInstance item)
	{
		_activeEnchantSupportItem = item;
	}

	public L2ItemInstance getActiveEnchantSupportItem()
	{
		return _activeEnchantSupportItem;
	}

	public long getActiveEnchantTimestamp()
	{
		return _activeEnchantTimestamp;
	}

	public void setActiveEnchantTimestamp(long val)
	{
		_activeEnchantTimestamp = val;
	}

	public void setActiveEnchantAttrItem(L2ItemInstance stone)
	{
		_activeEnchantAttrItem = stone;
	}

	public L2ItemInstance getActiveEnchantAttrItem()
	{
		return _activeEnchantAttrItem;
	}

	public void setIsEnchanting(boolean val)
	{
		_isEnchanting = val;
	}

	public boolean isEnchanting()
	{
		return _isEnchanting;
	}

	/**
	 * Set the fists weapon of the L2PcInstance (used when no weapon is equipped).<BR><BR>
	 *
	 * @param weaponItem The fists L2Weapon to set to the L2PcInstance
	 *
	 */
	public void setFistsWeaponItem(L2Weapon weaponItem)
	{
		_fistsWeaponItem = weaponItem;
	}

	/**
	 * Return the fists weapon of the L2PcInstance (used when no weapon is equipped).<BR><BR>
	 */
	public L2Weapon getFistsWeaponItem()
	{
		return _fistsWeaponItem;
	}

	/**
	 * Return the fists weapon of the L2PcInstance Class (used when no weapon is equipped).<BR><BR>
	 */
	public L2Weapon findFistsWeaponItem(int classId)
	{
		L2Weapon weaponItem = null;
		if ((classId >= 0x00) && (classId <= 0x09))
		{
			// Human fighter fists
			L2Item temp = ItemTable.getInstance().getTemplate(246);
			weaponItem = (L2Weapon) temp;
		}
		else if ((classId >= 0x0a) && (classId <= 0x11))
		{
			// Human mage fists
			L2Item temp = ItemTable.getInstance().getTemplate(251);
			weaponItem = (L2Weapon) temp;
		}
		else if ((classId >= 0x12) && (classId <= 0x18))
		{
			// Elven fighter fists
			L2Item temp = ItemTable.getInstance().getTemplate(244);
			weaponItem = (L2Weapon) temp;
		}
		else if ((classId >= 0x19) && (classId <= 0x1e))
		{
			// Elven mage fists
			L2Item temp = ItemTable.getInstance().getTemplate(249);
			weaponItem = (L2Weapon) temp;
		}
		else if ((classId >= 0x1f) && (classId <= 0x25))
		{
			// Dark elven fighter fists
			L2Item temp = ItemTable.getInstance().getTemplate(245);
			weaponItem = (L2Weapon) temp;
		}
		else if ((classId >= 0x26) && (classId <= 0x2b))
		{
			// Dark elven mage fists
			L2Item temp = ItemTable.getInstance().getTemplate(250);
			weaponItem = (L2Weapon) temp;
		}
		else if ((classId >= 0x2c) && (classId <= 0x30))
		{
			// Orc fighter fists
			L2Item temp = ItemTable.getInstance().getTemplate(248);
			weaponItem = (L2Weapon) temp;
		}
		else if ((classId >= 0x31) && (classId <= 0x34))
		{
			// Orc mage fists
			L2Item temp = ItemTable.getInstance().getTemplate(252);
			weaponItem = (L2Weapon) temp;
		}
		else if ((classId >= 0x35) && (classId <= 0x39))
		{
			// Dwarven fists
			L2Item temp = ItemTable.getInstance().getTemplate(247);
			weaponItem = (L2Weapon) temp;
		}

		return weaponItem;
	}

	/**
	 * Give Expertise skill of this level and remove beginner Lucky skill.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get the Level of the L2PcInstance </li>
	 * <li>If L2PcInstance Level is 5, remove beginner Lucky skill </li>
	 * <li>Add the Expertise skill corresponding to its Expertise level</li>
	 * <li>Update the overloaded status of the L2PcInstance</li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T give other free skills (SP needed = 0)</B></FONT><BR><BR>
	 *
	 */
	public void rewardSkills()
	{
		// Get the Level of the L2PcInstance
		int lvl = getLevel();

		// Remove beginner Lucky skill
		if (lvl > 9)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(194, 1);
			skill = removeSkill(skill);

			if (_log.isDebugEnabled() && skill != null)
				_log.debug("removed skill 'Lucky' from " + getName());
		}

		// Calculate the current higher Expertise of the L2PcInstance
		for (int i = 0; i < EXPERTISE_LEVELS.length; i++)
		{
			if (lvl >= EXPERTISE_LEVELS[i])
				setExpertiseIndex(i);
		}

		// Add the Expertise skill corresponding to its Expertise level
		if (getExpertiseIndex() > 0)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(239, getExpertiseIndex());
			addSkill(skill);

			if (_log.isDebugEnabled())
				_log.debug("awarded " + getName() + " with new expertise.");

		}
		else
		{
			if (_log.isDebugEnabled())
				_log.debug("No skills awarded at lvl: " + lvl);
		}

		// Active skill dwarven craft
		if (getSkillLevel(1321) < 1 && getRace() == Race.Dwarf)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(1321, 1);
			addSkill(skill);
		}

		// Active skill common craft
		if (getSkillLevel(1322) < 1)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(1322, 1);
			addSkill(skill);
		}

		for (int i = 0; i < COMMON_CRAFT_LEVELS.length; i++)
		{
			if (lvl >= COMMON_CRAFT_LEVELS[i] && getSkillLevel(1320) < (i + 1))
			{
				L2Skill skill = SkillTable.getInstance().getInfo(1320, (i + 1));
				addSkill(skill);
			}
		}

		// Auto-Learn skills if activated
		if (Config.ALT_AUTO_LEARN_SKILLS && !isCursedWeaponEquipped())
			sendMessage("You have learned " + SkillTreeTable.getInstance().giveAvailableSkills(this) + ".");

		if (isGM() && !hasSkill(7029))
		{
			addSkill(SkillTable.getInstance().getInfo(7029, 4));
		}

		sendSkillList();
	}

	/** Set the Experience value of the L2PcInstance. */
	public void setExp(long exp)
	{
		getStat().setExp(exp);
	}

	/**
	 * Regive all skills which aren't saved to database, like Noble, Hero, Clan Skills<BR><BR>
	 *
	 */
	public void regiveTemporarySkills()
	{
		// Do not call this on enterworld or char load

		// Add noble skills if noble
		if (isNoble())
			setNoble(true);

		// Add Hero skills if hero
		if (isHero())
			setHero(true);

		if (getClan() != null)
		{
			setPledgeClass(L2ClanMember.getCurrentPledgeClass(this));
			getClan().addSkillEffects(this, false);
			PledgeSkillList psl = new PledgeSkillList(getClan());
			sendPacket(psl);
			if (getClan().getLevel() >= Config.SIEGE_CLAN_MIN_LEVEL && isClanLeader())
				SiegeManager.getInstance().addSiegeSkills(this);
			enableResidentialSkills(true);
		}

		// Reload passive skills from armors / jewels / weapons
		getInventory().reloadEquippedItems();

		// Add Death Penalty Buff Level
		restoreDeathPenaltyBuffLevel();
	}

	public void enableResidentialSkills(boolean enable)
	{
		if (getClan() == null)
			return;

		if (enable)
		{
			if (getClan().getHasCastle() > 0)
				CastleManager.getInstance().getCastleByOwner(getClan()).giveResidentialSkills(this);

			if (getClan().getHasFort() > 0)
				FortManager.getInstance().getFortByOwner(getClan()).giveResidentialSkills(this);
		}
		else
		{
			if (getClan().getHasCastle() > 0)
				CastleManager.getInstance().getCastleByOwner(getClan()).removeResidentialSkills(this);
			if (getClan().getHasFort() > 0)
				FortManager.getInstance().getFortByOwner(getClan()).removeResidentialSkills(this);
		}
	}

	/**
	 * Return the Race object of the L2PcInstance.<BR><BR>
	 */
	public Race getRace()
	{
		if (!isSubClassActive())
			return getTemplate().getRace();

		L2PcTemplate charTemp = CharTemplateTable.getInstance().getTemplate(_baseClass);
		return charTemp.getRace();
	}

	public L2Radar getRadar()
	{
		if (_radar == null)
			_radar = new L2Radar(this);

		return _radar;
	}

	/** Return the SP amount of the L2PcInstance. */
	public int getSp()
	{
		return getStat().getSp();
	}

	/** Set the SP amount of the L2PcInstance. */
	public void setSp(int sp)
	{
		getStat().setSp(sp);
	}

	/**
	 * Return true if this L2PcInstance is a clan leader in
	 * ownership of the passed castle
	 */
	public boolean isCastleLord(int castleId)
	{
		L2Clan clan = getClan();
		// Player has clan and is the clan leader, check the castle info
		if ((clan != null) && (clan.getLeader().getPlayerInstance() == this))
		{
			// If the clan has a castle and it is actually the queried castle, return true
			Castle castle = CastleManager.getInstance().getCastleByOwner(clan);
			if ((castle != null) && (castle == CastleManager.getInstance().getCastleById(castleId)))
				return true;
		}
		return false;
	}

	/**
	 * Return the Clan Identifier of the L2PcInstance.<BR><BR>
	 */
	public int getClanId()
	{
		return _clanId;
	}

	/**
	 * Return the Clan Crest Identifier of the L2PcInstance or 0.<BR><BR>
	 */
	public int getClanCrestId()
	{
		if (_clan != null && _clan.hasCrest())
		{
			return _clan.getCrestId();
		}
		return 0;
	}

	/**
	 * @return The Clan CrestLarge Identifier or 0
	 */
	public int getClanCrestLargeId()
	{
		if (_clan != null && _clan.hasCrestLarge())
		{
			return _clan.getCrestLargeId();
		}
		return 0;
	}

	public long getClanJoinExpiryTime()
	{
		return _clanJoinExpiryTime;
	}

	public void setClanJoinExpiryTime(long time)
	{
		_clanJoinExpiryTime = time;
	}

	public long getClanCreateExpiryTime()
	{
		return _clanCreateExpiryTime;
	}

	public void setClanCreateExpiryTime(long time)
	{
		_clanCreateExpiryTime = time;
	}

	public void setOnlineTime(long time)
	{
		_onlineTime = time;
		_onlineBeginTime = System.currentTimeMillis();
	}

	/**
	 * Return the PcInventory Inventory of the L2PcInstance contained in _inventory.<BR><BR>
	 */
	@Override
	public PcInventory getInventory()
	{
		if (_inventory == null)
			_inventory = new PcInventory(this);

		return _inventory;
	}

	/**
	 * Delete a ShortCut of the L2PcInstance shortCuts.<BR><BR>
	 */
	public void removeItemFromShortCut(int objectId)
	{
		getShortCuts().deleteShortCutByObjectId(objectId);
	}

	/**
	 * Return True if the L2PcInstance is sitting.<BR><BR>
	 */
	public boolean isSitting()
	{
		return _waitTypeSitting;
	}

	public void sitDown()
	{
		sitDown(true);
	}

	/**
	 * Sit down the L2PcInstance, set the AI Intention to AI_INTENTION_REST and send a Server->Client ChangeWaitType packet (broadcast)<BR><BR>
	 */
	public void sitDown(boolean force)
	{
		if ((isCastingNow() || isCastingSimultaneouslyNow()) && !_relax)
		{
			sendMessage("Cannot sit while casting");
			return;
		}
		if (!(_waitTypeSitting || super.isAttackingDisabled() || isOutOfControl() || isImmobilized() || (!force && isTryingToSitOrStandup())))
		{
			_lastSitStandRequest = System.currentTimeMillis();
			breakAttack();
			_waitTypeSitting = true;
			getAI().setIntention(CtrlIntention.AI_INTENTION_REST);
			broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_SITTING));
		}
	}

	public void standUp()
	{
		standUp(true);
	}

	/**
	 * Stand up the L2PcInstance, set the AI Intention to AI_INTENTION_IDLE and send a Server->Client ChangeWaitType packet (broadcast)<BR><BR>
	 */
	public void standUp(boolean force)
	{
		if (!GlobalRestrictions.canStandUp(this))
			return;

		if (_waitTypeSitting && !isInStoreMode() && !isAlikeDead() && (!isTryingToSitOrStandup() || force))
		{
			_lastSitStandRequest = System.currentTimeMillis();
			if (_relax)
			{
				setRelax(false);
				stopEffects(L2EffectType.RELAXING);
			}
			_waitTypeSitting = false;
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_STANDING));
		}
	}

	/**
	 * Set the value of the _relax value. Must be True if using skill Relax and False if not.
	 */
	public void setRelax(boolean val)
	{
		_relax = val;
	}

	/**
	 * Return the PcWarehouse object of the L2PcInstance.<BR><BR>
	 */
	public PcWarehouse getWarehouse()
	{
		if (_warehouse == null)
		{
			_warehouse = new PcWarehouse(this);
			_warehouse.restore();
		}

		if (Config.WAREHOUSE_CACHE)
			WarehouseCacheManager.getInstance().add(this);

		return _warehouse;
	}

	/**
	 * Free memory used by Warehouse
	 */
	public void clearWarehouse()
	{
		if (_warehouse != null)
			_warehouse.deleteMe();
		_warehouse = null;
	}

	/**
	 * Return the PcFreight object of the L2PcInstance.<BR><BR>
	 */
	public PcFreight getFreight()
	{
		if (_freight == null)
		{
			_freight = new PcFreight(this);
			_freight.restore();
		}

		return _freight;
	}

	/**
	 * Free memory used by Freight
	 */
	public void clearFreight()
	{
		if (_freight != null)
			_freight.deleteMe();
		_freight = null;
	}

	/**
	 * Return deposited PcFreight object for the objectId
	 * or create new if not exist
	 */
	public PcFreight getDepositedFreight(int objectId)
	{
		if (_depositedFreight == null)
			_depositedFreight = new FastList<PcFreight>();
		else
		{
			for (PcFreight freight : _depositedFreight)
			{
				if (freight != null && freight.getOwnerId() == objectId)
					return freight;
			}
		}

		PcFreight freight = new PcFreight(null);
		freight.doQuickRestore(objectId);
		_depositedFreight.add(freight);
		return freight;
	}

	/**
	 * Clear memory used by deposited freight
	 */
	public void clearDepositedFreight()
	{
		if (_depositedFreight == null)
			return;

		for (PcFreight freight : _depositedFreight)
		{
			if (freight != null)
			{
				try
				{
					freight.deleteMe();
				}
				catch (Exception e)
				{
					_log.fatal("clearDepositedFreight()", e);
				}
			}
		}
		_depositedFreight.clear();
		_depositedFreight = null;
	}

	/**
	 * Return the Identifier of the L2PcInstance.<BR><BR>
	 */
	public int getCharId()
	{
		return _charId;
	}

	/**
	 * Set the Identifier of the L2PcInstance.<BR><BR>
	 */
	public void setCharId(int charId)
	{
		_charId = charId;
	}

	/**
	 * Return the Adena amount of the L2PcInstance.<BR><BR>
	 */
	public long getAdena()
	{
		return getInventory().getAdena();
	}

	/**
	 * Return the Ancient Adena amount of the L2PcInstance.<BR><BR>
	 */
	public long getAncientAdena()
	{
		return getInventory().getAncientAdena();
	}

	/**
	 * Add adena to Inventory of the L2PcInstance and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param count : long Quantity of adena to be added
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 */
	public void addAdena(String process, long count, L2Object reference, boolean sendMessage)
	{
		if (count > 0)
		{
			getInventory().addAdena(process, count, this, reference);

			if (sendMessage)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S1_ADENA);
				sm.addItemNumber(count);
				sendPacket(sm);
			}

			// Send update packet
			getInventory().updateInventory(getInventory().getAdenaInstance());
		}
	}

	/**
	 * Reduce adena in Inventory of the L2PcInstance and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param count : long Quantity of adena to be reduced
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	public boolean reduceAdena(String process, long count, L2Object reference, boolean sendMessage)
	{
		if (count > getAdena())
		{
			if (sendMessage)
				sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
			return false;
		}

		if (count > 0)
		{
			L2ItemInstance adenaItem = getInventory().getAdenaInstance();
			getInventory().reduceAdena(process, count, this, reference);

			// Send update packet
			getInventory().updateInventory(adenaItem);

			if (sendMessage)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_ADENA_DISAPPEARED);
				sm.addItemNumber(count);
				sendPacket(sm);
			}
		}

		return true;
	}

	/**
	 * Add ancient adena to Inventory of the L2PcInstance and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 *
	 * @param process : String Identifier of process triggering this action
	 * @param count : long Quantity of ancient adena to be added
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 */
	public void addAncientAdena(String process, long count, L2Object reference, boolean sendMessage)
	{
		if (sendMessage)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
			sm.addItemName(PcInventory.ANCIENT_ADENA_ID);
			sm.addItemNumber(count);
			sendPacket(sm);
		}

		if (count > 0)
		{
			getInventory().addAncientAdena(process, count, this, reference);
			getInventory().updateInventory(getInventory().getAncientAdenaInstance());
		}
	}

	/**
	 * Reduce ancient adena in Inventory of the L2PcInstance and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param count : long Quantity of ancient adena to be reduced
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	public boolean reduceAncientAdena(String process, long count, L2Object reference, boolean sendMessage)
	{
		if (count > getAncientAdena())
		{
			if (sendMessage)
				sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);

			return false;
		}

		if (count > 0)
		{
			getInventory().reduceAncientAdena(process, count, this, reference);
			getInventory().updateInventory(getInventory().getAncientAdenaInstance());
			if (sendMessage)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
				sm.addItemName(PcInventory.ANCIENT_ADENA_ID);
				sm.addItemNumber(count);
				sendPacket(sm);
			}
		}

		return true;
	}

	/**
	 * Adds item to inventory and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param item : L2ItemInstance to be added
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 */
	public L2ItemInstance addItem(String process, L2ItemInstance item, L2Object reference, boolean sendMessage, boolean UpdateIL)
	{
		if (item.getCount() > 0)
		{
			// Sends message to client if requested
			if (sendMessage)
			{
				if (item.getCount() > 1)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2);
					sm.addItemName(item);
					sm.addItemNumber(item.getCount());
					sendPacket(sm);
				}
				else if (item.getEnchantLevel() > 0)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.YOU_PICKED_UP_A_S1_S2);
					sm.addNumber(item.getEnchantLevel());
					sm.addItemName(item);
					sendPacket(sm);
				}
				else
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.YOU_PICKED_UP_S1);
					sm.addItemName(item);
					sendPacket(sm);
				}
			}

			// Add the item to inventory
			L2ItemInstance newitem = getInventory().addItem(process, item, this, reference);

			// Do treatments after adding this item
			processAddItem(UpdateIL, newitem);
			return newitem;
		}
		return null;
	}

	/**
	 * Adds item to Inventory and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param itemId : int Item Identifier of the item to be added
	 * @param count : long Quantity of items to be added
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 */
	public L2ItemInstance addItem(String process, int itemId, long count, L2Object reference, boolean sendMessage, boolean UpdateIL)
	{
		if (count > 0)
		{
			// Add the item to inventory
			L2ItemInstance newItem = getInventory().addItem(process, itemId, count, this, reference);

			// Sends message to client if requested
			if (sendMessage)
			{
				sendMessageForNewItem(newItem, count, process);
			}

			processAddItem(UpdateIL, newItem);
			return newItem;
		}
		return null;
	}

	/**
	 * @param UpdateIL
	 * @param newitem
	 */
	private void processAddItem(boolean UpdateIL, L2ItemInstance newitem)
	{
		// If over capacity, drop the item
		if (!isGM() && !getInventory().validateCapacity(0))
		{
			dropItem("InvDrop", newitem, null, true);
		}
		// Cursed Weapon
		else if (CursedWeaponsManager.getInstance().isCursed(newitem.getItemId()))
		{
			if (!CursedWeaponsManager.getInstance().activate(this, newitem))
				dropItem("CwDrop", newitem, null, true);
		}
		// Combat Flag
		else if (FortSiegeManager.getInstance().isCombat(newitem.getItemId()))
		{
			if (FortSiegeManager.getInstance().activateCombatFlag(this, newitem))
			{
				Fort fort = FortManager.getInstance().getFort(this);
				if (fort != null)
					fort.getSiege().announceToPlayer(new SystemMessage(SystemMessageId.C1_ACQUIRED_THE_FLAG), getName());
			}
			//else // FIXME: i'm not sure about this
			//	dropItem("CombatFlagDrop", newitem, null, true);
		}

		// Auto use herbs - autoloot
		else if (newitem.getItemType() == L2EtcItemType.HERB)
		{
			ItemHandler.getInstance().useItem(newitem.getItemId(), this, newitem);
		}

		// Update current load as well
		if (UpdateIL)
		{
			StatusUpdate su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
			sendPacket(su);
		}

		// Send inventory update packet
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(newitem);
			sendPacket(playerIU);
		}
		else
			sendPacket(new ItemList(this, false));
	}

	/**
	 * @param item : L2ItemInstance Item Identifier of the item to be added
	 * @param count : long Quantity of items to be added
     * @param process : String Identifier of process triggering this action
	 */
	private void sendMessageForNewItem(L2ItemInstance item, long count, String process)
	{
		if (count > 1)
		{
			if (process.equalsIgnoreCase("sweep") || process.equalsIgnoreCase("Quest"))
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
				sm.addItemName(item);
				sm.addItemNumber(count);
				sendPacket(sm);
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2);
				sm.addItemName(item);
				sm.addItemNumber(count);
				sendPacket(sm);
			}
		}
		else
		{
			if (process.equalsIgnoreCase("sweep") || process.equalsIgnoreCase("Quest"))
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S1);
				sm.addItemName(item);
				sendPacket(sm);
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.YOU_PICKED_UP_S1);
				sm.addItemName(item);
				sendPacket(sm);
			}
		}
	}

	public void addItem(String process, L2ItemInstance item, L2Object reference, boolean sendMessage)
	{
		addItem(process, item, reference, sendMessage, true);
	}

	public void addItem(String process, int itemId, long count, L2Object reference, boolean sendMessage)
	{
		addItem(process, itemId, count, reference, sendMessage, true);
	}

	/**
	 * Destroy item from inventory and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param item : L2ItemInstance to be destroyed
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	public boolean destroyItem(String process, L2ItemInstance item, L2Object reference, boolean sendMessage)
	{
		return this.destroyItem(process, item, item.getCount(), reference, sendMessage);
	}

	/**
	 * Destroy item from inventory and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param item : L2ItemInstance to be destroyed
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	public boolean destroyItem(String process, L2ItemInstance item, long count, L2Object reference, boolean sendMessage)
	{
		item = getInventory().destroyItem(process, item, count, this, reference);

		if (item == null)
		{
			if (sendMessage)
			{
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			}
			return false;
		}

		// Send inventory update packet
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(item);
			sendPacket(playerIU);
		}
		else
		{
			sendPacket(new ItemList(this, false));
		}

		// Update current load as well
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);

		// Sends message to client if requested
		if (sendMessage)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
			sm.addItemName(item);
			sm.addItemNumber(count);
			sendPacket(sm);
		}

		return true;
	}



	/**
	 * Destroys item from inventory and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param objectId : int Item Instance identifier of the item to be destroyed
	 * @param count : long Quantity of items to be destroyed
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	@Override
	public boolean destroyItem(String process, int objectId, long count, L2Object reference, boolean sendMessage)
	{
		L2ItemInstance item = getInventory().getItemByObjectId(objectId);
		if (item == null)
		{
			if (sendMessage)
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			return false;
		}

		return destroyItem(process, item, count, reference, sendMessage);
	}

	/**
	 * Destroys shots from inventory without logging and only occasional saving to database.
	 * Sends a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param objectId : int Item Instance identifier of the item to be destroyed
	 * @param count : long Quantity of items to be destroyed
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	public boolean destroyItemWithoutTrace(String process, int objectId, long count, L2Object reference, boolean sendMessage)
	{
		L2ItemInstance item = getInventory().getItemByObjectId(objectId);

		if (item == null || item.getCount() < count)
		{
			if (sendMessage)
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			return false;
		}

		return destroyItem(null, item, count, reference, sendMessage);
	}

	/**
	 * Destroy item from inventory by using its <B>itemId</B> and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param itemId : int Item identifier of the item to be destroyed
	 * @param count : long Quantity of items to be destroyed
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	@Override
	public boolean destroyItemByItemId(String process, int itemId, long count, L2Object reference, boolean sendMessage)
	{
		L2ItemInstance item = getInventory().getItemByItemId(itemId);
		if (item == null || item.getCount() < count || getInventory().destroyItemByItemId(process, itemId, count, this, reference) == null)
		{
			if (sendMessage)
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);

			return false;
		}

		// Send inventory update packet
		getInventory().updateInventory(item);

		// Sends message to client if requested
		if (sendMessage)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
			sm.addItemName(item);
			sm.addItemNumber(count);
			sendPacket(sm);
		}
		return true;
	}

	/**
	 * Destroy all weared items from inventory and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	public void destroyWearedItems(String process, L2Object reference, boolean sendMessage)
	{

		// Go through all Items of the inventory
		for (L2ItemInstance item : getInventory().getItems())
		{
			// Check if the item is a Try On item in order to remove it
			if (item.isWear())
			{
				if (item.isEquipped())
					getInventory().unEquipItemInSlotAndRecord(item.getLocationSlot());

				if (getInventory().destroyItem(process, item, this, reference) == null)
				{
					_log.warn("Player " + getName() + " can't destroy weared item: " + item.getName() + "[ " + item.getObjectId() + " ]");
					continue;
				}

				// Send an Unequipped Message in system window of the player for each Item
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_DISARMED);
				sm.addItemName(item);
				sendPacket(sm);

			}
		}

		// Send the ItemList Server->Client Packet to the player in order to refresh its Inventory
		ItemList il = new ItemList(getInventory().getItems(), true);
		sendPacket(il);

		// Send a Server->Client packet UserInfo to this L2PcInstance and CharInfo to all L2PcInstance in its _knownPlayers
		broadcastUserInfo();

		// Sends message to client if requested
		sendMessage("Trying-on mode has ended.");

	}

	/**
	 * Transfers item to another ItemContainer and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param objectId : int Item Identifier of the item to be transfered
	 * @param count : long Quantity of items to be transfered
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return L2ItemInstance corresponding to the new item or the updated item in inventory
	 */
	public L2ItemInstance transferItem(String process, int objectId, long count, Inventory target, L2Object reference)
	{
		L2ItemInstance oldItem = checkItemManipulation(objectId, count, "transfer");
		if (oldItem == null)
			return null;
		L2ItemInstance newItem = getInventory().transferItem(process, objectId, count, target, this, reference);
		if (newItem == null)
			return null;

		// Send inventory update packet
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate playerIU = new InventoryUpdate();

			if (oldItem.getCount() > 0 && oldItem != newItem)
				playerIU.addModifiedItem(oldItem);
			else
				playerIU.addRemovedItem(oldItem);

			sendPacket(playerIU);
		}
		else
			sendPacket(new ItemList(this, false));

		// Update current load as well
		StatusUpdate playerSU = new StatusUpdate(getObjectId());
		playerSU.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(playerSU);

		// Send target update packet
		if (target instanceof PcInventory)
		{
			L2PcInstance targetPlayer = ((PcInventory)target).getOwner();

			if (!Config.FORCE_INVENTORY_UPDATE)
			{
				InventoryUpdate playerIU = new InventoryUpdate();

				if (newItem.getCount() > count)
					playerIU.addModifiedItem(newItem);
				else
					playerIU.addNewItem(newItem);

				targetPlayer.sendPacket(playerIU);
			}
			else
				targetPlayer.sendPacket(new ItemList(targetPlayer, false));

			// Update current load as well
			playerSU = new StatusUpdate(targetPlayer.getObjectId());
			playerSU.addAttribute(StatusUpdate.CUR_LOAD, targetPlayer.getCurrentLoad());
			targetPlayer.sendPacket(playerSU);
		}
		else if (target instanceof PetInventory)
		{
			PetInventoryUpdate petIU = new PetInventoryUpdate();

			if (newItem.getCount() > count)
				petIU.addModifiedItem(newItem);
			else
				petIU.addNewItem(newItem);

			((PetInventory)target).getOwner().getOwner().sendPacket(petIU);
		}

		return newItem;
	}

	/**
	 * Drop item from inventory and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param item : L2ItemInstance to be dropped
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	public boolean dropItem(String process, L2ItemInstance item, L2Object reference, boolean sendMessage)
	{
		item = getInventory().dropItem(process, item, this, reference);

		if (item == null)
		{
			if (sendMessage)
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			return false;
		}

		item.dropMe(this, getX() + Rnd.get(50) - 25, getY() + Rnd.get(50) - 25, getZ() + 20);

		if (Config.AUTODESTROY_ITEM_AFTER > 0 && Config.DESTROY_DROPPED_PLAYER_ITEM && !Config.LIST_PROTECTED_ITEMS.contains(item.getItemId()))
		{
			if ((item.isEquipable() && Config.DESTROY_EQUIPABLE_PLAYER_ITEM) || !item.isEquipable())
				ItemsAutoDestroy.getInstance().addItem(item);
		}
		if (Config.DESTROY_DROPPED_PLAYER_ITEM)
		{
			if (!item.isEquipable() || (item.isEquipable() && Config.DESTROY_EQUIPABLE_PLAYER_ITEM))
				item.setProtected(false);
			else
				item.setProtected(true);
		}
		else
			item.setProtected(true);

		// Send inventory update packet
		getInventory().updateInventory(item);

		// Sends message to client if requested
		if (sendMessage)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DROPPED_S1);
			sm.addItemName(item);
			sendPacket(sm);
		}

		return true;
	}

	/**
	 * Drop item from inventory by using its <B>objectID</B> and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param objectId : int Item Instance identifier of the item to be dropped
	 * @param count : long Quantity of items to be dropped
	 * @param x : int coordinate for drop X
	 * @param y : int coordinate for drop Y
	 * @param z : int coordinate for drop Z
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return L2ItemInstance corresponding to the new item or the updated item in inventory
	 */
	public L2ItemInstance dropItem(String process, int objectId, long count, int x, int y, int z, L2Object reference, boolean sendMessage)
	{
		L2ItemInstance olditem = getInventory().getItemByObjectId(objectId);
		L2ItemInstance item = getInventory().dropItem(process, objectId, count, this, reference);

		if (item == null)
		{
			if (sendMessage)
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);

			return null;
		}

		item.dropMe(this, x, y, z);
		// Destroy item droped from inventory by player when DESTROY_PLAYER_INVENTORY_DROP is set to true
		if (Config.DESTROY_PLAYER_INVENTORY_DROP)
		{
			if (!Config.LIST_PROTECTED_ITEMS.contains(item.getItemId()))
			{
				if ((Config.AUTODESTROY_ITEM_AFTER > 0 && item.getItemType() != L2EtcItemType.HERB)
						|| (Config.HERB_AUTO_DESTROY_TIME > 0 && item.getItemType() == L2EtcItemType.HERB))
				{
					// Check if item is equipable
					if (item.isEquipable())
					{
						// Delete only when Configvalue DESTROY_EQUIPABLE_PLAYER_ITEM is set to true
						if (Config.DESTROY_EQUIPABLE_PLAYER_ITEM)
							ItemsAutoDestroy.getInstance().addItem(item);
					}
					else
					{
						ItemsAutoDestroy.getInstance().addItem(item);
					}
				}
			}
			item.setProtected(false);
		}
		// Avoids it from beeing removed by the auto item destroyer
		else
			item.setDropTime(0);

		// Send inventory update packet
		getInventory().updateInventory(olditem);

		// Sends message to client if requested
		if (sendMessage)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DROPPED_S1);
			sm.addItemName(item);
			sendPacket(sm);
		}
		return item;
	}

	public L2ItemInstance checkItemManipulation(int objectId, long count, String action)
	{
		L2ItemInstance item = getInventory().getItemByObjectId(objectId);
		if (item == null)
		{
			_log.debug(getObjectId() + ": player tried to " + action + " item he is not owner of");
			return null;
		}

		if (count < 0 || (count > 1 && !item.isStackable()))
		{
			_log.debug(getObjectId() + ": player tried to " + action + " item with invalid count: " + count);
			return null;
		}

		if (count > item.getCount())
		{
			_log.debug(getObjectId() + ": player tried to " + action + " more items than he owns");
			return null;
		}

		// Pet is summoned and not the item that summoned the pet AND not the buggle from strider you're mounting
		if (getPet() != null && getPet().getControlItemId() == objectId || getMountObjectID() == objectId)
		{
			if (_log.isDebugEnabled())
				_log.debug(getObjectId() + ": player tried to " + action + " item controling pet");
			return null;
		}

		if (getActiveEnchantItem() != null && getActiveEnchantItem().getObjectId() == objectId)
		{
			if (_log.isDebugEnabled())
				_log.debug(getObjectId() + ":player tried to " + action + " an enchant scroll he was using");
			return null;
		}

		if (item.isWear())
		{
			// Cannot drop/trade wear-items
			return null;
		}

		// We cannot put a Weapon with Augmention in WH while casting (Possible Exploit)
		if (item.isAugmented() && (isCastingNow() || isCastingSimultaneouslyNow()))
			return null;

		return item;
	}

	/**
	 * Set _protectEndTime according settings.
	 */
	public void setProtection(boolean protect)
	{
		int proTime = AutomatedTvT.isPlaying(this) ? Config.AUTO_TVT_SPAWN_PROTECT : Config.PLAYER_SPAWN_PROTECTION;
		if (protect && (proTime == 0 || isInOlympiadMode()))
			return;

		if (_log.isDebugEnabled() && (protect || _protectEndTime > 0))
			_log.debug(getName() + ": Protection "
					+ (protect ? "ON " + (GameTimeController.getGameTicks() + proTime * GameTimeController.TICKS_PER_SECOND) : "OFF")
					+ " (currently " + GameTimeController.getGameTicks() + ")");

		_protectEndTime = protect ? GameTimeController.getGameTicks() + proTime * GameTimeController.TICKS_PER_SECOND : 0;
	}

	public long getProtection()
	{
		return _protectEndTime;
	}

	/**
	 * Set protection from agro mobs when getting up from fake death, according settings.
	 */
	public void setRecentFakeDeath(boolean protect)
	{
		_recentFakeDeathEndTime = protect ? GameTimeController.getGameTicks() + Config.PLAYER_FAKEDEATH_UP_PROTECTION * GameTimeController.TICKS_PER_SECOND : 0;
	}

	public boolean isRecentFakeDeath()
	{
		return _recentFakeDeathEndTime > GameTimeController.getGameTicks();
	}

	/**
	 * Get the client owner of this char.<BR><BR>
	 */
	public L2GameClient getClient()
	{
		return _client;
	}

	/**
	 * Set the active connection with the client.<BR><BR>
	 */
	public void setClient(L2GameClient client)
	{
		_client = client;
	}

	public Point3D getCurrentSkillWorldPosition()
	{
		return _currentSkillWorldPosition;
	}

	public void setCurrentSkillWorldPosition(Point3D worldPosition)
	{
		_currentSkillWorldPosition = worldPosition;
	}

	public boolean canBeTargetedByAtSiege(L2PcInstance player)
	{
		Siege siege = SiegeManager.getInstance().getSiege(this);
		if (siege != null && siege.getIsInProgress())
		{
			L2Clan selfClan = getClan();
			L2Clan oppClan = player.getClan();
			if (selfClan != null && oppClan != null)
			{
				boolean self = false;
				for (L2SiegeClan clan : siege.getAttackerClans())
				{
					L2Clan cl = ClanTable.getInstance().getClan(clan.getClanId());

					if (cl == selfClan || cl.getAllyId() == getAllyId())
					{
						self = true;
						break;
					}
				}

				for (L2SiegeClan clan : siege.getDefenderClans())
				{
					L2Clan cl = ClanTable.getInstance().getClan(clan.getClanId());

					if (cl == selfClan || cl.getAllyId() == getAllyId())
					{
						self = true;
						break;
					}
				}

				boolean opp = false;
				for (L2SiegeClan clan : siege.getAttackerClans())
				{
					L2Clan cl = ClanTable.getInstance().getClan(clan.getClanId());

					if (cl == oppClan || cl.getAllyId() == player.getAllyId())
					{
						opp = true;
						break;
					}
				}

				for (L2SiegeClan clan : siege.getDefenderClans())
				{
					L2Clan cl = ClanTable.getInstance().getClan(clan.getClanId());

					if (cl == oppClan || cl.getAllyId() == player.getAllyId())
					{
						opp = true;
						break;
					}
				}

				return self && opp;
			}

			return false;
		}

		return true;
	}

	/**
	 * Manage actions when a player click on this L2PcInstance.<BR><BR>
	 *
	 * <B><U> Actions on first click on the L2PcInstance (Select it)</U> :</B><BR><BR>
	 * <li>Set the target of the player</li>
	 * <li>Send a Server->Client packet MyTargetSelected to the player (display the select window)</li><BR><BR>
	 *
	 * <B><U> Actions on second click on the L2PcInstance (Follow it/Attack it/Intercat with it)</U> :</B><BR><BR>
	 * <li>Send a Server->Client packet MyTargetSelected to the player (display the select window)</li>
	 * <li>If this L2PcInstance has a Private Store, notify the player AI with AI_INTENTION_INTERACT</li>
	 * <li>If this L2PcInstance is autoAttackable, notify the player AI with AI_INTENTION_ATTACK</li><BR><BR>
	 * <li>If this L2PcInstance is NOT autoAttackable, notify the player AI with AI_INTENTION_FOLLOW</li><BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Client packet : Action, AttackRequest</li><BR><BR>
	 *
	 * @param player The player that start an action on this L2PcInstance
	 *
	 */
	@Override
	public void onAction(L2PcInstance player)
	{
		if (player == null)
			return;
		// Restrict interactions during restart/shutdown
		if (Shutdown.isActionDisabled(DisableType.PC_ITERACTION))
		{
			sendMessage("Player interaction disabled during restart/shutdown.");
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (!GlobalRestrictions.canTarget(player, this, true))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		// Check if the L2PcInstance is confused
		if (player.isOutOfControl())
		{
			// Send a Server->Client packet ActionFailed to the player
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		// Aggression target lock effect
		if (!player.canChangeLockedTarget(this))
			return;

		// Check if the player already target this L2PcInstance
		if (player.getTarget() != this)
		{
			// Set the target of the player
			player.setTarget(this);
		}
		else
		{
			if (player != this)
				player.sendPacket(new ValidateLocation(this));
			// Check if this L2PcInstance has a Private Store
			if (getPrivateStoreType() != 0)
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
			}
			else
			{
				// Check if this L2PcInstance is autoAttackable
				if (isAutoAttackable(player))
				{
					// Player with lvl < 21 can't attack a cursed weapon holder
					// And a cursed weapon holder  can't attack players with lvl < 21
					if ((isCursedWeaponEquipped() && player.getLevel() < 21) || (player.isCursedWeaponEquipped() && getLevel() < 21))
					{
						player.sendPacket(ActionFailed.STATIC_PACKET);
					}
					else
					{
						if (GeoData.getInstance().canSeeTarget(player, this))
						{
							player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
							player.onActionRequest();
						}
					}
				}
				else
				{
					// This Action Failed packet avoids player getting stuck when clicking three or more times
					player.sendPacket(ActionFailed.STATIC_PACKET);

					if (GeoData.getInstance().canSeeTarget(player, this))
						player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
				}
			}
		}
	}

	@Override
	public void onActionShift(L2PcInstance gm)
	{
		gm.sendPacket(ActionFailed.STATIC_PACKET);
		if (gm.isGM())
		{
			if (this != gm.getTarget())
			{
				gm.setTarget(this);
			}
			else
			{
				AdminEditChar.gatherCharacterInfo(gm, this, "charinfo.htm");
			}
		}
	}

	@Override
	public boolean isInFunEvent()
	{
		return ((TvT._started && isInEvent(TvTPlayerInfo.class)) || (DM._started && isInEvent(DMPlayerInfo.class)) || (CTF._started && isInEvent(CTFPlayerInfo.class)) || (VIP._started && isInEvent(VIPPlayerInfo.class)) && !isGM() || _inEventTvTi);
	}

	/**
	 * Returns true if cp update should be done, false if not
	 * @return boolean
	 */
	private boolean needCpUpdate(int barPixels)
	{
		double currentCp = getStatus().getCurrentCp();

		if (currentCp <= 1.0 || getMaxCp() < barPixels)
			return true;

		if (currentCp <= _cpUpdateDecCheck || currentCp >= _cpUpdateIncCheck)
		{
			if (currentCp == getMaxCp())
			{
				_cpUpdateIncCheck = currentCp + 1;
				_cpUpdateDecCheck = currentCp - _cpUpdateInterval;
			}
			else
			{
				double doubleMulti = currentCp / _cpUpdateInterval;
				int intMulti = (int) doubleMulti;

				_cpUpdateDecCheck = _cpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti);
				_cpUpdateIncCheck = _cpUpdateDecCheck + _cpUpdateInterval;
			}

			return true;
		}

		return false;
	}

	/**
	 * Returns true if mp update should be done, false if not
	 * @return boolean
	 */
	private boolean needMpUpdate(int barPixels)
	{
		double currentMp = getStatus().getCurrentMp();

		if (currentMp <= 1.0 || getMaxMp() < barPixels)
			return true;

		if (currentMp <= _mpUpdateDecCheck || currentMp >= _mpUpdateIncCheck)
		{
			if (currentMp == getMaxMp())
			{
				_mpUpdateIncCheck = currentMp + 1;
				_mpUpdateDecCheck = currentMp - _mpUpdateInterval;
			}
			else
			{
				double doubleMulti = currentMp / _mpUpdateInterval;
				int intMulti = (int) doubleMulti;

				_mpUpdateDecCheck = _mpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti);
				_mpUpdateIncCheck = _mpUpdateDecCheck + _mpUpdateInterval;
			}

			return true;
		}

		return false;
	}

	/**
	 * Send packet StatusUpdate with current HP,MP and CP to the L2PcInstance and only current HP, MP and Level to all other L2PcInstance of the Party.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Send the Server->Client packet StatusUpdate with current HP, MP and CP to this L2PcInstance </li><BR>
	 * <li>Send the Server->Client packet PartySmallWindowUpdate with current HP, MP and Level to all other L2PcInstance of the Party </li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND current HP and MP to all L2PcInstance of the _statusListener</B></FONT><BR><BR>
	 *
	 */
	@Override
	public final void broadcastStatusUpdateImpl()
	{
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_HP, (int)getStatus().getCurrentHp());
		su.addAttribute(StatusUpdate.CUR_MP, (int)getStatus().getCurrentMp());
		su.addAttribute(StatusUpdate.CUR_CP, (int)getStatus().getCurrentCp());
		sendPacket(su);

		if (isInParty() && (needHpUpdate(352) || needMpUpdate(352) || needCpUpdate(352)))
			getParty().broadcastToPartyMembers(this, new PartySmallWindowUpdate(this));

		if (isInOlympiadMode() && isOlympiadStart())
		{
			for (L2PcInstance player : Olympiad.getInstance().getPlayers(_olympiadGameId))
				if (player != null && player != this)
					player.sendPacket(new ExOlympiadUserInfo(this, 2));

			final List<L2PcInstance> spectators = Olympiad.getInstance().getSpectators(getOlympiadGameId());

			if (spectators != null && !spectators.isEmpty())
			{
				final ExOlympiadUserInfo eoui = new ExOlympiadUserInfo(this);

				for (L2PcInstance spectator : spectators)
					spectator.sendPacket(eoui);
			}
		}

		if (isInDuel())
			DuelManager.getInstance().broadcastToOppositTeam(this, new ExDuelUpdateUserInfo(this));
	}

	@Override
	public void updateEffectIconsImpl()
	{
		final EffectInfoPacketList list = new EffectInfoPacketList(this);

		sendPacket(new MagicEffectIcons(list));

		if (isInParty())
			getParty().broadcastToPartyMembers(this, new PartySpelled(list));
	}

	/**
	 * Send a Server->Client packet UserInfo to this L2PcInstance and CharInfo to all L2PcInstance in its _knownPlayers.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * Others L2PcInstance in the detection area of the L2PcInstance are identified in <B>_knownPlayers</B>.
	 * In order to inform other players of this L2PcInstance state modifications, server just need to go through _knownPlayers to send Server->Client Packet<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Send a Server->Client packet UserInfo to this L2PcInstance (Public and Private Data)</li>
	 * <li>Send a Server->Client packet CharInfo to all L2PcInstance in _knownPlayers of the L2PcInstance (Public data only)</li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : DON'T SEND UserInfo packet to other players instead of CharInfo packet.
	 * Indeed, UserInfo packet contains PRIVATE DATA as MaxHP, STR, DEX...</B></FONT><BR><BR>
	 *
	 */
	public final void broadcastUserInfo()
	{
		broadcastFullInfo();
	}

	public final void broadcastTitleInfo()
	{
		// Send a Server->Client packet UserInfo to this L2PcInstance
		sendPacket(new UserInfo(this));

		// Send a Server->Client packet NicknameChanged to all L2PcInstance in _KnownPlayers of the L2PcInstance
		if (_log.isDebugEnabled())
			_log.debug("players to notify:" + getKnownList().getKnownPlayers().size() + " packet: [S] cc NicknameChanged");

		Broadcast.toKnownPlayers(this, new NicknameChanged(this));
	}

	/**
	 * Return the Alliance Identifier of the L2PcInstance.<BR><BR>
	 */
	public int getAllyId()
	{
		return (_clan == null) ? 0 : _clan.getAllyId();
	}

	public int getAllyCrestId()
	{
		if (getClanId() == 0)
		{
			return 0;
		}
		if (getClan().getAllyId() == 0)
		{
			return 0;
		}
		return getClan().getAllyCrestId();
	}

	public void queryGameGuard()
	{
		getClient().setGameGuardOk(false);
		sendPacket(GameGuardQuery.STATIC_PACKET);
		if (Config.GAMEGUARD_ENFORCE)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new GameGuardCheck(), 30 * 1000);
		}
	}

	class GameGuardCheck implements Runnable
	{

		/**
		 * @see java.lang.Runnable#run()
		 */
		public void run()
		{
			L2GameClient client = getClient();
			if (client != null && !client.isAuthedGG() && isOnline() == 1)
			{
				//GmListTable.broadcastMessageToGMs("Client "+client+" failed to reply GameGuard query and is being kicked!");
				_log.info("Client " + client + " failed to reply GameGuard query and is being kicked!");
				new Disconnection(client, L2PcInstance.this).defaultSequence(false);
			}
		}
	}

	/**
	 * Send a Server->Client packet to the L2PcInstance.<BR>
	 * <BR>
	 */
	@Override
	@SuppressWarnings("deprecation")
	public void sendPacket(L2GameServerPacket packet)
	{
		final L2GameClient client = _client;
		if (client != null)
			client.sendPacket(packet);
	}

	/**
	 * Sends a SystemMessage without any parameter added. No instancing at all!
	 */
	@Override
	public void sendPacket(SystemMessageId sm)
	{
		sendPacket(sm.getSystemMessage());
	}

	@Override
	public void sendPacket(StaticPacket packet)
	{
		sendPacket((L2GameServerPacket) packet);
	}

	@Override
	@SuppressWarnings("deprecation")
	public void sendMessage(String message)
	{
		sendPacket(SystemMessage.sendString(message));
	}

	/**
	 * Manage Interact Task with another L2PcInstance.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>If the private store is a STORE_PRIVATE_SELL, send a Server->Client PrivateBuyListSell packet to the L2PcInstance</li>
	 * <li>If the private store is a STORE_PRIVATE_BUY, send a Server->Client PrivateBuyListBuy packet to the L2PcInstance</li>
	 * <li>If the private store is a STORE_PRIVATE_MANUFACTURE, send a Server->Client RecipeShopSellList packet to the L2PcInstance</li><BR><BR>
	 *
	 * @param target The L2Character targeted
	 *
	 */
	public void doInteract(L2Character target)
	{
		if (target instanceof L2PcInstance)
		{
			L2PcInstance temp = (L2PcInstance) target;
			sendPacket(ActionFailed.STATIC_PACKET);

			if (temp.getPrivateStoreType() == STORE_PRIVATE_SELL || temp.getPrivateStoreType() == STORE_PRIVATE_PACKAGE_SELL)
				sendPacket(new PrivateStoreListSell(this, temp));
			else if (temp.getPrivateStoreType() == STORE_PRIVATE_BUY)
				sendPacket(new PrivateStoreListBuy(this, temp));
			else if (temp.getPrivateStoreType() == STORE_PRIVATE_MANUFACTURE)
				sendPacket(new RecipeShopSellList(this, temp));
		}
		else
		{
			// _interactTarget=null should never happen but one never knows ^^;
			if (target != null)
				target.onAction(this);
		}
	}

	/**
	 * Manage AutoLoot Task.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Send a System Message to the L2PcInstance : YOU_PICKED_UP_S1_ADENA or YOU_PICKED_UP_S1_S2</li>
	 * <li>Add the Item to the L2PcInstance inventory</li>
	 * <li>Send a Server->Client packet InventoryUpdate to this L2PcInstance with NewItem (use a new slot) or ModifiedItem (increase amount)</li>
	 * <li>Send a Server->Client packet StatusUpdate to this L2PcInstance with current weight</li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : If a Party is in progress, distribute Items between party members</B></FONT><BR><BR>
	 *
	 * @param target The L2ItemInstance dropped
	 *
	 */
	public void doAutoLoot(L2Attackable target, L2Attackable.RewardItem item)
	{
		if (isInParty())
			getParty().distributeItem(this, item, false, target);
		else if (item.getItemId() == 57)
			addAdena("Loot", item.getCount(), target, true);
		else
			addItem("Loot", item.getItemId(), item.getCount(), target, true, false);
	}

	/**
	 * Manage Pickup Task.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Send a Server->Client packet StopMove to this L2PcInstance </li>
	 * <li>Remove the L2ItemInstance from the world and send server->client GetItem packets </li>
	 * <li>Send a System Message to the L2PcInstance : YOU_PICKED_UP_S1_ADENA or YOU_PICKED_UP_S1_S2</li>
	 * <li>Add the Item to the L2PcInstance inventory</li>
	 * <li>Send a Server->Client packet InventoryUpdate to this L2PcInstance with NewItem (use a new slot) or ModifiedItem (increase amount)</li>
	 * <li>Send a Server->Client packet StatusUpdate to this L2PcInstance with current weight</li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : If a Party is in progress, distribute Items between party members</B></FONT><BR><BR>
	 *
	 * @param object The L2ItemInstance to pick up
	 *
	 */
	protected void doPickupItem(L2Object object)
	{
		if (isAlikeDead() || isFakeDeath())
			return;

		// Set the AI Intention to AI_INTENTION_IDLE
		getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

		// Check if the L2Object to pick up is a L2ItemInstance
		if (!(object instanceof L2ItemInstance))
		{
			// Dont try to pickup anything that is not an item :)
			_log.warn("trying to pickup wrong target." + getTarget());
			return;
		}

		L2ItemInstance target = (L2ItemInstance) object;

		// Send a Server->Client packet ActionFailed to this L2PcInstance
		sendPacket(ActionFailed.STATIC_PACKET);

		// Send a Server->Client packet StopMove to this L2PcInstance
		StopMove sm = new StopMove(getObjectId(), getX(), getY(), getZ(), getHeading());
		if (_log.isDebugEnabled())
			_log.debug("pickup pos: " + target.getX() + " " + target.getY() + " " + target.getZ());
		sendPacket(sm);

		synchronized (target)
		{
			// Check if the target to pick up is visible
			if (!target.isVisible())
			{
				// Send a Server->Client packet ActionFailed to this L2PcInstance
				sendPacket(ActionFailed.STATIC_PACKET);
				return;

			}

			if (((isInParty() && getParty().getLootDistribution() == L2Party.ITEM_LOOTER) || !isInParty()) && !getInventory().validateCapacity(target))
			{
				sendPacket(SystemMessageId.SLOTS_FULL);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			if (isInvul() && !isGM())
			{
				SystemMessage smsg = new SystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1);
				smsg.addItemName(target);
				sendPacket(smsg);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			if (getActiveTradeList() != null)
			{
				sendPacket(SystemMessageId.CANNOT_PICKUP_OR_USE_ITEM_WHILE_TRADING);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			if (target.getOwnerId() != 0 && target.getOwnerId() != getObjectId() && !isInLooterParty(target.getOwnerId()))
			{
				sendPacket(ActionFailed.STATIC_PACKET);

				if (target.getItemId() == 57)
				{
					SystemMessage smsg = new SystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1_ADENA);
					smsg.addItemNumber(target.getCount());
					sendPacket(smsg);
				}
				else if (target.getCount() > 1)
				{
					SystemMessage smsg = new SystemMessage(SystemMessageId.FAILED_TO_PICKUP_S2_S1_S);
					smsg.addItemName(target);
					smsg.addItemNumber(target.getCount());
					sendPacket(smsg);
				}
				else
				{
					SystemMessage smsg = new SystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1);
					smsg.addItemName(target);
					sendPacket(smsg);
				}

				return;
			}

			// Cursed Weapons
			if (CursedWeaponsManager.getInstance().isCursed(target.getItemId()) && isCursedWeaponEquipped())
			{
				ItemTable.getInstance().destroyItem("Pickup CW", target, this, null);
				CursedWeapon cw = CursedWeaponsManager.getInstance().getCursedWeapon(getCursedWeaponEquippedId());
				cw.increaseKills(cw.getStageKills());
				return;
			}

			// You can pickup only 1 combat flag
			if (FortSiegeManager.getInstance().isCombat(target.getItemId()))
			{
				if (!FortSiegeManager.getInstance().checkIfCanPickup(this))
					return;
			}

			if (target.getItemLootShedule() != null && (target.getOwnerId() == getObjectId() || isInLooterParty(target.getOwnerId())))
				target.resetOwnerTimer();

			// Remove the L2ItemInstance from the world and send server->client GetItem packets
			target.pickupMe(this);
		}
		// Auto use herbs - pick up
		if (target.getItemType() == L2EtcItemType.HERB)
		{
			ItemHandler.getInstance().useItem(target.getItemId(), this, target);
		}
		// Cursed Weapons are not distributed
		else if (CursedWeaponsManager.getInstance().isCursed(target.getItemId()))
		{
			addItem("Pickup", target, null, true);
		}
		else if (FortSiegeManager.getInstance().isCombat(target.getItemId()))
		{
			addItem("Pickup", target, null, true);
		}
		else
		{
			// If item is instance of L2ArmorType or L2WeaponType broadcast an "Attention" system message
			if (target.getItemType() instanceof L2ArmorType || target.getItemType() instanceof L2WeaponType)
			{
				if (target.getEnchantLevel() > 0)
				{
					SystemMessage msg = new SystemMessage(SystemMessageId.ANNOUNCEMENT_C1_PICKED_UP_S2_S3);
					msg.addPcName(this);
					msg.addNumber(target.getEnchantLevel());
					msg.addItemName(target);
					broadcastPacket(msg, 1400);
				}
				else
				{
					SystemMessage msg = new SystemMessage(SystemMessageId.ANNOUNCEMENT_C1_PICKED_UP_S2);
					msg.addPcName(this);
					msg.addItemName(target);
					broadcastPacket(msg, 1400);
				}
			}

			// Check if a Party is in progress
			if (isInParty())
				getParty().distributeItem(this, target);
			// Target is adena
			else if (target.getItemId() == 57 && getInventory().getAdenaInstance() != null)
			{
				addAdena("Pickup", target.getCount(), null, true);
				ItemTable.getInstance().destroyItem("Pickup", target, this, null);
			}
			// Target is regular item
			else
				addItem("Pickup", target, null, true);
		}
	}

	/**
	 * Set a target.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove the L2PcInstance from the _statusListener of the old target if it was a L2Character </li>
	 * <li>Add the L2PcInstance to the _statusListener of the new target if it's a L2Character </li>
	 * <li>Target the new L2Object (add the target to the L2PcInstance _target, _knownObject and L2PcInstance to _KnownObject of the L2Object)</li><BR><BR>
	 *
	 * @param newTarget The L2Object to target
	 *
	 */
	@Override
	public void setTarget(L2Object newTarget)
	{
		if (newTarget != null)
		{
			if (this != newTarget && newTarget instanceof L2Character)
				sendPacket(new ValidateLocation((L2Character)newTarget));

			if (!isGM())
			{
				if (newTarget instanceof L2FestivalMonsterInstance && !isFestivalParticipant())
					return;

				if (isInParty() && getParty().isInDimensionalRift())
				{
					byte riftType = getParty().getDimensionalRift().getType();
					byte riftRoom = getParty().getDimensionalRift().getCurrentRoom();

					if (!DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(newTarget))
						return;
				}
			}

			if (!(newTarget instanceof L2PcInstance) || !isInParty() ||  getParty() != ((L2PcInstance)newTarget).getParty())
			{
				if (!newTarget.isVisible())
					return;

				if (Math.abs(newTarget.getZ() - getZ()) > 1000)
					return;
			}
		}

		super.setTarget(newTarget);
	}

	@Override
	protected void refreshTarget(L2Object newTarget)
	{
		final L2Object oldTarget = getTarget();

		if (oldTarget instanceof L2Character)
			((L2Character)oldTarget).getStatus().removeStatusListener(this);

		if (newTarget instanceof L2Character)
			((L2Character)newTarget).getStatus().addStatusListener(this);

		if (newTarget != null)
		{
			broadcastPacket(new TargetSelected(this, newTarget));

			sendPacket(new MyTargetSelected(this, newTarget));
		}
		else if (oldTarget != null)
		{
			broadcastPacket(new TargetUnselected(this));
		}

		super.refreshTarget(newTarget);
	}

	/**
	 * Return the active weapon instance (always equipped in the right hand).<BR><BR>
	 */
	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
	}

	/**
	 * Return the active weapon item (always equipped in the right hand).<BR><BR>
	 */
	@Override
	public L2Weapon getActiveWeaponItem()
	{
		L2ItemInstance weapon = getActiveWeaponInstance();

		if (weapon == null)
			return getFistsWeaponItem();

		return (L2Weapon)weapon.getItem();
	}

	public L2ItemInstance getChestArmorInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
	}

	public L2ItemInstance getLegsArmorInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS);
	}

	public L2Armor getActiveChestArmorItem()
	{
		L2ItemInstance armor = getChestArmorInstance();

		if (armor == null)
			return null;

		return (L2Armor) armor.getItem();
	}

	public L2Armor getActiveLegsArmorItem()
	{
		L2ItemInstance legs = getLegsArmorInstance();

		if (legs == null)
			return null;

		return (L2Armor) legs.getItem();
	}

	public boolean isWearingHeavyArmor()
	{
		if ((getChestArmorInstance() != null) && getLegsArmorInstance() != null)
		{
			L2ItemInstance legs = getLegsArmorInstance();
			L2ItemInstance armor = getChestArmorInstance();
			if (legs.getItemType() == L2ArmorType.HEAVY && (armor.getItemType() == L2ArmorType.HEAVY))
				return true;
		}
		if (getChestArmorInstance() != null)
		{
			L2ItemInstance armor = getChestArmorInstance();

			if (getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST).getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR
					&& armor.getItemType() == L2ArmorType.HEAVY)
				return true;
		}

		return false;
	}

	public boolean isWearingLightArmor()
	{
		if ((getChestArmorInstance() != null) && getLegsArmorInstance() != null)
		{
			L2ItemInstance legs = getLegsArmorInstance();
			L2ItemInstance armor = getChestArmorInstance();
			if (legs.getItemType() == L2ArmorType.LIGHT && (armor.getItemType() == L2ArmorType.LIGHT))
				return true;
		}
		if (getChestArmorInstance() != null)
		{
			L2ItemInstance armor = getChestArmorInstance();

			if (getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST).getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR
					&& armor.getItemType() == L2ArmorType.LIGHT)
				return true;
		}

		return false;
	}

	public boolean isWearingMagicArmor()
	{
		if ((getChestArmorInstance() != null) && getLegsArmorInstance() != null)
		{
			L2ItemInstance legs = getLegsArmorInstance();
			L2ItemInstance armor = getChestArmorInstance();
			if (legs.getItemType() == L2ArmorType.MAGIC && (armor.getItemType() == L2ArmorType.MAGIC))
				return true;
		}
		if (getChestArmorInstance() != null)
		{
			L2ItemInstance armor = getChestArmorInstance();

			if (getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST).getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR
					&& armor.getItemType() == L2ArmorType.MAGIC)
				return true;
		}

		return false;
	}

	public boolean isWearingFormalWear()
	{
		return _IsWearingFormalWear;
	}

	public void setIsWearingFormalWear(boolean value)
	{
		_IsWearingFormalWear = value;
	}

	/**
	 * Return the secondary weapon instance (always equipped in the left hand).<BR><BR>
	 */
	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
	}

	/**
	 * Return the secondary weapon item (always equipped in the left hand) or the fists weapon.<BR><BR>
	 */
	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		L2ItemInstance weapon = getSecondaryWeaponInstance();

		if (weapon == null)
			return getFistsWeaponItem();

		L2Item item = weapon.getItem();

		if (item instanceof L2Weapon)
			return (L2Weapon)item;

		return null;
	}

	/**
	 * Kill the L2Character, Apply Death Penalty, Manage gain/loss Karma and Item Drop.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Reduce the Experience of the L2PcInstance in function of the calculated Death Penalty </li>
	 * <li>If necessary, unsummon the Pet of the killed L2PcInstance </li>
	 * <li>Manage Karma gain for attacker and Karma loss for the killed L2PcInstance </li>
	 * <li>If the killed L2PcInstance has Karma, manage Drop Item</li>
	 * <li>Kill the L2PcInstance </li><BR><BR>
	 *
	 *
	 * @param killer The L2Character who attacks
	 *
	 */
	@Override
	public boolean doDie(L2Character killer)
	{
		/* Since L2Character.doDie() calls stopAllEffects(), which includes
		 * setting charm of curage and other blessings as false, this stores value
		 * before calling superclass method
		 */
		boolean charmOfCourage = getCharmOfCourage();

		// Kill the L2PcInstance
		if (!super.doDie(killer))
			return false;

		if (isMounted())
			stopFeed();

		synchronized (this)
		{
			if (isFakeDeath())
				stopFakeDeath(true);
		}

		// Clear resurrect xp calculation
		setExpBeforeDeath(0);

		// Issues drop of Cursed Weapon.
		if (isCursedWeaponEquipped())
		{
			CursedWeaponsManager.getInstance().drop(_cursedWeaponEquippedId, killer);
		}
		else if (isCombatFlagEquipped())
		{
			FortSiegeManager.getInstance().dropCombatFlag(this);
		}

		Castle castle = null;
		if (getClan() != null)
		{
			castle = CastleManager.getInstance().getCastleByOwner(getClan());
			if (castle != null)
				castle.destroyClanGate();
		}

		if (killer != null)
		{
			L2PcInstance pk = killer.getActingPlayer();

			boolean clanWarKill = false;
			boolean playerKill = false;

			if (GlobalRestrictions.playerKilled(killer, this))
			{
			}

			else if (pk != null)
			{
				clanWarKill = pk.getClan() != null && getClan() != null && !isAcademyMember() && !pk.isAcademyMember() &&
							((_clan.isAtWarWith(pk.getClanId()) && pk.getClan().isAtWarWith(getClanId()))
						|| (isInSiege() && pk.isInSiege()));
				playerKill = true;
			}

			boolean srcInPvP = isInsideZone(L2Zone.FLAG_PVP) && !isInSiege();

			if (killer instanceof L2PcInstance && srcInPvP && Config.ARENA_ENABLED)
			{
				ArenaManager.getInstance().onKill(killer.getObjectId(), killer.getName());
				ArenaManager.getInstance().onDeath(getObjectId(), getName());
			}

			if (clanWarKill && pk != null)
			{
				if (getClan().getReputationScore() > 0) // When your reputation score is 0 or below, the other clan cannot acquire any reputation points
				{
					pk.getClan().setReputationScore(pk.getClan().getReputationScore() + Config.REPUTATION_SCORE_PER_KILL, true);
				}
				if (pk.getClan().getReputationScore() > 0) // When the opposing sides reputation score is 0 or below, your clans reputation score does not decrease
				{
					_clan.setReputationScore(_clan.getReputationScore() - Config.REPUTATION_SCORE_PER_KILL, true);
				}
			}

			if (!srcInPvP)
			{
				if (pk == null || !pk.isCursedWeaponEquipped())
				{
					onDieDropItem(killer); // Check if any item should be dropped

					if (!srcInPvP)
					{
						if (Config.ALT_GAME_DELEVEL)
						{
							// Reduce the Experience of the L2PcInstance in function of the calculated Death Penalty
							// NOTE: deathPenalty +- Exp will update karma
							// Penalty is lower if the player is at war with the pk (war has to be declared)
							if (getSkillLevel(L2Skill.SKILL_LUCKY) < 0 || getStat().getLevel() > 9)
							{
								deathPenalty(clanWarKill, playerKill, charmOfCourage, killer instanceof L2SiegeGuard);
							}
						}
						else
						{
							if (!(isInsideZone(L2Zone.FLAG_PVP) && !isInsideZone(L2Zone.FLAG_PVP)) || pk == null)
								onDieUpdateKarma(); // Update karma if delevel is not allowed
						}
					}
				}
				if (pk != null)
				{
					if (Config.ALT_ANNOUNCE_PK)
					{
						String announcetext = "";
						// Build announce text
						if (getPvpFlag() == 0)
							announcetext = pk.getName() + " has slaughtered " + getName();
						else
							announcetext = pk.getName() + " has defeated " + getName();

						// Announce to player
						if (Config.ALT_ANNOUNCE_PK_NORMAL_MESSAGE)
							Announcements.getInstance().announceToPlayers(announcetext);
						else
							Announcements.getInstance().announceToAll(announcetext);
					}
				}
			}
			else if (pk != null && Config.ALT_ANNOUNCE_PK)
			{
				if (Config.ALT_ANNOUNCE_PK_NORMAL_MESSAGE)
					Announcements.getInstance().announceToPlayers(pk.getName() + " has defeated " + getName());
				else
					Announcements.getInstance().announceToAll(pk.getName() + " has defeated " + getName());
			}
		}

		// Force Charges
		clearCharges(); // Empty charges

		updatePvPFlag(0); // Clear the pvp flag
		// Pet shouldn't get unsummoned after masters death.
		// Unsummon the Pet
		//if (getPet() != null) getPet().unSummon(this);

		// Unsummon Cubics
		if (!_cubics.isEmpty())
		{
			for (L2CubicInstance cubic : _cubics.values())
			{
				cubic.stopAction();
				cubic.cancelDisappear();
			}
			_cubics.clear();
		}

		if (_fusionSkill != null)
			abortCast();

		for (L2Character character : getKnownList().getKnownCharacters())
			if (character.getFusionSkill() != null && character.getFusionSkill().getTarget() == this)
				character.abortCast();

		if (isInParty() && getParty().isInDimensionalRift())
		{
			getParty().getDimensionalRift().memberDead(this);
		}

		// Calculate death penalty buff
		calculateDeathPenaltyBuffLevel(killer);

		// [L2J_JP ADD SANDMAN]
		// When the player has been annihilated, the player is banished from the Four Sepulcher.
		if (FourSepulchersManager.getInstance().checkIfInZone(this) && (getZ() >= -7250 && getZ() <= -6841))
			FourSepulchersManager.getInstance().checkAnnihilated(this);
		// When the player has been annihilated, the player is banished from the lair.
		else if (SailrenManager.getInstance().checkIfInZone(this))
			SailrenManager.getInstance().checkAnnihilated();
		else if (AntharasManager.getInstance().checkIfInZone(this))
			AntharasManager.getInstance().checkAnnihilated();
		else if (ValakasManager.getInstance().checkIfInZone(this))
			ValakasManager.getInstance().checkAnnihilated();
		else if (BaiumManager.getInstance().checkIfInZone(this))
			BaiumManager.getInstance().checkAnnihilated();
		else if (BaylorManager.getInstance().checkIfInZone(this))
			BaylorManager.getInstance().checkAnnihilated();
		else if (FrintezzaManager.getInstance().checkIfInZone(this))
			FrintezzaManager.getInstance().checkAnnihilated();
		else if (LastImperialTombManager.getInstance().checkIfInZone(this))
			LastImperialTombManager.getInstance().checkAnnihilated();

		return true;
	}

	/** UnEnquip on skills with disarm effect **/
	public void onDisarm(L2PcInstance target)
	{
		target.getInventory().unEquipItemInBodySlotAndRecord(14);
	}

	private void onDieDropItem(L2Character killer)
	{
		if (killer == null)
			return;
		
		L2PcInstance pk = killer.getActingPlayer();
		if (pk != null && getKarma() <= 0 && pk.getClan() != null && getClan() != null
				&& (pk.getClan().isAtWarWith(getClanId())
				//|| this.getClan().isAtWarWith(((L2PcInstance)killer).getClanId())
				))
			return;

		if ((!isInsideZone(L2Zone.FLAG_PVP) || pk == null) && (!isGM() || Config.KARMA_DROP_GM))
		{
			boolean isKarmaDrop = false;
			boolean isKillerNpc = (killer instanceof L2Npc);
			int pkLimit = Config.KARMA_PK_LIMIT;

			int dropEquip = 0;
			int dropEquipWeapon = 0;
			int dropItem = 0;
			int dropLimit = 0;
			int dropPercent = 0;

			if (getKarma() > 0 && getPkKills() >= pkLimit)
			{
				isKarmaDrop = true;
				dropPercent = Config.KARMA_RATE_DROP;
				dropEquip = Config.KARMA_RATE_DROP_EQUIP;
				dropEquipWeapon = Config.KARMA_RATE_DROP_EQUIP_WEAPON;
				dropItem = Config.KARMA_RATE_DROP_ITEM;
				dropLimit = Config.KARMA_DROP_LIMIT;
			}
			else if (isKillerNpc && getLevel() > 4 && !isFestivalParticipant())
			{
				dropPercent = Config.PLAYER_RATE_DROP;
				dropEquip = Config.PLAYER_RATE_DROP_EQUIP;
				dropEquipWeapon = Config.PLAYER_RATE_DROP_EQUIP_WEAPON;
				dropItem = Config.PLAYER_RATE_DROP_ITEM;
				dropLimit = Config.PLAYER_DROP_LIMIT;
			}

			if (dropPercent > 0 && Rnd.get(100) < dropPercent)
			{
				int dropCount = 0;
				int itemDropPercent = 0;
				for (L2ItemInstance itemDrop : getInventory().getItems())
				{
					// Don't drop
					if (!itemDrop.isDropable() || itemDrop.getItemId() == 57 // Adena
							// Dont drop Shadow Items
							|| itemDrop.isShadowItem()
							// Dont drop Time Limited Items
							|| itemDrop.isTimeLimitedItem()
							// Quest Items
							|| itemDrop.getItem().getType2() == L2Item.TYPE2_QUEST
							// Control Item of active pet
							|| getPet() != null && getPet().getControlItemId() == itemDrop.getItemId()
							// Item listed in the non droppable item list
							|| Arrays.binarySearch(Config.KARMA_LIST_NONDROPPABLE_ITEMS, itemDrop.getItemId()) >= 0
							// Item listed in the non droppable pet item list
							|| Arrays.binarySearch(Config.KARMA_LIST_NONDROPPABLE_PET_ITEMS, itemDrop.getItemId()) >= 0
					)
						continue;

					if (itemDrop.isEquipped())
					{
						// Set proper chance according to Item type of equipped Item
						itemDropPercent = itemDrop.getItem().getType2() == L2Item.TYPE2_WEAPON ? dropEquipWeapon : dropEquip;
						getInventory().unEquipItemInSlotAndRecord(itemDrop.getLocationSlot());
					}
					else
						itemDropPercent = dropItem; // Item in inventory

					// NOTE: Each time an item is dropped, the chance of another item being dropped gets lesser (dropCount * 2)
					if (Rnd.get(100) < itemDropPercent)
					{
						dropItem("DieDrop", itemDrop, killer, true);

						if (isKarmaDrop)
							_log.info(getName() + " has karma and dropped " + itemDrop);
						else
							_log.info(getName() + " dropped " + itemDrop);

						if (++dropCount >= dropLimit)
							break;
					}
				}
			}
			// Player can drop adena against other player
			if (Config.ALT_PLAYER_CAN_DROP_ADENA && !isKillerNpc && Config.PLAYER_RATE_DROP_ADENA > 0 && 100 >= Config.PLAYER_RATE_DROP_ADENA
					&& !(killer instanceof L2PcInstance && ((L2PcInstance) killer).isGM()))
			{
				L2ItemInstance itemDrop = getInventory().getAdenaInstance();
				long iCount = getInventory().getAdena();
				// Adena count depends on config
				iCount = iCount * Config.PLAYER_RATE_DROP_ADENA / 100;
				// Drop only adena this time
				if (itemDrop != null && itemDrop.getItemId() == 57) // Adena
				{
					dropItem("DieDrop", itemDrop.getObjectId(), iCount, getPosition().getX() + Rnd.get(50) - 25, getPosition().getY() + Rnd.get(50) - 25,
							getPosition().getZ() + 20, killer, true);
				}
			}
		}
	}

	private void onDieUpdateKarma()
	{
		// Karma lose for server that does not allow delevel
		if (getKarma() > 0)
		{
			// This formula seems to work relatively well:
			// baseKarma * thisLVL * (thisLVL/100)
			// Calculate the new Karma of the attacker : newKarma = baseKarma*pkCountMulti*lvlDiffMulti
			double karmaLost = Config.KARMA_LOST_BASE;
			karmaLost *= getLevel(); // Multiply by char lvl
			karmaLost *= (getLevel() / 100.0); // Divide by 0.charLVL
			karmaLost = Math.round(karmaLost);
			if (karmaLost < 0)
				karmaLost = 1;

			// Decrease Karma of the L2PcInstance and Send it a Server->Client StatusUpdate packet with Karma and PvP Flag if necessary
			setKarma(getKarma() - (int) karmaLost);
		}
	}

	public void onKillUpdatePvPKarma(L2Character target)
	{
		if (target == null)
			return;
		if (!(target instanceof L2Playable))
			return;

		L2PcInstance targetPlayer = target.getActingPlayer();

		if (targetPlayer == null)
			return; // Target player is null
		if (targetPlayer == this)
			return; // Target player is self

		if (isCursedWeaponEquipped())
		{
			CursedWeaponsManager.getInstance().increaseKills(_cursedWeaponEquippedId);
			// Custom message for time left
			// CursedWeapon cw = CursedWeaponsManager.getInstance().getCursedWeapon(_cursedWeaponEquippedId);
			// SystemMessage msg = new SystemMessage(SystemMessageId.THERE_IS_S1_HOUR_AND_S2_MINUTE_LEFT_OF_THE_FIXED_USAGE_TIME);
			// int timeLeftInHours = (int)(((cw.getTimeLeft()/60000)/60));
			// msg.addItemName(_cursedWeaponEquippedId);
			// msg.addNumber(timeLeftInHours);
			// sendPacket(msg);
			return;
		}

		// If in duel and you kill (only can kill l2summon), do nothing
		if (isInDuel() && targetPlayer.isInDuel())
			return;

		// If in Arena, do nothing
		if (isInsideZone(L2Zone.FLAG_PVP))
			return;

		// Check if it's pvp
		if ((checkIfPvP(target) && //  Can pvp and
				targetPlayer.getPvpFlag() != 0 // Target player has pvp flag set
				)
				|| // or
				(isInsideZone(L2Zone.FLAG_PVP) && // Player is inside pvp zone and
				targetPlayer.isInsideZone(L2Zone.FLAG_PVP) // Target player is inside pvp zone
				))
		{
			if (target instanceof L2PcInstance)
				increasePvpKills();
			// Give faction pvp points
			if (Config.FACTION_ENABLED && targetPlayer.getSide() != getSide() && targetPlayer.getSide() != 0 && getSide() != 0 && Config.FACTION_KILL_REWARD)
				increaseFactionKillPoints(targetPlayer.getLevel(), false);
		}
		else
		// Target player doesn't have pvp flag set
		{
			// Check factions
			if (Config.FACTION_ENABLED && targetPlayer.getSide() != getSide() && targetPlayer.getSide() != 0 && getSide() != 0 && Config.FACTION_KILL_REWARD)
			{
				// Give faction pk points
				increaseFactionKillPoints(targetPlayer.getLevel(), true);
				// No karma
				return;
			}

			// Check about wars
			boolean clanWarKill = (targetPlayer.getClan() != null && getClan() != null && !isAcademyMember() && !(targetPlayer.isAcademyMember())
					&& _clan.isAtWarWith(targetPlayer.getClanId()) && targetPlayer.getClan().isAtWarWith(_clan.getClanId()));
			if (clanWarKill)
			{
				// 'Both way war' -> 'PvP Kill'
				if (target instanceof L2PcInstance)
					increasePvpKills();
				return;
			}

			// 'No war' or 'One way war' -> 'Normal PK'
			if (targetPlayer.getKarma() > 0) // Target player has karma
			{
				if (Config.KARMA_AWARD_PK_KILL)
				{
					if (target instanceof L2PcInstance)
						increasePvpKills();
				}
			}
			else if (targetPlayer.getPvpFlag() == 0) // Target player doesn't have karma
			{
				increasePkKillsAndKarma(targetPlayer.getLevel(), target instanceof L2PcInstance);
				// Unequip adventurer items
				if (getInventory().getPaperdollItemId(7) >= 7816 && getInventory().getPaperdollItemId(7) <= 7831)
				{
					L2ItemInstance invItem = getInventory().getItemByItemId(getInventory().getPaperdollItemId(7));
					if (invItem.isEquipped())
					{
						L2ItemInstance[] unequiped = getInventory().unEquipItemInSlotAndRecord(invItem.getLocationSlot());
						InventoryUpdate iu = new InventoryUpdate();
						for (L2ItemInstance itm: unequiped)
							iu.addModifiedItem(itm);
						sendPacket(iu);
					}
					refreshExpertisePenalty();
					sendPacket(SystemMessageId.YOU_ARE_UNABLE_TO_EQUIP_THIS_ITEM_WHEN_YOUR_PK_COUNT_IS_GREATER_THAN_OR_EQUAL_TO_ONE);
				}
			}
		}
	}

	/**
	 * Increase the faction points depending on level
	 * PK Kills give half the points of a PVP Kill
	 */
	public void increaseFactionKillPoints(int level, boolean pk)
	{
		int points;
		points = (level / getLevel()) * (Config.FACTION_KILL_RATE / 100);
		if (pk)
			points /= 2;
		_faction.addFactionPoints(points);
		sendMessage("You earned " + String.valueOf(points) + " Facion Points");
	}

	/**
	 * Increase the pvp kills count and send the info to the player
	 *
	 */
	private void increasePvpKills()
	{
		// Add karma to attacker and increase its PK counter
		setPvpKills(getPvpKills() + 1);

		// Send a Server->Client UserInfo packet to attacker with its Karma and PK Counter
		sendPacket(new UserInfo(this));
	}

	/**
	 * Increase pk count, karma and send the info to the player
	 *
	 * @param targLVL : level of the killed player
	 * @param increasePk : true if PK counter should be increased too
	 */
	private void increasePkKillsAndKarma(int targLVL, boolean increasePk)
	{
		int baseKarma = (int)(Config.KARMA_MIN_KARMA * Config.KARMA_RATE);
		int newKarma = baseKarma;
		int karmaLimit = (int)(Config.KARMA_MAX_KARMA * Config.KARMA_RATE);

		int pkLVL = getLevel();
		int pkPKCount = getPkKills();

		int lvlDiffMulti = 0;
		int pkCountMulti = 0;

		// Check if the attacker has a PK counter greater than 0
		if (pkPKCount > 0)
			pkCountMulti = pkPKCount / 2;
		else
			pkCountMulti = 1;
		if (pkCountMulti < 1)
			pkCountMulti = 1;

		// Calculate the level difference Multiplier between attacker and killed L2PcInstance
		if (pkLVL > targLVL)
			lvlDiffMulti = pkLVL / targLVL;
		else
			lvlDiffMulti = 1;
		if (lvlDiffMulti < 1)
			lvlDiffMulti = 1;

		// Calculate the new Karma of the attacker : newKarma = baseKarma*pkCountMulti*lvlDiffMulti
		newKarma = (int)(newKarma * pkCountMulti * lvlDiffMulti * Config.KARMA_RATE);

		// Make sure newKarma is less than karmaLimit and higher than baseKarma
		if (newKarma < baseKarma)
			newKarma = baseKarma;
		if (newKarma > karmaLimit)
			newKarma = karmaLimit;

		// Fix to prevent overflow (=> karma has a  max value of 2 147 483 647)
		if (getKarma() > (Integer.MAX_VALUE - newKarma))
			newKarma = Integer.MAX_VALUE - getKarma();

		// Add karma to attacker and increase its PK counter
		if (increasePk)
			setPkKills(getPkKills() + 1);
		setKarma(getKarma() + newKarma);

		// Send a Server->Client UserInfo packet to attacker with its Karma and PK Counter
		sendPacket(new UserInfo(this));
	}

	public int calculateKarmaLost(long exp)
	{
		// KARMA LOSS
		// When a Player Killer gets killed by another player or a L2MonsterInstance, it loses a certain amount of Karma based on their level.
		// this (with defaults) results in a level 1 losing about ~2 karma per death, and a lvl 70 loses about 11760 karma per death...
		// You lose karma as long as you were not in a pvp zone and you did not kill urself.
		// NOTE: exp for death (if delevel is allowed) is based on the players level

		long expGained = Math.abs(exp);
		expGained /= Config.KARMA_XP_DIVIDER;

		int karmaLost = 0;
		if (expGained > Integer.MAX_VALUE)
			karmaLost = Integer.MAX_VALUE;
		else
			karmaLost = (int) expGained;

		if (karmaLost < Config.KARMA_LOST_BASE)
			karmaLost = Config.KARMA_LOST_BASE;
		if (karmaLost > getKarma())
			karmaLost = getKarma();

		return karmaLost;
	}

	private static final class PvPFlagManager extends AbstractIterativePeriodicTaskManager<L2PcInstance>
	{
		private static final PvPFlagManager _instance = new PvPFlagManager();

		private static PvPFlagManager getInstance()
		{
			return _instance;
		}

		private PvPFlagManager()
		{
			super(1000);
		}

		@Override
		protected void callTask(L2PcInstance task)
		{
			if (System.currentTimeMillis() > task.getPvpFlagLasts())
			{
				task.stopPvPFlag();
			}
			else if (System.currentTimeMillis() > (task.getPvpFlagLasts() - 20000))
			{
				task.updatePvPFlag(2);
			}
			else
			{
				task.updatePvPFlag(1);
			}
		}

		@Override
		protected String getCalledMethodName()
		{
			return "updatePvPFlag()";
		}
	}

	/** The PvP Flag state of the L2PcInstance (0=White, 1=Purple) */
	private byte _pvpFlag;
	private long _pvpFlagLasts;

	/**
	 * Set the PvP Flag of the L2PcInstance.<BR>
	 * <BR>
	 */
	private void setPvpFlag(int pvpFlag)
	{
		_pvpFlag = (byte)pvpFlag;
	}

	public byte getPvpFlag()
	{
		return _pvpFlag;
	}

	public void updatePvPFlag(int value)
	{
		if (getPvpFlag() == value)
			return;

		setPvpFlag((byte)value);

		if (getPvpFlag() == 0)
			PvPFlagManager.getInstance().stopTask(this);
		else
			PvPFlagManager.getInstance().startTask(this);

		sendPacket(new UserInfo(this));
		broadcastRelationChanged();
	}

	private void setPvpFlagLasts(long time)
	{
		_pvpFlagLasts = time;
	}

	private long getPvpFlagLasts()
	{
		return _pvpFlagLasts;
	}

	private void startPvPFlag()
	{
		updatePvPFlag(1);
	}

	private void stopPvPFlag()
	{
		updatePvPFlag(0);
	}

	public void updatePvPStatus()
	{
		if (isInsideZone(L2Zone.FLAG_PVP))
			return;
		
		setPvpFlagLasts(System.currentTimeMillis() + Config.PVP_NORMAL_TIME);

		if (getPvpFlag() == 0)
			startPvPFlag();
	}

	public void updatePvPStatus(L2Character target)
	{
		L2PcInstance player_target = target.getActingPlayer();

		if (player_target == null)
			return;
		
		if ((isInDuel() && player_target.getDuelId() == getDuelId()))
			return;
		if ((!isInsideZone(L2Zone.FLAG_PVP) || !player_target.isInsideZone(L2Zone.FLAG_PVP)) && player_target.getKarma() == 0)
		{
			if (checkIfPvP(player_target))
				setPvpFlagLasts(System.currentTimeMillis() + Config.PVP_PVP_TIME);
			else
				setPvpFlagLasts(System.currentTimeMillis() + Config.PVP_NORMAL_TIME);
			if (getPvpFlag() == 0)
				startPvPFlag();
		}
	}

	/**
	 * Restore the specified % of experience this L2PcInstance has
	 * lost and sends a Server->Client StatusUpdate packet.<BR><BR>
	 */
	public void restoreExp(double restorePercent)
	{
		if (getExpBeforeDeath() > 0)
		{
			// Restore the specified % of lost experience.
			getStat().addExp((int) Math.round((getExpBeforeDeath() - getExp()) * restorePercent / 100));
			setExpBeforeDeath(0);
		}
	}

	/**
	 * Reduce the Experience (and level if necessary) of the L2PcInstance in function of the calculated Death Penalty.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Calculate the Experience loss </li>
	 * <li>Set the value of _expBeforeDeath </li>
	 * <li>Set the new Experience value of the L2PcInstance and Decrease its level if necessary </li>
	 * <li>Send a Server->Client StatusUpdate packet with its new Experience </li><BR><BR>
	 *
	 */
	public void deathPenalty(boolean atwar, boolean killed_by_pc, boolean charmOfCourage, boolean killed_by_siege_npc)
	{
		if (charmOfCourage && isInSiege())
			return;

		if ((killed_by_pc || killed_by_siege_npc)
			&& ((isInsideZone(L2Zone.FLAG_PVP) && !isInSiege()) || isInSiege()))
			return;
		
		// FIXME: Need Correct Penalty

		// Get the level of the L2PcInstance
		final int lvl = getLevel();

		byte level = (byte)getLevel();

		int clan_luck = getSkillLevel(L2Skill.SKILL_CLAN_LUCK);

		double clan_luck_modificator = 1.0;

		if (!killed_by_pc)
		{
			switch (clan_luck)
			{
				case 3:
					clan_luck_modificator = 0.8;
					break;
				case 2:
					clan_luck_modificator = 0.8;
					break;
				case 1:
					clan_luck_modificator = 0.88;
					break;
				default:
					clan_luck_modificator = 1.0;
					break;
			}
		}
		else
		{
			switch (clan_luck)
			{
				case 3:
					clan_luck_modificator = 0.5;
					break;
				case 2:
					clan_luck_modificator = 0.5;
					break;
				case 1:
					clan_luck_modificator = 0.5;
					break;
				default:
					clan_luck_modificator = 1.0;
					break;
			}
		}

		// The death steal you some Exp
		double percentLost = Config.PLAYER_XP_PERCENT_LOST[getLevel()]*clan_luck_modificator;

		switch (level)
		{
			case 78:
				percentLost = (1.5*clan_luck_modificator);
				break;
			case 77:
				percentLost = (2.0*clan_luck_modificator);
				break;
			case 76:
				percentLost = (2.5*clan_luck_modificator);
				break;
			default:
				if (level < 40)
					percentLost = (7.0*clan_luck_modificator);
				else if (level >= 40 && level <= 75)
					percentLost = (4.0*clan_luck_modificator);
				break;
		}

		if (getKarma() > 0)
			percentLost *= Config.RATE_KARMA_EXP_LOST;

		if (isFestivalParticipant() || atwar)
			percentLost /= 4.0;

		// Calculate the Experience loss
		final long lostExp;
		
		if (lvl < Experience.MAX_LEVEL)
			lostExp = Math.round((getStat().getExpForLevel(lvl + 1) - getStat().getExpForLevel(lvl)) * percentLost / 100);
		else
			lostExp = Math.round((getStat().getExpForLevel(Experience.MAX_LEVEL) - getStat().getExpForLevel(Experience.MAX_LEVEL - 1)) * percentLost / 100);
		

		if (_log.isDebugEnabled())
			_log.debug(getName() + " died and lost " + lostExp + " experience.");

		// Get the Experience before applying penalty
		setExpBeforeDeath(getExp());
		
		// Set the new Experience value of the L2PcInstance
		getStat().addExp(-lostExp);
	}

	public void deathPenalty(boolean atwar, boolean killed_by_pc, boolean killed_by_siege_npc)
	{
		deathPenalty(atwar, killed_by_pc, getCharmOfCourage(), killed_by_siege_npc);
	}

	/**
	 * @param b
	 */
	public void setPartyMatchingAutomaticRegistration(boolean b)
	{
		_partyMatchingAutomaticRegistration = b;
	}

	/**
	 * @param b
	 */
	public void setPartyMatchingShowLevel(boolean b)
	{
		_partyMatchingShowLevel = b;
	}

	/**
	 * @param b
	 */
	public void setPartyMatchingShowClass(boolean b)
	{
		_partyMatchingShowClass = b;
	}

	/**
	 * @param memo
	 */
	public void setPartyMatchingMemo(String memo)
	{
		_partyMatchingMemo = memo;
	}

	public boolean isPartyMatchingAutomaticRegistration()
	{
		return _partyMatchingAutomaticRegistration;
	}

	public String getPartyMatchingMemo()
	{
		return _partyMatchingMemo;
	}

	public boolean isPartyMatchingShowClass()
	{
		return _partyMatchingShowClass;
	}

	public boolean isPartyMatchingShowLevel()
	{
		return _partyMatchingShowLevel;
	}

	/**
	 * Stop the HP/MP/CP Regeneration task.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Set the RegenActive flag to False </li>
	 * <li>Stop the HP/MP/CP Regeneration task </li><BR><BR>
	 *
	 */
	public void stopAllTimers()
	{
		getStatus().stopHpMpRegeneration();
		stopWarnUserTakeBreak();
		stopAutoSaveTask();
		stopWaterTask();

		stopFeed();
		clearPetData();
		storePetFood(_mountNpcId);

		stopSoulTask();
		stopChargeTask();
		stopFameTask();

		stopPvPFlag();
		stopJailTask(true);

		if (Config.ENABLE_VITALITY)
			stopVitalityTask();
	}

	/**
	 * Return the L2Summon of the L2PcInstance or null.<BR><BR>
	 */
	@Override
	public L2Summon getPet()
	{
		return _summon;
	}

	/**
	 * Return the L2Decoy of the L2PcInstance or null.<BR><BR>
	 */
	public L2Decoy getDecoy()
	{
		return _decoy;
	}

	/**
	 * Return the L2Trap of the L2PcInstance or null.<BR><BR>
	 */
	public L2Trap getTrap()
	{
		return _trap;
	}

	/**
	 * Set the L2Summon of the L2PcInstance.<BR><BR>
	 */
	public void setPet(L2Summon summon)
	{
		_summon = summon;
		// update attack element value display
		if ((_summon == null || _summon instanceof L2SummonInstance)
				&& getClassId().isSummoner() && getAttackElement() != Elementals.NONE)
			sendPacket(new UserInfo(this));
	}

	/**
	 * Set the L2Decoy of the L2PcInstance.<BR><BR>
	 */
	public void setDecoy(L2Decoy decoy)
	{
		_decoy = decoy;
	}

	/**
	 * Set the L2Trap of this L2PcInstance<BR><BR>
	 * @param trap
	 */
	public void setTrap(L2Trap trap)
	{
		_trap = trap;
	}

	/**
	 * Return the L2Summon of the L2PcInstance or null.<BR><BR>
	 */
	public L2TamedBeastInstance getTrainedBeast()
	{
		return _tamedBeast;
	}

	/**
	 * Set the L2Summon of the L2PcInstance.<BR><BR>
	 */
	public void setTrainedBeast(L2TamedBeastInstance tamedBeast)
	{
		_tamedBeast = tamedBeast;
	}

	/**
	 * Return the L2PcInstance requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...).<BR><BR>
	 */
	public L2Request getRequest()
	{
		if (_request == null)
			_request = new L2Request(this);

		return _request;
	}

	/**
	 * Set the L2PcInstance requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...).<BR><BR>
	 */
	public synchronized void setActiveRequester(L2PcInstance requester)
	{
		_activeRequester = requester;
	}

	/**
	 * Return true if last request is expired.
	 * @return
	 */
	public boolean isRequestExpired()
	{
		return !(_requestExpireTime > GameTimeController.getGameTicks());
	}

	/**
	 * Return the L2PcInstance requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...).<BR><BR>
	 */
	public L2PcInstance getActiveRequester()
	{
		return _activeRequester;
	}

	/**
	 * Return True if a transaction is in progress.<BR><BR>
	 */
	public boolean isProcessingRequest()
	{
		return _activeRequester != null || _requestExpireTime > GameTimeController.getGameTicks();
	}

	/**
	 * Return True if a transaction is in progress.<BR><BR>
	 */
	public boolean isProcessingTransaction()
	{
		return _activeRequester != null || _activeTradeList != null || _requestExpireTime > GameTimeController.getGameTicks();
	}

	/**
	 * Select the Warehouse to be used in next activity.<BR><BR>
	 */
	public void onTransactionRequest(L2PcInstance partner)
	{
		_requestExpireTime = GameTimeController.getGameTicks() + REQUEST_TIMEOUT * GameTimeController.TICKS_PER_SECOND;
		partner.setActiveRequester(this);
	}

	/**
	 * Select the Warehouse to be used in next activity.<BR><BR>
	 */
	public void onTransactionResponse()
	{
		_requestExpireTime = 0;
	}

	/**
	 * Select the Warehouse to be used in next activity.<BR><BR>
	 */
	public void setActiveWarehouse(ItemContainer warehouse)
	{
		_activeWarehouse = warehouse;
	}

	/**
	 * Return active Warehouse.<BR><BR>
	 */
	public ItemContainer getActiveWarehouse()
	{
		return _activeWarehouse;
	}

	/**
	 * Select the TradeList to be used in next activity.<BR><BR>
	 */
	public void setActiveTradeList(TradeList tradeList)
	{
		_activeTradeList = tradeList;
	}

	/**
	 * Return active TradeList.<BR><BR>
	 */
	public TradeList getActiveTradeList()
	{
		return _activeTradeList;
	}

	public void onTradeStart(L2PcInstance partner)
	{
		_activeTradeList = new TradeList(this);
		_activeTradeList.setPartner(partner);

		SystemMessage msg = new SystemMessage(SystemMessageId.BEGIN_TRADE_WITH_C1);
		msg.addPcName(partner);
		sendPacket(msg);
		sendPacket(new TradeStart(this));
	}

	public void onTradeConfirm(L2PcInstance partner)
	{
		SystemMessage msg = new SystemMessage(SystemMessageId.C1_CONFIRMED_TRADE);
		msg.addPcName(partner);
		sendPacket(msg);
		sendPacket(TradeOtherDone.STATIC_PACKET);
	}

	public void onTradeCancel(L2PcInstance partner)
	{
		if (_activeTradeList == null)
			return;

		_activeTradeList.lock();
		_activeTradeList = null;
		sendPacket(new TradeDone(0));
		SystemMessage msg = new SystemMessage(SystemMessageId.C1_CANCELED_TRADE);
		msg.addPcName(partner);
		sendPacket(msg);
	}

	public void onTradeFinish(boolean successfull)
	{
		_activeTradeList = null;
		sendPacket(new TradeDone(1));
		if (successfull)
			sendPacket(SystemMessageId.TRADE_SUCCESSFUL);
	}

	public void startTrade(L2PcInstance partner)
	{
		onTradeStart(partner);
		partner.onTradeStart(this);
	}

	public void cancelActiveTrade()
	{
		if (_activeTradeList == null)
			return;

		L2PcInstance partner = _activeTradeList.getPartner();
		if (partner != null)
			partner.onTradeCancel(this);
		onTradeCancel(this);
	}

	/**
	 * Return the _createList object of the L2PcInstance.<BR><BR>
	 */
	public L2ManufactureList getCreateList()
	{
		return _createList;
	}

	/**
	 * Set the _createList object of the L2PcInstance.<BR><BR>
	 */
	public void setCreateList(L2ManufactureList x)
	{
		_createList = x;
	}

	/**
	 * Return the _buyList object of the L2PcInstance.<BR><BR>
	 */
	public TradeList getSellList()
	{
		if (_sellList == null)
			_sellList = new TradeList(this);
		return _sellList;
	}

	/**
	 * Return the _buyList object of the L2PcInstance.<BR><BR>
	 */
	public TradeList getBuyList()
	{
		if (_buyList == null)
			_buyList = new TradeList(this);
		return _buyList;
	}

	/**
	 * Set the Private Store type of the L2PcInstance.<BR><BR>
	 *
	 * <B><U> Values </U> :</B><BR><BR>
	 * <li>0 : STORE_PRIVATE_NONE</li>
	 * <li>1 : STORE_PRIVATE_SELL</li>
	 * <li>2 : sellmanage</li><BR>
	 * <li>3 : STORE_PRIVATE_BUY</li><BR>
	 * <li>4 : buymanage</li><BR>
	 * <li>5 : STORE_PRIVATE_MANUFACTURE</li><BR>
	 *
	 */
	public void setPrivateStoreType(int type)
	{
		_privatestore = type;
	}

	/**
	 * Return the Private Store type of the L2PcInstance.<BR><BR>
	 *
	 * <B><U> Values </U> :</B><BR><BR>
	 * <li>0 : STORE_PRIVATE_NONE</li>
	 * <li>1 : STORE_PRIVATE_SELL</li>
	 * <li>2 : sellmanage</li><BR>
	 * <li>3 : STORE_PRIVATE_BUY</li><BR>
	 * <li>4 : buymanage</li><BR>
	 * <li>5 : STORE_PRIVATE_MANUFACTURE</li><BR>
	 *
	 */
	public int getPrivateStoreType()
	{
		return _privatestore;
	}

	/**
	 * Set the _skillLearningClassId object of the L2PcInstance.<BR><BR>
	 */
	public void setSkillLearningClassId(ClassId classId)
	{
		_skillLearningClassId = classId;
	}

	/**
	 * Return the _skillLearningClassId object of the L2PcInstance.<BR><BR>
	 */
	public ClassId getSkillLearningClassId()
	{
		return _skillLearningClassId;
	}

	/**
	 * Set the _clan object, _clanId, _clanLeader Flag and title of the L2PcInstance.<BR><BR>
	 */
	public void setClan(L2Clan clan)
	{
		_clan = clan;
		setTitle("");

		if (clan == null)
		{
			_clanId = 0;
			_clanPrivileges = 0;
			_subPledgeType = 0;
			_pledgeRank = 0;
			_lvlJoinedAcademy = 0;
			_apprentice = 0;
			_sponsor = 0;
			return;
		}

		if (!clan.isMember(getObjectId()))
		{
			// Char has been kicked from clan
			setClan(null);
			return;
		}

		_clanId = clan.getClanId();
	}

	/**
	 * Return the _clan object of the L2PcInstance.<BR><BR>
	 */
	public L2Clan getClan()
	{
		return _clan;
	}

	/**
	 * Return True if the L2PcInstance is the leader of its clan.<BR><BR>
	 */
	public boolean isClanLeader()
	{
		return (getClan() != null) && getObjectId() == getClan().getLeaderId();
	}

	/**
	 * Disarm the player's weapon and shield.<BR><BR>
	 */
	public boolean disarmWeapons(boolean shield)
	{
		// Don't allow disarming a cursed weapon
		if (isCursedWeaponEquipped())
			return false;

		// Unequip the weapon
		L2ItemInstance wpn = getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		if (wpn == null)
			wpn = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if (wpn != null)
		{
			if (wpn.isWear())
				return false;

			L2ItemInstance[] unequipped = getInventory().unEquipItemInBodySlotAndRecord(wpn.getItem().getBodyPart());
			InventoryUpdate iu = new InventoryUpdate();
			for (L2ItemInstance element : unequipped)
				iu.addModifiedItem(element);
			sendPacket(iu);

			abortAttack();
			refreshExpertisePenalty();
			broadcastUserInfo();

			// This can be 0 if the user pressed the right mousebutton twice very fast
			if (unequipped.length > 0)
			{
				SystemMessage sm = null;
				if (unequipped[0].getEnchantLevel() > 0)
				{
					sm = new SystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
					sm.addNumber(unequipped[0].getEnchantLevel());
					sm.addItemName(unequipped[0]);
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.S1_DISARMED);
					sm.addItemName(unequipped[0]);
				}
				sendPacket(sm);
			}
		}

		if (!shield)
			return true;

		// Unequip the shield
		L2ItemInstance sld = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if (sld != null)
		{
			if (sld.isWear())
				return false;

			L2ItemInstance[] unequipped = getInventory().unEquipItemInBodySlotAndRecord(sld.getItem().getBodyPart());
			InventoryUpdate iu = new InventoryUpdate();
			for (L2ItemInstance element : unequipped)
				iu.addModifiedItem(element);
			sendPacket(iu);

			abortAttack();
			refreshExpertisePenalty();
			broadcastUserInfo();

			// This can be 0 if the user pressed the right mousebutton twice very fast
			if (unequipped.length > 0)
			{
				SystemMessage sm = null;
				if (unequipped[0].getEnchantLevel() > 0)
				{
					sm = new SystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
					sm.addNumber(unequipped[0].getEnchantLevel());
					sm.addItemName(unequipped[0]);
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.S1_DISARMED);
					sm.addItemName(unequipped[0]);
				}
				sendPacket(sm);
			}
		}

		return true;
	}

	/**
	 * Reduce the number of arrows/bolts owned by the L2PcInstance and send it Server->Client Packet InventoryUpdate or ItemList (to unequip if the last arrow was consummed).<BR><BR>
	 */
	@Override
	protected void reduceArrowCount(boolean bolts)
	{
		L2ItemInstance arrows = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);

		if (arrows == null)
		{
			getInventory().unEquipItemInSlot(Inventory.PAPERDOLL_LHAND);
			if (bolts)
				_boltItem = null;
			else
				_arrowItem = null;
			sendPacket(new ItemList(this, false));
			return;
		}

		// Adjust item quantity
		if (arrows.getCount() > 1)
		{
			synchronized (arrows)
			{
				arrows.changeCountWithoutTrace(-1, this, null);
				arrows.setLastChange(L2ItemInstance.MODIFIED);

				// Could do also without saving, but let's save approx 1 of 10
				if (GameTimeController.getGameTicks() % 10 == 0)
					arrows.updateDatabase();
				getInventory().refreshWeight();
			}
		}
		else
		{
			// Destroy entire item and save to database
			getInventory().destroyItem("Consume", arrows, this, null);

			getInventory().unEquipItemInSlot(Inventory.PAPERDOLL_LHAND);
			if (bolts)
				_boltItem = null;
			else
				_arrowItem = null;

			if (_log.isDebugEnabled())
				_log.debug("removed arrows count");
			sendPacket(new ItemList(this, false));
			return;
		}

		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(arrows);
			sendPacket(iu);
		}
		else
			sendPacket(new ItemList(this, false));
	}

	/**
	 * Equip arrows needed in left hand and send a Server->Client packet ItemList to the L2PcINstance then return True.<BR><BR>
	 */
	@Override
	protected boolean checkAndEquipArrows()
	{
		// Check if nothing is equipped in left hand
		if (getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND) == null)
		{
			// Get the L2ItemInstance of the arrows needed for this bow
			_arrowItem = getInventory().findArrowForBow(getActiveWeaponItem());

			if (_arrowItem != null)
			{
				// Equip arrows needed in left hand
				getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, _arrowItem);
				// Send inventory update packet
				getInventory().updateInventory(_arrowItem);
			}
		}
		else
		{
			// Get the L2ItemInstance of arrows equipped in left hand
			_arrowItem = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		}

		return _arrowItem != null;
	}

	/**
	 * Equip bolts needed in left hand and send a Server->Client packet ItemList to the L2PcINstance then return True.<BR><BR>
	 */
	@Override
	protected boolean checkAndEquipBolts()
	{
		// Check if nothing is equipped in left hand
		if (getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND) == null)
		{
			// Get the L2ItemInstance of the arrows needed for this bow
			_boltItem = getInventory().findBoltForCrossBow(getActiveWeaponItem());

			if (_boltItem != null)
			{
				// Equip arrows needed in left hand
				getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, _boltItem);

				// Send a Server->Client packet ItemList to this L2PcINstance to update left hand equipement
				ItemList il = new ItemList(this, false);
				sendPacket(il);
			}
		}
		else
		{
			// Get the L2ItemInstance of arrows equipped in left hand
			_boltItem = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		}

		return _boltItem != null;
	}

	public boolean mount(L2Summon pet)
	{
		// TODO: all checks from usercommandhandler mount and requestactionuse should be handled in ONE place, and this is not l2pcinstance
		// so this is temporary
		if (!isInsideRadius(pet, 80, true, false))
		{
			sendPacket(SystemMessageId.TOO_FAR_AWAY_FROM_FENRIR_TO_MOUNT);
			return false;
		}
		else if (!GeoData.getInstance().canSeeTarget(this, pet))
		{
			sendPacket(SystemMessageId.CANT_SEE_TARGET);
			return false;
		}

		if (!disarmWeapons(true))
			return false;
		if (isTransformed())
			return false;

		for (L2Effect e : getAllEffects())
		{
			if (e != null && e.getSkill().isToggle())
				e.exit();
		}

		Ride mount = new Ride(this, true, pet.getTemplate().getNpcId());
		setMount(pet.getNpcId(), pet.getLevel(), mount.getMountType());
		setMountObjectID(pet.getControlItemId());
		clearPetData();
		startFeed(pet.getNpcId());
		broadcastPacket(mount);

		// Notify self and others about speed change
		broadcastUserInfo();

		pet.unSummon(this);

		return true;
	}

	public boolean remount(L2PcInstance player)
	{
		Ride dismount = new Ride(this, false, 0);
		Ride mount = new Ride(this, true, getMountNpcId());

		player.sendPacket(dismount);
		player.sendPacket(mount);
		return true;
	}

	public boolean mount(int npcId, int controlItemObjId, boolean useFood)
	{
		if (!disarmWeapons(true))
			return false;
		if (isTransformed())
			return false;

		for (L2Effect e : getAllEffects())
		{
			if (e != null && e.getSkill().isToggle())
				e.exit();
		}

		Ride mount = new Ride(this, true, npcId);
		if (setMount(npcId, getLevel(), mount.getMountType()))
		{
			clearPetData();
			setMountObjectID(controlItemObjId);
			broadcastPacket(mount);
			// Notify self and others about speed change
			broadcastUserInfo();
			if (useFood)
				startFeed(npcId);
			return true;
		}
		return false;
	}

	public boolean mountPlayer(L2Summon pet)
	{
		if (pet != null && pet.isMountable() && !isMounted() && !isBetrayed() && !pet.isOutOfControl())
		{
			if (pet.getNpcId() == 16030 && pet.getLevel() < Config.GREAT_WOLF_MOUNT_LEVEL)
			{
				sendMessage("Your Wolf needs minimum level " + Config.GREAT_WOLF_MOUNT_LEVEL);
				return false;
			}
			else if (isInEvent(CTFPlayerInfo.class) && as(CTFPlayerInfo.class)._haveFlagCTF)
			{
				// You cannot mount a steed while holding a flag.
				sendPacket(SystemMessageId.YOU_CANNOT_MOUNT_A_STEED_WHILE_HOLDING_A_FLAG);
				return false;
			}
			else if (isParalyzed())
			{
				// You cannot mount a steed while petrified.
				sendPacket(SystemMessageId.YOU_CANNOT_MOUNT_A_STEED_WHILE_PETRIFIED);
				return false;
			}
			else if (isDead())
			{
				//A strider cannot be ridden when dead
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_DEAD);
				return false;
			}
			else if (pet.isDead())
			{
				//A dead strider cannot be ridden.
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.DEAD_STRIDER_CANT_BE_RIDDEN);
				return false;
			}
			else if (pet.isInCombat() || pet.isRooted() || pet.isParalyzed())
			{
				//A strider in battle cannot be ridden
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.STRIDER_IN_BATLLE_CANT_BE_RIDDEN);
				return false;

			}
			else if (isInCombat())
			{
				//A strider cannot be ridden while in battle
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE);
				return false;
			}
			else if (isSitting() || isInsideZone(L2Zone.FLAG_WATER))
			{
				//A strider can be ridden only when standing
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.STRIDER_CAN_BE_RIDDEN_ONLY_WHILE_STANDING);
				return false;
			}
			else if (isFishing())
			{
				//You can't mount, dismount, break and drop items while fishing
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_2);
				return false;
			}
			else if (isInDuel())
			{
				// You cannot mount a steed while in a duel.
				sendPacket(SystemMessageId.YOU_CANNOT_MOUNT_A_STEED_WHILE_IN_A_DUEL);
				return false;
			}
			else if (isTransformed() || isCursedWeaponEquipped())
			{
				// no message needed, player while transformed doesn't have mount action
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
			else if (getInventory().getItemByItemId(9819) != null)
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.YOU_CANNOT_MOUNT_A_STEED_WHILE_HOLDING_A_FLAG); // TODO: confirm this message
				return false;
			}
			else if (isCastingNow())
			{
				// You cannot mount a steed while skill casting.
				sendPacket(SystemMessageId.YOU_CANNOT_MOUNT_A_STEED_WHILE_SKILL_CASTING);
				return false;
			}
			else if (pet.isHungry())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.HUNGRY_STRIDER_NOT_MOUNT);
				return false;
			}
			else if (!Util.checkIfInRange(200, this, pet, true))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.TOO_FAR_AWAY_FROM_FENRIR_TO_MOUNT);
				return false;
			}
			else if (!pet.isDead() && !isMounted())
			{
				mount(pet);
			}
		}
		else if (isRentedPet())
		{
			stopRentPet();
		}
		else if (isMounted())
		{
			if (getMountType() == 2 && isInsideZone(L2Zone.FLAG_NOWYVERN))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(new SystemMessage(SystemMessageId.NO_DISMOUNT_HERE));
				return false;
			}
			else if (isHungry())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(new SystemMessage(SystemMessageId.HUNGRY_STRIDER_NOT_MOUNT));
				return false;
			}
			else if (ObjectRestrictions.getInstance().checkRestriction(this, AvailableRestriction.PlayerUnmount))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendMessage("You cannot dismount due to a restriction.");
				return false;
			}
			else
				dismount();
		}
		return true;
	}

	public boolean dismount()
	{
		boolean wasFlying = isFlying();

		sendPacket(new SetupGauge(3, 0, 0));
		int petId = _mountNpcId;
		if (setMount(0, 0, 0 ))
		{
			stopFeed();
			clearPetData();

			if (wasFlying)
				removeSkill(SkillTable.getInstance().getInfo(4289, 1));
			Ride dismount = new Ride(this, false, 0);
			broadcastPacket(dismount);
			setMountObjectID(0);
			storePetFood(petId);

			// Notify self and others about speed change
			broadcastUserInfo();
			return true;
		}
		return false;
	}

	/**
	 * Return True if the L2PcInstance use a dual weapon.<BR><BR>
	 */
	@Override
	public boolean isUsingDualWeapon()
	{
		L2Weapon weaponItem = getActiveWeaponItem();

		if (weaponItem == null)
			return false;
		if (weaponItem.getItemType() == L2WeaponType.DUAL)
		{
			return true;
		}
		else if (weaponItem.getItemType() == L2WeaponType.DUALFIST)
		{
			return true;
		}
		else if (weaponItem.getItemId() == 248) // Orc fighter fists
		{
			return true;
		}
		else return weaponItem.getItemId() == 252;
	}

	public void setUptime(long time)
	{
		_uptime = time;
	}

	public long getUptime()
	{
		return System.currentTimeMillis() - _uptime;
	}

	public long getOnlineTime()
	{
		long totalOnlineTime = _onlineTime;

		if (_onlineBeginTime > 0)
			totalOnlineTime += (System.currentTimeMillis() - _onlineBeginTime) / 1000;

		return totalOnlineTime;
	}

	/**
	 * Return True if the L2PcInstance is invulnerable.<BR><BR>
	 */
	@Override
	public boolean isInvul()
	{
		return super.isInvul() || _protectEndTime > GameTimeController.getGameTicks();
	}

	/**
	 * Return True if the L2PcInstance has a Party in progress.<BR><BR>
	 */
	@Override
	public boolean isInParty()
	{
		return _party != null;
	}

	/**
	 * Set the _party object of the L2PcInstance (without joining it).<BR><BR>
	 */
	public void setParty(L2Party party)
	{
		_party = party;
	}

	/**
	 * Set the _party object of the L2PcInstance AND join it.<BR><BR>
	 */
	public void joinParty(L2Party party)
	{
		if (party != null)
		{
			// First set the party otherwise this wouldn't be considered
			// as in a party into the L2Character.updateEffectIcons() call.
			_party = party;
			if (!party.addPartyMember(this))
				_party = null;
		}
	}

	/**
	 * Manage the Leave Party task of the L2PcInstance.<BR><BR>
	 */
	public void leaveParty()
	{
		if (isInParty())
		{
			_party.removePartyMember(this);
			_party = null;
		}
	}

	/**
	 * Return the _party object of the L2PcInstance.<BR><BR>
	 */
	@Override
	public L2Party getParty()
	{
		return _party;
	}

	/**
	 * Set the _isGm Flag of the L2PcInstance.<BR><BR>
	 */
	public void setIsGM(boolean status)
	{
		_isGm = status;
	}

	/**
	 * Return True if the L2PcInstance is a GM.<BR><BR>
	 */
	public boolean isGM()
	{
		return _isGm;
	}

	/**
	 * Set the _accessLevel of the L2PcInstance.<BR><BR>
	 */
	public void setAccessLevel(int level)
	{
		_accessLevel = level;
		if (_accessLevel >= Config.GM_MIN)
			setIsGM(true);
	}

	public void setAccountAccesslevel(int level)
	{
		LoginServerThread.getInstance().sendAccessLevel(getAccountName(), level);
	}

	/**
	 * Return the _accessLevel of the L2PcInstance.<BR><BR>
	 */
	public int getAccessLevel()
	{
		return _accessLevel;
	}

	@Override
	public double getLevelMod()
	{
		return (89 + getLevel()) / 100.0;
	}

	/**
	 * Send a Server->Client StatusUpdate packet with Karma and PvP Flag to the L2PcInstance and all L2PcInstance to inform (broadcast).<BR><BR>
	 * @param flag
	 */
	public void setKarmaFlag(int flag)
	{
		sendPacket(new UserInfo(this));
		broadcastRelationChanged();
	}

	/**
	 * Send a Server->Client StatusUpdate packet with Karma to the L2PcInstance and all L2PcInstance to inform (broadcast).<BR><BR>
	 */
	public void broadcastKarma()
	{
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.KARMA, getKarma());
		sendPacket(su);
		broadcastRelationChanged();
	}

	/**
	 * Set the online Flag to True or False and update the characters table of the database with online status and lastAccess (called when login and logout).<BR><BR>
	 */
	public void setOnlineStatus(boolean isOnline)
	{
		final byte value = isOnline ? ONLINE_STATE_ONLINE : ONLINE_STATE_DELETED;

		if (_isOnline != value)
		{
			_isOnline = value;

			// Update the characters table of the database with online status and lastAccess (called when login and logout)
			updateOnlineStatusInDb();
		}
	}

	public void setIsIn7sDungeon(boolean isIn7sDungeon)
	{
		_isIn7sDungeon = isIn7sDungeon;
	}

	/**
	 * Update the characters table of the database with online status and lastAccess of this L2PcInstance (called when
	 * login and logout).<BR>
	 * <BR>
	 */
	private void updateOnlineStatusInDb()
	{
		RecordTable.getInstance().update();

		SQLQueue.getInstance().add(new SQLQuery() {
			public void execute(Connection con)
			{
				try
				{
					PreparedStatement statement = con.prepareStatement("UPDATE characters SET online=?, lastAccess=? WHERE charId=?");
					statement.setInt(1, isOnline());
					statement.setLong(2, System.currentTimeMillis());
					statement.setInt(3, getObjectId());
					statement.execute();
					statement.close();
				}
				catch (Exception e)
				{
					_log.error("Failed updating character online status.", e);
				}
			}
		});
	}

	/**
	 * Create a new player in the characters table of the database.<BR><BR>
	 */
	private boolean createDb()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("INSERT INTO characters "
					+ "(account_name,charId,char_name,level,maxHp,curHp,maxCp,curCp,maxMp,curMp,"
					+ "face,hairStyle,hairColor,sex,exp,sp,karma,fame,pvpkills,pkkills,clanid,race,"
					+ "classid,deletetime,cancraft,title,accesslevel,online,isin7sdungeon,clan_privs,"
					+ "wantspeace,base_class,newbie,nobless,pledge_rank) "
					+ "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			statement.setString(1, _accountName);
			statement.setInt(2, getObjectId());
			statement.setString(3, getName());
			statement.setInt(4, getLevel());
			statement.setInt(5, getMaxHp());
			statement.setDouble(6, getStatus().getCurrentHp());
			statement.setInt(7, getMaxCp());
			statement.setDouble(8, getStatus().getCurrentCp());
			statement.setInt(9, getMaxMp());
			statement.setDouble(10, getStatus().getCurrentMp());
			statement.setInt(11, getAppearance().getFace());
			statement.setInt(12, getAppearance().getHairStyle());
			statement.setInt(13, getAppearance().getHairColor());
			statement.setInt(14, getAppearance().getSex() ? 1 : 0);
			statement.setLong(15, getExp());
			statement.setInt(16, getSp());
			statement.setInt(17, getKarma());
			statement.setInt(18, getFame());
			statement.setInt(19, getPvpKills());
			statement.setInt(20, getPkKills());
			statement.setInt(21, getClanId());
			statement.setInt(22, getRace().ordinal());
			statement.setInt(23, getClassId().getId());
			statement.setLong(24, getDeleteTimer());
			statement.setInt(25, hasDwarvenCraft() ? 1 : 0);
			statement.setString(26, getTitle());
			statement.setInt(27, getAccessLevel());
			statement.setInt(28, isOnline());
			statement.setInt(29, isIn7sDungeon() ? 1 : 0);
			statement.setInt(30, getClanPrivileges());
			statement.setInt(31, getWantsPeace());
			statement.setInt(32, getBaseClass());
			statement.setInt(33, getNewbie());
			statement.setInt(34, isNoble() ? 1 : 0);
			statement.setLong(35, 0);

			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Could not insert char data: ", e);
			return false;
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		return true;
	}

	public static void disconnectIfOnline(int objectId)
	{
		L2PcInstance onlinePlayer = L2World.getInstance().findPlayer(objectId);
		if (onlinePlayer == null)
			return;

		if (!onlinePlayer.isInOfflineMode())
			_log.warn("Avoiding duplicate character! Disconnecting online character (" + onlinePlayer.getName() + ")");

		new Disconnection(onlinePlayer).defaultSequence(true);
	}

	/**
	 * Retrieve a L2PcInstance from the characters table of the database and add it in _allObjects of the L2world.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Retrieve the L2PcInstance from the characters table of the database </li>
	 * <li>Add the L2PcInstance object in _allObjects </li>
	 * <li>Set the x,y,z position of the L2PcInstance and make it invisible</li>
	 * <li>Update the overloaded status of the L2PcInstance</li><BR><BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 *
	 * @return The L2PcInstance loaded from the database
	 *
	 */
	public static L2PcInstance load(int objectId)
	{
		disconnectIfOnline(objectId);

		L2PcInstance player = null;
		Connection con = null;

		try
		{
			// Retrieve the L2PcInstance from the characters table of the database
			con = L2DatabaseFactory.getInstance().getConnection(con);

			PreparedStatement statement = con.prepareStatement(RESTORE_CHARACTER);
			statement.setInt(1, objectId);
			ResultSet rset = statement.executeQuery();

			double currentHp = 1, currentMp = 1, currentCp = 1;
			if (rset.next())
			{
				final int activeClassId = rset.getInt("classid");
				final boolean female = rset.getInt("sex") != 0;
				final L2PcTemplate template = CharTemplateTable.getInstance().getTemplate(activeClassId);
				PcAppearance app = new PcAppearance(rset.getByte("face"), rset.getByte("hairColor"), rset.getByte("hairStyle"), female);

				player = new L2PcInstance(objectId, template, rset.getString("account_name"), app);
				player.setName(rset.getString("char_name"));
				player._lastAccess = rset.getLong("lastAccess");

				player.getStat().setExp(rset.getLong("exp"));
				player.setExpBeforeDeath(rset.getLong("expBeforeDeath"));
				player.getStat().setLevel(rset.getByte("level"));
				player.getStat().setSp(rset.getInt("sp"));

				player.setWantsPeace(rset.getInt("wantspeace"));

				player.setHeading(rset.getInt("heading"));

				player.setKarma(rset.getInt("karma"));
				player.setFame(rset.getInt("fame"));
				player.setPvpKills(rset.getInt("pvpkills"));
				player.setPkKills(rset.getInt("pkkills"));

				player.setClanJoinExpiryTime(rset.getLong("clan_join_expiry_time"));
				if (player.getClanJoinExpiryTime() < System.currentTimeMillis())
				{
					player.setClanJoinExpiryTime(0);
				}
				player.setClanCreateExpiryTime(rset.getLong("clan_create_expiry_time"));
				if (player.getClanCreateExpiryTime() < System.currentTimeMillis())
				{
					player.setClanCreateExpiryTime(0);
				}

				int clanId = rset.getInt("clanid");

				if (clanId > 0)
				{
					player.setClan(ClanTable.getInstance().getClan(clanId));
				}

				player.setDeleteTimer(rset.getLong("deletetime"));
				player.setOnlineTime(rset.getLong("onlinetime"));
				player.setNewbie(rset.getInt("newbie"));
				player.setNoble(rset.getInt("nobless") == 1);

				player.setTitle(rset.getString("title"));
				player.setAccessLevel(rset.getInt("accesslevel"));
				player.setFistsWeaponItem(player.findFistsWeaponItem(activeClassId));
				player.setUptime(System.currentTimeMillis());

				// Only 1 line needed for each and their values only have to be set once as long as you don't die before it's set.
				currentHp = rset.getDouble("curHp");
				currentMp = rset.getDouble("curMp");
				currentCp = rset.getDouble("curCp");

				player._classIndex = 0;
				try
				{
					player.setBaseClass(rset.getInt("base_class"));
				}
				catch (Exception e)
				{
					player.setBaseClass(activeClassId);
				}

				// Restore Subclass Data (cannot be done earlier in function)
				if (restoreSubClassData(player))
				{
					if (activeClassId != player.getBaseClass())
					{
						for (SubClass subClass : player.getSubClasses().values())
							if (subClass.getClassId() == activeClassId)
								player._classIndex = subClass.getClassIndex();
					}
				}
				if (player.getClassIndex() == 0 && activeClassId != player.getBaseClass())
				{
					// Subclass in use but doesn't exist in DB -
					// a possible restart-while-modifysubclass cheat has been attempted.
					// Switching to use base class
					player.setClassId(player.getBaseClass());
					_log.warn("Player " + player.getName() + " reverted to base class. Possibly has tried a relogin exploit while subclassing.");
				}
				else
					player._activeClass = activeClassId;

				player.setIsIn7sDungeon(rset.getInt("isin7sdungeon") == 1);
				player.setInJail(rset.getInt("in_jail") == 1);
				player.setJailTimer(rset.getLong("jail_timer"));
				player.setBanChatTimer(rset.getLong("banchat_timer"));
				if (player.isInJail())
					player.setJailTimer(rset.getLong("jail_timer"));
				else
					player.setJailTimer(0);

				CursedWeaponsManager.getInstance().onEnter(player);

				player.setNoble(rset.getBoolean("nobless"));
				player.setCharViP((rset.getInt("charViP") == 1));
				player.setSubPledgeType(rset.getInt("subpledge"));
				player.setPledgeRank(rset.getInt("pledge_rank"));
				player.setApprentice(rset.getInt("apprentice"));
				player.setSponsor(rset.getInt("sponsor"));
				if (player.getClan() != null)
				{
					if (player.getClan().getLeaderId() != player.getObjectId())
					{
						if (player.getPledgeRank() == 0)
						{
							player.setPledgeRank(5);
						}
						player.setClanPrivileges(player.getClan().getRankPrivs(player.getPledgeRank()));
					}
					else
					{
						player.setClanPrivileges(L2Clan.CP_ALL);
						player.setPledgeRank(1);
					}
				}
				else
				{
					player.setClanPrivileges(L2Clan.CP_NOTHING);
				}
				player.setLvlJoinedAcademy(rset.getInt("lvl_joined_academy"));
				player.setAllianceWithVarkaKetra(rset.getInt("varka_ketra_ally"));
				player.setDeathPenaltyBuffLevel(rset.getInt("death_penalty_level"));
				player.setTrustLevel(rset.getInt("trust_level"));
				player.setVitalityPoints(rset.getDouble("vitality_points"), false);

				// Add the L2PcInstance object in _allObjects
				// L2World.getInstance().storeObject(player);

				// Set the x,y,z position of the L2PcInstance and make it invisible
				player.getPosition().setXYZInvisible(rset.getInt("x"), rset.getInt("y"), rset.getInt("z"));

				// Set Teleport Bookmark Slot
				player.setBookMarkSlot(rset.getInt("BookmarkSlot"));
			}

			rset.close();
			statement.close();

			if (player == null)
				return null;

			// Retrieve from the database all secondary data of this L2PcInstance
			// and reward expertise/lucky skills if necessary.
			player.restoreCharData();
			player.rewardSkills();

			// Buff and status icons
			if (Config.STORE_SKILL_COOLTIME)
				player.restoreEffects();

			player.stopEffects(L2EffectType.HEAL_OVER_TIME);
			player.stopEffects(L2EffectType.COMBAT_POINT_HEAL_OVER_TIME);

			// Restore current Cp, HP and MP values
			player.getStatus().setCurrentCp(currentCp);
			player.getStatus().setCurrentHp(currentHp);
			player.getStatus().setCurrentMp(currentMp);

			if (currentHp < 0.5)
			{
				player.setIsDead(true);
				player.getStatus().stopHpMpRegeneration();
			}

			// Restore pet if exists in the world
			player.setPet(L2World.getInstance().getPet(player.getObjectId()));
			if (player.getPet() != null)
				player.getPet().setOwner(player);

			// Update the overloaded status of the L2PcInstance
			player.refreshOverloaded();
			// Update the expertise status of the L2PcInstance
			player.refreshExpertisePenalty();
		}
		catch (Exception e)
		{
			_log.error("Failed loading character.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		return player;
	}

	/**
	 * @return
	 */
	public Forum getMail()
	{
		if (_forumMail == null)
		{
			setMail(ForumsBBSManager.getInstance().getForumByName("MailRoot").getChildByName(getName()));

			if (_forumMail == null)
			{
				ForumsBBSManager.getInstance().createNewForum(getName(), ForumsBBSManager.getInstance().getForumByName("MailRoot"), Forum.MAIL,
						Forum.OWNERONLY, getObjectId());
				setMail(ForumsBBSManager.getInstance().getForumByName("MailRoot").getChildByName(getName()));
			}
		}

		return _forumMail;
	}

	/**
	 * @param forum
	 */
	public void setMail(Forum forum)
	{
		_forumMail = forum;
	}

	/**
	 * @return
	 */
	public Forum getMemo()
	{
		if (_forumMemo == null)
		{
			setMemo(ForumsBBSManager.getInstance().getForumByName("MemoRoot").getChildByName(_accountName));

			if (_forumMemo == null)
			{
				ForumsBBSManager.getInstance().createNewForum(_accountName, ForumsBBSManager.getInstance().getForumByName("MemoRoot"), Forum.MEMO,
						Forum.OWNERONLY, getObjectId());
				setMemo(ForumsBBSManager.getInstance().getForumByName("MemoRoot").getChildByName(_accountName));
			}
		}

		return _forumMemo;
	}

	/**
	 * @param forum
	 */
	public void setMemo(Forum forum)
	{
		_forumMemo = forum;
	}

	/**
	 * Restores sub-class data for the L2PcInstance, used to check the current
	 * class index for the character.
	 */
	private static boolean restoreSubClassData(L2PcInstance player)
	{
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement(RESTORE_CHAR_SUBCLASSES);
			statement.setInt(1, player.getObjectId());

			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				SubClass subClass = new SubClass();
				subClass.setClassId(rset.getInt("class_id"));
				subClass.setLevel(rset.getByte("level"));
				subClass.setExp(rset.getLong("exp"));
				subClass.setSp(rset.getInt("sp"));
				subClass.setClassIndex(rset.getInt("class_index"));

				// Enforce the correct indexing of _subClasses against their class indexes.
				player.getSubClasses().put(subClass.getClassIndex(), subClass);
			}

			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Could not restore classes for " + player.getName() + ": ", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		return true;
	}

	/**
	 * Restores secondary data for the L2PcInstance, based on the current class index.
	 */
	private void restoreCharData()
	{
		// Retrieve from the database all skills of this L2PcInstance and add them to _skills.
		restoreSkills();

		// Retrieve from the database all macroses of this L2PcInstance and add them to macroses.
		getMacroses().restore();

		// Retrieve from the database all shortCuts of this L2PcInstance and add them to shortCuts.
		getShortCuts().restore();

		// Retrieve from the database all henna of this L2PcInstance and add them to _henna.
		restoreHenna();

		// Retrieve from the database all teleport bookmark of this L2PcInstance and add them to _tpbookmark.
		restoreTeleportBookmark();

		// Retrieve from the database all recom data of this L2PcInstance and add to _recomChars.
		RecommendationManager.getInstance().onJoin(this);

		// Retrieve from the database the recipe book of this L2PcInstance.
		restoreRecipeBook(true);
	}

	/**
	 * Restore recipe book data for this L2PcInstance.
	 */
	private void restoreRecipeBook(boolean loadCommon)
	{
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			String sql = loadCommon ? "SELECT id, type, classIndex FROM character_recipebook WHERE charId=?" : "SELECT id FROM character_recipebook WHERE charId=? AND classIndex=? AND type = 1";
			PreparedStatement statement = con.prepareStatement(sql);
			statement.setInt(1, getObjectId());
			if (!loadCommon)
				statement.setInt(2, _classIndex);
			ResultSet rset = statement.executeQuery();
			
			_dwarvenRecipeBook.clear();
			
			L2RecipeList recipe;
			while (rset.next())
			{
				recipe = RecipeController.getInstance().getRecipeList(rset.getInt("id"));
				
				if (loadCommon)
				{
					if (rset.getInt(2) == 1)
					{
						if (rset.getInt(3) == _classIndex)
							registerDwarvenRecipeList(recipe, false);
					}
					else
						registerCommonRecipeList(recipe, false);
				}
				else
					registerDwarvenRecipeList(recipe, false);
			}

			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Could not restore recipe book data:", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/** player coords from client */
	private int _clientX;
	private int _clientY;
	private int _clientZ;
	private int _clientHeading;

	@Override
	public int getClientX()
	{
		return _clientX;
	}

	@Override
	public int getClientY()
	{
		return _clientY;
	}

	@Override
	public int getClientZ()
	{
		return _clientZ;
	}

	@Override
	public int getClientHeading()
	{
		return _clientHeading;
	}

	public void setClientX(int val)
	{
		_clientX = val;
	}

	public void setClientY(int val)
	{
		_clientY = val;
	}

	public void setClientZ(int val)
	{
		_clientZ = val;
	}

	public void setClientHeading(int val)
	{
		_clientHeading = val;
	}

	/**
	 * Update L2PcInstance stats in the characters table of the database.<BR><BR>
	 */
	private long _lastStore;

	public void store()
	{
		store(false, true);
	}

	public void store(boolean storeActiveEffects)
	{
		store(false, storeActiveEffects);
	}

	public synchronized void store(boolean items, boolean storeActiveEffects)
	{
		_lastStore = System.currentTimeMillis();

		if (getOnlineState() == ONLINE_STATE_DELETED)
			return;

		// Update client coords, if these look like true
		if (isInsideRadius(getClientX(), getClientY(), 1000, true))
			getPosition().setXYZ(getClientX(), getClientY(), getClientZ());

		storeCharBase();
		storeCharSub();
		storeEffect(storeActiveEffects);
		transformInsertInfo();

		if (Config.UPDATE_ITEMS_ON_CHAR_STORE || items)
			getInventory().updateDatabase();
	}

	private void storeCharBase()
	{
		Connection con = null;

		try
		{
			// Get the exp, level, and sp of base class to store in base table
			int currentClassIndex = getClassIndex();
			_classIndex = 0;
			long exp = getStat().getExp();
			int level = getStat().getLevel();
			int sp = getStat().getSp();
			_classIndex = currentClassIndex;
			long totalOnlineTime = _onlineTime;

			if (_onlineBeginTime > 0)
				totalOnlineTime += (System.currentTimeMillis() - _onlineBeginTime) / 1000;

			con = L2DatabaseFactory.getInstance().getConnection(con);

			// Update base class
			PreparedStatement statement = con.prepareStatement(UPDATE_CHARACTER);
			statement.setInt(1, level);
			statement.setInt(2, getMaxHp());
			statement.setDouble(3, getStatus().getCurrentHp());
			statement.setInt(4, getMaxCp());
			statement.setDouble(5, getStatus().getCurrentCp());
			statement.setInt(6, getMaxMp());
			statement.setDouble(7, getStatus().getCurrentMp());
			statement.setInt(8, getAppearance().getFace());
			statement.setInt(9, getAppearance().getHairStyle());
			statement.setInt(10, getAppearance().getHairColor());
			statement.setInt(11, getAppearance().getSex() ? 1 : 0);
			statement.setInt(12, getHeading());
			statement.setInt(13, _observerMode ? _obsX : getX());
			statement.setInt(14, _observerMode ? _obsY : getY());
			statement.setInt(15, _observerMode ? _obsZ : getZ());
			statement.setLong(16, exp);
			statement.setLong(17, getExpBeforeDeath());
			statement.setInt(18, sp);
			statement.setInt(19, getKarma());
			statement.setInt(20, getFame());
			statement.setInt(21, getPvpKills());
			statement.setInt(22, getPkKills());
			statement.setInt(23, getClanId());
			statement.setInt(24, getRace().ordinal());
			statement.setInt(25, getClassId().getId());
			statement.setLong(26, getDeleteTimer());
			statement.setString(27, getTitle());
			statement.setInt(28, getAccessLevel());
			statement.setInt(29, isOnline());
			statement.setInt(30, isIn7sDungeon() ? 1 : 0);
			statement.setInt(31, getClanPrivileges());
			statement.setInt(32, getWantsPeace());
			statement.setInt(33, getBaseClass());

			statement.setLong(34, totalOnlineTime);
			statement.setInt(35, isInJail() ? 1 : 0);
			statement.setLong(36, getJailTimer());
			statement.setInt(37, getNewbie());
			statement.setInt(38, isNoble() ? 1 : 0);
			statement.setLong(39, getPledgeRank());
			statement.setInt(40, getSubPledgeType());
			statement.setInt(41, getLvlJoinedAcademy());
			statement.setLong(42, getApprentice());
			statement.setLong(43, getSponsor());
			statement.setInt(44, getAllianceWithVarkaKetra());
			statement.setLong(45, getClanJoinExpiryTime());
			statement.setLong(46, getClanCreateExpiryTime());
			statement.setLong(47, getBanChatTimer());
			statement.setString(48, getName());
			statement.setLong(49, getDeathPenaltyBuffLevel());
			statement.setLong(50, getTrustLevel());
			statement.setDouble(51, getVitalityPoints());
			statement.setInt(52, getBookMarkSlot());
		    statement.setInt(53, getObjectId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Could not store char base data: ", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	private void storeCharSub()
	{
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement(UPDATE_CHAR_SUBCLASS);

			if (getTotalSubClasses() > 0)
			{
				for (SubClass subClass : getSubClasses().values())
				{
					statement.setLong(1, subClass.getExp());
					statement.setInt(2, subClass.getSp());
					statement.setInt(3, subClass.getLevel());

					statement.setInt(4, subClass.getClassId());
					statement.setInt(5, getObjectId());
					statement.setInt(6, subClass.getClassIndex());

					statement.execute();
				}
			}
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Could not store sub class data for " + getName() + ": ", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	private void storeEffect(boolean storeEffects)
	{
		if (!Config.STORE_SKILL_COOLTIME)
			return;

		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);

			// Delete all current stored effects for char to avoid dupe
			PreparedStatement statement = con.prepareStatement(DELETE_SKILL_SAVE);
			statement.setInt(1, getObjectId());
			statement.setInt(2, getClassIndex());
			statement.execute();
			statement.close();

			int buff_index = 0;

			statement = con.prepareStatement(ADD_SKILL_SAVE);

			Set<Integer> storedSkills = new HashSet<Integer>();

			if (storeEffects)
			{
				// Store all effect data along with calulated remaining
				// reuse delays for matching skills. 'restore_type'= 0.
				for (L2Effect effect : getAllEffects())
				{
					if (effect != null && effect.canBeStoredInDb())
					{
						int skillId = effect.getSkill().getId();

						if (!storedSkills.add(skillId))
							continue;

						statement.setInt(1, getObjectId());
						statement.setInt(2, skillId);
						statement.setInt(3, effect.getSkill().getLevel());
						statement.setInt(4, effect.getCount());
						statement.setInt(5, effect.getPeriod() - effect.getTime());
						if (_reuseTimeStamps.containsKey(skillId))
						{
							TimeStamp t = _reuseTimeStamps.get(skillId);
							statement.setLong(6, t.hasNotPassed() ? t.getReuse() : 0);
							statement.setLong(7, t.hasNotPassed() ? t.getStamp() : 0 );
						}
						else
						{
							statement.setLong(6, 0);
							statement.setLong(7, 0);
						}
						statement.setInt(8, 0);
						statement.setInt(9, getClassIndex());
						statement.setInt(10, ++buff_index);
						statement.execute();
					}
				}
			}

			// Store the reuse delays of remaining skills which
			// lost effect but still under reuse delay. 'restore_type' 1.
			for (TimeStamp t : _reuseTimeStamps.values())
			{
				if (t.hasNotPassed())
				{
					int skillId = t.getSkill();

					if (!storedSkills.add(skillId))
						continue;

					statement.setInt(1, getObjectId());
					statement.setInt(2, skillId);
					statement.setInt(3, -1);
					statement.setInt(4, -1);
					statement.setInt(5, -1);
					statement.setLong(6, t.getReuse());
					statement.setLong(7, t.getStamp());
					statement.setInt(8, 1);
					statement.setInt(9, getClassIndex());
					statement.setInt(10, ++buff_index);
					statement.execute();
				}
			}
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Could not store char effect data: ", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * Return True if the L2PcInstance is on line.<BR><BR>
	 */
	public int isOnline()
	{
		return (getOnlineState() == ONLINE_STATE_ONLINE) ? 1 : 0;
	}

	public byte getOnlineState()
	{
		return _isOnline;
	}

	public boolean isIn7sDungeon()
	{
		return _isIn7sDungeon;
	}

	/**
	 * Add a skill to the L2PcInstance _skills and its Func objects to the calculator set of the L2PcInstance and save update in the character_skills table of the database.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All skills own by a L2PcInstance are identified in <B>_skills</B><BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Replace oldSkill by newSkill or Add the newSkill </li>
	 * <li>If an old skill has been replaced, remove all its Func objects of L2Character calculator set</li>
	 * <li>Add Func objects of newSkill to the calculator set of the L2Character </li><BR><BR>
	 *
	 * @param newSkill The L2Skill to add to the L2Character
	 *
	 * @return The L2Skill replaced or null if just added a new L2Skill
	 *
	 */
	public L2Skill addSkill(L2Skill newSkill, boolean save)
	{
		// Add a skill to the L2PcInstance _skills and its Func objects to the calculator set of the L2PcInstance
		final L2Skill oldSkill = addSkill(newSkill);

		// Add or update a L2PcInstance skill in the character_skills table of the database
		if (save)
			storeSkill(newSkill, oldSkill, -1);

		return oldSkill;
	}

	public L2Skill removeSkill(L2Skill skill, boolean store)
	{
		return store ? removeSkill(skill) : super.removeSkill(skill);
	}

	/**
	 * Remove a skill from the L2Character and its Func objects from calculator set of the L2Character and save update in the character_skills table of the database.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All skills own by a L2Character are identified in <B>_skills</B><BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove the skill from the L2Character _skills </li>
	 * <li>Remove all its Func objects from the L2Character calculator set</li><BR><BR>
	 *
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance : Save update in the character_skills table of the database</li><BR><BR>
	 *
	 * @param skill The L2Skill to remove from the L2Character
	 *
	 * @return The L2Skill removed
	 *
	 */
	@Override
	public L2Skill removeSkill(L2Skill skill)
	{
		// Remove a skill from the L2Character and its Func objects from calculator set of the L2Character
		L2Skill oldSkill = super.removeSkill(skill);

		if (oldSkill != null)
		{
			Connection con = null;
			try
			{
				// Remove or update a L2PcInstance skill from the character_skills table of the database
				con = L2DatabaseFactory.getInstance().getConnection(con);
				PreparedStatement statement = con.prepareStatement(DELETE_SKILL_FROM_CHAR);
				statement.setInt(1, oldSkill.getId());
				statement.setInt(2, getObjectId());
				statement.setInt(3, getClassIndex());
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				_log.error("Error could not delete skill: ", e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}

		if (transformId() > 0 || isCursedWeaponEquipped())
			return oldSkill;

		for (L2ShortCut sc : getAllShortCuts())
			if (sc != null && skill != null && sc.getId() == skill.getId() && sc.getType() == L2ShortCut.TYPE_SKILL)
				deleteShortCut(sc.getSlot(), sc.getPage());

		return oldSkill;
	}

	/**
	 * Add or update a L2PcInstance skill in the character_skills table of the database.
	 * <BR><BR>
	 * If newClassIndex > -1, the skill will be stored with that class index, not the current one.
	 */
	private void storeSkill(L2Skill newSkill, L2Skill oldSkill, int newClassIndex)
	{
		if (newSkill == null || newSkill.getId() > 369 && newSkill.getId() < 392)
			return;

		int classIndex = _classIndex;

		if (newClassIndex > -1)
			classIndex = newClassIndex;

		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement;

			if (oldSkill != null)
			{
				statement = con.prepareStatement(UPDATE_CHARACTER_SKILL_LEVEL);
				statement.setInt(1, newSkill.getLevel());
				statement.setInt(2, oldSkill.getId());
				statement.setInt(3, getObjectId());
				statement.setInt(4, classIndex);
				statement.execute();
				statement.close();
			}
			else
			{
				statement = con.prepareStatement(ADD_NEW_SKILL);
				statement.setInt(1, getObjectId());
				statement.setInt(2, newSkill.getId());
				statement.setInt(3, newSkill.getLevel());
				statement.setInt(4, classIndex);
				statement.execute();
				statement.close();
			}
		}
		catch (Exception e)
		{
			_log.error("Error could not store char skills: ", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * check player skills and remove unlegit ones (excludes hero, noblesse and cursed weapon skills)
	 */
	public void checkAllowedSkills()
	{
		if (isGM())
			return;

		Set<Integer> skillTreeUIDs = SkillTreeTable.getInstance().getAllowedSkillUIDs(getClassId());

		skill_loop: for (L2Skill skill : getAllSkills())
		{
			int skillid = skill.getId();

			if (getTransformation() != null && containsAllowedTransformSkill(skillid))
				continue;
			// Loop through all skills in players skilltree
			if (skillTreeUIDs.contains(SkillTable.getSkillUID(skillid, SkillTable.getInstance().getNormalLevel(skill))))
				continue;
			// skills learned by L2SkillType.LEARN_SKILL
			if (SkillTable.getInstance().isLearnedSkill(skill))
				continue;
			// Exclude noble skills
			if (isNoble() && NobleSkillTable.isNobleSkill(skillid))
				continue skill_loop;
			// Exclude hero skills
			if (isHero() && HeroSkillTable.isHeroSkill(skillid))
				continue skill_loop;
			// Exclude cursed weapon skills
			if (isCursedWeaponEquipped() && skillid == CursedWeaponsManager.getInstance().getCursedWeapon(_cursedWeaponEquippedId).getSkillId())
				continue skill_loop;
			// Exclude clan skills
			if (getClan() != null && (skillid >= 370 && skillid <= 391))
				continue skill_loop;
			// Exclude residential skills
			if (getClan() != null && (getClan().getHasCastle() > 0 || getClan().getHasFort() > 0))
				if (590 <= skillid && skillid <= 610)
					continue skill_loop;
			// Exclude seal of ruler / build siege hq
			if (getClan() != null && getClan().getLeaderId() == getObjectId() && (skillid == 246 || skillid == 247))
				continue skill_loop;
			// Exclude fishing skills and common skills + dwarfen craft
			if (skillid >= 1312 && skillid <= 1322)
				continue skill_loop;
			if (skillid >= 1368 && skillid <= 1373)
				continue skill_loop;
			// Exclude sa / enchant bonus / penality etc. skills
			if (skillid >= 3000 && skillid < 7000)
				continue skill_loop;
			// Exclude Armor Set skills
			if (skillid >= 8100 && skillid < 8400)
				continue skill_loop;
			// Exclude Skills from AllowedSkills in options.properties
			if (Config.ALLOWED_SKILLS_LIST.contains(skillid))
				continue skill_loop;
			// Exclude VIP character
			if (isCharViP() && Config.CHAR_VIP_SKIP_SKILLS_CHECK)
				continue skill_loop;

			// Remove skill from ingame, but not from the database to avoid accidentally removal of skills
			// if something failed loading and do a lil log message
			removeSkill(skill, false);
			sendMessage("Skill " + skill.getName() + " removed and GM informed!");
			_log.fatal("Cheater?! " + skill + " removed from " + getName() + " (" + getAccountName() + ")");
		}
	}

	/**
	 * Retrieve from the database all skills of this L2PcInstance and add them to _skills.<BR><BR>
	 */
	private void restoreSkills()
	{
		List<L2Skill> tmp = new ArrayList<L2Skill>();

		Connection con = null;
		try
		{
			// Retrieve all skills of this L2PcInstance from the database
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(RESTORE_SKILLS_FOR_CHAR);
			statement.setInt(1, getObjectId());
			statement.setInt(2, getClassIndex());
			ResultSet rset = statement.executeQuery();

			// Go though the recordset of this SQL query
			while (rset.next())
			{
				int id = rset.getInt("skill_id");
				int level = rset.getInt("skill_level");

				if (id > 9000 && id < 9007)
					continue; // Fake skills for base stats

				// Create a L2Skill object for each record
				L2Skill skill = SkillTable.getInstance().getInfo(id, level);

				if (skill == null)
					continue;

				tmp.add(skill);
			}

			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Could not restore character skills: ", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		L2Skill[] skills = tmp.toArray(new L2Skill[tmp.size()]);

		Arrays.sort(skills, SKILL_LIST_COMPARATOR);

		// Add the L2Skill object to the L2Character _skills and its Func objects to the calculator set of the L2Character
		for (L2Skill skill : skills)
			super.addSkill(skill);

		// Restore clan skills
		if (_clan != null)
			_clan.addSkillEffects(this, false);
	}

	/**
	 * Retrieve from the database all skill effects of this L2PcInstance and add them to the player.<BR><BR>
	 */
	public void restoreEffects()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement;

			ResultSet rset;

			/**
			 *  Restore Type 0
			 *  These skill were still in effect on the character
			 *  upon logout. Some of which were self casted and
			 *  might still have had a long reuse delay which also
			 *  is restored.
			 */

			statement = con.prepareStatement(RESTORE_SKILL_SAVE);
			statement.setInt(1, getObjectId());
			statement.setInt(2, getClassIndex());
			statement.setInt(3, 0);
			rset = statement.executeQuery();

			while (rset.next())
			{
				int skillId = rset.getInt("skill_id");
				int skillLvl = rset.getInt("skill_level");
				int effectCount = rset.getInt("effect_count");
				int effectCurTime = rset.getInt("effect_cur_time");
				long reuseDelay = rset.getLong("reuse_delay");
				long systime = rset.getLong("systime");

				long remainingTime = systime - System.currentTimeMillis();

				// Just incase the admin minipulated this table incorrectly :x
				if (skillId == -1 || effectCount == -1 || effectCurTime == -1 || reuseDelay < 0)
					continue;

				L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLvl);
				if (skill != null)
					skill.getEffects(this, this);

				if (remainingTime > 10)
				{
					disableSkill(skillId, remainingTime);
					addTimeStamp(new TimeStamp(skillId, reuseDelay, systime));
				}

				for (L2Effect effect : getAllEffects())
				{
					if (effect.getSkill().getId() == skillId)
					{
						effect.setTiming(effectCount, effect.getPeriod() - effectCurTime);
					}
				}
			}

			rset.close();
			statement.close();

			/**
			 * Restore Type 1
			 * The remaning skills lost effect upon logout but
			 * were still under a high reuse delay.
			 */
			statement = con.prepareStatement(RESTORE_SKILL_SAVE);
			statement.setInt(1, getObjectId());
			statement.setInt(2, getClassIndex());
			statement.setInt(3, 1);
			rset = statement.executeQuery();

			while (rset.next())
			{
				int skillId = rset.getInt("skill_id");
				long reuseDelay = rset.getLong("reuse_delay");
				long systime = rset.getLong("systime");

				long remainingTime = systime - System.currentTimeMillis();

				if (remainingTime < 10)
					continue;

				disableSkill(skillId, remainingTime);
				addTimeStamp(new TimeStamp(skillId, reuseDelay, systime));
			}
			rset.close();
			statement.close();

			// No reason to delete it here, since it will be before it's stored again,
			// and character infos get saved on every disconnection
//			statement = con.prepareStatement(DELETE_SKILL_SAVE);
//			statement.setInt(1, getObjectId());
//			statement.setInt(2, getClassIndex());
//			statement.executeUpdate();
//			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Could not restore active effect data: ", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * Retrieve from the database all Henna of this L2PcInstance, add them to _henna and calculate stats of the L2PcInstance.<BR><BR>
	 */
	private void restoreHenna()
	{
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement(RESTORE_CHAR_HENNAS);
			statement.setInt(1, getObjectId());
			statement.setInt(2, getClassIndex());
			ResultSet rset = statement.executeQuery();

			for (int i = 0; i < 3; i++)
				_henna[i] = null;

			while (rset.next())
			{
				int slot = rset.getInt("slot");

				if (slot < 1 || slot > 3)
					continue;

				_henna[slot - 1] = HennaTable.getInstance().getTemplate(rset.getInt("symbol_id"));
			}

			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Failed restoing character hennas.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		// Calculate Henna modifiers of this L2PcInstance
		recalcHennaStats();
	}

	/**
	 * Return the number of Henna empty slot of the L2PcInstance.<BR><BR>
	 */
	public int getHennaEmptySlots()
	{
		int totalSlots;
		if (getClassId().level() == 1)
			totalSlots = 2;
		else
			totalSlots = 3;

		for (int i = 0; i < 3; i++)
			if (_henna[i] != null)
				totalSlots--;

		if (totalSlots <= 0)
			return 0;

		return totalSlots;
	}

	/**
	 * Remove a Henna of the L2PcInstance, save update in the character_hennas table of the database and send Server->Client HennaInfo/UserInfo packet to this L2PcInstance.<BR><BR>
	 */
	public boolean removeHenna(int slot)
	{
		if (slot < 1 || slot > 3)
			return false;

		slot--;

		if (_henna[slot] == null)
			return false;

		L2Henna henna = _henna[slot];
		_henna[slot] = null;

		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement(DELETE_CHAR_HENNA);
			statement.setInt(1, getObjectId());
			statement.setInt(2, slot + 1);
			statement.setInt(3, getClassIndex());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Failed removing character henna.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		// Calculate Henna modifiers of this L2PcInstance
		recalcHennaStats();

		// Send Server->Client HennaInfo packet to this L2PcInstance
		sendPacket(new HennaInfo(this));

		// Send Server->Client UserInfo packet to this L2PcInstance
		sendPacket(new UserInfo(this));

		// Add the recovered dyes to the player's inventory and notify them.
		L2ItemInstance dye = getInventory().addItem("Henna", henna.getItemId(), henna.getAmount() / 2, this, null);
		getInventory().updateInventory(dye);

		reduceAdena("Henna", henna.getPrice() / 5, this, false);

		SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
		sm.addItemName(henna.getItemId());
		sm.addItemNumber(henna.getAmount() / 2);
		sendPacket(sm);

		sendPacket(SystemMessageId.SYMBOL_DELETED);

		return true;
	}

	/**
	 * Add a Henna to the L2PcInstance, save update in the character_hennas table of the database and send Server->Client HennaInfo/UserInfo packet to this L2PcInstance.<BR><BR>
	 * <B>Does not do <U>any</U> validation!</B>
	 */
	public void addHenna(L2Henna henna)
	{
		for (int i = 0; i < 3; i++)
		{
			if (_henna[i] == null)
			{
				_henna[i] = henna;

				// Calculate Henna modifiers of this L2PcInstance
				recalcHennaStats();

				Connection con = null;
				try
				{
					con = L2DatabaseFactory.getInstance().getConnection(con);
					PreparedStatement statement = con.prepareStatement(ADD_CHAR_HENNA);
					statement.setInt(1, getObjectId());
					statement.setInt(2, henna.getSymbolId());
					statement.setInt(3, i + 1);
					statement.setInt(4, getClassIndex());
					statement.execute();
					statement.close();
				}
				catch (Exception e)
				{
					_log.error("Failed saving character henna.", e);
				}
				finally
				{
					L2DatabaseFactory.close(con);
				}

				// Send Server->Client HennaInfo packet to this L2PcInstance
				sendPacket(new HennaInfo(this));
				// Send Server->Client UserInfo packet to this L2PcInstance
				sendPacket(new UserInfo(this));
				return;
			}
		}
	}

	/**
	 * Calculate Henna modifiers of this L2PcInstance.<BR><BR>
	 */
	private void recalcHennaStats()
	{
		_hennaINT = 0;
		_hennaSTR = 0;
		_hennaCON = 0;
		_hennaMEN = 0;
		_hennaWIT = 0;
		_hennaDEX = 0;

		for (int i = 0; i < 3; i++)
		{
			if (_henna[i] == null)
				continue;
			_hennaINT += _henna[i].getStatINT();
			_hennaSTR += _henna[i].getStatSTR();
			_hennaMEN += _henna[i].getStatMEM();
			_hennaCON += _henna[i].getStatCON();
			_hennaWIT += _henna[i].getStatWIT();
			_hennaDEX += _henna[i].getStatDEX();
		}

		if (_hennaINT > 5)
			_hennaINT = 5;
		if (_hennaSTR > 5)
			_hennaSTR = 5;
		if (_hennaMEN > 5)
			_hennaMEN = 5;
		if (_hennaCON > 5)
			_hennaCON = 5;
		if (_hennaWIT > 5)
			_hennaWIT = 5;
		if (_hennaDEX > 5)
			_hennaDEX = 5;
	}

	/**
	 * Return the Henna of this L2PcInstance corresponding to the selected slot.<BR><BR>
	 */
    public L2Henna getHenna(int slot)
    {
        if (slot < 1 || slot > 3) return null;

        return _henna[slot - 1];
    }

	/**
	 * Return the INT Henna modifier of this L2PcInstance.<BR><BR>
	 */
	public int getHennaStatINT()
	{
		return _hennaINT;
	}

	/**
	 * Return the STR Henna modifier of this L2PcInstance.<BR><BR>
	 */
	public int getHennaStatSTR()
	{
		return _hennaSTR;
	}

	/**
	 * Return the CON Henna modifier of this L2PcInstance.<BR><BR>
	 */
	public int getHennaStatCON()
	{
		return _hennaCON;
	}

	/**
	 * Return the MEN Henna modifier of this L2PcInstance.<BR><BR>
	 */
	public int getHennaStatMEN()
	{
		return _hennaMEN;
	}

	/**
	 * Return the WIT Henna modifier of this L2PcInstance.<BR><BR>
	 */
	public int getHennaStatWIT()
	{
		return _hennaWIT;
	}

	/**
	 * Return the DEX Henna modifier of this L2PcInstance.<BR><BR>
	 */
	public int getHennaStatDEX()
	{
		return _hennaDEX;
	}

	/**
	 * Return True if the L2PcInstance is autoAttackable.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Check if the attacker isn't the L2PcInstance Pet </li>
	 * <li>Check if the attacker is L2MonsterInstance</li>
	 * <li>If the attacker is a L2PcInstance, check if it is not in the same party </li>
	 * <li>Check if the L2PcInstance has Karma </li>
	 * <li>If the attacker is a L2PcInstance, check if it is not in the same siege clan (Attacker, Defender) </li><BR><BR>
	 *
	 */
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		// Check if the attacker isn't the L2PcInstance Pet
		if (attacker == this || attacker == getPet())
			return false;

		// Check if the attacker is a L2MonsterInstance
		if (attacker instanceof L2MonsterInstance)
			return true;

		switch (GlobalRestrictions.getCombatState(L2Object.getActingPlayer(attacker), this))
		{
			case ENEMY:
				return true;
			case FRIEND:
				return false;
		}
		
		// Check if the attacker is not in the same party
		if (getParty() != null && getParty().getPartyMembers().contains(attacker))
			return false;

		if (isCursedWeaponEquipped())
			return true;

		// Check if the attacker is in olympia and olympia start
		if (attacker instanceof L2PcInstance && ((L2PcInstance) attacker).isInOlympiadMode())
		{
            return isInOlympiadMode() && isOlympiadStart() && ((L2PcInstance) attacker).getOlympiadGameId() == getOlympiadGameId();
        }

		// Check if the attacker is not in the same clan
		if (getClan() != null && attacker != null && getClan().isMember(attacker.getObjectId()))
			return false;

		if (attacker instanceof L2Playable && isInsideZone(L2Zone.FLAG_PEACE))
			return false;

		// Check if the L2PcInstance has Karma
		if (getKarma() > 0 || getPvpFlag() > 0)
			return true;

		// Check if the attacker is a L2Playable
		if (attacker instanceof L2Playable)
		{
			// Is AutoAttackable if both playables are in the same duel and the duel is still going on
			if (getDuelState() == Duel.DUELSTATE_DUELLING && getDuelId() == attacker.getActingPlayer().getDuelId())
				return true;
		}

		// Check if the attacker is a L2PcInstance
		if (attacker instanceof L2PcInstance)
		{
			// Check if the L2PcInstance is in an arena or a siege area
			if (isInsideZone(L2Zone.FLAG_PVP) && attacker.isInsideZone(L2Zone.FLAG_PVP))
				return true;

			// Check if the L2PcInstance holds a cursed weapon
			if (((L2PcInstance) attacker).isCursedWeaponEquipped())
				return true;

			if (getClan() != null)
			{
				Siege siege = SiegeManager.getInstance().getSiege(getX(), getY(), getZ());
				if (siege != null)
				{
					// Check if a siege is in progress and if attacker and the L2PcInstance aren't in the Defender clan
					if (siege.checkIsDefender(((L2PcInstance) attacker).getClan()) && siege.checkIsDefender(getClan()))
						return false;

					// Check if a siege is in progress and if attacker and the L2PcInstance aren't in the Attacker clan
					if (siege.checkIsAttacker(((L2PcInstance) attacker).getClan()) && siege.checkIsAttacker(getClan()))
						return false;
				}

				// Check if clan is at war
				if (getClan() != null
						&& ((L2PcInstance) attacker).getClan() != null
						&& (getClan().isAtWarWith(((L2PcInstance) attacker).getClanId())
						&& ((L2PcInstance)attacker).getClan().isAtWarWith(getClanId())
						&& getWantsPeace() == 0 && ((L2PcInstance) attacker).getWantsPeace() == 0 && !isAcademyMember()))
					return true;
			}
		}
		else if (attacker instanceof L2SiegeGuardInstance)
		{
			if (getClan() != null)
			{
				Siege siege = SiegeManager.getInstance().getSiege(this);
				return (siege != null && siege.checkIsAttacker(getClan()));
			}
		}
		else if (attacker instanceof L2FortSiegeGuardInstance)
		{
			if (getClan() != null)
			{
				FortSiege siege = FortSiegeManager.getInstance().getSiege(this);
				return (siege != null && siege.checkIsAttacker(getClan()));
			}
		}

		return false;
	}

	/**
	 * Checks if the client was allowed to call that skill at all, or not.
	 */
	public boolean canUseMagic(L2Skill skill)
	{
		if (skill == null || skill.getSkillType() == L2SkillType.NOTDONE)
			return false;

		// players mounted on pets cannot use any toggle skills
		if (skill.isToggle() && isMounted())
			return false;

		// Check if the skill is active
		if (skill.isPassive() || skill.isChance())
			return false;

		if (isTransformationDisabledSkill(skill) && !skill.isPotion())
			return false;

		return true;
	}

	private boolean isTransformationDisabledSkill(L2Skill skill)
	{
		if (_transformation != null && !containsAllowedTransformSkill(skill.getId()) && !skill.allowOnTransform())
			return true;

		return false;
	}

	/**
	 * Check if the active L2Skill can be casted.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Check if the skill isn't toggle and is offensive </li>
	 * <li>Check if the target is in the skill cast range </li>
	 * <li>Check if the skill is Spoil type and if the target isn't already spoiled </li>
	 * <li>Check if the caster owns enought consummed Item, enough HP and MP to cast the skill </li>
	 * <li>Check if the caster isn't sitting </li>
	 * <li>Check if all skills are enabled and this skill is enabled </li><BR><BR>
	 * <li>Check if the caster own the weapon needed </li><BR><BR>
	 * <li>Check if the skill is active </li><BR><BR>
	 * <li>Check if all casting conditions are completed</li><BR><BR>
	 * <li>Notify the AI with AI_INTENTION_CAST and target</li><BR><BR>
	 *
	 * @param skill The L2Skill to use
	 * @param forceUse used to force ATTACK on players
	 * @param dontMove used to prevent movement, if not in range
	 *
	 */
	@Override
	public void useMagic(L2Skill skill, boolean forceUse, boolean dontMove)
	{
		if (!canUseMagic(skill))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		//************************************* Check Casting in Progress *******************************************

		// If a skill is currently being used, queue this one if this is not the same
		if (isCastingNow())
		{
			SkillDat currentSkill = getCurrentSkill();
			// Check if new skill different from current skill in progress
			if (currentSkill != null && skill.getId() == currentSkill.getSkillId())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			// Create a new SkillDat object and queue it in the player _queuedSkill
			setQueuedSkill(skill, forceUse, dontMove);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		setIsCastingNow(true);
		// Create a new SkillDat object and set the player _currentSkill
		// This is used mainly to save & queue the button presses, since L2Character has
		// _lastSkillCast which could otherwise replace it
		setCurrentSkill(skill, forceUse, dontMove);

		if (getQueuedSkill() != null) // Wiping out previous values, after casting has been aborted
			setQueuedSkill(null, false, false);

		if (!checkUseMagicConditions(skill, forceUse, dontMove))
		{
			setIsCastingNow(false);
			return;
		}

		// Check if the target is correct and Notify the AI with AI_INTENTION_CAST and target
		L2Character target = null;

		switch (skill.getTargetType())
		{
			case TARGET_AURA:    // AURA, SELF should be cast even if no target has been found
			case TARGET_FRONT_AURA:
			case TARGET_BEHIND_AURA:
			case TARGET_GROUND:
			case TARGET_SELF:
				target = this;
				break;
			default:
				// Get the first target of the list
				target = skill.getFirstOfTargetList(this);
				break;
		}

		// Notify the AI with AI_INTENTION_CAST and target
		getAI().setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
	}

	@Override
	public void setIsCastingNow(boolean value)
	{
		if (!value)
		{
			_currentSkill = null;
		}
		super.setIsCastingNow(value);
	}

	public void sendReuseMessage(L2Skill skill)
	{
		SystemMessage sm = null;

		TimeStamp timeStamp = getReuseTimeStamps().get(skill.getId());
		if (timeStamp != null)
		{
			int remainingTime = (int)(timeStamp.getRemaining() / 1000);
			int hours = remainingTime / 3600;
			int minutes = (remainingTime % 3600) / 60;
			int seconds = (remainingTime % 60);
			if (hours > 0)
			{
				sm = new SystemMessage(SystemMessageId.S2_HOURS_S3_MINUTES_S4_SECONDS_REMAINING_FOR_REUSE_S1);
				sm.addSkillName(skill);
				sm.addNumber(hours);
				sm.addNumber(minutes);
			}
			else if (minutes > 0)
			{
				sm = new SystemMessage(SystemMessageId.S2_MINUTES_S3_SECONDS_REMAINING_FOR_REUSE_S1);
				sm.addSkillName(skill);
				sm.addNumber(minutes);
			}
			else
			{
				sm = new SystemMessage(SystemMessageId.S2_SECONDS_REMAINING_FOR_REUSE_S1);
				sm.addSkillName(skill);
			}
			sm.addNumber(seconds);
		}
		else
		{
			sm = new SystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE);
			sm.addSkillName(skill);
		}
		sendPacket(sm);
	}

	private boolean checkUseMagicConditions(L2Skill skill, boolean forceUse, boolean dontMove)
	{
		L2SkillType sklType = skill.getSkillType();

		//************************************* Check Player State *******************************************

		// Abnormal effects(ex : Stun, Sleep...) are checked in L2Character useMagic()

		if (!SkillHandler.getInstance().checkConditions(this, skill))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		if (isOutOfControl())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		// Check if the player is dead
		if (isDead())
		{
			abortCast();
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		if (isFishing() && (sklType != L2SkillType.PUMPING &&
				sklType != L2SkillType.REELING && sklType != L2SkillType.FISHING))
		{
			// Only fishing skills are available
			sendPacket(SystemMessageId.ONLY_FISHING_SKILLS_NOW);
			return false;
		}

		if (inObserverMode())
		{
			sendPacket(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE);
			abortCast();
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		// Check if the caster is sitting
		if (isSitting() && !skill.isPotion())
		{
			// Send a System Message to the caster
			sendPacket(SystemMessageId.CANT_MOVE_SITTING);

			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		// Check if the skill type is TOGGLE
		if (skill.isToggle())
		{
			// Get effects of the skill
			L2Effect effect = getFirstEffect(skill);

			if (effect != null)
			{
				effect.exit();

				// Send a Server->Client packet ActionFailed to the L2PcInstance
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
		}

		// Check if the player uses "Fake Death" skill
		// Note: do not check this before TOGGLE reset
		if (isFakeDeath())
		{
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		// Check if it's ok to summon
		// Siege Golem (13), Wild Hog Cannon (299), Swoop Cannon (448)
		switch (skill.getId())
		{
		case 13:
		case 299:
		case 448:
			if ((!SiegeManager.getInstance().checkIfOkToSummon(this, false) && !FortSiegeManager.getInstance().checkIfOkToSummon(this, false))
					|| SevenSigns.getInstance().checkSummonConditions(this))
			{
				return false;
			}
		}

		//************************************* Check Target *******************************************

		// Create and set a L2Object containing the target of the skill
		L2Object target = null;
		SkillTargetType sklTargetType = skill.getTargetType();
		Point3D worldPosition = getCurrentSkillWorldPosition();

		if (sklTargetType == SkillTargetType.TARGET_GROUND && worldPosition == null)
		{
			_log.info("WorldPosition is null for skill: " + skill.getName() + ", player: " + getName() + ".");
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		switch (sklTargetType)
		{
		// Target the player if skill type is AURA, PARTY, CLAN or SELF
		case TARGET_AURA:
		case TARGET_FRONT_AURA:
		case TARGET_BEHIND_AURA:
		case TARGET_PARTY:
		case TARGET_ALLY:
		case TARGET_CLAN:
		case TARGET_PARTY_CLAN:
		case TARGET_GROUND:
		case TARGET_SELF:
			target = this;
			break;
		case TARGET_PET:
		case TARGET_SUMMON:
			target = getPet();
			break;
		default:
			target = getTarget();
			break;
		}

		// Check the validity of the target
		if (target == null)
		{
			//sendPacket(SystemMessageId.TARGET_CANT_FOUND);
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		// Skills can be used on walls and doors only during siege
		if (target instanceof L2DoorInstance)
		{
			L2DoorInstance door = (L2DoorInstance) target;
			boolean isCastleDoor = (door.getCastle() != null && door.getCastle().getSiege().getIsInProgress());
			boolean isFortDoor = (door.getFort() != null && door.getFort().getSiege().getIsInProgress() && !door.getIsCommanderDoor());
			if (!isCastleDoor && !isFortDoor && !(door.isUnlockable() && skill.getSkillType() == L2SkillType.UNLOCK))
				return false;
		}

		// Are the target and the player in the same duel?
		if (isInDuel())
		{
			if (!(target instanceof L2Playable && target.getActingPlayer().getDuelId() == getDuelId()))
			{
				sendMessage("You cannot do this while duelling.");
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
		}

		//************************************* Check skill availability *******************************************

		// Check if this skill is enabled (ex : reuse time)
		if (isSkillDisabled(skill.getId()))
		{
			sendReuseMessage(skill);
			return false;
		}

		//************************************* Check Consumables *******************************************

		if (skill.getTransformId() > 0)
		{
			if (getPet() != null)
				getPet().unSummon(this); // Unsummon pets

			if (getEffects().hasEffect(L2EffectType.TRANSFORMATION) || getPet() != null || isMounted() || isFlying())
			{
				sendPacket(new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill));
				return false;
			}
		}

		//************************************* Check Consumables *******************************************

		// Check if spell consumes a Soul
		// Most kamael skills have only optional soul consumption to empower skills!
		if (getSouls() < skill.getSoulConsumeCount())
		{
			sendPacket(SystemMessageId.THERE_IS_NOT_ENOUGH_SOUL);
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		// Check if spell consumes charges
		if (_charges < skill.getNeededCharges())
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addSkillName(skill);
			sendPacket(sm);
			return false;
		}

		// Check if spell adds charges
		if (!skill.isOffensive() && skill.getGiveCharges() > 0 && _charges >= skill.getMaxCharges())
		{
			sendPacket(SystemMessageId.FORCE_MAXLEVEL_REACHED);
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		//************************************* Check Casting Conditions *******************************************

		// Check if all casting conditions are completed
		if (!skill.checkCondition(this, target))
		{
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		//************************************* Check Skill Type *******************************************

		// Check if this is offensive magic skill
		if (skill.isOffensive())
		{
			if (L2Character.isInsidePeaceZone(this, target))
			{
				// If L2Character or target is in a peace zone, send a system message TARGET_IN_PEACEZONE a Server->Client packet ActionFailed
				sendPacket(SystemMessageId.TARGET_IN_PEACEZONE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}

			if (isInOlympiadMode() && !isOlympiadStart())
			{
				// If the L2PcInstance is in Olympia and the match isn't already start, send a Server->Client packet ActionFailed
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}

			// Check if the target is attackable
			if (!target.isAttackable() && (getAccessLevel() < Config.GM_PEACEATTACK))
			{
				// If target is not attackable, send a Server->Client packet ActionFailed
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}

			// Check if a Forced ATTACK is in progress on non-attackable target
			if (!target.isAutoAttackable(this) && !forceUse)
			{
				switch (sklTargetType)
				{
				case TARGET_AURA:
				case TARGET_FRONT_AURA:
				case TARGET_BEHIND_AURA:
				case TARGET_CLAN:
				case TARGET_PARTY_CLAN:
				case TARGET_ALLY:
				case TARGET_PARTY:
				case TARGET_SELF:
				case TARGET_GROUND:
					// Everything okay
					break;
				default:
					// Send a Server->Client packet ActionFailed to the L2PcInstance
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
			}

			// Check if the target is in the skill cast range
			if (dontMove)
			{
				// Calculate the distance between the L2PcInstance and the target
				if (sklTargetType == SkillTargetType.TARGET_GROUND)
				{
					if (!isInsideRadius(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), skill.getCastRange()
							+ getTemplate().getCollisionRadius(), false, false))
					{
						// Send a System Message to the caster
						sendPacket(SystemMessageId.TARGET_TOO_FAR);

						// Send a Server->Client packet ActionFailed to the L2PcInstance
						sendPacket(ActionFailed.STATIC_PACKET);
						return false;
					}
				}
				else if (skill.getCastRange() > 0 && !isInsideRadius(target, skill.getCastRange() + getTemplate().getCollisionRadius(), false, false))
				{
					// Send a System Message to the caster
					sendPacket(SystemMessageId.TARGET_TOO_FAR);

					// Send a Server->Client packet ActionFailed to the L2PcInstance
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
			}
		}

		// Check if the skill is defensive
		if (!skill.isOffensive() && target instanceof L2MonsterInstance && !forceUse && !skill.isNeutral())
		{
			switch (sklTargetType)
			{
			case TARGET_PET:
			case TARGET_SUMMON:
			case TARGET_AURA:
			case TARGET_FRONT_AURA:
			case TARGET_BEHIND_AURA:
			case TARGET_CLAN:
			case TARGET_PARTY_CLAN:
			case TARGET_SELF:
			case TARGET_PARTY:
			case TARGET_ALLY:
			case TARGET_CORPSE_MOB:
			case TARGET_AREA_CORPSE_MOB:
			case TARGET_GROUND:
				// Everything okay
				break;
			default:
				switch (sklType)
				{
				case BEAST_FEED:
				case DELUXE_KEY_UNLOCK:
				case UNLOCK:
				case MAKE_KILLABLE:
					// Everything okay
					break;
				default:
					// send the action failed so that the skill doens't go off.
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
			}
		}

		if (!SkillHandler.getInstance().checkConditions(this, skill, L2Object.getActingCharacter(target)))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		// Check if this is a Pvp skill and target isn't a non-flagged/non-karma player
		switch (sklTargetType)
		{
		case TARGET_PARTY:
		case TARGET_ALLY: // For such skills, checkPvpSkill() is called from L2Skill.getTargetList()
		case TARGET_CLAN: // For such skills, checkPvpSkill() is called from L2Skill.getTargetList()
		case TARGET_PARTY_CLAN:// For such skills, checkPvpSkill() is called from L2Skill.getTargetList()
		case TARGET_AURA:
		case TARGET_FRONT_AURA:
		case TARGET_BEHIND_AURA:
		case TARGET_GROUND:
		case TARGET_SELF:
			break;
		default:
			if (!checkPvpSkill(target, skill) && (getAccessLevel() < Config.GM_PEACEATTACK))
			{
				// Send a System Message to the L2PcInstance
				sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				
				// Send a Server->Client packet ActionFailed to the L2PcInstance
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
		}

		if ((sklTargetType == SkillTargetType.TARGET_HOLY && (!TakeCastle.checkIfOkToCastSealOfRule(this)))
			|| (sklTargetType == SkillTargetType.TARGET_FLAGPOLE && !TakeFort.checkIfOkToCastFlagDisplay(this, false, skill, getTarget())))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			abortCast();
			return false;
		}

		// GeoData Los Check here
		if (skill.getCastRange() > 0)
		{
			if (sklTargetType == SkillTargetType.TARGET_GROUND)
			{
				if (!GeoData.getInstance().canSeeTarget(this, worldPosition))
				{
					sendPacket(SystemMessageId.CANT_SEE_TARGET);
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
			}
			else if (!GeoData.getInstance().canSeeTarget(this, target))
			{
				sendPacket(SystemMessageId.CANT_SEE_TARGET);
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
		}

		// Finally, after passing all conditions
		return true;
	}

	public boolean isInLooterParty(int LooterId)
	{
		L2PcInstance looter = (L2PcInstance) L2World.getInstance().findObject(LooterId);

		// If L2PcInstance is in a CommandChannel
		if (isInParty() && getParty().isInCommandChannel() && looter != null)
			return getParty().getCommandChannel().getMembers().contains(looter);

		if (isInParty() && looter != null)
			return getParty().getPartyMembers().contains(looter);

		return false;
	}

	/**
	 * Check if the requested casting is a Pc->Pc skill cast and if it's a valid pvp condition
	 * @param obj L2Object instance containing the target
	 * @param skill L2Skill instance with the skill being casted
	 * @return False if the skill is a pvpSkill and target is not a valid pvp target
	 */
	public boolean checkPvpSkill(L2Object obj, L2Skill skill)
	{
		return checkPvpSkill(obj, skill, false);
	}

	/**
	 * Check if the requested casting is a Pc->Pc skill cast and if it's a valid pvp condition
	 * @param obj L2Object instance containing the target
	 * @param skill L2Skill instance with the skill being casted
	 * @param srcIsSummon is L2Summon - caster?
	 * @return False if the skill is a pvpSkill and target is not a valid pvp target
	 */
	public boolean checkPvpSkill(L2Object obj, L2Skill skill, boolean srcIsSummon)
	{
		if (obj instanceof L2Character)
			if (GlobalRestrictions.isProtected(this, (L2Character) obj, skill, false))
				return false;

		// Check for PC->PC Pvp status
		if (obj != this && // Target is not self and
				obj instanceof L2PcInstance && // Target is L2PcInstance and
				!(isInDuel() && ((L2PcInstance) obj).getDuelId() == getDuelId()) && // Self is not in a duel and attacking opponent
				!isInsideZone(L2Zone.FLAG_PVP) && // Pc is not in PvP zone
				!((L2PcInstance) obj).isInsideZone(L2Zone.FLAG_PVP) // Target is not in PvP zone
		)
		{
			L2PcInstance target = (L2PcInstance) obj;

			if (skill.isPvpSkill()) // Pvp skill
			{
				if (getClan() != null && target.getClan() != null)
				{
					if (getClan().isAtWarWith(target.getClan().getClanId())
							&& target.getClan().isAtWarWith(getClan().getClanId()))
						return true; // In clan war player can attack whites even with sleep etc.
				}
				if (target.getPvpFlag() == 0 && // Target's pvp flag is not set and
						target.getKarma() == 0 // Target has no karma
				)
					return false;
			}
			else if ((getCurrentSkill() != null && !getCurrentSkill().isCtrlPressed() && skill.isOffensive() && !srcIsSummon)
					|| (getCurrentPetSkill() != null && !getCurrentPetSkill().isCtrlPressed() && skill.isOffensive() && srcIsSummon))
			{
				if (getClan() != null && target.getClan() != null)
				{
					if(getClan().isAtWarWith(target.getClan().getClanId()) && target.getClan().isAtWarWith(getClan().getClanId()))
						return true; // In clan war player can attack whites even without ctrl
				}
				if (target.getPvpFlag() == 0 && // Target's pvp flag is not set and
						target.getKarma() == 0 // Target has no karma
				)
					return false;
			}
		}
		return true;
	}

	/**
	 * Return True if the L2PcInstance is a Mage.<BR><BR>
	 */
	public boolean isMageClass()
	{
		return getClassId().isMage();
	}

	public boolean isMounted()
	{
		return _mountType > 0;
	}

	public boolean checkCanLand()
	{
		// Check if char is in a no landing zone
		if (isInsideZone(L2Zone.FLAG_NOWYVERN))
			return false;

		// If this is a castle that is currently being sieged, and the rider is NOT a castle owner
		// he cannot land.
		// Castle owner is the leader of the clan that owns the castle where the pc is
        return !(SiegeManager.getInstance().checkIfInZone(this)
                && !(getClan() != null && CastleManager.getInstance().getCastle(this) == CastleManager.getInstance().getCastleByOwner(getClan()) && this == getClan()
                .getLeader().getPlayerInstance()));
		}

	/**
	 * Set the type of Pet mounted (0 : none, 1 : Stridder, 2 : Wyvern) and send a Server->Client packet InventoryUpdate to the L2PcInstance.<BR><BR>
	 * @return false if the change of mount type false
	 */
	public boolean setMount(int npcId, int npcLevel, int mountType)
	{
		switch (mountType)
		{
		case 0:
			setIsRidingStrider(false);
			setIsRidingRedStrider(false);
			setIsRidingHorse(false);
			setIsFlying(false);
			isFalling(false, 0); // Initialize the fall just incase dismount was made while in-air
			break; // Dismounted
		case 1:
			if (npcId >= 12526 && npcId <= 12528)
			{
				setIsRidingStrider(true);
			}
			else if (npcId >= 16038 && npcId <= 16040)
			{
				setIsRidingRedStrider(true);
			}
			if (isNoble())
			{
				L2Skill striderAssaultSkill = SkillTable.getInstance().getInfo(325, 1);
				addSkill(striderAssaultSkill); // Not saved to DB
			}
			break;
		case 2:
			setIsFlying(true);
			break; // Flying Wyvern
		case 4:
			setIsRidingHorse(true);
			break;
		}

		_mountType = mountType;
		_mountNpcId = npcId;
		_mountLevel = npcLevel;

		return true;
	}

	/**
	 * @return the type of Pet mounted (0 : none, 1 : Strider, 2 : Wyvern, 3: Wolf).
	 */
	public int getMountType()
	{
		return _mountType;
	}

	/**
	 * Disable the Inventory and create a new task to enable it after 1.5s.<BR><BR>
	 */
	public void tempInventoryDisable()
	{
		_inventoryDisabled = true;

		ThreadPoolManager.getInstance().scheduleGeneral(new InventoryEnable(), 1500);
	}

	/**
	 * Return True if the Inventory is disabled.<BR><BR>
	 */
	public boolean isInventoryDisabled()
	{
		return _inventoryDisabled;
	}

	class InventoryEnable implements Runnable
	{
		public void run()
		{
			_inventoryDisabled = false;
		}
	}

	public Map<Integer, L2CubicInstance> getCubics()
	{
		return _cubics;
	}

	/**
	 * Add a L2CubicInstance to the L2PcInstance _cubics.<BR><BR>
	 */
	public void addCubic(int id, int level, double matk, int activationtime, int activationchance, int totalLifeTime)
	{
		if (_log.isDebugEnabled())
			_log.info("L2PcInstance(" + getName() + "): addCubic(" + id + "|" + level + "|" + matk + ")");
		L2CubicInstance cubic = new L2CubicInstance(this, id, level, (int) matk, activationtime, activationchance, totalLifeTime);
		_cubics.put(id, cubic);
	}
	
	public void addCubic(L2SkillSummon skill)
	{
		addCubic(skill.getNpcId(), skill.getLevel(), skill.getPower(), skill.getActivationTime(), skill
				.getActivationChance(), skill.getTotalLifeTime());
	}

	/**
	 * Remove a L2CubicInstance from the L2PcInstance _cubics.<BR><BR>
	 */
	public void delCubic(int id)
	{
		_cubics.remove(id);
	}

	/**
	 * Return the L2CubicInstance corresponding to the Identifier of the L2PcInstance _cubics.<BR><BR>
	 */
	public L2CubicInstance getCubic(int id)
	{
		return _cubics.get(id);
	}

	@Override
	public String toString()
	{
		return "player " + getName();
	}

	/**
	 * Return the modifier corresponding to the Enchant Effect of the Active Weapon (Min : 127).<BR><BR>
	 */
	public int getEnchantEffect()
	{
		L2ItemInstance wpn = getActiveWeaponInstance();

		if (wpn == null)
			return 0;

		return Math.min(127, wpn.getEnchantLevel());
	}

	/**
	 * Set the _lastFolkNpc of the L2PcInstance corresponding to the last Folk wich one the player talked.<BR><BR>
	 */
	public void setLastFolkNPC(L2Npc folkNpc)
	{
		_lastFolkNpc = folkNpc;
	}

	/**
	 * Return the _lastFolkNpc of the L2PcInstance corresponding to the last Folk wich one the player talked.<BR><BR>
	 */
	public L2Npc getLastFolkNPC()
	{
		return _lastFolkNpc;
	}

	/**
	 * Return True if L2PcInstance is a participant in the Festival of Darkness.<BR><BR>
	 */
	public boolean isFestivalParticipant()
	{
		return SevenSignsFestival.getInstance().isParticipant(this);
	}

	private ScheduledFuture<?>	_taskWarnUserTakeBreak;

	class WarnUserTakeBreak implements Runnable
	{
		public void run()
		{
			sendPacket(SystemMessageId.PLAYING_FOR_LONG_TIME);
		}
	}

	class RentPetTask implements Runnable
	{
		public void run()
		{
			stopRentPet();
		}
	}

	class WaterTask implements Runnable
	{
		public void run()
		{
			double reduceHp = getMaxHp() / 100.0;

			if (reduceHp < 1)
				reduceHp = 1;

			reduceCurrentHp(reduceHp, L2PcInstance.this, false);
			// Reduced hp, because not rest
			SystemMessage sm = new SystemMessage(SystemMessageId.DROWN_DAMAGE_S1);
			sm.addNumber((int) reduceHp);
			sendPacket(sm);

		}
	}

	class LookingForFishTask implements Runnable
	{
		boolean	_isNoob, _isUpperGrade;
		int		_fishType, _fishGutsCheck, _gutsCheckTime;
		long	_endTaskTime;

		protected LookingForFishTask(int fishWaitTime, int fishGutsCheck, int fishType, boolean isNoob, boolean isUpperGrade)
		{
			_fishGutsCheck = fishGutsCheck;
			_endTaskTime = System.currentTimeMillis() + fishWaitTime + 10000;
			_fishType = fishType;
			_isNoob = isNoob;
			_isUpperGrade = isUpperGrade;
		}

		public void run()
		{
			if (System.currentTimeMillis() >= _endTaskTime)
			{
				endFishing(false);
				return;
			}

			if (!GameTimeController.getInstance().isNowNight() && _lure.isNightLure())
				return;

			int check = Rnd.get(1000);
			if (_fishGutsCheck > check)
			{
				stopLookingForFishTask();
				startFishCombat(_isNoob, _isUpperGrade);
			}
		}
	}

	public int getClanPrivileges()
	{
		return _clanPrivileges;
	}

	public void setClanPrivileges(int n)
	{
		_clanPrivileges = n;
	}

	public int getBookMarkSlot()
	{
		return _bookmarkslot;
	}

	public void setBookMarkSlot(int slot)
	{
		_bookmarkslot = slot;
		sendPacket(new ExGetBookMarkInfoPacket(this));
	}

	public void enterObserverMode(int x, int y, int z)
	{
		_obsX = getX();
		_obsY = getY();
		_obsZ = getZ();

		setTarget(null);
		stopMove(null);
		startParalyze();
		setIsInvul(true);
		getAppearance().setInvisible();
		sendPacket(new GMHide(1));
		sendPacket(new ObservationMode(x, y, z));
		getPosition().setXYZ(x, y, z);

		_observerMode = true;

		updateInvisibilityStatus();
	}

	public void enterOlympiadObserverMode(int x, int y, int z, int id, boolean storeCoords)
	{
		if (getPet() != null)
			getPet().unSummon(this);

		if (!getCubics().isEmpty())
		{
			for (L2CubicInstance cubic : getCubics().values())
			{
				cubic.stopAction();
				cubic.cancelDisappear();
			}

			getCubics().clear();
		}

		if (getParty() != null)
			getParty().removePartyMember(this);

		_olympiadGameId = id;
		if (isSitting())
			standUp();
		if (storeCoords)
		{
			_obsX = getX();
			_obsY = getY();
			_obsZ = getZ();
		}
		setTarget(null);
		setIsInvul(true);
		getAppearance().setInvisible();
		teleToLocation(x, y, z, false);
		sendPacket(new GMHide(1));
		sendPacket(new ExOlympiadMode(3));
		_observerMode = true;

		updateInvisibilityStatus();
	}

	// [L2J_JP ADD SANDMAN]
	public void enterMovieMode()
	{
		setTarget(null);
		stopMove(null);
		setIsInvul(true);
		setIsImmobilized(true);
		sendPacket(new CameraMode(1));
	}

	public void leaveMovieMode()
	{
		if (!isGM())
			setIsInvul(false);
		setIsImmobilized(false);
		sendPacket(new CameraMode(0));
	}

	/**
	 * yaw:North=90, south=270, east=0, west=180<BR>
	 * pitch > 0:looks up,pitch < 0:looks down<BR>
	 * time:faster that small value is.<BR>
	 */
	public void specialCamera(L2Object target, int dist, int yaw, int pitch, int time, int duration)
	{
		sendPacket(new SpecialCamera(target.getObjectId(), dist, yaw, pitch, time, duration));
	}

	// L2JJP END

	public void leaveObserverMode()
	{
		setTarget(null);
		getPosition().setXYZ(_obsX, _obsY, _obsZ);
		sendPacket(new GMHide(0));
		stopParalyze(false);

		if (!isGM())
		{
			getAppearance().setVisible();
			setIsInvul(false);
		}

		if (getAI() != null)
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

		teleToLocation(_obsX, _obsY, _obsZ);
		sendPacket(new ObservationReturn(this));
		_observerMode = false;
		broadcastUserInfo();
	}

	public void leaveOlympiadObserverMode()
	{
		setTarget(null);
		sendPacket(new ExOlympiadMode(0));
		teleToLocation(_obsX, _obsY, _obsZ);
		sendPacket(new GMHide(0));
		if (!isGM())
		{
			getAppearance().setVisible();
			setIsInvul(false);
		}
		if (getAI() != null)
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
		Olympiad.removeSpectator(_olympiadGameId, this);
		_olympiadGameId = -1;
		_observerMode = false;
		broadcastUserInfo();
	}

	public void updateNameTitleColor()
	{
		if (isGM())
		{
			if (Config.GM_NAME_COLOR_ENABLED)
			{
				if (getAccessLevel() >= 100)
					getAppearance().setNameColor(Config.ADMIN_NAME_COLOR);
				else if (getAccessLevel() >= 75)
					getAppearance().setNameColor(Config.GM_NAME_COLOR);
			}
			else
			{
				getAppearance().setNameColor(0xFFFFFF);
			}
			if (Config.GM_TITLE_COLOR_ENABLED)
			{
				if (getAccessLevel() >= 100)
					getAppearance().setTitleColor(Config.ADMIN_TITLE_COLOR);
				else if (getAccessLevel() >= 75)
					getAppearance().setTitleColor(Config.GM_TITLE_COLOR);
			}
			else
			{
				getAppearance().setTitleColor(0xFFFF77);
			}
		}
		else if (getClan() != null && isClanLeader() && Config.CLAN_LEADER_COLOR_ENABLED
				&& getClan().getLevel() >= Config.CLAN_LEADER_COLOR_CLAN_LEVEL)
		{
			if (Config.CLAN_LEADER_COLORED == Config.ClanLeaderColored.name)
				getAppearance().setNameColor(Config.CLAN_LEADER_COLOR);
			else
				getAppearance().setTitleColor(Config.CLAN_LEADER_COLOR);
		}
		else
		{
			getAppearance().setNameColor(0xFFFFFF);
			getAppearance().setTitleColor(0xFFFF77);
		}
		broadcastUserInfo();
	}

	public void setOlympiadSide(int i)
	{
		_olympiadSide = i;
	}

	public int getOlympiadSide()
	{
		return _olympiadSide;
	}

	public void setOlympiadGameId(int id)
	{
		_olympiadGameId = id;
	}

	public int getOlympiadGameId()
	{
		return _olympiadGameId;
	}

	public int getOlyDamage()
	{
		return _olyDamage;
	}

	public void setOlyDamage(int dmg)
	{
		_olyDamage = dmg;
	}

	public void addOlyDamage(int dmg)
	{
		_olyDamage = _olyDamage + dmg;
	}

	public void reduceOlyDamage(int dmg)
	{
		if (_olyDamage - dmg < 0)
			_olyDamage = 0;
		else
			_olyDamage = _olyDamage - dmg;
	}

	public int getObsX()
	{
		return _obsX;
	}

	public int getObsY()
	{
		return _obsY;
	}

	public int getObsZ()
	{
		return _obsZ;
	}

	public boolean inObserverMode()
	{
		return _observerMode;
	}

	public int getTeleMode()
	{
		return _telemode;
	}

	public void setTeleMode(int mode)
	{
		_telemode = mode;
	}

	public void setLoto(int i, int val)
	{
		_loto[i] = val;
	}

	public int getLoto(int i)
	{
		return _loto[i];
	}

	public void setRace(int i, int val)
	{
		_race[i] = val;
	}

	public int getRace(int i)
	{
		return _race[i];
	}

	@Deprecated
	private void setBanChatTimer(long timer)
	{
	}

	@Deprecated
	private long getBanChatTimer()
	{
		return 0;
	}

	public boolean isChatBanned()
	{
		return ObjectRestrictions.getInstance().checkRestriction(this, AvailableRestriction.PlayerChat);
	}

	public boolean getMessageRefusal()
	{
		return _messageRefusal;
	}

	public void setMessageRefusal(boolean mode)
	{
		_messageRefusal = mode;
		sendEtcStatusUpdate();
	}

	public void setDietMode(boolean mode)
	{
		_dietMode = mode;
	}

	public boolean getDietMode()
	{
		return _dietMode;
	}

	public void setTradeRefusal(boolean mode)
	{
		_tradeRefusal = mode;
	}

	public boolean getTradeRefusal()
	{
		return _tradeRefusal;
	}

	public void setExchangeRefusal(boolean mode)
	{
		_exchangeRefusal = mode;
	}

	public boolean getExchangeRefusal()
	{
		return _exchangeRefusal;
	}

	public BlockList getBlockList()
	{
		if (_blockList == null)
			_blockList = new BlockList(this);

		return _blockList;
	}

	public L2FriendList getFriendList()
	{
		if (_friendList == null)
			_friendList = new L2FriendList(this);

		return _friendList;
	}

	public void setHero(boolean hero)
	{
		if (hero && _baseClass == _activeClass)
			for (L2Skill s : HeroSkillTable.getHeroSkills())
				addSkill(s, false); // Dont Save Hero skills to Sql
		else
			for (L2Skill s : HeroSkillTable.getHeroSkills())
				super.removeSkill(s); // Just Remove skills without deleting from Sql
		_hero = hero;
		sendSkillList();
	}

	public void setIsInOlympiadMode(boolean b)
	{
		_inOlympiadMode = b;
	}

	public void setIsOlympiadStart(boolean b)
	{
		_olympiadStart = b;
		// Clear olympiad damage incase its not the first match since init of l2pcisntance
		if (b)
			setOlyDamage(0);
	}

	public boolean isOlympiadStart()
	{
		return _olympiadStart;
	}

	public boolean isHero()
	{
		return _hero;
	}

	public boolean isInOlympiadMode()
	{
		return _inOlympiadMode;
	}

	public void setNoble(boolean val)
	{
		if (val)
			for (L2Skill s : NobleSkillTable.getNobleSkills())
				addSkill(s, false); // Dont Save Noble skills to Sql
		else
			for (L2Skill s : NobleSkillTable.getNobleSkills())
				super.removeSkill(s); // Just Remove skills without deleting from Sql
		_noble = val;
		sendSkillList();
	}

	public boolean isInDuel()
	{
		return _isInDuel;
	}

	public int getDuelId()
	{
		return _duelId;
	}

	public void setDuelState(int mode)
	{
		_duelState = mode;
	}

	public int getDuelState()
	{
		return _duelState;
	}

	/**
	 * Sets up the duel state using a non 0 duelId.
	 * @param duelId 0=not in a duel
	 */
	public void setIsInDuel(int duelId)
	{
		if (duelId > 0)
		{
			_isInDuel = true;
			_duelState = Duel.DUELSTATE_DUELLING;
			_duelId = duelId;
		}
		else
		{
			if (_duelState == Duel.DUELSTATE_DEAD)
			{
				enableAllSkills();
				getStatus().startHpMpRegeneration();
			}
			_isInDuel = false;
			_duelState = Duel.DUELSTATE_NODUEL;
			_duelId = 0;
		}
	}

	/**
	 * This returns a SystemMessage stating why
	 * the player is not available for duelling.
	 * @return S1_CANNOT_DUEL... message
	 */
	public SystemMessage getNoDuelReason()
	{
		// This is somewhat hacky - but that case should never happen anyway...
		if (_noDuelReason == 0)
			_noDuelReason = SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL.getId();

		SystemMessage sm = new SystemMessage(SystemMessageId.getSystemMessageId(_noDuelReason));
		sm.addPcName(this);
		_noDuelReason = 0;
		return sm;
	}

	/**
	 * Checks if this player might join / start a duel.
	 * To get the reason use getNoDuelReason() after calling this function.
	 * @return true if the player might join/start a duel.
	 */
	public boolean canDuel()
	{
		if (isInCombat() || isInJail())
		{
			_noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_ENGAGED_IN_BATTLE.getId();
			return false;
		}
		if (isDead() || isAlikeDead() || (getStatus().getCurrentHp() < getStat().getMaxHp() / 2 || getStatus().getCurrentMp() < getStat().getMaxMp() / 2))
		{
			_noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_HP_OR_MP_IS_BELOW_50_PERCENT.getId();
			return false;
		}
		if (isInDuel())
		{
			_noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_ALREADY_ENGAGED_IN_A_DUEL.getId();
			return false;
		}
		if (isInOlympiadMode())
		{
			_noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_PARTICIPATING_IN_THE_OLYMPIAD.getId();
			return false;
		}
		if (isCursedWeaponEquipped())
		{
			_noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_IN_A_CHAOTIC_STATE.getId();
			return false;
		}
		if (getPrivateStoreType() != STORE_PRIVATE_NONE)
		{
			_noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_ENGAGED_IN_A_PRIVATE_STORE_OR_MANUFACTURE.getId();
			return false;
		}
		if (isMounted() || isInBoat())
		{
			_noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_RIDING_A_BOAT_WYVERN_OR_STRIDER.getId();
			return false;
		}
		if (isFishing())
		{
			_noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_FISHING.getId();
			return false;
		}
		if (isInsideZone(L2Zone.FLAG_PVP) || isInsideZone(L2Zone.FLAG_PEACE) || SiegeManager.getInstance().checkIfInZone(this))
		{
			_noDuelReason = SystemMessageId.C1_CANNOT_MAKE_A_CHALLANGE_TO_A_DUEL_BECAUSE_C1_IS_CURRENTLY_IN_A_DUEL_PROHIBITED_AREA.getId();
			return false;
		}
		if (GlobalRestrictions.isRestricted(this, DuelRestriction.class))
		{
			_noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_ENGAGED_IN_BATTLE.getId(); // TODO
			return false;
		}
		return true;
	}

	public boolean isNoble()
	{
		/**_noble = false;

		 if (isSubClassActive())
		 {
		 if (getTotalSubClasses() > 1)
		 {
		 _noble = true;
		 return _noble;
		 }
		 if (getLevel() >= 75)
		 {
		 _noble = true;
		 return _noble;
		 }
		 }
		 else
		 {
		 if (getSubClasses() == null)
		 return _noble;

		 if (getLevel() >= 76 && getClassId().getId() >= 88)
		 {
		 for (SubClass sub : getSubClasses().values())
		 {
		 if (sub.getLevel() >= 75)
		 {
		 _noble = true;
		 return _noble;
		 }
		 }
		 }
		 }*/

		return _noble;
	}

	public int getSubLevel()
	{
		if (isSubClassActive())
		{
			int lvl = getLevel();
			return lvl;
		}
		return 0;
	}

	// Baron, Wise Man etc, calculated on EnterWorld and when rank is changing
	public void setPledgeClass(int classId)
	{
		_pledgeClass = classId;
	}

	public int getPledgeClass()
	{
		return _pledgeClass;
	}

	public void setSubPledgeType(int typeId)
	{
		_subPledgeType = typeId;
	}

	public int getSubPledgeType()
	{
		return _subPledgeType;
	}

	public int getPledgeRank()
	{
		return _pledgeRank;
	}

	public void setPledgeRank(int rank)
	{
		_pledgeRank = rank;
	}

	public int getApprentice()
	{
		return _apprentice;
	}

	public void setApprentice(int apprentice_id)
	{
		_apprentice = apprentice_id;
	}

	public int getSponsor()
	{
		return _sponsor;
	}

	public void setSponsor(int sponsor_id)
	{
		_sponsor = sponsor_id;
	}

	public void setLvlJoinedAcademy(int lvl)
	{
		_lvlJoinedAcademy = lvl;
	}

	public int getLvlJoinedAcademy()
	{
		return _lvlJoinedAcademy;
	}

	public boolean isAcademyMember()
	{
		return _lvlJoinedAcademy > 0;
	}

	public void setTeam(int team)
	{
		_team = team;
		if (getPet() != null)
			getPet().broadcastStatusUpdate();
	}

	public int getTeam()
	{
		return _team;
	}

	public void setWantsPeace(int wantsPeace)
	{
		_wantsPeace = wantsPeace;
	}

	public int getWantsPeace()
	{
		return _wantsPeace;
	}

	public boolean isFishing()
	{
		return _fishing;
	}

	public void setFishing(boolean fishing)
	{
		_fishing = fishing;
	}

	public void setAllianceWithVarkaKetra(int sideAndLvlOfAlliance)
	{
		// [-5,-1] varka, 0 neutral, [1,5] ketra
		_alliedVarkaKetra = sideAndLvlOfAlliance;
	}

	public int getAllianceWithVarkaKetra()
	{
		return _alliedVarkaKetra;
	}

	public boolean isAlliedWithVarka()
	{
		return (_alliedVarkaKetra < 0);
	}

	public boolean isAlliedWithKetra()
	{
		return (_alliedVarkaKetra > 0);
	}

	private final Comparator<L2Skill> SKILL_LIST_COMPARATOR = new Comparator<L2Skill>() {
		@Override
		public int compare(L2Skill s1, L2Skill s2)
		{
			int o1 = getOrder(s1);
			int o2 = getOrder(s2);

			if (o1 != o2)
				return (o1 < o2) ? -1 : 1;

			int so1 = getSubOrder(s1);
			int so2 = getSubOrder(s2);

			if (so1 != so2)
				return (so1 < so2) ? -1 : 1;

			return s1.getId().compareTo(s2.getId());
		}

		private int getOrder(L2Skill s)
		{
			if (s.getSkillType() == L2SkillType.NOTDONE)
				return 10;

			if (isTransformationDisabledSkill(s))
				return 9;

			if (s.getId() > 9000 && s.getId() < 9007)
				return 7;

			// TODO: add other ordering conditions, if there is any other useful :)

			return 0;
		}

		private int getSubOrder(L2Skill s)
		{
			if (s.isPositive())
				return 1;
			else if (s.isNeutral())
				return 0;
			else // s.isOffensive()
				return -1;
		}
	};

	public L2Skill[] getSortedAllSkills(boolean isGM)
	{
		L2Skill[] array = getAllSkills();

		for (int i = 0; i < array.length; i++)
		{
			L2Skill s = array[i];

			if (s == null)
				continue;

			if (!isGM)
			{
				if (s.getId() > 9000 && s.getId() < 9007)
				{
					array[i] = null;
					continue; // Fake skills to change base stats
				}

				// Hide skills when transformed if they are not passive
				if (isTransformationDisabledSkill(s))
				{
					array[i] = null;
					continue;
				}
			}

			if (s.getSkillType() == L2SkillType.NOTDONE)
			{
				switch (Config.SEND_NOTDONE_SKILLS)
				{
					case 1:
					{
						array[i] = null;
						continue;
					}
					case 2:
					{
						if (!isGM)
						{
							array[i] = null;
							continue;
						}
					}
				}
			}
		}

		array = L2Arrays.compact(array);

		Arrays.sort(array, SKILL_LIST_COMPARATOR);

		return array;
	}

	public void sendSkillList()
	{
		sendPacket(new SkillList(this));
	}

	public void setTransformAllowedSkills(int[] ids)
	{
		_transformAllowedSkills.clear();
		for (int id : ids)
			addTransformAllowedSkill(id);
	}

	public void addTransformAllowedSkill(int[] ids)
	{
		for (int id : ids)
			addTransformAllowedSkill(id);
	}

	public void addTransformAllowedSkill(int id)
	{
		_transformAllowedSkills.add(id);
	}

	public boolean containsAllowedTransformSkill(int id)
	{
		return _transformAllowedSkills.contains(id);
	}

	/** Section for mounted pets */
	class FeedTask implements Runnable
	{
		public void run()
		{
			try
			{
				if (!isMounted())
				{
					stopFeed();
					return;
				}

				if (getCurrentFeed() > getFeedConsume())
				{
					// eat
					setCurrentFeed(getCurrentFeed()-getFeedConsume());
				}
				else
				{
					// go back to pet control item, or simply said, unsummon it
					setCurrentFeed(0);
					stopFeed();
					dismount();
					sendPacket(new SystemMessage(SystemMessageId.OUT_OF_FEED_MOUNT_CANCELED));
				}

				int[] foodIds = PetDataTable.getFoodItemId(getMountNpcId());
				if (foodIds[0] == 0)
					return;
				L2ItemInstance food = null;
				food = getInventory().getItemByItemId(foodIds[0]);

				// use better strider food if exists
				if (PetDataTable.isStrider(getMountNpcId()))
				{
					if (getInventory().getItemByItemId(foodIds[1]) != null)
						food = getInventory().getItemByItemId(foodIds[1]);
				}
				if (food != null && isHungry())
				{
					if (ItemHandler.getInstance().useItem(food.getItemId(), L2PcInstance.this, food))
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.PET_TOOK_S1_BECAUSE_HE_WAS_HUNGRY);
						sm.addItemName(food.getItemId());
						sendPacket(sm);
					}
				}
			}
			catch (Exception e)
			{
				_log.fatal("Mounted Pet [NpcId: "+getMountNpcId()+"] a feed task error has occurred", e);
			}
		}
	}

	protected synchronized void startFeed(int npcId)
	{
		_canFeed = npcId > 0;
		if (!isMounted())
			return;
		if (getPet() != null)
		{
			setCurrentFeed(((L2PetInstance) getPet()).getCurrentFed());
			_controlItemId = getPet().getControlItemId();
			sendPacket(new SetupGauge(3, getCurrentFeed()*10000/getFeedConsume(), getMaxFeed()*10000/getFeedConsume()));
			if (!isDead())
			{
				_mountFeedTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new FeedTask(), 10000, 10000);
			}
		}
		else if (_canFeed)
		{
			setCurrentFeed(getMaxFeed());
			SetupGauge sg = new SetupGauge(3, getCurrentFeed()*10000/getFeedConsume(), getMaxFeed()*10000/getFeedConsume());
			sendPacket(sg);
			if (!isDead())
			{
				_mountFeedTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new FeedTask(), 10000, 10000);
			}
		}
	}

	protected synchronized void stopFeed()
	{
		if (_mountFeedTask != null)
		{
			_mountFeedTask.cancel(false);
			_mountFeedTask = null;
			if (_log.isDebugEnabled())
				_log.info("Pet [#"+_mountNpcId+"] feed task stop");
		}
	}

	protected final void clearPetData()
	{
		_data = null;
	}

	protected final L2PetData getPetData(int npcId)
	{
		if (_data == null && getPet() != null)
		{
			_data = PetDataTable.getInstance().getPetData(getPet().getNpcId(), getPet().getLevel());
		}
		else if (_data == null && npcId > 0)
		{
			_data = PetDataTable.getInstance().getPetData(npcId, getLevel());
		}

		return _data;
	}

	public int getCurrentFeed()
	{
		return _curFeed;
	}

	protected int getFeedConsume()
	{
		// if pet is attacking
		if (isAttackingNow())
			return getPetData(_mountNpcId).getPetFeedBattle();
		else
			return getPetData(_mountNpcId).getPetFeedNormal();
	}

	public void setCurrentFeed(int num)
	{
		_curFeed = num > getMaxFeed() ? getMaxFeed() : num;
		SetupGauge sg = new SetupGauge(3, getCurrentFeed()*10000/getFeedConsume(), getMaxFeed()*10000/getFeedConsume());
		sendPacket(sg);
	}

	protected int getMaxFeed()
	{
		return getPetData(_mountNpcId).getPetMaxFeed();
	}

	protected boolean isHungry()
	{
		return _canFeed ? (getCurrentFeed() < (0.55 * getPetData(getMountNpcId()).getPetMaxFeed())) : false;
	}

	public class dismount implements Runnable
	{
		public void run()
		{
			dismount();
		}
	}

	public void enteredNoWyvernZone()
	{
		sendPacket(SystemMessageId.AREA_CANNOT_BE_ENTERED_WHILE_MOUNTED_WYVERN);

		_dismountTask = ThreadPoolManager.getInstance().scheduleGeneral(new L2PcInstance.dismount(), 5000);
	}

	public void exitedNoWyvernZone()
	{
		if (_dismountTask != null)
		{
			_dismountTask.cancel(false);
			_dismountTask = null;
		}
	}

	public void storePetFood(int petId)
	{
		if (_controlItemId != 0 && petId != 0)
		{
			String req = "UPDATE pets SET fed=? WHERE item_obj_id = ?";
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(req);
				statement.setInt(1, getCurrentFeed());
				statement.setInt(2, _controlItemId);
				statement.executeUpdate();
				statement.close();
				_controlItemId = 0;
			}
			catch (Exception e)
			{
				_log.fatal("Failed to store Pet [NpcId: "+petId+"] data", e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}
	}
	/** End of section for mounted pets */

	/**
	 * 1. Add the specified class ID as a subclass (up to the maximum number of <b>three</b>)
	 * for this character.<BR>
	 * 2. This method no longer changes the active _classIndex of the player. This is only
	 * done by the calling of setActiveClass() method as that should be the only way to do so.
	 *
	 * @param classId
	 * @param classIndex
	 * @return subclassAdded
	 */
	public boolean addSubClass(int classId, int classIndex)
	{
		if (!_subclassLock.tryLock())
			return false;

    	try
    	{
        	if (getTotalSubClasses() == Config.ALT_MAX_SUBCLASS || classIndex == 0)
        		return false;

        	if (getSubClasses().containsKey(classIndex))
        		return false;

        	// Note: Never change _classIndex in any method other than setActiveClass().

            SubClass newClass = new SubClass();
            newClass.setClassId(classId);
            newClass.setClassIndex(classIndex);

            Connection con = null;
            try
            {
                // Store the basic info about this new sub-class.
                con = L2DatabaseFactory.getInstance().getConnection();
                PreparedStatement statement = con.prepareStatement(ADD_CHAR_SUBCLASS);
                statement.setInt(1, getObjectId());
                statement.setInt(2, newClass.getClassId());
                statement.setLong(3, newClass.getExp());
                statement.setInt(4, newClass.getSp());
                statement.setInt(5, newClass.getLevel());
                statement.setInt(6, newClass.getClassIndex()); // <-- Added
                statement.execute();
                statement.close();
            }
            catch (Exception e)
            {
                _log.warn("Could not add character sub class for " + getName() + ": ", e);
                return false;
            }
            finally
            {
            	L2DatabaseFactory.close(con);
            }

            // Commit after database INSERT incase exception is thrown.
            getSubClasses().put(newClass.getClassIndex(), newClass);

            if (_log.isDebugEnabled())
                _log.info(getName() + " added class ID " + classId + " as a sub class at index " + classIndex + ".");

    		ClassId subTemplate = ClassId.values()[classId];
    		Collection<L2SkillLearn> skillTree = SkillTreeTable.getInstance().getAllowedSkills(subTemplate);

    		if (skillTree == null)
    			return true;

    		Map<Integer, L2Skill> prevSkillList = new HashMap<Integer, L2Skill>();

    		for (L2SkillLearn skillInfo : skillTree)
    		{
    			if (skillInfo.getMinLevel() <= 40)
    			{
    				L2Skill prevSkill = prevSkillList.get(skillInfo.getId());
    				L2Skill newSkill = SkillTable.getInstance().getInfo(skillInfo.getId(), skillInfo.getLevel());

    				if (prevSkill != null && (prevSkill.getLevel() >= newSkill.getLevel()))
    					continue;

    				prevSkillList.put(newSkill.getId(), newSkill);
    				storeSkill(newSkill, prevSkill, classIndex);
    			}
    		}

    		if (_log.isDebugEnabled())
                _log.info(getName() + " was given " + getAllSkills().length + " skills for their new sub class.");

            return true;
    	}
    	finally
    	{
    		_subclassLock.unlock();
    	}
	}

	/**
	 * 1. Completely erase all existance of the subClass linked to the classIndex.<BR>
	 * 2. Send over the newClassId to addSubClass()to create a new instance on this classIndex.<BR>
	 * 3. Upon Exception, revert the player to their BaseClass to avoid further problems.<BR>
	 *
	 * @param classIndex
	 * @param newClassId
	 * @return subclassAdded
	 */
	public boolean modifySubClass(int classIndex, int newClassId)
	{
    	if (!_subclassLock.tryLock())
    		return false;

    	try
    	{
        	int oldClassId = getSubClasses().get(classIndex).getClassId();

            if (_log.isDebugEnabled())
    	    	_log.info(getName() + " has requested to modify sub class index " + classIndex + " from class ID " + oldClassId + " to " + newClassId + ".");

        	Connection con = null;
            try
            {
                con = L2DatabaseFactory.getInstance().getConnection();

                // Remove all henna info stored for this sub-class.
                PreparedStatement statement = con.prepareStatement(DELETE_CHAR_HENNAS);
                statement.setInt(1, getObjectId());
                statement.setInt(2, classIndex);
                statement.execute();
                statement.close();

                // Remove all shortcuts info stored for this sub-class.
                statement = con.prepareStatement(DELETE_CHAR_SHORTCUTS);
                statement.setInt(1, getObjectId());
                statement.setInt(2, classIndex);
                statement.execute();
                statement.close();

                // Remove all effects info stored for this sub-class.
                statement = con.prepareStatement(DELETE_SKILL_SAVE);
                statement.setInt(1, getObjectId());
                statement.setInt(2, classIndex);
                statement.execute();
                statement.close();

                // Remove all skill info stored for this sub-class.
                statement = con.prepareStatement(DELETE_CHAR_SKILLS);
                statement.setInt(1, getObjectId());
                statement.setInt(2, classIndex);
                statement.execute();
                statement.close();

                // Remove all basic info stored about this sub-class.
                statement = con.prepareStatement(DELETE_CHAR_SUBCLASS);
                statement.setInt(1, getObjectId());
                statement.setInt(2, classIndex);
                statement.execute();
                statement.close();
            }
            catch (Exception e)
            {
            	_log.warn("Could not modify sub class for " + getName() + " to class index " + classIndex + ": ", e);
            	// This must be done in order to maintain data consistency.
                getSubClasses().remove(classIndex);
            	return false;
            }
            finally
            {
                L2DatabaseFactory.close(con);
            }

            getSubClasses().remove(classIndex);
    	}
    	finally
    	{
    		_subclassLock.unlock();
    	}

    	return addSubClass(newClassId, classIndex);
	}

	public boolean isSubClassActive()
	{
		return _classIndex > 0;
	}

	public Map<Integer, SubClass> getSubClasses()
	{
		if (_subClasses == null)
			_subClasses = new SingletonMap<Integer, SubClass>();

		return _subClasses;
	}

	public int getTotalSubClasses()
	{
		return getSubClasses().size();
	}

	public int getBaseClass()
	{
		return _baseClass;
	}

	public int getActiveClass()
	{
		return _activeClass;
	}

	public int getClassIndex()
	{
		return _classIndex;
	}

	private void setClassTemplate(int classId)
	{
		_activeClass = classId;

		L2PcTemplate t = CharTemplateTable.getInstance().getTemplate(classId);

		if (t == null)
		{
			_log.fatal("Missing template for classId: " + classId);
			throw new Error();
		}

		// Set the template of the L2PcInstance
		setTemplate(t);
	}

	/**
	 * Changes the character's class based on the given class index.
	 * <BR><BR>
	 * An index of zero specifies the character's original (base) class,
	 * while indexes 1-3 specifies the character's sub-classes respectively.
	 * <br><br>
	 * <font color="00FF00"/>WARNING: Use only on subclass change</font>
	 *
	 * @param classIndex
	 */
    public boolean setActiveClass(int classIndex)
    {
    	if (!_subclassLock.tryLock())
    		return false;

    	try
    	{
        	//  Cannot switch or change subclasses while transformed
            if (_transformation != null)
                return false;

            // Remove active item skills before saving char to database
            // because next time when choosing this class, weared items can be different

            for (L2ItemInstance temp : getInventory().getAugmentedItems())
                if (temp != null && temp.isEquipped())
                	temp.getAugmentation().removeBonus(this);

            // Remove class circlets (can't equip circlets while being in subclass)
            L2ItemInstance circlet = getInventory().getPaperdollItem(Inventory.PAPERDOLL_HAIRALL);
            if (circlet != null)
            {
            	if (((circlet.getItemId() >= 9397 && circlet.getItemId() <= 9408) || circlet.getItemId() == 10169) && circlet.isEquipped())
            	{
            		L2ItemInstance[] unequipped = getInventory().unEquipItemInBodySlotAndRecord(circlet.getItem().getBodyPart());
            		InventoryUpdate iu = new InventoryUpdate();
            		for (L2ItemInstance element : unequipped)
            			iu.addModifiedItem(element);
            		sendPacket(iu);
            	}
            }

            // Delete a force buff upon class change.
            if(_fusionSkill != null)
                abortCast();

            // Stop casting for any player that may be casting a force buff on this l2pcinstance.
    		for(L2Character character : getKnownList().getKnownCharacters())
    		{
    			if(character.getFusionSkill() != null && character.getFusionSkill().getTarget() == this)
    				character.abortCast();
    		}

            /*
             * 1. Call store() before modifying _classIndex to avoid skill effects rollover.
             * 2. Register the correct _classId against applied 'classIndex'.
             */

            store(Config.SUBCLASS_STORE_SKILL_COOLTIME);
            _reuseTimeStamps.clear();

            // clear charges
            clearCharges();

            if (classIndex == 0)
            {
            	setClassTemplate(getBaseClass());
            }
            else
            {
                try
                {
                	setClassTemplate(getSubClasses().get(classIndex).getClassId());
                }
                catch (Exception e)
                {
                    _log.info("Could not switch " + getName() + "'s sub class to class index " + classIndex + ": ", e);
                    return false;
                }
            }
            _classIndex = classIndex;

            if(isInParty())
            {
            	if (Config.MAX_PARTY_LEVEL_DIFFERENCE > 0)
            	{
            		for (L2PcInstance p : getParty().getPartyMembers())
            		{
            			if (Math.abs(p.getLevel() - getLevel()) > Config.MAX_PARTY_LEVEL_DIFFERENCE)
            			{
            				getParty().removePartyMember(this);
            				sendMessage("You have been removed from your party, because the level difference is too big.");
            				break;
            			}
            		}
            	}
            	else
            		getParty().recalculatePartyLevel();
            }

            /*
    		 * Update the character's change in class status.
             *
    		 * 1. Remove any active cubics from the player.
             * 2. Renovate the characters table in the database with the new class info, storing also buff/effect data.
             * 3. Remove all existing skills.
             * 4. Restore all the learned skills for the current class from the database.
             * 5. Restore effect/buff data for the new class.
             * 6. Restore henna data for the class, applying the new stat modifiers while removing existing ones.
             * 7. Reset HP/MP/CP stats and send Server->Client character status packet to reflect changes.
             * 8. Restore shortcut data related to this class.
             * 9. Resend a class change animation effect to broadcast to all nearby players.
             * 10.Unsummon any active servitor from the player.
             */

            if (getPet() instanceof L2SummonInstance)
            	getPet().unSummon(this);

            if (!getCubics().isEmpty())
            {
                for (L2CubicInstance cubic : getCubics().values())
                {
                    cubic.stopAction();
                    cubic.cancelDisappear();
                }

                getCubics().clear();
            }

            abortCast();

            for (L2Skill oldSkill : getAllSkills())
                super.removeSkill(oldSkill);

            stopAllEffects();

            restoreRecipeBook(false);

            // Restore any Death Penalty Buff
            restoreDeathPenaltyBuffLevel();

            restoreSkills();
            regiveTemporarySkills();
            rewardSkills();

            // Prevents some issues when changing between subclases that shares skills
            if(_disabledSkills != null && !_disabledSkills.isEmpty())
            	_disabledSkills.clear();

            restoreEffects();
            updateEffectIcons();

            // If player has quest "422: Repent Your Sins", remove it
            QuestState st = getQuestState("422_RepentYourSins");
            if (st != null)
            {
            	st.exitQuest(true);
            }

            for (int i = 0; i < 3; i++)
                _henna[i] = null;

            restoreHenna();
            sendPacket(new HennaInfo(this));

            checkItemRestriction();

            if (getCurrentHp() > getMaxHp())
            	getStatus().setCurrentHp(getMaxHp());
            if (getCurrentMp() > getMaxMp())
            	getStatus().setCurrentMp(getMaxMp());
            if (getCurrentCp() > getMaxCp())
            	getStatus().setCurrentCp(getMaxCp());

            getInventory().restoreEquipedItemsPassiveSkill();
            getInventory().restoreArmorSetPassiveSkill();

            refreshOverloaded();
    		refreshExpertisePenalty();
            broadcastUserInfo();

            // Clear resurrect xp calculation
            setExpBeforeDeath(0);

            //getMacroses().restore();
            //getMacroses().sendUpdate();

            getShortCuts().restore();
            sendPacket(new ShortCutInit(this));

            broadcastPacket(new SocialAction(getObjectId(), SocialAction.LEVEL_UP));
            sendPacket(new SkillCoolTime(this));
            sendPacket(new ExStorageMaxCount(this));

            broadcastClassIcon();

            return true;
    	}
    	finally
    	{
    		_subclassLock.unlock();
    	}
    }

    public boolean isLocked()
    {
    	return _subclassLock.isLocked();
    }

	public void broadcastClassIcon()
	{
		// Update class icon in party and clan
		if (isInParty())
			getParty().broadcastToPartyMembers(new PartySmallWindowUpdate(this));

		if (getClan() != null)
			getClan().broadcastToOnlineMembers(new PledgeShowMemberListUpdate(this));
	}

	public void stopWarnUserTakeBreak()
	{
		if (_taskWarnUserTakeBreak != null)
		{
			_taskWarnUserTakeBreak.cancel(false);
			_taskWarnUserTakeBreak = null;
		}
	}

	public void startWarnUserTakeBreak()
	{
		if (_taskWarnUserTakeBreak == null)
			_taskWarnUserTakeBreak = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new WarnUserTakeBreak(), 7200000, 7200000);
	}

	public void stopRentPet()
	{
		if (_taskRentPet != null)
		{
			// If the rent of a wyvern expires while over a flying zone, tp to down before unmounting
			if (getMountType() == 2 && !checkCanLand())
				teleToLocation(TeleportWhereType.Town);

			if (dismount()) // This should always be true now, since we teleported already
			{
				_taskRentPet.cancel(true);
				_taskRentPet = null;
			}
		}
	}

	public void startRentPet(int seconds)
	{
		if (_taskRentPet == null)
			_taskRentPet = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new RentPetTask(), seconds * 1000L, seconds * 1000);
	}

	public boolean isRentedPet()
	{
        return _taskRentPet != null;
    }

	public void stopWaterTask()
	{
		if (_taskWater != null)
		{
			_taskWater.cancel(false);

			_taskWater = null;
			sendPacket(new SetupGauge(SetupGauge.CYAN, 0));
			// Added to sync fall when swimming stops:
			// (e.g. catacombs players swim down and then they fell when they got out of the water).
			isFalling(false, 0);
		}

		broadcastUserInfo();
	}

	public void startWaterTask()
	{
		// Temp fix here
		if (isMounted())
			dismount();

		if (isTransformed() && !isCursedWeaponEquipped())
		{
			untransform();
		}
		// TODO: update to only send speed status when that packet is known
		else
			broadcastUserInfo();

		if (!isDead() && _taskWater == null)
		{
			int timeinwater = (int) calcStat(Stats.BREATH, 60000, this, null);

			sendPacket(new SetupGauge(2, timeinwater));
			_taskWater = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new WaterTask(), timeinwater, 1000);
		}
	}

	public boolean isInWater()
	{
        return _taskWater != null;
    }

	public void onPlayerEnter()
	{
		startWarnUserTakeBreak();
		startAutoSaveTask();

		if (SevenSigns.getInstance().isSealValidationPeriod() || SevenSigns.getInstance().isCompResultsPeriod())
		{
			if (!isGM() && isIn7sDungeon() && Config.ALT_STRICT_SEVENSIGNS
					&& SevenSigns.getInstance().getPlayerCabal(this) != SevenSigns.getInstance().getCabalHighestScore())
			{
				teleToLocation(TeleportWhereType.Town);
				setIsIn7sDungeon(false);
				sendPacket(SystemMessage.sendString("You have been teleported to the nearest town due to the beginning of the Seal Validation period."));
			}
		}
		else
		{
			if (!isGM() && isIn7sDungeon() && Config.ALT_STRICT_SEVENSIGNS && SevenSigns.getInstance().getPlayerCabal(this) == SevenSigns.CABAL_NULL)
			{
				teleToLocation(TeleportWhereType.Town);
				setIsIn7sDungeon(false);
				sendPacket(SystemMessage.sendString("You have been teleported to the nearest town because you have not signed for any cabal."));
			}
		}

		// Jail task
		updateJailState();

		if (_isInvul) // isInvul() is always true on login if login protection is activated...
			sendPacket(SystemMessage.sendString("Entering world in Invulnerable mode."));
		if (getAppearance().isInvisible())
			sendPacket(SystemMessage.sendString("Entering world in Invisible mode."));
		if (getMessageRefusal())
			sendPacket(SystemMessage.sendString("Entering world in Message Refusal mode."));

		revalidateZone(true);

		// [L2J_JP ADD SANDMAN] Check of a restart prohibition area.
		if (!isGM())
		{
			// Four-Sepulcher,It is less than 5 minutes.
			if (FourSepulchersManager.getInstance().checkIfInZone(this) && (System.currentTimeMillis() - getLastAccess() >= 300000))
			{
				int driftX = Rnd.get(-80, 80);
				int driftY = Rnd.get(-80, 80);
				teleToLocation(178293 + driftX, -84607 + driftY, -7216);
			}

			// It is less than a time limit from player restarting.
			// TODO write code for restart fight against bosses.
			// Lair of bosses,It is less than 30 minutes from server starting.
			// It is only for Antharas and Valakas that I know it now.
			// Thanks a lot Serafiel of L2J_JP.

			// 10 minutes
			// Antharas
			else if (AntharasManager.getInstance().checkIfInZone(this))
			{
				// Lair of bosses,It is less than 30 minutes from server starting.
				// Player can restart inside lair, but Antharas do not respawn.
				if (System.currentTimeMillis() - GameServer.getStartedTime().getTimeInMillis() <= Config.ALT_TIMELIMITOFINVADE)
				{
					if (getQuestState("antharas") != null)
						getQuestState("antharas").exitQuest(true);
				}
				else if (System.currentTimeMillis() - getLastAccess() >= 600000)
				{
					if (getQuestState("antharas") != null)
						getQuestState("antharas").exitQuest(true);
					teleToLocation(TeleportWhereType.Town);
				}
			}

			// Baium
			else if (BaiumManager.getInstance().checkIfInZone(this))
			{
				if (System.currentTimeMillis() - getLastAccess() >= 600000)
				{
					if (getQuestState("baium") != null)
						getQuestState("baium").exitQuest(true);
					teleToLocation(TeleportWhereType.Town);
				}
				else
				{
					// Player can restart inside lair, but can not awake Baium.
					if (getQuestState("baium") != null)
						getQuestState("baium").exitQuest(true);
				}
			}

			// 10 minutes
			// Last Imperial Tomb (includes Frintezza's room)
			else if (LastImperialTombManager.getInstance().checkIfInZone(this))
			{
				// Lair of bosses,It is less than 30 minutes from server starting.
				// Player can restart inside lair, but Antharas do not respawn.
				if (System.currentTimeMillis() - GameServer.getStartedTime().getTimeInMillis() <= Config.ALT_TIMELIMITOFINVADE)
				{
					if (getQuestState("lastimperialtomb") != null)
						getQuestState("lastimperialtomb").exitQuest(true);
				}
				else if (System.currentTimeMillis() - getLastAccess() >= 600000)
				{
					if (getQuestState("lastimperialtomb") != null)
						getQuestState("lastimperialtomb").exitQuest(true);
					teleToLocation(TeleportWhereType.Town);
				}
			}

			// 10 minutes
			// Not sure if Imperial tomb includes Frintezza's room
			// So just incase lets clear that room from intruders till all FrintezzaManager will work like retail
			else if (FrintezzaManager.getInstance().checkIfInZone(this))
			{
				if (System.currentTimeMillis() - getLastAccess() >= 600000 && !FrintezzaManager.getInstance().getState().equals(GrandBossState.StateEnum.ALIVE))
					teleToLocation(TeleportWhereType.Town);
			}

			// Lilith
			/*else if (LilithManager.getInstance().checkIfInZone(this))
			{
				if (System.currentTimeMillis() - getLastAccess() >= 600000)
					teleToLocation(TeleportWhereType.Town);
			}

			// Anakim
			else if (AnakimManager.getInstance().checkIfInZone(this))
			{
				if (System.currentTimeMillis() - getLastAccess() >= 600000)
					teleToLocation(TeleportWhereType.Town);
			}

			// Zaken
			else if (ZakenManager.getInstance().checkIfInZone(this))
			{
				if (System.currentTimeMillis() - getLastAccess() >= 600000)
					teleToLocation(TeleportWhereType.Town);
			}*/

			// High Priestess van Halter
			else if (VanHalterManager.getInstance().checkIfInZone(this))
			{
				if (System.currentTimeMillis() - getLastAccess() >= 600000)
					teleToLocation(TeleportWhereType.Town);
				else
					VanHalterManager.getInstance().intruderDetection(this);
			}

			// 30 minutes
			// Valakas
			else if (ValakasManager.getInstance().checkIfInZone(this))
			{
				// Lair of bosses,It is less than 30 minutes from server starting.
				// Player can restart inside lair, and begin fight against Valakas 30min later.
				if (System.currentTimeMillis() - GameServer.getStartedTime().getTimeInMillis() <= Config.ALT_TIMELIMITOFINVADE
						&& ValakasManager.getInstance().getState() == GrandBossState.StateEnum.ALIVE)
				{
				}
				else
				{
					if (getQuestState("valakas") != null)
						getQuestState("valakas").exitQuest(true);
					teleToLocation(TeleportWhereType.Town);
				}
			}
			// Baylor
			else if (BaylorManager.getInstance().checkIfInZone(this))
			{
				if (getQuestState("baylor") != null)
					getQuestState("baylor").exitQuest(true);
				teleToLocation(TeleportWhereType.Town);
			}
			// Sailren
			else if (SailrenManager.getInstance().checkIfInZone(this))
			{
				if (getQuestState("sailren") != null)
					getQuestState("sailren").exitQuest(true);
				teleToLocation(TeleportWhereType.Town);
			}
		}
	}

	public void checkWaterState()
	{
		if (isInsideZone(L2Zone.FLAG_WATER))
		{
			startWaterTask();
		}
		else
		{
			stopWaterTask();
		}
	}

	public long getLastAccess()
	{
		return _lastAccess;
	}

	public int getBoatId()
	{
		return _boatId;
	}

	public void setBoatId(int boatId)
	{
		_boatId = boatId;
	}

	@Override
	public void doRevive()
	{
		super.doRevive();
		stopEffects(L2EffectType.CHARMOFCOURAGE);
		updateEffectIcons();
		_reviveRequested = false;
		_revivePower = 0;
		
		if (isMounted())
			startFeed(_mountNpcId);
		
		if (isInParty() && getParty().isInDimensionalRift())
		{
			if (!DimensionalRiftManager.getInstance().checkIfInPeaceZone(getX(), getY(), getZ()))
				getParty().getDimensionalRift().memberRessurected(this);
		}
		
		GlobalRestrictions.playerRevived(this);
	}

	@Override
	public void doRevive(double revivePower)
	{
		// Restore the player's lost experience,
		// depending on the % return of the skill used (based on its power).
		restoreExp(revivePower);
		doRevive();
	}

	public void reviveRequest(L2PcInstance reviver, L2Skill skill)
	{
		if (_reviveRequested || _revivePetRequested)
		{
			reviver.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED); // Resurrection is already been proposed.
			return;
		}
		if (isDead())
		{
			_reviveRequested = true;
			if (isPhoenixBlessed())
				_revivePower = 100;
			else if (skill != null)
				_revivePower = Formulas.calculateSkillResurrectRestorePercent(skill.getPower(), reviver.getStat().getWIT());
			else
				_revivePower = 0;

			int restoreExp = (int) Math.round((getExpBeforeDeath() - getExp()) * _revivePower / 100);

			if (getCharmOfCourage())
			{
				ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.RESURRECT_USING_CHARM_OF_COURAGE);
				dlg.addTime(60000);
				sendPacket(dlg);
				return;
			}

			ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.RESSURECTION_REQUEST_BY_C1_FOR_S2_XP);
			sendPacket(dlg.addPcName(reviver).addString("" + restoreExp));
		}
	}

	public void revivePetRequest(L2PcInstance reviver, L2Skill skill)
	{
		if (_reviveRequested || _revivePetRequested)
		{
			reviver.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED); // Resurrection is already been proposed.
			return;
		}

		if (getPet().isDead() && getPet() instanceof L2PetInstance)
		{
			_revivePetRequested = true;
			if (skill != null)
				_revivePetPower = Formulas.calculateSkillResurrectRestorePercent(skill.getPower(), reviver.getStat().getWIT());
			else
				_revivePetPower = 0;

			int restoreExp = (int) Math.round((((L2PetInstance)getPet()).getExpBeforeDeath() - getPet().getStat().getExp()) * _revivePetPower / 100);

			ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.RESSURECTION_REQUEST_BY_C1_FOR_S2_XP.getId());
			sendPacket(dlg.addPcName(reviver).addString("" + restoreExp));
		}
	}

	public void reviveAnswer(int answer)
	{
		if (!((_reviveRequested && isDead()) || (_revivePetRequested && getPet() != null && getPet().isDead())))
			return;
		// If character refuses a PhoenixBless autoress, cancel all buffs he had
		if (answer == 0 && isPhoenixBlessed() && isDead() && _reviveRequested)
		{
			stopPhoenixBlessing(true);
			stopAllEffectsExceptThoseThatLastThroughDeath();
		}

		if (answer == 1)
		{
			if (_reviveRequested)
			{
				if (_revivePower != 0)
					doRevive(_revivePower);
				else
					doRevive();
			}
			else if (_revivePetRequested && getPet() != null)
			{
				if (_revivePower != 0)
					getPet().doRevive(_revivePower);
				else
					getPet().doRevive();
			}
		}
		_reviveRequested = false;
		_revivePower = 0;
	}

	public boolean isReviveRequested()
	{
		return _reviveRequested;
	}

	public boolean isPetReviveRequested()
	{
		return _revivePetRequested;
	}

	public void removeReviving()
	{
		_reviveRequested = false;
		_revivePower = 0;
	}

	public void removePetReviving()
	{
		_revivePetRequested = false;
		_revivePetPower = 0;
	}

	public void onActionRequest()
	{
		setProtection(false);
	}

	/**
	 * @param expertiseIndex The expertiseIndex to set.
	 */
	public void setExpertiseIndex(int expertiseIndex)
	{
		_expertiseIndex = expertiseIndex;
	}

	/**
	 * @return Returns the expertiseIndex.
	 */
	public int getExpertiseIndex()
	{
		return _expertiseIndex;
	}

	@Override
	public boolean onTeleported()
	{
		if (!super.onTeleported())
			return false;

		getKnownList().updateKnownObjects();

		setProtection(true);

		// Trained beast is after teleport lost
		if (getTrainedBeast() != null)
		{
			getTrainedBeast().decayMe();
			setTrainedBeast(null);
		}
		// Modify the position of the pet if necessary
		L2Summon pet = getPet();
		if (pet != null)
		{
			pet.setFollowStatus(false);
			getPet().teleToLocation(getPosition().getX(), getPosition().getY(), getPosition().getZ(), false);
			((L2SummonAI)getPet().getAI()).setStartFollowController(true);
			pet.setFollowStatus(true);
			getPet().broadcastFullInfoImpl(0);
		}

		sendPacket(new UserInfo(this));

		if (getParty() != null)
			getParty().broadcastToPartyMembers(this, new PartyMemberPosition(this));
		return true;
	}

	private Point3D getLastPartyPosition()
	{
		if (_lastPartyPosition == null)
			_lastPartyPosition =  new Point3D(0, 0, 0);

		return _lastPartyPosition;
	}

	public void setLastPartyPosition(int x, int y, int z)
	{
		getLastPartyPosition().setXYZ(x,y,z);
	}

	public int getLastPartyPositionDistance(int x, int y, int z)
	{
		double dx = (x - getLastPartyPosition().getX());
		double dy = (y - getLastPartyPosition().getY());
		double dz = (z - getLastPartyPosition().getZ());

		return (int) Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	public void setLastServerPosition(int x, int y, int z)
	{
		getLastServerPosition().setXYZ(x, y, z);
	}

	public Point3D getLastServerPosition()
	{
		if (_lastServerPosition == null)
			_lastServerPosition =  new Point3D(0, 0, 0);

		return _lastServerPosition;
	}

	public boolean checkLastServerPosition(int x, int y, int z)
	{
		return getLastServerPosition().equals(x, y, z);
	}

	public int getLastServerDistance(int x, int y, int z)
	{
		double dx = (x - getLastServerPosition().getX());
		double dy = (y - getLastServerPosition().getY());
		double dz = (z - getLastServerPosition().getZ());

		return (int) Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	@Override
	public void addExpAndSp(long addToExp, int addToSp)
	{
		getStat().addExpAndSp(addToExp, addToSp);
	}

	public void addVitExpAndSp(long addToExp, int addToSp, L2Npc target)
	{
		if (target == null)
			return;

		long addToExpVit = addToExp;
		int addToSpVit = addToSp;

		if (!target.isRaid())
		{
			switch (getVitalityLevel())
			{
			case 0:
				break;
			case 1:
				addToExpVit = (long)(addToExpVit * Config.RATE_VITALITY_LEVEL_1);
				addToSpVit = (int)(addToSpVit * Config.RATE_VITALITY_LEVEL_1);
				break;
			case 2:
				addToExpVit = (long)(addToExpVit * Config.RATE_VITALITY_LEVEL_2);
				addToSpVit = (int)(addToSpVit * Config.RATE_VITALITY_LEVEL_2);
				break;
			case 3:
				addToExpVit = (long)(addToExpVit * Config.RATE_VITALITY_LEVEL_3);
				addToSpVit = (int)(addToSpVit * Config.RATE_VITALITY_LEVEL_3);
				break;
			case 4:
				addToExpVit = (long)(addToExpVit * Config.RATE_VITALITY_LEVEL_4);
				addToSpVit = (int)(addToSpVit * Config.RATE_VITALITY_LEVEL_4);
			}
		}
		getStat().addExpAndSp(addToExpVit, addToSpVit);
	}

	public void removeExpAndSp(long removeExp, int removeSp)
	{
		getStat().removeExpAndSp(removeExp, removeSp);
	}
	
	public void removeExpAndSp(long removeExp, int removeSp, boolean sendMessage)
	{
		getStat().removeExpAndSp(removeExp, removeSp, sendMessage);
	}

	/**
	 * Function is used in the PLAYER, calls snoop for all GMs listening to this player speak.
	 *
	 * @param channel - msg channel of the snooped player
	 * @param name - name of snooped player
	 * @param text - the msg the snooped player sent/received
	 */
	public void broadcastSnoop(SystemChatChannelId channel, String name, String text)
	{
		if (_snoopers.length == 0)
			return;

		final Snoop sn = new Snoop(this, channel, name, text);

		for (L2PcInstance snooper : _snoopers)
			snooper.sendPacket(sn);
	}

	/**
	 * Adds a spy ^^ GM to the player listener.
	 *
	 * @param pci - GM char that listens to the conversation
	 */
	public void addSnooper(L2PcInstance snooper)
	{
		if (!ArrayUtils.contains(_snoopers, snooper))
			_snoopers = (L2PcInstance[])ArrayUtils.add(_snoopers, snooper);
	}

	public void removeSnooper(L2PcInstance snooper)
	{
		_snoopers = (L2PcInstance[])ArrayUtils.removeElement(_snoopers, snooper);
	}

	public void removeSnooped(L2PcInstance snooped)
	{
		_snoopedPlayers = (L2PcInstance[])ArrayUtils.removeElement(_snoopedPlayers, snooped);
	}

	/**
	 * Adds a player to the GM queue for being listened.
	 * @param pci - player we listen to
	 */
	public void addSnooped(L2PcInstance snooped)
	{
		if (!ArrayUtils.contains(_snoopedPlayers, snooped))
		{
			_snoopedPlayers = (L2PcInstance[])ArrayUtils.add(_snoopedPlayers, snooped);

			sendPacket(new Snoop(snooped, SystemChatChannelId.Chat_Normal, "", "*** Starting snooping of player " + snooped.getName() + " ***"));
		}
	}

	public synchronized void buildBypassCache(final StringBuilder builder)
	{
		if (_validBypass != null)
			_validBypass.clear();

		if (_validBypass2 != null)
			_validBypass2.clear();

		for (int i = 0; i < builder.length(); i++)
		{
			int start = builder.indexOf("bypass -h", i);
			int finish = builder.indexOf("\"", start);

			if (start < 0 || finish < 0)
				break;

			start += 10;
			i = start;
			int finish2 = builder.indexOf("$", start);

			if (0 < finish2 && finish2 < finish)
			{
				if (_validBypass2 == null)
					_validBypass2 = new ArrayList<String>();

				_validBypass2.add(builder.substring(start, finish2).trim());
			}
			else
			{
				if (_validBypass == null)
					_validBypass = new ArrayList<String>();

				_validBypass.add(builder.substring(start, finish).trim());
			}
		}
	}

	public synchronized void validateBypass(String cmd) throws InvalidPacketException
	{
		if (_validBypass != null)
			for (String bp : _validBypass)
				if (bp != null && cmd.equals(bp))
					return;

		if (_validBypass2 != null)
			for (String bp : _validBypass2)
				if (bp != null && cmd.startsWith(bp))
					return;

		throw new InvalidPacketException("[" + this + "] sent invalid bypass '" + cmd + "'!");
	}

	public boolean validateItemManipulation(int objectId, String action)
	{
		L2ItemInstance item = getInventory().getItemByObjectId(objectId);

		if (item == null || item.getOwnerId() != getObjectId())
		{
			_log.debug(getObjectId() + ": player tried to " + action + " item he is not owner of");
			return false;
		}

		// Pet is summoned and not the item that summoned the pet AND not the buggle from strider you're mounting
		if (getPet() != null && getPet().getControlItemId() == objectId || getMountObjectID() == objectId)
		{
			if (_log.isDebugEnabled())
				_log.debug(getObjectId() + ": player tried to " + action + " item controling pet");

			return false;
		}

		if (getActiveEnchantItem() != null && getActiveEnchantItem().getObjectId() == objectId)
		{
			if (_log.isDebugEnabled())
				_log.debug(getObjectId() + ":player tried to " + action + " an enchant scroll he was using");

			return false;
		}

		if (CursedWeaponsManager.getInstance().isCursed(item.getItemId()))
		{
			// Cannot trade a cursed weapon
			return false;
		}

        return !item.isWear();
    }

	/**
	 * @return Returns the inBoat.
	 */
	public boolean isInBoat()
	{
		return _inBoat;
	}

	/**
	 * @param inBoat The inBoat to set.
	 */
	public void setInBoat(boolean inBoat)
	{
		_inBoat = inBoat;
	}

	/**
	 * @return
	 */
	public L2BoatInstance getBoat()
	{
		return _boat;
	}

	/**
	 * @param boat
	 */
	public void setBoat(L2BoatInstance boat)
	{
		_boat = boat;
	}

	/**
	 * @return Returns the inAirShip.
	 */
	public boolean isInAirShip()
	{
		return _inAirShip;
	}

	/**
	 * @param inAirShip The inAirShip to set.
	 */
	public void setInAirShip(boolean inAirShip)
	{
		_inAirShip = inAirShip;
	}

	/**
	 * @return
	 */
	public L2AirShipInstance getAirShip()
	{
		return _airShip;
	}

	/**
	 * @param airShip
	 */
	public void setAirShip(L2AirShipInstance airShip)
	{
		if (airShip != null)
			setInAirShip(true);
		else
			setInAirShip(false);
		_airShip = airShip;
	}
	public void setInCrystallize(boolean inCrystallize)
	{
		_inCrystallize = inCrystallize;
	}

	public boolean isInCrystallize()
	{
		return _inCrystallize;
	}

	/**
	 * @return
	 */
	public Point3D getInBoatPosition()
	{
		return _inBoatPosition;
	}

	public void setInBoatPosition(Point3D pt)
	{
		_inBoatPosition = pt;
	}

	/**
	 * @return
	 */
	public Point3D getInAirShipPosition()
	{
		return _inAirShipPosition;
	}

	public void setInAirShipPosition(Point3D pt)
	{
		_inAirShipPosition = pt;
	}

	/**
	 * Manage the delete task of a L2PcInstance (Leave Party, Unsummon pet, Save its inventory in the database, Remove it from the world...).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>If the L2PcInstance is in observer mode, set its position to its position before entering in observer mode </li>
	 * <li>Set the online Flag to True or False and update the characters table of the database with online status and lastAccess </li>
	 * <li>Stop the HP/MP/CP Regeneration task </li>
	 * <li>Cancel Crafting, Attak or Cast </li>
	 * <li>Remove the L2PcInstance from the world </li>
	 * <li>Stop Party and Unsummon Pet </li>
	 * <li>Update database with items in its inventory and remove them from the world </li>
	 * <li>Remove all L2Object from _knownObjects and _knownPlayer of the L2Character then cancel Attak or Cast and notify AI </li>
	 * <li>Close the connection with the client </li><BR><BR>
	 *
	 */
	public void deleteMe()
	{
		final HashSet<L2Zone> before = getZonesPlayerIn();
		
		if (getOnlineState() == ONLINE_STATE_DELETED)
			return;

		// Pause restrictions
		ObjectRestrictions.getInstance().pauseTasks(getObjectId());

		abortCast();
		abortAttack();

		try
		{
			if (isFlying())
				removeSkill(SkillTable.getInstance().getInfo(4289, 1));
		}
		catch (Exception e)
		{
			_log.fatal(e.getMessage(), e);
		}

		try
		{
			L2ItemInstance flag = getInventory().getItemByItemId(9819);
			if (flag != null)
			{
				Fort fort = FortManager.getInstance().getFort(this);
				if (fort != null)
					FortSiegeManager.getInstance().dropCombatFlag(this);
				else
				{
					int slot = flag.getItem().getBodyPart();
					getInventory().unEquipItemInBodySlotAndRecord(slot);
					destroyItem("CombatFlag", flag, null, true);
				}
			}
		}
		catch (Exception e)
		{
			_log.fatal(e.getMessage(), e);
		}

		// If the L2PcInstance has Pet, unsummon it
		if (getPet() != null)
		{
			try
			{
				getPet().unSummon(this);
				// dead pet wasnt unsummoned, broadcast npcinfo changes (pet will be without owner name - means owner offline)
				if (getPet() != null)
					getPet().broadcastFullInfoImpl(0);
			}
			catch (Exception e)
			{
				_log.error(e.getMessage(), e);
			}// Return pet to the control item
		}

		// Cancel trade
		if (getActiveRequester() != null)
		{
			getActiveRequester().onTradeCancel(this);
			onTradeCancel(getActiveRequester());

			cancelActiveTrade();

			setActiveRequester(null);
		}

		// Check if the L2PcInstance is in observer mode to set its position to its position before entering in observer mode
		if (inObserverMode())
			getPosition().setXYZ(_obsX, _obsY, _obsZ);
		else if (isInAirShip())
			getAirShip().oustPlayer(this);

		Castle castle = null;
		if (getClan() != null) {
			castle = CastleManager.getInstance().getCastleByOwner(getClan());
			if (castle != null)
				castle.destroyClanGate();
		}

		// Set the online Flag to True or False and update the characters table of the database with online status and lastAccess (called when login and logout)
		try
		{
			setOnlineStatus(false);
		}
		catch (Exception e)
		{
			_log.fatal(e.getMessage(), e);
		}

		// Stop the HP/MP/CP Regeneration task (scheduled tasks)
		try
		{
			stopAllTimers();
		}
		catch (Exception e)
		{
			_log.fatal(e.getMessage(), e);
		}

		GlobalRestrictions.playerDisconnected(this);

		// Stop crafting, if in progress
		try
		{
			RecipeController.getInstance().requestMakeItemAbort(this);
		}
		catch (Exception e)
		{
			_log.fatal(e.getMessage(), e);
		}

		try
		{
			setTarget(null);
		}
		catch (Exception e)
		{
			_log.fatal(e.getMessage(), e);
		}

		if (_throne != null)
			_throne.setOccupier(null);
		_throne = null;

		try
		{
			if (_fusionSkill != null)
			{
				abortCast();
			}
			for (L2Character character : getKnownList().getKnownCharacters())
				if (character.getFusionSkill() != null && character.getFusionSkill().getTarget() == this)
					character.abortCast();
		}
		catch (Exception e)
		{
			_log.fatal(e.getMessage(), e);
		}

		getEffects().stopAllEffects(true);

		// Remove from world regions zones
		L2WorldRegion oldRegion = getWorldRegion();

		// Remove the L2PcInstance from the world
		if (isVisible())
		{
			try
			{
				decayMe();
			}
			catch (Exception e)
			{
				_log.fatal(e.getMessage(), e);
			}
		}

		if (oldRegion != null)
			oldRegion.removeFromZones(this);

		// If a Party is in progress, leave it (and festival party)
		if (isInParty())
		{
			try
			{
				// If player is festival participant and it is in progress
				// notify party members that the player is not longer a participant.
				if (isFestivalParticipant() && SevenSignsFestival.getInstance().isFestivalInitialized())
				{
					getParty().broadcastToPartyMembers(SystemMessage.sendString(getName() + " has been removed from the upcoming festival."));
				}

				leaveParty();
			}
			catch (Exception e)
			{
				_log.fatal(e.getMessage(), e);
			}
		}

		if (getClanId() != 0 && getClan() != null)
		{
			// Set the status for pledge member list to OFFLINE
			try
			{
				L2ClanMember clanMember = getClan().getClanMember(getName());
				if (clanMember != null)
					clanMember.setPlayerInstance(null);
			}
			catch (Exception e)
			{
				_log.fatal(e.getMessage(), e);
			}
		}

		if (getActiveRequester() != null)
		{
			// Deals with sudden exit in the middle of transaction
			setActiveRequester(null);
		}

		// If the L2PcInstance is a GM, remove it from the GM List
		if (isGM())
		{
			try
			{
				GmListTable.deleteGm(this);
			}
			catch (Exception e)
			{
				_log.fatal(e.getMessage(), e);
			}
		}

		// Update database with items in its inventory and remove them from the world
		try
		{
			getInventory().deleteMe();
		}
		catch (Exception e)
		{
			_log.fatal(e.getMessage(), e);
		}

		// Update database with items in its warehouse and remove them from the world
		try
		{
			clearWarehouse();
		}
		catch (Exception e)
		{
			_log.fatal(e.getMessage(), e);
		}

		if (Config.WAREHOUSE_CACHE)
			WarehouseCacheManager.getInstance().remove(this);

		// Update database with items in its freight and remove them from the world
		try
		{
			clearFreight();
		}
		catch (Exception e)
		{
			_log.fatal(e.getMessage(), e);
		}

		try
		{
			clearDepositedFreight();
		}
		catch (Exception e)
		{
			_log.fatal(e.getMessage(), e);
		}

		// Remove all L2Object from _knownObjects and _knownPlayer of the L2Character then cancel Attak or Cast and notify AI
		try
		{
			getKnownList().removeAllKnownObjects();
		}
		catch (Exception e)
		{
			_log.fatal(e.getMessage(), e);
		}

		untransform();

		if (getClanId() > 0)
			getClan().broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(this), this);

		if (_snoopedPlayers.length > 0)
		{
			for (L2PcInstance snooped : _snoopedPlayers)
				snooped.removeSnooper(this);
			_snoopedPlayers = L2PcInstance.EMPTY_ARRAY;
		}

		if (_snoopers.length > 0)
		{
			broadcastSnoop(SystemChatChannelId.Chat_Normal, "", "*** Player " + getName() + " logged off ***");
			for (L2PcInstance snooper : _snoopers)
				snooper.removeSnooped(this);
			_snoopers = L2PcInstance.EMPTY_ARRAY;
		}

		for (Integer objId : getFriendList().getFriendIds())
		{
			L2PcInstance friend = L2World.getInstance().findPlayer(objId);
			if (friend != null)
			{
				friend.sendPacket(new FriendList(friend));
				friend.sendMessage("Friend: " + getName() + " has logged off.");
			}
		}

		// Remove L2Object object from _allObjects of L2World, if still in it
		L2World.getInstance().removeObject(this);

		try
		{
			// To delete the player from L2World on crit during teleport ;)
			setIsTeleporting(false);

			L2World.getInstance().removeOnlinePlayer(this);
		}
		catch (RuntimeException e)
		{
			_log.fatal( "deleteMe()", e);
		}

		RegionBBSManager.changeCommunityBoard(this, PlayerStateOnCommunity.NONE);

		//getClearableReference().clear();
		LeakTaskManager.getInstance().add(this);

		SQLQueue.getInstance().run();
		
		final HashSet<L2Zone> after = getZonesPlayerIn();
		
		if (!after.isEmpty())
		{
			_log.warn("Leaking zones before L2PcInstance.deleteMe(): " + before.toString());
			_log.warn("Leaking zones after L2PcInstance.deleteMe(): " + after.toString());
		}
	}
	
	private HashSet<L2Zone> getZonesPlayerIn()
	{
		final HashSet<L2Zone> set = new HashSet<L2Zone>();
		
		for (Map<String, L2Zone> zones : ZoneManager.getInstance().getZones())
			if (zones != null)
				for (L2Zone zone : zones.values())
					if (zone.getCharactersInside().contains(this))
						set.add(zone);
		
		return set;
	}

	public boolean canLogout()
	{
		if (!isGM() || !Config.GM_RESTART_FIGHTING)
		{
			if (AttackStanceTaskManager.getInstance().getAttackStanceTask(this))
			{
				sendPacket(SystemMessageId.CANT_LOGOUT_WHILE_FIGHTING);
				return false;
			}
		}

		if (isFlying())
		{
			sendMessage("You can't log out while flying.");
			return false;
		}

		L2Summon summon = getPet();

		if (summon != null && summon instanceof L2PetInstance && !summon.isBetrayed() && summon.isAttackingNow())
		{
			sendPacket(SystemMessageId.PET_CANNOT_SENT_BACK_DURING_BATTLE);
			return false;
		}

		if (isInFunEvent())
		{
			sendMessage("A superior power doesn't allow you to leave the event.");
			return false;
		}

		if (isInOlympiadMode() || Olympiad.getInstance().isRegistered(this) || getOlympiadGameId() != -1)
		{
			sendMessage("You can't logout while you are at Olympiad.");
			return false;
		}

		// Prevent player from restarting if they are a festival participant
		if (isFestivalParticipant() /* && SevenSignsFestival.getInstance().isFestivalInitialized()*/)
		{
			sendMessage("You can't logout while you are a participant in a festival.");
			return false;
		}

		if (getPrivateStoreType() != 0)
		{
			sendMessage("You can't logout while trading.");
			return false;
		}

		if (getActiveEnchantItem() != null)
		{
			sendMessage("You can't logout while enchanting.");
			return false;
		}
		if (getActiveEnchantAttrItem() != null)
		{
			sendMessage("You can't logout while enchanting.");
			return false;
		}

		if (isLocked())
		{
			sendMessage("You can't logout while changing class.");
			return false;
		}

		if (!isGM())
		{
			if (isInsideZone(L2Zone.FLAG_NOESCAPE))
			{
				sendPacket(SystemMessageId.NO_LOGOUT_HERE);
				teleToLocation(TeleportWhereType.Town);
			}
		}

		return true;
	}

	private FishData	_fish;

	public void startFishing(int x, int y, int z)
	{
		_fishx = x;
		_fishy = y;
		_fishz = z;

		stopMove(null);
		setIsImmobilized(true);
		_fishing = true;
		broadcastUserInfo();
		// Start Fishing
		int lvl = getRandomFishLvl();
		int group = getRandomGroup();
		int type = getRandomFishType(group);
		List<FishData> fishs = FishTable.getInstance().getFish(lvl, type, group);
		if (fishs == null || fishs.size() == 0)
		{
			endFishing(false);
			return;
		}
		int check = Rnd.get(fishs.size());
		_fish = fishs.get(check);
		fishs.clear();
		fishs = null;
		sendPacket(SystemMessageId.CAST_LINE_AND_START_FISHING);
		ExFishingStart efs = null;
		efs = new ExFishingStart(this, _fish.getType(), x, y, z, _lure.isNightLure());
		broadcastPacket(efs);
		startLookingForFishTask();
	}

	public void stopLookingForFishTask()
	{
		if (_taskforfish != null)
		{
			_taskforfish.cancel(false);
			_taskforfish = null;
		}
	}

	public void startLookingForFishTask()
	{
		if (!isDead() && _taskforfish == null)
		{
			int checkDelay = 0;
			boolean isNoob = false;
			boolean isUpperGrade = false;

			if (_lure != null)
			{
				int lureid = _lure.getItemId();
				isNoob = _fish.getGroup() == 0;
				isUpperGrade = _fish.getGroup() == 2;
				if (lureid == 6519 || lureid == 6522 || lureid == 6525 || lureid == 8505 || lureid == 8508 || lureid == 8511) // Low Grade
					checkDelay = Math.round((float) (_fish.getGutsCheckTime() * (1.33)));
				else if (lureid == 6520 || lureid == 6523 || lureid == 6526 || (lureid >= 8505 && lureid <= 8513) || (lureid >= 7610 && lureid <= 7613)
						|| (lureid >= 7807 && lureid <= 7809) || (lureid >= 8484 && lureid <= 8486)) // Medium Grade, Beginner, Prize-Winning & Quest Special Bait
					checkDelay = Math.round((float) (_fish.getGutsCheckTime() * (1.00)));
				else if (lureid == 6521 || lureid == 6524 || lureid == 6527 || lureid == 8507 || lureid == 8510 || lureid == 8513) // High grade
					checkDelay = Math.round((float) (_fish.getGutsCheckTime() * (0.66)));
			}
			_taskforfish = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(
					new LookingForFishTask(_fish.getWaitTime(), _fish.getFishGuts(), _fish.getType(), isNoob, isUpperGrade), 10000, checkDelay);
		}
	}

	private int getRandomGroup()
	{
		switch (_lure.getItemId())
		{
		case 7807: // Green for Beginners
		case 7808: // Purple for Beginners
		case 7809: // Yellow for Beginners
		case 8486: // Prize-Winning for Beginners
			return 0;
		case 8485: // Prize-Winning Luminous
		case 8506: // Green Luminous
		case 8509: // Purple Luminous
		case 8512: // Yellow Luminous
			return 2;
		default:
			return 1;
		}
	}

	private int getRandomFishType(int group)
	{
		int check = Rnd.get(100);
		int type = 1;
		switch (group)
		{
		case 0: // Fish for Novices
			switch (_lure.getItemId())
			{
			case 7807: // Green Lure, preferred by fast-moving (nimble) fish (type 5)
				if (check <= 54)
					type = 5;
				else if (check <= 77)
					type = 4;
				else
					type = 6;
				break;
			case 7808: // Purple Lure, preferred by fat fish (type 4)
				if (check <= 54)
					type = 4;
				else if (check <= 77)
					type = 6;
				else
					type = 5;
				break;
			case 7809: // Yellow Lure, preferred by ugly fish (type 6)
				if (check <= 54)
					type = 6;
				else if (check <= 77)
					type = 5;
				else
					type = 4;
				break;
			case 8486: // Prize-Winning Fishing Lure for Beginners
				if (check <= 33)
					type = 4;
				else if (check <= 66)
					type = 5;
				else
					type = 6;
				break;
			}
			break;
		case 1: // Normal Fish
			switch (_lure.getItemId())
			{
			case 7610:
			case 7611:
			case 7612:
			case 7613:
				type = 3;
				break;
			case 6519: // All theese lures (green) are prefered by fast-moving (nimble) fish (type 1)
			case 8505:
			case 6520:
			case 6521:
			case 8507:
				if (check <= 54)
					type = 1;
				else if (check <= 74)
					type = 0;
				else if (check <= 94)
					type = 2;
				else
					type = 3;
				break;
			case 6522: // All theese lures (purple) are prefered by fat fish (type 0)
			case 8508:
			case 6523:
			case 6524:
			case 8510:
				if (check <= 54)
					type = 0;
				else if (check <= 74)
					type = 1;
				else if (check <= 94)
					type = 2;
				else
					type = 3;
				break;
			case 6525: // All theese lures (yellow) are prefered by ugly fish (type 2)
			case 8511:
			case 6526:
			case 6527:
			case 8513:
				if (check <= 55)
					type = 2;
				else if (check <= 74)
					type = 1;
				else if (check <= 94)
					type = 0;
				else
					type = 3;
				break;
			case 8484: // Prize-Winning Fishing Lure
				if (check <= 33)
					type = 0;
				else if (check <= 66)
					type = 1;
				else
					type = 2;
				break;
			}
			break;
		case 2: // Upper Grade Fish, Luminous Lure
			switch (_lure.getItemId())
			{
			case 8506: // Green Lure, preferred by fast-moving (nimble) fish (type 8)
				if (check <= 54)
					type = 8;
				else if (check <= 77)
					type = 7;
				else
					type = 9;
				break;
			case 8509: // Purple Lure, preferred by fat fish (type 7)
				if (check <= 54)
					type = 7;
				else if (check <= 77)
					type = 9;
				else
					type = 8;
				break;
			case 8512: // Yellow Lure, preferred by ugly fish (type 9)
				if (check <= 54)
					type = 9;
				else if (check <= 77)
					type = 8;
				else
					type = 7;
				break;
			case 8485: // Prize-Winning Fishing Lure
				if (check <= 33)
					type = 7;
				else if (check <= 66)
					type = 8;
				else
					type = 9;
				break;
			}
		}
		return type;
	}

	private int getRandomFishLvl()
	{
		L2Effect[] effects = getAllEffects();
		int skilllvl = getSkillLevel(1315);
		for (L2Effect e : effects)
		{
			if (e.getSkill().getId() == 2274)
				skilllvl = (int) e.getSkill().getPower();
		}
		if (skilllvl <= 0)
			return 1;
		int randomlvl;
		int check = Rnd.get(100);

		if (check <= 50)
		{
			randomlvl = skilllvl;
		}
		else if (check <= 85)
		{
			randomlvl = skilllvl - 1;
			if (randomlvl <= 0)
			{
				randomlvl = 1;
			}
		}
		else
		{
			randomlvl = skilllvl + 1;
			if (randomlvl > 27)
				randomlvl = 27;
		}

		return randomlvl;
	}

	public void startFishCombat(boolean isNoob, boolean isUpperGrade)
	{
		_fishCombat = new L2Fishing(this, _fish, isNoob, isUpperGrade);
	}

	public void endFishing(boolean win)
	{
		ExFishingEnd efe = new ExFishingEnd(win, this);
		broadcastPacket(efe);
		_fishing = false;
		_fishx = 0;
		_fishy = 0;
		_fishz = 0;
		broadcastUserInfo();
		if (_fishCombat == null)
			sendPacket(SystemMessageId.BAIT_LOST_FISH_GOT_AWAY);
		_fishCombat = null;

		_lure = null;
		// End Fishing
		sendPacket(SystemMessageId.REEL_LINE_AND_STOP_FISHING);
		setIsImmobilized(false);
		stopLookingForFishTask();
	}

	public L2Fishing getFishCombat()
	{
		return _fishCombat;
	}

	public int getFishx()
	{
		return _fishx;
	}

	public int getFishy()
	{
		return _fishy;
	}

	public int getFishz()
	{
		return _fishz;
	}

	public void setLure(L2ItemInstance lure)
	{
		_lure = lure;
	}

	public L2ItemInstance getLure()
	{
		return _lure;
	}

	public int getInventoryLimit()
	{
		int ivlim;
		if (isGM())
		{
			ivlim = Config.INVENTORY_MAXIMUM_GM;
		}
		else if (getRace() == Race.Dwarf)
		{
			ivlim = Config.INVENTORY_MAXIMUM_DWARF;
		}
		else
		{
			ivlim = Config.INVENTORY_MAXIMUM_NO_DWARF;
		}
		ivlim += (int) getStat().calcStat(Stats.INV_LIM, 0, null, null);

		return ivlim;
	}

	public int getWareHouseLimit()
	{
		int whlim;
		if (getRace() == Race.Dwarf)
		{
			whlim = Config.WAREHOUSE_SLOTS_DWARF;
		}
		else
		{
			whlim = Config.WAREHOUSE_SLOTS_NO_DWARF;
		}
		whlim += (int) getStat().calcStat(Stats.WH_LIM, 0, null, null);

		return whlim;
	}

	public int getPrivateSellStoreLimit()
	{
		int pslim;
		if (getRace() == Race.Dwarf)
		{
			pslim = Config.MAX_PVTSTORESELL_SLOTS_DWARF;
		}
		else
		{
			pslim = Config.MAX_PVTSTORESELL_SLOTS_OTHER;
		}
		pslim += (int) getStat().calcStat(Stats.P_SELL_LIM, 0, null, null);

		return pslim;
	}

	public int getPrivateBuyStoreLimit()
	{
		int pblim;
		if (getRace() == Race.Dwarf)
		{
			pblim = Config.MAX_PVTSTOREBUY_SLOTS_DWARF;
		}
		else
		{
			pblim = Config.MAX_PVTSTOREBUY_SLOTS_OTHER;
		}
		pblim += (int) getStat().calcStat(Stats.P_BUY_LIM, 0, null, null);

		return pblim;
	}

	public int getFreightLimit()
	{
		return Config.FREIGHT_SLOTS + (int) getStat().calcStat(Stats.FREIGHT_LIM, 0, null, null);
	}

	public int getDwarfRecipeLimit()
	{
		int recdlim = Config.ALT_DWARF_RECIPE_LIMIT;
		recdlim += (int) getStat().calcStat(Stats.REC_D_LIM, 0, null, null);
		return recdlim;
	}

	public int getCommonRecipeLimit()
	{
		int recclim = Config.ALT_COMMON_RECIPE_LIMIT;
		recclim += (int) getStat().calcStat(Stats.REC_C_LIM, 0, null, null);
		return recclim;
	}

	/**
	 * @return Returns the mountNpcId.
	 */
	public int getMountNpcId()
	{
		return _mountNpcId;
	}

    /**
     * @return Returns the mountLevel.
     */
    public int getMountLevel()
    {
    	return _mountLevel;
    }

	public void setMountObjectID(int newID)
	{
		_mountObjectID = newID;
	}

	public int getMountObjectID()
	{
		return _mountObjectID;
	}

	private L2ItemInstance	_lure	= null;

	/**
	 * Get the current skill in use or return null.<BR><BR>
	 *
	 */
	public SkillDat getCurrentSkill()
	{
		return _currentSkill;
	}

	/**
	 * Create a new SkillDat object and set the player _currentSkill.<BR><BR>
	 *
	 */
	public void setCurrentSkill(L2Skill currentSkill, boolean ctrlPressed, boolean shiftPressed)
	{
		if (currentSkill == null)
		{
			if (_log.isDebugEnabled())
				_log.info("Setting current skill: NULL for " + getName() + ".");

			_currentSkill = null;
			return;
		}

		if (_log.isDebugEnabled())
			_log.info("Setting current skill: " + currentSkill.getName() + " (ID: " + currentSkill.getId() + ") for " + getName() + ".");

		_currentSkill = new SkillDat(currentSkill, ctrlPressed, shiftPressed);
	}

	/**
	 * Get the current pet skill in use or return null.<BR><BR>
	 *
	 */
	public SkillDat getCurrentPetSkill()
	{
		return _currentPetSkill;
	}
	/**
	 * Create a new SkillDat object and set the player _currentPetSkill.<BR><BR>
	 *
	 */
	public void setCurrentPetSkill(L2Skill currentSkill, boolean ctrlPressed, boolean shiftPressed)
	{
		if (currentSkill == null)
		{
			if (_log.isDebugEnabled())
				_log.info("Setting current skill: NULL for " + getName() + ".");

			_currentPetSkill = null;
			return;
		}

		if (_log.isDebugEnabled())
			_log.info("Setting current skill: " + currentSkill.getName() + " (ID: " + currentSkill.getId() + ") for " + getName() + ".");

		_currentPetSkill = new SkillDat(currentSkill, ctrlPressed, shiftPressed);
	}

	public SkillDat getQueuedSkill()
	{
		return _queuedSkill;
	}

	/**
	 * Create a new SkillDat object and queue it in the player _queuedSkill.<BR><BR>
	 *
	 */
	public void setQueuedSkill(L2Skill queuedSkill, boolean ctrlPressed, boolean shiftPressed)
	{
		if (queuedSkill == null)
		{
			if (_log.isDebugEnabled())
				_log.info("Setting queued skill: NULL for " + getName() + ".");

			_queuedSkill = null;
			return;
		}

		if (_log.isDebugEnabled())
			_log.info("Setting queued skill: " + queuedSkill.getName() + " (ID: " + queuedSkill.getId() + ") for " + getName() + ".");

		_queuedSkill = new SkillDat(queuedSkill, ctrlPressed, shiftPressed);
	}

	private long	_skillQueueProtectionTime	= 0;

	public void setSkillQueueProtectionTime(long time)
	{
		_skillQueueProtectionTime = time;
	}

	public long getSkillQueueProtectionTime()
	{
		return _skillQueueProtectionTime;
	}

	public boolean isMaried()
	{
		return _maried;
	}

	public void setMaried(boolean state)
	{
		_maried = state;
	}

	public boolean isEngageRequest()
	{
		return _engagerequest;
	}

	public void setEngageRequest(boolean state, int playerid)
	{
		_engagerequest = state;
		_engageid = playerid;
	}

	public void setMaryRequest(boolean state)
	{
		_maryrequest = state;
	}

	public boolean isMary()
	{
		return _maryrequest;
	}

	public void setMaryAccepted(boolean state)
	{
		_maryaccepted = state;
	}

	public boolean isMaryAccepted()
	{
		return _maryaccepted;
	}

	public int getEngageId()
	{
		return _engageid;
	}

	public int getPartnerId()
	{
		return _partnerId;
	}

	public void setPartnerId(int partnerid)
	{
		_partnerId = partnerid;
	}

	public int getCoupleId()
	{
		return _coupleId;
	}

	public void setCoupleId(int coupleId)
	{
		_coupleId = coupleId;
	}

	public void engageAnswer(int answer)
	{
		if (!_engagerequest)
			return;
		else if (_engageid == 0)
			return;
		else
		{
			L2Object obj = getKnownList().getKnownObject(_engageid);
			setEngageRequest(false, 0);
			if (obj instanceof L2PcInstance)
			{
				L2PcInstance ptarget = (L2PcInstance) obj;
				if (answer == 1)
				{
					CoupleManager.getInstance().createCouple(ptarget, L2PcInstance.this);
					ptarget.sendMessage("Engage accepted.");
				}
				else
					ptarget.sendMessage("Engage declined.");
			}
			else
			{
				sendMessage("Too far away.");
			}
		}
	}

	public void setClientRevision(int clientrev)
	{
		_clientRevision = clientrev;
	}

	public int getClientRevision()
	{
		return _clientRevision;
	}

	public boolean isInJail()
	{
		return _inJail;
	}

	public void setInJail(boolean state)
	{
		//setInJail(state, 0);
		_inJail = state;
	}

	public void setInJail(boolean state, int delayInMinutes)
	{
		_inJail = state;
		// Remove the task if any
		stopJailTask(false);

		if (_inJail)
		{
			if (delayInMinutes > 0)
			{
				_jailTimer = delayInMinutes * 60000L; // In millisec

				// Start the countdown
				_jailTask = ThreadPoolManager.getInstance().scheduleGeneral(new JailTask(), _jailTimer);
				sendMessage("You are in jail for " + delayInMinutes + " minutes.");
			}

			// Open a Html message to inform the player
			NpcHtmlMessage htmlMsg = new NpcHtmlMessage(0);
			String jailInfos = HtmCache.getInstance().getHtm("data/html/jail_in.htm");
			if (jailInfos != null)
				htmlMsg.setHtml(jailInfos);
			else
				htmlMsg.setHtml("<html><body>You have been put in jail by an admin.</body></html>");
			sendPacket(htmlMsg);

			if (isFlyingMounted())
				untransform();
			setInstanceId(0);
			setIsIn7sDungeon(false);
			
			teleToLocation(-114356, -249645, -2984, false); // Jail
		}
		else
		{
			// Open a Html message to inform the player
			NpcHtmlMessage htmlMsg = new NpcHtmlMessage(0);
			String jailInfos = HtmCache.getInstance().getHtm("data/html/jail_out.htm");
			if (jailInfos != null)
				htmlMsg.setHtml(jailInfos);
			else
				htmlMsg.setHtml("<html><body>You are free for now, respect server rules!</body></html>");
			sendPacket(htmlMsg);

			teleToLocation(17836, 170178, -3507); // Floran
		}

		// Store in database
		storeCharBase();

		RegionBBSManager.changeCommunityBoard(this, PlayerStateOnCommunity.IN_JAIL);
	}

	public long getJailTimer()
	{
		return _jailTimer;
	}

	public void setJailTimer(long time)
	{
		_jailTimer = time;
	}

	private void updateJailState()
	{
		if (isInJail())
		{
			// If jail time is elapsed, free the player
			if (_jailTimer > 0)
			{
				// Restart the countdown
				_jailTask = ThreadPoolManager.getInstance().scheduleGeneral(new JailTask(), _jailTimer);
				sendMessage("You are still in jail for " + Math.round(_jailTimer / 60000) + " minutes.");
			}

			// If player escaped, put him back in jail
			if (!isInsideZone(L2Zone.FLAG_JAIL))
				teleToLocation(L2JailZone.JAIL_LOCATION, false);
		}
	}

	public void stopJailTask(boolean save)
	{
		if (_jailTask != null)
		{
			if (save)
			{
				long delay = _jailTask.getDelay(TimeUnit.MILLISECONDS);
				if (delay < 0)
					delay = 0;
				setJailTimer(delay);
			}
			_jailTask.cancel(false);
			_jailTask = null;
		}
	}

	/**
	 * Return True if the L2PcInstance is a ViP.<BR><BR>
	 */
	public boolean isCharViP()
	{
		return _charViP;
	}

	/**
	 * Set the _charViP Flag of the L2PcInstance.<BR><BR>
	 */
	public void setCharViP(boolean status)
	{
		_charViP = status;
	}

	private ScheduledFuture<?>	_jailTask;
	private int					_cursedWeaponEquippedId	= 0;
	private boolean				_combatFlagEquipped		= false;

	private boolean				_reviveRequested		= false;
	private double				_revivePower			= 0;
	private boolean				_revivePetRequested		= false;
	private double				_revivePetPower			= 0;

	private double				_cpUpdateIncCheck		= .0;
	private double				_cpUpdateDecCheck		= .0;
	private double				_cpUpdateInterval		= .0;
	private double				_mpUpdateIncCheck		= .0;
	private double				_mpUpdateDecCheck		= .0;
	private double				_mpUpdateInterval		= .0;

	private class JailTask implements Runnable
	{
		public void run()
		{
			setInJail(false, 0);
		}
	}

	public void restoreHPMP()
	{
		getStatus().setCurrentHpMp(getMaxHp(), getMaxMp());
	}

	public boolean isCursedWeaponEquipped()
	{
		return _cursedWeaponEquippedId != 0;
	}

	public void setCursedWeaponEquippedId(int value)
	{
		_cursedWeaponEquippedId = value;

		RegionBBSManager.changeCommunityBoard(this, PlayerStateOnCommunity.CURSED_WEAPON_OWNER);
	}

	public int getCursedWeaponEquippedId()
	{
		return _cursedWeaponEquippedId;
	}

	public boolean isCombatFlagEquipped()
	{
		return _combatFlagEquipped;
	}

	public void setCombatFlagEquipped(boolean value)
	{
		_combatFlagEquipped = value;
	}

	public void setNPCFaction(FactionMember fm)
	{
		_faction = fm;
	}

	public FactionMember getNPCFaction()
	{
		return _faction;
	}

	public boolean removeNPCFactionPoints(int factionPoints)
	{
		if (_faction != null)
		{
			if (_faction.getFactionPoints() < factionPoints)
				return false;
			_faction.reduceFactionPoints(factionPoints);
			return true;
		}
		return false;
	}

	public int getNPCFactionPoints()
	{
		return _faction.getFactionPoints();
	}

	public int getSide()
	{
		return _faction.getSide();
	}

	public void quitNPCFaction()
	{
		if (_faction != null)
		{
			_faction.quitFaction();
			_faction = null;
		}
	}

	public boolean getCharmOfCourage()
	{
		return _charmOfCourage;
	}

	public void setCharmOfCourage(boolean val)
	{
		_charmOfCourage = val;
		sendEtcStatusUpdate();
	}

	/** Return True if the L2Character is riding. */
	public final boolean isRidingStrider()
	{
		return _isRidingStrider;
	}

	public final boolean isRidingRedStrider()
	{
		return _isRidingRedStrider;
	}

	public final boolean isRidingHorse()
	{
		return _isRidingHorse;
	}

	/** Set the L2Character riding mode to True. */
	public final void setIsRidingStrider(boolean mode)
	{
		_isRidingStrider = mode;
	}

	public final void setIsRidingRedStrider(boolean mode)
	{
		_isRidingRedStrider = mode;
	}

	public final void setIsRidingHorse(boolean mode)
	{
		_isRidingHorse = mode;
	}

	public int getCharges()
	{
		return _charges;
	}

	private static final int[] CHARGE_SKILLS = {570, 8, 50}; // Transformation skill is checked first

	public L2Skill getChargeSkill()
	{
		for (int id : L2PcInstance.CHARGE_SKILLS)
		{
			L2Skill skill = getKnownSkill(id);
			if (skill != null && skill.getMaxCharges() > 0)
			{
				return skill;
			}
		}
		return null;
	}

	public void increaseCharges(int count, int max)
	{
		if (count <= 0) // Wrong usage
			return;

		// Checking charges maximum
		if (_charges >= max)
		{
			sendPacket(SystemMessageId.FORCE_MAXLEVEL_REACHED);
			return;
		}

		// Increase charges
		setCharges(Math.min(_charges + count, max));

		SystemMessage sm = new SystemMessage(SystemMessageId.FORCE_INCREASED_TO_S1);
		sm.addNumber(_charges);
		sendPacket(sm);
	}

	public void increaseChargesBySkill(L2Skill skill)
	{
		if (skill.getGiveCharges() > 0)
			increaseCharges(skill.getGiveCharges(), skill.getMaxCharges());
	}

	private void setCharges(int charges)
	{
		_charges = Math.max(0, charges);

		sendEtcStatusUpdate();
		if (_charges == 0)
			stopChargeTask();
		else
			restartChargeTask();
	}

	public void decreaseCharges(int count)
	{
		if (count < 0) // Wrong usage
			return;
		setCharges(_charges - count);
	}

	public class ChargeTask implements Runnable
	{
		public void run()
		{
			clearCharges();
		}
	}

	/**
	* Clear out all charges from this L2PcInstance
	*/
	public void clearCharges()
	{
		setCharges(0);
	}

	/**
	* Starts/Restarts the SoulTask to Clear Charges after 10 Mins.
	*/
	private void restartChargeTask()
	{
		if (_chargeTask != null)
		{
			_chargeTask.cancel(false);
			_chargeTask = null;
		}
		_chargeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ChargeTask(), 600000);
	}

	/**
	* Stops the Clearing Task.
	*/
	public void stopChargeTask()
	{
		if (_chargeTask != null)
		{
			_chargeTask.cancel(false);
			_chargeTask = null;
		}
	}

	/**
	 * Returns the Number of Souls this L2PcInstance got.
	 *
	 * @return
	 */
	public int getSouls()
	{
		return _souls;
	}

	public int getLastSoulConsume()
	{
		return _lastSoulConsume;
	}

	public void resetLastSoulConsume()
	{
		_lastSoulConsume = 0;
	}

	/**
	 * Absorbs a Soul from a Npc.
	 *
	 * @param skill
	 * @param target
	 */
	public void absorbSoulFromNpc(L2Skill soulMastery, L2Character target)
	{
		if (_souls >= soulMastery.getNumSouls())
		{
			sendPacket(SystemMessageId.SOUL_CANNOT_BE_INCREASED_ANYMORE);
			return;
		}

		increaseSouls(1);

		// Npc -> Player absorb animation
		if (target != null)
			broadcastPacket(new ExSpawnEmitter(getObjectId(), target.getObjectId()), 500);
	}

	/**
	 * Increase Souls
	 *
	 * @param count
	 */
	private void increaseSouls(int count) // By skill or mob kill
	{
		setSouls(_souls + count);

		SystemMessage sm = new SystemMessage(SystemMessageId.YOUR_SOUL_HAS_INCREASED_BY_S1_SO_IT_IS_NOW_AT_S2);
		sm.addNumber(count);
		sm.addNumber(_souls);
		sendPacket(sm);
	}

	public void increaseSoulsBySkill(L2Skill skill)
	{
		if (skill.getNumSouls() == 0)
			return;

		final L2Skill soulmastery = getKnownSkill(L2Skill.SKILL_SOUL_MASTERY);
		if (soulmastery == null)
			return;

		if (_souls >= soulmastery.getNumSouls())
		{
			sendPacket(SystemMessageId.SOUL_CANNOT_BE_INCREASED_ANYMORE);
			return;
		}

		increaseSouls(Math.min(skill.getNumSouls(), soulmastery.getNumSouls() - getSouls()));
	}

	/**
	 * Decreases existing Souls.
	 *
	 * @param skill
	 */
	public void decreaseSouls(L2Skill skill)
	{
		if (_souls == 0)
			return;

		// Calculation part
		int souls = _souls;
		if (skill.getSoulConsumeCount() > 0)
		{
			souls -= skill.getSoulConsumeCount();
		}
		else if (skill.getMaxSoulConsumeCount() > 0)
		{
			int consume = Math.min(_souls, skill.getMaxSoulConsumeCount());
			souls -= consume;
			_lastSoulConsume = consume; // Store for PDAM/MDAM boosting
		}

		setSouls(souls);
	}

	public void setSouls(int count)
	{
		_souls = L2Math.limit(0, count, 45); // Client can't display more

		if (_souls > 0)
			restartSoulTask();
		else
			stopSoulTask();

		sendEtcStatusUpdate();
	}

	private class SoulTask implements Runnable
	{
		public void run()
		{
			clearSouls();
		}
	}

	/**
	 * Clear out all Souls from this L2PcInstance
	 */
	public void clearSouls()
	{
		setSouls(0);
	}

	/**
	 * Starts/Restarts the SoulTask to Clear Souls after 10 Mins.
	 */
	private void restartSoulTask()
	{
		if (_soulTask != null)
		{
			_soulTask.cancel(false);
			_soulTask = null;
		}
		_soulTask = ThreadPoolManager.getInstance().scheduleGeneral(new SoulTask(), 600000);
	}

	/**
	 * Stops the Clearing Task.
	 */
	public void stopSoulTask()
	{
		if (_soulTask != null)
		{
			_soulTask.cancel(false);
			_soulTask = null;
		}
	}

	public void startFameTask(long delay, int fameFixRate)
	{
		if (getLevel() < 40 || getClassId().level() < 2)
			return;
		if (_fameTask == null)
			_fameTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new FameTask(fameFixRate), delay, delay);
	}

	public void stopFameTask()
	{
		if (_fameTask != null)
		{
			_fameTask.cancel(false);
			_fameTask = null;
		}
	}

	public class FameTask implements Runnable
	{
		protected int _value;

		protected FameTask(int value)
		{
			_value = value;
		}

		public void run()
		{
			if (isDead() && !Config.ALT_FAME_FOR_DEAD_PLAYERS)
				return;

			setFame(getFame() + _value);
			SystemMessage sm = new SystemMessage(SystemMessageId.ACQUIRED_S1_REPUTATION_SCORE);
			sm.addNumber(_value);
			sendPacket(sm);
			sendPacket(new UserInfo(L2PcInstance.this));
		}
	}


	/**
	 *
	 * @param magicId
	 * @param level
	 * @param time
	 */
	public void shortBuffStatusUpdate(int magicId, int level, int time)
	{
		if (_shortBuffTask != null)
		{
			_shortBuffTask.cancel(false);
			_shortBuffTask = null;
		}
		_shortBuffTask = ThreadPoolManager.getInstance().scheduleGeneral(new ShortBuffTask(), time * 1000);

		setShortBuffTaskSkillId(magicId);
		sendPacket(new ShortBuffStatusUpdate(magicId, level, time));
	}

	private int _shortBuffTaskSkillId;

	public void setShortBuffTaskSkillId(int id)
	{
		_shortBuffTaskSkillId = id;
	}

	public int getShortBuffTaskSkillId()
	{
		return _shortBuffTaskSkillId;
	}

	public int getDeathPenaltyBuffLevel()
	{
		return _deathPenaltyBuffLevel;
	}

	public void setDeathPenaltyBuffLevel(int level)
	{
		_deathPenaltyBuffLevel = level;
	}

	public void calculateDeathPenaltyBuffLevel(L2Character killer)
	{
		if (Config.DEATH_PENALTY_CHANCE < 1)
			return;
		
		if (!(killer instanceof L2Playable) && !isGM() && !(getCharmOfLuck() && killer.isRaid())
				&& !isPhoenixBlessed() && !isInFunEvent() && (getKarma() > 0 || Rnd.get(100) < Config.DEATH_PENALTY_CHANCE))
			increaseDeathPenaltyBuffLevel();
	}

	public void increaseDeathPenaltyBuffLevel()
	{
		if (getDeathPenaltyBuffLevel() >= 15) // Maximum level reached
			return;

		if (getDeathPenaltyBuffLevel() != 0)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel());

			if (skill != null)
				removeSkill(skill, true);
		}

		_deathPenaltyBuffLevel++;

		addSkill(SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel()), false);
		sendEtcStatusUpdate();
		SystemMessage sm = new SystemMessage(SystemMessageId.DEATH_PENALTY_LEVEL_S1_ADDED);
		sm.addNumber(getDeathPenaltyBuffLevel());
		sendPacket(sm);
	}

	public void reduceDeathPenaltyBuffLevel()
	{
		if (getDeathPenaltyBuffLevel() <= 0)
			return;

		L2Skill skill = SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel());

		if (skill != null)
			removeSkill(skill, true);

		_deathPenaltyBuffLevel--;

		if (getDeathPenaltyBuffLevel() > 0)
		{
			addSkill(SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel()), false);
			SystemMessage sm = new SystemMessage(SystemMessageId.DEATH_PENALTY_LEVEL_S1_ADDED);
			sm.addNumber(getDeathPenaltyBuffLevel());
			sendPacket(sm);
		}
		else
		{
			sendPacket(SystemMessageId.DEATH_PENALTY_LIFTED);
		}

		sendEtcStatusUpdate();
	}

	public void restoreDeathPenaltyBuffLevel()
	{
		L2Skill skill = SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel());

		if (skill != null)
			removeSkill(skill, true);

		if (getDeathPenaltyBuffLevel() > 0)
		{
			addSkill(SkillTable.getInstance().getInfo(5076, getDeathPenaltyBuffLevel()), false);
		}
	}

	private boolean _canFeed;

	private final Map<Integer, TimeStamp>	_reuseTimeStamps	= new SingletonMap<Integer, TimeStamp>().setShared();

	public Map<Integer, TimeStamp> getReuseTimeStamps()
	{
		return _reuseTimeStamps;
	}

	/**
	 * Simple class containing all neccessary information to maintain
	 * valid timestamps and reuse for skills upon relog. Filter this
	 * carefully as it becomes redundant to store reuse for small delays.
	 * @author  Yesod
	 */
	public static class TimeStamp
	{
		private final int		skill;
		private final long	reuse;
		private final long	stamp;

		public TimeStamp(int _skill, long _reuse)
		{
			skill = _skill;
			reuse = _reuse;
			stamp = System.currentTimeMillis() + reuse;
		}

		public TimeStamp(int _skill, long _reuse, long _systime)
		{
			skill = _skill;
			reuse = _reuse;
			stamp = _systime;
		}

		public long getStamp()
		{
			return stamp;
		}

		public int getSkill()
		{
			return skill;
		}

		public long getReuse()
		{
			return reuse;
		}

		public long getRemaining()
		{
			return Math.max(stamp - System.currentTimeMillis(), 0);
		}

		/* Check if the reuse delay has passed and
		 * if it has not then update the stored reuse time
		 * according to what is currently remaining on
		 * the delay. */
		public boolean hasNotPassed()
		{
			return System.currentTimeMillis() < stamp;
		}
	}

	/**
	 * Index according to skill id the current
	 * timestamp of use.
	 * @param s skill
	 * @param r delay
	 */
	@Override
	public void addTimeStamp(int s, int r)
	{
		_reuseTimeStamps.put(s, new TimeStamp(s, r));
	}

	/**
	 * Index according to skill this TimeStamp
	 * instance for restoration purposes only.
	 * @param ts TimeStamp
	 */
	public void addTimeStamp(TimeStamp ts)
	{
		_reuseTimeStamps.put(ts.getSkill(), ts);
	}

	/**
	 * Index according to skill id the current
	 * timestamp of use.
	 * @param s skillid
	 */
	@Override
	public void removeTimeStamp(int s)
	{
		_reuseTimeStamps.remove(s);
	}

	@Override
	public final void sendDamageMessage(L2Character target, int damage, boolean mcrit, boolean pcrit, boolean miss)
	{
		if (miss)
		{
			sendPacket(SystemMessageId.MISSED_TARGET);
			target.sendAvoidMessage(this);
			return;
		}

		if (pcrit)
		{
			sendPacket(SystemMessageId.CRITICAL_HIT);

			if (target instanceof L2Npc)
			{
				// Soul Mastery skill
				final L2Skill skill = getKnownSkill(L2Skill.SKILL_SOUL_MASTERY);

				if (skill != null && Rnd.get(100) < skill.getCritChance())
					absorbSoulFromNpc(skill, target);
			}
		}

		if (mcrit)
			sendPacket(SystemMessageId.CRITICAL_HIT_MAGIC);

		if (isInOlympiadMode() && target instanceof L2PcInstance && ((L2PcInstance)target).isInOlympiadMode()
			&& ((L2PcInstance)target).getOlympiadGameId() == getOlympiadGameId())
		{
			Olympiad.getInstance().notifyCompetitorDamage(this, damage, getOlympiadGameId());
		}

		final SystemMessage sm;
		if (target.isInvul() && !(target instanceof L2NpcInstance))
		{
			sm = new SystemMessage(SystemMessageId.ATTACK_WAS_BLOCKED);
		}
		else
		{
			sm = new SystemMessage(SystemMessageId.YOU_DID_S1_DMG);
			sm.addNumber(damage);
		}
		sendPacket(sm);
	}

	@Override
	public final void sendAvoidMessage(L2Character attacker)
	{
		sendPacket(new SystemMessage(SystemMessageId.AVOIDED_C1S_ATTACK).addCharName(attacker));
	}

	public boolean isKamaelic()
	{
		return getRace() == Race.Kamael;
	}

	public boolean canOpenPrivateStore()
	{
		return !isAlikeDead() && !isInOlympiadMode() && !isMounted();
	}

	public void tryOpenPrivateBuyStore()
	{
		// Player shouldn't be able to set stores if he/she is alike dead (dead or fake death)
		if (canOpenPrivateStore())
		{
			if (getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_BUY || getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_BUY + 1)
			{
				setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			}
			else if (isInsideZone(L2Zone.FLAG_NOSTORE))
			{
				sendPacket(SystemMessageId.NO_PRIVATE_STORE_HERE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			if (getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_NONE)
			{
				if (isSitting())
				{
					this.standUp();
				}
				setPrivateStoreType(L2PcInstance.STORE_PRIVATE_BUY + 1);
				this.sendPacket(new PrivateStoreManageListBuy(this));
			}
		}
		else
		{
			sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	public void tryOpenPrivateSellStore(boolean isPackageSale)
	{
		// Player shouldn't be able to set stores if he/she is alike dead (dead or fake death)
		if (canOpenPrivateStore())
		{
			if (getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_SELL || getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_SELL + 1
					|| getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_PACKAGE_SELL)
			{
				setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			}
			else if (isInsideZone(L2Zone.FLAG_NOSTORE))
			{
				sendPacket(SystemMessageId.NO_PRIVATE_STORE_HERE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			if (getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_NONE)
			{
				if (isSitting())
				{
					this.standUp();
				}
				setPrivateStoreType(L2PcInstance.STORE_PRIVATE_SELL + 1);
				this.sendPacket(new PrivateStoreManageListSell(this, isPackageSale));
			}
		}
		else
		{
			sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	public boolean isTransformed()
	{
		return _transformation != null && !_transformation.isStance();
	}
	
	public boolean isInStance()
	{
		return _transformation != null && _transformation.isStance();
	}
	
	public void transform(L2Transformation transformation)
	{
		if (_transformation != null)
		{
			// You already polymorphed and cannot polymorph again.
			sendPacket(SystemMessageId.YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN);
			return;
		}
		if(isMounted())
		{
			// Get off the strider or something else if character is mounted
			dismount();
		}
		if (getPet() != null)
		{
			// Unsummon pets
			getPet().unSummon(this);
		}
		_transformation = transformation;
		for (L2Effect e : getAllEffects())
		{
			if (e != null && e.getSkill().isToggle())
				e.exit();
		}
		transformation.onTransform(this);
		sendSkillList();
		sendPacket(new SkillCoolTime(this));
		ExBasicActionList.sendTo(this);
		broadcastUserInfo();
	}

	public void untransform()
	{
		if (_transformation != null)
		{
			_transformAllowedSkills.clear();
			_transformation.onUntransform(this);
			_transformation = null;
			stopEffects(L2EffectType.TRANSFORMATION);
			broadcastUserInfo();
			ExBasicActionList.sendTo(this);
			sendSkillList();
			sendPacket(new SkillCoolTime(this));
		}
	}

	public L2Transformation getTransformation()
	{
		return _transformation;
	}

	/**
	 * This returns the transformation Id of the current transformation.
	 * For example, if a player is transformed as a Buffalo, and then picks up the Zariche,
	 * the transform Id returned will be that of the Zariche, and NOT the Buffalo.
	 * @return Transformation Id
	 */
	public int getTransformationId()
	{
		L2Transformation transformation = getTransformation();
		if (transformation == null)
			return 0;
		return transformation.getId();
	}

	/**
	 * This returns the transformation Id stored inside the character table, selected by the method: transformSelectInfo()
	 * For example, if a player is transformed as a Buffalo, and then picks up the Zariche,
	 * the transform Id returned will be that of the Buffalo, and NOT the Zariche.
	 * @return Transformation Id
	 */
	public int transformId()
	{
		return _transformationId;
	}

	/**
	 * This is a simple query that inserts the transform Id into the character table for future reference.
	 */
	public void transformInsertInfo()
	{

		if (getTransformationId() == L2Transformation.TRANSFORM_AKAMANAH || getTransformationId() == L2Transformation.TRANSFORM_ZARICHE)
			return;

		_transformationId = getTransformationId();

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement(UPDATE_CHAR_TRANSFORM);
			statement.setInt(1, _transformationId);
			statement.setInt(2, getObjectId());

			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.fatal("Transformation insert info: " + e, e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * This selects the current
	 * @return transformation Id
	 */
	public int transformSelectInfo()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement(SELECT_CHAR_TRANSFORM);
			statement.setInt(1, getObjectId());

			ResultSet rset = statement.executeQuery();
			if (rset.next())
				_transformationId = rset.getInt("transform_id");

			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.fatal("Transformation select info error:" + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		return _transformationId;
	}

	@Override
	public boolean mustFallDownOnDeath()
	{
		return (super.mustFallDownOnDeath()) || (isInFunEvent() && Config.FALLDOWNONDEATH);
	}

	/**
	*
	* @param npcId
	*/
	public void setAgathionId(int npcId)
	{
		_agathionId = npcId;
	}

	/**
	*
	* @return
	*/
	public int getAgathionId()
	{
		return _agathionId;
	}

	/**
	 * Functions for Vitality Level.
	 */
	/** Starts Vitality Task of this PcInstance **/
	private void startVitalityTask()
	{
		stopVitalityTask();
		_vitalityTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new VitalityTask(this), 1000, 60000);
	}

	/** Stops Vitality Task of this PcInstance **/
	private void stopVitalityTask()
	{
		if (_vitalityTask != null)
		{
			_vitalityTask.cancel(true);
			_vitalityTask = null;
		}
	}

	/** Update VL of this PcInstance **/
	public void updateVitalityLevel(boolean sendMessage)
	{
		if (getVitalityPoints() > 18200.0 && getVitalityLevel() != 4)
			setVitalityLevel(4, sendMessage);
		else if (getVitalityPoints() > 14600.0 && getVitalityPoints() <= 18200.0 && getVitalityLevel() != 3)
			setVitalityLevel(3, sendMessage);
		else if (getVitalityPoints() > 1800.0 && getVitalityPoints() <= 14600.0 && getVitalityLevel()!= 2)
			setVitalityLevel(2, sendMessage);
		else if (getVitalityPoints() > 240.0 && getVitalityPoints() <= 1800.0 && getVitalityLevel() != 1)
			setVitalityLevel(1, sendMessage);
		else if (getVitalityPoints() < 240.0 && getVitalityLevel() != 0)
			setVitalityLevel(0, sendMessage);
	}

	/** Add/Increase VP of this PcInstance  **/
	public synchronized void addVitalityPoints(double val, boolean sendMessage)
	{
		if ((getVitalityPoints() + val) > 20000.0)
			_vitalityPoints = 20000.0;
		else
			_vitalityPoints += val;

		if (sendMessage)
			sendPacket(new SystemMessage(SystemMessageId.GAINED_VITALITY_POINTS));

		updateVitalityLevel(sendMessage);
	}

	/** Remove/Decrease VP of this PcInstance **/
	public synchronized void removeVitalityPoints(double val, boolean sendMessage)
	{
		if ((getVitalityPoints() - val) < 1.0)
			_vitalityPoints = 1.0;
		else
			_vitalityPoints -= val;

		updateVitalityLevel(sendMessage);
	}

	/** Returns VL Points **/
	public double getVitalityPoints()
	{
		return _vitalityPoints;
	}

	/** Set VP of this PcInstance **/
	public synchronized void setVitalityPoints(double val, boolean sendMessage)
	{
		if (val >= 20000.0)
			val = 20000.0;
		else if (val < 1.0)
			val = 1.0;

		_vitalityPoints = val;
		updateVitalityLevel(sendMessage);
	}

	/** Returns VL of this PcInstance **/
	public int getVitalityLevel()
	{
		return _vitalityLevel;
	}

	/** Set VL of this PcInstance **/
	private void setVitalityLevel(int level, boolean sendMessage)
	{
		if (level > 4)
			level = 4;
		else if (level < 0)
			level = 0;

		if (sendMessage)
		{
			if (getVitalityLevel() < level)
				sendPacket(new SystemMessage(SystemMessageId.VITALITY_HAS_INCREASED));
			else if (getVitalityLevel() > level)
				sendPacket(new SystemMessage(SystemMessageId.VITALITY_HAS_DECREASED));
			if (level == 4)
				sendPacket(new SystemMessage(SystemMessageId.VITALITY_IS_AT_MAXIMUM));
			else if (level == 0)
				sendPacket(new SystemMessage(SystemMessageId.VITALITY_IS_EXHAUSTED));
		}
		_vitalityLevel = level;

		sendPacket(new UserInfo(this));
	}

	/** Calcules points to add/remove on this PcInstance - no party **/
	public void calculateVitalityPoints(L2Npc target, int damage)
	{
		double points;

		if (target == null)
			return;

		if (getLevel() < 10)
			return;

		L2NpcTemplate template = target.getTemplate();
		int targetExp = template.getRewardExp();
		int targetLevel = template.getLevel();

		int targetTotalHp = target.getMaxHp();
		double damageRate = 1.0;

		int playerLevel = getLevel();
		double consumeRate = 1.0;

		if (targetExp <= 0)
			return;

		if (damage >= targetTotalHp)
			damage = targetTotalHp;

		if (damage > 0)
			damageRate = damage / targetTotalHp;

		if (playerLevel >= 76 && playerLevel < 79)
			consumeRate = 1.5;
		else if (playerLevel >= 79)
			consumeRate = 2.0;
		else
			consumeRate = 1.0;

		final boolean isVitalityBuffed = isVitalityBuffed();

		points = Math.round(((((targetExp / (targetLevel * targetLevel)) * 100) / 9) * damageRate) * consumeRate);

		if (points > 0)
		{
			if (target instanceof L2RaidBossInstance || target instanceof L2GrandBossInstance)
			{
				// Just a quick divider to avoid giving 10'000 points, needs to be changed
				points = (points * Config.RATE_VITALITY_GAIN) / 100;
				addVitalityPoints(points, true);
			}
			else if (!(target instanceof L2MinionInstance))
			{
				if (isVitalityBuffed)
				{
					points = points * Config.RATE_VITALITY_GAIN;
					addVitalityPoints(points, true);
				}
				else
				{
					points *= Config.RATE_VITALITY_LOST;
					removeVitalityPoints(points, true);
				}
			}
		}
		else if (points < 0)
			_log.warn("Vitality: Error in decrease " + getName() + " vitality points (no party), the calcul result was negative: " + points + "=> Target XP: " + targetExp + " TargetLevel: " + targetLevel + " Damage Rate: " + damageRate + " Vitality Rate Lost: " + Config.RATE_VITALITY_LOST + " Vitality Rate Gain: " + Config.RATE_VITALITY_GAIN + "");
	}

	private boolean isVitalityBuffed()
	{
		for (L2Effect e : getAllEffects())
		{
			switch (e.getSkill().getId())
			{
				case 2580:
				case 8244:
				case 5655:
				case 23016:
				case 23024:
				case 23032:
				case 23035:
				case 23063:
				case 23064:
					return true;
			}
		}

		return false;
	}

	/** Calcules points to add/remove on this PcInstance - in party **/
	public void calculateVitalityPoints(L2Npc target, int damage, int partyMembers, float partyExpRate)
	{
		double points;

		if (target == null)
			return;

		if (getLevel() < 10)
			return;

		if (partyMembers <= 0)
			partyMembers = 1;

		L2NpcTemplate template = target.getTemplate();
		int targetExp = template.getRewardExp();
		int targetLevel = template.getLevel();

		int targetTotalHp = target.getMaxHp();
		double damageRate = 1.0;

		int playerLevel = getLevel();
		double consumeRate = 1.0;

		if (targetExp <= 0)
			return;

		if (damage >= targetTotalHp)
			damage = targetTotalHp;

		if (damage > 0)
			damageRate = damage / targetTotalHp;

		if (playerLevel >= 76 && playerLevel < 79)
			consumeRate = 1.5;
		else if (playerLevel >= 79)
			consumeRate = 2.0;
		else
			consumeRate = 1.0;

		final boolean isVitalityBuffed = isVitalityBuffed();

		points = Math.round((((((targetExp / (targetLevel * targetLevel)) * 100) / 9) * damageRate) * consumeRate) * partyExpRate);

		if (points > 0)
		{
			if (target instanceof L2RaidBossInstance || target instanceof L2GrandBossInstance)
			{
				// Just a quick divider to avoid giving 10'000 points, needs to be changed
				points = (points * Config.RATE_VITALITY_GAIN) / 100;
				addVitalityPoints(points, true);
			}
			else if (!(target instanceof L2MinionInstance))
			{
				if (isVitalityBuffed)
				{
					points = points * Config.RATE_VITALITY_GAIN;
					addVitalityPoints(points, true);
				}
				else
				{
					points *= Config.RATE_VITALITY_LOST;
					removeVitalityPoints(points, true);
				}
			}
		}
		else if (points < 0)
			_log.warn("Vitality: Error in decrease " + getName() + " vitality points (in party), the calcul result was negative: " + points + "=> Target XP: " + targetExp + " TargetLevel: " + targetLevel + " Damage Rate: " + damageRate + " Vitality Rate Lost: " + Config.RATE_VITALITY_LOST + " Vitality Rate Gain: " + Config.RATE_VITALITY_GAIN + "");
	}

	/** Add Vitality Points on relog **/
    public void restoreVitality()
    {
		long lastAccessTime = getLastAccess();
		long curTime = System.currentTimeMillis();
		double vitalityPointsRest = getVitalityPoints();
		double vitalityPointsToAdd = ((curTime - lastAccessTime) / 60000.0) * Config.RATE_RECOVERY_ON_RECONNECT;

		if (vitalityPointsToAdd <= 0)
			vitalityPointsToAdd = 0;

		setVitalityPoints(vitalityPointsRest + vitalityPointsToAdd, false);
	}

	public L2StaticObjectInstance getObjectSittingOn()
	{
		return _throne;
	}

	public void setObjectSittingOn(L2StaticObjectInstance throne)
	{
		//prevent misuse
		if (throne == null)
			resetThrone();
		else
			_throne = throne;
	}

	public void resetThrone()
	{
		if (_throne == null) return;
		_throne.setOccupier(null);
		_throne = null;
	}

	public int getOlympiadOpponentId()
	{
		return _olympiadOpponentId;
	}

	public void setOlympiadOpponentId(int value)
	{
		_olympiadOpponentId = value;
	}

	private ImmutableReference<L2PcInstance> _immutableReference;
	private ClearableReference<L2PcInstance> _clearableReference;

	public ImmutableReference<L2PcInstance> getImmutableReference()
	{
		if (_immutableReference == null)
			_immutableReference = new ImmutableReference<L2PcInstance>(this);

		return _immutableReference;
	}

	public ClearableReference<L2PcInstance> getClearableReference()
	{
		if (_clearableReference == null)
			_clearableReference = new ClearableReference<L2PcInstance>(this);

		return _clearableReference;
	}

	@Override
	public final L2PcInstance getActingPlayer()
	{
		return this;
	}

	@Override
	public final L2Summon getActingSummon()
	{
		return getPet();
	}

	public int getTrustLevel()
	{
		return _trustlevel;
	}

	public void setTrustLevel(int trustlevel)
	{
		_trustlevel = trustlevel;
	}

	public void increaseTrustLevel(int inc)
	{
		if((_trustlevel + inc)>=1000000)
			_trustlevel = 1000000;
		else
			_trustlevel += inc;
	}

	public void decreaseTrustLevel(int inc)
	{
		if((_trustlevel - inc)<=0)
			_trustlevel = 0;
		else
			_trustlevel -= inc;
	}

	/**
	 * Set the Fame of this L2PcInstane <BR><BR>
	 * @param fame
	 */
	public void setFame(int fame)
	{
		if (fame > Config.MAX_PERSONAL_FAME_POINTS)
			_fame = Config.MAX_PERSONAL_FAME_POINTS;
		else
			_fame = fame;
	}

	/**
	 * Return the Fame of this L2PcInstance <BR><BR>
	 * @return
	 */
	public int getFame()
	{
		return _fame;
	}

	/**
	* Sets VL of this L2PcInstance<BR><BR>
	* @param level
	*/
	/*
	public void setVitalityLevel(int level)
	{
		if (level > 5)
			level = 5;
		else if (level < 0)
			level = 0;

		_vitalityLevel = level;
	}*/

	/*
	 * Function for skill Summon Friend or Gate Chant.
	 */
	// Summon friend
	private L2PcInstance _summonRequestTarget;
	private L2Skill _summonRequestSkill = null;

	/** Request Teleport **/
	public boolean teleportRequest(L2PcInstance requester, L2Skill skill)
	{
		if (_summonRequestTarget != null && requester != null)
			return false;
		_summonRequestTarget = requester;
		_summonRequestSkill = skill;
		return true;
	}

	/** Action teleport **/
	public void teleportAnswer(int answer, int requesterId)
	{
		if (_summonRequestTarget == null)
			return;
		if (answer == 1 && _summonRequestTarget.getCharId() == requesterId)
		{
			SummonFriend.teleToTarget(this, _summonRequestTarget, _summonRequestSkill);
		}
		_summonRequestTarget = null;
		_summonRequestSkill = null;
	}

	// Open/Close Gates
	private L2DoorInstance _gatesRequestTarget = null;

	public void gatesRequest(L2DoorInstance door)
	{
		_gatesRequestTarget = door;
	}

	public void gatesAnswer(int answer, int type)
	{
		if (_gatesRequestTarget == null)
			return;
		if (answer == 1 && getTarget() == _gatesRequestTarget && type == 1)
		{
			_gatesRequestTarget.openMe();
		}
		else if (answer == 1 && getTarget() == _gatesRequestTarget && type == 0)
		{
			_gatesRequestTarget.closeMe();
		}

		_gatesRequestTarget = null;
	}

	private ScheduledFuture<?> _autoSaveTask;

	private void startAutoSaveTask()
	{
		if (_autoSaveTask == null && Config.CHAR_STORE_INTERVAL > 0)
			_autoSaveTask = ThreadPoolManager.getInstance().schedule(new AutoSave(), 300000);
	}

	private void stopAutoSaveTask()
	{
		if (_autoSaveTask != null)
		{
			_autoSaveTask.cancel(false);
			_autoSaveTask = null;
		}
	}

	private final class AutoSave implements Runnable
	{
		public void run()
		{
			long period = Config.CHAR_STORE_INTERVAL * 60000L;
			long delay = _lastStore + period - System.currentTimeMillis();

			if (delay <= 0)
			{
				try
				{
					store();
				}
				catch (RuntimeException e)
				{
					_log.fatal("", e);
				}

				delay = period;
			}

			if (Config.CHAR_STORE_INTERVAL > 0)
				_autoSaveTask = ThreadPoolManager.getInstance().schedule(this, delay);
		}
	}

    public void checkItemRestriction()
    {
    	for (int i = 0; i < Inventory.PAPERDOLL_TOTALSLOTS; i++)
    	{
    		L2ItemInstance equippedItem = getInventory().getPaperdollItem(i);
    		if (equippedItem != null && !equippedItem.getItem().checkCondition(this))
    		{
				getInventory().unEquipItemInSlotAndRecord(i);
				if (equippedItem.isWear())
					continue;
				SystemMessage sm = null;
				if (equippedItem.getEnchantLevel() > 0)
				{
					sm = new SystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
					sm.addNumber(equippedItem.getEnchantLevel());
					sm.addItemName(equippedItem);
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.S1_DISARMED);
					sm.addItemName(equippedItem);
				}
				sendPacket(sm);
    		}
    	}
    }

	@Override
	public void setName(String name)
	{
		super.setName(name);

		CharNameTable.getInstance().update(getObjectId(), getName());
	}

	// =========================================================================================
	// Condition listeners
	// TODO: wrapper conditions - ConditionLogicAnd, ConditionLogicOr, etc
	// TODO: make it more similar to conventional java listeners
	// TODO: add every listener which makes sense - currently listens for hp percent and game time only

	public enum ConditionListenerDependency
	{
		PLAYER_HP,
		GAME_TIME,
		;
	}

	private abstract class ConditionListener
	{
		private final Map<Func, Boolean> _values = new SingletonMap<Func, Boolean>().setShared();
		private final Env _env;

		protected ConditionListener()
		{
			_env = new Env();
			_env.player = L2PcInstance.this;
		}

		protected void refresh(ConditionListenerDependency dependency)
		{
			for (Entry<Func, Boolean> entry : _values.entrySet())
			{
				boolean newValue = entry.getKey().isAllowed(_env);
				boolean oldValue = entry.setValue(newValue);

				if (newValue != oldValue)
					onChange(entry.getKey(), newValue);
			}
		}

		protected void onChange(Func f, boolean newValue)
		{
			sendMessage(f.funcOwner.getFuncOwnerName() + (newValue ? " activated." : " deactivated."));
		}

		protected void onFuncAddition(Func f)
		{
			final boolean newValue = f.isAllowed(_env);

			_values.put(f, newValue);

			if (newValue)
				onChange(f, true);
		}

		protected void onFuncRemoval(Func f)
		{
			_values.remove(f);
		}
	}

	private final class ConditionPlayerHpListener extends ConditionListener
	{
		@Override
		protected void onFuncAddition(Func f)
		{
			if (f.condition instanceof ConditionPlayerHp)
				super.onFuncAddition(f);
		}

		@Override
		protected void refresh(ConditionListenerDependency dependency)
		{
			if (dependency == ConditionListenerDependency.PLAYER_HP)
				super.refresh(dependency);
		}

		@Override
		protected void onChange(Func f, boolean newValue)
		{
			final SystemMessage sm;

			if (newValue)
				sm = new SystemMessage(SystemMessageId.S1_HP_DECREASED_EFFECT_APPLIES);
			else
				sm = new SystemMessage(SystemMessageId.S1_HP_DECREASED_EFFECT_DISAPPEARS);

			if (f.funcOwner.getFuncOwnerSkill() != null)
				sm.addSkillName(f.funcOwner.getFuncOwnerSkill());
			else
				sm.addString(f.funcOwner.getFuncOwnerName());

			sendPacket(sm);

			broadcastUserInfo();
		}
	}

	private final class ConditionGameTimeListener extends ConditionListener
	{
		@Override
		protected void onFuncAddition(Func f)
		{
			if (f.condition instanceof ConditionGameTime)
				super.onFuncAddition(f);
		}

		@Override
		protected void refresh(ConditionListenerDependency dependency)
		{
			if (dependency == ConditionListenerDependency.GAME_TIME)
				super.refresh(dependency);
		}

		@Override
		protected void onChange(Func f, boolean newValue)
		{
			final SystemMessage sm;

			if (newValue)
				sm = new SystemMessage(SystemMessageId.S1_NIGHT_EFFECT_APPLIES);
			else
				sm = new SystemMessage(SystemMessageId.S1_NIGHT_EFFECT_DISAPPEARS);

			if (f.funcOwner.getFuncOwnerSkill() != null)
				sm.addSkillName(f.funcOwner.getFuncOwnerSkill());
			else
				sm.addString(f.funcOwner.getFuncOwnerName());

			sendPacket(sm);

			broadcastUserInfo();
		}
	}

	private ConditionListener[] _conditionListeners;

	private ConditionListener[] getConditionListeners()
	{
		if (_conditionListeners == null)
			_conditionListeners = new ConditionListener[] { new ConditionPlayerHpListener(), new ConditionGameTimeListener() };

		return _conditionListeners;
	}

	public void onFuncAddition(Func f)
	{
		for (ConditionListener listener : getConditionListeners())
			listener.onFuncAddition(f);
	}

	public void onFuncRemoval(Func f)
	{
		for (ConditionListener listener : getConditionListeners())
			listener.onFuncRemoval(f);
	}

	public void refreshConditionListeners(ConditionListenerDependency dependency)
	{
		for (ConditionListener listener : getConditionListeners())
			listener.refresh(dependency);
	}

	// =========================================================================================

	public void sendEtcStatusUpdate()
	{
		addPacketBroadcastMask(BroadcastMode.SEND_ETC_STATUS_UPDATE);
	}

	public void sendEtcStatusUpdateImpl()
	{
		sendPacket(EtcStatusUpdate.STATIC_PACKET);
	}

	public void broadcastRelationChanged()
	{
		addPacketBroadcastMask(BroadcastMode.BROADCAST_RELATION_CHANGED);
	}

	public void broadcastRelationChangedImpl()
	{
		if (!getKnownList().getKnownPlayers().isEmpty())
			for (L2PcInstance player : getKnownList().getKnownPlayers().values())
				RelationChanged.sendRelationChanged(this, player);
	}

	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		if (isInBoat())
			getPosition().setWorldPosition(getBoat().getPosition());
		else if (isInAirShip())
			getPosition().setWorldPosition(getAirShip().getPosition());

		if (getPoly().isMorphed())
		{
			activeChar.sendPacket(new AbstractNpcInfo.PcMorphInfo(this, getPoly().getNpcTemplate()));
		}
		else
		{
			activeChar.sendPacket(new CharInfo(this));
			//activeChar.sendPacket(new ExBrExtraUserInfo(this));
		}

		if (isInBoat())
			activeChar.sendPacket(new GetOnVehicle(this, getBoat(), getInBoatPosition().getX(), getInBoatPosition().getY(), getInBoatPosition().getZ()));
		else if (isInAirShip())
			activeChar.sendPacket(new ExGetOnAirShip(this, getAirShip()));

		if (getMountType() == 4)
		{
			// TODO: Remove when horse mounts fixed
			activeChar.sendPacket(new Ride(this, false, 0));
			activeChar.sendPacket(new Ride(this, true, getMountNpcId()));
		}
		switch (getPrivateStoreType())
		{
			case L2PcInstance.STORE_PRIVATE_SELL:
			{
				activeChar.sendPacket(new PrivateStoreMsgSell(this));
				break;
			}
			case L2PcInstance.STORE_PRIVATE_PACKAGE_SELL:
			{
				activeChar.sendPacket(new ExPrivateStoreSetWholeMsg(this));
				break;
			}
			case L2PcInstance.STORE_PRIVATE_BUY:
			{
				activeChar.sendPacket(new PrivateStoreMsgBuy(this));
				break;
			}
			case L2PcInstance.STORE_PRIVATE_MANUFACTURE:
			{
				activeChar.sendPacket(new RecipeShopMsg(this));
				break;
			}
		}
	}

	@Override
	public void broadcastFullInfoImpl()
	{
		refreshOverloaded();
		refreshExpertisePenalty();

		sendPacket(new UserInfo(this));
		if (getPoly().isMorphed())
		{
			Broadcast.toKnownPlayers(this, new AbstractNpcInfo.PcMorphInfo(this, getPoly().getNpcTemplate()));
		}
		else
		{
			Broadcast.toKnownPlayers(this, new CharInfo(this));
			//Broadcast.toKnownPlayers(this, new ExBrExtraUserInfo(this));
		}
	}

	@Override
	protected boolean shouldAddPacketBroadcastMask()
	{
		return getOnlineState() != L2PcInstance.ONLINE_STATE_LOADED;
	}

	/**
	 * @return afro haircut id
	 */
	public int getAfroHaircutId()
	{
		return _afroId ;
	}

	public void setAfroHaircutId(int id)
	{
		_afroId = id;
		broadcastUserInfo();
	}

	public void setIsInSiege(boolean b)
	{
		_isInSiege = b;
	}

	public boolean isInSiege()
	{
		return _isInSiege;
	}

	@Override
	protected CharShots initShots()
	{
		return new PcShots(this);
	}

	@Override
	public PcShots getShots()
	{
		return (PcShots)_shots;
	}

	public boolean isTryingToSitOrStandup()
	{
		return ((_lastSitStandRequest + 2333) > System.currentTimeMillis());
	}

	public static enum TeleportMode
	{
		SCROLL_OF_ESCAPE,
		RECALL,
		UNSTUCK,
		GOTOLOVE,
	}

	public boolean canTeleport(TeleportMode mode)
	{
		return canTeleport(mode, false);
	}

	public boolean canTeleport(TeleportMode mode, boolean bySkill)
	{
		if (mode != TeleportMode.RECALL && !bySkill)
		{
			if (isCastingNow() || isMuted() || isAlikeDead() || isMovementDisabled() || isAllSkillsDisabled())
				return false;
		}

		if (mode == TeleportMode.SCROLL_OF_ESCAPE)
		{
			if (isSitting())
			{
				sendPacket(SystemMessageId.CANT_MOVE_SITTING);
				return false;
			}
		}

		if (!GlobalRestrictions.canTeleport(this))
			return false;

		// [L2J_JP ADD]
		if (isInsideZone(L2Zone.FLAG_NOESCAPE))
		{
			sendMessage("You can't teleport from here.");
			return false;
		}

		if (isInOlympiadMode())
		{
			if (mode == TeleportMode.SCROLL_OF_ESCAPE)
				sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
			else if (mode == TeleportMode.RECALL)
				sendPacket(SystemMessageId.THIS_SKILL_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
			else
				sendMessage("You can't teleport during Olympiad.");
			return false;
		}

		// Check to see if the player is in a festival.
		if (isFestivalParticipant())
		{
			sendMessage("You can't teleport in a festival.");
			return false;
		}

		if (inObserverMode())
		{
			sendMessage("You can't teleport during Observation Mode.");
			return false;
		}

		return true;
	}

	public boolean canSee(L2Character cha)
	{
		if (isGM())
			return true;

		final L2PcInstance player = cha.getActingPlayer();

		if (player != null)
		{
			if (player.getAppearance().isInvisible())
				return false;

			if (player.inObserverMode())
				return false;
		}
		/* Are traps invisible for other chars than owner?
		else if (cha instanceof L2Trap)
		{
		}
		*/
		return true;
	}

	public static class TeleportBookmark
	{
		public int _id,_x,_y,_z,_icon;
		public String _name,_tag;

		public TeleportBookmark(int id, int x, int y, int z, int icon,String tag , String name)
		{
			_id = id;
			_x = x;
			_y = y;
			_z = z;
			_icon = icon;
			_name = name;
			_tag = tag;
		}
	}

	public void teleportBookmarkModify(int Id,int icon, String tag,String name)
	{
		int count = 0;
		int size = tpbookmark.size();
		while(size > count)
		{
			if (tpbookmark.get(count)._id==Id)
			{
				tpbookmark.get(count)._icon = icon;
				tpbookmark.get(count)._tag = tag;
				tpbookmark.get(count)._name = name;

				Connection con = null;
				try
				{

					con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement statement = con.prepareStatement(UPDATE_TP_BOOKMARK);

					statement.setInt(1, icon);
					statement.setString(2, tag);
					statement.setString(3, name);
					statement.setInt(4, getObjectId());
					statement.setInt(5, Id);

					statement.execute();
					statement.close();
				}
				catch (Exception e)
				{
					_log.error("Could not update character teleport bookmark data: " + e, e);
				}
				finally
				{
					L2DatabaseFactory.close(con);
				}
			}
			count++;
		}
		sendPacket(new ExGetBookMarkInfoPacket(this));
	}

	public void teleportBookmarkDelete(int Id)
	{
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(DELETE_TP_BOOKMARK);

			statement.setInt(1, getObjectId());
			statement.setInt(2, Id);

			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Could not delete character teleport bookmark data: " + e, e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		int count = 0;
		int size = tpbookmark.size();

		while (size > count)
		{
			if (tpbookmark.get(count)._id==Id)
			{
				tpbookmark.remove(count);
				break;
			}
			count++;
		}

		sendPacket(new ExGetBookMarkInfoPacket(this));
	}

	public void teleportBookmarkGo(int Id)
	{
		if (!teleportBookmarkCondition(0))
			return;
		if (getInventory().getInventoryItemCount(20025, 0) == 0)
		{
			sendPacket(new SystemMessage(2359));
			return;
		}
		SystemMessage sm = new SystemMessage(SystemMessageId.S1_DISAPPEARED);
		sm.addItemName(20025);
		sendPacket(sm);

		int count = 0;
		int size = tpbookmark.size();
		while (size > count)
		{
			if (tpbookmark.get(count)._id==Id)
			{
				destroyItem("Consume", getInventory().getItemByItemId(20025).getObjectId(), 1, null, false);
				teleToLocation(tpbookmark.get(count)._x, tpbookmark.get(count)._y, tpbookmark.get(count)._z);
				break;
			}
			count++;
		}
		sendPacket(new ExGetBookMarkInfoPacket(this));
	}

	public boolean teleportBookmarkCondition(int type)
	{

		if (isInCombat())
		{
			sendPacket(new SystemMessage(2348));
			return false;
		}
		else if (isInSiege())
		{
			sendPacket(new SystemMessage(2349));
			return false;
		}
		else if (isInDuel())
		{
			sendPacket(new SystemMessage(2350));
			return false;
		}
		else if (isFlying())
		{
			sendPacket(new SystemMessage(2351));
			return false;
		}
		else if (isInOlympiadMode())
		{
			sendPacket(new SystemMessage(2352));
			return false;
		}
		else if (isParalyzed())
		{
			sendPacket(new SystemMessage(2353));
			return false;
		}
		else if (isDead())
		{
			sendPacket(new SystemMessage(2354));
			return false;
		}
		else if (isInBoat() || isInAirShip() || isInJail() || isInsideZone(L2Zone.FLAG_NOSUMMON))
		{
			if(type == 0)
				sendPacket(new SystemMessage(2355));
			else if (type == 1)
				sendPacket(new SystemMessage(2410));
			return false;
		}
		else if (isInWater())
		{
			sendPacket(new SystemMessage(2356));
			return false;
		}
		/* TODO: Instant Zone still not implement
		else if (this.isInsideZone(ZONE_INSTANT))
		{
			sendPacket(new SystemMessage(2357));
			return;
		}
		*/
		else
			return true;
	}

	public void teleportBookmarkAdd(int x,int y,int z,int icon, String tag, String name)
	{
		if (!teleportBookmarkCondition(1))
			return;

		if (tpbookmark.size() >= _bookmarkslot)
		{
			sendPacket(new SystemMessage(2358));
			return;
		}

		if (getInventory().getInventoryItemCount(20033, 0) == 0)
		{
			sendPacket(SystemMessageId.YOU_CANNOT_BOOKMARK_THIS_LOCATION_BECAUSE_YOU_DO_NOT_HAVE_A_MY_TELEPORT_FLAG);
			return;
		}

		int count = 0;
		int id = 1;
		List<Integer> idlist = new ArrayList<Integer>();

		int size = tpbookmark.size();

		while (size > count)
		{
			idlist.add(tpbookmark.get(count)._id);
			count++;
		}

		for (int i=1;i<10;i++)
		{
			if (!idlist.contains(i))
			{
				id = i;
				break;
			}
		}

		TeleportBookmark tpadd = new TeleportBookmark(id,x,y,z,icon,tag,name);
		tpbookmark.add(tpadd);

		destroyItem("Consume", getInventory().getItemByItemId(20033).getObjectId(), 1, null, false);

		SystemMessage sm = new SystemMessage(SystemMessageId.S1_DISAPPEARED);
		sm.addItemName(20033);
		sendPacket(sm);

		Connection con = null;

		try
		{

			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(INSERT_TP_BOOKMARK);

			statement.setInt(1, getObjectId());
			statement.setInt(2, id);
			statement.setInt(3, x);
			statement.setInt(4, y);
			statement.setInt(5, z);
			statement.setInt(6, icon);
			statement.setString(7, tag);
			statement.setString(8, name);


			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Could not insert character teleport bookmark data: " + e, e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		sendPacket(new ExGetBookMarkInfoPacket(this));
	}

	public void restoreTeleportBookmark()
	{
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(RESTORE_TP_BOOKMARK);
			statement.setInt(1, getObjectId());
			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				tpbookmark.add(new TeleportBookmark(rset.getInt("Id"), rset.getInt("x"), rset.getInt("y"), rset.getInt("z"), rset.getInt("icon"), rset.getString("tag"), rset.getString("name")));
			}

			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.fatal("Failed restoing character teleport bookmark.", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public boolean isFlyingMounted()
	{
		return _isFlyingMounted;
	}

	public void setIsFlyingMounted(boolean val)
	{
		_isFlyingMounted = val;
		setIsFlying(val);
	}

	public synchronized boolean enterOfflineMode()
	{
		if (isInOfflineMode())
			return false;

		leaveParty();
		if (Config.ALLOW_OFFLINE_TRADE_COLOR_NAME)
			getAppearance().setNameColor(Config.OFFLINE_TRADE_COLOR_NAME);
		sendMessage("Offline mode enabled.");

		new Disconnection(this).store().close(false);

		RegionBBSManager.changeCommunityBoard(this, PlayerStateOnCommunity.OFFLINE);
		return true;
	}

	public boolean isInOfflineMode()
	{
		return getOnlineState() == ONLINE_STATE_ONLINE && getClient() == null;
	}

	public boolean isChaotic()
	{
		return getKarma() > 0 || isCursedWeaponEquipped();
	}

	public void setOpenedSecretDoor(int doorId)
	{
		_openedSecretDoor = doorId;
		_positionWhenSecretDoorX = getX();
		_positionWhenSecretDoorY = getY();
		_positionWhenSecretDoorZ = getZ();

		ThreadPoolManager.getInstance().scheduleGeneral(new checkPositionForSecretDoor(), 1000);
	}

	class checkPositionForSecretDoor implements Runnable
	{
		public void run()
		{
			if (getX() != _positionWhenSecretDoorX || getY() != _positionWhenSecretDoorY || getZ() != _positionWhenSecretDoorZ)
			{
				DoorTable.getInstance().getDoor(_openedSecretDoor).closeMe();

				_openedSecretDoor = 0;
				_positionWhenSecretDoorX = 0;
				_positionWhenSecretDoorY = 0;
				_positionWhenSecretDoorZ = 0;
			}
			else
				ThreadPoolManager.getInstance().scheduleGeneral(new checkPositionForSecretDoor(), 500);
		}
	}

	public int getCertificationLevel(int classIndex)
	{
		Connection con = null;
		int certificationLevel = -1;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement(GET_CHAR_CERTIFICATION);

			statement.setInt(1, getObjectId());
			statement.setInt(2, classIndex);

			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				certificationLevel = rset.getInt("certif_level");
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Could not get subclass certification level: ", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		return certificationLevel;
	}

	public void storeCertificationLevel(int classIndex)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement(STORE_CHAR_CERTIFICATION);

			statement.setInt(1, getObjectId());
			statement.setInt(2, classIndex);
			statement.setInt(3, 0);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Could not store character subclass certification: ", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public void updateCertificationLevel(int classIndex, int level)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement(UPDATE_CHAR_CERTIFICATION);

			statement.setInt(1, level);
			statement.setInt(2, getObjectId());
			statement.setInt(3, classIndex);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Could not update character subclass certification: ", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public void deleteSubclassCertifications()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement(DELETE_CHAR_CERTIFICATION);

			statement.setInt(1, getObjectId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Could not delete character subclass certifications: ", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public int getSubClassType()
	{
		/*
		 * 1: Warrior
		 * 2: Rogue
		 * 3: Knight
		 * 4: Summoner
		 * 5: Wizard
		 * 6: Healer
		 * 7: Enchanter
		 */

		int classId = getActiveClass();
		int subClassType = 0;

		if (classId == 3 || classId == 2 || classId == 46 || classId == 48 || classId == 55 || classId == 57 || classId == 127 || classId == 128 || classId == 129 || classId == 89 || classId == 88 || classId == 113 || classId == 114 || classId == 117 || classId == 118 || classId == 131 || classId == 132 || classId == 133)
			subClassType = 1;
		else if (classId == 9 || classId == 24 || classId == 37 || classId == 130 || classId == 8 || classId == 23 || classId == 36 || classId == 92 || classId == 102 || classId == 109 || classId == 134 || classId == 93 || classId == 101 || classId == 108)
			subClassType = 2;
		else if (classId == 5 || classId == 6 || classId == 20 || classId == 33 || classId == 90 || classId == 91 || classId == 99 || classId == 106)
			subClassType = 3;
		else if (classId == 14 || classId == 28 || classId == 41 || classId == 96 || classId == 104 || classId == 111)
			subClassType = 4;
		else if (classId == 12 || classId == 13 || classId == 27 || classId == 40 || classId == 94 || classId == 95 || classId == 103 || classId == 110)
			subClassType = 5;
		else if (classId == 16 || classId == 30 || classId == 43 || classId == 97 || classId == 105 || classId == 112)
			subClassType = 6;
		else if (classId == 17 || classId == 21 || classId == 34 || classId == 52 || classId == 51 || classId == 135 || classId == 98 || classId == 100 || classId == 107 || classId == 116 || classId == 115 || classId == 136)
			subClassType = 7;

		return subClassType;
	}

	private final long[] _floodProtectors = new long[FloodProtector.Protected.VALUES_LENGTH];

	public long[] getFloodProtectors()
	{
		return _floodProtectors;
	}
	
	private AbstractFunEventPlayerInfo _playerInfoForEvents;
	
	public <T extends AbstractFunEventPlayerInfo> void setPlayerInfo(T info)
	{
		if (_playerInfoForEvents != null && info != null)
			throw new IllegalStateException();
		
		_playerInfoForEvents = info;
	}
	
	public <T extends AbstractFunEventPlayerInfo> T getPlayerInfo(Class<T> clazz)
	{
		final AbstractFunEventPlayerInfo info = _playerInfoForEvents;
		
		if (clazz.isInstance(info))
			return clazz.cast(info);
		else
			return null;
	}
	
	public <T extends AbstractFunEventPlayerInfo> boolean isInEvent(Class<T> clazz)
	{
		return clazz.isInstance(_playerInfoForEvents);
	}
	
	public <T extends AbstractFunEventPlayerInfo> T as(Class<T> clazz)
	{
		final AbstractFunEventPlayerInfo info = _playerInfoForEvents;
		
		if (clazz.isInstance(info))
			return clazz.cast(info);
		else
			throw new IllegalStateException();
	}
}
