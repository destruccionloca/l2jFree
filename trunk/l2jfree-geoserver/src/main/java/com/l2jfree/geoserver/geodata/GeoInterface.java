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

import com.l2jfree.geoserver.model.L2Territory;
import com.l2jfree.geoserver.model.Location;

public interface GeoInterface
{
	public GeoMove getGeoMove();

	public short getType(int x, int y);

	public int getHeight(int x, int y, int z);

	public boolean canSeeCoord(int x, int y, int z, int tx, int ty, int tz);

	public boolean canMoveToCoord(int x, int y, int z, int tx, int ty, int tz);

	public boolean canMoveToCoordWithCollision(int x, int y, int z, int tx, int ty, int tz);

	public short getNSWE(int x, int y, int z);

	public Location moveCheck(int x, int y, int z, int tx, int ty);

	public Location moveCheckForAI(Location loc1, Location loc2);

	public Location MoveCheckForPathFind(int x, int y, int z, int tx, int ty);

	public boolean canSeeTarget(int x, int y, int z, int tx, int ty, int tz);

	public boolean canMoveToTarget(int x, int y, int z, int tx, int ty, int tz);

	public boolean canMoveToTargetWithCollision(int x, int y, int z, int tx, int ty, int tz);

	public short getSpawnHeight(int x, int y, int zmin, int zmax, int spawnid);

	public short NgetNSWE(int geoX, int geoY, int z);

	public int NgetHeight(int geoX, int geoY, int z);

	public void openDoor(L2Territory pos);

	public void closeDoor(L2Territory pos);
}