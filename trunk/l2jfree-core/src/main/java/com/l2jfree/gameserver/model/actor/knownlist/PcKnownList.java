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
package com.l2jfree.gameserver.model.actor.knownlist;


import java.util.Map;

import com.l2jfree.Config;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Decoy;
import com.l2jfree.gameserver.model.actor.L2Npc;
import com.l2jfree.gameserver.model.actor.L2Summon;
import com.l2jfree.gameserver.model.actor.L2Trap;
import com.l2jfree.gameserver.model.actor.instance.L2BoatInstance;
import com.l2jfree.gameserver.model.actor.instance.L2DecoyInstance;
import com.l2jfree.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfree.gameserver.model.actor.instance.L2StaticObjectInstance;
import com.l2jfree.gameserver.model.actor.instance.L2TrapInstance;
import com.l2jfree.gameserver.network.serverpackets.AbstractNpcInfo;
import com.l2jfree.gameserver.network.serverpackets.CharInfo;
import com.l2jfree.gameserver.network.serverpackets.DeleteObject;
import com.l2jfree.gameserver.network.serverpackets.DropItem;
import com.l2jfree.gameserver.network.serverpackets.ExBrExtraUserInfo;
import com.l2jfree.gameserver.network.serverpackets.ExPrivateStoreSetWholeMsg;
import com.l2jfree.gameserver.network.serverpackets.GetOnVehicle;
import com.l2jfree.gameserver.network.serverpackets.PetInfo;
import com.l2jfree.gameserver.network.serverpackets.PetItemList;
import com.l2jfree.gameserver.network.serverpackets.PrivateStoreMsgBuy;
import com.l2jfree.gameserver.network.serverpackets.PrivateStoreMsgSell;
import com.l2jfree.gameserver.network.serverpackets.RecipeShopMsg;
import com.l2jfree.gameserver.network.serverpackets.Ride;
import com.l2jfree.gameserver.network.serverpackets.ServerObjectInfo;
import com.l2jfree.gameserver.network.serverpackets.SpawnItem;
import com.l2jfree.gameserver.network.serverpackets.StaticObject;
import com.l2jfree.gameserver.network.serverpackets.VehicleInfo;
import com.l2jfree.util.SingletonMap;

public class PcKnownList extends PlayableKnownList
{
    // =========================================================
    // Data Field
	private Map<Integer, Integer> _knownRelations;
    
    // =========================================================
    // Constructor
    public PcKnownList(L2PcInstance activeChar)
    {
        super(activeChar);
    }

    // =========================================================
    // Method - Public
    
	public final Map<Integer, Integer> getKnownRelations()
	{
		if (_knownRelations == null)
			_knownRelations = new SingletonMap<Integer, Integer>().setShared();
		return _knownRelations;
	}
    
