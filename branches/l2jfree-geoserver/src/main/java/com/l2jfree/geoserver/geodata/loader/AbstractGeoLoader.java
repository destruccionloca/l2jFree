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
package com.l2jfree.geoserver.geodata.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.GeoConfig;

public abstract class AbstractGeoLoader implements GeoLoader
{

	final static Log				_log				= LogFactory.getLog(AbstractGeoLoader.class.getName());

	private static final Pattern	SCANNER_DELIMITER	= Pattern.compile("([_|\\.]){1}");

	public boolean isAcceptable(File file)
	{

		if (!file.exists())
		{
			_log.info("Geo Engine: File " + file.getName() + " was not loaded!!! Reason: file doesn't exists.");
			return false;
		}

		if (file.isDirectory())
		{
			_log.info("Geo Engine: File " + file.getName() + " was not loaded!!! Reason: file is directory.");
			return false;
		}

		if (file.isHidden())
		{
			_log.info("Geo Engine: File " + file.getName() + " was not loaded!!! Reason: file is hidden.");
			return false;
		}

		if (file.length() > Integer.MAX_VALUE)
		{
			_log.info("Geo Engine: File " + file.getName() + " was not loaded!!! Reason: file is to big.");
			return false;
		}

		if (!getPattern().matcher(file.getName()).matches())
		{
			if (_log.isDebugEnabled())
				_log.info(getClass().getSimpleName() + ": can't load file: " + file.getName() + "!!! Reason: pattern missmatch");
			return false;
		}

		GeoFileInfo geoFileInfo = createGeoFileInfo(file);
		int x = geoFileInfo.getX() - 15;
		int y = geoFileInfo.getY() - 10;

		if (x < 0 || y < 0 || x > (GeoConfig.MAP_MAX_X >> 15) + Math.abs(GeoConfig.MAP_MIN_X >> 15)
				|| y > (GeoConfig.MAP_MAX_Y >> 15) + Math.abs(GeoConfig.MAP_MIN_Y >> 15))
		{
			_log.warn("Geo Engine: File " + file.getName() + " was not loaded!!! Reason: file is out of map.");
			return false;
		}

		return true;
	}

	public GeoFileInfo readFile(File file)
	{

		_log.info(getClass().getSimpleName() + ": loading geodata file: " + file.getName());

		FileInputStream fis = null;
		byte[] data = null;
		try
		{
			fis = new FileInputStream(file);
			data = new byte[fis.available()];
			int readed = fis.read(data);
			if (readed != data.length)
			{
				_log.warn("Not fully readed file?");
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return null;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
		finally
		{
			try
			{
				if (fis != null)
				{
					fis.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		GeoFileInfo geoFileInfo = createGeoFileInfo(file);
		geoFileInfo.setData(parse(convert(data)));
		return geoFileInfo;
	}

	protected GeoFileInfo createGeoFileInfo(File file)
	{
		Scanner scanner = new Scanner(file.getName());
		scanner.useDelimiter(SCANNER_DELIMITER);
		int ix = scanner.nextInt();
		int iy = scanner.nextInt();
		scanner.close();

		GeoFileInfo geoFileInfo = new GeoFileInfo();
		geoFileInfo.setX(ix);
		geoFileInfo.setY(iy);
		return geoFileInfo;
	}

	protected abstract byte[][] parse(byte[] data);

	public abstract Pattern getPattern();

	public abstract byte[] convert(byte[] data);
}