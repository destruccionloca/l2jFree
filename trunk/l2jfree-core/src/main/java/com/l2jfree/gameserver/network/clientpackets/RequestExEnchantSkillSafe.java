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
package com.l2jfree.gameserver.network.clientpackets;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.datatables.SkillTreeTable;
import com.l2jfree.gameserver.model.L2EnchantSkillLearn;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2ShortCut;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2EnchantSkillLearn.EnchantSkillDetail;
import com.l2jfree.gameserver.model.actor.instance.L2FolkInstance;
import com.l2jfree.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.base.Experience;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ExBrExtraUserInfo;
import com.l2jfree.gameserver.network.serverpackets.ShortCutRegister;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.network.serverpackets.UserInfo;
import com.l2jfree.gameserver.util.IllegalPlayerAction;
import com.l2jfree.gameserver.util.Util;
import com.l2jfree.tools.random.Rnd;

/**
 * Format (ch) dd
 * c: (id) 0xD0
 * h: (subid) 0x32
 * d: skill id
 * d: skill lvl
 * @author -Wooden-
 *
 */
public final class RequestExEnchantSkillSafe extends L2GameClientPacket
{
    private final static Log _log = LogFactory.getLog(RequestExEnchantSkillSafe.class.getName());
	private int _skillId;
	private int _skillLvl;
	
	
	@Override
    protected void readImpl()
	{
		_skillId = readD();
		_skillLvl = readD();
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.clientpackets.ClientBasePacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
        L2PcInstance player = getClient().getActiveChar();
        if (player == null)
            return;
        
        L2FolkInstance trainer = player.getLastFolkNPC();
        if (trainer == null)
            return;
        
        int npcid = trainer.getNpcId();
        
        if (!player.isInsideRadius(trainer, L2NpcInstance.INTERACTION_DISTANCE, false, false) && !player.isGM())
            return;
        
        if (player.getClassId().level() < 3) // requires to have 3rd class quest completed
            return;
        
        if (player.getLevel() < 76) 
            return;
        
        L2Skill skill = SkillTable.getInstance().getInfo(_skillId, _skillLvl);
        if (skill == null)
        {
            player.sendMessage("This skill enchant is not available.");
            return;
        }
        
        if (!skill.canTeachBy(npcid) || !skill.getCanLearn(player.getClassId()))
        {
            if (!Config.ALT_GAME_SKILL_LEARN)
            {
                player.sendMessage("You are trying to learn skill that u can't..");
                Util.handleIllegalPlayerAction(player, "Client "+this.getClient()+" tried to learn skill that he can't!!!", IllegalPlayerAction.PUNISH_KICK);
                return;
            }
        }

        int costMultiplier = SkillTreeTable.SAFE_ENCHANT_COST_MULTIPLIER;
        int reqItemId = SkillTreeTable.SAFE_ENCHANT_BOOK;
        
        L2EnchantSkillLearn s = SkillTreeTable.getInstance().getSkillEnchantmentBySkillId(_skillId);
        if (s == null)
        {
            player.sendMessage("This skill enchant is not available.");
            return;
        }
        EnchantSkillDetail esd = s.getEnchantSkillDetail(_skillLvl);
        if (player.getSkillLevel(_skillId) != esd.getMinSkillLevel())
        {
            player.sendMessage("This skill enchant is not available.");
            return;
        }
        
        int requiredSp = esd.getSpCost() * costMultiplier;
        int requiredExp = esd.getExp() * costMultiplier;
        int rate = esd.getRate(player);
        
        if (player.getSp() >= requiredSp)
        {
            long expAfter = player.getExp() - requiredExp;
            if (player.getExp() >= requiredExp && expAfter >= Experience.LEVEL[player.getLevel()])
            {
                // No config option for safe enchant book consume
                L2ItemInstance spb = player.getInventory().getItemByItemId(reqItemId);
                if (spb == null)// Haven't spellbook
                {
                    player.sendPacket(new SystemMessage(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL));
                    return;
                }

                boolean check;
                check = player.getStat().removeExpAndSp(requiredExp, requiredSp);
                check &= player.destroyItem("Consume", spb.getObjectId(), 1, trainer, true);
                
                if (!check)
                {
                    player.sendPacket(new SystemMessage(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL));
                    return;
                }
                
                // ok.  Destroy ONE copy of the book
                if (Rnd.get(100) <= rate)
                {
                    
                    player.addSkill(skill, true);
                    
                    if (_log.isDebugEnabled())
                    {
                        _log.info("Learned skill ID: "+_skillId+" Level: "+_skillLvl+" for "+requiredSp+" SP, "+requiredExp+" EXP.");
                    }

                    player.sendPacket(new UserInfo(player));
                    player.sendPacket(new ExBrExtraUserInfo(player));

                    SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_SUCCEEDED_IN_ENCHANTING_THE_SKILL_S1);
                    sm.addSkillName(_skillId);
                    player.sendPacket(sm);
                    updateSkillShortcuts(player);
                }
                else
                {
                    SystemMessage sm = new SystemMessage(SystemMessageId.SKILL_ENCHANT_FAILED_S1_LEVEL_WILL_REMAIN);
                    sm.addSkillName(_skillId);
                    player.sendPacket(sm);
                }
                trainer.showEnchantSkillList(player, true); // list of safe enchants
                
            }
            else
            {
                SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DONT_HAVE_ENOUGH_EXP_TO_ENCHANT_THAT_SKILL);
                player.sendPacket(sm);
            }
        }
        else
        {
            SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DONT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL);
            player.sendPacket(sm);
        }
	}
    
    private void updateSkillShortcuts(L2PcInstance player)
    {
        // update all the shortcuts to this skill
        L2ShortCut[] allShortCuts = player.getAllShortCuts();
        
        for (L2ShortCut sc : allShortCuts)
        {
            if (sc.getId() == _skillId && sc.getType() == L2ShortCut.TYPE_SKILL)
            {
                L2ShortCut newsc = new L2ShortCut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), _skillLvl, 1);
                player.sendPacket(new ShortCutRegister(newsc));
                player.registerShortCut(newsc);
            }
        }
    }

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return "[C] D0:32 RequestExEnchantSkillSafe";
	}
	
}
