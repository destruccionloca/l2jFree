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
import com.l2jfree.gameserver.instancemanager.CursedWeaponsManager;
import com.l2jfree.gameserver.model.L2Transformation;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Decoy;
import com.l2jfree.gameserver.model.actor.L2Npc;
import com.l2jfree.gameserver.model.actor.L2Summon;
import com.l2jfree.gameserver.model.actor.L2Trap;
import com.l2jfree.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.itemcontainer.Inventory;
import com.l2jfree.gameserver.network.L2GameClient;

public abstract class AbstractNpcInfo extends L2GameServerPacket
{
	private static final String _S__22_NPCINFO = "[S] 0c NpcInfo";

	protected int _x, _y, _z, _heading;
	protected int _idTemplate;
	protected boolean _isSummoned;
	protected int _mAtkSpd, _pAtkSpd;
	protected int _runSpd, _walkSpd, _swimRunSpd, _swimWalkSpd, _flRunSpd, _flWalkSpd, _flyRunSpd, _flyWalkSpd;
	protected int _rhand, _lhand, _chest, _val;
	protected int _collisionHeight, _collisionRadius;
	protected String _name = "";
	protected String _title = "";

	public AbstractNpcInfo(L2Character cha)
	{
		_isSummoned = cha.isShowSummonAnimation();
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_heading = cha.getHeading();
		_mAtkSpd = cha.getMAtkSpd();
		_pAtkSpd = cha.getPAtkSpd();
		_runSpd = cha.getTemplate().getBaseRunSpd();
		_walkSpd = cha.getTemplate().getBaseWalkSpd();
		_swimRunSpd = _flRunSpd = _flyRunSpd = _runSpd;
		_swimWalkSpd = _flWalkSpd = _flyWalkSpd = _walkSpd;
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__22_NPCINFO;
	}

	/**
	 * Packet for Npcs
	 */
	public static class NpcInfo extends AbstractNpcInfo
	{
		private L2Npc _npc;

		public NpcInfo(L2Npc cha)
		{
			super(cha);
			_npc = cha;
			_idTemplate = cha.getTemplate().getIdTemplate(); // On every subclass
			_rhand = cha.getRightHandItem();  // On every subclass
			_lhand = cha.getLeftHandItem(); // On every subclass
			_collisionHeight = cha.getCollisionHeight(); // On every subclass
			_collisionRadius = cha.getCollisionRadius(); // On every subclass

			if (cha.getTemplate().isServerSideName())
				_name = cha.getTemplate().getName(); // On every subclass

			if (cha.isChampion())
			{
				_title = Config.CHAMPION_TITLE; // On every subclass
			}
			else if (cha.getTemplate().isServerSideTitle())
			{
				_title = cha.getTemplate().getTitle(); // On every subclass
			}
			else
			{
				_title = cha.getTitle(); // On every subclass
			}

			if (Config.SHOW_NPC_LVL && _npc instanceof L2MonsterInstance)
			{
				String t = "Lv " + cha.getLevel() + (cha.getAggroRange() > 0 ? "*" : "");
				if (_title != null && !_title.isEmpty())
					t += " " + _title;

				_title = t;
			}
		}

