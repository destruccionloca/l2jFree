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
package net.sf.l2j.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import net.sf.l2j.gameserver.datatables.DoorTable;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * This class ...
 * 
 * @version $Revision$ $Date$
 */
public class L2DooropenInstance extends L2FolkInstance
{
    /**
     * @param template
     */
    public L2DooropenInstance(int objectID, L2NpcTemplate template)
    {
        super(objectID, template);
    }

    public void onBypassFeedback(L2PcInstance player, String command)
    {
        if (command.startsWith("Chat"))
        {
            showMessageWindow(player);
            return;
        }
        else if (command.startsWith("open_doors"))
        {
            DoorTable doorTable = DoorTable.getInstance();
            StringTokenizer st = new StringTokenizer(command.substring(10), ", ");

            while (st.hasMoreTokens())
            {
                int _doorid = Integer.parseInt(st.nextToken());
                doorTable.getDoor(_doorid).openMe();
            }
            return;

        }
        super.onBypassFeedback(player, command);
    }

    /**
     * this is called when a player interacts with this NPC
     * @param player
     */
    public void onAction(L2PcInstance player)
    {
        player.sendPacket(new ActionFailed());
        player.setTarget(this);
        player.sendPacket(new MyTargetSelected(getObjectId(), -15));

        if (isInsideRadius(player, INTERACTION_DISTANCE, false, false))
            showMessageWindow(player);
    }

    public void showMessageWindow(L2PcInstance player)
    {
        //player.sendPacket(new ActionFailed());
        String filename = "data/html/dooropen/" + getTemplate().npcId + ".htm";

        NpcHtmlMessage html = new NpcHtmlMessage(1);
        html.setFile(filename);

        html.replace("%objectId%", String.valueOf(getObjectId()));
        player.sendPacket(html);
    }
}
