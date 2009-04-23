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

/**
 * Format (ch) dd
 * c: (id) 0xD0
 * h: (subid) 0x33
 * d: skill id
 * d: skill lvl
 * @author -Wooden-
 *
 */
public final class RequestExEnchantSkillUntrain extends L2GameClientPacket
{
    protected final static Log _log = LogFactory.getLog(RequestExEnchantSkillUntrain.class.getName());
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
        
	    int reqItemId = SkillTreeTable.UNTRAIN_ENCHANT_BOOK;
        
        L2EnchantSkillLearn s = SkillTreeTable.getInstance().getSkillEnchantmentBySkillId(_skillId);
        if (s == null)
        {
            return;
        }
        
        int currentLevel = player.getSkillLevel(_skillId);
        if (currentLevel - 1 != _skillLvl && (currentLevel%100 != 1 || _skillLvl != s.getBaseLevel()))
        {
            return;
        }
        
        EnchantSkillDetail esd = s.getEnchantSkillDetail(currentLevel);
        
        
        int requiredSp = esd.getSpCost();
        int requiredExp = esd.getExp();
        //int rate = esd.getRate(player);
        
        if (player.getSp() >= requiredSp)
        {
            long expAfter = player.getExp() - requiredExp;
            if (player.getExp() >= requiredExp && expAfter >= Experience.LEVEL[player.getLevel()])
            {
                L2ItemInstance spb = player.getInventory().getItemByItemId(reqItemId);
                if (Config.ALT_ES_SP_BOOK_NEEDED) //if (Config.ES_SP_BOOK_NEEDED && usesBook)  dissabled till official info - will request book for each untrain
                {
                    if (spb == null)// Haven't spellbook
                    {
                        player.sendPacket(new SystemMessage(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL));
                        return;
                    }
                }
                
                boolean check;
                check = player.getStat().removeExpAndSp(requiredExp, requiredSp);
                if (Config.ALT_ES_SP_BOOK_NEEDED) //if (Config.ES_SP_BOOK_NEEDED && usesBook)  dissabled till official info - will request book for each untrain 
                {
                    check &= player.destroyItem("Consume", spb.getObjectId(), 1, trainer, true);
                }
                
                
                if (!check)
                {
                    player.sendPacket(new SystemMessage(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL));
                    return;
                }
                
                if (_skillLvl%100 == 0)
                {
                    _skillLvl = s.getBaseLevel();
                }
                
                player.addSkill(skill, true);
                
                if (_log.isDebugEnabled())
                {
                    _log.info("Learned skill ID: "+_skillId+" Level: "+_skillLvl+" for "+requiredSp+" SP, "+requiredExp+" EXP.");
                }
                
                
                
                player.sendPacket(new UserInfo(player));
                player.sendPacket(new ExBrExtraUserInfo(player));
                
                if (_skillLvl > 100)
                {
                    SystemMessage sm = new SystemMessage(SystemMessageId.UNTRAIN_SUCCESSFUL_SKILL_S1_ENCHANT_LEVEL_DECREASED_BY_ONE);
                    sm.addSkillName(_skillId);
                    player.sendPacket(sm);
                }
                else
                {
                    SystemMessage sm = new SystemMessage(SystemMessageId.UNTRAIN_SUCCESSFUL_SKILL_S1_ENCHANT_LEVEL_RESETED);
                    sm.addSkillName(_skillId);
                    player.sendPacket(sm);
                }
                
                trainer.showEnchantUntrainSkillList(player, player.getClassId());
                this.updateSkillShortcuts(player);
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
		return "[C] D0:33 RequestExEnchantSkillUntrain";
	}
	
}
