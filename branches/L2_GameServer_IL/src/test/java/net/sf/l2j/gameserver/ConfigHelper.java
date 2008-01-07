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
package net.sf.l2j.gameserver;

import net.sf.l2j.Config;
import net.sf.l2j.Config.IdFactoryType;
import net.sf.l2j.Config.ObjectMapType;
import net.sf.l2j.Config.ObjectSetType;

/**
 * This class is used to help creating a basic configuration by setting some default value
 *
 */
public class ConfigHelper
{
    /**
     * Default configuration
     */
    public static void configure ()
    {
        // Set a increment id factory for test purpose
        Config.IDFACTORY_TYPE = IdFactoryType.Increment;
        
        Config.MAP_TYPE = ObjectMapType.WorldObjectMap;
        Config.SET_TYPE = ObjectSetType.WorldObjectSet;
        
        Config.THREAD_P_EFFECTS                = 6;
        Config.THREAD_P_GENERAL                = 15;
        Config.GENERAL_PACKET_THREAD_CORE_SIZE = 4;
        Config.IO_PACKET_THREAD_CORE_SIZE      = 2;
        Config.GENERAL_THREAD_CORE_SIZE        = 4;
        Config.AI_MAX_THREAD                   = 10;
        
    }
}
