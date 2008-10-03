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
package com.l2jfree.geoserver.geodata;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.GeoConfig;
import com.l2jfree.geoserver.geodata.loader.GeoFileInfo;
import com.l2jfree.geoserver.geodata.loader.GeoLoader;
import com.l2jfree.geoserver.geodata.loader.GeoLoaderFactory;
import com.l2jfree.geoserver.model.L2Territory;
import com.l2jfree.geoserver.model.Location;

/**
 * @Author: Diamond
 * @Date: 20/5/2007
 * @Time: 9:57:48
 */
public class GeoEngine implements GeoInterface
{
	final static Log					_log					= LogFactory.getLog(GeoEngine.class.getName());

	public static final byte			BLOCKTYPE_FLAT			= 0;
	public static final byte			BLOCKTYPE_COMPLEX		= 1;
	public static final byte			BLOCKTYPE_MULTILEVEL	= 2;

	public static final byte			GEODATA_ARRAY_OFFSET_X	= 15;
	public static final byte			GEODATA_ARRAY_OFFSET_Y	= 10;

	private static final byte			E						= 1;													// 0001
	private static final byte			W						= 2;													// 0010
	private static final byte			S						= 4;													// 0100
	private static final byte			N						= 8;													// 1000

	private final GeoMove				geoMove					= new GeoMove(this);

	public static final int				geodataSizeX			= GeoConfig.MAP_MAX_X - GeoConfig.MAP_MIN_X + 1 >> 15;
	public static final int				geodataSizeY			= GeoConfig.MAP_MAX_Y - GeoConfig.MAP_MIN_Y + 1 >> 15;

	protected static final byte[][][][]	geodata					= new byte[geodataSizeX][geodataSizeY][][];

	public short getType(int x, int y)
	{
		return NgetType(x - GeoConfig.MAP_MIN_X >> 4, y - GeoConfig.MAP_MIN_Y >> 4);
	}

	public int getHeight(int x, int y, int z)
	{
		return NgetHeight(x - GeoConfig.MAP_MIN_X >> 4, y - GeoConfig.MAP_MIN_Y >> 4, z);
	}

	public boolean canSeeCoord(int x, int y, int z, int tx, int ty, int tz)
	{
		return canSeeTarget(x, y, z, tx, ty, tz);
	}

	public boolean canMoveToCoord(int x, int y, int z, int tx, int ty, int tz)
	{
		return canMoveToTarget(x, y, z, tx, ty, tz);
	}

	public boolean canMoveToCoordWithCollision(int x, int y, int z, int tx, int ty, int tz)
	{
		return canMoveToTargetWithCollision(x, y, z, tx, ty, tz);
	}

	public short getNSWE(int x, int y, int z)
	{
		return NgetNSWE(x - GeoConfig.MAP_MIN_X >> 4, y - GeoConfig.MAP_MIN_Y >> 4, z);
	}

	public Location moveCheck(int x, int y, int z, int tx, int ty)
	{
		return MoveCheck(x - GeoConfig.MAP_MIN_X >> 4, y - GeoConfig.MAP_MIN_Y >> 4, z, tx - GeoConfig.MAP_MIN_X >> 4, ty - GeoConfig.MAP_MIN_Y >> 4);
	}

	public Location moveCheckForAI(Location loc1, Location loc2)
	{
		return MoveCheckForAI(loc1.getX() - GeoConfig.MAP_MIN_X >> 4, loc1.getY() - GeoConfig.MAP_MIN_Y >> 4, loc1.getZ(),
				loc2.getY() - GeoConfig.MAP_MIN_X >> 4, loc2.getY() - GeoConfig.MAP_MIN_Y >> 4);
	}

	public boolean canSeeTarget(int x, int y, int z, int tx, int ty, int tz)
	{
		if (tz >= z)
			return canSee(x - GeoConfig.MAP_MIN_X >> 4, y - GeoConfig.MAP_MIN_Y >> 4, z, tx - GeoConfig.MAP_MIN_X >> 4, ty - GeoConfig.MAP_MIN_Y >> 4, tz);
		return canSee(tx - GeoConfig.MAP_MIN_X >> 4, ty - GeoConfig.MAP_MIN_Y >> 4, tz, x - GeoConfig.MAP_MIN_X >> 4, y - GeoConfig.MAP_MIN_Y >> 4, z);
	}

	public boolean canMoveToTarget(int x, int y, int z, int tx, int ty, int tz)
	{
		return canMove(x - GeoConfig.MAP_MIN_X >> 4, y - GeoConfig.MAP_MIN_Y >> 4, z, tx - GeoConfig.MAP_MIN_X >> 4, ty - GeoConfig.MAP_MIN_Y >> 4, tz);
	}

	public boolean canMoveToTargetWithCollision(int x, int y, int z, int tx, int ty, int tz)
	{
		return canMoveWithCollision(x - GeoConfig.MAP_MIN_X >> 4, y - GeoConfig.MAP_MIN_Y >> 4, z, tx - GeoConfig.MAP_MIN_X >> 4,
				ty - GeoConfig.MAP_MIN_Y >> 4, tz);
	}

	/**
	 * @param NSWE
	 * @param x
	 * @param y
	 * @param tx
	 * @param ty
	 * 
	 * @return True if NSWE dont block given direction
	 */
	public boolean checkNSWE(short NSWE, int x, int y, int tx, int ty)
	{
		// Check NSWE
		if (NSWE == 15)
			return true;
		if (tx > x) // E
		{
			if ((NSWE & E) == 0)
				return false;
		}
		else if (tx < x)
			if ((NSWE & W) == 0)
				return false;
		if (ty > y) // S
		{
			if ((NSWE & S) == 0)
				return false;
		}
		else if (ty < y)
			if ((NSWE & N) == 0)
				return false;
		return true;
	}

