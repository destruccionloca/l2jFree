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
package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GeoData;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.instancemanager.ZoneManager;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.zone.L2Zone;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.tools.random.Rnd;

public class Fishing implements ISkillHandler
{
	// private static Logger _log = Logger.getLogger(SiegeFlag.class.getName());
	// protected SkillType[] _skillIds = {SkillType.FISHING};
	private static final SkillType[] SKILL_IDS =
	{ SkillType.FISHING };

	public void useSkill(L2Character activeChar, @SuppressWarnings("unused")
	L2Skill skill, @SuppressWarnings("unused")
	L2Object[] targets)
	{
		if (activeChar == null || !(activeChar instanceof L2PcInstance))
			return;

		L2PcInstance player = (L2PcInstance) activeChar;

		if (!Config.ALLOW_FISHING)
		{
			player.sendMessage("Fishing is disabled.");
			return;
		}
		if (player.isFishing())
		{
			if (player.getFishCombat() != null)
				player.getFishCombat().doDie(false);
			else
				player.endFishing(false);
			//Cancels fishing
			player.sendPacket(new SystemMessage(SystemMessageId.FISHING_ATTEMPT_CANCELLED));
			return;
		}
		if (player.isInBoat())
		{
			//You can't fish while you are on boat
			player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_FISH_ON_BOAT));
			return;
		}
		if (!player.isInsideZone(L2Zone.FLAG_FISHING) || player.isInsideZone(L2Zone.FLAG_PEACE))
		{
			//You can't fish here
			player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_FISH_HERE));
			return;
		}
		if (player.isInsideZone(L2Zone.FLAG_WATER))
		{
			//You can't fish in water
			player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_FISH_UNDER_WATER));
			return;
		}

		// calculate point of a float
		int d = Rnd.get(50) + 150;
		double angle = Util.convertHeadingToDegree(player.getHeading());
		double radian = Math.toRadians(angle);

		int dx = (int) (d * Math.sin(radian));
		int dy = (int) (d * Math.cos(radian));

		int x = activeChar.getX() - dx;
		int y = activeChar.getY() + dy;

		L2Zone water = ZoneManager.getInstance().isInsideZone(L2Zone.ZoneType.Water, x, y);

		// float must be in water
		if (water == null)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_FISH_HERE));
			return;
		}

		int z = water.getMaxZ(x, y, activeChar.getZ());

		if (Config.GEODATA && !GeoData.getInstance().canSeeTarget(activeChar.getX(), activeChar.getY(), activeChar.getZ(), x, y, z)
				|| (!Config.GEODATA && (Util.calculateDistance(activeChar.getX(), activeChar.getY(), activeChar.getZ(), x, y, z, true) > d * 1.73)))
		{
			player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_FISH_HERE));
			return;
		}
		if (player.isInCraftMode() || player.isInStoreMode())
		{
			player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_FISH_WHILE_USING_RECIPE_BOOK));
			return;
		}
		L2Weapon weaponItem = player.getActiveWeaponItem();
		if ((weaponItem == null || weaponItem.getItemType() != L2WeaponType.ROD))
		{
			//Fishing poles are not installed
			player.sendPacket(new SystemMessage(SystemMessageId.FISHING_POLE_NOT_EQUIPPED));
			return;
		}
		L2ItemInstance lure = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if (lure == null)
		{
			//Bait not equiped.
			player.sendPacket(new SystemMessage(SystemMessageId.BAIT_ON_HOOK_BEFORE_FISHING));
			return;
		}
		player.setLure(lure);
		L2ItemInstance lure2 = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);

		if (lure2 == null || lure2.getCount() < 1) //Not enough bait.
		{
			player.sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_BAIT));
			player.sendPacket(new ItemList(player, false));
		}
		else
		//Has enough bait, consume 1 and update inventory. Start fishing follows.
		{
			lure2 = player.getInventory().destroyItem("Consume", player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND), 1, player, null);
			InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(lure2);
			player.sendPacket(iu);
		}

		// client itself find z coord of a float
		player.startFishing(x, y, z + 10);
	}

	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
