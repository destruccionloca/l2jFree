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
package net.sf.l2j.gameserver.model.actor.instance;

import java.util.Collection;
import java.util.Map;

import javolution.util.FastMap;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.boat.events.BoatCaptain;
import net.sf.l2j.gameserver.boat.events.Boatrun;
import net.sf.l2j.gameserver.boat.model.L2BoatTrajet;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.BoatKnownList;
import net.sf.l2j.gameserver.network.SystemChatChannelId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.VehicleCheckLocation;
import net.sf.l2j.gameserver.network.serverpackets.VehicleDeparture;
import net.sf.l2j.gameserver.network.serverpackets.VehicleInfo;
import net.sf.l2j.gameserver.templates.L2CharTemplate;
import net.sf.l2j.gameserver.templates.L2Weapon;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A boat instance represent the boat and the boat captain. 
 * When Boat instance are instantiated and spawn, the cycle of their trip began.
 * They did only two trajet : one way and the return.
 * Those datas are loaded from csv file for now.
 * 
 * A Boat instance could have several L2PcInstance on board. They are notified by gamepackets
 * when the boat moves. 
 * 
 * When the boat leaves, we check all L2PcInstance in the knownlist, check that they are on boat (isOnBoat())
 * and destroy the item ticket from their inventory. Then, players are teleported.
 * 
 * @author Maktakien
 *
 */
public class L2BoatInstance extends L2Character
{
    public static final int DEPARTURE_IN_10_MINUTES = 10;
    public static final int DEPARTURE_IN_5_MINUTES = 5;
    public static final int DEPARTURE_IN_1_MINUTES = 1;
    public static final int DEPARTURE = 0;

    private final static Log _log = LogFactory.getLog(L2BoatInstance.class.getName());
    
    public static final int TRAJET_WAY_1 = 1;
    public static final int TRAJET_WAY_2 = 2;
    
    /** Name of this boat instance */
    private String _name;
    /** Trajet for this boat */
    private L2BoatTrajet _t1;
    private L2BoatTrajet _t2;
    
    /** Cycle : one way and the return */
	private int _cycle = 0;
    
    /**game packet to notify departure of the boat */
    private VehicleDeparture _vd = null;
    
    /**Map of L2PcInstance on board*/
	private Map<Integer,L2PcInstance> _inboat;
    
    private int _runstate  = 0;
    private int lastx = -1;
    private int lasty= -1;
    private boolean needOnVehicleCheckLocation= false;

    /**
     * Constructor 
     * @param objectId
     * @param template
     * @param name
     */
	public L2BoatInstance(int objectId, L2CharTemplate template,String name)
	{
		super(objectId, template);
		super.setKnownList(new BoatKnownList(this));
		_name = name;
	}
	
	/**
	 * @param x
	 * @param y
	 * @param z
     * @param speed
	 */
	public void moveToLocation(int x, int y, int z,float speed)
	{
       final int curX = getX();
       final int curY = getY();
       final int curZ = getZ();

       // Calculate distance (dx,dy) between current position and destination
       final int dx = (x - curX);
       final int dy = (y - curY);
       double distance = Math.sqrt(dx*dx + dy*dy);

       if (_log.isDebugEnabled()) _log.debug("distance to target:" + distance);

       // Define movement angles needed
       // ^
       // |     X (x,y)
       // |   /
       // |  /distance
       // | /
       // |/ angle
       // X ---------->
       // (curx,cury)

       double cos;
       double sin;
       sin = dy/distance;
       cos = dx/distance;
       // Create and Init a MoveData object
       MoveData m = new MoveData();

       // Caclulate the Nb of ticks between the current position and the destination
       m._ticksToMove = (int)(GameTimeController.TICKS_PER_SECOND * distance / speed);

       // Calculate the xspeed and yspeed in unit/ticks in function of the movement speed
       m._xSpeedTicks = (float)(cos * speed / GameTimeController.TICKS_PER_SECOND);
       m._ySpeedTicks = (float)(sin * speed / GameTimeController.TICKS_PER_SECOND);

       // Calculate and set the heading of the L2Character
       int heading = (int) (Math.atan2(-sin, -cos) * 10430.378350470452724949566316381);
       heading += 32768;
       getPosition().setHeading(heading);

       if (_log.isDebugEnabled()) _log.debug("dist:"+ distance +"speed:" + speed + " ttt:" +m._ticksToMove +
                   " dx:"+(int)m._xSpeedTicks + " dy:"+(int)m._ySpeedTicks + " heading:" + heading);

       m._xDestination = x;
       m._yDestination = y;
       m._zDestination = z; // this is what was requested from client
       m._heading = 0;

       m._moveStartTime = GameTimeController.getGameTicks();
       m._xMoveFrom = curX;
       m._yMoveFrom = curY;
       m._zMoveFrom = curZ;

       // If necessary set Nb ticks needed to a min value to ensure small distancies movements
       if (m._ticksToMove < 1 )
           m._ticksToMove = 1;

       if (_log.isDebugEnabled()) 
    	   _log.debug("time to target:" + m._ticksToMove);

       // Set the L2Character _move object to MoveData object
       _move = m;

       // Add the L2Character to movingObjects of the GameTimeController
       // The GameTimeController manage objects movement
       GameTimeController.getInstance().registerMovingObject(this);
	}
	
