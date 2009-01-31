/**
 * 
 */
package com.l2jfree.gameserver;

import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author noctarius
 */
public class L2JfreeInfo
{
	public static final void showStartupInfo()
	{
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println(" ___       ___");
		System.out.println("/\\_ \\    /'___`\\   __  /'___\\");
		System.out.println("\\//\\ \\  /\\_\\ /\\ \\ /\\_\\/\\ \\__/  _ __    __     __");
		System.out.println("  \\ \\ \\ \\/_/// /__\\/\\ \\ \\ ,__\\/\\`'__\\/'__`\\ /'__`\\");
		System.out.println("   \\_\\ \\_  // /_\\ \\\\ \\ \\ \\ \\_/\\ \\ \\//\\  __//\\  __/");
		System.out.println("   /\\____\\/\\______/_\\ \\ \\ \\_\\  \\ \\_\\\\ \\____\\ \\____\\");
		System.out.println("   \\/____/\\/_____//\\ \\_\\ \\/_/   \\/_/ \\/____/\\/____/");
		System.out.println("                  \\ \\____/");
		System.out.println("                   \\/___/  [starting Version: " + GameServer.getVersionNumber() + "]");
	}

	public static final void versionInfo(L2PcInstance activeChar)
	{
		activeChar.sendMessage(":__.     :_____:_____:_____:_____:_____:_____:");
		activeChar.sendMessage("|    |__|___   |__.     |     __|        |     __|     __|");
		activeChar.sendMessage("|         |   ___|   |     |     __|    ) _|     __|     __|");
		activeChar.sendMessage("|_____|_____|_____|__|    |__|__|_____|_____|");
		activeChar.sendMessage("l2jfree version: "+ GameServer.getVersionNumber() + " license: gpl 3 ");
	}
}
