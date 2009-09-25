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
package com.l2jfree.gameserver;

import java.util.Date;

import org.mmocore.network.SelectorThread;

import com.l2jfree.L2Config;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.clientpackets.EnterWorld.GameDataQueue;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.versionning.Version;

/**
 * @author noctarius
 */
public final class CoreInfo
{
	private CoreInfo()
	{
	}
	
	private static final Version coreVersion = new Version(GameServer.class);
	private static final Version commonsVersion = new Version(L2Config.class);
	private static final Version mmocoreVersion = new Version(SelectorThread.class);
	
	public static void showStartupInfo()
	{
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println(" ___       ___           ___");
		System.out.println("/\\_ \\    /'___`\\   __  /'___\\");
		System.out.println("\\//\\ \\  /\\_\\ /\\ \\ /\\_\\/\\ \\__/  _ __    __     __");
		System.out.println("  \\ \\ \\ \\/_/// /__\\/\\ \\ \\ ,__\\/\\`'__\\/'__`\\ /'__`\\");
		System.out.println("   \\_\\ \\_  // /_\\ \\\\ \\ \\ \\ \\_/\\ \\ \\//\\  __//\\  __/");
		System.out.println("   /\\____\\/\\______/_\\ \\ \\ \\_\\  \\ \\_\\\\ \\____\\ \\____\\");
		System.out.println("   \\/____/\\/_____//\\ \\_\\ \\/_/   \\/_/ \\/____/\\/____/");
		System.out.println("                  \\ \\____/");
		System.out.println("                   \\/___/  [starting version: " + coreVersion.getVersionNumber() + "]");
	}
	
	public static final void versionInfo(L2PcInstance activeChar)
	{
		activeChar.sendMessage(":__.     :_____:_____:_____:_____:_____:_____:");
		activeChar.sendMessage("|    |__|___   |__.     |     __|        |     __|     __|");
		activeChar.sendMessage("|         |   ___|   |     |     __|    ) _|     __|     __|");
		activeChar.sendMessage("|_____|_____|_____|__|    |__|__|_____|_____|");
		activeChar.sendMessage("l2jfree " + getVersionInfo() + ", gpl 3 license");
	}
	
	public static final void versionInfo(GameDataQueue gdq)
	{
		gdq.add(SystemMessage.sendString(":__.     :_____:_____:_____:_____:_____:_____:"));
		gdq.add(SystemMessage.sendString("|    |__|___   |__.     |     __|        |     __|     __|"));
		gdq.add(SystemMessage.sendString("|         |   ___|   |     |     __|    ) _|     __|     __|"));
		gdq.add(SystemMessage.sendString("|_____|_____|_____|__|    |__|__|_____|_____|"));
		gdq.add(SystemMessage.sendString("l2jfree " + getVersionInfo() + ", gpl 3 license"));
	}
	
	public static String getVersionInfo()
	{
		return getVersionInfo(coreVersion);
	}
	
	public static String[] getFullVersionInfo()
	{
		return new String[] {
			"l2jfree-core :    " + getFullVersionInfo(coreVersion),
			"l2j-commons  :    " + getFullVersionInfo(commonsVersion),
			"l2j-mmocore  :    " + getFullVersionInfo(mmocoreVersion) };
	}

	private static String getVersionInfo(Version version)
	{
		return String.format("%-6s [ %4s ]", version.getVersionNumber(), version.getRevisionNumber());
	}
	
	private static String getFullVersionInfo(Version version)
	{
		return getVersionInfo(version) + " - " + version.getBuildJdk() + " - " + new Date(version.getBuildTime());
	}
}
