/**
 * 
 */
package com.l2jfree.gameserver;

/**
 * @author noctarius
 */
public class L2JfreeInfo {
	public static final void showStartupInfo() {
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
		System.out.println("                   \\/___/  starting Version: "+GameServer.getVersionNumber());
	}
}
