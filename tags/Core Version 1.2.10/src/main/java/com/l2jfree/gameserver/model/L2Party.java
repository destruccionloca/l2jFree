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
package com.l2jfree.gameserver.model;

import java.util.List;

import javolution.util.FastList;

import com.l2jfree.Config;
import com.l2jfree.gameserver.SevenSignsFestival;
import com.l2jfree.gameserver.datatables.ItemTable;
import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.instancemanager.DuelManager;
import com.l2jfree.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfree.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jfree.gameserver.model.entity.DimensionalRift;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.CreatureSay;
import com.l2jfree.gameserver.network.serverpackets.ExCloseMPCC;
import com.l2jfree.gameserver.network.serverpackets.ExMultiPartyCommandChannelInfo;
import com.l2jfree.gameserver.network.serverpackets.ExOpenMPCC;
import com.l2jfree.gameserver.network.serverpackets.ExPartyPetWindowAdd;
import com.l2jfree.gameserver.network.serverpackets.ExPartyPetWindowDelete;
import com.l2jfree.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jfree.gameserver.network.serverpackets.PartySmallWindowAdd;
import com.l2jfree.gameserver.network.serverpackets.PartySmallWindowAll;
import com.l2jfree.gameserver.network.serverpackets.PartySmallWindowDelete;
import com.l2jfree.gameserver.network.serverpackets.PartySmallWindowDeleteAll;
import com.l2jfree.gameserver.network.serverpackets.PartySmallWindowUpdate;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.skills.Stats;
import com.l2jfree.gameserver.util.Util;
import com.l2jfree.tools.random.Rnd;

/**
 * This class ...
 * 
 * @author nuocnam
 * @version $Revision: 1.6.2.2.2.6 $ $Date: 2005/04/11 19:12:16 $
 */
public class L2Party
{
	private static final double[] BONUS_EXP_SP = {1, 1.30, 1.39, 1.50, 1.54, 1.58, 1.63, 1.67, 1.71};

	private FastList<L2PcInstance> _members = null;
	private int _pendingInvitation = 0;       // Number of players that already have been invited (but not replied yet)
	private int _partyLvl = 0;
	private int _itemDistribution = 0;
	private int _itemLastLoot = 0;
	private L2CommandChannel _commandChannel = null;

	private DimensionalRift _dr;

	public static final int ITEM_LOOTER = 0;
	public static final int ITEM_RANDOM = 1;
	public static final int ITEM_RANDOM_SPOIL = 2;
	public static final int ITEM_ORDER = 3;
	public static final int ITEM_ORDER_SPOIL = 4;

	/**
	 * constructor ensures party has always one member - leader
	 * @param leader
	 * @param itemDistributionMode
	 */
	public L2Party(L2PcInstance leader, int itemDistribution) 
	{
		_itemDistribution = itemDistribution;
		getPartyMembers().add(leader);
		_partyLvl = leader.getLevel();
	}
	
	/**
	 * returns number of party members
	 * @return
	 */
	public int getMemberCount() { return getPartyMembers().size(); }

    /**
     * returns number of players that already been invited, but not replied yet
     * @return
     */
    public int getPendingInvitationNumber() { return _pendingInvitation; }
    
    /**
     * decrease number of players that already been invited but not replied yet
     * happens when: player join party or player decline to join
     */
    public void decreasePendingInvitationNumber() { _pendingInvitation--; }
    
    /**
     * increase number of players that already been invite but not replied yet
     */
    public void increasePendingInvitationNumber() { _pendingInvitation++; }
    
	/**
	 * returns all party members
	 * @return
	 */
	public synchronized FastList<L2PcInstance> getPartyMembers()
	{
		if (_members == null)
			_members = new FastList<L2PcInstance>();
		return _members;
	}

	/**
	 * get random member from party
	 * @return
	 */
	//private L2PcInstance getRandomMember() { return getPartyMembers().get(Rnd.get(getPartyMembers().size())); }
	private L2PcInstance getCheckedRandomMember(int ItemId, L2Character target)
	{
		List<L2PcInstance> availableMembers = new FastList<L2PcInstance>();
		for (L2PcInstance member : getPartyMembers())
		{
			if (member.getInventory().validateCapacityByItemId(ItemId) &&
				Util.checkIfInRange(Config.ALT_PARTY_RANGE2, target, member, true)) availableMembers.add(member);
		}
		if (availableMembers.size() > 0)
			return availableMembers.get(Rnd.get(availableMembers.size()));

		return null;
	}

