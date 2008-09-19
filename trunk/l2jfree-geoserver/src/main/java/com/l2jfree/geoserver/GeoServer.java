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

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Calendar;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.GeoConfig;
import com.l2jfree.geoclient.GeoClientInterface;
import com.l2jfree.geoserver.geodata.GeoEngine;
import com.l2jfree.geoserver.geodata.RemoteGeoEngine;
import com.l2jfree.geoserver.geodata.network.SocketFactory;
import com.l2jfree.geoserver.model.L2Territory;
import com.l2jfree.geoserver.model.Location;
import com.l2jfree.geoserver.util.Util;
import com.l2jfree.versionning.Version;

/**
 * @Author: Death
 * @Date: 23/11/2007
 * @Time: 10:57:02
 */
@SuppressWarnings("serial")
public class GeoServer extends UnicastRemoteObject implements GeoServerInterface
{
	private static final Logger													log		= Logger.getLogger(GeoServer.class.getName());
	private static final ConcurrentHashMap<GeoClientInterface, RemoteGeoEngine>	clients	= new ConcurrentHashMap<GeoClientInterface, RemoteGeoEngine>();

	public GeoServer() throws RemoteException
	{
		new Thread(new Runnable()
		{
			public void run()
			{
				while (true)
				{
					try
					{
						Thread.sleep(1000);
					}
					catch (InterruptedException e)
					{
						log.severe("GeoServer: Cleaning Thread interrupted.");
					}

					for (GeoClientInterface client : clients.keySet())
						try
						{
							client.testConnection();
						}
						catch (RemoteException e)
						{
							clients.remove(client);
							log.info("GeoServer: Client disconnected.");
						}
				}
			}
		}).start();
	}

	private RemoteGeoEngine getGeoEngine(GeoClientInterface client) throws RemoteException
	{
		RemoteGeoEngine geo = clients.get(client);
		if (geo != null)
			return geo;
		throw new RemoteException("Null geoEngine for current geoClient!");
	}

	public void addClient(GeoClientInterface client)
	{
		clients.put(client, new RemoteGeoEngine());
	}

	public short getType(GeoClientInterface client, int x, int y) throws RemoteException
	{
		return getGeoEngine(client).getType(x, y);
	}

	public int getHeight(GeoClientInterface client, int x, int y, int z) throws RemoteException
	{
		return getGeoEngine(client).getHeight(x, y, z);
	}

	public boolean canSeeCoord(GeoClientInterface client, int x, int y, int z, int tx, int ty, int tz) throws RemoteException
	{
		return getGeoEngine(client).canSeeCoord(x, y, z, tx, ty, tz);
	}

	public boolean canMoveToCoord(GeoClientInterface client, int x, int y, int z, int tx, int ty, int tz) throws RemoteException
	{
		return getGeoEngine(client).canMoveToCoord(x, y, z, tx, ty, tz);
	}

	public boolean canMoveToCoordWithCollision(GeoClientInterface client, int x, int y, int z, int tx, int ty, int tz) throws RemoteException
	{
		return getGeoEngine(client).canMoveToTargetWithCollision(x, y, z, tx, ty, tz);
	}

	public short getNSWE(GeoClientInterface client, int x, int y, int z) throws RemoteException
	{
		return getGeoEngine(client).getNSWE(x, y, z);
	}

	public Location moveCheck(GeoClientInterface client, int x, int y, int z, int tx, int ty) throws RemoteException
	{
		return getGeoEngine(client).moveCheck(x, y, z, tx, ty);
	}

	public Location moveCheckForAI(GeoClientInterface client, Location loc1, Location loc2) throws RemoteException
	{
		return getGeoEngine(client).moveCheckForAI(loc1, loc2);
	}

	public boolean canSeeTarget(GeoClientInterface client, int x, int y, int z, int tx, int ty, int tz) throws RemoteException
	{
		return getGeoEngine(client).canSeeTarget(x, y, z, tx, ty, tz);
	}

	public boolean canMoveToTarget(GeoClientInterface client, int x, int y, int z, int tx, int ty, int tz) throws RemoteException
	{
		return getGeoEngine(client).canMoveToCoord(x, y, z, tx, ty, tz);
	}

	public boolean canMoveToTargetWithCollision(GeoClientInterface client, int x, int y, int z, int tx, int ty, int tz) throws RemoteException
	{
		return getGeoEngine(client).canMoveToTargetWithCollision(x, y, z, tx, ty, tz);
	}

	public Vector<Location> checkMovement(GeoClientInterface client, int x, int y, int z, Location target) throws RemoteException
	{
		return getGeoEngine(client).getGeoMove().checkMovement(x, y, z, target);
	}

	public void closeDoor(GeoClientInterface client, L2Territory pos) throws RemoteException
	{
		getGeoEngine(client).closeDoor(pos);
	}

	public void openDoor(GeoClientInterface client, L2Territory pos) throws RemoteException
	{
		getGeoEngine(client).openDoor(pos);
	}

	public short getSpawnHeight(GeoClientInterface client, int x, int y, int zmin, int zmax, int spawnid) throws RemoteException
	{
		return getGeoEngine(client).getSpawnHeight(x, y, zmin, zmax, spawnid);
	}

	private static final Log		_log			= LogFactory.getLog(GeoServer.class);
	private static final Calendar	_serverStarted	= Calendar.getInstance();
	public static final Version		version			= new Version();

	public static String getVersionNumber()
	{
		return version.getVersionNumber();
	}

	public static void printMemUsage()
	{
		Util.printSection("Memory");
		for (String line : Util.getMemUsage())
			_log.info(line);
	}

	public static Calendar getStartedTime()
	{
		return _serverStarted;
	}

	public static void main(String[] args) throws Throwable
	{
		long serverLoadStart = System.currentTimeMillis();
		L2JfreeInfo.showStartupInfo();
		File f = new File("geoserver.policy");
		if (f.exists() && f.isFile())
		{
			System.setProperty("java.security.policy", "geoserver.policy");
			System.setSecurityManager(new SecurityManager());
			log.info("GeoServer: geoserver.policy initialized.");
		}
		else
			log.info("GeoServer: geoserver.policy missing. SecurityManager setup ignored.");

		try
		{
			GeoConfig.load();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			log.severe("Can't init geodata config.");
			System.exit(0);
		}

		try
		{
			GeoEngine.loadGeo();
			Registry r = LocateRegistry.createRegistry(GeoConfig.PORT, new SocketFactory(), new SocketFactory());
			GeoServer server = new GeoServer();
			r.bind("geoServer", server);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(0);
		}
		System.gc();
		Util.printSection("l2jfree");
		version.loadInformation(GeoServer.class);
		_log.info("Revision: " + version.getVersionNumber());
		_log.info("Compiler version: " + version.getBuildJdk());
		_log.info("Operating System: " + Util.getOSName() + " " + Util.getOSVersion() + " " + Util.getOSArch());
		_log.info("Available CPUs: " + Util.getAvailableProcessors());
		printMemUsage();
		_log.info("Server Loaded in " + ((System.currentTimeMillis() - serverLoadStart) / 1000) + " seconds");
		Util.printSection("GeoServerLog");
		log.info("GeoServer: Running at " + GeoConfig.SERVER_BIND_HOST + ":" + GeoConfig.PORT + "");
	}
}