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
package com.l2jfree.gameserver.network.clientpackets;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.gameserver.ai.CtrlIntention;
import com.l2jfree.gameserver.ai.L2SummonAI;
import com.l2jfree.gameserver.datatables.PetSkillsTable;
import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.model.L2CharPosition;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2ManufactureList;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2Summon;
import com.l2jfree.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfree.gameserver.model.actor.instance.L2SiegeSummonInstance;
import com.l2jfree.gameserver.model.actor.instance.L2StaticObjectInstance;
import com.l2jfree.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.RecipeShopManageList;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 *
 * @version $Revision: 1.11.2.7.2.9 $ $Date: 2005/04/06 16:13:48 $
 */
public class RequestActionUse extends L2GameClientPacket
{
	private static final String	_C__45_REQUESTACTIONUSE	= "[C] 45 RequestActionUse";
	private final static Log	_log					= LogFactory.getLog(RequestActionUse.class.getName());

	private int					_actionId;
	private boolean				_ctrlPressed;
	private boolean				_shiftPressed;

	/**
	 * packet type id 0x45
	 * format:      cddc
	 * @param rawPacket
	 */
	@Override
	protected void readImpl()
	{
		_actionId = readD();
		_ctrlPressed = (readD() == 1);
		_shiftPressed = (readC() == 1);
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null)
			return;

		if (_log.isDebugEnabled())
			_log.debug(activeChar.getName() + " request Action use: id " + _actionId + " 2:" + _ctrlPressed + " 3:" + _shiftPressed);