	/**
	 * 
	 */
	public void evtArrived()
	{
		if(_runstate == 0)
		{
			//DO nothing :P
		}
		else
		{
            // prepare the next departure
			Boatrun bc = new Boatrun(_runstate,this);
			ThreadPoolManager.getInstance().scheduleGeneral(bc, DEPARTURE_IN_10_MINUTES);
			_runstate = 0;
		}
	}
    
	/**
	 * @param activeChar
	 */
	public void sendVehicleDeparture(L2PcInstance activeChar)
	{		
		if(_vd != null)
		{
			activeChar.sendPacket(_vd);
		}
	}
	public VehicleDeparture getVehicleDeparture()
	{
		return _vd;
	}
    public void setVehicleDeparture(VehicleDeparture vd)
    {
        _vd=vd;
    }
    /**
     * Begin the trip cycle
     * Prepare for departure, send message for the destination
     * and schedule departure in ThreadPoolManager
     *
     */
	private void beginCycle()
	{				
		say(DEPARTURE_IN_10_MINUTES);     
		BoatCaptain bc = new BoatCaptain(1,this); 
		ThreadPoolManager.getInstance().scheduleGeneral(bc, 300000);   
	}
    
	/**
	 * @param x
	 * @param y
	 * @param z
	 */
	public void updatePeopleInTheBoat(int x, int y, int z)
	{
		
		if(_inboat != null)
		{
			boolean check = false;
			if((lastx == -1)||(lasty == -1))
			{
				check = true;
				lastx = x;
				lasty = y;
			}
			else if( (x - lastx) * (x - lastx) + (y - lasty) * (y - lasty) > 2250000) // 1500 * 1500 = 2250000
			{
				check = true;
				lastx = x;
				lasty = y;
			}
			for (int i = 0; i < _inboat.size();i++)
			{
				L2PcInstance player= _inboat.get(i);
                if(player != null && player.isInBoat())
				{
					if(player.getBoat() == this)
					{
						//player.getKnownList().addKnownObject(this);
						player.getPosition().setXYZ(x,y,z);
						player.revalidateZone(false);
					}
				}
				if(check == true)
				{
					if(needOnVehicleCheckLocation == true)
					{
						VehicleCheckLocation vcl = new VehicleCheckLocation(this,x,y,z);
						player.sendPacket(vcl);
					}
				}
			}
		}
		
	}
	/**
	 * @param i
	 */
	public void begin()
	{		
		if(_cycle == TRAJET_WAY_1)
		{
			Collection<L2PcInstance> knownPlayers = getKnownList().getKnownPlayers().values();
			if (knownPlayers != null && !knownPlayers.isEmpty())
			{
				_inboat = new FastMap<Integer,L2PcInstance>();
				int i = 0;
				for (L2PcInstance player : knownPlayers)
				{
                    if(player != null && player.isInBoat())
					{
    					L2ItemInstance it;
    					it = player.getInventory().getItemByItemId(_t1.getIdWTicket1());
    					if((it != null)&&(it.getCount() >= 1))
    					{					
    						player.getInventory().destroyItem("Boat", it.getObjectId(), 1, player, this);
    						InventoryUpdate iu = new InventoryUpdate();
    						iu.addModifiedItem(it);
    						player.sendPacket(iu);	
    						_inboat.put(i,player);
    						i++;
    					}
    					else if (it == null && _t1.getIdWTicket1() == 0)
    					{
    						_inboat.put(i,player);
    						i++;
    					}
    					else
    					{
    						player.teleToLocation(_t1.getNtx1(),_t1.getNtx1(),_t1.getNtz1(), false);
    					}
					}
				}				  
			}
			Boatrun bc = new Boatrun(0,this);
			ThreadPoolManager.getInstance().scheduleGeneral(bc, 0);
		}
		else if(_cycle == TRAJET_WAY_2)
		{
			Collection<L2PcInstance> knownPlayers = getKnownList().getKnownPlayers().values();
			if (knownPlayers != null && !knownPlayers.isEmpty())
			{
				_inboat = new FastMap<Integer,L2PcInstance>();
				int i = 0;
				for (L2PcInstance player : knownPlayers)
				{
                    if(player != null && player.isInBoat())
					{
    					L2ItemInstance it;
    					it = player.getInventory().getItemByItemId(_t2.getIdWTicket1());
    					if((it != null)&&(it.getCount() >= 1))
    					{	
    						
    						player.getInventory().destroyItem("Boat", it.getObjectId(), 1, player, this);
    						InventoryUpdate iu = new InventoryUpdate();
    						iu.addModifiedItem(it);
    						player.sendPacket(iu);
    						_inboat.put(i,player);
    						i++;
    						}
    					else if (it == null && _t2.getIdWTicket1() == 0)
    					{
    						_inboat.put(i,player);
    						i++;
    					}
    					else
    					{
    						player.teleToLocation(_t2.getNtx1(),_t2.getNty1(),_t2.getNtz1(), false);
    					}
					}
				}
				
			}
			Boatrun bc = new Boatrun(0,this);
			ThreadPoolManager.getInstance().scheduleGeneral(bc, 0); 
		}
	}
	
