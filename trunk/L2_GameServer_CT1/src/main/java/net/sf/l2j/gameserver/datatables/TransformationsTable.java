/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.datatables;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.Map;

import javolution.util.FastMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TransformationsTable
{
	private Map							transforms;
	private static TransformationsTable	_instance;
	private final static Log			_log	= LogFactory.getLog(ArmorSetsTable.class.getName());
	
	public class L2Transformation
	{
		private int		id;
		private String	name;
		private double	collision_radius;
		private double	collision_radius_f;
		private double	collision_height;
		private double	collision_height_f;
		private int		runSpd;
		
		public int getId()
		{
			return id;
		}
		
		public String getName()
		{
			return name;
		}
		
		public int getSpeed()
		{
			return runSpd;
		}
		
		public double getCollisionRadius(boolean isFemale)
		{
			if (id == 251 && isFemale)
				return collision_radius_f;
			else
				return collision_radius;
		}
		
		public double getCollisionHeight(boolean isFemale)
		{
			if (id == 251 && isFemale)
				return collision_height_f;
			else
				return collision_height;
		}
		
		public void parseParam(String ln)
		{
			String t[] = ln.split("=");
			if (t[0].equalsIgnoreCase("name"))
				name = t[1];
			if (t[0].equalsIgnoreCase("coll_radius"))
				collision_radius = Double.parseDouble(t[1]);
			if (t[0].equalsIgnoreCase("coll_height"))
				collision_height = Double.parseDouble(t[1]);
			if (t[0].equalsIgnoreCase("coll_radius_f"))
				collision_radius_f = Double.parseDouble(t[1]);
			if (t[0].equalsIgnoreCase("coll_height_f"))
				collision_height_f = Double.parseDouble(t[1]);
			if (t[0].equalsIgnoreCase("base_speed"))
				runSpd = Integer.parseInt(t[1]);
		}
		
		public L2Transformation(int _id)
		{
			super();
			id = _id;
		}
	}
	
	public TransformationsTable()
	{
	}
	
	public static TransformationsTable getInstance()
	{
		if (_instance == null)
			_instance = new TransformationsTable();
		return _instance;
	}
	
	public L2Transformation getTransform(int id)
	{
		return (L2Transformation) transforms.get(Integer.valueOf(id));
	}
	
	public void engineInit()
	{
		LineNumberReader lnr;
		String lineId;
		L2Transformation transform;
		boolean n;
		transforms = new FastMap();
		lnr = null;
		lineId = "";
		transform = null;
		n = false;
		try
		{
			lnr = new LineNumberReader(new BufferedReader(new FileReader(new File("data/transform.rsf"))));
			String line;
			while ((line = lnr.readLine()) != null)
				if (line.trim().length() != 0 && !line.startsWith("#"))
				{
					lineId = line;
					if (line.startsWith("transform"))
					{
						n = true;
						String t[] = line.split(" ");
						transform = new L2Transformation(Integer.parseInt(t[1]));
					}
					if (n && !line.startsWith("transform") && !line.startsWith("end"))
					{
						line = line.replaceAll(" ", "");
						transform.parseParam(line);
					}
					if (line.startsWith("end"))
					{
						n = false;
						transforms.put(Integer.valueOf(transform.getId()), transform);
					}
				}
		}
		catch (Exception e)
		{
			_log.warn("TransformationsTable.engineInit() >> last line parsed is \n[" + lineId + "]\n");
			e.printStackTrace();
		}
		
		try
		{
			lnr.close();
		}
		catch (Exception exception1)
		{
		}
		
		try
		{
			lnr.close();
		}
		catch (Exception exception2)
		{
		}
		
		_log.info("TransformationsTable: Loaded " + transforms.size() + " transforms.");
		return;
	}
}