	/**
	 * get next item looter
	 * @return
	 */
	/*private L2PcInstance getNextLooter()
	{
		_itemLastLoot++;
		if (_itemLastLoot > getPartyMembers().size() -1) _itemLastLoot = 0;
		
		return (getPartyMembers().size() > 0) ? getPartyMembers().get(_itemLastLoot) : null;
	}*/
	private L2PcInstance getCheckedNextLooter(int ItemId, L2Character target)
	{
		for (int i = 0; i < getMemberCount(); i++)
		{
			if (++_itemLastLoot >= getMemberCount())
				_itemLastLoot = 0;
			L2PcInstance member;
			try 
			{ 
				member = getPartyMembers().get(_itemLastLoot); 
				if (member.getInventory().validateCapacityByItemId(ItemId) &&
					Util.checkIfInRange(Config.ALT_PARTY_RANGE2, target, member, true)) return member;
			} 
			catch (Exception e)
			{
				// continue, take another member if this just logged off
			}
		}
		
		return null;
	}
	
	/**
	 * get next item looter
	 * @return
	 */
	private L2PcInstance getActualLooter(L2PcInstance player, int ItemId, boolean spoil, L2Character target)
	{
		L2PcInstance looter = player;
        
        switch (_itemDistribution)
        {
            case ITEM_RANDOM:
                if (!spoil) looter = getCheckedRandomMember(ItemId, target);
                break;
            case ITEM_RANDOM_SPOIL:
                looter = getCheckedRandomMember(ItemId, target);
                break;
            case ITEM_ORDER:
                if (!spoil) looter = getCheckedNextLooter(ItemId, target);
                break;
            case ITEM_ORDER_SPOIL:
                looter = getCheckedNextLooter(ItemId, target);
                break;
        }
        
        if (looter == null) looter = player;
        return looter;
	}
		
	/**
	 * true if player is party leader
	 * @param player
	 * @return
	 */
	public boolean isLeader(L2PcInstance player) { return (getLeader().equals(player)); }
    
    /**
     * Returns the Object ID for the party leader to be used as a unique identifier of this party
     * @return int 
     */
    public int getPartyLeaderOID() { return getLeader().getObjectId(); }
		
	/**
	 * Broadcasts packet to every party member 
	 * @param msg
	 */
	public void broadcastToPartyMembers(L2GameServerPacket msg) 
	{
		for (L2PcInstance member : getPartyMembers())
		{
			if (member != null)
				member.sendPacket(msg);
		}
	}

	public void broadcastCSToPartyMembers(CreatureSay msg, L2PcInstance broadcaster)
	{
		for (L2PcInstance member : getPartyMembers())
		{
			if (!(Config.REGION_CHAT_ALSO_BLOCKED && BlockList.isBlocked(member, broadcaster)))
				member.sendPacket(msg);
		}
	}

	/**
	 * Broadcasts packet to every party member 
	 * @param msg
	 */
	public void broadcastSnoopToPartyMembers(int type, String name, String text)
	{
		for (L2PcInstance member : getPartyMembers())
		{
			if (member == null)
				continue;
			member.broadcastSnoop(type, name, text);
		}
	}
	
	/**
	 * Send a Server->Client packet to all other L2PcInstance of the Party.<BR><BR>
	 */
	public void broadcastToPartyMembers(L2PcInstance player, L2GameServerPacket msg) 
    {
		for (L2PcInstance member : getPartyMembers())
		{
			if (member != null && !member.equals(player))
				member.sendPacket(msg);
		}
	}
	
