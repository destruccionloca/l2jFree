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

import java.util.Collection;
import java.util.concurrent.ScheduledFuture;

import javolution.text.TextBuilder;
import javolution.util.FastList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.ai.CtrlIntention;
import com.l2jfree.gameserver.ai.L2CharacterAI;
import com.l2jfree.gameserver.ai.L2DoorAI;
import com.l2jfree.gameserver.datatables.ClanTable;
import com.l2jfree.gameserver.instancemanager.CastleManager;
import com.l2jfree.gameserver.instancemanager.FortManager;
import com.l2jfree.gameserver.instancemanager.SiegeManager;
import com.l2jfree.gameserver.model.L2CharPosition;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2Clan;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2SiegeClan;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.knownlist.DoorKnownList;
import com.l2jfree.gameserver.model.actor.stat.DoorStat;
import com.l2jfree.gameserver.model.actor.status.DoorStatus;
import com.l2jfree.gameserver.model.entity.Castle;
import com.l2jfree.gameserver.model.entity.ClanHall;
import com.l2jfree.gameserver.model.entity.Fort;
import com.l2jfree.gameserver.model.entity.Siege;
import com.l2jfree.gameserver.model.mapregion.L2MapRegion;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.DoorStatusUpdate;
import com.l2jfree.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jfree.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfree.gameserver.network.serverpackets.StaticObject;
import com.l2jfree.gameserver.network.serverpackets.ValidateLocation;
import com.l2jfree.gameserver.templates.L2CharTemplate;
import com.l2jfree.gameserver.templates.L2Weapon;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.2.2.2.5 $ $Date: 2005/03/27 15:29:32 $
 */
public class L2DoorInstance extends L2Character
{

	final static Log			_log				= LogFactory.getLog(L2DoorInstance.class.getName());

	/** The castle index in the array of L2Castle this L2DoorInstance belongs to */
	private int					_castleIndex		= -2;
	private Castle				_castle;
	/** The fort index in the array of L2Fort this L2DoorInstance belongs to */
	private int					_fortIndex			= -2;
	private Fort				_fort;

	private L2MapRegion			_mapRegion			= null;

	// when door is closed, the dimensions are
	private int					_rangeXMin			= 0;
	private int					_rangeYMin			= 0;
	private int					_rangeZMin			= 0;
	private int					_rangeXMax			= 0;
	private int					_rangeYMax			= 0;
	private int					_rangeZMax			= 0;

	protected final int			_doorId;
	protected final String		_name;
	private int					_open;
	private boolean				_unlockable;

	private ClanHall			_clanHall;

	protected int				_autoActionDelay	= -1;
	private ScheduledFuture<?>	_autoActionTask;

	/** This class may be created only by L2Character and only for AI */
	public class AIAccessor extends L2Character.AIAccessor
	{
		protected AIAccessor()
		{
		}

		@Override
		public L2DoorInstance getActor()
		{
			return L2DoorInstance.this;
		}

		@Override
		@SuppressWarnings("unused")
		public void moveTo(int x, int y, int z, int offset)
		{
		}

		@Override
		@SuppressWarnings("unused")
		public void moveTo(int x, int y, int z)
		{
		}

		@Override
		@SuppressWarnings("unused")
		public void stopMove(L2CharPosition pos)
		{
		}

		@Override
		@SuppressWarnings("unused")
		public void doAttack(L2Character target)
		{
		}

		@Override
		@SuppressWarnings("unused")
		public void doCast(L2Skill skill)
		{
		}
	}

	@Override
	public L2CharacterAI getAI()
	{
		if (_ai == null)
		{
			synchronized (this)
			{
				_ai = new L2DoorAI(new AIAccessor());
			}
		}
		return _ai;
	}

	@Override
	public boolean hasAI()
	{
		return (_ai != null);
	}

	class CloseTask implements Runnable
	{
		public void run()
		{
			try
			{
				onClose();
			}
			catch (Throwable e)
			{
				_log.fatal("", e);
			}
		}
	}

	/**
	 * Manages the auto open and closing of a door.
	 */
	class AutoOpenClose implements Runnable
	{
		public void run()
		{
			try
			{
				String doorAction;

				if (getOpen() == 1)
				{
					doorAction = "opened";
					openMe();
				}
				else
				{
					doorAction = "closed";
					closeMe();
				}

				if (_log.isDebugEnabled())
					_log.info("Auto " + doorAction + " door ID " + _doorId + " (" + _name + ") for " + (_autoActionDelay / 60000) + " minute(s).");
			}
			catch (Exception e)
			{
				_log.warn("Could not auto open/close door ID " + _doorId + " (" + _name + ")");
			}
		}
	}

	/**
	 */
	public L2DoorInstance(int objectId, L2CharTemplate template, int doorId, String name, boolean unlockable)
	{
		super(objectId, template);
		getKnownList(); // init knownlist
		getStat(); // init stats
		getStatus(); // init status
		_doorId = doorId;
		_name = name;
		_unlockable = unlockable;
	}

