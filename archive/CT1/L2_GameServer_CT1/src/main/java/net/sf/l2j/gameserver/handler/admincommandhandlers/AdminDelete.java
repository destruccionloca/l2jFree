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
package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.instancemanager.GrandBossSpawnManager;
import net.sf.l2j.gameserver.instancemanager.RaidBossSpawnManager;
import net.sf.l2j.gameserver.instancemanager.grandbosses.FrintezzaManager;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

/**
 * This class handles following admin commands: - delete = deletes target
 * 
 * @version $Revision: 1.2.2.1.2.4 $ $Date: 2005/04/11 10:05:56 $
 */
public class AdminDelete implements IAdminCommandHandler
{
    //private final static Log _log = LogFactory.getLog(AdminDelete.class.getName());

    private static final String[] ADMIN_COMMANDS = {"admin_delete" , "admin_frintezza" };

    private static final int REQUIRED_LEVEL = Config.GM_NPC_EDIT;

    public boolean useAdminCommand(String command, L2PcInstance activeChar)
    {
        if (!Config.ALT_PRIVILEGES_ADMIN)
        {
            if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM())) return false;
        }

        if (command.equals("admin_delete")) handleDelete(activeChar);

        else if (command.equals("admin_frintezza"))
        {
			activeChar.sendMessage("Frintezza was Initialized.");
        	FrintezzaManager.getInstance().setScarletSpawnTask();
        }
        
        return true;
        
    }

    public String[] getAdminCommandList()
    {
        return ADMIN_COMMANDS;
    }

    private boolean checkLevel(int level)
    {
        return (level >= REQUIRED_LEVEL);
    }

    private void handleDelete(L2PcInstance activeChar)
    {
        L2Object obj = activeChar.getTarget();
        if ((obj != null) && (obj instanceof L2NpcInstance))
        {
            L2NpcInstance target = (L2NpcInstance) obj;
            target.deleteMe();

            L2Spawn spawn = target.getSpawn();
            if (spawn != null)
            {
                spawn.stopRespawn();

                if (RaidBossSpawnManager.getInstance().isDefined(spawn.getNpcId()))
                    RaidBossSpawnManager.getInstance().deleteSpawn(spawn, true);
                else if (GrandBossSpawnManager.getInstance().isDefined(spawn.getNpcId()))
                    GrandBossSpawnManager.getInstance().deleteSpawn(spawn, true);
                else
                    SpawnTable.getInstance().deleteSpawn(spawn, true);
            }

            activeChar.sendMessage("Deleted " + target.getName() + " from " + target.getObjectId() + ".");
        }
        else
        {
            activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
        }
    }
}
