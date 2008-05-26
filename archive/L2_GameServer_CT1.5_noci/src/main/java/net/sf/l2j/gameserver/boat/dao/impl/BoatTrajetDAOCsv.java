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
import net.sf.l2j.gameserver.boat.dao.IBoatTrajetDAO;
import net.sf.l2j.gameserver.boat.model.L2BoatPoint;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * Implementation of BoatPointDAO with a csv file
 * 
 * @see net.sf.l2j.gameserver.boat.dao.IBoatTrajetDAO
 *
 */
public class BoatTrajetDAOCsv implements IBoatTrajetDAO
{
    private static final Log _log = LogFactory.getLog(BoatTrajetDAOCsv.class.getName());
    /** Map of all boat points. We store */
    private Map<Integer, Map<Integer,L2BoatPoint>>  _trajet;
    
    /**
     * Default constructor.
     * Load the boat points from a csv file.
     */
    public BoatTrajetDAOCsv ()
    {
        _trajet = new FastMap<Integer, Map<Integer,L2BoatPoint>>();
    }
    
    /**
     * Syntax of the line : 
     * 
     * id;nbpoint;(speed1;speed2;x;y;z;time(ms))
     * 
     * the id is the waypoint identifier that we find in the boat trajet.
     * 
     * @param line
     * @return
     */
    public void parseLine(String line)
    {
        Map<Integer,L2BoatPoint> points = new FastMap<Integer, L2BoatPoint>();
        StringTokenizer st = new StringTokenizer(line, ";");
        int idWayPoint = Integer.parseInt(st.nextToken());
        int max = Integer.parseInt(st.nextToken());
        for (int i = 0; i < max; i++)
        {
            L2BoatPoint bp = new L2BoatPoint();
            bp.speed1 = Integer.parseInt(st.nextToken());
            bp.speed2 = Integer.parseInt(st.nextToken());
            bp.x = Integer.parseInt(st.nextToken());
            bp.y = Integer.parseInt(st.nextToken());
            bp.z = Integer.parseInt(st.nextToken());
            bp.time = Integer.parseInt(st.nextToken());
            points.put(i, bp);
        }
        _trajet.put(idWayPoint, points);
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.boat.dao.IBoatTrajetDAO#load()
     */
    public void load()
    {
        LineNumberReader lnr = null;
        try
        {
            File boatPathsData = new File(Config.DATAPACK_ROOT, "data/boatpath.csv");
            lnr = new LineNumberReader(new BufferedReader(new FileReader(boatPathsData)));

            String line = null;
            while ((line = lnr.readLine()) != null)
            {
                if (line.trim().length() == 0 || line.startsWith("#"))
                    continue;
                parseLine(line);
            }
        } catch (FileNotFoundException e)
        {
            _log.warn("boatpath.csv is missing in data folder");
        } catch (Exception e)
        {
            _log.warn("error while creating boat data " + e);
        } finally
        {
            try
            {
                lnr.close();
            } catch (Exception e1)
            { /* ignore problems */
            }
        }
    }
    
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.boat.dao.IBoatPointDAO#getBoatPoint(int)
     */
    public L2BoatPoint getBoatPoint(int idWayPoint,int boatPoint)
    {
        if (_trajet.get(idWayPoint) != null )
        {
            return _trajet.get(idWayPoint).get(boatPoint);  
        }
        return null;
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.boat.dao.IBoatPointDAO#getNumberOfBoatPoints()
     */
    public int getNumberOfBoatPoints(int idWayPoint)
    {
        if (_trajet.get(idWayPoint) != null )
        {
            return _trajet.get(idWayPoint).size();  
        }
        return 0;
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.boat.dao.IBoatTrajetDAO#getNumberOfBoatTrajet()
     */
    public int getNumberOfBoatTrajet()
    {
        return _trajet.size();
    }    
    
    
}
