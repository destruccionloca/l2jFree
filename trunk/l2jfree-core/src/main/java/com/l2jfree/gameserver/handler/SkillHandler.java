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
import com.l2jfree.gameserver.handler.skillhandlers.ChangeFace;
import com.l2jfree.gameserver.handler.skillhandlers.CombatPointHeal;
import com.l2jfree.gameserver.handler.skillhandlers.Continuous;
import com.l2jfree.gameserver.handler.skillhandlers.CpDam;
import com.l2jfree.gameserver.handler.skillhandlers.Craft;
import com.l2jfree.gameserver.handler.skillhandlers.DeluxeKey;
import com.l2jfree.gameserver.handler.skillhandlers.Detection;
import com.l2jfree.gameserver.handler.skillhandlers.Disablers;
import com.l2jfree.gameserver.handler.skillhandlers.DrainSoul;
import com.l2jfree.gameserver.handler.skillhandlers.Dummy;
import com.l2jfree.gameserver.handler.skillhandlers.Extractable;
import com.l2jfree.gameserver.handler.skillhandlers.Fishing;
import com.l2jfree.gameserver.handler.skillhandlers.FishingSkill;
import com.l2jfree.gameserver.handler.skillhandlers.GetPlayer;
import com.l2jfree.gameserver.handler.skillhandlers.GiveSp;
import com.l2jfree.gameserver.handler.skillhandlers.GiveVitality;
import com.l2jfree.gameserver.handler.skillhandlers.Harvest;
import com.l2jfree.gameserver.handler.skillhandlers.Heal;
import com.l2jfree.gameserver.handler.skillhandlers.InstantJump;
import com.l2jfree.gameserver.handler.skillhandlers.LearnSkill;
import com.l2jfree.gameserver.handler.skillhandlers.MakeKillable;
import com.l2jfree.gameserver.handler.skillhandlers.MakeQuestDropable;
import com.l2jfree.gameserver.handler.skillhandlers.ManaHeal;
import com.l2jfree.gameserver.handler.skillhandlers.Manadam;
import com.l2jfree.gameserver.handler.skillhandlers.Mdam;
import com.l2jfree.gameserver.handler.skillhandlers.OpenDoor;
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
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.instance.L2CubicInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.skills.Formulas;
import com.l2jfree.gameserver.skills.l2skills.L2SkillDrain;
import com.l2jfree.gameserver.templates.skills.L2SkillType;
import com.l2jfree.util.EnumHandlerRegistry;
import com.l2jfree.util.HandlerRegistry;

public final class SkillHandler extends EnumHandlerRegistry<L2SkillType, ISkillHandler>
{
	public static SkillHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private SkillHandler()
	{
		super(L2SkillType.class);
		
		registerSkillHandler(new BalanceLife());
		registerSkillHandler(new BallistaBomb());
		registerSkillHandler(new BeastFeed());
		registerSkillHandler(new ChangeFace());
		registerSkillHandler(new CombatPointHeal());
		registerSkillHandler(new Continuous());
		registerSkillHandler(new CpDam());
		registerSkillHandler(new Craft());
		registerSkillHandler(new DeluxeKey());
		registerSkillHandler(new Detection());
		registerSkillHandler(new Disablers());
		registerSkillHandler(new DrainSoul());
		registerSkillHandler(new Dummy());
		registerSkillHandler(new Extractable());
		registerSkillHandler(new Fishing());
		registerSkillHandler(new FishingSkill());
		registerSkillHandler(new GetPlayer());
		registerSkillHandler(new GiveSp());
		registerSkillHandler(new GiveVitality());
		registerSkillHandler(new Harvest());
		registerSkillHandler(new Heal());
		registerSkillHandler(new InstantJump());
		registerSkillHandler(new LearnSkill());
		registerSkillHandler(new MakeKillable());
		registerSkillHandler(new MakeQuestDropable());
		registerSkillHandler(new ManaHeal());
		registerSkillHandler(new Manadam());
		registerSkillHandler(new Mdam());
		registerSkillHandler(new OpenDoor());
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
	
	public void useSkill(L2SkillType skillType, L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		final ISkillHandler handler = get(skillType);
		
		if (handler != null)
			handler.useSkill(activeChar, skill, targets);
		else
			skill.useSkill(activeChar, targets);
	}
	
	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		if (activeChar.isAlikeDead())
			return;
		
		useSkill(skill.getSkillType(), activeChar, skill, targets);
		
		for (L2Character target : targets)
		{
			Formulas.calcLethalHit(activeChar, target, skill);
		}
		
		// Increase Charges, Souls, Etc
		if (activeChar instanceof L2PcInstance)
		{
			((L2PcInstance) activeChar).increaseChargesBySkill(skill);
			((L2PcInstance) activeChar).increaseSoulsBySkill(skill);
		}
		
		skill.getEffectsSelf(activeChar);
		
		if (skill.isSuicideAttack())
			activeChar.doDie(activeChar);
	}
	
	public void useCubicSkill(L2CubicInstance cubic, L2Skill skill, L2Character... targets)
	{
		final ISkillHandler handler = get(skill.getSkillType());
		
		if (handler instanceof ICubicSkillHandler)
			((ICubicSkillHandler) handler).useCubicSkill(cubic, skill, targets);
		else if (skill instanceof L2SkillDrain)
			((L2SkillDrain) skill).useCubicSkill(cubic, targets);
		else if (handler != null)
			handler.useSkill(cubic.getOwner(), skill, targets);
		else
			skill.useSkill(cubic.getOwner(), targets);
	}
	
	public boolean checkConditions(L2Character activeChar, L2Skill skill)
	{
		final ISkillHandler handler = get(skill.getSkillType());
		
		if (handler instanceof ISkillConditionChecker)
			return ((ISkillConditionChecker) handler).checkConditions(activeChar, skill);
		
		return true;
	}
	
	public boolean checkConditions(L2Character activeChar, L2Skill skill, L2Character target)
	{
		final ISkillHandler handler = get(skill.getSkillType());
		
		if (handler instanceof ISkillConditionChecker)
			return ((ISkillConditionChecker) handler).checkConditions(activeChar, skill, target);
		
		return true;
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final SkillHandler _instance = new SkillHandler();
	}
}
