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
package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 *
 * @author  Julian
 */
public class DeluxeKey implements ISkillHandler
{
	private final static Log _log = LogFactory.getLog(BeastFeed.class.getName());
	private static final SkillType[] SKILL_IDS = {SkillType.DELUXE_KEY_UNLOCK};

	public void useSkill(L2Character activeChar, L2Skill skill, @SuppressWarnings("unused") L2Object[] targets)
	{
		if (!(activeChar instanceof L2PcInstance))
			return;

		L2Object[] targetList = skill.getTargetList(activeChar);

		if (targetList == null)
		{
			return;
		}

		if(_log.isDebugEnabled())
			_log.info("Delux key casting succeded.");

		// This is just a dummy skill handler for the golden food and crystal food skills,
		// since the AI responce onSkillUse handles the rest.

		//6665 6666 6667 6668 6669 6670 6671 6672 Chest KeyId
		int skLevel = skill.getLevel();
		int keyId = 6664 + skLevel;

		// Get the L2ItemInstance consummed by the spell
		L2ItemInstance requiredItems = ((L2PcInstance)activeChar).getInventory().getItemByItemId(keyId);

		// Check if the caster owns enought consummed Item to cast
		if (requiredItems == null || requiredItems.getCount() < 1)
		{
                    // Send a System Message to the caster
                    sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
                    return;
		}

		((L2PcInstance)activeChar).destroyItemWithoutTrace("Consume", keyId, 1, null, false);
	}

	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