	/**
	 * adds new member to party
	 * @param player
	 */
	public boolean addPartyMember(L2PcInstance player) 
	{
		if (Config.MAX_PARTY_LEVEL_DIFFERENCE > 0)
		{
			int _min = _partyLvl;
			int _max = _partyLvl;
			
			boolean invalidMember = false;
			
			for (int i = 0; i < getMemberCount(); i++)
			{
				int _lvl = getPartyMembers().get(i).getLevel();
				if (_lvl<_min) _min = _lvl;
				if (_lvl>_max) _max = _lvl;
			}
			
			if (player.getLevel()>_max)
				invalidMember = (player.getLevel() - _min)>Config.MAX_PARTY_LEVEL_DIFFERENCE;
			if (player.getLevel()<_min)
				invalidMember = (_max - player.getLevel())>Config.MAX_PARTY_LEVEL_DIFFERENCE;

			if (invalidMember)
			{
				getLeader().sendMessage("Level difference too high, you can't invite "+player.getName()+" this party.");
				player.sendMessage("Level difference too high, you can't join this party.");
				return false;
			}
		}

		//sends new member party window for all members
		//we do all actions before adding member to a list, this speeds things up a little
		player.sendPacket(new PartySmallWindowAll(player, getPartyMembers()));

		// sends pets/summons of party members
		L2Summon summon;
		for (L2PcInstance pMember : getPartyMembers())
		{
			if ((summon = pMember.getPet()) != null)
			{
				player.sendPacket(new ExPartyPetWindowAdd(summon));
			}
		}

		SystemMessage msg = new SystemMessage(SystemMessageId.YOU_JOINED_S1_PARTY);
		msg.addString(getLeader().getName());
		player.sendPacket(msg);
		
		msg = new SystemMessage(SystemMessageId.S1_JOINED_PARTY);
		msg.addString(player.getName());
		broadcastToPartyMembers(msg);
		broadcastToPartyMembers(new PartySmallWindowAdd(player));

		// if member has pet/summon add it to other as well
		if (player.getPet() != null)
		{
			broadcastToPartyMembers(new ExPartyPetWindowAdd(player.getPet()));
		}

		//add player to party, adjust party level
		getPartyMembers().add(player);
		if (player.getLevel() > _partyLvl)
		{
			_partyLvl = player.getLevel();
		}
		//update partySpelled 
		for (L2PcInstance member : getPartyMembers())
		{
			member.updateEffectIcons();
			summon = member.getPet();
			if (summon != null)
			{
				summon.updateEffectIcons();
			}
		}

		if (isInDimensionalRift())
		{
			_dr.partyMemberInvited();
		}

		// open the CCInformationwindow 
		if (isInCommandChannel())
		{
			player.sendPacket(ExOpenMPCC.STATIC_PACKET);
			getCommandChannel().broadcastToChannelMembers(new ExMultiPartyCommandChannelInfo(getCommandChannel()));
		}

		return true;
	}
	
	/**
	 * removes player from party
	 * @param player
	 */
	public void removePartyMember(L2PcInstance player) 
	{
		if (getPartyMembers().contains(player)) 
		{
			getPartyMembers().remove(player);
			recalculatePartyLevel();
			
			if (player.isFestivalParticipant())
				SevenSignsFestival.getInstance().updateParticipants(player, this);

			if(player.isInDuel())
				DuelManager.getInstance().onRemoveFromParty(player);

			try
			{
				if (player.getForceBuff() != null)
					player.abortCast();

				for (L2Character character : player.getKnownList().getKnownCharacters())
					if (character.getForceBuff() != null && character.getForceBuff().getTarget() == player)
						character.abortCast();
			}
			catch (Exception e){
				e.printStackTrace();
			}

			SystemMessage msg = new SystemMessage(SystemMessageId.YOU_LEFT_PARTY);
			player.sendPacket(msg);
			player.sendPacket(new PartySmallWindowDeleteAll());
			player.setParty(null);
			
			msg = new SystemMessage(SystemMessageId.S1_LEFT_PARTY);
			msg.addString(player.getName());
			broadcastToPartyMembers(msg);
			broadcastToPartyMembers(new PartySmallWindowDelete(player));
			L2Summon summon = player.getPet();
			if (summon != null)
			{
				broadcastToPartyMembers(new ExPartyPetWindowDelete(summon));
			}

			if (isInDimensionalRift())
				_dr.partyMemberExited(player);

			if (getPartyMembers().size() == 1)
			{
				if (isInCommandChannel())
				{
					L2CommandChannel cmd = getCommandChannel();
					getCommandChannel().removeParty(this);
					cmd.broadcastToChannelMembers(new SystemMessage(SystemMessageId.S1_PARTY_LEFT_COMMAND_CHANNEL)
																		.addString(getLeader().getName()));
				}
				L2PcInstance leader = getLeader();
				if (leader != null)
				{
					leader.setParty(null);
					if (leader.isInDuel())
						DuelManager.getInstance().onRemoveFromParty(leader);
					if (player.isFestivalParticipant())
						SevenSignsFestival.getInstance().updateParticipants(player, this);
					leader.sendPacket(new PartySmallWindowDeleteAll());
				}
			}
			else
			{
				if (isInCommandChannel())
				{
					player.sendPacket(ExCloseMPCC.STATIC_PACKET);
					getCommandChannel().broadcastToChannelMembers(new ExMultiPartyCommandChannelInfo(getCommandChannel()));
				}
			}
		}
	}
	
