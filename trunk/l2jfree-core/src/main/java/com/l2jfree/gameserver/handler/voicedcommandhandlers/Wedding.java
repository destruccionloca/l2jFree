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
package com.l2jfree.gameserver.handler.voicedcommandhandlers;

import com.l2jfree.Config;
import com.l2jfree.gameserver.SevenSigns;
import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.ai.CtrlIntention;
import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.handler.IVoicedCommandHandler;
import com.l2jfree.gameserver.instancemanager.CoupleManager;
import com.l2jfree.gameserver.instancemanager.DimensionalRiftManager;
import com.l2jfree.gameserver.instancemanager.SiegeManager;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.entity.Siege;
import com.l2jfree.gameserver.model.restriction.AvailableRestriction;
import com.l2jfree.gameserver.model.restriction.ObjectRestrictions;
import com.l2jfree.gameserver.model.zone.L2Zone;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ConfirmDlg;
import com.l2jfree.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jfree.gameserver.network.serverpackets.SetupGauge;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.util.Broadcast;

/**
 * @author evill33t
 * 
 */
public class Wedding implements IVoicedCommandHandler
{
	private static final String[]	VOICED_COMMANDS	=
													{ "divorce", "engage", "gotolove" };

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.handler.IVoicedCommandHandler#useVoicedCommand(String, com.l2jfree.gameserver.model.L2PcInstance), String)
	 */
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.startsWith("engage"))
			return engage(activeChar);
		else if (command.startsWith("divorce"))
			return divorce(activeChar);
		else if (command.startsWith("gotolove"))
			return goToLove(activeChar);
		return false;
	}

	public boolean divorce(L2PcInstance activeChar)
	{
		if (activeChar.getPartnerId() == 0)
			return false;

		int _partnerId = activeChar.getPartnerId();
		int _coupleId = activeChar.getCoupleId();
		int AdenaAmount = 0;

		if (activeChar.isMaried())
		{
			activeChar.sendMessage("You are now divorced.");

			AdenaAmount = (activeChar.getAdena() / 100) * Config.WEDDING_DIVORCE_COSTS;
			activeChar.getInventory().reduceAdena("Wedding", AdenaAmount, activeChar, null);

		}
		else
			activeChar.sendMessage("You have broken up as a couple.");

		L2PcInstance partner = L2World.getInstance().getPlayer(_partnerId);
		if (partner != null)
		{
			partner.setPartnerId(0);
			if (partner.isMaried())
				partner.sendMessage("Your fiance has decided to divorce from you.");
			else
				partner.sendMessage("Your fiance has decided to break the engagement with you.");

			// give adena
			if (AdenaAmount > 0)
				partner.addAdena("WEDDING", AdenaAmount, null, false);
		}

		CoupleManager.getInstance().deleteCouple(_coupleId);
		return true;
	}

	public boolean engage(L2PcInstance activeChar)
	{
		// Check target
		if (activeChar.getTarget() == null)
		{
			activeChar.sendMessage("You have no one targeted.");
			return false;
		}

		// Check if target is a L2PcInstance
		if (!(activeChar.getTarget() instanceof L2PcInstance))
		{
			activeChar.sendMessage("You can only ask another player for engagement");

			return false;
		}

		// Check if player is already engaged
		if (activeChar.getPartnerId() != 0)
		{
			activeChar.sendMessage("You are already engaged.");
			if (Config.WEDDING_PUNISH_INFIDELITY)
			{
				activeChar.startAbnormalEffect(L2Character.ABNORMAL_EFFECT_BIG_HEAD); // give player a Big Head
				// lets recycle the sevensigns debuffs
				int skillId;

				int skillLevel = 1;

				if (activeChar.getLevel() > 40)
					skillLevel = 2;

				if (activeChar.isMageClass())
					skillId = 4361;
				else
					skillId = 4362;

				L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);

				if (activeChar.getFirstEffect(skill) == null)
				{
					skill.getEffects(activeChar, activeChar);
					SystemMessage sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
					sm.addSkillName(skill);
					activeChar.sendPacket(sm);
				}
			}
			return false;
		}

		L2PcInstance ptarget = (L2PcInstance) activeChar.getTarget();

		// Check if player target himself
		if (ptarget.getObjectId() == activeChar.getObjectId())
		{
			activeChar.sendMessage("Is there something wrong with you, are you trying to go out with yourself?");
			return false;
		}

		if (ptarget.isMaried())
		{
			activeChar.sendMessage("Player already married.");
			return false;
		}

		if (ptarget.getPartnerId() != 0)
		{
			activeChar.sendMessage("Player already engaged.");
			return false;
		}

		if (ptarget.isEngageRequest())
		{
			activeChar.sendMessage("Player already asked by someone else.");
			return false;
		}

		if (ptarget.getAppearance().getSex() == activeChar.getAppearance().getSex() && !Config.WEDDING_SAMESEX)
		{
			activeChar.sendMessage("You can't ask someone of the same sex for engagement.");
			return false;
		}

		if (!activeChar.getFriendList().contains(ptarget))
		{
			activeChar
					.sendMessage("The player you want to ask is not on your friends list, you must first be on each others friends list before you choose to engage.");
			return false;
		}

		ptarget.setEngageRequest(true, activeChar.getObjectId());
		ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.S1.getId());
		ptarget.sendPacket(dlg.addString(activeChar.getName() + " is asking to engage you. Do you want to start a new relationship?"));
		return true;
	}

	private static L2PcInstance checkGoToLoveState(L2PcInstance activeChar)
	{
		Siege siege = SiegeManager.getInstance().getSiege(activeChar);

		if (!activeChar.isMaried())
		{
			activeChar.sendMessage("You're not married.");
			return null;
		}
		else if (activeChar.getPartnerId() == 0)
		{
			activeChar.sendMessage("Couldn't find your fiance in Database - Inform a Gamemaster.");
			_log.error("Married but couldn't find partner for " + activeChar.getName());
			return null;
		}
		// Check to see if the player is in olympiad.
		else if (activeChar.isInOlympiadMode())
		{
			activeChar.sendMessage("You are in Olympiad!");
			return null;
		}
		// Check to see if the player is in observer mode
		else if (activeChar.inObserverMode())
		{
			activeChar.sendMessage("You are in observer mode.");
			return null;
		}
		// Check to see if the player is in an event
		else if (activeChar.isInFunEvent())
		{
			activeChar.sendMessage("You are in event now.");
			return null;
		}
		// Check to see if the player is in a festival.
		else if (activeChar.isFestivalParticipant())
		{
			activeChar.sendMessage("You can't escape from a festival.");
			return null;
		}
		// Check to see if the player is in dimensional rift.
		else if (activeChar.isInParty() && activeChar.getParty().isInDimensionalRift())
		{
			activeChar.sendMessage("You are in the dimensional rift.");
			return null;
		}
		// Check to see if player is in jail
		else if (activeChar.isInJail() || activeChar.isInsideZone(L2Zone.FLAG_JAIL))
		{
			activeChar.sendMessage("You can't escape from jail.");
			return null;
		}
		// Check if player is in Siege
		else if (siege != null && siege.getIsInProgress())
		{
			activeChar.sendMessage("You are in siege, you can't go to your partner.");
			return null;
		}
		// Check if player is in Duel
		else if (activeChar.isInDuel())
		{
			activeChar.sendMessage("You are in a duel!");
			return null;
		}
		// Check if player is a Cursed Weapon owner
		else if (activeChar.isCursedWeaponEquipped())
		{
			activeChar.sendMessage("You are currently holding a cursed weapon.");
			return null;
		}
		// Check if player is in a Monster Derby Track
		else if (activeChar.isInsideZone(L2Zone.FLAG_NOESCAPE))
		{
			activeChar.sendMessage("You cannot escape from here.");
			return null;
		}
		else if (ObjectRestrictions.getInstance().checkRestriction(activeChar, AvailableRestriction.PlayerGotoLove))
		{
			activeChar.sendMessage("You cannot find your love due to a restriction.");
			return null;
		}

		L2PcInstance partner = L2World.getInstance().getPlayer(activeChar.getPartnerId());
		if (partner != null)
		{
			siege = SiegeManager.getInstance().getSiege(partner);
		}
		else
		{
			activeChar.sendMessage("Your partner is not online.");
			return null;
		}
		
		// Check to see if the player is in a instance.
		if (activeChar.getInstanceId() != partner.getInstanceId())
		{
			activeChar.sendMessage("Your partner is in another World!");
			return null;
		}
		else if (partner.isInJail() || partner.isInsideZone(L2Zone.FLAG_JAIL))
		{
			activeChar.sendMessage("Your partner is in jail.");
			return null;
		}
		else if (partner.isInOlympiadMode())
		{
			activeChar.sendMessage("Your partner is in Olympiad now.");
			return null;
		}
		else if (partner.inObserverMode())
		{
			activeChar.sendMessage("Your partner is in observer mode.");
			return null;
		}
		else if (partner.isInDuel())
		{
			activeChar.sendMessage("Your partner is in a duel.");
			return null;
		}
		else if (partner.isInFunEvent())
		{
			activeChar.sendMessage("Your partner is in an event.");
			return null;
		}
		else if (DimensionalRiftManager.getInstance().checkIfInRiftZone(partner.getX(), partner.getY(), partner.getZ(), false))
		{
			activeChar.sendMessage("Your partner is in dimensional rift.");
			return null;
		}
		else if (partner.isFestivalParticipant())
		{
			activeChar.sendMessage("Your partner is in a festival.");
			return null;
		}
		else if (siege != null && siege.getIsInProgress())
		{
			if (partner.getAppearance().getSex())
				activeChar.sendMessage("Your partner is in siege, you can't go to her.");
			else
				activeChar.sendMessage("Your partner is in siege, you can't go to him.");
			return null;
		}
		else if (partner.isCursedWeaponEquipped())
		{
			activeChar.sendMessage("Your partner is currently holding a cursed weapon.");
			return null;
		}
		else if (partner.isInsideZone(L2Zone.FLAG_NOESCAPE) || partner.isInsideZone(L2Zone.FLAG_SUNLIGHTROOM) || partner.isInsideZone(L2Zone.FLAG_NOSUMMON))
		{
			activeChar.sendMessage("Your partner is in a unsuitable area for teleporting.");
			return null;
		}
		else if (partner.isIn7sDungeon() && !activeChar.isIn7sDungeon())
		{
			int playerCabal = SevenSigns.getInstance().getPlayerCabal(activeChar);
			boolean isSealValidationPeriod = SevenSigns.getInstance().isSealValidationPeriod();
			int compWinner = SevenSigns.getInstance().getCabalHighestScore();

			if (isSealValidationPeriod)
			{
				if (playerCabal != compWinner)
				{
					activeChar.sendMessage("Your Partner is in a Seven Signs Dungeon and you are not in the winner Cabal!");
					return null;
				}
			}
			else
			{
				if (playerCabal == SevenSigns.CABAL_NULL)
				{
					activeChar.sendMessage("Your Partner is in a Seven Signs Dungeon and you are not registered!");
					return null;
				}
			}
		}
		
		return partner;
	}

	public boolean goToLove(L2PcInstance activeChar)
	{
		if (activeChar.isCastingNow() || activeChar.isMovementDisabled() || activeChar.isMuted() || activeChar.isAlikeDead())
			return false;

		L2PcInstance partner = null;
		if ((partner = checkGoToLoveState(activeChar)) == null)
			return false;

		int teleportTimer = Config.WEDDING_TELEPORT_INTERVAL * 1000;

		activeChar.sendMessage("After " + teleportTimer / 60000 + " min. you will be teleported to your fiance.");
		activeChar.getInventory().reduceAdena("Wedding", Config.WEDDING_TELEPORT_PRICE, activeChar, null);

		activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		// SoE Animation section
		activeChar.setTarget(activeChar);
		activeChar.disableAllSkills();

		MagicSkillUse msk = new MagicSkillUse(activeChar, 1050, 1, teleportTimer, 0);
		Broadcast.toSelfAndKnownPlayersInRadius(activeChar, msk, 810000/*900*/);
		SetupGauge sg = new SetupGauge(0, teleportTimer);
		activeChar.sendPacket(sg);
		// End SoE Animation section

		EscapeFinalizer ef = new EscapeFinalizer(activeChar, partner.getX(), partner.getY(), partner.getZ(), partner.isIn7sDungeon());
		// Continue execution later
		activeChar.setSkillCast(ThreadPoolManager.getInstance().scheduleGeneral(ef, teleportTimer));
		activeChar.forceIsCastingForDuration(teleportTimer);

		return true;
	}

	private static class EscapeFinalizer implements Runnable
	{
		private L2PcInstance	_activeChar;
		private int				_partnerx;
		private int				_partnery;
		private int				_partnerz;
		private boolean			_to7sDungeon;

		EscapeFinalizer(L2PcInstance activeChar, int x, int y, int z, boolean to7sDungeon)
		{
			_activeChar = activeChar;
			_partnerx = x;
			_partnery = y;
			_partnerz = z;
			_to7sDungeon = to7sDungeon;
		}

		public void run()
		{
			if (_activeChar.isDead())
				return;
			_activeChar.enableAllSkills();
			_activeChar.setIsCastingNow(false);

			if (checkGoToLoveState(_activeChar) == null)
				return;

			try
			{
				_activeChar.setIsIn7sDungeon(_to7sDungeon);
				_activeChar.teleToLocation(_partnerx, _partnery, _partnerz);
			}
			catch (Exception e)
			{
				_log.error(e.getMessage(), e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.handler.IVoicedCommandHandler#getVoicedCommandList()
	 */
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}