		@Override
		protected void writeImpl(L2GameClient client, L2PcInstance activeChar)
		{
			writeC(0x0c);
			writeD(_npc.getObjectId());
			writeD(_idTemplate + 1000000); // npctype id
			writeD(_npc.isAutoAttackable(activeChar) ? 1 : 0);
			writeD(_x);
			writeD(_y);
			writeD(_z);
			writeD(_heading);
			writeD(0x00);
			writeD(_mAtkSpd);
			writeD(_pAtkSpd);
			writeD(_runSpd);
			writeD(_walkSpd);
			writeD(_swimRunSpd); // swimspeed
			writeD(_swimWalkSpd); // swimspeed
			writeD(_flRunSpd);
			writeD(_flWalkSpd);
			writeD(_flyRunSpd);
			writeD(_flyWalkSpd);
			writeF(_npc.getStat().getMovementSpeedMultiplier());
			writeF(_npc.getStat().getAttackSpeedMultiplier());
			writeF(_collisionRadius);
			writeF(_collisionHeight);
			writeD(_rhand); // right hand weapon
			writeD(_chest);
			writeD(_lhand); // left hand weapon
			writeC(1); // name above char 1=true ... ??
			writeC(_npc.isRunning() ? 1 : 0);
			writeC(_npc.isInCombat() ? 1 : 0);
			writeC(_npc.isAlikeDead() ? 1 : 0);
			writeC(_isSummoned ? 2 : _val); // 0=teleported 1=default 2=summoned
			writeS(_name);
			writeS(_title);
			writeD(0x00); // Title color 0=client default
			writeD(0x00);
			writeD(0x00); // pvp flag
			
			writeD(_npc.getAbnormalEffect()); // C2
			writeD(0x00);
			
			writeD(0000); // C2
			writeD(0000); // C2
			writeD(0000); // C2
			writeC(0000); // C2
			writeC(0x00); // title color 0=client
			
			writeF(_collisionRadius);
			writeF(_collisionHeight);
			writeD(0x00); // C4
			writeD(0x00); // C6
			writeD(0x00);
			writeD(0x00);// CT1.5 Pet form and skills
		}
	}

	public static class TrapInfo extends AbstractNpcInfo
	{
		private L2Trap _trap;

		public TrapInfo(L2Trap cha)
		{
			super(cha);
			_trap = cha;
			_idTemplate = cha.getTemplate().getIdTemplate();
			_rhand = 0;
			_lhand = 0;
			_collisionHeight = _trap.getTemplate().getCollisionHeight();
			_collisionRadius = _trap.getTemplate().getCollisionRadius();
			_title = cha.getOwner().getName();
			_runSpd = _trap.getStat().getRunSpeed();
			_walkSpd = _trap.getStat().getWalkSpeed();
			_swimRunSpd = _flRunSpd = _flyRunSpd = _runSpd;
			_swimWalkSpd = _flWalkSpd = _flyWalkSpd = _walkSpd;
		}

		@Override
		protected void writeImpl(L2GameClient client, L2PcInstance activeChar)
		{
			writeC(0x0c);
			writeD(_trap.getObjectId());
			writeD(_idTemplate + 1000000);  // npctype id
			writeD(_trap.isAutoAttackable(activeChar) ? 1 : 0);
			writeD(_x);
			writeD(_y);
			writeD(_z);
			writeD(_heading);
			writeD(0x00);
			writeD(_mAtkSpd);
			writeD(_pAtkSpd);
			writeD(_runSpd);
			writeD(_walkSpd);
			writeD(_swimRunSpd);  // swimspeed
			writeD(_swimWalkSpd);  // swimspeed
			writeD(_flRunSpd);
			writeD(_flWalkSpd);
			writeD(_flyRunSpd);
			writeD(_flyWalkSpd);
			writeF(_trap.getStat().getMovementSpeedMultiplier());
			writeF(_trap.getStat().getAttackSpeedMultiplier());
			writeF(_collisionRadius);
			writeF(_collisionHeight);
			writeD(_rhand); // right hand weapon
			writeD(_chest);
			writeD(_lhand); // left hand weapon
			writeC(1);	// name above char 1=true ... ??
			writeC(_trap.isRunning() ? 1 : 0);
			writeC(_trap.isInCombat() ? 1 : 0);
			writeC(_trap.isAlikeDead() ? 1 : 0);
			writeC(_isSummoned ? 2 : _val); //  0=teleported  1=default   2=summoned
			writeS(_name);
			writeS(_title);
			writeD(0x00);  // title color 0 = client default

			writeD(0x00);
			writeD(0x00);  // pvp flag

			writeD(_trap.getAbnormalEffect());  // C2
			
			writeD(0x00);   // 0x01 only for summons
			writeD(0000);  // C2
			writeD(0000);  // C2
			writeD(0000);  // C2
			writeC(0000);  // C2

			writeC(0x00);  // Title color 0=client default 

			writeF(_collisionRadius);
			writeF(_collisionHeight);
			writeD(0x00);  // C4
			writeD(0x00);  // C6
			writeD(0x00);
			writeD(0);//CT1.5 Pet form and skills
		}
	}

