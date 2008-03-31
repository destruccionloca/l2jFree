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
package net.sf.l2j.gameserver.network.clientpackets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Format: (ch)
 * just a trigger
 * @author  -Wooden-
 * 
 */
public class RequestExFishRanking extends L2GameClientPacket
{
    private static final String _C__D0_1F_REQUESTEXFISHRANKING = "[C] D0:1F RequestExFishRanking";
    private final static Log _log = LogFactory.getLog(RequestExFishRanking.class.getName());
    
    @Override
    protected void readImpl()
    {
        // trigger
    }

    /**
     * @see net.sf.l2j.gameserver.network.clientpackets.ClientBasePacket#runImpl()
     */
    @Override
    protected void runImpl()
    {
        _log.debug("C5: RequestExFishRanking");
    }

    /**
     * @see net.sf.l2j.gameserver.network.BasePacket#getType()
     */
    @Override
    public String getType()
    {
        return _C__D0_1F_REQUESTEXFISHRANKING;
    }
}
