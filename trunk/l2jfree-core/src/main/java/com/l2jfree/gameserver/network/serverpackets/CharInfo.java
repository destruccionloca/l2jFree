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
import com.l2jfree.gameserver.instancemanager.CursedWeaponsManager;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.actor.appearance.PcAppearance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.itemcontainer.Inventory;
import com.l2jfree.gameserver.network.L2GameClient;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;

public class CharInfo extends L2GameServerPacket
{
	private static final String	_S__31_CHARINFO	= "[S] 31 CharInfo [dddddsddd dddddddddddd dddddddd hhhh d hhhhhhhhhhhh d hhhh hhhhhhhhhhhhhhhh dddddd dddddddd ffff ddd s ddddd ccccccc h c d c h ddd cc d ccc ddddddddddd]";
	private L2PcInstance		_activeChar;
	private PcAppearance		_appearance;
	private Inventory			_inv;
	private int					_x, _y, _z, _heading;
	private int					_mAtkSpd, _pAtkSpd;
	private int					_runSpd, _walkSpd, _swimRunSpd, _swimWalkSpd, _flRunSpd, _flWalkSpd, _flyRunSpd, _flyWalkSpd;
	private float				_moveMultiplier, _attackSpeedMultiplier;

	/**
	 * @param _characters
	 */
	public CharInfo(L2PcInstance cha)
	{
		_activeChar = cha;
		_appearance = cha.getAppearance();
		_inv = _activeChar.getInventory();
		_x = _activeChar.getX();
		_y = _activeChar.getY();
		_z = _activeChar.getZ();
		_heading = _activeChar.getHeading();
		_mAtkSpd = _activeChar.getMAtkSpd();
		_pAtkSpd = _activeChar.getPAtkSpd();
		_moveMultiplier = _activeChar.getStat().getMovementSpeedMultiplier();
		_attackSpeedMultiplier = _activeChar.getStat().getAttackSpeedMultiplier();
		_runSpd = (int) (_activeChar.getRunSpeed() / _moveMultiplier);
		_walkSpd = (int) (_activeChar.getStat().getWalkSpeed() / _moveMultiplier);
		_swimRunSpd = _flRunSpd = _flyRunSpd = _runSpd;
		_swimWalkSpd = _flWalkSpd = _flyWalkSpd = _walkSpd;
	}
	
	@Override
	public void runImpl(L2GameClient client, L2PcInstance attacker)
	{
		RelationChanged.sendRelationChanged(_activeChar, attacker);
	}
	
