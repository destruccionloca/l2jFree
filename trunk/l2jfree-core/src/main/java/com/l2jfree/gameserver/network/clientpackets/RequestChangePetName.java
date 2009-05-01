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

import com.l2jfree.Config;
import com.l2jfree.gameserver.datatables.PetNameTable;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Summon;
import com.l2jfree.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.4.4 $ $Date: 2005/04/06 16:13:48 $
 */
public class RequestChangePetName extends L2GameClientPacket
{
	private static final String REQUESTCHANGEPETNAME__C__89 = "[C] 89 RequestChangePetName";
	//private final static Log _log = LogFactory.getLog(RequestChangePetName.class.getName());
	
	private String _name;
	
    @Override
    protected void readImpl()
    {
        _name = readS();
    }

    @Override
    protected void runImpl()
	{
		L2Character activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		final L2Summon pet = activeChar.getPet(); 
		if (pet == null)
			return;

		if (pet.getName() != null && pet.getName().trim().length() != 0)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.NAMING_YOU_CANNOT_SET_NAME_OF_THE_PET));
			return;
		}
		else if (PetNameTable.getInstance().doesPetNameExist(_name, pet.getTemplate().getNpcId()))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.NAMING_ALREADY_IN_USE_BY_ANOTHER_PET));
			return;
		}
        else if (_name.length() < 3 || _name.length() > 16)
		{
			activeChar.sendMessage("Your pet's name can be up to 16 characters.");
			return;
		}
        else if (!Config.CLAN_ALLY_NAME_PATTERN.matcher(_name).matches())
		{
        	activeChar.sendPacket(new SystemMessage(SystemMessageId.NAMING_PETNAME_CONTAINS_INVALID_CHARS));
			return;
		}
		
		pet.setName(_name);
		pet.broadcastFullInfo();
		
		// set the flag on the control item to say that the pet has a name
		if (pet instanceof L2PetInstance)
		{
			L2ItemInstance controlItem = pet.getOwner().getInventory().getItemByObjectId(pet.getControlItemId());
			if (controlItem != null)
			{
				controlItem.setCustomType2(1);
				controlItem.updateDatabase();
				InventoryUpdate iu = new InventoryUpdate();
				iu.addModifiedItem(controlItem);
				activeChar.sendPacket(iu);
			}
		}
	}
	
	@Override
	public String getType()
	{
		return REQUESTCHANGEPETNAME__C__89;
	}
}
