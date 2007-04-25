/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.clientpackets;

import java.nio.ByteBuffer;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.TaskPriority;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.ObjectKnownList.KnownListAsynchronousUpdateTask;
import net.sf.l2j.gameserver.serverpackets.PartyMemberPosition;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.serverpackets.ValidateLocationInVehicle;
import net.sf.l2j.tools.geometry.Point3D;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class ...
 * 
 * @version $Revision: 1.13.4.7 $ $Date: 2005/03/27 15:29:30 $
 */
public class ValidatePosition extends ClientBasePacket
{
    private final static Log _log = LogFactory.getLog(ValidatePosition.class.getName());
    private static final String _C__48_VALIDATEPOSITION = "[C] 48 ValidatePosition";
    
    /** urgent messages, execute immediatly */
    public TaskPriority getPriority() { return TaskPriority.PR_HIGH; }
    
    private final int _x;
    private final int _y;
    private final int _z;
    private final int _heading;
    @SuppressWarnings("unused")
    private final int _data;
    /**
     * packet type id 0x48
     * format:      cddddd
     * @param decrypt
     */
    public ValidatePosition(ByteBuffer buf, ClientThread client)
    {
        super(buf, client);
        _x  = readD();
        _y  = readD();
        _z  = readD();
        _heading  = readD();
        _data  = readD();
    }
    
