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
package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.tools.random.Rnd;

/**
 * @author  chris
 */
public class DoorKey implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		// Pagan temple
		8273, 8274, 8275,
		// Key of Splendor Room
		8056,
		// Key of Enigma
		8060
	};
	public static final int INTERACTION_DISTANCE = 100;
	
	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		
		int itemId = item.getItemId();
		if (!(playable instanceof L2PcInstance)) return;
		L2PcInstance activeChar = (L2PcInstance) playable;
		L2Object target = activeChar.getTarget();
		
		if (target == null || !(target instanceof L2DoorInstance))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		L2DoorInstance door = (L2DoorInstance)target;
		
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
			case 8273: //AnteroomKey
			{
				if (door.getDoorName().startsWith("Anteroom"))
				{
					if (!playable.destroyItem("Consume", item.getObjectId(), 1, null, false)) return;

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
						PlaySound playSound = new PlaySound("interfacesound.system_close_01");
						activeChar.sendPacket(playSound);
					}
				}
				else
				{
					activeChar.sendMessage("Incorrect Door.");
				}
				break;
			}

			case 8274: //Chapelkey, Capel Door has a Gatekeeper?? I use this key for Altar Entrance
			{
				if (door.getDoorName().startsWith("Altar_Entrance"))
				{
					if (!playable.destroyItem("Consume", item.getObjectId(), 1, null, false)) return;

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
						PlaySound playSound = new PlaySound("interfacesound.system_close_01");
						activeChar.sendPacket(playSound);
					}
				}
				else
				{
					activeChar.sendMessage("Incorrect Door.");
				}
				break;
			}

			case 8275: //Key of Darkness
			{
				if (door.getDoorName().startsWith("Door_of_Darkness"))
				{
					if (!playable.destroyItem("Consume", item.getObjectId(), 1, null, false)) return;

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
						PlaySound playSound = new PlaySound("interfacesound.system_close_01");
						activeChar.sendPacket(playSound);
					}
				}
				else
				{
					activeChar.sendMessage("Incorrect Door.");
				}
			}
			break;

			case 8056: //Splendor room
			{
				if ((door.getDoorId() != 23150003 && door.getDoorId() != 23150004) || door.getOpen() == 0)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				if (!playable.destroyItem("Consume", item.getObjectId(), 1, null, false)) return;

				door.openMe();
				door.onOpen(); // Auto close
				break;
			}

			case 8060:
			{
				L2Skill skill = SkillTable.getInstance().getInfo(2260,1);
				if(skill != null)
					activeChar.doCast(skill);
				break;
			}
		}
	}
	
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
