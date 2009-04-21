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
package com.l2jfree.gameserver.network.clientpackets;

import com.l2jfree.gameserver.instancemanager.RecommendationManager;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;

public class RequestEvaluate extends L2GameClientPacket
{
    private static final String _C__B9_REQUESTEVALUATE = "[C] B9 RequestEvaluate";

    //private final static Log _log = LogFactory.getLog(RequestEvaluate.class.getName());

    @SuppressWarnings("unused")
    private int _targetId;

    @Override
    protected void readImpl()
    {
        _targetId = readD();
    }

    @Override
    protected void runImpl()
    {
    	L2PcInstance activeChar = getClient().getActiveChar();
    	L2Object target = activeChar.getTarget();
    	if (target instanceof L2PcInstance)
    		RecommendationManager.getInstance().recommend(activeChar, (L2PcInstance) target);
    	else
    		activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
    }
    
    /* (non-Javadoc)
     * @see com.l2jfree.gameserver.clientpackets.ClientBasePacket#getType()
     */
    @Override
    public String getType()
    {
        return _C__B9_REQUESTEVALUATE;
    }
}