	private boolean NLOS(int ray_x, int ray_y, int ray_z)
	{
		Layer[] layers = NGetLayers(ray_x, ray_y);
		if (layers.length == 0)
			return true;
		short geo_z = Short.MIN_VALUE;
		for (Layer layer : layers)
			if (Math.abs(ray_z - geo_z) > Math.abs(ray_z - layer.height))
				geo_z = layer.height;
		return ray_z >= geo_z;
	}

	private boolean canSee(int _x, int _y, int _z, int _tx, int _ty, int _tz)
	{
		int dx = _tx - _x;
		int dy = _ty - _y;
		int dz = _tz - _z;
		int inc_x = sign(dx);
		int inc_y = sign(dy);
		dx = Math.abs(dx);
		dy = Math.abs(dy);
		if (dx + dy == 0)
			return true;
		float inc_z_for_x = dx == 0 ? 0 : dz / dx;
		float inc_z_for_y = dy == 0 ? 0 : dz / dy;
		int x = _x;
		int y = _y;
		int z = _z + 64;
		int next_x = x;
		int next_y = y;
		int next_z = z;
		if (dx >= dy) // dy/dx <= 1
		{
			int delta_A = 2 * dy;
			int d = delta_A - dx;
			int delta_B = delta_A - 2 * dx;
			for (int i = 0; i < dx; i++)
			{
				x = next_x;
				y = next_y;
				z = next_z;
				if (d > 0)
				{
					d += delta_B;
					next_x += inc_x;
					next_z += inc_z_for_x;
					next_y += inc_y;
					next_z += inc_z_for_y;
					if (!NLOS(x, y, z))
						return false;
				}
				else
				{
					d += delta_A;
					next_x += inc_x;
					next_z += inc_z_for_x;
					if (!NLOS(x, y, z))
						return false;
				}
			}
		}
		else
		{
			int delta_A = 2 * dx;
			int d = delta_A - dy;
			int delta_B = delta_A - 2 * dy;
			for (int i = 0; i < dy; i++)
			{
				x = next_x;
				y = next_y;
				z = next_z;
				if (d > 0)
				{
					d += delta_B;
					next_x += inc_x;
					next_z += inc_z_for_x;
					next_y += inc_y;
					next_z += inc_z_for_y;
					if (!NLOS(x, y, z))
						return false;
				}
				else
				{
					d += delta_A;
					next_y += inc_y;
					next_z += inc_z_for_y;
					if (!NLOS(x, y, z))
						return false;
				}
			}
		}
		return true;
	}

	public boolean canMoveWithCollision(int x, int y, int z, int tx, int ty, int tz)
	{
		int dx = tx - x;
		int dy = ty - y;
		int inc_x = sign(dx);
		int inc_y = sign(dy);
		dx = Math.abs(dx);
		dy = Math.abs(dy);
		if (dx + dy == 0)
			return tz > z ? tz - z < 64 : true;
		int next_x = x;
		int next_y = y;
		int next_z = z;
		if (dx >= dy) // dy/dx <= 1
		{
			int delta_A = 2 * dy;
			int d = delta_A - dx;
			int delta_B = delta_A - 2 * dx;
			for (int i = 0; i < dx; i++)
			{
				x = next_x;
				y = next_y;
				z = next_z;
				if (d > 0)
				{
					d += delta_B;
					next_x += inc_x;
					next_y += inc_y;
					next_z = NcanMoveNext(x, y, z, next_x, next_y);
					if (next_z == 0 || NcanMoveNext(x - 1, y, z, next_x - 1, next_y) == 0 || NcanMoveNext(x + 1, y, z, next_x + 1, next_y) == 0)
						return false;
				}
				else
				{
					d += delta_A;
					next_x += inc_x;
					next_z = NcanMoveNext(x, y, z, next_x, next_y);
					if (next_z == 0 || NcanMoveNext(x - 1, y, z, next_x - 1, next_y) == 0 || NcanMoveNext(x + 1, y, z, next_x + 1, next_y) == 0)
						return false;
				}
			}
		}
		else
		{
			int delta_A = 2 * dx;
			int d = delta_A - dy;
			int delta_B = delta_A - 2 * dy;
			for (int i = 0; i < dy; i++)
			{
				x = next_x;
				y = next_y;
				z = next_z;
				if (d > 0)
				{
					d += delta_B;
					next_x += inc_x;
					next_y += inc_y;
					next_z = NcanMoveNext(x, y, z, next_x, next_y);
					if (next_z == 0 || NcanMoveNext(x - 1, y, z, next_x - 1, next_y) == 0 || NcanMoveNext(x + 1, y, z, next_x + 1, next_y) == 0)
						return false;
				}
				else
				{
					d += delta_A;
					next_y += inc_y;
					next_z = NcanMoveNext(x, y, z, next_x, next_y);
					if (next_z == 0 || NcanMoveNext(x - 1, y, z, next_x - 1, next_y) == 0 || NcanMoveNext(x + 1, y, z, next_x + 1, next_y) == 0)
						return false;
				}
			}
		}
		return Math.abs(tz - next_z) < 64;
	}

	public boolean canMove(int x, int y, int z, int tx, int ty, int tz)
	{
		int dx = tx - x;
		int dy = ty - y;
		int inc_x = sign(dx);
		int inc_y = sign(dy);
		dx = Math.abs(dx);
		dy = Math.abs(dy);
		if (dx + dy == 0)
			return tz > z ? tz - z < 64 : true;
		int next_x = x;
		int next_y = y;
		int next_z = z;
		if (dx >= dy) // dy/dx <= 1
		{
			int delta_A = 2 * dy;
			int d = delta_A - dx;
			int delta_B = delta_A - 2 * dx;
			for (int i = 0; i < dx; i++)
			{
				x = next_x;
				y = next_y;
				z = next_z;
				if (d > 0)
				{
					d += delta_B;
					next_x += inc_x;
					next_y += inc_y;
					next_z = NcanMoveNext(x, y, z, next_x, next_y);
					if (next_z == 0)
						return false;
				}
				else
				{
					d += delta_A;
					next_x += inc_x;
					next_z = NcanMoveNext(x, y, z, next_x, next_y);
					if (next_z == 0)
						return false;
				}
			}
		}
		else
		{
			int delta_A = 2 * dx;
			int d = delta_A - dy;
			int delta_B = delta_A - 2 * dy;
			for (int i = 0; i < dy; i++)
			{
				x = next_x;
				y = next_y;
				z = next_z;
				if (d > 0)
				{
					d += delta_B;
					next_x += inc_x;
					next_y += inc_y;
					next_z = NcanMoveNext(x, y, z, next_x, next_y);
					if (next_z == 0)
						return false;
				}
				else
				{
					d += delta_A;
					next_y += inc_y;
					next_z = NcanMoveNext(x, y, z, next_x, next_y);
					if (next_z == 0)
						return false;
				}
			}
		}
		return Math.abs(tz - next_z) < 64;
	}

