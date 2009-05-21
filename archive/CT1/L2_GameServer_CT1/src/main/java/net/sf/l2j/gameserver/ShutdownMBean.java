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
package net.sf.l2j.gameserver;

/**
 * interface for jmx administration This interface gives the ability to shutdown or restart the gameserver
 */
public interface ShutdownMBean
{
	/**
	 * This functions starts a shutdown countdown
	 * 
	 * @param seconds
	 *            seconds until shutdown
	 */
	public void processShutdown(int seconds);
	
	/**
	 * This functions starts a restart countdown
	 * 
	 * @param seconds
	 *            seconds until restart
	 */
	public void processRestart(int seconds);
}