    void runImpl()
    {
        L2PcInstance activeChar = getClient().getActiveChar();
        if (activeChar == null || activeChar.isTeleporting()) return;
        
        if (Config.COORD_SYNCHRONIZE > 0)
        {
            activeChar.setClientX(_x);
            activeChar.setClientY(_y);
            activeChar.setClientZ(_z);
            activeChar.setClientHeading(_heading);
            int realX = activeChar.getX();
            int realY = activeChar.getY();
            int realZ = activeChar.getZ();
            
            double dx = _x - realX;
            double dy = _y - realY;
            double diffSq = (dx*dx + dy*dy);

            if (_log.isDebugEnabled() ) 
                {
//                int dxs = (_x - activeChar._lastClientPosition.x); 
//                int dys = (_y - activeChar._lastClientPosition.y);
//                int dist = (int)Math.sqrt(dxs*dxs + dys*dys);
//                int heading = dist > 0 ? (int)(Math.atan2(-dys/dist, -dxs/dist) * 10430.378350470452724949566316381) + 32768 : 0;
                _log.debug("Client X:" + _x + ", Y:" + _y + ", Z:" + _z + ", H:" + _heading/* + "(" + heading + ")"*/ + ", Dist:" + activeChar.getLastClientDistance(_x, _y, _z));
                _log.debug("Server X:" + realX + ", Y:" + realY + ", Z:" + realZ + ", H:" + activeChar.getHeading() + ", Dist:" + activeChar.getLastServerDistance(realX, realY, realZ));
                }

            if (diffSq > 0)
            {
                if ((Config.COORD_SYNCHRONIZE & 1) == 1
                    && (!activeChar.isMoving() // character is not moving, take coordinates from client
                    || !activeChar.validateMovementHeading(_heading))) // Heading changed on client = possible obstacle
                {
                    if (_log.isDebugEnabled()) _log.debug(activeChar.getName() + ": Synchronizing position Client --> Server" + (activeChar.isMoving()?" (collision)":" (stay sync)"));
                    activeChar.setXYZ(_x, _y, _z);
                    activeChar.setHeading(_heading);
                }
                else if ((Config.COORD_SYNCHRONIZE & 2) == 2 
                        && diffSq > 10000) // more than can be considered to be result of latency
                {
                    if (_log.isDebugEnabled())  _log.debug(activeChar.getName() + ": Synchronizing position Server --> Client");
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
            activeChar.setLastClientPosition(_x, _y, _z);
            activeChar.setLastServerPosition(activeChar.getX(), activeChar.getY(), activeChar.getZ());
        }
        else if (Config.COORD_SYNCHRONIZE == -1)
        {
            activeChar.setClientX(_x);
            activeChar.setClientY(_y);
            activeChar.setClientZ(_z);
            activeChar.setClientHeading(_heading);
            int realX = activeChar.getX();
            int realY = activeChar.getY();
            int realZ = activeChar.getZ();
            
            if (Point3D.distanceSquared(activeChar.getPosition().getWorldPosition(), new Point3D(_x, _y, _z)) < 500 * 500)
                activeChar.setXYZ(activeChar.getX(),activeChar.getY(),_z);
            int realHeading = activeChar.getHeading();
        
            //activeChar.setHeading(_heading);
            
            //TODO: do we need to validate?
            /*double dx = (_x - realX); 
             double dy = (_y - realY); 
             double dist = Math.sqrt(dx*dx + dy*dy);
             if ((dist < 500)&&(dist > 2)) //check it wasnt teleportation, and char isn't there yet
             activeChar.sendPacket(new CharMoveToLocation(activeChar));*/
            
            if (_log.isDebugEnabled()) {
                _log.debug("client pos: "+ _x + " "+ _y + " "+ _z +" head "+ _heading);
                _log.debug("server pos: "+ realX + " "+realY+ " "+realZ +" head "+realHeading);
            }
            
            if (Config.DEVELOPER)
            {
                double dx = _x - realX;
                double dy = _y - realY;
                double diff2 = (dx*dx + dy*dy);
                if (diff2 > 1000000) {
                    if (_log.isDebugEnabled()) _log.debug("client/server dist diff "+ (int)Math.sqrt(diff2));
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
            //trigger a KnownList update
            ThreadPoolManager.getInstance().executeTask( new KnownListAsynchronousUpdateTask(activeChar));
//          // check for objects that are now out of range
//          activeChar.updateKnownCounter += 1;
//          if (activeChar.updateKnownCounter >3)
//          {
//          int delete = 0;
//          Iterator<L2Object> known = activeChar.iterateKnownObjects();
//          ArrayList<L2Object> toBeDeleted = new ArrayList<L2Object>();
//          
//          while (known.hasNext())
//          {
//          L2Object obj = known.next();
//          if (distance(activeChar, obj) > 4000*4000)
//          {
//          toBeDeleted.add(obj);
//          delete++;
//          }
//          }
//          
//          if (delete >0)
//          {
//          for (int i = 0; i < toBeDeleted.size(); i++)
//          {
//          L2Object obj = toBeDeleted.get(i);
//          activeChar.removeKnownObject(obj);
//          obj.removeKnownObject(activeChar);
//          
//          }
//          if (_log.isDebugEnabled()) _log.debug("deleted " +delete+" objects");
//          }
//          
//          
//          // check for new objects that are now in range
//          int newObjects = 0;
//          L2Object[] visible = L2World.getInstance().getVisibleObjects(activeChar, 3000);
//          for (int i = 0; i < visible.length; i++)
//          {
//          if (! activeChar.knownsObject(visible[i]))
//          {
//          activeChar.addKnownObject(visible[i]);
//          visible[i].addKnownObject(activeChar);
//          newObjects++;
//          }
//          }
//          
//          if (newObjects >0)
//          {
//          if (_log.isDebugEnabled()) _log.debug("added " + newObjects + " new objects");
//          }
//          activeChar.updateKnownCounter = 0;  
//          }
        }
		if(activeChar.getParty() != null)
			activeChar.getParty().broadcastToPartyMembers(activeChar,new PartyMemberPosition(activeChar));
		
		if (Config.ALLOW_WATER)
			activeChar.checkWaterState();

		// [L2J_JP ADD START SANDMAN]
		// if this is a castle that is currently being sieged, and the rider is NOT a castle owner
		// he cannot flying.
		// castle owner is the leader of the clan that owns the castle where the pc is
		if ((!Config.ALT_FLYING_WYVERN_IN_SIEGE) && (activeChar.getMountType() == 2))
		{
		    if (SiegeManager.getInstance().checkIfInZone(activeChar)
		            && !(activeChar.getClan() != null
		            && CastleManager.getInstance().getCastle(activeChar) == CastleManager.getInstance().getCastleByOwner(activeChar.getClan())
		            && activeChar == activeChar.getClan().getLeader().getPlayerInstance()))
		    {
		        SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
		        sm.addString("You entered into a no-fly zone.");
		        activeChar.sendPacket(sm);
		
		        activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
		    }
		}
		// [L2J_JP ADD END]
    }
    
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
     */
    public String getType()
    {
        return _C__48_VALIDATEPOSITION;
    }
    
    public boolean Equal(ValidatePosition pos)
    {
        return _x == pos._x && _y == pos._y && _z == pos._z && _heading == pos._heading;
    }
}