	private Location MoveCheck(int x, int y, int z, int tx, int ty)
	{
		int dx = tx - x;
		int dy = ty - y;
		int inc_x = sign(dx);
		int inc_y = sign(dy);
		dx = Math.abs(dx);
		dy = Math.abs(dy);
		if (dx + dy < 2 || dx == 2 && dy == 0 || dx == 0 && dy == 2)
			return new Location((x << 4) + GeoConfig.MAP_MIN_X, (y << 4) + GeoConfig.MAP_MIN_Y, z);
		int prev_x;
		int prev_y;
		int prev_z;
		int next_x = x;
		int next_y = y;
		int next_z = z;
		if (dx >= dy) // dy/dx <= 1
		{
			int delta_A = 2 * dy;
			int d = delta_A - dx;
			int delta_B = delta_A - 2 * dx;
			for (int i = 0; i < dx; i++)
			{
				prev_x = x;
				prev_y = y;
				prev_z = z;
				x = next_x;
				y = next_y;
				z = next_z;
				if (d > 0)
				{
					d += delta_B;
					next_x += inc_x;
					next_y += inc_y;
					next_z = NcanMoveNext(x, y, z, next_x, next_y);
					if (next_z == 0)
						return new Location((prev_x << 4) + GeoConfig.MAP_MIN_X, (prev_y << 4) + GeoConfig.MAP_MIN_Y, prev_z);
				}
				else
				{
					d += delta_A;
					next_x += inc_x;
					next_z = NcanMoveNext(x, y, z, next_x, next_y);
					if (next_z == 0)
						return new Location((prev_x << 4) + GeoConfig.MAP_MIN_X, (prev_y << 4) + GeoConfig.MAP_MIN_Y, prev_z);
				}
			}
		}
		else
		{
			int delta_A = 2 * dx;
			int d = delta_A - dy;
			int delta_B = delta_A - 2 * dy;
			for (int i = 0; i < dy; i++)
			{
				prev_x = x;
				prev_y = y;
				prev_z = z;
				x = next_x;
				y = next_y;
				z = next_z;
				if (d > 0)
				{
					d += delta_B;
					next_x += inc_x;
					next_y += inc_y;
					next_z = NcanMoveNext(x, y, z, next_x, next_y);
					if (next_z == 0)
						return new Location((prev_x << 4) + GeoConfig.MAP_MIN_X, (prev_y << 4) + GeoConfig.MAP_MIN_Y, prev_z);
				}
				else
				{
					d += delta_A;
					next_y += inc_y;
					next_z = NcanMoveNext(x, y, z, next_x, next_y);
					if (next_z == 0)
						return new Location((prev_x << 4) + GeoConfig.MAP_MIN_X, (prev_y << 4) + GeoConfig.MAP_MIN_Y, prev_z);
				}
			}
		}
		return new Location((next_x << 4) + GeoConfig.MAP_MIN_X, (next_y << 4) + GeoConfig.MAP_MIN_Y, next_z);
	}

	public Location MoveCheckForPathFind(int x, int y, int z, int tx, int ty)
	{
		int dx = tx - x;
		int dy = ty - y;
		int inc_x = sign(dx);
		int inc_y = sign(dy);
		dx = Math.abs(dx);
		dy = Math.abs(dy);
		int prev_x = x;
		int prev_y = y;
		int prev_z = z;
		int next_x = x;
		int next_y = y;
		int next_z = z;
		if (dx >= dy) // dy/dx <= 1
		{
			int delta_A = 2 * dy;
			int d = delta_A - dx;
			int delta_B = delta_A - 2 * dx;
			for (int i = 0; i < dx; i++)
			{
				prev_x = x;
				prev_y = y;
				prev_z = z;
				x = next_x;
				y = next_y;
				z = next_z;
				if (d > 0)
				{
					d += delta_B;
					next_x += inc_x;
					next_y += inc_y;
					next_z = NcanMoveNextForPathFind(x, y, z, next_x, next_y);
					if (next_z == 0 || NcanMoveNextForPathFind(x - 1, y, z, next_x - 1, next_y) == 0
							|| NcanMoveNextForPathFind(x + 1, y, z, next_x + 1, next_y) == 0)
						return new Location(prev_x, prev_y, prev_z);
				}
				else
				{

					d += delta_A;
					next_x += inc_x;
					next_z = NcanMoveNextForPathFind(x, y, z, next_x, next_y);
					if (next_z == 0 || NcanMoveNextForPathFind(x - 1, y, z, next_x - 1, next_y) == 0
							|| NcanMoveNextForPathFind(x + 1, y, z, next_x + 1, next_y) == 0)
						return new Location(prev_x, prev_y, prev_z);
				}
			}
		}
		else
		{
			int delta_A = 2 * dx;
			int d = delta_A - dy;
			int delta_B = delta_A - 2 * dy;
			for (int i = 0; i < dy; i++)
			{
				prev_x = x;
				prev_y = y;
				prev_z = z;
				x = next_x;
				y = next_y;
				z = next_z;
				if (d > 0)
				{
					d += delta_B;
					next_x += inc_x;
					next_y += inc_y;
					next_z = NcanMoveNextForPathFind(x, y, z, next_x, next_y);
					if (next_z == 0 || NcanMoveNextForPathFind(x - 1, y, z, next_x - 1, next_y) == 0
							|| NcanMoveNextForPathFind(x + 1, y, z, next_x + 1, next_y) == 0)
						return new Location(prev_x, prev_y, prev_z);
				}
				else
				{
					d += delta_A;
					next_y += inc_y;
					next_z = NcanMoveNextForPathFind(x, y, z, next_x, next_y);
					if (next_z == 0 || NcanMoveNextForPathFind(x - 1, y, z, next_x - 1, next_y) == 0
							|| NcanMoveNextForPathFind(x + 1, y, z, next_x + 1, next_y) == 0)
						return new Location(prev_x, prev_y, prev_z);
				}
			}
		}
		return new Location(next_x, next_y, next_z);
	}

