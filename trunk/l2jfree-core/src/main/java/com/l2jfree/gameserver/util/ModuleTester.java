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
package com.l2jfree.gameserver.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.l2jfree.Config;
import com.l2jfree.util.L2Arrays;

/**
 * @author NB4L1
 */
@SuppressWarnings("unused")
public final class ModuleTester extends Config
{
	public static void main(String[] args) throws Exception
	{
		Config.load();
		Config.DATAPACK_ROOT = new File("../l2jfree-datapack");
		//L2DatabaseFactory.getInstance();
		
		// here comes what you want to test
		//SkillTable.getInstance();
		//HtmCache.getInstance();
		
		//new WeaponSQLConverter().convert();
		
		System.gc();
		System.runFinalization();
		Thread.sleep(1000);
	}
	
	private static abstract class SQLConverter
	{
		protected abstract String getFileName();
		
		protected abstract ArrayList<String> convertImpl(ArrayList<String> list);
		
		protected final void convert() throws IOException
		{
			File f = new File(getFileName());
			
			System.out.println("Converting: '" + f.getCanonicalPath() + "'");
			
			ArrayList<String> list = new ArrayList<String>();
			
			LineNumberReader lnr = null;
			try
			{
				lnr = new LineNumberReader(new FileReader(f));
				
				for (String line; (line = lnr.readLine()) != null;)
					list.add(line);
			}
			finally
			{
				IOUtils.closeQuietly(lnr);
			}
			
			final List<String> result = convertImpl(list);
			
			PrintStream ps = null;
			try
			{
				ps = new PrintStream(f);
				
				for (String line : result)
					ps.println(line);
			}
			finally
			{
				IOUtils.closeQuietly(ps);
			}
			
			System.out.println();
			System.out.flush();
		}
	}
	
	private static final class WeaponSQLConverter extends SQLConverter
	{
		@Override
		protected String getFileName()
		{
			return "../l2jfree-datapack/sql/weapon.sql";
		}
		
		@Override
		protected ArrayList<String> convertImpl(ArrayList<String> list)
		{
			final ArrayList<String> result = new ArrayList<String>();
			
			for (String line : list)
			{
				try
				{
					String[] array = line.trim().replace("Rsk., Evasion", "Rsk. Evasion").split(",");
					
					if (array.length > 10)
					{
						array[13] = array[13].replaceAll(".00000", "");
						
						{
							int enchant4SkillId = Integer.parseInt(array[28]);
							int enchant4SkillLvl = Integer.parseInt(array[29]);
							
							if (enchant4SkillId == 0 && enchant4SkillLvl == 0)
							{
								array[28] = "''";
							}
							else
							{
								array[28] = "'" + enchant4SkillId + "-" + enchant4SkillLvl + "'";
							}
							array[29] = null;
						}
						{
							int onCastSkillId = Integer.parseInt(array[30]);
							int onCastSkillLvl = Integer.parseInt(array[31]);
							int onCastSkillChance = Integer.parseInt(array[32]);
							
							if (onCastSkillId == 0 && onCastSkillLvl == 0 && onCastSkillChance == 0)
							{
								array[30] = "''";
							}
							else
							{
								array[30] = "'" + onCastSkillId + "-" + onCastSkillLvl + "-" + onCastSkillChance + "'";
							}
							
							array[31] = null;
							array[32] = null;
						}
						{
							int onCritSkillId = Integer.parseInt(array[33]);
							int onCritSkillLvl = Integer.parseInt(array[34]);
							int onCritSkillChance = Integer.parseInt(array[35]);
							
							if (onCritSkillId == 0 && onCritSkillLvl == 0 && onCritSkillChance == 0)
							{
								array[33] = "''";
							}
							else
							{
								array[33] = "'" + onCritSkillId + "-" + onCritSkillLvl + "-" + onCritSkillChance + "'";
							}
							array[34] = null;
							array[35] = null;
						}
						{
							array[37] = array[37].replaceAll(";'", "'").replaceAll("0-0", "");
						}
						
						array = L2Arrays.compact(array);
					}
					
					StringBuilder sb = new StringBuilder();
					
					for (int i = 0; i < array.length; i++)
					{
						if (i != 0)
							sb.append(',');
						sb.append(array[i]);
					}
					
					if (line.endsWith(","))
						sb.append(',');
					
					result.add(sb.toString());
				}
				catch (RuntimeException e)
				{
					System.out.println(line);
					throw e;
				}
			}
			
			return result;
		}
	}
}
