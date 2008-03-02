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

import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SplendorKey implements IItemHandler
{
	protected static Log		_log					= LogFactory.getLog(SplendorKey.class);

	public static final int		INTERACTION_DISTANCE	= 100;

	private static final int[]	ITEM_IDS				=
														{ 8056 };

	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;

		L2PcInstance activeChar = (L2PcInstance) playable;
		L2Object target = activeChar.getTarget();

		if (target == null || !(target instanceof L2DoorInstance))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			activeChar.sendPacket(new ActionFailed());
		}
		else
		{
			L2DoorInstance door = (L2DoorInstance) target;
			if (door.getDoorId() == 23150003 || door.getDoorId() == 23150004)
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
				activeChar.sendPacket(new ActionFailed());
				return;
			}
			else if (door.getOpen()==1)
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
				activeChar.sendPacket(new ActionFailed());
				return;
			}
			// Remove the item from inventory.
			activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
			door.openMe();
			ThreadPoolManager.getInstance().scheduleGeneral(new CloseDoor(door), 30000);
		}
	}

	public int[] getItemIds()
	{
		return ITEM_IDS;
	}

	private class CloseDoor implements Runnable
	{
		private L2DoorInstance	_door;

		public CloseDoor(L2DoorInstance door)
		{
			_door = door;
		}

		public void run()
		{
			try
			{
				_door.closeMe();
			}
			catch (Exception e)
			{
				_log.warn(e.getMessage());
			}
		}
	}
}
