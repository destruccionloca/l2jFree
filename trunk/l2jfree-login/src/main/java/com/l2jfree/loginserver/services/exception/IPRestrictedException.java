/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package com.l2jfree.loginserver.services.exception;

/**
 * This class is used to notify if someone connects from an IP contained in banned_ip.cfg
 * and the ban duration is not set.
 * @author Savormix
 */
public final class IPRestrictedException extends Exception {

	private static final long serialVersionUID = -4182476422930886408L;
	private final long duration;

	public IPRestrictedException() {
		super();
		duration = -1;
	}

	public IPRestrictedException(String ipAddr) {
		super("IP: " + ipAddr + " is defined as permanently banned.");
		duration = -1;
	}

	public IPRestrictedException(long endTime) {
		super();
		duration = endTime - System.currentTimeMillis();
	}

	public IPRestrictedException(String ipAddr, long endTime) {
		super("IP: " + ipAddr + " is defined as temporarily banned.");
		duration = endTime - System.currentTimeMillis();
	}

	public final int getMinutesLeft() {
		if (duration != -1) {
			return (int)(duration/1000/60);
		}
		else
			return -1;
	}
}