	/**
     * Activate the captain announce
     * 
	 * @param i the type of announce
	 */
	public void say(int i)
	{
		
		Collection<L2PcInstance> knownPlayers = getKnownList().getKnownPlayers().values();        
		CreatureSay sm;
		PlaySound ps;
		switch(i)
		{     	   
		case DEPARTURE_IN_10_MINUTES:
			if(_cycle == TRAJET_WAY_1)
			{
				sm =new CreatureSay(0, SystemChatChannelId.Chat_Shout.getId(),_t1.getNpc1(), _t1.getSysmess10_1());
			}
			else
			{
				sm =new CreatureSay(0, SystemChatChannelId.Chat_Shout.getId(),_t2.getNpc1(), _t2.getSysmess10_1());
			}
			ps = new PlaySound(0, "itemsound.ship_arrival_departure", 1, getObjectId(), getX(), getY(), getZ());
			if (knownPlayers == null || knownPlayers.isEmpty())
				return;  
			for (L2PcInstance player : knownPlayers)
			{
				player.sendPacket(sm);
				player.sendPacket(ps);
			}
			break;
		case DEPARTURE_IN_5_MINUTES:
			if(_cycle == TRAJET_WAY_1)
			{
				sm =new CreatureSay(0, SystemChatChannelId.Chat_Shout.getId(),_t1.getNpc1(), _t1.getSysmess5_1());
			}
			else
			{
				sm =new CreatureSay(0, SystemChatChannelId.Chat_Shout.getId(),_t2.getNpc1(), _t2.getSysmess5_1());
			}
			ps = new PlaySound(0, "itemsound.ship_5min", 1, getObjectId(), getX(), getY(), getZ());
			if (knownPlayers == null || knownPlayers.isEmpty())
				return;  
			for (L2PcInstance player : knownPlayers)
			{
				player.sendPacket(sm);
				player.sendPacket(ps);
			}
			break;
		case DEPARTURE_IN_1_MINUTES:
			
			if(_cycle == TRAJET_WAY_1)
			{
				sm =new CreatureSay(0, SystemChatChannelId.Chat_Shout.getId(),_t1.getNpc1(), _t1.getSysmess1_1());
			}
			else
			{
				sm =new CreatureSay(0, SystemChatChannelId.Chat_Shout.getId(),_t2.getNpc1(), _t2.getSysmess1_1());
			}
			ps = new PlaySound(0, "itemsound.ship_1min", 1, getObjectId(), getX(), getY(), getZ());
			if (knownPlayers == null || knownPlayers.isEmpty())
				return;  
			for (L2PcInstance player : knownPlayers)
			{
				player.sendPacket(sm);
				player.sendPacket(ps);
			}
			break;
		case DEPARTURE:
			
			if(_cycle == TRAJET_WAY_1)
			{
				sm =new CreatureSay(0, SystemChatChannelId.Chat_Shout.getId(),_t1.getNpc1(), _t1.getSysmess0_1());
			}
			else
			{
				sm =new CreatureSay(0, SystemChatChannelId.Chat_Shout.getId(),_t2.getNpc1(), _t2.getSysmess0_1());
			}			
			if (knownPlayers == null || knownPlayers.isEmpty())
				return;  
			for (L2PcInstance player : knownPlayers)
			{
				player.sendPacket(sm);
				//player.sendPacket(ps);
			}
			break;
		case -1:
			if(_cycle == TRAJET_WAY_1)
			{
				sm =new CreatureSay(0, SystemChatChannelId.Chat_Shout.getId(),_t1.getNpc1(), _t1.getSysmessb_1());
			}
			else
			{
				sm =new CreatureSay(0, SystemChatChannelId.Chat_Shout.getId(),_t2.getNpc1(), _t2.getSysmessb_1());
			}
			ps = new PlaySound(0, "itemsound.ship_arrival_departure", 1, getObjectId(), getX(), getY(), getZ());
			for (L2PcInstance player : knownPlayers)
			{
				player.sendPacket(sm);
				player.sendPacket(ps);
			}
			break;
		}
	}
	
