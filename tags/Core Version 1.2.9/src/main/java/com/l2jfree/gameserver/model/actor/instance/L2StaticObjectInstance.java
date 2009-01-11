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
package com.l2jfree.gameserver.model.actor.instance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.gameserver.ai.CtrlIntention;
import com.l2jfree.gameserver.cache.HtmCache;
import com.l2jfree.gameserver.instancemanager.CastleManager;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.knownlist.NullKnownList;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.ChairSit;
import com.l2jfree.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jfree.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfree.gameserver.network.serverpackets.ShowTownMap;
/**
 * @author godson
 */
public class L2StaticObjectInstance extends L2Object
{
    private final static Log _log = LogFactory.getLog(L2StaticObjectInstance.class.getName());
    
    /** The interaction distance of the L2StaticObjectInstance */
    public static final int INTERACTION_DISTANCE = 150;

    private int _staticObjectId;
    private int _type = -1;         // 0 - Map signs, 1 - Throne , 2 - Arena signs
    private int _x;
    private int _y;
    private String _texture;

    private L2PcInstance actualPersonToSitOn = null;
    
    /**
     * @return Returns the StaticObjectId.
     */
    public int getStaticObjectId()
    {
        return _staticObjectId;
    }
    /**
     * @param doorId The doorId to set.
     */
    public void setStaticObjectId(int StaticObjectId)
    {
        _staticObjectId = StaticObjectId;
    }
    /**
     */
    public L2StaticObjectInstance(int objectId)
    {
        super(objectId);
        getKnownList();
    }
    
    @Override
	public NullKnownList getKnownList()
    {
        if (_knownList == null)
            _knownList = new NullKnownList(this);
        
        return (NullKnownList)_knownList;
    }
    
    
    public boolean isBusy()
    {
    	return (actualPersonToSitOn != null);
    }
    public void setBusyStatus(L2PcInstance actualPersonToSitOn)
    {
    	this.actualPersonToSitOn = actualPersonToSitOn;
    }

    public int getType()
    {
        return _type;
    }
    
    public void setType(int type)
    {
        _type = type;
    }

    public void setMap(String texture, int x, int y)
    {
        _texture = "town_map."+texture;
        _x = x;
        _y = y;
    }

    private int getMapX()
    {
    return _x;
    }

    private int getMapY()
    {
    return _y;
    }
    
    /**
     * This is called when a player interacts with this NPC
     * @param player
     */
    @Override
    public void onAction(L2PcInstance player)
    {
        if(_type < 0) _log.info("L2StaticObjectInstance: StaticObject with invalid type! StaticObjectId: "+getStaticObjectId());
        // Check if the L2PcInstance already target the L2NpcInstance
        if (this != player.getTarget())
        {
            // Set the target of the L2PcInstance player
            player.setTarget(this);
            player.sendPacket(new MyTargetSelected(getObjectId(), 0));
        }
        else
        {
            MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
            player.sendPacket(my);

            // Calculate the distance between the L2PcInstance and the L2NpcInstance
            if (!player.isInsideRadius(this, INTERACTION_DISTANCE, false, false))
            {
                // Notify the L2PcInstance AI with AI_INTENTION_INTERACT
                player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
                
                // Send a Server->Client packet ActionFailed (target is out of interaction range) to the L2PcInstance player
                player.sendPacket(ActionFailed.STATIC_PACKET);
            }
            else
            {
                if(_type == 2)
                {
                    String filename = "data/html/signboard.htm";
                    String content = HtmCache.getInstance().getHtm(filename);
                    NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());

                    if (content == null) html.setHtml("<html><body>Signboard is missing:<br>"+filename+"</body></html>");
                    else html.setHtml(content);

                    player.sendPacket(html);
                    player.sendPacket(ActionFailed.STATIC_PACKET);
                }
                else if(_type == 0)
                {
                    player.sendPacket(new ShowTownMap(_texture, getMapX(), getMapY()));
                    // Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
                    player.sendPacket(ActionFailed.STATIC_PACKET);
                }
            }
        }
    }
    
    /**
     * Tries to use the StaticObjectInstance as a throne with the given
     * player to assign to
     * 
     * @param player The actual person who wants to sit on the throne
     * @return Sitting was possible or not
     */
    public boolean useThrone(L2PcInstance player) {
    	// This check is added if char that sits on the chair had
    	// a critical game error and throne wasn't release in a
    	// clean way to avoid that "isBusy" will be true all the
    	// way until server restarts.
    	if (actualPersonToSitOn != null && 
    			L2World.getInstance().findPlayer(							// If the actual user isn't
    			actualPersonToSitOn.getObjectId()) == null)					// found in the world anymore
    		setBusyStatus(null);											// release me
    	
    	if (player.getTarget() != this ||									// Player's target isn't me or
    			getType() != 1 ||											// I'm no throne or
    			isBusy())													// I'm already in use
    		return false;
    	
    	if (player.getClan() == null ||										// Player has no clan or
    			CastleManager.getInstance().getCastle(this) == null ||		// I got no castle assigned or
    			CastleManager.getInstance().getCastleById(
    					player.getClan().getHasCastle()) == null)			// Player's clan has no castle
    		return false;
    	
    	if (!player.isInsideRadius(this, 									// Player is not in radius
    			INTERACTION_DISTANCE, false, false))						// to interact with me
    		return false;
    	
    	if (CastleManager.getInstance().getCastle(this) != 					// Player's clan castle isn't
					CastleManager.getInstance().getCastleById(				// the same as mine
							player.getClan().getHasCastle()))
    		return false;
    	
    	if (Config.ALT_ONLY_CLANLEADER_CAN_SIT_ON_THRONE &&					// Only clan leader can use throne is set and
    			player.getObjectId() != player.getClan().getLeaderId())		// Player is not the clan leader
    		return false;
    	
    	setBusyStatus(player);
		player.setObjectSittingOn(this);
		
		ChairSit cs = new ChairSit(player, getStaticObjectId());
		player.sitDown();
		player.broadcastPacket(cs);

		return true;
    }
    
    /* (non-Javadoc)
     * @see com.l2jfree.gameserver.model.L2Object#isAttackable()
     */
    @Override
    @SuppressWarnings("unused")
    public boolean isAutoAttackable(L2Character attacker)
    {
        return false;
    }
}