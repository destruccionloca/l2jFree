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
package com.l2jfree.gameserver.model;

import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

/**
 * Description: <br>
 * This will handle the transformation, giving the skills, and removing them, when the player logs out and is transformed these skills
 * do not save.
 * When the player logs back in, there will be a call from the enterworld packet that will add all their skills.
 * The enterworld packet will transform a player.
 * 
 * @author Ahmed
 *
 */
public abstract class L2DefaultTransformation extends L2Transformation
{
	public L2DefaultTransformation(int id, double collisionRadius, double collisionHeight)
	{
		super(id, collisionRadius, collisionHeight);
	}

	public L2DefaultTransformation(int id, int graphicalId, double collisionRadius, double collisionHeight)
	{
		super(id, graphicalId, collisionRadius, collisionHeight);
	}
	
	@Override
	public void addSkill(L2PcInstance player, int skillId, int skillLevel)
	{
		player.addTransformAllowedSkill(skillId);
		super.addSkill(player, skillId, skillLevel);
	}

	public void addSkills(L2PcInstance player, int... skills)
	{
		for (int skill : skills)
			addSkill(player, skill, 1);
	}

	@Override
	public void onTransform(L2PcInstance player)
	{
		if (player.getTransformationId() != getId() || player.isCursedWeaponEquipped())
			return;
		
		// give transformation skills
		transformedSkills(player);

		// Transfrom Dispel
		addSkill(player, 619, 1);
		// Decrease Bow/Crossbow Attack Speed
		addSkill(player, 5491, 1);
	}

	public abstract void transformedSkills(L2PcInstance player);

	@Override
	public void onUntransform(L2PcInstance player)
	{
		// remove transformation skills
		removeSkills(player);

		// Transfrom Dispel
		removeSkill(player, 619);
		// Decrease Bow/Crossbow Attack Speed
		removeSkill(player, 5491);
	}

	public void removeSkills(L2PcInstance player, int... skills)
	{
		for (int skill : skills)
			removeSkill(player, skill);
	}

	public abstract void removeSkills(L2PcInstance player);
}