    /**
     * Add a visible L2Object to L2PcInstance _knownObjects and _knownPlayer (if necessary) and send Server-Client Packets needed to inform the L2PcInstance of its state and actions in progress.<BR><BR>
     *
     * <B><U> object is a L2ItemInstance </U> :</B><BR><BR>
     * <li> Send Server-Client Packet DropItem/SpawnItem to the L2PcInstance </li><BR><BR>
     *
     * <B><U> object is a L2DoorInstance </U> :</B><BR><BR>
     * <li> Send Server-Client Packets DoorInfo and DoorStatusUpdate to the L2PcInstance </li>
     * <li> Send Server->Client packet MoveToPawn/MoveToLocation and AutoAttackStart to the L2PcInstance </li><BR><BR>
     *
     * <B><U> object is a L2Npc </U> :</B><BR><BR>
     * <li> Send Server-Client Packet NpcInfo to the L2PcInstance </li>
     * <li> Send Server->Client packet MoveToPawn/MoveToLocation and AutoAttackStart to the L2PcInstance </li><BR><BR>
     *
     * <B><U> object is a L2Summon </U> :</B><BR><BR>
     * <li> Send Server-Client Packet NpcInfo/PetItemList (if the L2PcInstance is the owner) to the L2PcInstance </li>
     * <li> Send Server->Client packet MoveToPawn/MoveToLocation and AutoAttackStart to the L2PcInstance </li><BR><BR>
     *
     * <B><U> object is a L2PcInstance </U> :</B><BR><BR>
     * <li> Send Server-Client Packet CharInfo to the L2PcInstance </li>
     * <li> If the object has a private store, Send Server-Client Packet PrivateStoreMsgSell to the L2PcInstance </li>
     * <li> Send Server->Client packet MoveToPawn/MoveToLocation and AutoAttackStart to the L2PcInstance </li><BR><BR>
     *
     * @param object The L2Object to add to _knownObjects and _knownPlayer
     */
    @Override
    public boolean addKnownObject(L2Object object, L2Character dropper)
    {
		if (object instanceof L2PcInstance && ((L2PcInstance)object).inObserverMode())
			return false;
		
        if (!super.addKnownObject(object, dropper)) return false;

		if (object instanceof L2PcInstance)
			getKnownRelations().put(object.getObjectId(), -1);
        
        if (object.getPoly().isMorphed() && object.getPoly().getPolyType().equals("item"))
        {
            //if (object.getPolytype().equals("item"))
                getActiveChar().sendPacket(new SpawnItem(object));
            //else if (object.getPolytype().equals("npc"))
            //    sendPacket(new NpcInfoPoly(object, this));

        }
        else
        {
            if (object instanceof L2ItemInstance)
            {
                if (dropper != null)
                    getActiveChar().sendPacket(new DropItem((L2ItemInstance) object, dropper.getObjectId()));
                else
                    getActiveChar().sendPacket(new SpawnItem(object));
            }
            else if (object instanceof L2DoorInstance)
            {
                getActiveChar().sendPacket(new StaticObject((L2DoorInstance)object));
            }
            else if (object instanceof L2BoatInstance)
            {
                if(!getActiveChar().isInBoat())
                if(object != getActiveChar().getBoat())
                {
                    getActiveChar().sendPacket(new VehicleInfo((L2BoatInstance) object));
                    ((L2BoatInstance) object).sendVehicleDeparture(getActiveChar());
                }
            }
            else if (object instanceof L2StaticObjectInstance)
            {
                getActiveChar().sendPacket(new StaticObject((L2StaticObjectInstance) object));
            }
            else if (object instanceof L2Decoy || object instanceof L2DecoyInstance)
            {
                getActiveChar().sendPacket(new AbstractNpcInfo.DecoyInfo((L2Decoy) object));
            }
            else if (object instanceof L2Trap || object instanceof L2TrapInstance)
            {
                getActiveChar().sendPacket(new AbstractNpcInfo.TrapInfo((L2Trap)object));
            }
            else if (object instanceof L2Npc)
            {
                if (Config.TEST_KNOWNLIST && getActiveChar().isGM()) getActiveChar().sendMessage("Knownlist, added NPC: "+ object.getName());
                if (((L2Npc) object).getRunSpeed() == 0)
                    getActiveChar().sendPacket(new ServerObjectInfo((L2Npc)object));
                else
                    getActiveChar().sendPacket(new AbstractNpcInfo.NpcInfo((L2Npc)object));
            }
            else if (object instanceof L2Summon)
            {
                L2Summon summon = (L2Summon) object;

                // Check if the L2PcInstance is the owner of the Pet
                if (getActiveChar() == summon.getOwner())
                {
                    getActiveChar().sendPacket(new PetInfo(summon, 0));
                    if (summon instanceof L2PetInstance)
                    {
                        getActiveChar().sendPacket(new PetItemList((L2PetInstance) summon));
                    }
                }
                else
                    getActiveChar().sendPacket(new AbstractNpcInfo.SummonInfo(summon, 0));
            }
            else if (object instanceof L2PcInstance)
            {
                L2PcInstance otherPlayer = (L2PcInstance) object;
                if (otherPlayer.isInBoat())
                {
                    otherPlayer.getPosition().setWorldPosition(otherPlayer.getBoat().getPosition());
                    getActiveChar().sendPacket(new CharInfo(otherPlayer));
                    getActiveChar().sendPacket(new ExBrExtraUserInfo(otherPlayer));
                    getActiveChar().sendPacket(new GetOnVehicle(otherPlayer, otherPlayer.getBoat(), otherPlayer.getInBoatPosition().getX(), otherPlayer.getInBoatPosition().getY(), otherPlayer.getInBoatPosition().getZ()));
                }
                else
                {
                    getActiveChar().sendPacket(new CharInfo(otherPlayer));
                    getActiveChar().sendPacket(new ExBrExtraUserInfo(otherPlayer));
                }

                if (otherPlayer.getMountType() == 4)
                {
                	// TODO: Remove when horse mounts fixed
                	getActiveChar().sendPacket(new Ride(otherPlayer, false, 0));
                	getActiveChar().sendPacket(new Ride(otherPlayer, true, otherPlayer.getMountNpcId()));
                }
                if (otherPlayer.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_SELL)
                {
                    getActiveChar().sendPacket(new PrivateStoreMsgSell(otherPlayer));
                }
                else if (otherPlayer.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_PACKAGE_SELL)
                {
                    getActiveChar().sendPacket(new ExPrivateStoreSetWholeMsg(otherPlayer));
                }
                else if (otherPlayer.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_BUY)
                {
                    getActiveChar().sendPacket(new PrivateStoreMsgBuy(otherPlayer));
                }
                else if (otherPlayer.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_MANUFACTURE)
                {
                    getActiveChar().sendPacket(new RecipeShopMsg(otherPlayer));
                }
            }

            if (object instanceof L2Character)
            {
                // Update the state of the L2Character object client side by sending Server->Client packet MoveToPawn/MoveToLocation and AutoAttackStart to the L2PcInstance
                L2Character obj = (L2Character) object;
                if (obj.getAI() != null)
                    obj.getAI().describeStateToPlayer(getActiveChar());
            }
        }

        return true;
    }
    
