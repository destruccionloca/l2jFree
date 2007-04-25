/* This program is free software; you can redistribute it and/or modify
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
package net.sf.l2j.gameserver.instancemanager;

import java.io.File;

import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.jython.QuestJython;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class QuestManager
{
    protected static Log _log = LogFactory.getLog(QuestManager.class.getName());

    // =========================================================
    private static QuestManager _Instance;
    public static final QuestManager getInstance()
    {
        if (_Instance == null)
        {
            File jscript;
            
            if ( _log.isDebugEnabled())_log.debug("Initializing QuestManager");
            _Instance = new QuestManager();
            
            jscript = new File(Config.DATAPACK_ROOT, "data/jscript");
            for (File file : jscript.listFiles())
            {
                if (file.isFile() && file.getName().endsWith("$py.class"))
                    file.delete();
            }
            
            if (!Config.ALT_DEV_NO_QUESTS)
               _Instance.load();
            else
                _log.info("QuestManager Disabled");
        }
        return _Instance;
    }
    // =========================================================

    
    // =========================================================
    // Data Field
    private FastList<Quest> _Quests;
    
    // =========================================================
    // Constructor
    public QuestManager()
    {
    }

    // =========================================================
    // Method - Public
    // NOT WORKING CORRECTLY BECAUSE BSFMANAGER DOEN'T UNLOAD JYTHON SCRIPT
    // NEED TO FIND THE SOLUTION BEFORE THIS WILL WORK CORRECLY
    public final void reload()
    {
    	/* Re-add later just incase problem exist now
        this.getQuests().clear();
        this.load();
        */
    }

    // =========================================================
    // Method - Private
    private final void load()
    {
        QuestJython.init();
        _log.info("Loaded: " + getQuests().size() + " quests");
    }

    // =========================================================
    // Property - Public
    public final Quest getQuest(String name)
    {
        int index = getQuestIndex(name);
        if (index >= 0) return getQuests().get(index);
        return null;
    }

    public final Quest getQuest(int questId)
    {
        int index = getQuestIndex(questId);
        if (index >= 0) return getQuests().get(index);
        return null;
    }
    
    public final int getQuestIndex(String name)
    {
        Quest quest;
        for (int i = 0; i < getQuests().size(); i++)
        {
            quest = getQuests().get(i);
            if (quest != null && quest.getName().equalsIgnoreCase(name)) return i;
        }
        return -1;
    }
    
    public final int getQuestIndex(int questId)
    {
        Quest quest;
        for (int i = 0; i < getQuests().size(); i++)
        {
            quest = getQuests().get(i);
            if (quest != null && quest.getQuestIntId() == questId) return i;
        }
        return -1;
    }

    public final FastList<Quest> getQuests()
    {
        if (_Quests == null) _Quests = new FastList<Quest>();
        return _Quests;
    }
}