	private Location MoveCheckForAI(int x, int y, int z, int tx, int ty)
	{
		int dx = tx - x;
		int dy = ty - y;
		int inc_x = sign(dx);
		int inc_y = sign(dy);
		dx = Math.abs(dx);
		dy = Math.abs(dy);
		if (dx + dy < 2 || dx == 2 && dy == 0 || dx == 0 && dy == 2)
			return new Location((x << 4) + GeoConfig.MAP_MIN_X, (y << 4) + GeoConfig.MAP_MIN_Y, z);
		int prev_x = x;
		int prev_y = y;
		int prev_z = z;
		int next_x = x;
		int next_y = y;
		int next_z = z;
		if (dx >= dy) // dy/dx <= 1
		{
			int delta_A = 2 * dy;
			int d = delta_A - dx;
			int delta_B = delta_A - 2 * dx;
			for (int i = 0; i < dx; i++)
			{
				prev_x = x;
				prev_y = y;
				prev_z = z;
				x = next_x;
				y = next_y;
				z = next_z;
				if (d > 0)
				{
					d += delta_B;
					next_x += inc_x;
					next_y += inc_y;
					next_z = NcanMoveNextForAI(x, y, z, next_x, next_y);
					if (next_z == 0)
						return new Location((prev_x << 4) + GeoConfig.MAP_MIN_X, (prev_y << 4) + GeoConfig.MAP_MIN_Y, prev_z);
				}
				else
				{
					d += delta_A;
					next_x += inc_x;
					next_z = NcanMoveNextForAI(x, y, z, next_x, next_y);
					if (next_z == 0)
						return new Location((prev_x << 4) + GeoConfig.MAP_MIN_X, (prev_y << 4) + GeoConfig.MAP_MIN_Y, prev_z);
				}
			}
		}
		else
		{
			int delta_A = 2 * dx;
			int d = delta_A - dy;
			int delta_B = delta_A - 2 * dy;
			for (int i = 0; i < dy; i++)
			{
				prev_x = x;
				prev_y = y;
				prev_z = z;
				x = next_x;
				y = next_y;
				z = next_z;
				if (d > 0)
				{
					d += delta_B;
					next_x += inc_x;
					next_y += inc_y;
					next_z = NcanMoveNextForAI(x, y, z, next_x, next_y);
					if (next_z == 0)
						return new Location((prev_x << 4) + GeoConfig.MAP_MIN_X, (prev_y << 4) + GeoConfig.MAP_MIN_Y, prev_z);
				}
				else
				{
					d += delta_A;
					next_y += inc_y;
					next_z = NcanMoveNextForAI(x, y, z, next_x, next_y);
					if (next_z == 0)
						return new Location((prev_x << 4) + GeoConfig.MAP_MIN_X, (prev_y << 4) + GeoConfig.MAP_MIN_Y, prev_z);
				}
			}
		}
		return new Location((next_x << 4) + GeoConfig.MAP_MIN_X, (next_y << 4) + GeoConfig.MAP_MIN_Y, next_z);
	}

	public int NcanMoveNext(int x, int y, int z, int next_x, int next_y)
	{
		Layer[] layers1 = NGetLayers(x, y);
		Layer[] layers2 = NGetLayers(next_x, next_y);

		if (layers1.length == 0 || layers2.length == 0)
			return z == 0 ? 1 : z;

		short z1 = Short.MIN_VALUE;
		short z2 = Short.MIN_VALUE;
		short NSWE1 = 15;

		for (Layer layer : layers1)
			if (Math.abs(z - z1) > Math.abs(z - layer.height))
			{
				z1 = layer.height;
				NSWE1 = layer.nswe;
			}

		if (z1 < -30000)
			return 0;

		if (!checkNSWE(NSWE1, x, y, next_x, next_y))
			return 0;

		for (Layer layer : layers2)
			if (layer.height < z1 + 64 && Math.abs(z1 - z2) > Math.abs(z1 - layer.height))
				z2 = layer.height;

		if (z2 < -30000)
			return 0;

		return z2 == 0 ? 1 : z2;
	}

	public int NcanMoveNextForPathFind(int x, int y, int z, int next_x, int next_y)
	{
		Layer[] layers1 = NGetLayers(x, y);
		Layer[] layers2 = NGetLayers(next_x, next_y);

		if (layers1.length == 0 || layers2.length == 0)
			return z == 0 ? 1 : z;

		short z1 = Short.MIN_VALUE;
		short z2 = Short.MIN_VALUE;
		short NSWE1 = 15;
		short NSWE2 = 15;

		for (Layer layer : layers1)
			if (Math.abs(z - z1) > Math.abs(z - layer.height))
			{
				z1 = layer.height;
				NSWE1 = layer.nswe;
			}

		if (z1 < -30000)
			return 0;

		for (Layer layer : layers2)
			if (layer.height < z1 + 64 && Math.abs(z1 - z2) > Math.abs(z1 - layer.height))
			{
				z2 = layer.height;
				NSWE2 = layer.nswe;
			}

		if (z2 < -30000)
			return 0;

		if (!checkNSWE(NSWE1, x, y, next_x, next_y) || !checkNSWE(NSWE2, next_x, next_y, x, y))
			return 0;

		return z2 == 0 ? 1 : z2;
	}

