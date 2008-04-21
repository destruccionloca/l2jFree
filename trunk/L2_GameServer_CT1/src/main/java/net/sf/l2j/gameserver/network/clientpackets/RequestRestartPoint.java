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
package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.instancemanager.FortManager;
import net.sf.l2j.gameserver.instancemanager.FortSiegeManager;
import net.sf.l2j.gameserver.instancemanager.MapRegionManager;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
import net.sf.l2j.gameserver.model.L2SiegeClan;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.model.entity.Fort;
import net.sf.l2j.gameserver.model.entity.FortSiege;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.mapregion.TeleportWhereType;
import net.sf.l2j.gameserver.model.zone.L2Zone;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.IllegalPlayerAction;
import net.sf.l2j.gameserver.util.Util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class RequestRestartPoint extends L2GameClientPacket
{
	private static final String _C__6d_REQUESTRESTARTPOINT = "[C] 6d RequestRestartPoint";
	private final static Log _log = LogFactory.getLog(RequestRestartPoint.class.getName());	
	
	protected int     _requestedPointType;
	protected boolean _continuation;
	
	/**
	 * packet type id 0x6d
	 * format: c
	 * @param decrypt
	 */
	@Override
	protected void readImpl()
	{
		_requestedPointType = readD();
	}
	
	class DeathTask implements Runnable
	{
		final L2PcInstance activeChar;
		DeathTask (L2PcInstance _activeChar)
		{
			activeChar = _activeChar;
		}
		
		public void run()
		{
			try
			{
				Location loc = null;
				Siege siege = null;
				FortSiege fsiege = null;
				boolean isInDefense = false;

				if (activeChar.isInJail()) _requestedPointType = 27;
				else if (activeChar.isFestivalParticipant()) _requestedPointType = 5;

				switch (_requestedPointType)
				{
					case 1: // to clanhall
						if (activeChar.getClan().getHasHideout() == 0)
						{
							//cheater
							activeChar.sendMessage("You may not use this respawn point!");
							Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " used respawn cheat.", IllegalPlayerAction.PUNISH_KICK);
							return;
						}
						loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.ClanHall);
						
						if (ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan())!= null &&
								ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan()).getFunction(ClanHall.FUNC_RESTORE_EXP) != null)
						{
							activeChar.restoreExp(ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan()).getFunction(ClanHall.FUNC_RESTORE_EXP).getLvl());
						}
						break;

					case 2: // to castle
						siege = SiegeManager.getInstance().getSiege(activeChar);
						if (siege != null && siege.getIsInProgress())
						{
							//siege in progress
							if (siege.checkIsDefender(activeChar.getClan()))
								loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.Castle);
							// Just in case you lost castle while beeing dead.. Port to nearest Town.
							else if (siege.checkIsAttacker(activeChar.getClan()))
								loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.Town);
							else
							{
								//cheater
								activeChar.sendMessage("You may not use this respawn point!");
								Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " used respawn cheat.", IllegalPlayerAction.PUNISH_KICK);
								return;
							}
						}
						else
						{
							if (activeChar.getClan().getHasCastle() == 0)
							{
								//cheater
								activeChar.sendMessage("You may not use this respawn point!");
								Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " used respawn cheat.", IllegalPlayerAction.PUNISH_KICK);
								return;
							}
							else
								loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.Castle);
						}
						break;

					case 3: // to Fortress
						fsiege = FortSiegeManager.getInstance().getSiege(activeChar);
						if (fsiege != null && fsiege.getIsInProgress())
						{
							//siege in progress
							if (fsiege.checkIsDefender(activeChar.getClan()))
								loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.Fortress);
							// Just in case you lost fort while beeing dead.. Port to nearest Town.
							else if (fsiege.checkIsAttacker(activeChar.getClan()))
								loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.Town);
							else
							{
								//cheater
								activeChar.sendMessage("You may not use this respawn point!");
								Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " used respawn cheat.", IllegalPlayerAction.PUNISH_KICK);
								return;
							}
						}
						else
						{
							if (activeChar.getClan().getHasFort() == 0)
							{
								//cheater
								activeChar.sendMessage("You may not use this respawn point!");
								Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " used respawn cheat.", IllegalPlayerAction.PUNISH_KICK);
								return;
							}
							else
								loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.Fortress);
						}
						break;

					case 4: // to siege HQ
						L2SiegeClan siegeClan = null;
						siege = SiegeManager.getInstance().getSiege(activeChar);
						
						if (siege != null && siege.getIsInProgress())
							siegeClan = siege.getAttackerClan(activeChar.getClan());
						else
						{
							fsiege = FortSiegeManager.getInstance().getSiege(activeChar);
							if (fsiege != null && fsiege.getIsInProgress())
								siegeClan = siege.getAttackerClan(activeChar.getClan());
						}
						
						if (siegeClan == null || siegeClan.getFlag().size() == 0)
						{
							//cheater
							activeChar.sendMessage("You may not use this respawn point!");
							Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " used respawn cheat.", IllegalPlayerAction.PUNISH_KICK);
							return;
						}
						loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.SiegeFlag);
						break;

					case 5: // Fixed or Player is a festival participant
						if (!activeChar.isGM() && !activeChar.isFestivalParticipant())
						{
							//cheater
							activeChar.sendMessage("You may not use this respawn point!");
							Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " used respawn cheat.", IllegalPlayerAction.PUNISH_KICK);
							return;
						}
						if (activeChar.isGM()) activeChar.restoreExp(100.0);
						loc = new Location(activeChar.getX(), activeChar.getY(), activeChar.getZ()); // spawn them where they died
						break;

					case 27: // to jail
						if (!activeChar.isInJail()) return;
						loc = new Location(-114356, -249645, -2984);
						break;

					default: // 0
						if (activeChar.isInsideZone(L2Zone.FLAG_JAIL) || activeChar.isInsideZone(L2Zone.FLAG_NOESCAPE))
						{
							if (loc == null)
								loc = new Location(activeChar.getX(), activeChar.getY(), activeChar.getZ()); // spawn them where they died
						}
						else
							loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.Town);
						break;
				}
				//Teleport and revive
				activeChar.setIsPendingRevive(true);
				activeChar.teleToLocation(loc, true);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		if (activeChar.isFakeDeath())
		{
			activeChar.stopFakeDeath(null);
			return;
		}
		else if(!activeChar.isDead())
		{
			_log.warn("Living player ["+activeChar.getName()+"] called RestartPointPacket! Ban this player!");
			return;
		}

		Castle castle = CastleManager.getInstance().getCastle(activeChar.getX(),activeChar.getY(), activeChar.getZ());
		if (castle != null && castle.getSiege().getIsInProgress())
		{
			//DeathFinalizer df = new DeathFinalizer(10000);
			SystemMessage sm = new SystemMessage(SystemMessageId.S1);
			if (activeChar.getClan() != null
					&& castle.getSiege().checkIsAttacker(activeChar.getClan()))
			{
				// Schedule respawn delay for attacker
				ThreadPoolManager.getInstance().scheduleGeneral(new DeathTask(activeChar), castle.getSiege().getAttackerRespawnDelay());
				sm.addString("You will be re-spawned in " + castle.getSiege().getAttackerRespawnDelay()/1000 + " seconds.");
				activeChar.sendPacket(sm);
			}
			else
			{
				// Schedule respawn delay for defender with penalty for CT lose
				ThreadPoolManager.getInstance().scheduleGeneral(new DeathTask(activeChar), castle.getSiege().getDefenderRespawnDelay());
				sm.addString("You will be re-spawned in " + castle.getSiege().getDefenderRespawnDelay()/1000 + " seconds.");
				activeChar.sendPacket(sm);
			}
			return;
		}
		

		// run immediatelly (no need to schedule)
		new DeathTask(activeChar).run();
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__6d_REQUESTRESTARTPOINT;
	}
}
