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
package com.l2jfree.gameserver.model.actor.instance;

import java.util.concurrent.ScheduledFuture;

import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.L2Npc;
import com.l2jfree.gameserver.model.zone.L2Zone;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;

/**
 * @author Drunkard Zabb0x Lets drink2code!
 */
public class L2XmassTreeInstance extends L2Npc
{
	public static final int SPECIAL_TREE_ID = 13007;
	
	private ScheduledFuture<?> _aiTask;
	
	private final class XmassAI implements Runnable
	{
		public void run()
		{
			final L2Skill skill = SkillTable.getInstance().getInfo(2139, 1);
			
			if (skill == null || isInsideZone(L2Zone.FLAG_PEACE))
			{
				_aiTask.cancel(false);
				_aiTask = null;
				return;
			}
			
			for (L2PcInstance player : getKnownList().getKnownPlayersInRadius(200))
				if (player.getFirstEffect(skill.getId()) == null)
					skill.getEffects(player, player);
		}
	}
	
	public L2XmassTreeInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		
		if (template.getNpcId() == SPECIAL_TREE_ID)
			_aiTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new XmassAI(), 3000, 3000);
	}
	
	@Override
	public void deleteMe()
	{
		if (_aiTask != null)
			_aiTask.cancel(true);
		
		super.deleteMe();
	}
	
	@Override
	public void onAction(L2PcInstance player, boolean interact)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
}