	@Override
	public final DoorKnownList getKnownList()
	{
		if (_knownList == null)
			_knownList = new DoorKnownList(this);

		return (DoorKnownList) _knownList;
	}

	@Override
	public DoorStat getStat()
	{
		if (_stat == null)
			_stat = new DoorStat(this);

		return (DoorStat) _stat;
	}

	@Override
	public DoorStatus getStatus()
	{
		if (_status == null)
			_status = new DoorStatus(this);

		return (DoorStatus) _status;
	}

	public final boolean isUnlockable()
	{
		return _unlockable;
	}

	@Override
	public final int getLevel()
	{
		return 1;
	}

	/**
	 * @return Returns the doorId.
	 */
	public int getDoorId()
	{
		return _doorId;
	}

	/**
	 * @return Returns the open.
	 */
	public int getOpen()
	{
		return _open;
	}

	/**
	 * @param open
	 *            The open to set.
	 */
	public void setOpen(int open)
	{
		_open = open;
	}

	/**
	 * Sets the delay in milliseconds for automatic opening/closing of this door
	 * instance. <BR>
	 * <B>Note:</B> A value of -1 cancels the auto open/close task.
	 * 
	 * @param int
	 *            actionDelay
	 */
	public void setAutoActionDelay(int actionDelay)
	{
		if (_autoActionDelay == actionDelay)
			return;

		if (actionDelay > -1)
		{
			AutoOpenClose ao = new AutoOpenClose();
			ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(ao, actionDelay, actionDelay);
		}
		else
		{
			if (_autoActionTask != null)
				_autoActionTask.cancel(false);
		}

		_autoActionDelay = actionDelay;
	}

	public int getDamage()
	{
		int dmg = 6 - (int) Math.ceil(getStatus().getCurrentHp() / getMaxHp() * 6);
		if (dmg > 6)
			return 6;
		if (dmg < 0)
			return 0;
		return dmg;
	}

	public final Castle getCastle()
	{
		if (_castle == null)
		{
			Castle castle = null;

			if (_castleIndex < 0)
			{
				castle = CastleManager.getInstance().getCastle(this);
				if (castle != null)
					_castleIndex = castle.getCastleId();
			}
			if (_castleIndex > 0)
				castle = CastleManager.getInstance().getCastleById(_castleIndex);
			_castle = castle;
		}
		return _castle;
	}

	public final Fort getFort()
	{
		if (_fort == null)
		{
			if (_fortIndex < 0)
				_fortIndex = FortManager.getInstance().getFortIndex(this);
			if (_fortIndex < 0)
				return null;
			_fort = FortManager.getInstance().getForts().get(_fortIndex);
		}
		return _fort;
	}

	public void setClanHall(ClanHall clanHall)
	{
		_clanHall = clanHall;
	}

	public ClanHall getClanHall()
	{
		return _clanHall;
	}

	public boolean isEnemy()
	{
		if (getCastle() != null && getCastle().getSiege().getIsInProgress())
			return true;
		if (getFort() != null && getFort().getSiege().getIsInProgress())
			return true;
		return false;
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		if (isUnlockable())
			return true;

		// Doors can't be attacked by NPCs
		if (!(attacker instanceof L2PcInstance))
			return false;

		// Attackable only during siege by everyone
		boolean isCastle = (getCastle() != null && getCastle().getCastleId() > 0 && getCastle().getSiege().getIsInProgress());

		boolean isFort = (getFort() != null && getFort().getFortId() > 0 && getFort().getSiege().getIsInProgress());

		return (isCastle || isFort);
	}

	public boolean isAttackable(L2Character attacker)
	{
		return isAutoAttackable(attacker);
	}

	@Override
	public void updateAbnormalEffect()
	{
	}

	public int getDistanceToWatchObject(L2Object object)
	{
		if (!(object instanceof L2PcInstance))
			return 0;
		return 2000;
	}

	/**
	 * Return the distance after which the object must be remove from
	 * _knownObject according to the type of the object.<BR>
	 * <BR>
	 * 
	 * <B><U> Values </U> :</B><BR>
	 * <BR>
	 * <li> object is a L2PcInstance : 4000</li>
	 * <li> object is not a L2PcInstance : 0 </li>
	 * <BR>
	 * <BR>
	 * 
	 */
	public int getDistanceToForgetObject(L2Object object)
	{
		if (!(object instanceof L2PcInstance))
			return 0;

		return 4000;
	}

