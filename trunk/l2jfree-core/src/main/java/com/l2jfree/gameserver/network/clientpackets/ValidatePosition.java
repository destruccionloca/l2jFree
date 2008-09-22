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
import com.l2jfree.gameserver.TaskPriority;
import com.l2jfree.gameserver.geoeditorcon.GeoEditorListener;
import com.l2jfree.gameserver.instancemanager.SiegeManager;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.entity.Siege;
import com.l2jfree.gameserver.model.mapregion.TeleportWhereType;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.PartyMemberPosition;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.network.serverpackets.ValidateLocation;
import com.l2jfree.gameserver.network.serverpackets.ValidateLocationInVehicle;
import com.l2jfree.tools.random.Rnd;

/**
 * This class ...
 *
 * @version $Revision: 1.13.4.7 $ $Date: 2005/03/27 15:29:30 $
 */
public class ValidatePosition extends L2GameClientPacket
{
    private final static Log _log = LogFactory.getLog(ValidatePosition.class.getName());
    private static final String _C__48_VALIDATEPOSITION = "[C] 48 ValidatePosition";

    /** urgent messages, execute immediately */
    public TaskPriority getPriority() { return TaskPriority.PR_HIGH; }

    private int _x;
    private int _y;
    private int _z;
    private int _heading;
    @SuppressWarnings("unused")
    private int _data;


    @Override
    protected void readImpl()
    {
        try
        {
            _x  = readD();
            _y  = readD();
            _z  = readD();
            _heading  = readD();
            _data  = readD();
        }
        catch(Throwable t)
        {
            int location[][] = {
                { 46934,51467,-2977 },        { 9745,15606,-4574 },        { -80826,149775,-3043 },
                { 15670,142983,-2705 },        { -44836,-112524,-235 },    { 115113,-178212,-901 },
                { -84318,244579,-3730 },    { 46934,51467,-2977 },        { 9745,15606,-4574 },
                { -12672,122776,-3116 }},
                i = Rnd.get(10);
            _x = location[i][0];
            _y = location[i][1];
            _z = location[i][2];
            _heading = 0;
            getClient().getActiveChar().teleToLocation(_x, _y, _z);
        }
    }

    @Override
    protected void runImpl()
    {
        L2PcInstance activeChar = getClient().getActiveChar();
        if (activeChar == null || activeChar.isTeleporting()) return;

        activeChar.setClientX(_x);
        activeChar.setClientY(_y);
        activeChar.setClientZ(_z);
        activeChar.setClientHeading(_heading); // No real need to validate heading.
        int realX = activeChar.getX();
        int realY = activeChar.getY();
        int realZ = activeChar.getZ();
        activeChar.setLastServerPosition(realX, realY, realZ);
        
        if(activeChar.getParty() != null && activeChar.getLastPartyPositionDistance(_x, _y, _z) > 150)
        {
            activeChar.setLastPartyPosition(_x, _y, _z);
            activeChar.getParty().broadcastToPartyMembers(activeChar,new PartyMemberPosition(activeChar));
        }
        
        double dx = _x - realX;
        double dy = _y - realY;
        double dz = Math.abs(_z - realZ);
        double diffSq = (dx*dx + dy*dy);
        
        if (Config.DEVELOPER)
        {
            _log.info("client pos: "+ _x + " "+ _y + " "+ _z +" head "+ _heading);
            _log.info("server pos: "+ realX + " "+realY+ " "+realZ +" head "+activeChar.getHeading());
        }
        if (Config.ACCEPT_GEOEDITOR_CONN)
            if (GeoEditorListener.getInstance().getThread() != null  
                    && GeoEditorListener.getInstance().getThread().isWorking()  
                    && GeoEditorListener.getInstance().getThread().isSend(activeChar))
                GeoEditorListener.getInstance().getThread().sendGmPosition(_x,_y,(short)_z);

        if (activeChar.isFlying())
        {
            activeChar.getPosition().setXYZ(_x, _y, _z);
        }
        else if (diffSq < 250000) // if too large, messes observation
        {
            if (Config.COORD_SYNCHRONIZE == -1) // Only Z coordinate synched to server, mainly used when no geodata
            {
                activeChar.getPosition().setXYZ(realX, realY, _z);
                return;
            }
            if (Config.COORD_SYNCHRONIZE == 1 && (!activeChar.isMoving() // Trusting client coordinates (should not be used with geodata)
                || !activeChar.validateMovementHeading(_heading)))       // Heading changed on client = possible obstacle
            {
                // character is not moving, take coordinates from client
                if (diffSq < 2500) // 50*50 - attack won't work fluently if even small differences are corrected
                    activeChar.getPosition().setXYZ(realX, realY, _z);
                else
                    activeChar.getPosition().setXYZ(_x, _y, _z);
                activeChar.setHeading(_heading);
                return;
            }
            if (Config.GEODATA && (diffSq > 10000 || dz > 500)) // Sync 2 (or other), 
                                                 // intended for geodata. Only sends a validation packet to client 
            {                                     // when too far from server calculated true coordinate.
                // a new geodata check seems unnecessary
                //short geoHeight = GeoData.getInstance().getSpawnHeight(realX, realY, realZ-30, realZ+30, activeChar.getObjectId()); 
                //if (realZ != geoHeight) System.out.println("realZ:"+realZ+" spawnheight:"+geoHeight);
                //activeChar.getPosition().setXYZ(realX, realY, geoHeight);

                //System.out.println("validating pos diffsq:"+diffSq+" dz:"+dz);
                if (Config.DEVELOPER)
                     _log.info(activeChar.getName() + ": Synchronizing position Server --> Client");
                if (activeChar.isInBoat())
                {
                    sendPacket(new ValidateLocationInVehicle(activeChar));
                }
                else
                {
                    activeChar.sendPacket(new ValidateLocation(activeChar));
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see com.l2jfree.gameserver.clientpackets.ClientBasePacket#getType()
     */
    @Override
    public String getType()
    {
        return _C__48_VALIDATEPOSITION;
    }
}