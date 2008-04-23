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
package net.sf.l2j.gameserver.boat.dao.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.Map;
import java.util.StringTokenizer;

import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.boat.dao.IBoatDAO;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.actor.instance.L2BoatInstance;
import net.sf.l2j.gameserver.templates.L2CharTemplate;
import net.sf.l2j.gameserver.templates.StatsSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * Implementation of BoatDAO with a csv file
 * 
 * @see net.sf.l2j.gameserver.boat.dao.IBoatDAO
 *
 */
public class BoatDAOCsv implements IBoatDAO
{
    private static final Log _log = LogFactory.getLog(BoatDAOCsv.class.getName());
    /** Map of all boats */
    private Map<Integer,L2BoatInstance> _boats = new FastMap<Integer,L2BoatInstance>();
    
    /**
     * Load boats from a csv file and fill a map with all L2BoatInstance
     * All errors are ignored.
     * 
     * @see net.sf.l2j.gameserver.boat.dao.IBoatDAO#load()
     */
    public void load()
    {
        if(!Config.ALLOW_BOAT)
        {
            return;
        }
        
        LineNumberReader lnr = null;
        
        try 
        {
            File boatData = new File(Config.DATAPACK_ROOT, "data/boat.csv");
            lnr = new LineNumberReader(new BufferedReader(new FileReader(boatData)));

            String line = null;
            while ((line = lnr.readLine()) != null) 
            {
                if (line.trim().length() == 0 || line.startsWith("#")) 
                    continue;               
                L2BoatInstance boat = parseLine(line);
                if (boat != null)
                {
                    boat.spawn();
                    _boats.put(boat.getObjectId(), boat);
                    if (_log.isDebugEnabled())
                    {
                        _log.debug("Boat ID : " + boat.getObjectId());
                    }
                }
            }                   
        } 
        catch (FileNotFoundException e) 
        {
            _log.warn("boat.csv is missing in data folder");
        } 
        catch (Exception e) 
        {
            _log.warn("error while creating boat table " + e,e);
        } 
        finally 
        {
            try { lnr.close(); } catch (Exception e1) { /* ignore problems */ }
        }
        _log.info("BoatService: initialized");		
        
    }
    
    /**
     * Syntax of the line : 
     * #name;id;spawnx;spawny;spawnz;heading;IdWaypoint1;Ticket1;xtelenoticket;ytelenoticket;ztelenoticket;annonceur1;message10_1;message5_1;message1_1;message0_1;message_begin_1;;IdWaypoint2;Ticket2;xtelenoticket;ytelenoticket;ztelenoticket;annonceur2;message10_2;message5_2;message1_2;message0_2;message_begin_2 
     * 
     * @param line
     * @return a L2BoatInstance
     */
    private L2BoatInstance parseLine(String line)
    {
        try
        {
            L2BoatInstance boat;
            StringTokenizer st = new StringTokenizer(line, ";");
    
            String name = st.nextToken();
            int id = Integer.parseInt(st.nextToken());
            int xspawn = Integer.parseInt(st.nextToken());
            int yspawn = Integer.parseInt(st.nextToken());
            int zspawn = Integer.parseInt(st.nextToken());
            int heading = Integer.parseInt(st.nextToken()); 
            
            StatsSet npcDat = new StatsSet(); 
            npcDat.set("npcId", id);
            npcDat.set("level", 0);
            npcDat.set("jClass", "boat");
    
            npcDat.set("baseSTR", 0);
            npcDat.set("baseCON", 0);
            npcDat.set("baseDEX", 0);
            npcDat.set("baseINT", 0);
            npcDat.set("baseWIT", 0);
            npcDat.set("baseMEN", 0);
    
            npcDat.set("baseShldDef", 0);
            npcDat.set("baseShldRate", 0);
            npcDat.set("baseAccCombat", 38);
            npcDat.set("baseEvasRate",  38);
            npcDat.set("baseCritRate",  38);
    
            //npcDat.set("name", "");
            npcDat.set("collision_radius", 0);
            npcDat.set("collision_height", 0);
            npcDat.set("fcollision_radius", 0);
            npcDat.set("fcollision_height", 0);
            npcDat.set("sex", "male");
            npcDat.set("type", "");
            npcDat.set("baseAtkRange", 0);
            npcDat.set("baseMpMax", 0);
            npcDat.set("baseCpMax", 0);
            npcDat.set("rewardExp", 0);
            npcDat.set("rewardSp", 0);
            npcDat.set("basePAtk", 0);
            npcDat.set("baseMAtk", 0);
            npcDat.set("basePAtkSpd", 0);
            npcDat.set("aggroRange", 0);
            npcDat.set("baseMAtkSpd", 0);
            npcDat.set("rhand", 0);
            npcDat.set("lhand", 0);
            npcDat.set("armor", 0);
            npcDat.set("baseWalkSpd", 0);
            npcDat.set("baseRunSpd", 0);
            npcDat.set("name", name);
            npcDat.set("baseHpMax", 50000);
            npcDat.set("baseHpReg", 3.e-3f);
            npcDat.set("baseMpReg", 3.e-3f);
            npcDat.set("basePDef", 100);
            npcDat.set("baseMDef", 100);        
            L2CharTemplate template = new L2CharTemplate(npcDat);       
            boat = new L2BoatInstance(IdFactory.getInstance().getNextId(),template,name);
            boat.getPosition().setHeading(heading);
            boat.getPosition().setXYZ(xspawn,yspawn,zspawn);
            //boat.spawnMe();
    
            int IdWaypoint1 = Integer.parseInt(st.nextToken());
            int IdWTicket1 = Integer.parseInt(st.nextToken());
            int ntx1 = Integer.parseInt(st.nextToken());
            int nty1 = Integer.parseInt(st.nextToken());
            int ntz1 = Integer.parseInt(st.nextToken());        
            String npc1 = st.nextToken();
            String mess10_1 = st.nextToken();
            String mess5_1 = st.nextToken();
            String mess1_1 = st.nextToken();
            String mess0_1 = st.nextToken();
            String messb_1 = st.nextToken();                                           
            boat.setTrajet1(IdWaypoint1,IdWTicket1,ntx1,nty1,ntz1,npc1,mess10_1,mess5_1,mess1_1,mess0_1,messb_1);
            IdWaypoint1 = Integer.parseInt(st.nextToken());
            IdWTicket1 = Integer.parseInt(st.nextToken());
            ntx1 = Integer.parseInt(st.nextToken());
            nty1 = Integer.parseInt(st.nextToken());
            ntz1 = Integer.parseInt(st.nextToken());        
            npc1 = st.nextToken();
            mess10_1 = st.nextToken();
            mess5_1 = st.nextToken();
            mess1_1 = st.nextToken();
            mess0_1 = st.nextToken();
            messb_1 = st.nextToken();
            boat.setTrajet2(IdWaypoint1,IdWTicket1,ntx1,nty1,ntz1,npc1,mess10_1,mess5_1,mess1_1,mess0_1,messb_1);
            return boat;
        }
        catch (Exception e)
        {
            _log.warn("Unable to parse line in boat.csv : " + line );
            _log.warn("Because error : " +e.toString());
            return null;
        }
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.boat.dao.IBoatDAO#getBoat(int)
     */
    public L2BoatInstance getBoat(int boatId)
    {
        return _boats.get(boatId);
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.boat.dao.IBoatDAO#getNumberOfBoat()
     */
    public int getNumberOfBoat()
    {
        return _boats.size();
    }    
    
    
}