	public int NcanMoveNextForAI(int x, int y, int z, int next_x, int next_y)
	{
		Layer[] layers1 = NGetLayers(x, y);
		Layer[] layers2 = NGetLayers(next_x, next_y);

		if (layers1.length == 0 || layers2.length == 0)
			return z == 0 ? 1 : z;

		short z1 = Short.MIN_VALUE;
		short z2 = Short.MIN_VALUE;
		short NSWE1 = 15;
		short NSWE2 = 15;

		for (Layer layer : layers1)
			if (Math.abs(z - z1) > Math.abs(z - layer.height))
			{
				z1 = layer.height;
				NSWE1 = layer.nswe;
			}

		if (z1 < -30000)
			return 0;

		for (Layer layer : layers2)
			if (layer.height < z1 + 64 && Math.abs(z1 - z2) > Math.abs(z1 - layer.height))
			{
				z2 = layer.height;
				NSWE2 = layer.nswe;
			}

		if (z2 < -30000)
			return 0;

		if (z1 > z2 && z1 - z2 >= 64)
			return 0;

		if (!checkNSWE(NSWE1, x, y, next_x, next_y) || !checkNSWE(NSWE2, next_x, next_y, x, y))
			return 0;

		return z2 == 0 ? 1 : z2;
	}

	private Layer[] NGetLayers(int geoX, int geoY)
	{
		byte[] block = getGeoBlockFromGeoCoords(geoX, geoY);

		if (block == null)
			return new Layer[0];

		int cellX, cellY;
		int index = 0;
		// Read current block type: 0 - flat, 1 - complex, 2 - multilevel
		byte type = block[index];
		index++;

		switch (type)
		{
		case BLOCKTYPE_FLAT:
			short height = makeShort(block[index + 1], block[index]);
			height = (short) (height & 0x0fff0);
			return new Layer[]
			{ new Layer(height, (short) 15) };
		case BLOCKTYPE_COMPLEX:
			cellX = getCell(geoX);
			cellY = getCell(geoY);
			index += (cellX << 3) + cellY << 1;
			height = makeShort(block[index + 1], block[index]);
			return new Layer[]
			{ new Layer((short) ((short) (height & 0x0fff0) >> 1), (short) (height & 0x0F)) };
		case BLOCKTYPE_MULTILEVEL:
			cellX = getCell(geoX);
			cellY = getCell(geoY);
			int offset = (cellX << 3) + cellY;
			while (offset > 0)
			{
				byte lc = block[index];
				index += (lc << 1) + 1;
				offset--;
			}
			byte layer_count = block[index];
			index++;
			if (layer_count <= 0 || layer_count > 125)
				return new Layer[0];
			Layer[] layers = new Layer[layer_count];
			while (layer_count > 0)
			{
				height = makeShort(block[index + 1], block[index]);
				layer_count--;
				layers[layer_count] = new Layer((short) ((short) (height & 0x0fff0) >> 1), (short) (height & 0x0F));
				index += 2;
			}
			return layers;
		default:
			_log.fatal("GeoEngine: Unknown block type");
			return new Layer[0];
		}
	}

	private short NgetType(int geoX, int geoY)
	{
		byte[] block = getGeoBlockFromGeoCoords(geoX, geoY);

		if (block == null)
			return 0;

		return block[0];
	}

	public int NgetHeight(int geoX, int geoY, int z)
	{
		byte[] block = getGeoBlockFromGeoCoords(geoX, geoY);

		if (block == null)
			return z;

		int cellX, cellY, index = 0;

		// Read current block type: 0 - flat, 1 - complex, 2 - multilevel
		byte type = block[index];
		index++;

		short height;
		switch (type)
		{
		case BLOCKTYPE_FLAT:
			height = makeShort(block[index + 1], block[index]);
			return (short) (height & 0x0fff0);
		case BLOCKTYPE_COMPLEX:
			cellX = getCell(geoX);
			cellY = getCell(geoY);
			index += (cellX << 3) + cellY << 1;
			height = makeShort(block[index + 1], block[index]);
			height = (short) (height & 0x0fff0);
			height = (short) (height >> 1); // height / 2
			return height;
		case BLOCKTYPE_MULTILEVEL:
			cellX = getCell(geoX);
			cellY = getCell(geoY);
			int offset = (cellX << 3) + cellY;
			while (offset > 0)
			{
				byte lc = block[index];
				index += (lc << 1) + 1;
				offset--;
			}
			byte layers = block[index];
			index++;
			if (layers <= 0 || layers > 125)
				return (short) z;
			short temph = Short.MIN_VALUE;
			while (layers > 0)
			{
				height = makeShort(block[index + 1], block[index]);
				height = (short) (height & 0x0fff0);
				height = (short) (height >> 1); // height / 2
				if ((z - temph) * (z - temph) > (z - height) * (z - height))
					temph = height;
				layers--;
				index += 2;
			}
			return temph;
		default:
			_log.fatal("GeoEngine: Unknown blockType");
			return z;
		}
	}

	public short NgetNSWE(int geoX, int geoY, int z)
	{
		byte[] block = getGeoBlockFromGeoCoords(geoX, geoY);

		if (block == null)
			return 15;

		int cellX, cellY;
		short NSWE = 15;
		int index = 0;

		// Read current block type: 0 - flat, 1 - complex, 2 - multilevel
		byte type = block[index];
		index++;

		switch (type)
		{
		case BLOCKTYPE_FLAT:
			return 15;
		case BLOCKTYPE_COMPLEX:
			cellX = getCell(geoX);
			cellY = getCell(geoY);
			index += (cellX << 3) + cellY << 1;
			short height = makeShort(block[index + 1], block[index]);
			return (short) (height & 0x0F);
		case BLOCKTYPE_MULTILEVEL:
			cellX = getCell(geoX);
			cellY = getCell(geoY);
			int offset = (cellX << 3) + cellY;
			while (offset > 0)
			{
				byte lc = block[index];
				index += (lc << 1) + 1;
				offset--;
			}
			byte layers = block[index];
			index++;
			if (layers <= 0 || layers > 125)
				return 15;
			short tempz = Short.MIN_VALUE;
			while (layers > 0)
			{
				height = makeShort(block[index + 1], block[index]);
				height = (short) (height & 0x0fff0);
				height = (short) (height >> 1); // height / 2

				if ((z - tempz) * (z - tempz) > (z - height) * (z - height))
				{
					tempz = height;
					NSWE = makeShort(block[index + 1], block[index]);
					NSWE = (short) (NSWE & 0x0F);
				}
				layers--;
				index += 2;
			}
			return NSWE;
		default:
			_log.fatal("GeoEngine: Unknown block type.");
			return 0;
		}
	}

