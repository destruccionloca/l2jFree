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

import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ExPutEnchantTargetItemResult;
import com.l2jfree.gameserver.network.serverpackets.RequestEnchant;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.templates.L2Item;
import com.l2jfree.gameserver.templates.L2WeaponType;

/** 
 * @author evill33t
 * 
 */
public class RequestExTryToPutEnchantTargetItem extends L2GameClientPacket
{
	private static final String	_C__D0_78_REQUESTEXTRYTOPUTENCHANTTARGETITEM	= "[C] D0 4F RequestExTryToPutEnchantTargetItem";

	private int _objectId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar != null)
		{
			L2ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_objectId);
			L2ItemInstance enchantScroll = activeChar.getActiveEnchantItem();

			if (targetItem == null || enchantScroll == null)
				return;
			
			if (targetItem.isEtcItem() || targetItem.isWear() || targetItem.getItem().getItemType() == L2WeaponType.ROD || targetItem.isHeroItem() || targetItem.getItemId() >= 7816 && targetItem.getItemId() <= 7831
					|| targetItem.isShadowItem() || targetItem.getItem().isCommonItem())
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.DOES_NOT_FIT_SCROLL_CONDITIONS));
				activeChar.setActiveEnchantItem(null);
				activeChar.sendPacket(new ExPutEnchantTargetItemResult(2));
				return;
			}

			int itemType2 = targetItem.getItem().getType2();
			boolean enchantItem = false;

			/** pretty code ;D */
			switch (targetItem.getItem().getCrystalType())
			{
				case L2Item.CRYSTAL_A:
					switch (enchantScroll.getItemId())
					{
						case 729:
						case 731:
						case 6569:
							if (itemType2 == L2Item.TYPE2_WEAPON)
								enchantItem = true;
							break;
						case 730:
						case 732:
						case 6570:
							if (itemType2 == L2Item.TYPE2_SHIELD_ARMOR || itemType2 == L2Item.TYPE2_ACCESSORY)
								enchantItem = true;
							break;
					}
					break;
				case L2Item.CRYSTAL_B:
					switch (enchantScroll.getItemId())
					{
						case 947:
						case 949:
						case 6571:
							if (itemType2 == L2Item.TYPE2_WEAPON)
								enchantItem = true;
							break;
						case 948:
						case 950:
						case 6572:
							if (itemType2 == L2Item.TYPE2_SHIELD_ARMOR || itemType2 == L2Item.TYPE2_ACCESSORY)
								enchantItem = true;
							break;
					}
					break;
				case L2Item.CRYSTAL_C:
					switch (enchantScroll.getItemId())
					{
						case 951:
						case 953:
						case 6573:
							if (itemType2 == L2Item.TYPE2_WEAPON)
								enchantItem = true;
							break;
						case 952:
						case 954:
						case 6574:
							if (itemType2 == L2Item.TYPE2_SHIELD_ARMOR || itemType2 == L2Item.TYPE2_ACCESSORY)
								enchantItem = true;
							break;
					}
					break;
				case L2Item.CRYSTAL_D:
					switch (enchantScroll.getItemId())
					{
						case 955:
						case 957:
						case 6575:
							if (itemType2 == L2Item.TYPE2_WEAPON)
								enchantItem = true;
							break;
						case 956:
						case 958:
						case 6576:
							if (itemType2 == L2Item.TYPE2_SHIELD_ARMOR || itemType2 == L2Item.TYPE2_ACCESSORY)
								enchantItem = true;
							break;
					}
					break;
				case L2Item.CRYSTAL_S:
				case L2Item.CRYSTAL_S80:
					switch (enchantScroll.getItemId())
					{
						case 959:
						case 961:
						case 6577:
							if (itemType2 == L2Item.TYPE2_WEAPON)
								enchantItem = true;
							break;
						case 960:
						case 962:
						case 6578:
							if (itemType2 == L2Item.TYPE2_SHIELD_ARMOR || itemType2 == L2Item.TYPE2_ACCESSORY)
								enchantItem = true;
							break;
					}
					break;
			}
			if (!enchantItem)
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.DOES_NOT_FIT_SCROLL_CONDITIONS));
				activeChar.setActiveEnchantItem(null);
				activeChar.sendPacket(new ExPutEnchantTargetItemResult(2));
				return;
			}
			activeChar.sendPacket(new RequestEnchant(1));
		}
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__D0_78_REQUESTEXTRYTOPUTENCHANTTARGETITEM;
	}
}
