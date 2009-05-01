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
package com.l2jfree.gameserver.handler.itemhandlers;

import com.l2jfree.Config;
import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.datatables.NpcTable;
import com.l2jfree.gameserver.datatables.SummonItemsData;
import com.l2jfree.gameserver.handler.IItemHandler;
import com.l2jfree.gameserver.idfactory.IdFactory;
import com.l2jfree.gameserver.instancemanager.ClanHallManager;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2Spawn;
import com.l2jfree.gameserver.model.L2SummonItem;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.L2Playable;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfree.gameserver.model.entity.ClanHall;
import com.l2jfree.gameserver.model.entity.events.CTF;
import com.l2jfree.gameserver.model.entity.events.DM;
import com.l2jfree.gameserver.model.entity.events.TvT;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.MagicSkillLaunched;
import com.l2jfree.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jfree.gameserver.network.serverpackets.PetItemList;
import com.l2jfree.gameserver.network.serverpackets.SetupGauge;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;
import com.l2jfree.gameserver.util.Broadcast;
import com.l2jfree.gameserver.util.FloodProtector;
import com.l2jfree.gameserver.util.FloodProtector.Protected;

/**
 * 
 * @author FBIagent
 * 
 */
public class SummonItems implements IItemHandler
{
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;

		L2PcInstance activeChar = (L2PcInstance) playable;

		if (!FloodProtector.tryPerformAction(activeChar, Protected.ITEMPETSUMMON))
			return;

		if ((activeChar._inEventTvT && TvT._started && !Config.TVT_ALLOW_SUMMON) || (activeChar._inEventCTF && CTF._started && !Config.CTF_ALLOW_SUMMON)
				|| (activeChar._inEventDM && DM._started && !Config.DM_ALLOW_SUMMON))
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (activeChar.isSitting())
		{
			activeChar.sendPacket(SystemMessageId.CANT_MOVE_SITTING);
			return;
		}