	protected static short makeShort(byte b1, byte b0)
	{
		return (short) (b1 << 8 | b0 & 0xff);
	}

	protected static int getBlock(int geoPos)
	{
		return (geoPos >> 3) % 256;
	}

	protected static int getCell(int geoPos)
	{
		return geoPos % 8;
	}

	protected static int getBlockIndex(int blockX, int blockY)
	{
		return (blockX << 8) + blockY;
	}

	private static byte sign(int x)
	{
		if (x >= 0)
			return +1;
		return -1;
	}

	protected byte[] getGeoBlockFromGeoCoords(int geoX, int geoY)
	{
		int ix = geoX >> 11;
		int iy = geoY >> 11;

		if (ix < 0 || ix >= geodataSizeX || iy < 0 || iy >= geodataSizeY)
			return null;

		byte[][] region = geodata[ix][iy];

		if (region == null)
			return null;

		int blockX = getBlock(geoX);
		int blockY = getBlock(geoY);

		return region[getBlockIndex(blockX, blockY)];
	}

	public static void loadGeo()
	{
		_log.info("Geo Engine: - Loading Geodata...");

		File f = new File("geodata");

		if (!f.exists() || !f.isDirectory())
		{
			_log.info("Geo Engine: Files missing, loading aborted.");
			return;
		}

		for (File q : f.listFiles())
		{
			if (q.isHidden() || q.isDirectory())
				continue;

			GeoLoader geoLoader = GeoLoaderFactory.getInstance().getGeoLoader(q);

			if (geoLoader != null)
			{
				GeoFileInfo geoFileInfo = geoLoader.readFile(q);
				if (geoFileInfo != null)
				{

					int x = geoFileInfo.getX() - GEODATA_ARRAY_OFFSET_X;
					int y = geoFileInfo.getY() - GEODATA_ARRAY_OFFSET_Y;

					if (geodata[x][y] != null && geodata[x][y].length > 0)
					{
						_log.warn("Geodata in region " + geoFileInfo.getX() + "_" + geoFileInfo.getY() + " was replased by "
								+ geoLoader.getClass().getSimpleName());
					}

					addToGeoArray(x, y, geoFileInfo.getData());
				}
			}
		}
	}

	/**
	 * Adds to array of geodata "data" bytes.<br>
	 * Please make sure that array positions are after index offset substraction.<br>
	 * <p>
	 * Example:
	 * <ul>
	 * <li>X: 25 - {@value #GEODATA_ARRAY_OFFSET_X} ({@link #GEODATA_ARRAY_OFFSET_X}) = 10</li>
	 * <li>Y: 25 - {@value #GEODATA_ARRAY_OFFSET_Y} ({@link #GEODATA_ARRAY_OFFSET_Y}) = 15</li>
	 * </ul>
	 *
	 * @see #GEODATA_ARRAY_OFFSET_X
	 * @see #GEODATA_ARRAY_OFFSET_Y
	 *
	 * @param xArrayPos x position of the block in the array
	 * @param yArrayPos y position of the block in the array
	 * @param data bytes that will be setted into geodata array
	 */
	public static void addToGeoArray(int xArrayPos, int yArrayPos, byte[][] data)
	{
		geodata[xArrayPos][yArrayPos] = data;
	}

	/*
		public static boolean DumpGeodataFile(int cx, int cy)
		{
			byte rx = (byte) (Math.floor((float) cx / (float) 32768) + 20);
			byte ry = (byte) (Math.floor((float) cy / (float) 32768) + 18);
			String name = "log/" + rx + "_" + ry + ".l2j";
			return DumpGeodataFile(name, rx, ry);
		}

		public static boolean DumpGeodataFile(String _name, byte rx, byte ry)
		{
			int ix = rx - 15;
			int iy = ry - 10;

			byte[][] geoblocks = geodata[ix][iy];
			if(geoblocks == null)
				return false;

			FileChannel wChannel;
			try
			{
				File f = new File(_name);
				if(f.exists())
					f.delete();
				wChannel = new RandomAccessFile(_name, "rw").getChannel();

				for(byte[] geoblock : geoblocks)
				{
					ByteBuffer buffer = ByteBuffer.wrap(geoblock);
					wChannel.write(buffer);
					buffer = null;
				}
				wChannel.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return false;
			}

			return true;
		}
	*/

	protected void copyBlock(int ix, int iy, int blockIndex)
	{
		byte[][] region = geodata[ix][iy];

		if (region == null)
		{
			_log.warn("door at null region? [" + ix + "][" + iy + "]");
			return;
		}

		byte[] block = region[blockIndex];
		byte blockType = block[0];

		switch (blockType)
		{
		case BLOCKTYPE_FLAT:
			short height = makeShort(block[2], block[1]);
			height &= 0x0fff0;
			height <<= 1;
			height |= N;
			height |= S;
			height |= W;
			height |= E;
			byte[] newblock = new byte[129];
			newblock[0] = BLOCKTYPE_COMPLEX;
			for (int i = 1; i < 129; i += 2)
			{
				newblock[i + 1] = (byte) (height >> 8);
				newblock[i] = (byte) (height & 0x00ff);
			}
			region[blockIndex] = newblock;
		}
	}

	private static int	Door_MaxZDiff	= 256;

