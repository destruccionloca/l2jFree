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
package com.l2jfree.gameserver.network.serverpackets;

import com.l2jfree.Config;
import com.l2jfree.gameserver.datatables.NpcTable;
import com.l2jfree.gameserver.model.actor.appearance.PcAppearance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.stat.PcStat;
import com.l2jfree.gameserver.model.actor.status.PcStatus;
import com.l2jfree.gameserver.model.actor.view.PcView;
import com.l2jfree.gameserver.model.itemcontainer.Inventory;
import com.l2jfree.gameserver.model.itemcontainer.PcInventory;
import com.l2jfree.gameserver.network.L2GameClient;
import com.l2jfree.gameserver.skills.AbnormalEffect;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;

/**
 * 0000: 04 03 15 00 00 77 ff 00 00 80 f1 ff ff 00 00 00    .....w..........
 * 0010: 00 2a 89 00 4c 43 00 61 00 6c 00 61 00 64 00 6f    .*..LC.a.l.a.d.o
 * 0020: 00 6e 00 00 00 01 00 00 00 00 00 00 00 19 00 00    .n..............
 * 0030: 00 0d 00 00 00 ee 81 02 00 15 00 00 00 18 00 00    ................
 * 0040: 00 19 00 00 00 25 00 00 00 17 00 00 00 28 00 00    .....%.......(..
 * 0050: 00 14 01 00 00 14 01 00 00 02 01 00 00 02 01 00    ................
 * 0060: 00 fa 09 00 00 81 06 00 00 26 34 00 00 2e 00 00    .........&4.....
 * 0070: 00 00 00 00 00 db 9f a1 41 93 26 64 41 de c8 31    ........A.&dA..1
 * 0080: 41 ca 73 c0 41 d5 22 d0 41 83 bd 41 41 81 56 10    A.s.A.".A..AA.V.
 * 0090: 41 00 00 00 00 27 7d 30 41 69 aa e0 40 b4 fb d3    A....'}0Ai..@...
 * 00a0: 41 91 f9 63 41 00 00 00 00 81 56 10 41 00 00 00    A..cA.....V.A...
 * 00b0: 00 71 00 00 00 71 00 00 00 76 00 00 00 74 00 00    .q...q...v...t..
 * 00c0: 00 74 00 00 00 2a 00 00 00 e8 02 00 00 00 00 00    .t...*..........
 * 00d0: 00 5f 04 00 00 ac 01 00 00 cf 01 00 00 62 04 00    ._...........b..
 * 00e0: 00 00 00 00 00 e8 02 00 00 0b 00 00 00 52 01 00    .............R..
 * 00f0: 00 4d 00 00 00 2a 00 00 00 2f 00 00 00 29 00 00    .M...*.../...)..
 * 0100: 00 12 00 00 00 82 01 00 00 52 01 00 00 53 00 00    .........R...S..
 * 0110: 00 00 00 00 00 00 00 00 00 7a 00 00 00 55 00 00    .........z...U..
 * 0120: 00 32 00 00 00 32 00 00 00 00 00 00 00 00 00 00    .2...2..........
 * 0130: 00 00 00 00 00 00 00 00 00 a4 70 3d 0a d7 a3 f0    ..........p=....
 * 0140: 3f 64 5d dc 46 03 78 f3 3f 00 00 00 00 00 00 1e    ?d].F.x.?.......
 * 0150: 40 00 00 00 00 00 00 38 40 02 00 00 00 01 00 00    @......8@.......
 * 0160: 00 00 00 00 00 00 00 00 00 00 00 c1 0c 00 00 01    ................
 * 0170: 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00    ................
 * 0180: 00 00 00 00                                        ....
 *
 *
 * dddddSdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddffffddddSdddcccdd (h)
 * dddddSddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd  ffffddddSdddddcccddh (h) c dc hhdh
 * dddddSdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddffffddddSdddddcccddh (h) c dc hhdh ddddc c dcc cddd d (from 654)
 * but it actually reads
 * dddddSdddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddffffddddSdddddcccddh (h) c dc *dddddddd* hhdh ddddc dcc cddd d
 * 																					*...*: here i am not sure at least it looks like it reads that much data (32 bytes), not sure about the format inside because it is not read thanks to the ususal parsing function
 *
 * dddddSddddQddddddddddddddddddddddddddddddddddddddddddddddddhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhddddddddddddddddddddffffddddSdddddcccddh [h] c dc d hhdh ddddc c dcc cddd d c dd d d
 * dddddSddddQddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhddddddddddddddddddddffffddddSdddddcccddh

 * @version $Revision: 1.14.2.4.2.12 $ $Date: 2005/04/11 10:05:55 $
 */