		if (activeChar.isAlikeDead() || activeChar.isOutOfControl() ||
				activeChar.isTryingToSitOrStandup())
		{
			getClient().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		// don't allow to do some action if player is transformed
		if (activeChar.isTransformed())
		{
			int[] notAllowedActions = {0, 10, 28, 37, 51, 61};
			if (Arrays.binarySearch(notAllowedActions, _actionId) >= 0)
			{
				getClient().sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}

		L2Summon pet = activeChar.getPet();
		L2Object target = activeChar.getTarget();

		if (_log.isDebugEnabled())
			_log.info("Requested Action ID: " + String.valueOf(_actionId));

		switch (_actionId)
		{
		case 0:
			if (activeChar.getMountType() != 0)
				break;

			if (target instanceof L2StaticObjectInstance && !activeChar.isSitting())
			{
				if (!((L2StaticObjectInstance) target).onSit(activeChar))
					activeChar.sendMessage("Sitting on throne has failed.");
				else
					return;
			}

			if (activeChar.isSitting())
			{
				activeChar.resetThrone();
				activeChar.standUp(false); // User requested - Checks if animation already running.
			}
			else
				activeChar.sitDown(false); // User requested - Checks if animation already running.

			if (_log.isDebugEnabled())
				_log.debug("new wait type: " + (activeChar.isSitting() ? "STANDING" : "SITTING"));

			break;
		case 1:
			if (activeChar.isRunning())
				activeChar.setWalking();
			else
				activeChar.setRunning();

			if (_log.isDebugEnabled())
				_log.debug("new move type: " + (activeChar.isRunning() ? "RUNNING" : "WALKIN"));
			break;
		case 10:
			// Private Store Sell
			activeChar.tryOpenPrivateSellStore(false);
			break;
		case 15:
		case 21: // pet follow/stop
			if (pet != null && !pet.isOutOfControl())
				((L2SummonAI)pet.getAI()).notifyFollowStatusChange();
			break;
		case 16:
		case 22: // pet attack
			if (target != null && pet != null && pet != target && !pet.isAttackingDisabled() && !pet.isOutOfControl())
			{
				if (pet instanceof L2PetInstance && (pet.getLevel() - activeChar.getLevel() > 20))
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.PET_TOO_HIGH_TO_CONTROL));
					return;
				}
				if (activeChar.isInOlympiadMode() && !activeChar.isOlympiadStart())
				{
					// if L2PcInstance is in Olympia and the match isn't already start, send a Server->Client packet ActionFailed
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}

				if (L2Character.isInsidePeaceZone(pet, target))
				{
					activeChar.sendPacket(SystemMessageId.TARGET_IN_PEACEZONE);
					return;
				}

				if (pet.getNpcId() == 12564 || pet.getNpcId() == 12621)
				{
					// sin eater and wyvern can't attack with attack button
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}

				if (target.isAutoAttackable(activeChar) || _ctrlPressed)
				{
					if (target instanceof L2DoorInstance)
					{
						if (((L2DoorInstance) target).isAttackable(activeChar) && pet.getNpcId() != L2SiegeSummonInstance.SWOOP_CANNON_ID)
							pet.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
					}
					// siege golem AI doesn't support attacking other than doors at the moment
					else if (pet.getNpcId() != L2SiegeSummonInstance.SIEGE_GOLEM_ID)
						pet.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
				}
				else
				{
					pet.setFollowStatus(false);
					pet.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, target);
				}
			}
			break;
		case 17:
		case 23: // pet - cancel action
			if (pet != null && !pet.isMovementDisabled() && !pet.isOutOfControl())
				pet.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);

			break;
		case 19: // pet unsummon
			if (pet != null && !pet.isOutOfControl())
			{
				//returns pet to control item
				if (pet.isDead())
				{
					activeChar.sendPacket(SystemMessageId.DEAD_PET_CANNOT_BE_RETURNED);
				}
				else if (pet.isAttackingNow() || pet.isInCombat() || pet.isRooted() || pet.isBetrayed())
				{
					activeChar.sendPacket(SystemMessageId.PET_CANNOT_SENT_BACK_DURING_BATTLE);
				}
				else
				{
					// if it is a pet and not a summon
					if (pet instanceof L2PetInstance)
					{
						if (!pet.isHungry())
						{
							if (pet.isInCombat())
								activeChar.sendPacket(SystemMessageId.PET_CANNOT_SENT_BACK_DURING_BATTLE);
							else
								pet.unSummon(activeChar);
						}
						else
							activeChar.sendPacket(SystemMessageId.YOU_CANNOT_RESTORE_HUNGRY_PETS);
					}
				}
			}
			break;
		case 38: // pet mount
			// mount
			activeChar.mountPlayer(pet);
			break;
		case 28:
			activeChar.tryOpenPrivateBuyStore();
			break;
		case 32: // Wild Hog Cannon - Mode Change
			useSkill(4230);
			break;
		case 36: // Soulless - Toxic Smoke
			useSkill(4259);
			break;
		case 37: // Manufacture - Dwarven
			if (activeChar.isAlikeDead())
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			if (activeChar.getPrivateStoreType() != 0)
			{
				activeChar.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
				activeChar.broadcastUserInfo();
			}
			if (activeChar.isSitting())
				activeChar.standUp();

			if (activeChar.getCreateList() == null)
			{
				activeChar.setCreateList(new L2ManufactureList());
			}

			activeChar.sendPacket(new RecipeShopManageList(activeChar, true));
			break;
		case 39: // Soulless - Parasite Burst
			useSkill(4138);
			break;
		case 41: // Wild Hog Cannon - Attack
			useSkill(4230);
			break;
		case 42: // Kai the Cat - Self Damage Shield
			useSkill(4378, activeChar);
			break;
		case 43: // Unicorn Merrow - Hydro Screw
			useSkill(4137);
			break;
		case 44: // Big Boom - Boom Attack
			useSkill(4139, activeChar.getTarget());
			break;
		case 45: // Unicorn Boxer - Master Recharge
			useSkill(4025, activeChar);
			break;
		case 46: // Mew the Cat - Mega Storm Strike
			useSkill(4261);
			break;
		case 47: // Silhouette - Steal Blood
			useSkill(4260);
			break;
		case 48: // Mechanic Golem - Mech. Cannon
			useSkill(4068);
			break;
		case 51: // Manufacture -  non-dwarfen
			// Player shouldn't be able to set stores if he/she is alike dead (dead or fake death)
			if (activeChar.isAlikeDead())
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			if (activeChar.getPrivateStoreType() != 0)
			{
				activeChar.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
				activeChar.broadcastUserInfo();
			}
			if (activeChar.isSitting())
				activeChar.standUp();

			if (activeChar.getCreateList() == null)
				activeChar.setCreateList(new L2ManufactureList());

			activeChar.sendPacket(new RecipeShopManageList(activeChar, false));
			break;
		case 52: // unsummon
			if (pet != null && pet instanceof L2SummonInstance)
			{
				if (pet.isOutOfControl())
					activeChar.sendPacket(SystemMessageId.PET_REFUSING_ORDER);
				else if (pet.isAttackingNow() || pet.isInCombat())
					activeChar.sendPacket(new SystemMessage(SystemMessageId.PET_CANNOT_SENT_BACK_DURING_BATTLE));
				else if (pet.isDead())
					activeChar.sendPacket(SystemMessageId.DEAD_PET_CANNOT_BE_RETURNED);
				else
					pet.unSummon(activeChar);
			}
			break;
		case 53: // move to target
			if (target != null && pet != null && pet != target && !pet.isMovementDisabled() && !pet.isOutOfControl())
			{
				pet.setFollowStatus(false);
				pet.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(target.getX(), target.getY(), target.getZ(), 0));
			}
			break;
		case 54: // move to target hatch/strider
			if (target != null && pet != null && pet != target && !pet.isMovementDisabled() && !pet.isOutOfControl())
			{
				pet.setFollowStatus(false);
				pet.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(target.getX(), target.getY(), target.getZ(), 0));
			}
			break;
		case 61:
			// Private Store Package Sell
			activeChar.tryOpenPrivateSellStore(true);
			break;
		case 64:
			// Teleport bookmark button
		case 65:
			// Bot report Button.
			activeChar.sendMessage("Not implemented yet.");
			break;
		case 96: // Quit Party Command Channel
			_log.info("96 Accessed");
			break;
		case 97: // Request Party Command Channel Info
			//if (!PartyCommandManager.getInstance().isPlayerInChannel(activeChar))
			//return;
			_log.info("97 Accessed");
			//PartyCommandManager.getInstance().getActiveChannelInfo(activeChar);
			break;
		case 1000: // Siege Golem - Siege Hammer
			if (target instanceof L2DoorInstance)
				useSkill(4079);
			break;
		case 1001:
			break;
		case 1003: // Wind Hatchling/Strider - Wild Stun
			useSkill(4710);
			break;
		case 1004: // Wind Hatchling/Strider - Wild Defense
			useSkill(4711, activeChar);
			break;
		case 1005: // Star Hatchling/Strider - Bright Burst
			useSkill(4712);
			break;
		case 1006: // Star Hatchling/Strider - Bright Heal
			useSkill(4713, activeChar);
			break;
		case 1007: // Cat Queen - Blessing of Queen
			useSkill(4699, activeChar);
			break;
		case 1008: // Cat Queen - Gift of Queen
			useSkill(4700, activeChar);
			break;
		case 1009: // Cat Queen - Cure of Queen
			useSkill(4701);
			break;
		case 1010: // Unicorn Seraphim - Blessing of Seraphim
			useSkill(4702, activeChar);
			break;
		case 1011: // Unicorn Seraphim - Gift of Seraphim
			useSkill(4703, activeChar);
			break;
		case 1012: // Unicorn Seraphim - Cure of Seraphim
			useSkill(4704);
			break;
		case 1013: // Nightshade - Curse of Shade
			useSkill(4705);
			break;
		case 1014: // Nightshade - Mass Curse of Shade
			useSkill(4706, activeChar);
			break;
		case 1015: // Nightshade - Shade Sacrifice
			useSkill(4707);
			break;
		case 1016: // Cursed Man - Cursed Blow
			useSkill(4709);
			break;
		case 1017: // Cursed Man - Cursed Strike/Stun
			useSkill(4708);
			break;
		case 1031: // Feline King - Slash
			useSkill(5135);
			break;
		case 1032: // Feline King - Spinning Slash
			useSkill(5136);
			break;
		case 1033: // Feline King - Grip of the Cat
			useSkill(5137);
			break;
		case 1034: // Magnus the Unicorn - Whiplash
			useSkill(5138);
			break;
		case 1035: // Magnus the Unicorn - Tridal Wave
			useSkill(5139);
			break;
		case 1036: // Spectral Lord - Corpse Kaboom
			useSkill(5142);
			break;
		case 1037: // Spectral Lord - Dicing Death
			useSkill(5141);
			break;
		case 1038: // Spectral Lord - Force Curse
			useSkill(5140);
			break;
		case 1039: // Swoop Cannon - Cannon Fodder
			if (!(target instanceof L2DoorInstance))
				useSkill(5110);
			break;
		case 1040: // Swoop Cannon - Big Bang
			if (!(target instanceof L2DoorInstance))
				useSkill(5111);
			break;
		case 1041: // Great Wolf - Bite Attack
			useSkill(5442);
			break;
		case 1042: // Great Wolf - Maul
			useSkill(5444);
			break;
		case 1043: // Great Wolf - Cry of the Wolf
			useSkill(5443);
			break;
		case 1044: // Great Wolf - Awakening
			useSkill(5445);
			break;
		case 1045: // Great Wolf - Howl
			useSkill(5584);
			break;
		// Added by Skatershi
		case 1046: // Strider - Roar
			useSkill(5585);
			break;
		case 1047: // Divine Beast - Bite
			useSkill(5580);
			break;
		case 1048: // Divine Beast - Stun Attack
			useSkill(5581);
			break;
		case 1049: // Divine Beast - Fire Breath
			useSkill(5582);
			break;
		case 1050: // Divine Beast - Roar
			useSkill(5583);
			break;
		case 1051: // Bless the Body (Feline Queen)
			useSkill(5638);
			break;
		case 1052: // Bless the Soul (Feline Queen)
			useSkill(5639);
			break;
		case 1053: // Haste (Feline Queen)
			useSkill(5640);
			break;
		case 1054: // Acumen (Seraphim Unicorn)
			useSkill(5643);
			break;
		case 1055: // Clarity (Seraphim Unicorn)
			useSkill(5647);
			break;
		case 1056: // Empower (Seraphim Unicorn)
			useSkill(5648);
			break;
		case 1057: // Wild Magic (Seraphim Unicorn)
			useSkill(5646);
			break;
		case 1058: // Death Whisper (Nightshade)
			useSkill(5652);
			break;
		case 1059: // Focus (Nightshade)
			useSkill(5653);
			break;
		case 1060: // Guidance (Nightshade)
			useSkill(5654);
			break;
		default:
			_log.warn(activeChar.getName() + ": unhandled action type " + _actionId);
		}
	}

	/*
	 * Cast a skill for active pet/servitor.
	 * Target is specified as a parameter but can be
	 * overwrited or ignored depending on skill type.
	 */
	private void useSkill(int skillId, L2Object target)
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		L2Summon activeSummon = activeChar.getPet();

		if (activeChar.getPrivateStoreType() != 0)
		{
			activeChar.sendMessage("Cannot use skills while trading");
			return;
		}

		if (activeSummon != null && !activeSummon.isOutOfControl())
		{
			if (activeSummon instanceof L2PetInstance && (activeSummon.getLevel() - activeChar.getLevel() > 20))
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.PET_TOO_HIGH_TO_CONTROL));
				return;
			}

			int lvl = PetSkillsTable.getInstance().getAvailableLevel(activeSummon, skillId);
			if (lvl == 0)
				return;

			L2Skill skill = SkillTable.getInstance().getInfo(skillId, lvl);
			if (skill == null)
				return;

			activeSummon.setTarget(target);
			activeSummon.useMagic(skill, _ctrlPressed, _shiftPressed);
		}
	}

	/*
	 * Cast a skill for active pet/servitor.
	 * Target is retrieved from owner' target,
	 * then validated by overloaded method useSkill(int, L2Character).
	 */
	private void useSkill(int skillId)
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		useSkill(skillId, activeChar.getTarget());
	}

	@Override
	public String getType()
	{
		return _C__45_REQUESTACTIONUSE;
	}
}
