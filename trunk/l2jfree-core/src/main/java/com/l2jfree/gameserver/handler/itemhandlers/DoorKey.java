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
package com.l2jfree.gameserver.handler.itemhandlers;

import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.handler.IItemHandler;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.L2Playable;
import com.l2jfree.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.PlaySound;
import com.l2jfree.gameserver.network.serverpackets.SocialAction;
import com.l2jfree.tools.random.Rnd;

/**
 * @author chris
 */
public class DoorKey implements IItemHandler
{
	// All the item IDs that this handler knows.
	private static final int[]	ITEM_IDS				=
														{
			// Pagan temple
			8273,
			8274,
			8275,
			// Key of Splendor Room
			8056,
			// Key of Enigma
			8060,
			// Blue Coral Key
			9698,
			// Red Coral Key
			9699,
			// Secret Race Key
			9694										};

	public static final int		INTERACTION_DISTANCE	= 100;

	public void useItem(L2Playable playable, L2ItemInstance item)
	{

		int itemId = item.getItemId();
		if (!(playable instanceof L2PcInstance))
			return;
		L2PcInstance activeChar = (L2PcInstance) playable;

		// Key of Enigma (Pavel Research Quest)
		if (itemId == 8060)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(2260, 1);
			if (skill != null)
				activeChar.doSimultaneousCast(skill);
			return;
		}

		L2Object target = activeChar.getTarget();

		if (!(target instanceof L2DoorInstance))
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		L2DoorInstance door = (L2DoorInstance) target;

		if (!(activeChar.isInsideRadius(door, INTERACTION_DISTANCE, false, false)))
		{
			activeChar.sendMessage("Too far.");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (activeChar.getAbnormalEffect() > 0 || activeChar.isInCombat())
		{
			activeChar.sendMessage("You are currently engaged in combat.");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		int openChance = 35;

		switch (itemId)
		{
		case 8273: // Anteroom Key
		{
			if (door.getDoorName().startsWith("Anteroom"))
			{
				if (!playable.destroyItem("Consume", item.getObjectId(), 1, null, false))
					return;

				if (openChance > 0 && Rnd.get(100) < openChance)
				{
					activeChar.sendMessage("You opened Anterooms Door.");
					door.openMe();
					door.onOpen(); // Closes the door after 60sec
					activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 3));
				}
				else
				{
					//test with: activeChar.sendPacket(new SystemMessage(SystemMessageId.FAILED_TO_UNLOCK_DOOR)); 
					activeChar.sendMessage("You failed to open Anterooms Door.");
					activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 13));
					PlaySound playSound = new PlaySound(0, "interfacesound.system_close_01");
					activeChar.sendPacket(playSound);
				}
			}
			else
			{
				activeChar.sendMessage("Incorrect Door.");
			}
			break;
		}

		case 8274: // Chapel Key, Capel Door has a Gatekeeper?? I use this key for Altar Entrance
		{
			if (door.getDoorName().startsWith("Altar_Entrance"))
			{
				if (!playable.destroyItem("Consume", item.getObjectId(), 1, null, false))
					return;

				if (openChance > 0 && Rnd.get(100) < openChance)
				{
					activeChar.sendMessage("You opened Altar Entrance.");
					door.openMe();
					door.onOpen(); // Auto close
					activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 3));
				}
				else
				{
					activeChar.sendMessage("You failed to open Altar Entrance.");
					activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 13));
					PlaySound playSound = new PlaySound(0, "interfacesound.system_close_01");
					activeChar.sendPacket(playSound);
				}
			}
			else
			{
				activeChar.sendMessage("Incorrect Door.");
			}
			break;
		}

		case 8275: // Key of Darkness
		{
			if (door.getDoorName().startsWith("Door_of_Darkness"))
			{
				if (!playable.destroyItem("Consume", item.getObjectId(), 1, null, false))
					return;

				if (openChance > 0 && Rnd.get(100) < openChance)
				{
					activeChar.sendMessage("You opened Door of Darkness.");
					door.openMe();
					door.onOpen(); // Auto close
					activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 3));
				}
				else
				{
					activeChar.sendMessage("You failed to open Door of Darkness.");
					activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 13));
					PlaySound playSound = new PlaySound(0, "interfacesound.system_close_01");
					activeChar.sendPacket(playSound);
				}
			}
			else
			{
				activeChar.sendMessage("Incorrect Door.");
			}
		}
			break;

		case 8056: // Splendor room
		{
			if ((door.getDoorId() != 23150003 && door.getDoorId() != 23150004) || door.getOpen())
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			if (!playable.destroyItem("Consume", item.getObjectId(), 1, null, false))
				return;

			door.openMe();
			door.onOpen(); // Auto close
			break;
		}

		case 9698: // Sapphire Gate
		{
			if ((door.getDoorId() != 24220020) || door.getOpen())
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			if (!playable.destroyItem("Consume", item.getObjectId(), 1, null, false))
				return;

			door.openMe();
			break;
		}
		case 9699: // Corridor Gate
		{
			if ((door.getDoorId() != 24220022) || door.getOpen())
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			if (!playable.destroyItem("Consume", item.getObjectId(), 1, null, false))
				return;

			door.openMe();
			break;
		}
		case 9694: // Secret Garden Key
		{
			if ((door.getDoorId() != 24220001 && door.getDoorId() != 24220002 && door.getDoorId() != 24220003 && door.getDoorId() != 24220004)
					|| door.getOpen())
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			if (!playable.destroyItem("Consume", item.getObjectId(), 1, null, false))
				return;

			door.openMe();
			break;
		}
		}
	}

	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}