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
package com.l2jfree.geoserver.geodata.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMISocketFactory;

import com.l2jfree.GeoConfig;

/**
 * @Author: Death
 * @Date: 23/11/2007
 * @Time: 10:59:37
 */
public class SocketFactory extends RMISocketFactory
{
	/**
	 * Creates a client socket connected to the specified host and port.
	 *
	 * @param host the host name
	 * @param port the port number
	 * @return a socket connected to the specified host and port.
	 * @throws java.io.IOException if an I/O error occurs during socket creation
	 * @since JDK1.1
	 */
	@Override
	public Socket createSocket(String host, int port) throws IOException
	{
		Socket s = new Socket();
		s.connect(new InetSocketAddress(InetAddress.getByName(host), port));
		return s;
	}

	/**
	 * Create a server socket on the specified port (port 0 indicates
	 * an anonymous port).
	 *
	 * @param port the port number
	 * @return the server socket on the specified port
	 * @throws java.io.IOException if an I/O error occurs during server socket
	 *                             creation
	 * @since JDK1.1
	 */
	@Override
	public ServerSocket createServerSocket(int port) throws IOException
	{
		ServerSocket s = new ServerSocket();

		InetSocketAddress address;

		if(GeoConfig.SERVER_BIND_HOST.equals("*"))
			address = new InetSocketAddress(port);
		else
			address = new InetSocketAddress(InetAddress.getByName(GeoConfig.SERVER_BIND_HOST), port);

		s.bind(address);

		return s;
	}
} 