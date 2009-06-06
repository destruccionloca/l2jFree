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
package com.l2jfree.gameserver.model.actor.shot;

import java.util.Set;

import com.l2jfree.gameserver.datatables.ShotTable;
import com.l2jfree.gameserver.handler.ItemHandler;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ExAutoSoulShot;
import com.l2jfree.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.skills.Stats;
import com.l2jfree.gameserver.templates.item.L2Weapon;
import com.l2jfree.gameserver.templates.item.L2WeaponType;
import com.l2jfree.util.SingletonSet;

/**
 * @author NB4L1
 */
public final class PcShots extends CharShots
{
	private static final int[][] SKILL_IDS;
	private static final int[][] SHOT_IDS;
	
	static
	{
		SKILL_IDS = new int[ShotType.values().length][];
		
		SKILL_IDS[ShotType.SOUL.ordinal()] = new int[] { 2039, 2150, 2151, 2152, 2153, 2154, 2154 };
		SKILL_IDS[ShotType.SPIRIT.ordinal()] = new int[] { 2061, 2155, 2156, 2157, 2158, 2159, 2159 };
		SKILL_IDS[ShotType.BLESSED_SPIRIT.ordinal()] = new int[] { 2061, 2160, 2161, 2162, 2163, 2164, 2164 };
		SKILL_IDS[ShotType.FISH.ordinal()] = new int[] { 2181, 2182, 2183, 2184, 2185, 2186, 2186 };
		
		SHOT_IDS = new int[ShotType.values().length][];
		
		SHOT_IDS[ShotType.SOUL.ordinal()] = new int[] { 1835, 1463, 1464, 1465, 1466, 1467, 1467 };
		SHOT_IDS[ShotType.SPIRIT.ordinal()] = new int[] { 2509, 2510, 2511, 2512, 2513, 2514, 2514 };
		SHOT_IDS[ShotType.BLESSED_SPIRIT.ordinal()] = new int[] { 3947, 3948, 3949, 3950, 3951, 3952, 3952 };
		SHOT_IDS[ShotType.FISH.ordinal()] = new int[] { 6535, 6536, 6537, 6538, 6539, 6540, 6540 };
	}
	
	private final Set<Integer> _activeSoulShots = new SingletonSet<Integer>().setShared();
	
	public PcShots(L2PcInstance activeChar)
	{
		super(activeChar);
	}
	
	public void addAutoSoulShot(int itemId)
	{
		if (_activeSoulShots.add(itemId))
		{
			getActiveChar().sendPacket(new ExAutoSoulShot(itemId, 1));
			getActiveChar().sendPacket(new SystemMessage(SystemMessageId.USE_OF_S1_WILL_BE_AUTO).addItemName(itemId));
		}
	}
	