	@Override
	public final void removeAllKnownObjects()
	{
		super.removeAllKnownObjects();
		
		getKnownRelations().clear();
	}
    
    /**
     * Remove a L2Object from L2PcInstance _knownObjects and _knownPlayer (if necessary) and send Server-Client Packet DeleteObject to the L2PcInstance.<BR><BR>
     *
     * @param object The L2Object to remove from _knownObjects and _knownPlayer
     *
     */
    @Override
    public boolean removeKnownObject(L2Object object)
    {
            if (!super.removeKnownObject(object)) return false;
        // Send Server-Client Packet DeleteObject to the L2PcInstance
        getActiveChar().sendPacket(new DeleteObject(object));
        
		if (object instanceof L2PcInstance)
			getKnownRelations().remove(object.getObjectId());
        
       if (Config.TEST_KNOWNLIST && getActiveChar().isGM() && object instanceof L2Npc) getActiveChar().sendMessage("Knownlist,removed NPC: "+ object.getName());
        return true;
    }
    
    // =========================================================
    // Method - Private

    // =========================================================
    // Property - Public
    @Override
    public final L2PcInstance getActiveChar() { return (L2PcInstance)_activeChar; }

    @Override
    public int getDistanceToForgetObject(L2Object object)
    {
        // when knownlist grows, the distance to forget should be at least
        // the same as the previous watch range, or it becomes possible that
        // extra charinfo packets are being sent (watch-forget-watch-forget)
        int knownlistSize = getKnownObjects().size();
        if (knownlistSize <= 25)
        	return 4000;
        
        if (knownlistSize <= 35)
        	return 3500;
        
        if (knownlistSize <= 70)
        	return 2910;
        
        return 2310;
    }
 
    @Override
    public int getDistanceToWatchObject(L2Object object)
    {
        int knownlistSize = getKnownObjects().size();
 
        if (knownlistSize <= 25)
        	return 3400; // empty field
        
        if (knownlistSize <= 35)
        	return 2900;
        if (knownlistSize <= 70)
        	return 2300;
        
        return 1700; // Siege, TOI, city
    }
}
