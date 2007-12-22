/*
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * http://www.gnu.org/copyleft/gpl.html
 */

package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class TransformationItems implements IItemHandler
{
	private static final int	ITEM_IDS[]	= { 9648, 9649, 9650, 9651, 9652, 9653, 9654, 9655, 9897, 10131, 10132, 10133, 10134, 10135, 10136, 10137, 10138,
			10151, 10274					};
	
	public TransformationItems()
	{
	}
	
	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		L2PcInstance client = (L2PcInstance) playable;
		int itemId = item.getItemId();
		if (client.getPet() != null || client.isTransformed())
		{
			client.sendPacket((new SystemMessage(113)).addItemName(itemId));
			return;
		}
		int skillId = 0;
		
		switch (itemId)
		{
			case 9897:
				skillId = 2370;
				break;
			case 9648:
			case 10131:
				skillId = 2371;
				break;
			case 9649:
			case 10132:
				skillId = 2372;
				break;
			case 9650:
			case 10133:
				skillId = 2373;
				break;
			case 9651:
			case 10134:
				skillId = 2374;
				break;
			case 9652:
			case 10135:
				skillId = 2375;
				break;
			case 9653:
			case 10136:
				skillId = 2376;
				break;
			case 9654:
			case 10137:
				skillId = 2377;
				break;
			case 9655:
			case 10138:
				skillId = 2378;
				break;
			case 10151:
				skillId = 2394;
				break;
			case 10274:
				skillId = 2428;
				break;
			default:
				break;
		}
		
		L2Skill skill = SkillTable.getInstance().getInfo(skillId, 1);
		client.callSkill(skill, new L2Character[] { client });
		
		client.destroyItem("Consume", item.getObjectId(), 1, null, true);
	}
	
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