	private static boolean check_door_z(int minZ, int maxZ, int geoZ)
	{
		if (minZ <= geoZ && geoZ <= maxZ)
			return true;
		return Math.abs((minZ + maxZ) / 2 - geoZ) <= Door_MaxZDiff;
	}

	private static boolean check_cell_in_door(int geoX, int geoY, L2Territory pos)
	{
		geoX = (geoX << 4) + GeoConfig.MAP_MIN_X + 8;
		geoY = (geoY << 4) + GeoConfig.MAP_MIN_Y + 8;
		for (int ax = geoX; ax < geoX + 16; ax++)
			for (int ay = geoY; ay < geoY + 16; ay++)
				if (pos.isInside(ax, ay))
					return true;
		return false;
	}

	public void openDoor(L2Territory pos)
	{
		int minX = pos.getXmin() - GeoConfig.MAP_MIN_X >> 4;
		int maxX = pos.getXmax() - GeoConfig.MAP_MIN_X >> 4;
		int minY = pos.getYmin() - GeoConfig.MAP_MIN_Y >> 4;
		int maxY = pos.getYmax() - GeoConfig.MAP_MIN_Y >> 4;
		short height;

		for (int geoX = minX; geoX <= maxX; geoX++)
			for (int geoY = minY; geoY <= maxY; geoY++)
			{
				if (!check_cell_in_door(geoX, geoY, pos))
					continue;
				int ix = geoX >> 11;
				int iy = geoY >> 11;

				int blockX = getBlock(geoX);
				int blockY = getBlock(geoY);
				int blockIndex = getBlockIndex(blockX, blockY);

				copyBlock(ix, iy, blockIndex);

				byte[][] region = geodata[ix][iy];
				if (region == null)
				{
					_log.warn("GeoEngine: Attempt to close door at block with no geodata");
					return;
				}
				byte[] block = region[blockIndex];

				int cellX = getCell(geoX);
				int cellY = getCell(geoY);

				int index = 0;
				byte blockType = block[index];
				index++;

				switch (blockType)
				{
				case BLOCKTYPE_COMPLEX:
					index += (cellX << 3) + cellY << 1;

					height = makeShort(block[index + 1], block[index]);

					if (!check_door_z(pos.getZmin(), pos.getZmax(), height))
						break;

					height <<= 1;
					height |= N;
					height |= S;
					height |= W;
					height |= E;

					block[index + 1] = (byte) (height >> 8);
					block[index] = (byte) (height & 0x00ff);
					break;
				case BLOCKTYPE_MULTILEVEL:
					int neededIndex = -1;

					cellX = getCell(geoX);
					cellY = getCell(geoY);
					int offset = (cellX << 3) + cellY;
					while (offset > 0)
					{
						byte lc = block[index];
						index += (lc << 1) + 1;
						offset--;
					}
					byte layers = block[index];
					index++;
					if (layers <= 0 || layers > 125)
						break;
					short temph = Short.MIN_VALUE;
					while (layers > 0)
					{
						height = makeShort(block[index + 1], block[index]);
						height &= 0xfff0;
						height >>= 1;
						int z_diff_last = Math.abs(pos.getZmin() - temph);
						int z_diff_curr = Math.abs(pos.getZmin() - height);
						if (z_diff_last > z_diff_curr)
						{
							temph = height;
							neededIndex = index;
						}
						layers--;
						index += 2;
					}

					if (!check_door_z(pos.getZmin(), pos.getZmax(), temph))
						break;

					temph <<= 1;
					temph |= N;
					temph |= W;
					temph |= S;
					temph |= E;

					block[neededIndex + 1] = (byte) (temph >> 8);
					block[neededIndex] = (byte) (temph & 0x00ff);
					break;
				}
			}
	}

	public void closeDoor(L2Territory pos)
	{
		int minX = pos.getXmin() - GeoConfig.MAP_MIN_X >> 4;
		int maxX = pos.getXmax() - GeoConfig.MAP_MIN_X >> 4;
		int minY = pos.getYmin() - GeoConfig.MAP_MIN_Y >> 4;
		int maxY = pos.getYmax() - GeoConfig.MAP_MIN_Y >> 4;
		short height;

		for (int geoX = minX; geoX <= maxX; geoX++)
			for (int geoY = minY; geoY <= maxY; geoY++)
			{
				if (!check_cell_in_door(geoX, geoY, pos))
					continue;

				int ix = geoX >> 11;
				int iy = geoY >> 11;

				int blockX = getBlock(geoX);
				int blockY = getBlock(geoY);
				int blockIndex = getBlockIndex(blockX, blockY);

				copyBlock(ix, iy, blockIndex);

				byte[][] region = geodata[ix][iy];
				if (region == null)
				{
					_log.warn("GeoEngine: Attempt to close door at block with no geodata");
					return;
				}
				byte[] block = region[blockIndex];

				int cellX = getCell(geoX);
				int cellY = getCell(geoY);

				int index = 0;
				byte blockType = block[index];
				index++;

				switch (blockType)
				{
				case BLOCKTYPE_COMPLEX:
					index += (cellX << 3) + cellY << 1;

					height = makeShort(block[index + 1], block[index]);

					if (!check_door_z(pos.getZmin(), pos.getZmax(), height))
						break;

					height <<= 1;
					height &= 0xfff0;

					block[index + 1] = (byte) (height >> 8);
					block[index] = (byte) (height & 0x00ff);
					break;
				case BLOCKTYPE_MULTILEVEL:
					int neededIndex = -1;

					int offset = (cellX << 3) + cellY;
					while (offset > 0)
					{
						byte lc = block[index];
						index += (lc << 1) + 1;
						offset--;
					}
					byte layers = block[index];
					index++;
					if (layers <= 0 || layers > 125)
						break;
					short temph = Short.MIN_VALUE;
					while (layers > 0)
					{
						height = makeShort(block[index + 1], block[index]);
						height &= 0xfff0;
						height >>= 1;
						int z_diff_last = Math.abs(pos.getZmin() - temph);
						int z_diff_curr = Math.abs(pos.getZmin() - height);
						if (z_diff_last > z_diff_curr)
						{
							temph = height;
							neededIndex = index;
						}
						layers--;
						index += 2;
					}

					if (!check_door_z(pos.getZmin(), pos.getZmax(), temph))
						break;

					temph <<= 1;
					temph &= 0xfff0;

					block[neededIndex + 1] = (byte) (temph >> 8);
					block[neededIndex] = (byte) (temph & 0x00ff);
					break;
				}
			}
	}

