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
package com.l2jfree.gameserver.handler;

import com.l2jfree.gameserver.handler.skillhandlers.BalanceLife;
import com.l2jfree.gameserver.handler.skillhandlers.BallistaBomb;
import com.l2jfree.gameserver.handler.skillhandlers.BeastFeed;
import com.l2jfree.gameserver.handler.skillhandlers.Blow;
import com.l2jfree.gameserver.handler.skillhandlers.CPperHeal;
import com.l2jfree.gameserver.handler.skillhandlers.ClanGate;
import com.l2jfree.gameserver.handler.skillhandlers.CombatPointHeal;
import com.l2jfree.gameserver.handler.skillhandlers.Continuous;
import com.l2jfree.gameserver.handler.skillhandlers.CpDam;
import com.l2jfree.gameserver.handler.skillhandlers.Craft;
import com.l2jfree.gameserver.handler.skillhandlers.DeluxeKey;
import com.l2jfree.gameserver.handler.skillhandlers.Disablers;
import com.l2jfree.gameserver.handler.skillhandlers.DrainSoul;
import com.l2jfree.gameserver.handler.skillhandlers.Dummy;
import com.l2jfree.gameserver.handler.skillhandlers.Extractable;
import com.l2jfree.gameserver.handler.skillhandlers.Fishing;
import com.l2jfree.gameserver.handler.skillhandlers.FishingSkill;
import com.l2jfree.gameserver.handler.skillhandlers.GetPlayer;
import com.l2jfree.gameserver.handler.skillhandlers.GiveSp;
import com.l2jfree.gameserver.handler.skillhandlers.Harvest;
import com.l2jfree.gameserver.handler.skillhandlers.Heal;
import com.l2jfree.gameserver.handler.skillhandlers.InstantJump;
import com.l2jfree.gameserver.handler.skillhandlers.MakeKillable;
import com.l2jfree.gameserver.handler.skillhandlers.ManaHeal;
import com.l2jfree.gameserver.handler.skillhandlers.Manadam;
import com.l2jfree.gameserver.handler.skillhandlers.Mdam;
import com.l2jfree.gameserver.handler.skillhandlers.Pdam;
import com.l2jfree.gameserver.handler.skillhandlers.Recall;
import com.l2jfree.gameserver.handler.skillhandlers.Resurrect;
import com.l2jfree.gameserver.handler.skillhandlers.ShiftTarget;
import com.l2jfree.gameserver.handler.skillhandlers.SiegeFlag;
import com.l2jfree.gameserver.handler.skillhandlers.Soul;
import com.l2jfree.gameserver.handler.skillhandlers.Sow;
import com.l2jfree.gameserver.handler.skillhandlers.Spoil;
import com.l2jfree.gameserver.handler.skillhandlers.StrSiegeAssault;
import com.l2jfree.gameserver.handler.skillhandlers.SummonFriend;
import com.l2jfree.gameserver.handler.skillhandlers.SummonTreasureKey;
import com.l2jfree.gameserver.handler.skillhandlers.Sweep;
import com.l2jfree.gameserver.handler.skillhandlers.TakeCastle;
import com.l2jfree.gameserver.handler.skillhandlers.TakeFort;
import com.l2jfree.gameserver.handler.skillhandlers.TransformDispel;
import com.l2jfree.gameserver.handler.skillhandlers.Trap;
import com.l2jfree.gameserver.handler.skillhandlers.Unlock;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.templates.skills.L2SkillType;
import com.l2jfree.util.HandlerRegistry;

public final class SkillHandler extends HandlerRegistry<L2SkillType, ISkillHandler> implements ISkillHandler
{
	private static SkillHandler	_instance;

	public static SkillHandler getInstance()
	{
		if (_instance == null)
			_instance = new SkillHandler();

		return _instance;
	}

	private SkillHandler()
	{
		registerSkillHandler(new BalanceLife());
		registerSkillHandler(new BallistaBomb());
		registerSkillHandler(new BeastFeed());
		registerSkillHandler(new Blow());
		registerSkillHandler(new ClanGate());
		registerSkillHandler(new CPperHeal());
		registerSkillHandler(new CombatPointHeal());
		registerSkillHandler(new Continuous());
		registerSkillHandler(new CpDam());
		registerSkillHandler(new Craft());
		registerSkillHandler(new DeluxeKey());
		registerSkillHandler(new Disablers());
		registerSkillHandler(new DrainSoul());
		registerSkillHandler(new Dummy());
		registerSkillHandler(new Extractable());
		registerSkillHandler(new Fishing());
		registerSkillHandler(new FishingSkill());
		registerSkillHandler(new GetPlayer());
		registerSkillHandler(new GiveSp());
		registerSkillHandler(new Harvest());
		registerSkillHandler(new Heal());
		registerSkillHandler(new InstantJump());
		registerSkillHandler(new MakeKillable());
		registerSkillHandler(new ManaHeal());
		registerSkillHandler(new Manadam());
		registerSkillHandler(new Mdam());
		registerSkillHandler(new Pdam());
		registerSkillHandler(new Recall());
		registerSkillHandler(new Resurrect());
		registerSkillHandler(new ShiftTarget());
		registerSkillHandler(new SiegeFlag());
		registerSkillHandler(new Soul());
		registerSkillHandler(new Sow());
		registerSkillHandler(new Spoil());
		registerSkillHandler(new StrSiegeAssault());
		registerSkillHandler(new SummonFriend());
		registerSkillHandler(new SummonTreasureKey());
		registerSkillHandler(new Sweep());
		registerSkillHandler(new TakeCastle());
		registerSkillHandler(new TakeFort());
		registerSkillHandler(new TransformDispel());
		registerSkillHandler(new Trap());
		registerSkillHandler(new Unlock());

		HandlerRegistry._log.info("SkillHandler: Loaded " + size() + " handlers.");
	}

	public void registerSkillHandler(ISkillHandler handler)
	{
		registerAll(handler, handler.getSkillIds());
	}

	public ISkillHandler getSkillHandler(L2SkillType skillType)
	{
		ISkillHandler handler = get(skillType);

		return handler == null ? this : handler;
	}

	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		skill.useSkill(activeChar, targets);
	}

	public L2SkillType[] getSkillIds()
	{
		return null;
	}
}