	/**
	 * Change party leader (used for string arguments)
	 * @param name
	 */
	
	public void changePartyLeader(String name)
	{
		L2PcInstance player = getPlayerByName(name);
		L2PcInstance leader = getLeader();

		if (player != null && !player.isInDuel())
		{
			if (getPartyMembers().contains(player))
			{
				if (leader == player)
				{
					player.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_TRANSFER_RIGHTS_TO_YOURSELF));
				}
				else
				{
					//Swap party members
					int p1 = getPartyMembers().indexOf(player);
					getPartyMembers().set(0, getPartyMembers().get(p1));
					getPartyMembers().set(p1, leader);

					SystemMessage msg = new SystemMessage(SystemMessageId.S1_HAS_BECOME_A_PARTY_LEADER);
					msg.addString(getLeader().getName());
					broadcastToPartyMembers(msg);
					broadcastToPartyMembers(new PartySmallWindowUpdate(getLeader()));
					if (isInCommandChannel() && getCommandChannel().getChannelLeader() == leader)
					{
						_commandChannel.setChannelLeader(getLeader());
						_commandChannel.broadcastToChannelMembers(new SystemMessage(SystemMessageId.COMMAND_CHANNEL_LEADER_NOW_S1)
							.addString(getLeader().getName()));
					}
				}
			}
			else
			{
				player.sendPacket(new SystemMessage(SystemMessageId.YOU_CAN_TRANSFER_RIGHTS_ONLY_TO_ANOTHER_PARTY_MEMBER));
			}
		}
		
	}
	
	/**
	 * Used to refresh the party view window for all party members.
	 */
	public void refreshPartyView()
	{
		//delete view for all members:
		broadcastToPartyMembers(new PartySmallWindowDeleteAll());

		//rebuild the view for all members:
		for (L2PcInstance player : getPartyMembers())
		{
			if (isLeader(player))
			{
				for (L2PcInstance p : getPartyMembers(player,true))
					player.sendPacket(new PartySmallWindowAdd(p));
			}
			else
			{
				player.sendPacket(new PartySmallWindowAll(player, getPartyMembers()));
			}
		}
		for (L2PcInstance player : getPartyMembers())
		{
			if(player!=null)
				player.updateEffectIcons();
		}
	}
	
	/**
	 * returns all party members except for player
	 * @param receives the L2PcInstance player that needs the view
	 * @param receives boolean is the player a party leader
	 * @return List<L2PcInstance> of the party members that need to be shown
	 */
	public FastList<L2PcInstance> getPartyMembers(L2PcInstance player, boolean leader)
	{
		try
		{
			if (_members == null)
				return getPartyMembers();

			FastList<L2PcInstance> list = new FastList<L2PcInstance>();
			for (L2PcInstance p : _members)
			{
				if (p != player) 
					list.add(p);
			}
			if(!leader) 
				list.add(player);
			return list;
		}
		catch(Throwable t)
		{
			return getPartyMembers();
		}
	}

	/**
	 * finds a player in the party by name
	 * @param name
	 * @return
	 */
	private L2PcInstance getPlayerByName(String name) 
    {
		for (L2PcInstance member : getPartyMembers())
		{
			if (member.getName().equals(name)) return member;
		}
		return null;
	}

	/**
	 * Oust player from party
	 * @param player
	 */
	public void oustPartyMember(L2PcInstance player) 
	{
		if (getPartyMembers().contains(player)) 
		{
			if (isLeader(player))
			{
				removePartyMember(player);
				if (getPartyMembers().size() > 1)
				{
					SystemMessage msg = new SystemMessage(SystemMessageId.S1_HAS_BECOME_A_PARTY_LEADER);
					msg.addString(getLeader().getName());
					broadcastToPartyMembers(msg);
					broadcastToPartyMembers(new PartySmallWindowUpdate(getLeader()));
					refreshPartyView();
				}
			}
			else 
			{
				removePartyMember(player);
			}
			if (getPartyMembers().size() == 1)
			{
				// No more party needed
				_members = null;
			}
		}
	}

	/**
	 * Oust player from party
	 * Overloaded method that takes player's name as parameter
	 * @param name
	 */
	public void oustPartyMember(String name) 
	{
		L2PcInstance player = getPlayerByName(name);
		
		if (player != null) 
		{
			if (isLeader(player)) 
			{
				removePartyMember(player);
				if (getPartyMembers().size() > 1)
				{
					SystemMessage msg = new SystemMessage(SystemMessageId.S1_HAS_BECOME_A_PARTY_LEADER);
					msg.addString(getLeader().getName());
					broadcastToPartyMembers(msg);
					broadcastToPartyMembers(new PartySmallWindowUpdate(getLeader()));
					refreshPartyView();
				}
			}
			else
			{
				removePartyMember(player);
			}
			if (getPartyMembers().size() == 1)
			{
				// No more party needed
				_members = null;
			}
		}
	}

	/**
	 * dissolves entire party
	 *
	 */
    /*  [DEPRECATED]
	private void dissolveParty() 
    {
		SystemMessage msg = new SystemMessage(SystemMessageId.PARTY_DISPERSED);
		for(int i = 0; i < _members.size(); i++) 
        {
			L2PcInstance temp = _members.get(i);
			temp.sendPacket(msg);
			temp.sendPacket(new PartySmallWindowDeleteAll());
			temp.setParty(null);
		}
	}
    */

	
	/**
	 * distribute item(s) to party members
	 * @param player
	 * @param item
	 */
	public void distributeItem(L2PcInstance player, L2ItemInstance item)
	{
		if (item.getItemId() == 57)
		{
			distributeAdena(player, item.getCount(), player);
			ItemTable.getInstance().destroyItem("Party", item, player, null);
			return;
		}

		L2PcInstance target = getActualLooter(player, item.getItemId(), false, player);
		target.addItem("Party", item, player, true);
	    
	    // Send messages to other party members about reward
	    if (item.getCount() > 1) 
	    {
	    	SystemMessage msg = new SystemMessage(SystemMessageId.S1_PICKED_UP_S2_S3);
		    msg.addString(target.getName());
		    msg.addItemName(item);
	    	msg.addNumber(item.getCount());
		    broadcastToPartyMembers(target, msg);
	    }
	    else
	    {
	    	SystemMessage msg = new SystemMessage(SystemMessageId.S1_PICKED_UP_S2);
		    msg.addString(target.getName());
		    msg.addItemName(item);
		    broadcastToPartyMembers(target, msg);
	    }
	}
	
	/**
	 * distribute item(s) to party members
	 * @param player
	 * @param item
	 */
	public void distributeItem(L2PcInstance player, L2Attackable.RewardItem item, boolean spoil, L2Attackable target)
	{
		if (item == null || player == null)
			return;

		if (item.getItemId() == 57)
		{
			distributeAdena(player, item.getCount(), target);
			return;
		}

		L2PcInstance looter = getActualLooter(player, item.getItemId(), spoil, target);
		
		looter.addItem(spoil?"Sweep":"Party", item.getItemId(), item.getCount(), player, true);
		
	    // Send messages to other aprty members about reward
	    if (item.getCount() > 1) 
	    {
	    	SystemMessage msg = spoil ?  new SystemMessage(SystemMessageId.S1_SWEEPED_UP_S2_S3) 
	    	                          : new SystemMessage(SystemMessageId.S1_PICKED_UP_S2_S3);
		    msg.addString(looter.getName());
		    msg.addItemName(item.getItemId());
	    	msg.addNumber(item.getCount());
            broadcastToPartyMembers(looter, msg);
	    }
	    else
	    {
	    	SystemMessage msg = spoil ?  new SystemMessage(SystemMessageId.S1_SWEEPED_UP_S2) 
	    	                          : new SystemMessage(SystemMessageId.S1_PICKED_UP_S2);
		    msg.addString(looter.getName());
		    msg.addItemName(item.getItemId());
            broadcastToPartyMembers(looter, msg);
	    }
	}
	
	/**
	 * distribute adena to party members
	 * @param adena
	 */
    public void distributeAdena(L2PcInstance player, int adena, L2Character target) 
    {
        // Get all the party members
        FastList<L2PcInstance> membersList = getPartyMembers();
        
        // Check the number of party members that must be rewarded
        // (The party member must be in range to receive its reward)
        FastList<L2PcInstance> ToReward = new FastList<L2PcInstance>();
        for (L2PcInstance member : membersList)
        {
            if (!Util.checkIfInRange(Config.ALT_PARTY_RANGE2, target, member, true)) continue;
            ToReward.add(member);
        }
        
        // Avoid null exceptions, if any
        if (ToReward.isEmpty()) return;
        
        // Now we can actually distribute the adena reward
        // (Total adena splitted by the number of party members that are in range and must be rewarded)
        int count = adena / ToReward.size();
        for (L2PcInstance member : ToReward)
            member.addAdena("Party", count, player, true);
    }
    
	/**
	 * Distribute Experience and SP rewards to L2PcInstance Party members in the known area of the last attacker.<BR><BR>
	 * 
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get the L2PcInstance owner of the L2SummonInstance (if necessary) </li>
	 * <li>Calculate the Experience and SP reward distribution rate </li>
	 * <li>Add Experience and SP to the L2PcInstance </li><BR><BR>
	 * 
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T GIVE rewards to L2PetInstance</B></FONT><BR><BR>
	 * Exception are L2PetInstances that leech from the owner's XP; they get the exp indirectly, via the owner's exp gain<BR>
	 * 
	 * @param xpReward The Experience reward to distribute
	 * @param spReward The SP reward to distribute
	 * @param rewardedMembers The list of L2PcInstance to reward
	 * 
	 */
    public void distributeXpAndSp(long xpReward, int spReward, List<L2PlayableInstance> rewardedMembers, int topLvl, L2NpcInstance target, int partyDmg)
    {
        L2SummonInstance summon = null;
        List<L2PlayableInstance> validMembers = getValidMembers(rewardedMembers, topLvl);
        
        float penalty;
        double sqLevel;
        double preCalculationExp;
        double preCalculationSp;

		xpReward *= getExpBonus(validMembers.size());
		spReward *= getSpBonus(validMembers.size());

        double sqLevelSum = 0;
        for (L2PlayableInstance character : validMembers)
            sqLevelSum += (character.getLevel() * character.getLevel());
        
        // Go through the L2PcInstances and L2PetInstances (not L2SummonInstances) that must be rewarded
        synchronized(rewardedMembers)
        {
    		for (L2Character member : rewardedMembers)
    		{
    			if(member.isDead()) continue;

				penalty = 0;
					
				// The L2SummonInstance penalty
				if (member.getPet() instanceof L2SummonInstance)
    			{
					summon     = (L2SummonInstance)member.getPet();
					penalty    = summon.getExpPenalty();
    			}
				// Pets that leech xp from the owner (like babypets) do not get rewarded directly
				if (member instanceof L2PetInstance)
				{
					if (((L2PetInstance)member).getPetData().getOwnerExpTaken() > 0)
						continue;

					penalty = (float)0.85;
				}
				
				// Calculate and add the EXP and SP reward to the member
				if (validMembers.contains(member))
				{
					sqLevel = member.getLevel() * member.getLevel();
					preCalculationExp = (sqLevel / sqLevelSum) * (1 - penalty);
					preCalculationSp = (sqLevel / sqLevelSum);
					
					// Add the XP/SP points to the requested party member
					if (!member.isDead())
					{
						long addexp = Math.round(member.calcStat(Stats.EXPSP_RATE, xpReward * preCalculationExp, null, null));
						int addsp = (int)member.calcStat(Stats.EXPSP_RATE, spReward * preCalculationSp, null, null);
						if (target != null && member instanceof L2PcInstance)
						{
							int soulMasteryLevel = ((L2PcInstance)member).getSkillLevel(L2Skill.SKILL_SOUL_MASTERY);
							// Soul Mastery skill
							if (soulMasteryLevel > 0)
							{
								L2Skill skill = SkillTable.getInstance().getInfo(L2Skill.SKILL_SOUL_MASTERY, soulMasteryLevel);
								if (skill.getExpNeeded() <= addexp)
									((L2PcInstance)member).absorbSoulFromNpc(skill,target);
							}
						}
						
						if (Config.ENABLE_VITALITY)
						{
							if (member instanceof L2PcInstance)
							{
								((L2PcInstance) member).addVitExpAndSp(addexp, addsp, target);
								((L2PcInstance) member).calculateVitalityPointsAddRed(target, partyDmg, rewardedMembers.size(), Config.RATE_PARTY_XP);
							}
							else
								member.addExpAndSp(addexp,addsp);
						}
						else
							member.addExpAndSp(addexp,addsp);
					}
				}
				else
				{
					member.addExpAndSp(0, 0);
    			}
    		}
        }
	}
	
    /**
     * Calculates and gives final XP and SP rewards to the party member.<BR>
     * This method takes in consideration number of members, members' levels, rewarder's level and bonus modifier for the actual party.<BR><BR>
     * 
     * @param member is the L2Character to be rewarded
     * @param xpReward is the total amount of XP to be "splited" and given to the member
     * @param spReward is the total amount of SP to be "splited" and given to the member
     * @param penalty is the penalty that must be applied to the XP rewards of the requested member
     */
    
	/**
	 * refresh party level
	 *
	 */
	public void recalculatePartyLevel() 
    {
		int newLevel = 0;
		for (L2PcInstance member : getPartyMembers())
		{
			if (member != null && member.getLevel() > newLevel)
				newLevel = member.getLevel();
		}
		_partyLvl = newLevel;
	}
	
	private List<L2PlayableInstance> getValidMembers(List<L2PlayableInstance> members, int topLvl)
	{
		List<L2PlayableInstance> validMembers = new FastList<L2PlayableInstance>();
		
//      Fixed LevelDiff cutoff point
        if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("level"))
        {
        	for (L2PlayableInstance member : members)
			{
				if (topLvl - member.getLevel() <= Config.PARTY_XP_CUTOFF_LEVEL)
					validMembers.add(member);
			}
        }
