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
package com.l2jfree;

/**
 * @author NB4L1
 */
public abstract class DatabaseInstaller
{
	public static void main(String[] args)
	{
		final DatabaseInstaller installer;
		
		if (System.getProperty("os.name").contains("Windows"))
			installer = new WindowsInstaller();
		else if (System.getProperty("os.name").contains("Linux"))
			installer = new LinuxInstaller();
		else
			throw new IllegalStateException();
		
		installer.printMenu();
	}
	
	private DatabaseInstaller()
	{
	}
	
	private final void printMenu()
	{
	}
	
	private static final class WindowsInstaller extends DatabaseInstaller
	{
		private WindowsInstaller()
		{
		}
	}
	
	private static final class LinuxInstaller extends DatabaseInstaller
	{
		private LinuxInstaller()
		{
		}
	}
}
