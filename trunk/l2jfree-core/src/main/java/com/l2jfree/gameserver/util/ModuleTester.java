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
package com.l2jfree.gameserver.util;

import java.io.File;

import com.l2jfree.Config;
import com.l2jfree.gameserver.GameServer;
import com.l2jfree.gameserver.cache.HtmCache;

/**
 * @author NB4L1
 */
public final class ModuleTester
{
	public static void main(String[] args) throws Exception
	{
		GameServer.init();
		Config.load();
		Config.DATAPACK_ROOT = new File("../l2jfree-datapack");
		
		// here comes what you want to test
		//SkillTable.getInstance();
		HtmCache.getInstance();
		
		System.gc();
		System.runFinalization();
		Thread.sleep(1000);
	}
}