	/**
	 * Return null.<BR>
	 * <BR>
	 */
	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		return null;
	}

	@Override
	public L2Weapon getActiveWeaponItem()
	{
		return null;
	}

	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}

	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		return null;
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		if (player == null)
			return;

		if (Config.SIEGE_ONLY_REGISTERED)
		{
			boolean opp = false;
			Siege siege = SiegeManager.getInstance().getSiege(player);
			L2Clan oppClan = player.getClan();
			if (siege != null && siege.getIsInProgress())
			{
				if (oppClan != null)
				{
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
				}
			}
			else
				opp = true;

			if (!opp)
				return;
		}

		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);

			// Send a Server->Client packet MyTargetSelected to the L2PcInstance
			// player
			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);

			// send HP amount if doors are inside castle/fortress zone
			// TODO: needed to be added here doors from conquerable clanhalls
			StaticObject su = new StaticObject(this, (getCastle() != null || getFort() != null));
			player.sendPacket(su);

			// Send a Server->Client packet ValidateLocation to correct the
			// L2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		else
		{
			if (isAutoAttackable(player))
			{
				if (Math.abs(player.getZ() - getZ()) < 400) // this max heigth
				// difference might
				// need some
				// tweaking
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
				}
			}
			else if (player.getClan() != null && getClanHall() != null && player.getClanId() == getClanHall().getOwnerId())
			{
				if (!isInsideRadius(player, L2NpcInstance.INTERACTION_DISTANCE, false, false))
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				}
				else
				{
					if (getOpen() == 1)
						openMe();
					else
						closeMe();
				}
			}
		}
		// Send a Server->Client ActionFailed to the L2PcInstance in order to
		// avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public void onActionShift(L2PcInstance player)
	{
		if (player.getAccessLevel() >= Config.GM_ACCESSLEVEL)
		{
			player.setTarget(this);
			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);

			// send HP amount if doors are inside castle/fortress zone
			// TODO: needed to be added here doors from conquerable clanhalls
			StaticObject su = new StaticObject(this, (getCastle() != null || getFort() != null));
			player.sendPacket(su);

			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			TextBuilder html1 = new TextBuilder("<html><body><table border=0>");
			html1.append("<tr><td>S.Y.L. Says:</td></tr>");
			html1.append("<tr><td>Current HP  " + getStatus().getCurrentHp() + "</td></tr>");
			html1.append("<tr><td>Max HP      " + getMaxHp() + "</td></tr>");

			html1.append("<tr><td>Object ID: " + getObjectId() + "</td></tr>");
			html1.append("<tr><td>Door ID:<br>" + getDoorId() + "</td></tr>");
			html1.append("<tr><td><br></td></tr>");

			html1.append("<tr><td>Class: " + getClass().getName() + "</td></tr>");
			html1.append("<tr><td><br></td></tr>");
			html1.append("</table>");

			html1.append("<table><tr>");
			html1.append("<td><button value=\"Open\" action=\"bypass -h admin_open " + getDoorId()
					+ "\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			html1.append("<td><button value=\"Close\" action=\"bypass -h admin_close " + getDoorId()
					+ "\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			html1
					.append("<td><button value=\"Kill\" action=\"bypass -h admin_kill\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			html1
					.append("<td><button value=\"Delete\" action=\"bypass -h admin_delete\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			html1.append("</tr></table></body></html>");

			html.setHtml(html1.toString());
			player.sendPacket(html);
		}
		else
		{
			// ATTACK the mob without moving?
		}

		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public void broadcastStatusUpdate()
	{
		Collection<L2PcInstance> knownPlayers = getKnownList().getKnownPlayers().values();
		if (knownPlayers == null || knownPlayers.isEmpty())
			return;

		StaticObject su = new StaticObject(this, (getCastle() != null || getFort() != null));
		DoorStatusUpdate dsu = new DoorStatusUpdate(this);

		for (L2PcInstance player : knownPlayers)
		{
			player.sendPacket(su);
			player.sendPacket(dsu);
		}
	}

	public void onOpen()
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new CloseTask(), 60000);
	}

	public void onClose()
	{
		closeMe();
	}

	public final void closeMe()
	{
		setOpen(1);
		broadcastStatusUpdate();
	}

	public final void openMe()
	{
		setOpen(0);
		broadcastStatusUpdate();
	}

	@Override
	public String toString()
	{
		return "door " + _doorId;
	}

	public String getDoorName()
	{
		return _name;
	}

	public int getXMin()
	{
		return _rangeXMin;
	}

	public int getYMin()
	{
		return _rangeYMin;
	}

	public int getZMin()
	{
		return _rangeZMin;
	}

	public int getXMax()
	{
		return _rangeXMax;
	}

	public int getYMax()
	{
		return _rangeYMax;
	}

	public int getZMax()
	{
		return _rangeZMax;
	}

	public void setRange(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax)
	{
		_rangeXMin = xMin;
		_rangeYMin = yMin;
		_rangeZMin = zMin;

		_rangeXMax = xMax;
		_rangeYMax = yMax;
		_rangeZMax = zMax;
	}

	public L2MapRegion getMapRegion()
	{
		return _mapRegion;
	}

	public void setMapRegion(L2MapRegion region)
	{
		_mapRegion = region;
	}

	public Collection<L2SiegeGuardInstance> getKnownSiegeGuards()
	{
		FastList<L2SiegeGuardInstance> result = new FastList<L2SiegeGuardInstance>();

		for (L2Object obj : getKnownList().getKnownObjects().values())
		{
			if (obj instanceof L2SiegeGuardInstance)
				result.add((L2SiegeGuardInstance) obj);
		}

		return result;
	}
}
