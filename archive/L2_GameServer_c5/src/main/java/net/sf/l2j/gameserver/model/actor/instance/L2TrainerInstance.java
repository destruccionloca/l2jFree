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

import net.sf.l2j.gameserver.templates.L2NpcTemplate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 * This class ...
 * 
 * @version $Revision: 1.5.4.8 $ $Date: 2005/04/02 15:57:52 $
 */
public final class L2TrainerInstance extends L2FolkInstance
{
    private final static Log _log = LogFactory.getLog(L2TrainerInstance.class.getName());
    /**
     * @param template
     */
    public L2TrainerInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }

    /**
     * this is called when a player interacts with this NPC
     * @param player
     */
    public void onAction(L2PcInstance player)
    {
        if (_log.isDebugEnabled()) _log.debug("Trainer activated");
        player.setLastFolkNPC(this);
        super.onAction(player);
    }

    public String getHtmlPath(int npcId, int val)
    {
        String pom = "";
        if (val == 0)
        {
            pom = "" + npcId;
        }
        else
        {
            pom = npcId + "-" + val;
        }

        return "data/html/trainer/" + pom + ".htm";
    }

}
