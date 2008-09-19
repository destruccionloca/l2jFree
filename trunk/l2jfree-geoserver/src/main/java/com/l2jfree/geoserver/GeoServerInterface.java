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
package com.l2jfree.geoserver;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Vector;

import com.l2jfree.geoclient.GeoClientInterface;
import com.l2jfree.geoserver.model.L2Territory;
import com.l2jfree.geoserver.model.Location;

/**
 * author: Death
 */
public interface GeoServerInterface extends Remote
{
	void addClient(GeoClientInterface client) throws RemoteException;

	short getType(GeoClientInterface client, int x, int y) throws RemoteException;

	int getHeight(GeoClientInterface client, int x, int y, int z) throws RemoteException;

	boolean canSeeCoord(GeoClientInterface client, int x, int y, int z, int tx, int ty, int tz) throws RemoteException;

	boolean canMoveToCoord(GeoClientInterface client, int x, int y, int z, int tx, int ty, int tz) throws RemoteException;

	boolean canMoveToCoordWithCollision(GeoClientInterface client, int x, int y, int z, int tx, int ty, int tz) throws RemoteException;

	short getNSWE(GeoClientInterface client, int x, int y, int z) throws RemoteException;

	Location moveCheck(GeoClientInterface client, int x, int y, int z, int tx, int ty) throws RemoteException;

	Location moveCheckForAI(GeoClientInterface client, Location loc1, Location loc2) throws RemoteException;

	boolean canSeeTarget(GeoClientInterface client, int x, int y, int z, int tx, int ty, int tz) throws RemoteException;

	boolean canMoveToTarget(GeoClientInterface client, int x, int y, int z, int tx, int ty, int tz) throws RemoteException;

	boolean canMoveToTargetWithCollision(GeoClientInterface client, int x, int y, int z, int tx, int ty, int tz) throws RemoteException;

	Vector<Location> checkMovement(GeoClientInterface client, int x, int y, int z, Location target) throws RemoteException;

	void openDoor(GeoClientInterface client, L2Territory pos) throws RemoteException;

	void closeDoor(GeoClientInterface client, L2Territory pos) throws RemoteException;
	
	short getSpawnHeight(GeoClientInterface client, int x, int y, int zmin, int zmax, int spawnid) throws RemoteException;
}