public final class UserInfo extends L2GameServerPacket
{
	private static final String _S__04_USERINFO = "[S] 04 UserInfo";
	
	private final L2PcInstance _activeChar;
	private final int _relation;
	
	public UserInfo(L2PcInstance cha)
	{
		cha.getView().refresh();
		
		_activeChar = cha;
		
		int relation = cha.isClanLeader() ? 0x40 : 0;
		
		if (cha.getSiegeState() == L2PcInstance.SIEGE_STATE_ATTACKER)
			relation |= 0x180;
		else if (cha.getSiegeState() == L2PcInstance.SIEGE_STATE_DEFENDER)
			relation |= 0x80;
		
		_relation = relation;
	}
	
	@Override
	public void packetSent(L2GameClient client, L2PcInstance activeChar)
	{
		if (Config.PACKET_FINAL)
		{
			_activeChar.sendPacket(new ExBrExtraUserInfo(_activeChar));
			_activeChar.sendPacket(new ExVitalityPointInfo(_activeChar.getVitalityPoints()));
		}
	}
	
	@Override
	public boolean canBeSentTo(L2GameClient client, L2PcInstance activeChar)
	{
		return _activeChar == activeChar;
	}
	
	@Override
	protected void writeImpl()
	{
		final PcView view = _activeChar.getView();
		final PcAppearance _appearance = _activeChar.getAppearance();
		final PcInventory _inv = _activeChar.getInventory();
		final PcStat stat = _activeChar.getStat();
		final PcStatus status = _activeChar.getStatus();
		
		writeC(0x32);
		
		writeD(view.getX());
		writeD(view.getY());
		writeD(view.getZ());
		// heading from CT2.3 no longer used inside userinfo, here is now vehicle id (boat,airship)
		writeD((_activeChar.isInAirShip() && Config.PACKET_FINAL) ? _activeChar.getAirShip().getObjectId() : 0x00);
		writeD(view.getObjectId());
		writeS(_appearance.getVisibleName());
		writeD(_activeChar.getRace().ordinal());
		writeD(_appearance.getSex() ? 1 : 0);
		
		if (_activeChar.getClassIndex() == 0)
			writeD(_activeChar.getClassId().getId());
		else
			writeD(_activeChar.getBaseClass());
		
		writeD(_activeChar.getLevel());
		writeQ(_activeChar.getExp());
		writeD(stat.getSTR());
		writeD(stat.getDEX());
		writeD(stat.getCON());
		writeD(stat.getINT());
		writeD(stat.getWIT());
		writeD(stat.getMEN());
		writeD(stat.getMaxHp());
		writeD((int)status.getCurrentHp());
		writeD(stat.getMaxMp());
		writeD((int)status.getCurrentMp());
		writeD(_activeChar.getSp());
		writeD(_activeChar.getCurrentLoad());
		writeD(_activeChar.getMaxLoad());
		
		writeD(_activeChar.getActiveWeaponItem() != null ? 40 : 20); // 20 no weapon, 40 weapon equipped
		
		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_UNDER));
		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_REAR));
		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_LEAR));
		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_NECK));
		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_RFINGER));
		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_LFINGER));
		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_HEAD));
		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_RHAND));
		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_LHAND));
		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_GLOVES));
		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_CHEST));
		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_LEGS));
		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_FEET));
		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_BACK));
		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_LRHAND));
		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_HAIR));
		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_HAIR2));
		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_RBRACELET));
		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_LBRACELET));
		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_DECO1));
		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_DECO2));
		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_DECO3));
		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_DECO4));
		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_DECO5));
		writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_DECO6));
		if (Config.PACKET_FINAL)
			writeD(_inv.getPaperdollObjectId(Inventory.PAPERDOLL_BELT)); // CT2.3
			
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_UNDER));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_REAR));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_LEAR));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_NECK));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_RFINGER));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_LFINGER));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_FEET));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_BACK));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_LRHAND));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_HAIR));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_HAIR2));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_RBRACELET));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_LBRACELET));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_DECO1));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_DECO2));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_DECO3));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_DECO4));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_DECO5));
		writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_DECO6));
		if (Config.PACKET_FINAL)
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_BELT)); // CT2.3
			
		// c6 new h's
		writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_UNDER));
		writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_REAR));
		writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_LEAR));
		writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_NECK));
		writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_RFINGER));
		writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_LFINGER));
		writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_HEAD));
		writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_RHAND));
		writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_LHAND));
		writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_GLOVES));
		writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_CHEST));
		writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_LEGS));
		writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_FEET));
		writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_BACK));
		writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_LRHAND));
		writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_HAIR));
		writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_HAIR2));
		// end of c6 new h's
		
		// start of T1 new h's
		writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_RBRACELET));
		writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_LBRACELET));
		writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_DECO1));
		writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_DECO2));
		writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_DECO3));
		writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_DECO4));
		writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_DECO5));
		writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_DECO6));
		// end of T1 new h's
		
		if (Config.PACKET_FINAL)
		{
			writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_BELT)); // CT2.3
			writeD(_inv.getMaxTalismanCount()); // CT2.3
			writeD(_inv.getCloakStatus()); // CT2.3
		}
		
		writeD(view.getPAtk());
		writeD(view.getPAtkSpd());
		writeD(view.getPDef());
		writeD(view.getEvasionRate());
		writeD(view.getAccuracy());
		writeD(view.getCriticalHit());
		writeD(view.getMAtk());
		writeD(view.getMAtkSpd());
		writeD(view.getPAtkSpd());
		writeD(view.getMDef());
		writeD(_activeChar.getPvpFlag());
		writeD(_activeChar.getKarma());
		
		writeD(view.getRunSpd());
		writeD(view.getWalkSpd());
		writeD(view.getSwimRunSpd());
		writeD(view.getSwimWalkSpd());
		writeD(0);
		writeD(0);
		writeD(_activeChar.isFlying() ? view.getFlyRunSpd() : 0); // fly speed
		writeD(_activeChar.isFlying() ? view.getFlyWalkSpd() : 0); // fly speed
		
		writeF(view.getMovementSpeedMultiplier());
		writeF(view.getAttackSpeedMultiplier());
		
		writeF(view.getCollisionRadius());
		writeF(view.getCollisionHeight());
		
		writeD(_appearance.getHairStyle());
		writeD(_appearance.getHairColor());
		writeD(_appearance.getFace());
		writeD((_activeChar.getAccessLevel() >= Config.GM_ALTG_MIN_LEVEL) ? 1 : 0); // builder level
		
		String title = _appearance.getVisibleTitle();
		if (_appearance.isInvisible() && _activeChar.isGM())
			title = "Invisible";
		if (_activeChar.getPoly().isMorphed())
		{
			L2NpcTemplate polyObj = NpcTable.getInstance().getTemplate(_activeChar.getPoly().getPolyId());
			if (polyObj != null)
				title += " - " + polyObj.getName();
		}
		writeS(title);
		
		writeD(_activeChar.getClanId());
		writeD(_activeChar.getClanCrestId());
		writeD(_activeChar.getAllyId());
		writeD(_activeChar.getAllyCrestId()); // ally crest id
		// 0x40 leader rights
		// siege flags: attacker - 0x180 sword over name, defender - 0x80 shield, 0xC0 crown (|leader), 0x1C0 flag (|leader)
		writeD(_relation);
		writeC(_activeChar.getMountType()); // mount type
		writeC(_activeChar.getPrivateStoreType());
		writeC(_activeChar.hasDwarvenCraft() ? 1 : 0);
		writeD(_activeChar.getPkKills());
		writeD(_activeChar.getPvpKills());
		
		writeH(_activeChar.getCubics().size());
		for (int id : _activeChar.getCubics().keySet())
			writeH(id);
		
		writeC(_activeChar.isLookingForParty());
		
		if (_appearance.isInvisible())
		{
			writeD(_activeChar.getAbnormalEffect() | AbnormalEffect.STEALTH.getMask());
		}
		else
		{
			writeD(_activeChar.getAbnormalEffect());
		}
		writeC(_activeChar.isFlyingMounted() ? 2 : 0);
		
		writeD(_activeChar.getClanPrivileges());
		
		writeH(_activeChar.getEvaluations()); //c2  recommendations remaining
		writeH(_activeChar.getEvalPoints()); //c2  recommendations received
		writeD(_activeChar.getMountNpcId() > 0 ? _activeChar.getMountNpcId() + 1000000 : 0);
		writeH(_activeChar.getInventoryLimit());
		
		//writeH(_inv.getUnequippedSize());
		
		writeD(_activeChar.getClassId().getId());
		writeD(0x00); // special effects? circles around player...
		writeD(stat.getMaxCp());
		writeD((int)status.getCurrentCp());
		writeC(_activeChar.isMounted() ? 0 : _activeChar.getEnchantEffect());
		
		if (_activeChar.getTeam() == 1)
			writeC(0x01); //team circle around feet 1= Blue, 2 = red
		else if (_activeChar.getTeam() == 2)
			writeC(0x02); //team circle around feet 1= Blue, 2 = red
		else
			writeC(0x00); //team circle around feet 1= Blue, 2 = red
			
		writeD(_activeChar.getClanCrestLargeId());
		writeC(_activeChar.isNoble() ? 1 : 0); //0x01: symbol on char menu ctrl+I
		writeC((_activeChar.isHero() || (_activeChar.isGM() && Config.GM_HERO_AURA)) ? 1 : 0); //0x01: Hero Aura
		
		writeC(_activeChar.isFishing() ? 1 : 0); //Fishing Mode
		writeD(_activeChar.getFishx()); //fishing x
		writeD(_activeChar.getFishy()); //fishing y
		writeD(_activeChar.getFishz()); //fishing z
		writeD(view.getNameColor());
		
		//new c5
		writeC(_activeChar.isRunning() ? 0x01 : 0x00); //changes the Speed display on Status Window
		
		writeD(_activeChar.getPledgeClass()); //changes the text above CP on Status Window
		writeD(_activeChar.getSubPledgeType());
		
		writeD(view.getTitleColor());
		
		writeD(view.getCursedWeaponLevel());
		
		writeD(view.getTransformationGraphicalId());
		
		writePlayerElementAttribute(_activeChar);
		
		writeD(_activeChar.getAgathionId());
		
		// T2
		writeD(_activeChar.getFame()); // Fame
		writeD(0x01); // Unknown
		writeD(_activeChar.getVitalityPoints());
		if (Config.PACKET_FINAL)
		{
			writeD(_activeChar.getSpecialEffect());
			writeD(0x00); // CT2.3
			writeD(0x00); // CT2.3
			writeD(0x00); // CT2.3
		}
	}
	
	@Override
	public String getType()
	{
		return _S__04_USERINFO;
	}
}
