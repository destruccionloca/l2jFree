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
package com.l2jfree.gameserver.skills.effects;

import com.l2jfree.gameserver.ai.CtrlIntention;
import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.serverpackets.DeleteObject;
import com.l2jfree.gameserver.skills.AbnormalEffect;
import com.l2jfree.gameserver.skills.Env;
import com.l2jfree.gameserver.templates.effects.EffectTemplate;
import com.l2jfree.gameserver.templates.skills.L2EffectType;

/**
 * @author ZaKaX - nBd
 */
public class EffectHide extends L2Effect
{
	public EffectHide(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	public EffectHide(Env env, L2Effect effect)
	{
		super(env, effect);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.HIDE;
	}
	
	@Override
	protected boolean onStart()
	{
		if (getEffected() instanceof L2PcInstance)
		{
			L2PcInstance activeChar = ((L2PcInstance)getEffected());
			activeChar.getAppearance().setInvisible();
			activeChar.startAbnormalEffect(AbnormalEffect.STEALTH);

			if (activeChar.getAI().getNextCtrlIntention() == CtrlIntention.AI_INTENTION_ATTACK)
				activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

			final DeleteObject del = new DeleteObject(activeChar);
			for (L2Character obj : activeChar.getKnownList().getKnownCharacters())
			{
				if (obj == null)
					continue;
				
				if (obj.getTarget() == activeChar)
				{
					obj.setTarget(null);
					obj.abortAttack();
					obj.abortCast();
					obj.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				}
				
				if (obj instanceof L2PcInstance)
					((L2PcInstance)obj).sendPacket(del);
			}
		}
		
		return true;
	}
	
	@Override
	protected void onExit()
	{
		if (getEffected() instanceof L2PcInstance)
		{
			L2PcInstance activeChar = ((L2PcInstance)getEffected());
			activeChar.getAppearance().setVisible();
			activeChar.stopAbnormalEffect(AbnormalEffect.STEALTH);
		}
	}
}