	/**
	 * Packet for Decoys
	 */
	public static class DecoyInfo extends AbstractNpcInfo
	{
		private L2Decoy _decoy;

		public DecoyInfo(L2Decoy cha)
		{
			super(cha);

			if (_idTemplate <= 13070 || _idTemplate >= 13077)
			{
				if (Config.ASSERT)
					throw new AssertionError("Using DecoyInfo packet with an unsupported decoy template");
				else
					throw new IllegalArgumentException("Using DecoyInfo packet with an unsupported decoy template");
			}
			
			_decoy = cha;
			_idTemplate = cha.getTemplate().getIdTemplate();
			_heading = cha.getOwner().getHeading();
			// _mAtkSpd = cha.getMAtkSpd(); on abstract constructor
			_pAtkSpd = cha.getOwner().getPAtkSpd();
			_runSpd = cha.getOwner().getStat().getRunSpeed();
			_walkSpd = cha.getOwner().getStat().getWalkSpeed();
			_swimRunSpd = _flRunSpd = _flyRunSpd = _runSpd;
			_swimWalkSpd = _flWalkSpd = _flyWalkSpd = _walkSpd;
		}

		@Override
		protected void writeImpl()
		{
			L2PcInstance owner = _decoy.getOwner();
			Inventory inv = owner.getInventory();

			writeC(0x31);
			writeD(_x);
			writeD(_y);
			writeD(_z);
			writeD(_heading);
			writeD(_decoy.getObjectId());
			writeS(owner.getAppearance().getVisibleName());
			writeD(owner.getRace().ordinal());
			writeD(owner.getAppearance().getSex() ? 1 : 0);

			if (owner.getClassIndex() == 0)
				writeD(owner.getClassId().getId());
			else
				writeD(owner.getBaseClass());

			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_HAIRALL));
			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_FEET));
			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_BACK));
			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_LRHAND));
			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_HAIR));
			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_HAIR2));

			// T1 new d's
			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_RBRACELET));
			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_LBRACELET));
			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_DECO1));
			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_DECO2));
			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_DECO3));
			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_DECO4));
			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_DECO5));
			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_DECO6));
			// end of t1 new d's

			// c6 new h's
			writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_HAIRALL));
			writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_HEAD));
			writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_RHAND));
			writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_LHAND));
			writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_GLOVES));
			writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_CHEST));
			writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_LEGS));
			writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_FEET));
			writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_BACK));
			writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_LRHAND));
			writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_HAIR));
			writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_HAIR2));

			// T1 new h's 
			writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_RBRACELET));
			writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_LBRACELET));
			writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_DECO1));
			writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_DECO2));
			writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_DECO3));
			writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_DECO4));
			writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_DECO5));
			writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_DECO6));
			// end of t1 new h's 

			writeD(owner.getPvpFlag());
			writeD(owner.getKarma());

			writeD(_mAtkSpd);
			writeD(_pAtkSpd);

			writeD(owner.getPvpFlag());
			writeD(owner.getKarma());

			writeD(_runSpd);
			writeD(_walkSpd);
			writeD(50); // swimspeed
			writeD(50); // swimspeed
			writeD(_flRunSpd);
			writeD(_flWalkSpd);
			writeD(_flyRunSpd);
			writeD(_flyWalkSpd);
			writeF(owner.getStat().getMovementSpeedMultiplier()); //_activeChar.getProperMultiplier()
			writeF(owner.getStat().getAttackSpeedMultiplier()); // _activeChar.getAttackSpeedMultiplier

			L2Summon pet = _decoy.getPet();
			L2Transformation trans;
			if (owner.getMountType() != 0 && pet != null)
			{
				writeF(pet.getTemplate().getCollisionRadius());
				writeF(pet.getTemplate().getCollisionHeight());
			}
			else if ((trans = owner.getTransformation()) != null)
			{
				writeF(trans.getCollisionRadius(owner));
				writeF(trans.getCollisionHeight(owner));
			}
			else if (owner.getAppearance().getSex())
			{
				writeF(owner.getBaseTemplate().getFCollisionRadius());
				writeF(owner.getBaseTemplate().getFCollisionHeight());
			}
			else
			{
				writeF(owner.getBaseTemplate().getCollisionRadius());
				writeF(owner.getBaseTemplate().getCollisionHeight());
			}

			writeD(owner.getAppearance().getHairStyle());
			writeD(owner.getAppearance().getHairColor());
			writeD(owner.getAppearance().getFace());

			writeS(owner.getAppearance().getVisibleTitle());
			
			writeD(owner.getClanId());
			writeD(owner.getClanCrestId());
			writeD(owner.getAllyId());
			writeD(owner.getAllyCrestId());
			// In UserInfo leader rights and siege flags, but here found nothing??
			// Therefore RelationChanged packet with that info is required
			writeD(0);

			writeC(owner.isSitting() ? 0 : 1); // standing = 1 sitting = 0
			writeC(owner.isRunning() ? 1 : 0); // running = 1 walking = 0
			writeC(owner.isInCombat() ? 1 : 0);
			writeC(owner.isAlikeDead() ? 1 : 0);

			writeC(owner.getAppearance().isInvisible() ? 1 : 0); // invisible = 1 visible =0

			writeC(owner.getMountType()); // 1 on strider 2 on wyvern 3 on Great Wolf 0 no mount
			writeC(owner.getPrivateStoreType()); // 1 - sellshop

			writeH(owner.getCubics().size());
			for (int id : owner.getCubics().keySet())
				writeH(id);

			writeC(0x00); // find party members

			writeD(owner.getAbnormalEffect());

			writeC(owner.getEvaluations()); // Changed by Thorgrim
			writeH(owner.getEvalPoints()); // Blue value for name (0 = white, 255 = pure blue)
			writeD(owner.getClassId().getId());

			writeD(owner.getMaxCp());
			writeD((int) owner.getCurrentCp());
			writeC(owner.isMounted() ? 0 : owner.getEnchantEffect());

			if (owner.getTeam() == 1)
				writeC(0x01); // team circle around feet 1= Blue, 2 = red
			else if (owner.getTeam() == 2)
				writeC(0x02); // team circle around feet 1= Blue, 2 = red
			else
				writeC(0x00); // team circle around feet 1= Blue, 2 = red

			writeD(owner.getClanCrestLargeId());
			writeC(owner.isNoble() ? 1 : 0); // Symbol on char menu ctrl+I
			writeC(owner.isHero() ? 1 : 0); // Hero Aura

			writeC(owner.isFishing() ? 1 : 0); // 0x01: Fishing Mode (Cant be undone by setting back to 0)
			writeD(owner.getFishx());
			writeD(owner.getFishy());
			writeD(owner.getFishz());

			writeD(owner.getAppearance().getNameColor());

			writeD(_heading);

			writeD(owner.getPledgeClass());
			writeD(owner.getSubPledgeType());

			writeD(owner.getAppearance().getTitleColor());

			if (owner.isCursedWeaponEquipped())
				writeD(CursedWeaponsManager.getInstance().getLevel(owner.getCursedWeaponEquippedId()));
			else
				writeD(0x00);

			// T1 
			if (owner.getClan() != null)
				writeD(owner.getClan().getReputationScore());
			else
				writeD(0x00);

			// T1
			writeD(0x00); // Can Decoys be transformed?
			writeD(0x00); // Can Decoys have Agathions?
		}
	}

	/**
	 * Packet for summons
	 */
	public static class SummonInfo extends AbstractNpcInfo
	{
		private L2Summon _summon;
		private int _form = 0;

		public SummonInfo(L2Summon cha, int val)
		{
			super(cha);
			_summon = cha;
			_val = val;

			int npcId = cha.getTemplate().getNpcId();

			if (npcId == 16041 || npcId == 16042)
			{
				if (cha.getLevel() > 84)
					_form = 3;
				else if (cha.getLevel() > 79) 
					_form = 2;
				else if (cha.getLevel() > 74)
					_form = 1;
			}
			else if (npcId == 16025 || npcId == 16037)
			{
				if (cha.getLevel() > 69)
					_form = 3;
				else if (cha.getLevel() > 64) 
					_form = 2;
				else if (cha.getLevel() > 59) 
					_form = 1;
			}

			// fields not set on AbstractNpcInfo
			_rhand = cha.getWeapon();
			_lhand = 0;
			_chest = cha.getArmor();
			_name = cha.getName();
			_title = cha.getOwner() != null ? (cha.getOwner().isOnline() == 0 ? "" : cha.getOwner().getName()) : ""; // when owner online, summon will show in title owner name
			_idTemplate = cha.getTemplate().getIdTemplate();

			// few fields needing fix from AbstractNpcInfo
			_runSpd = cha.getPetSpeed();
			_walkSpd = cha.isMountable() ? 45 : 30;
			_swimRunSpd = _flRunSpd = _flyRunSpd = _runSpd;
			_swimWalkSpd = _flWalkSpd = _flyWalkSpd = _walkSpd;
		}
		
		@Override
		protected void writeImpl(L2GameClient client, L2PcInstance activeChar)
		{
			if (_summon.getOwner() != null && _summon.getOwner().getAppearance().isInvisible())
				return; // TODO get his out of here

			writeC(0x0c);
			writeD(_summon.getObjectId());
			writeD(_idTemplate + 1000000);  // npctype id
			writeD(_summon.isAutoAttackable(activeChar) ? 1 : 0);
			writeD(_x);
			writeD(_y);
			writeD(_z);
			writeD(_heading);
			writeD(0x00);
			writeD(_mAtkSpd);
			writeD(_pAtkSpd);
			writeD(_runSpd);
			writeD(_walkSpd);
			writeD(_swimRunSpd);  // swimspeed
			writeD(_swimWalkSpd);  // swimspeed
			writeD(_flRunSpd);
			writeD(_flWalkSpd);
			writeD(_flyRunSpd);
			writeD(_flyWalkSpd);
			writeF(_summon.getStat().getMovementSpeedMultiplier());
			writeF(_summon.getStat().getAttackSpeedMultiplier());
			writeF(_collisionRadius);
			writeF(_collisionHeight);
			writeD(_rhand); // right hand weapon
			writeD(_chest);
			writeD(_lhand); // left hand weapon
			writeC(1);	// name above char 1=true ... ??
			writeC(1);
			writeC(_summon.isInCombat() ? 1 : 0);
			writeC(_summon.isAlikeDead() ? 1 : 0);
			writeC(_isSummoned ? 2 : _val); //  0=teleported  1=default   2=summoned
			writeS(_name);
			writeS(_title);
			writeD(0x01);// Title color 0=client default

			writeD(0);
			writeD(_summon.getOwner().getPvpFlag());

			writeD(_summon.getAbnormalEffect());  // C2
			writeD(0x01);
			writeD(0000);  // C2
			writeD(0000);  // C2
			writeD(0000);  // C2
			writeC(0000);  // C2

			writeC(_summon.getOwner().getTeam());
			writeF(_collisionRadius);
			writeF(_collisionHeight);
			writeD(0x00);  // C4
			writeD(0x00);  // C6
			writeD(0x00);
			writeD(_form);//CT1.5 Pet form and skills
		}

		@Override
		public boolean canBeSentTo(L2GameClient client, L2PcInstance activeChar)
		{
			return _summon.getOwner() != activeChar;
		}
	}
}
