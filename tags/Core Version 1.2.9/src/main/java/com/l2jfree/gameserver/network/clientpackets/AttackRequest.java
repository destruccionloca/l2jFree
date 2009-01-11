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

import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;

/**
 * This class ...
 * 
 * @version $Revision: 1.7.2.1.2.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class AttackRequest extends L2GameClientPacket
{
    // cddddc
    private int _objectId;
    @SuppressWarnings("unused")
    private int _originX;
    @SuppressWarnings("unused")
    private int _originY;
    @SuppressWarnings("unused")
    private int _originZ;
    @SuppressWarnings("unused")
    private int _attackId;

    private static final String _C__0A_ATTACKREQUEST = "[C] 0A AttackRequest";
    
    @Override
    protected void readImpl()
    {
        _objectId  = readD();
        _originX  = readD();
        _originY  = readD();
        _originZ  = readD();
        _attackId  = readC();    // 0 for simple click   1 for shift-click
    }

    @Override
    protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		L2Object target = null;

		// Get object from target
		if (activeChar.getTargetId() == _objectId)
			target = activeChar.getTarget();

		// Get object from world
		if (target == null)
		{
			target = L2World.getInstance().findObject(_objectId);
			//_log.warn("Player "+activeChar.getName()+" attacked object from outside of his knownlist.");
		}

		if (target == null)
			return;

		if (activeChar.getTarget() != target)
		{
			target.onAction(activeChar);
		}
		else
		{
			if((target.getObjectId() != activeChar.getObjectId())
					&& activeChar.getPrivateStoreType() ==0 
					&& activeChar.getActiveRequester() ==null)
			{
				//_log.info("Starting ForcedAttack");
				target.onForcedAttack(activeChar);
				//_log.info("Ending ForcedAttack");
			} 
			else
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__0A_ATTACKREQUEST;
	}
}