	/**
	 * 
	 */
	public void spawn()
	{		
		Collection<L2PcInstance> knownPlayers = getKnownList().getKnownPlayers().values();
		_cycle = TRAJET_WAY_1;
		beginCycle();
		if (knownPlayers == null || knownPlayers.isEmpty())
			return;
		VehicleInfo vi = new VehicleInfo(this);
		for (L2PcInstance player : knownPlayers)
			player.sendPacket(vi);        
	}
	/**
	 * @param idWaypoint1
	 * @param idWTicket1
	 * @param ntx1
	 * @param nty1
	 * @param ntz1
	 * @param idnpc1
	 * @param sysmess10_1
	 * @param sysmess5_1
	 * @param sysmess1_1
	 * @param sysmessb_1
	 */
	public void setTrajet1(int idWaypoint1, int idWTicket1, int ntx1, int nty1, int ntz1, String idnpc1, String sysmess10_1, String sysmess5_1, String sysmess1_1, String sysmess0_1, String sysmessb_1)
	{		
		_t1 = new L2BoatTrajet(idWaypoint1,idWTicket1,ntx1,nty1,ntz1,idnpc1,sysmess10_1,sysmess5_1,sysmess1_1,sysmess0_1,sysmessb_1,_name);
	}
    public L2BoatTrajet getTrajet1()
    {
        return _t1;
    }
    
	public void setTrajet2(int idWaypoint1, int idWTicket1, int ntx1, int nty1, int ntz1, String idnpc1, String sysmess10_1, String sysmess5_1, String sysmess1_1, String sysmess0_1, String sysmessb_1)
	{		
		_t2 = new L2BoatTrajet(idWaypoint1,idWTicket1,ntx1,nty1,ntz1,idnpc1,sysmess10_1,sysmess5_1,sysmess1_1,sysmess0_1,sysmessb_1,_name);
	}
    public L2BoatTrajet getTrajet2()
    {
        return _t2;
    }
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.model.L2Character#updateAbnormalEffect()
	 */
	@Override
	public void updateAbnormalEffect()
	{
		
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.model.L2Character#getActiveWeaponInstance()
	 */
	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		return null;
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.model.L2Character#getActiveWeaponItem()
	 */
	@Override
	public L2Weapon getActiveWeaponItem()
	{
		return null;
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.model.L2Character#getSecondaryWeaponInstance()
	 */
	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.model.L2Character#getSecondaryWeaponItem()
	 */
	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		return null;
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.model.L2Character#getLevel()
	 */
	@Override
	public int getLevel()
	{
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.model.L2Object#isAutoAttackable(net.sf.l2j.gameserver.model.L2Character)
	 */
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}

    /**
     * @return the needOnVehicleCheckLocation
     */
    public boolean isNeedOnVehicleCheckLocation()
    {
        return needOnVehicleCheckLocation;
    }

    /**
     * @param needOnVehicleCheckLocation the needOnVehicleCheckLocation to set
     */
    public void setNeedOnVehicleCheckLocation(boolean _needOnVehicleCheckLocation)
    {
        needOnVehicleCheckLocation = _needOnVehicleCheckLocation;
    }

    /**
     * @return the _cycle
     */
    public int getCycle()
    {
        return _cycle;
    }

    /**
     * @param _cycle the _cycle to set
     */
    public void setCycle(int cycle)
    {
        _cycle = cycle;
    }

    /**
     * @param _runstate the _runstate to set
     */
    public void setRunstate(int runstate)
    {
        _runstate = runstate;
    }
}