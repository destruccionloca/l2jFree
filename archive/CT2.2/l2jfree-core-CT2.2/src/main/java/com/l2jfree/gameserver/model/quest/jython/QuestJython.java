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
package com.l2jfree.gameserver.model.quest.jython;


import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;

import com.l2jfree.Config;
import com.l2jfree.gameserver.model.quest.Quest;

public abstract class QuestJython extends Quest
{
	private static BSFManager	_bsf;

	/**
	 * Initialize the engine for scripts of quests, luxury shops and blacksmith
	 */
	public static void init()
	{
		try
		{
			// Initialize the engine for loading Jython scripts
			_bsf = new BSFManager();
			// Execution of all the scripts placed in data/scripts
			// inside the DataPack directory

			String dataPackDirForwardSlashes = Config.DATAPACK_ROOT.getPath().replaceAll("\\\\", "/");
			String loadingScript = "import sys;" + "sys.path.insert(0,'" + dataPackDirForwardSlashes + "');" + "import data";

			_bsf.exec("jython", "quest", 0, 0, loadingScript);
		}
		catch (BSFException e)
		{
			_log.error(e.getMessage(), e);
		}
	}

	public static boolean reloadQuest(String questFolder)
	{
		try
		{
			_bsf.exec("jython", "quest", 0, 0, "reload(data.scripts." + questFolder + ");");
			return true;
		}
		catch (Exception e)
		{
			//_log.warn("Reload Failed", e);
		}
		return false;
	}

	/**
	 * Constructor used in jython files.
	 * @param questId : int designating the ID of the quest
	 * @param name : String designating the name of the quest
	 * @param descr : String designating the description of the quest
	 */
	public QuestJython(int questId, String name, String descr)
	{
		super(questId, name, descr);
	}
}