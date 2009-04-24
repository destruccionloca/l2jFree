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

import com.l2jfree.gameserver.datatables.ShotTable;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2Summon;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.templates.item.L2Item;

public final class RequestAutoSoulShot extends L2GameClientPacket
{
	private static final String _C__CF_REQUESTAUTOSOULSHOT = "[C] CF RequestAutoSoulShot";
	
	// format  cd
	private int _shotId;
	private int _type; // 1 = on, 0 = off
	
	/**
	 * packet type id 0xcf format: chdd
	 */
	@Override
	protected void readImpl()
	{
		_shotId = readD();
		_type = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getActiveChar();
		if (activeChar == null)
			return;
		
		if (activeChar.getPrivateStoreType() == 0 && activeChar.getActiveRequester() == null && !activeChar.isDead())
		{
			if (_type == 1)
			{
				//Fishingshots are not automatic on retail, but who the fuck cares? ;)
				//if (ShotTable.getInstance().isFishingShot(_shotId))
				//	return;
				
				L2ItemInstance shot = activeChar.getInventory().getItemByItemId(_shotId);
				if (shot == null)
					return;
				
				if (ShotTable.getInstance().isBeastShot(_shotId))
				{
					L2Summon summon = activeChar.getPet();
					if (summon != null)
					{
						activeChar.getShots().addAutoSoulShot(_shotId);
						summon.rechargeShot();
					}
				}
				else
				{
					int weaponGrade = activeChar.getActiveWeaponItem().getCrystalType();
					if (weaponGrade == L2Item.CRYSTAL_S80)
						weaponGrade = L2Item.CRYSTAL_S;
					
					if (activeChar.getActiveWeaponItem() != activeChar.getFistsWeaponItem()
						&& shot.getItem().getCrystalType() == weaponGrade)
					{
						activeChar.getShots().addAutoSoulShot(_shotId);
						activeChar.rechargeShot();
					}
					else
					{
						if (ShotTable.getInstance().isMagicShot(_shotId))
							activeChar.sendPacket(SystemMessageId.SPIRITSHOTS_GRADE_MISMATCH);
						else
							activeChar.sendPacket(SystemMessageId.SOULSHOTS_GRADE_MISMATCH);
					}
				}
			}
			else if (_type == 0)
			{
				activeChar.getShots().removeAutoSoulShot(_shotId);
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _C__CF_REQUESTAUTOSOULSHOT;
	}
}