	@SuppressWarnings("unused")
	private void applyDoorNSWE(int geoX, int geoY, int z, byte NSWE, boolean open)
	{
		copyBlock(geoX >> 11, geoY >> 11, getBlockIndex(getBlock(geoX), getBlock(geoY)));

		byte[] block = getGeoBlockFromGeoCoords(geoX, geoY);

		if (block == null)
		{
			_log.fatal("Door at null geoBlock. geoX: " + geoX + ", geoY" + geoY);
			return;
		}

		int index = 0;
		byte type = block[index];
		index++;

		byte blockNSWE;
		switch (type)
		{
		case BLOCKTYPE_FLAT:
			_log.fatal("Door at FLAT block");
			return;
		case BLOCKTYPE_COMPLEX:
			int cellX = getCell(geoX);
			int cellY = getCell(geoY);

			index += (cellX << 3) + cellY << 1;

			blockNSWE = block[index];

			if (open)
				blockNSWE |= NSWE;
			else
				// close
				blockNSWE &= (byte) ~NSWE;

			block[index] = blockNSWE;
			return;

		case BLOCKTYPE_MULTILEVEL:
			int neededIndex = -1;

			cellX = getCell(geoX);
			cellY = getCell(geoY);
			int offset = (cellX << 3) + cellY;
			while (offset > 0)
			{
				byte lc = block[index];
				index += (lc << 1) + 1;
				offset--;
			}
			byte layers = block[index];
			index++;
			if (layers <= 0 || layers > 125)
			{
				_log.fatal("GeoEngine: Invalid layer count while openning door");
				return;
			}
			short temph = Short.MIN_VALUE;
			short height;
			while (layers > 0)
			{
				height = makeShort(block[index + 1], block[index]);
				height = (short) (height & 0x0fff0);
				height = (short) (height >> 1); // height / 2
				if ((z - temph) * (z - temph) > (z - height) * (z - height))
				{
					temph = height;
					neededIndex = index;
				}
				layers--;
				index += 2;
			}

			blockNSWE = block[neededIndex];

			if (open)
				blockNSWE |= NSWE;
			else
				// close
				blockNSWE &= (byte) ~NSWE;

			block[neededIndex] = blockNSWE;
		}
	}

	public GeoMove getGeoMove()
	{
		return geoMove;
	}

	/**
	 * @param x
	 * @param y
	 * @param zmin
	 * @param zmax
	 * @return Z betwen zmin and zmax
	 */
	private short nGetSpawnHeight(int geox, int geoy, int zmin, int zmax, int spawnid)
	{
		byte[] block = getGeoBlockFromGeoCoords(geox, geoy);
		if (block == null)
		{
			if (zmin == zmax)
				return (short) zmin;
			return (short) ((zmin + zmax) / 2);
		}

		int index = 0;
		int cellX, cellY;
		short temph = Short.MIN_VALUE;
		// Read current block type: 0-flat,1-complex,2-multilevel
		byte type = block[index];
		index++;

		short height;
		if (type == BLOCKTYPE_FLAT)// flat
		{
			height = makeShort(block[index + 1], block[index]);
			return (short) (height & 0x0fff0);
		}
		else if (type == BLOCKTYPE_COMPLEX)
		{
			cellX = getCell(geox);
			cellY = getCell(geoy);
			index += ((cellX << 3) + cellY) << 1;
			height = makeShort(block[index + 1], block[index]);
			height = (short) (height & 0x0fff0);
			height = (short) (height >> 1); // height / 2
			return height;
		}
		else
		{
			cellX = getCell(geox);
			cellY = getCell(geoy);
			int offset = (cellX << 3) + cellY;
			while (offset > 0)
			{
				byte lc = block[index];
				index += (lc << 1) + 1;
				offset--;
			}
			// Read current block type: 0-flat,1-complex,2-multilevel
			byte layers = block[index];
			index++;
			if (layers <= 0 || layers > 125)
			{
				_log.warn("Broken geofile (case2), - invalid layer count: " + layers + " at: " + geox + " " + geoy);
				return (short) zmin;
			}
			while (layers > 0)
			{
				height = makeShort(block[index + 1], block[index]);
				height = (short) (height & 0x0fff0);
				height = (short) (height >> 1); // height / 2
				if ((zmin - temph) * (zmin - temph) > (zmin - height) * (zmin - height))
					temph = height;
				layers--;
				index += 2;
			}
			if (temph > zmax + 200 || temph < zmin - 200)
			{
				if (_log.isDebugEnabled())
					_log.warn("SpawnHeight Error - Couldnt find correct layer to spawn NPC - GeoData or Spawnlist Bug!: zmin: " + zmin + " zmax: " + zmax
							+ " value: " + temph + " SpawnId: " + spawnid + " at: " + geox + " : " + geoy);
				return (short) zmin;
			}
		}
		if (temph > zmax + 1000 || temph < zmin - 1000)
		{
			if (_log.isDebugEnabled())
				_log.warn("SpawnHeight Error - Spawnlist z value is wrong or GeoData error: zmin: " + zmin + " zmax: " + zmax + " value: " + temph
						+ " SpawnId: " + spawnid + " at: " + geox + " : " + geoy);
			return (short) zmin;
		}
		return temph;
	}

	public short getSpawnHeight(int x, int y, int zmin, int zmax, int spawnid)
	{
		return nGetSpawnHeight((x - GeoConfig.MAP_MIN_X) >> 4, (y - GeoConfig.MAP_MIN_Y) >> 4, zmin, zmax, spawnid);
	}
}