	public void removeAutoSoulShot(int itemId)
	{
		if (_activeSoulShots.remove(itemId))
		{
			getActiveChar().sendPacket(new ExAutoSoulShot(itemId, 0));
			getActiveChar().sendPacket(new SystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED).addItemName(itemId));
		}
	}
	
	public boolean hasAutoSoulShot(int itemId)
	{
		return _activeSoulShots.contains(itemId);
	}
	
	public Set<Integer> getAutoSoulShots()
	{
		return _activeSoulShots;
	}
	
	@Override
	protected L2PcInstance getActiveChar()
	{
		return (L2PcInstance)_activeChar;
	}
	
	@Override
	protected ShotState getShotState()
	{
		L2ItemInstance weaponInst = getActiveChar().getActiveWeaponInstance();
		
		if (weaponInst != null)
			return weaponInst.getShotState();
		
		return ShotState.getEmptyInstance();
	}
	
	@Override
	public void rechargeShots()
	{
		/**
		 * Clears shot state, if it was marked to be recharged.<br>
		 * This has to be done to avoid being charged forever, because we don't call the recharge directly.<br>
		 * It's called through itemhandlers, and that's called only if the shot is automatic.
		 */
		chargeSoulshot(null);
		chargeSpiritshot(null);
		chargeBlessedSpiritshot(null);
		chargeFishshot(null);
		
		for (int itemId : getAutoSoulShots())
		{
			if (ShotTable.getInstance().isPcShot(itemId))
			{
				L2ItemInstance item = getActiveChar().getInventory().getItemByItemId(itemId);
				
				ItemHandler.getInstance().useItem(itemId, getActiveChar(), item);
				
				if (item == null)
					removeAutoSoulShot(itemId);
			}
		}
	}
	
	@Override
	protected boolean canChargeSoulshot(L2ItemInstance item)
	{
		L2Weapon weaponItem = getActiveChar().getActiveWeaponItem();
		
		int saSSCount = (int)getActiveChar().getStat().calcStat(Stats.SOULSHOT_COUNT, 0, null, null);
		
		if (!canCharge(ShotType.SOUL, weaponItem, item, saSSCount == 0 ? weaponItem.getSoulShotCount() : saSSCount))
			return false;
		
		if (saSSCount > 0)
			getActiveChar().sendMessage("Miser consumed only " + saSSCount + " soulshots.");
		
		return true;
	}
	
	@Override
	protected boolean canChargeSpiritshot(L2ItemInstance item)
	{
		L2Weapon weaponItem = getActiveChar().getActiveWeaponItem();
		
		return canCharge(ShotType.SPIRIT, weaponItem, item, weaponItem.getSpiritShotCount());
	}
	
	@Override
	protected boolean canChargeBlessedSpiritshot(L2ItemInstance item)
	{
		L2Weapon weaponItem = getActiveChar().getActiveWeaponItem();
		
		return canCharge(ShotType.BLESSED_SPIRIT, weaponItem, item, weaponItem.getSpiritShotCount());
	}
	
	@Override
	protected boolean canChargeFishshot(L2ItemInstance item)
	{
		L2Weapon weaponItem = getActiveChar().getActiveWeaponItem();
		
		if (weaponItem.getItemType() != L2WeaponType.ROD)
			return false;
		
		return canCharge(ShotType.FISH, weaponItem, item, 1);
	}
	
	private boolean canCharge(ShotType type, L2Weapon weapon, L2ItemInstance item, int count)
	{
		if (item == null)
			return false;
		
		L2PcInstance activeChar = getActiveChar();
		
		if (type == ShotType.BLESSED_SPIRIT)
		{
			// Blessed Spiritshot cannot be used in olympiad.
			if (activeChar.isInOlympiadMode())
			{
				activeChar.sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
				return false;
			}
		}
		
		if (count == 0)
		{
			if (!hasAutoSoulShot(item.getItemId()))
			{
				if (type == ShotType.SOUL)
					activeChar.sendPacket(SystemMessageId.CANNOT_USE_SOULSHOTS);
				else if (type != ShotType.FISH)
					activeChar.sendPacket(SystemMessageId.CANNOT_USE_SPIRITSHOTS);
			}
			
			return false;
		}
		
		int grade = weapon.getCrystalType();
		int shotId = item.getItemId();
		
		// Beginner's Soulshot
		if (shotId == 5789)
			shotId = 1835;
		
		// Beginner's Spiritshot
		if (shotId == 5790)
			shotId = 2509;
		
		if (SHOT_IDS[type.ordinal()][grade] != shotId)
		{
			if (!hasAutoSoulShot(item.getItemId()))
			{
				if (type == ShotType.SOUL)
					activeChar.sendPacket(SystemMessageId.SOULSHOTS_GRADE_MISMATCH);
				else if (type == ShotType.FISH)
					activeChar.sendPacket(SystemMessageId.WRONG_FISHINGSHOT_GRADE);
				else
					activeChar.sendPacket(SystemMessageId.SPIRITSHOTS_GRADE_MISMATCH);
			}
			
			return false;
		}
		
		if (!activeChar.destroyItemWithoutTrace("Consume", item.getObjectId(), count, null, false))
		{
			if (type == ShotType.SOUL)
				activeChar.sendPacket(SystemMessageId.ENABLED_SOULSHOT);
			else if (type != ShotType.FISH)
				activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_SPIRITSHOTS);
			
			removeAutoSoulShot(item.getItemId());
			return false;
		}
		
		if (type == ShotType.SOUL)
			activeChar.sendPacket(SystemMessageId.ENABLED_SOULSHOT);
		else if (type != ShotType.FISH)
			activeChar.sendPacket(SystemMessageId.ENABLED_SPIRITSHOT);
		
		activeChar.broadcastPacket(new MagicSkillUse(activeChar, SKILL_IDS[type.ordinal()][grade], 1, 0, 0));
		return true;
	}
}
