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

import org.mmocore.network.SelectorThread;

public final class CoreInfo
{
	private CoreInfo()
	{
	}

	private static final Version	coreVersion		= new Version(GameServer.class);
	private static final Version	commonsVersion	= new Version(L2Config.class);
	private static final Version	mmocoreVersion	= new Version(SelectorThread.class);

	public static void showStartupInfo()
	{
		System.out.println("                      YBBB4BBBBBBBBBBBBB8BBBBBBBBBBBBBBBBX                     ");
		System.out.println("                    iBBBBB0N0PNkPkPSkFk15FfFUlUlUlUlU16BBBBj                   ");
		System.out.println("                  .BBBBBr                              ,BBBBBi                 ");
		System.out.println("                 PBBBBk      YyiririrrrrrrrrrrrrryY      23BBBB.               ");
		System.out.println("               YBBBBM    .   BBBBBBBBBBB4BBBBBBBBBB   i    SBBBBP              ");
		System.out.println("             :BBBBB:    BBM                          rBB;    BBBBBP            ");
		System.out.println("            MBBBBY    2BBBB   ,BEEPNqE   BOZGEOOB.   :BBBB.   iBBBBB:          ");
		System.out.println("          FBBBBN    ;BBBB  X,  BBBBBBB   BBBBBBBB   O: vBBBE    JBBBBM         ");
		System.out.println("        rBBBBB.   :BBBB: iBBB                      5BB8  qBBB     ZBBBBU       ");
		System.out.println("      ,BBBBB;     .BBL  BBB8    rBBBBBBBBBBBBBB     vBBBYBB        .BBBBBr     ");
		System.out.println("     GBBBBU           GBBB  JB.  MBBBBBBBBBBBB2   BB   BB.    .BB    iBBBBB.   ");
		System.out.println("   JBBBBM    :BBBB. XBBB; .BBBB                  JBBBLB.    .BOBBBU    5BBBBN  ");
		System.out.println(" iBBBBB:    MBBB: ;BBB2  MBBBY                    ,BBB     .BG  BBBBr    MBBBBP");
		System.out.println("eBBBB;   .MBBBU  BBBB  LBBBq     .7XBBBBBBBBFr      ;    . 0BBB7 :BBBBr   :BBBL");
		System.out.println("2BBB     LBBB..BBBB, ,BBBB.   .kBBBBBBBBBBBBBBBBl      vBBN  MBBB: qBBP     BBB");
		System.out.println("vBBB          BBBl  BBBB7   ,BBBBBBBBBBBBBBBBBBBBBG.   :BBBBY iBBB:         BB3");
		System.out.println("LBBB   jBBr        BBB0    EBBBBBBBBBBBBBBBBBBBBBBBBl    :BBBi       jBB    BBB");
		System.out.println("iBBB   JBBB BBN     ..    BBBBBBBBBBBBBBBBBBBBBBBBBBBB          .qB.:BBB    BB3");
		System.out.println("YBBB   7BBG BBB, E7.     BBBBBBBBBBBBBBBBBBBBBBBBBBBBBB     iSL BBB..BBB    BBB");
		System.out.println("lBBB   rBBZ BBB  BBB    BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBP   UBBB MBB  BBB    BBT");
		System.out.println("YBBB   7BBZ BBB  BBB   :BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB   LBB8 MBB  BBB    BBB");
		System.out.println("lBBB   rBBM BBB .BBB   0BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBL  2BBB OBB. BBB    BBB");
		System.out.println("JBBB   rBBB            BBB           l2jfree          BBE       MBB  BBB    BBB");
		System.out.println("UBBB   rBBB 7F0  XNS   BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB2BP       8BB. BBB    BBB");
		System.out.println("jBBB   rBBM BBB  BBB   kBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB7  UBBB 8BB  BBB    BBB");
		System.out.println("UBBB   rBBM BBB  BBB    BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB   uBBM GBB. BBB    BBB");
		System.out.println("uBBB   7BBM BBB  BBB    PBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB7   rBBB MBB. BBB    BBB");
		System.out.println("2BBB   7BBB BBB  r.      BBBBBBBBBBBBBBBBBBBBBBBBBBBBBq      :, BBB: BBB    BBB");
		System.out.println("uBBB   UBBB U7     .i     8BBBBBBBBBBBBBBBBBBBBBBBBBBl    ii     LO :BBB    BBB");
		System.out.println("lBBB   ,0:     :  BBBB:    7BBBBBBBBBBBBBBBBBBBBBBBB:   .BBBB  :     rqG    BBB");
		System.out.println("2BBB         MBBBr YBBBM     2BBBBBBBBBBBBBBBBBBBBv    8BBBJ .BBBZ          BBB");
		System.out.println("GBBB    :BBBJ :BBBB  GBBBj     i8BBBBBBBBBBBBBB0,    lBBBZ  5BBBi YBBBi     BBB");
		System.out.println("YBBBBU   JBBBB  YBBB.  BBBBi      .ruZMBBM0Ji      iBBBB  rBBBu  BBBBY    7BBBB");
		System.out.println(" .BBBBBi   YBBBq  B     rBBBB.                   iBBBB: .BBBZ  GBBBJ    ,BBBBB:");
		System.out.println("   7BBBBB.   ZBB.    BBB  lBBB                   BBBY  OBBB. LBBBE     8BBBBU  ");
		System.out.println("     2BBBBE   .B   . 2BBBL  B7  :BBBBB   BBBBB:  .k  2BBBi :BBBB.    5BBBBZ    ");
		System.out.println("       MBBBBY     BBB  ZBBB:    XBBBBB   BBBBBP    7BBBU  BBBBi    rBBBBB.     ");
		System.out.println("        :BBBBBi   8BBBY .BBB                      .BBB  NBBBY    :BBBBBr       ");
		System.out.println("          7BBBBB    BBBB, u,  YBBBBBBBBBBBBBBBBBY  ,r JBBBE     MBBBBu         ");
		System.out.println("            XBBBBP   :BBBB    YBBBBBBBBBBBBBBBBBY    BBBB.    2BBBBG           ");
		System.out.println("              MBBBBL   UBB                           BBi    7BBBBB,            ");
		System.out.println("               :BBBBB:      BBBBBBBBBB   BBBBBBBBB7       ,BBBBBi              ");
		System.out.println("                 YBBBBM     MBBBBBBBBB   BBBBBBBBBM      MBBBBu                ");
		System.out.println("                   kBBBBX                              5BBBB0                  ");
		System.out.println("                     BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB.                   ");
		System.out.println("                      .RSBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB,                     ");
		System.out.println("Namaste! [starting version: " + coreVersion.getVersionNumber() + "]");
	}

	public static final void versionInfo(L2PcInstance activeChar)
	{
		activeChar.sendMessage("codename 'the arrow' - server version: " + getVersionInfo());
	}

	public static String getVersionInfo()
	{
		return getVersionInfo(coreVersion);
	}

	public static String[] getFullVersionInfo()
	{
		return new String[] { "l2jfree-core :    " + getFullVersionInfo(coreVersion), "l2j-commons  :    " + getFullVersionInfo(commonsVersion), "l2j-mmocore  :    " + getFullVersionInfo(mmocoreVersion) };
	}

	private static String getVersionInfo(Version version)
	{
		return String.format("%-6s [ %4s ]", version.getVersionNumber(), version.getRevisionNumber());
	}

	private static String getFullVersionInfo(Version version)
	{
		return getVersionInfo(version) + " - " + version.getBuildJdk();
	}
}