	@Override
	protected final void writeImpl(L2GameClient client, L2PcInstance activeChar)
	{
		if (_activeChar.inObserverMode())
			return;

		boolean gmSeeInvis = false;

		if (_appearance.isInvisible())
		{
			if (activeChar != null && activeChar.isGM())
				gmSeeInvis = true;
			else
				return;
		}

		if (_activeChar.getPoly().isMorphed())
		{
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(_activeChar.getPoly().getPolyId());

			if (template != null)
			{
				writeC(0x0c);
				writeD(_activeChar.getObjectId());
				writeD(_activeChar.getPoly().getPolyId() + 1000000); // npctype id
				writeD(_activeChar.getKarma() > 0 ? 1 : 0);
				writeD(_x);
				writeD(_y);
				writeD(_z);
				writeD(_heading);
				writeD(0x00);
				writeD(_mAtkSpd);
				writeD(_pAtkSpd);
				writeD(_runSpd); // TODO: the order of the speeds should be confirmed
				writeD(_walkSpd);
				writeD(_swimRunSpd); // swimspeed
				writeD(_swimWalkSpd); // swimspeed
				writeD(_flRunSpd);
				writeD(_flWalkSpd);
				writeD(_flyRunSpd);
				writeD(_flyWalkSpd);
				writeF(_moveMultiplier);
				writeF(_attackSpeedMultiplier);
				writeF(template.getCollisionRadius());
				writeF(template.getCollisionHeight());
				writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND)); // right hand weapon
				writeD(0x00);
				writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_LHAND)); // left hand weapon
				writeC(0x01); // name above char 1=true ... ??
				writeC(_activeChar.isRunning() ? 1 : 0);
				writeC(_activeChar.isInCombat() ? 1 : 0);
				writeC(_activeChar.isAlikeDead() ? 1 : 0);

				if (gmSeeInvis)
				{
					writeC(0x00);
				}
				else
				{
					writeC(_appearance.isInvisible() ? 1 : 0); // invisible ?? 0=false  1=true   2=summoned (only works if model has a summon animation)
				}

				writeS(_appearance.getVisibleName());

				if (gmSeeInvis)
				{
					writeS("(Invisible) " + _appearance.getVisibleTitle());
				}
				else
				{
					writeS(_appearance.getVisibleTitle());
				}
				writeD(0x00);
				writeD(_activeChar.getPvpFlag());
				writeD(_activeChar.getKarma());

				if (gmSeeInvis)
				{
					writeD((_activeChar.getAbnormalEffect() | L2Character.ABNORMAL_EFFECT_STEALTH));
				}
				else
				{
					writeD(_activeChar.getAbnormalEffect()); // C2
				}

				writeD(0x00);
				writeD(_activeChar.getClanCrestId());
				writeD(0x00);
				writeD(_activeChar.getAllyCrestId());
				writeC(0x00);
				writeC(_activeChar.getTeam());
				writeF(template.getCollisionRadius());
				writeF(template.getCollisionHeight());
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
			}
			else
			{
				_log.warn("Character " + _activeChar.getName() + " (" + _activeChar.getObjectId() + ") morphed in a Npc (" + _activeChar.getPoly().getPolyId()
						+ ") w/o template.");
			}
		}
		else
		{
			writeC(0x31);
			writeD(_x);
			writeD(_y);
			writeD(_z);
			writeD(0x00);
			writeD(_activeChar.getObjectId());
			writeS(_appearance.getVisibleName());
			writeD(_activeChar.getRace().ordinal());
			writeD(_appearance.getSex() ? 1 : 0);

			if (_activeChar.getClassIndex() == 0)
				writeD(_activeChar.getClassId().getId());
			else
				writeD(_activeChar.getBaseClass());

			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_UNDER));
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_FEET));
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_HAIR));
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_HAIR));
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_HAIR2));

			// T1 new d's
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_RBRACELET));
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_LBRACELET));
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_DECO1));
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_DECO2));
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_DECO3));
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_DECO4));
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_DECO5));
			writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_DECO6));
			// end of t1 new d's

			// c6 new h's
			writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_UNDER));
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

			// T1 new h's
			writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_RBRACELET));
			writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_LBRACELET));
			writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_DECO1));
			writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_DECO2));
			writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_DECO3));
			writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_DECO4));
			writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_DECO5));
			writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_DECO6));
			// end of t1 new h's

			writeD(_activeChar.getPvpFlag());
			writeD(_activeChar.getKarma());

			writeD(_mAtkSpd);
			writeD(_pAtkSpd);

			writeD(_activeChar.getPvpFlag());
			writeD(_activeChar.getKarma());

			writeD(_runSpd); // TODO: the order of the speeds should be confirmed
			writeD(_walkSpd);
			writeD(_swimRunSpd);
			writeD(_swimWalkSpd);
			writeD(_flRunSpd);
			writeD(_flWalkSpd);
			writeD(_flyRunSpd);
			writeD(_flyWalkSpd);
			writeF(_activeChar.getStat().getMovementSpeedMultiplier()); // _cha.getProperMultiplier()
			writeF(_activeChar.getStat().getAttackSpeedMultiplier()); // _cha.getAttackSpeedMultiplier()

			if (_activeChar.getMountType() != 0)
			{
				writeF(NpcTable.getInstance().getTemplate(_activeChar.getMountNpcId()).getdCollisionRadius());
				writeF(NpcTable.getInstance().getTemplate(_activeChar.getMountNpcId()).getdCollisionHeight());
			}
			else if (_activeChar.getTransformation() != null)
			{
				writeF(_activeChar.getTransformation().getCollisionRadius(_activeChar));
				writeF(_activeChar.getTransformation().getCollisionHeight(_activeChar));
			}
			else if (_appearance.getSex())
			{
				writeF(_activeChar.getBaseTemplate().getFCollisionRadius());
				writeF(_activeChar.getBaseTemplate().getFCollisionHeight());
			}
			else
			{
				writeF(_activeChar.getBaseTemplate().getdCollisionRadius());
				writeF(_activeChar.getBaseTemplate().getdCollisionHeight());
			}

			writeD(_appearance.getHairStyle());
			writeD(_appearance.getHairColor());
			writeD(_appearance.getFace());

			if (gmSeeInvis)
			{
				writeS("(Invisible) " + _appearance.getVisibleTitle());
			}
			else
			{
				writeS(_appearance.getVisibleTitle());
			}

			writeD(_activeChar.getClanId());
			writeD(_activeChar.getClanCrestId());
			writeD(_activeChar.getAllyId());
			writeD(_activeChar.getAllyCrestId());
			// In UserInfo leader rights and siege flags, but here found nothing??
			// Therefore RelationChanged packet with that info is required
			writeD(0);

			writeC(_activeChar.isSitting() ? 0 : 1); // standing = 1  sitting = 0
			writeC(_activeChar.isRunning() ? 1 : 0); // running = 1   walking = 0
			writeC(_activeChar.isInCombat() ? 1 : 0);
			writeC(_activeChar.isAlikeDead() ? 1 : 0);

			if (gmSeeInvis)
			{
				writeC(0);
			}
			else
			{
				writeC(_appearance.isInvisible() ? 1 : 0); // invisible = 1  visible =0
			}

			writeC(_activeChar.getMountType()); // 1-on Strider, 2-on Wyvern, 3-on Great Wolf, 0-no mount
			writeC(_activeChar.getPrivateStoreType()); //  1 - sellshop

			writeH(_activeChar.getCubics().size());
			for (int id : _activeChar.getCubics().keySet())
				writeH(id);

			writeC(0x00); // find party members

			if (gmSeeInvis)
			{
				writeD((_activeChar.getAbnormalEffect() | L2Character.ABNORMAL_EFFECT_STEALTH));
			}
			else
			{
				writeD(_activeChar.getAbnormalEffect());
			}

			writeC(_activeChar.getEvaluations()); //Changed by Thorgrim
			writeH(_activeChar.getEvalPoints()); //Blue value for name (0 = white, 255 = pure blue)
			writeD(_activeChar.getMountNpcId() + 1000000);

			writeD(_activeChar.getClassId().getId());
			writeD(0x00); //?
			writeC(_activeChar.isMounted() ? 0 : _activeChar.getEnchantEffect());

			if (_activeChar.getTeam() == 1)
				writeC(0x01); //team circle around feet 1= Blue, 2 = red
			else if (_activeChar.getTeam() == 2)
				writeC(0x02); //team circle around feet 1= Blue, 2 = red
			else
				writeC(0x00); //team circle around feet 1= Blue, 2 = red

			writeD(_activeChar.getClanCrestLargeId());
			writeC(_activeChar.isNoble() ? 1 : 0); // Symbol on char menu ctrl+I  
			writeC((_activeChar.isHero() || (_activeChar.isGM() && Config.GM_HERO_AURA)) ? 1 : 0); // Hero Aura

			writeC(_activeChar.isFishing() ? 1 : 0); //0x01: Fishing Mode (Cant be undone by setting back to 0)
			writeD(_activeChar.getFishx());
			writeD(_activeChar.getFishy());
			writeD(_activeChar.getFishz());

			writeD(_appearance.getNameColor());

			writeD(_heading);

			writeD(_activeChar.getPledgeClass());
			writeD(_activeChar.getSubPledgeType());

			writeD(_appearance.getTitleColor());

			if (_activeChar.isCursedWeaponEquipped())
				writeD(CursedWeaponsManager.getInstance().getLevel(_activeChar.getCursedWeaponEquippedId()));
			else
				writeD(0x00);

			if (_activeChar.getClan() != null)
				writeD(_activeChar.getClan().getReputationScore());
			else
				writeD(0x00);

			writeD(_activeChar.getTransformationId());
			writeD(_activeChar.getAgathionId());

			writeD(0x00);
		}
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__31_CHARINFO;
	}
	
	@Override
	public boolean canBeSentTo(L2GameClient client, L2PcInstance activeChar)
	{
		return _activeChar != activeChar;
	}
}