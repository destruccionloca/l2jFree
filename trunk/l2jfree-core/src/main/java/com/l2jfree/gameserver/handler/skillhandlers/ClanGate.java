package com.l2jfree.gameserver.handler.skillhandlers;

import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.handler.ISkillHandler;
import com.l2jfree.gameserver.instancemanager.CastleManager;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2Clan;
import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.entity.Castle;
import com.l2jfree.gameserver.model.zone.L2Zone;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.templates.skills.L2SkillType;

public class ClanGate implements ISkillHandler {
	private static final L2SkillType[] CG_SKILLS = { L2SkillType.CLAN_GATE };

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.handler.ISkillHandler#getSkillIds()
	 */
	@Override
	public L2SkillType[] getSkillIds() {
		return CG_SKILLS;
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.handler.ISkillHandler#useSkill(com.l2jfree.gameserver.model.L2Character, com.l2jfree.gameserver.model.L2Skill, com.l2jfree.gameserver.model.L2Character[])
	 */
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill,
			L2Character... targets) {
		L2PcInstance player = null;
		if (activeChar instanceof L2PcInstance)
			player = (L2PcInstance) activeChar;
		else
			return;

		if (player.isInFunEvent() || player.isInsideZone(L2Zone.FLAG_NOSUMMON) ||
				player.isInsideZone(L2Zone.FLAG_NOLANDING) || player.isInOlympiadMode()) {
			player.sendMessage("Cannot open the portal here.");
			return;
		}

		L2Clan clan = player.getClan();
		if (clan != null) {
			if(CastleManager.getInstance().getCastleByOwner(clan) != null) {
				Castle castle = CastleManager.getInstance().getCastleByOwner(clan);
				if (player.isCastleLord(castle.getCastleId())) {
					//please note clan gate expires in two minutes WHATEVER happens to the clan leader.
					ThreadPoolManager.getInstance().scheduleGeneral(new RemoveClanGate(castle.getCastleId()), skill.getTotalLifeTime());
					castle.createClanGate(activeChar.getX(), activeChar.getY(), activeChar.getZ() + 20);
					player.getClan().broadcastToOnlineMembers(new SystemMessage(SystemMessageId.COURT_MAGICIAN_CREATED_PORTAL));
				}
			}
		}
		L2Effect effect = player.getFirstEffect(skill.getId());
		if (effect != null && effect.isSelfEffect())
			effect.exit();
		skill.getEffectsSelf(player);
	}

	private class RemoveClanGate implements Runnable {
		private final int castle;

        private RemoveClanGate(int castle) {
        	this.castle = castle;
        }

        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
        	CastleManager.getInstance().getCastleById(castle).destroyClanGate();
        }
    }
}