		if (activeChar.inObserverMode())
			return;

		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
			return;
		}

		if (activeChar.isAllSkillsDisabled() || activeChar.isCastingNow())
			return;

		L2SummonItem sitem = SummonItemsData.getInstance().getSummonItem(item.getItemId());

		if ((activeChar.getPet() != null || activeChar.isMounted()) && sitem.isPetSummon())
		{
			activeChar.sendPacket(SystemMessageId.YOU_ALREADY_HAVE_A_PET);
			return;
		}

		if (activeChar.isAttackingNow())
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_SUMMON_IN_COMBAT);
			return;
		}

		if (activeChar.isCursedWeaponEquipped() && sitem.isPetSummon())
		{
			activeChar.sendPacket(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE);
			return;
		}

		int npcID = sitem.getNpcId();

		if (npcID == 0)
			return;

		L2NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(npcID);

		if (npcTemplate == null)
			return;

		activeChar.stopMove(null, false);

		// Restricting Red Striders/Snow Wolves/Snow Fenrir
		if (!Config.ALT_SPECIAL_PETS_FOR_ALL)
		{
			int _itemId = item.getItemId();
			if ((_itemId == 10307 || _itemId == 10611 || _itemId == 10308 || _itemId == 10309 || _itemId == 10310) && !activeChar.isGM())
			{
				if (activeChar.getClan() != null && ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan()) != null)
				{
					ClanHall clanHall = ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan());
					
					int clanHallId = clanHall.getId();
					if ( (clanHallId < 36 || clanHallId > 41) && (clanHallId < 51 || clanHallId > 57) )
					{
						activeChar.sendMessage("Cannot use special pets if you're not member of a clan that is owning a clanhall in Aden or Rune");
						return;
					}
				}
				else
				{
					activeChar.sendMessage("Cannot use special pets if you're not member of a clan that is owning a clanhall in Aden or Rune");
					return;
				}
			}
		}

		switch (sitem.getType())
		{
		case 0: // Static Summons (like christmas tree)
			try
			{
				L2Spawn spawn = new L2Spawn(npcTemplate);

				spawn.setId(IdFactory.getInstance().getNextId());
				spawn.setLocx(activeChar.getX());
				spawn.setLocy(activeChar.getY());
				spawn.setLocz(activeChar.getZ());
				L2World.getInstance().storeObject(spawn.spawnOne(true));
				activeChar.destroyItem("Summon", item.getObjectId(), 1, null, false);
				activeChar.sendMessage("Created " + npcTemplate.getName() + " at x: " + spawn.getLocx() + " y: " + spawn.getLocy() + " z: " + spawn.getLocz());
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Target is not ingame.");
			}

			break;
		case 1: // Pet Summons
			Broadcast.toSelfAndKnownPlayersInRadius(activeChar, new MagicSkillUse(activeChar, activeChar, 2046, 1, 5000, 0), 2000);
			activeChar.sendPacket(new SetupGauge(0, 5000));

			activeChar.sendPacket(SystemMessageId.SUMMON_A_PET);
			activeChar.setIsCastingNow(true);

			ThreadPoolManager.getInstance().scheduleGeneral(new PetSummonFinalizer(activeChar, npcTemplate, item), 5000);
			break;
		case 2: // Wyvern
			activeChar.mount(sitem.getNpcId(), item.getObjectId(), true);
			break;
		case 3: // Great Wolf
			activeChar.mount(sitem.getNpcId(), item.getObjectId(), false);
			break;
		case 4: // Light Purple Maned Horse
			activeChar.mount(sitem.getNpcId(), item.getObjectId(), false);
			break;
		}
	}

	static class PetSummonFeedWait implements Runnable
	{
		private L2PcInstance	_activeChar;
		private L2PetInstance	_petSummon;

		PetSummonFeedWait(L2PcInstance activeChar, L2PetInstance petSummon)
		{
			_activeChar = activeChar;
			_petSummon = petSummon;
		}

		public void run()
		{
			try
			{
				if (_petSummon.getCurrentFed() <= 0)
					_petSummon.unSummon(_activeChar);
				else
					_petSummon.startFeed();
			}
			catch (Exception e)
			{
				_log.error(e.getMessage(), e);
			}
		}
	}

	static class PetSummonFinalizer implements Runnable
	{
		private L2PcInstance	_activeChar;
		private L2ItemInstance _item;
		private L2NpcTemplate _npcTemplate;

		PetSummonFinalizer(L2PcInstance activeChar, L2NpcTemplate npcTemplate, L2ItemInstance item)
		{
			_activeChar = activeChar;
			_npcTemplate = npcTemplate;
			_item = item;
		}

		public void run()
		{
			try
			{
				_activeChar.sendPacket(new MagicSkillLaunched(_activeChar, 2046, 1));
				_activeChar.setIsCastingNow(false);
				L2PetInstance petSummon = L2PetInstance.spawnPet(_npcTemplate, _activeChar, _item);

				if (petSummon == null)
					return;

				petSummon.setTitle(_activeChar.getName());

				if (!petSummon.isRespawned())
				{
					petSummon.getStatus().setCurrentHp(petSummon.getMaxHp());
					petSummon.getStatus().setCurrentMp(petSummon.getMaxMp());
					petSummon.getStat().setExp(petSummon.getExpForThisLevel());
					petSummon.setCurrentFed(petSummon.getMaxFed());
				}

				petSummon.setRunning();

				if (!petSummon.isRespawned())
					petSummon.store();

				_activeChar.setPet(petSummon);

				L2World.getInstance().storeObject(petSummon);
				petSummon.spawnMe(_activeChar.getX() + 50, _activeChar.getY() + 100, _activeChar.getZ());
				petSummon.startFeed();
				_item.setEnchantLevel(petSummon.getLevel());

				if (petSummon.getCurrentFed() <= 0)
					ThreadPoolManager.getInstance().scheduleGeneral(new PetSummonFeedWait(_activeChar, petSummon), 60000);
				else
					petSummon.startFeed();

				petSummon.setFollowStatus(true);
				petSummon.setShowSummonAnimation(false); // shouldn't be this always true?
				int weaponId = petSummon.getWeapon();
				int armorId = petSummon.getArmor();
				int jewelId = petSummon.getJewel();
				if (weaponId > 0 && petSummon.getOwner().getInventory().getItemByItemId(weaponId)!= null)
				{
					L2ItemInstance item = petSummon.getOwner().getInventory().getItemByItemId(weaponId);
					L2ItemInstance newItem = petSummon.getOwner().transferItem("Transfer", item.getObjectId(), 1, petSummon.getInventory(), petSummon); 
					if (newItem == null)
					{
						_log.warn("Invalid item transfer request: " + petSummon.getName() + "(pet) --> " + petSummon.getOwner().getName());
						petSummon.setWeapon(0);
					}
					else
						petSummon.getInventory().equipItem(newItem);
				}
				else
					petSummon.setWeapon(0);
				if (armorId > 0 && petSummon.getOwner().getInventory().getItemByItemId(armorId)!= null)
				{
					L2ItemInstance item = petSummon.getOwner().getInventory().getItemByItemId(armorId);
					L2ItemInstance newItem = petSummon.getOwner().transferItem("Transfer", item.getObjectId(), 1, petSummon.getInventory(), petSummon); 
					if (newItem == null)
					{
						_log.warn("Invalid item transfer request: " + petSummon.getName() + "(pet) --> " + petSummon.getOwner().getName());
						petSummon.setArmor(0);
					}
					else
						petSummon.getInventory().equipItem(newItem);
				}
				else
					petSummon.setArmor(0);
				if (jewelId > 0 && petSummon.getOwner().getInventory().getItemByItemId(jewelId)!= null)
				{
					L2ItemInstance item = petSummon.getOwner().getInventory().getItemByItemId(jewelId);
					L2ItemInstance newItem = petSummon.getOwner().transferItem("Transfer", item.getObjectId(), 1, petSummon.getInventory(), petSummon); 
					if (newItem == null)
					{
						_log.warn("Invalid item transfer request: " + petSummon.getName() + "(pet) --> " + petSummon.getOwner().getName());
						petSummon.setJewel(0);
					}
					else
						petSummon.getInventory().equipItem(newItem);
				}
				else
					petSummon.setJewel(0);
				petSummon.getOwner().sendPacket(new PetItemList(petSummon));
				petSummon.broadcastStatusUpdate();
			}
			catch (Exception e)
			{
				_log.error(e.getMessage(), e);
			}
		}
	}

	public int[] getItemIds()
	{
		return SummonItemsData.getInstance().itemIDs();
	}
}