//      Fixed MinPercentage cutoff point
        else if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("percentage")) 
        {
            int sqLevelSum = 0;
            for (L2PlayableInstance member : members)
			{
				sqLevelSum += (member.getLevel() * member.getLevel());
			}
			
            for (L2PlayableInstance member : members)
			{
				int sqLevel = member.getLevel() * member.getLevel();
				if (sqLevel * 100 >= sqLevelSum * Config.PARTY_XP_CUTOFF_PERCENT)
					validMembers.add(member);
			}
        }
//      Automatic cutoff method
        else if (Config.PARTY_XP_CUTOFF_METHOD.equalsIgnoreCase("auto")) 
        {
            int sqLevelSum = 0;
            for (L2PlayableInstance member : members)
			{
				sqLevelSum += (member.getLevel() * member.getLevel());
			}
			
			int i = members.size() - 1;
			if (i < 1 ) return members;
            if (i >= BONUS_EXP_SP.length) i = BONUS_EXP_SP.length -1;

            for (L2PlayableInstance member : members)
			{
				int sqLevel = member.getLevel() * member.getLevel();
				if (sqLevel >= sqLevelSum * (1-1/(1 +BONUS_EXP_SP[i] -BONUS_EXP_SP[i-1])))
					validMembers.add(member);
			}
		}
		return validMembers;
	}
	
	private double getBaseExpSpBonus(int membersCount)
	{
		int i = membersCount -1;
        if (i < 1 ) return 1;
        if (i >= BONUS_EXP_SP.length) i = BONUS_EXP_SP.length -1;

        return BONUS_EXP_SP[i];
    }
    
	private double getExpBonus(int membersCount) 
	{
		if(membersCount < 2)
		{
			//not is a valid party		    
			return getBaseExpSpBonus(membersCount); 
		}

		return getBaseExpSpBonus(membersCount) * Config.RATE_PARTY_XP;
	}
	
	private double getSpBonus(int membersCount) 
	{ 
		if(membersCount < 2)
		{	
			//not is a valid party
			return getBaseExpSpBonus(membersCount);
		}			    	 

		return getBaseExpSpBonus(membersCount) * Config.RATE_PARTY_SP;
	}
	
	public int getLevel() { return _partyLvl; }

	public int getLootDistribution() { return _itemDistribution; }

	public boolean isInCommandChannel()
	{
		return _commandChannel != null;
	}

	public L2CommandChannel getCommandChannel()
	{
		return _commandChannel;
	}

	public void setCommandChannel(L2CommandChannel channel)
	{
		_commandChannel = channel;
	}

	public boolean isInDimensionalRift() { return _dr != null; }

	public void setDimensionalRift(DimensionalRift dr) { _dr = dr; }

	public DimensionalRift getDimensionalRift() { return _dr; }

	public synchronized L2PcInstance getLeader()
	{
		return _members == null ? null : _members.getFirst();
	}
}
