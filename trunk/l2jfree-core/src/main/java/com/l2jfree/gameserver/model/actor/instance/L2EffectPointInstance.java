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

import com.l2jfree.gameserver.datatables.NpcTable;
import com.l2jfree.gameserver.idfactory.IdFactory;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.L2Skill.SkillTargetType;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Npc;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;
import com.l2jfree.tools.geometry.Point3D;

public class L2EffectPointInstance extends L2Npc
{
	public static L2EffectPointInstance newInstance(int npcId, L2Character caster, L2Skill skill)
	{
		final int objectId = IdFactory.getInstance().getNextId();
		final L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
		
		final L2EffectPointInstance effectPoint = new L2EffectPointInstance(objectId, template, caster);
		effectPoint.getStatus().setCurrentHp(effectPoint.getMaxHp());
		effectPoint.getStatus().setCurrentMp(effectPoint.getMaxMp());
		L2World.getInstance().storeObject(effectPoint);
		
		int x = caster.getX();
		int y = caster.getY();
		int z = caster.getZ();
		
		if (caster instanceof L2PcInstance && skill.getTargetType() == SkillTargetType.TARGET_GROUND)
		{
			Point3D wordPosition = ((L2PcInstance)caster).getCurrentSkillWorldPosition();
			
			if (wordPosition != null)
			{
				x = wordPosition.getX();
				y = wordPosition.getY();
				z = wordPosition.getZ();
			}
		}
		
		skill.getEffects(caster, effectPoint);
		
		effectPoint.setIsInvul(true);
		effectPoint.setInstanceId(caster.getInstanceId());
		effectPoint.spawnMe(x, y, z);
		
		return effectPoint;
	}
	
	private final L2Character _owner;
	
	public L2EffectPointInstance(int objectId, L2NpcTemplate template, L2Character owner)
	{
		super(objectId, template);
		setIsInvul(false);
		_owner = owner;
	}
	
	public L2Character getOwner()
	{
		return _owner;
	}
	
	/**
	 * this is called when a player interacts with this NPC
	 * 
	 * @param player
	 */
	@Override
	public void onAction(L2PcInstance player, boolean interact)
	{
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public void onActionShift(L2PcInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
}
