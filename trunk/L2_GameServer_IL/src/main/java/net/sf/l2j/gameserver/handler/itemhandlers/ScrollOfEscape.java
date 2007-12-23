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
package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.instancemanager.ZoneManager;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.mapregion.TeleportWhereType;
import net.sf.l2j.gameserver.model.zone.ZoneEnum.ZoneType;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class ...
 * 
 * @version $Revision: 1.2.2.3.2.5 $ $Date: 2005/03/27 15:30:07 $
 */

public class ScrollOfEscape implements IItemHandler
{
	protected static Log _log = LogFactory.getLog(ScrollOfEscape.class);

	// all the items ids that this handler knowns
	private static final int[] ITEM_IDS =
	{ 736, 1830, 1829, 1538, 3958, 5858, 5859, 7117, 7118, 7119, 7120, 7121, 7122, 7123, 7124, 7125, 7126, 7127, 7128, 7129,
			7130, 7131, 7132, 7133, 7134, 7135, 7554, 7555, 7556, 7557, 7558, 7559, 7618, 7619 };

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.l2j.gameserver.handler.IItemHandler#useItem(net.sf.l2j.gameserver.model.L2PcInstance,
	 *      net.sf.l2j.gameserver.model.L2ItemInstance)
	 */
	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		L2PcInstance activeChar = (L2PcInstance) playable;

		if (activeChar.isMovementDisabled() || activeChar.isMuted() || activeChar.isAlikeDead()
				|| activeChar.isAllSkillsDisabled())
			return;

		// [L2J_JP ADD]
		if (ZoneManager.getInstance().checkIfInZone(ZoneType.NoEscape, activeChar) || ZoneManager.getInstance().checkIfInZone(ZoneType.MonsterDerbyTrack, activeChar))
		{
			activeChar.sendMessage("You can not escape from here.");
			activeChar.sendPacket(new ActionFailed());
			return;
		}

		if (activeChar.isSitting())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_MOVE_SITTING));
			return;
		}

		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
			return;
		}

		// Check to see if the player is in a festival.
		if (activeChar.isFestivalParticipant())
		{
			activeChar.sendMessage("You may not use an escape skill in a festival.");
			return;
		}

		// Check to see if the current player is in TvT , CTF or ViP events.
		if (activeChar._inEventCTF || activeChar._inEventTvT || activeChar._inEventVIP)
		{
			activeChar.sendMessage("You may not use an escape skill in a Event.");
			return;
		}

		// Check to see if player is in jail
		if (activeChar.isInJail() || ZoneManager.getInstance().checkIfInZone(ZoneType.Jail, activeChar))
		{
			activeChar.sendMessage("You can not escape from jail.");
			return;
		}

		if (activeChar.inObserverMode())
		{
			activeChar.sendMessage("You can not use Scroll of Escape during Observation Mode.");
			return;
		}

		// Check to see if player is in a duel
		if (activeChar.isInDuel())
		{
			activeChar.sendMessage("You cannot use escape skills during a duel.");
			return;
		}

		// activeChar.abortCast();
		activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		// SoE Animation section
		activeChar.setTarget(activeChar);

		// Modified by Tempy - 28 Jul 05 \\
		// Check if this is a blessed scroll, if it is then shorten the cast
		// time.
		int itemId = item.getItemId();
		int escapeSkill = (itemId == 1538 || itemId == 5858 || itemId == 5859 || itemId == 3958) ? 2036 : 2013;

		if (!activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false))
			return;

		activeChar.disableAllSkills();

		L2Skill skill = SkillTable.getInstance().getInfo(escapeSkill, 1);
		MagicSkillUser msu = new MagicSkillUser(activeChar, escapeSkill, 1, skill.getHitTime(), 0);
		activeChar.broadcastPacket(msu);
		SetupGauge sg = new SetupGauge(0, skill.getHitTime());
		activeChar.sendPacket(sg);
		// End SoE Animation section

		SystemMessage sm = new SystemMessage(SystemMessageId.S1_DISAPPEARED);
		sm.addItemName(item);
		activeChar.sendPacket(sm);

		EscapeFinalizer ef = new EscapeFinalizer(activeChar, itemId);
		// continue execution later
		activeChar.setSkillCast(ThreadPoolManager.getInstance().scheduleEffect(ef, skill.getHitTime()));
		activeChar.setSkillCastEndTime(10 + GameTimeController.getGameTicks() + skill.getHitTime()
				/ GameTimeController.MILLIS_IN_TICK);
	}

	static class EscapeFinalizer implements Runnable
	{
		private L2PcInstance _activeChar;

		private int _itemId;

		EscapeFinalizer(L2PcInstance activeChar, int itemId)
		{
			_activeChar = activeChar;
			_itemId = itemId;
		}

		public void run()
		{
			if (_activeChar.isDead())
				return;
			_activeChar.enableAllSkills();

			_activeChar.setIsIn7sDungeon(false);

			try
			{
				// escape to castle own's one
				if ((_itemId == 1830 || _itemId == 5859)
						&& CastleManager.getInstance().getCastleByOwner(_activeChar.getClan()) != null)
				{
					_activeChar.teleToLocation(TeleportWhereType.Castle);
				}
				// escape to clan hall if own's one
				else if ((_itemId == 1829 || _itemId == 5858) && _activeChar.getClan() != null
						&& ClanHallManager.getInstance().getClanHallByOwner(_activeChar.getClan()) != null)
				{
					_activeChar.teleToLocation(TeleportWhereType.ClanHall);
				}
				else if (_itemId == 5858) // do nothing
				{
					_activeChar.sendPacket(new SystemMessage(SystemMessageId.CLAN_HAS_NO_CLAN_HALL));
					return;
				}
				else if (_itemId == 5859) // do nothing
				{
					_activeChar.sendMessage("The clan does not own a castle.");
					return;
				}
				else
				{
					if (_itemId < 7117)
						_activeChar.teleToLocation(TeleportWhereType.Town);
					else
					{
						switch (_itemId)
						{
						// Talking Island
						case 7117:
							_activeChar.teleToLocation(-84318, 244579, -3730, true);
							break;
						// Talking Island quest scroll
						case 7554:
							_activeChar.teleToLocation(-84318, 244579, -3730, true); 
							break;
						// Elven Village
						case 7118:
							_activeChar.teleToLocation(46934, 51467, -2977, true);
							break;
						// Elven Village quest scroll
						case 7555:
							_activeChar.teleToLocation(46934, 51467, -2977, true); 
							break;
						// Dark Elven Village
						case 7119:
							_activeChar.teleToLocation(9745, 15606, -4574, true);
							break;
						// Dark Elven Village quest scroll
						case 7556:
							_activeChar.teleToLocation(9745, 15606, -4574, true);
							break;
						// Orc Village
						case 7120:
							_activeChar.teleToLocation(-44836, -112524, -235, true);
							break;
						// Orc Village quest scroll
						case 7557:
							_activeChar.teleToLocation(-44836, -112524, -235, true);
							break;
						// Dwarven Village
						case 7121:
							_activeChar.teleToLocation(115113, -178212, -901, true);
							break;
						// Dwarven Village quest scroll
						case 7558:
							_activeChar.teleToLocation(115113, -178212, -901, true);
							break;
						// Gludin Village
						case 7122:
							_activeChar.teleToLocation(-80826, 149775, -3043, true);
							break;
						// Gludio Castle Town
						case 7123:
							_activeChar.teleToLocation(-12678, 122776, -3116, true);
							break;
						// Dion Castle Town
						case 7124:
							_activeChar.teleToLocation(15670, 142983, -2705, true);
							break;
						// Floran Village
						case 7125:
							_activeChar.teleToLocation(17836, 170178, -3507, true);
							break;
						// Giran Castle Town
						case 7126:
							_activeChar.teleToLocation(83400, 147943, -3404, true);
							break;
						// Giran Castle Town quest scroll
						case 7559:
							_activeChar.teleToLocation(83400, 147943, -3404, true);
							break;
						// Hardin's Private Academy
						case 7127:
							_activeChar.teleToLocation(105918, 109759, -3207, true);
							break;
						// Heine
						case 7128:
							_activeChar.teleToLocation(111409, 219364, -3545, true);
							break;
						// Oren Castle Town
						case 7129:
							_activeChar.teleToLocation(82956, 53162, -1495, true);
							break;
						// Ivory Tower
						case 7130:
							_activeChar.teleToLocation(85348, 16142, -3699, true);
							break;
						// Hunters Village
						case 7131:
							_activeChar.teleToLocation(116819, 76994, -2714, true);
							break;
						// Aden Castle Town
						case 7132:
							_activeChar.teleToLocation(146331, 25762, -2018, true);
							break;
						// Goddard Castle Town
						case 7133:
							_activeChar.teleToLocation(147928, -55273, -2734, true);
							break;
						// Rune Castle Town
						case 7134:
							_activeChar.teleToLocation(43799, -47727, -798, true);
							break;
						// Schuttgart Castle Town
						case 7135:
							_activeChar.teleToLocation(87331, -142842, -1317, true);
							break;
						// Ketra Orc Village
						case 7618:
							_activeChar.teleToLocation(149864, -81062, -5618, true);
							break;
						// Varka Silenos Village
						case 7619:
							_activeChar.teleToLocation(108275, -53785, -2524, true);
							break;
						// To nearest town
						default:
							_activeChar.teleToLocation(TeleportWhereType.Town);
							break;
						}
					}
				}
			}
			catch (Throwable e)
			{
				_log.error(e.getMessage(), e);
			}
		}
	}

	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
