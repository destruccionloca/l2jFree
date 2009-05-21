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
package net.sf.l2j.gameserver.clientpackets;

import net.sf.l2j.gameserver.model.BlockList;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RequestBlock extends L2GameClientPacket
{
    private static final String _C__A0_REQUESTBLOCK = "[C] A0 RequestBlock";
    private final static Log _log = LogFactory.getLog(L2PcInstance.class.getName());

    private final static int BLOCK = 0;
    private final static int UNBLOCK = 1;
    private final static int BLOCKLIST = 2;
    private final static int ALLBLOCK = 3;
    private final static int ALLUNBLOCK = 4;

    private String _name;
    private Integer _type;
    private L2PcInstance _target;

    @Override
    protected void readImpl()
    {
        _type = readD(); //0x00 - block, 0x01 - unblock, 0x03 - allblock, 0x04 - allunblock

        if( _type == BLOCK || _type == UNBLOCK )
        {
            _name = readS();
            _target = L2World.getInstance().getPlayer(_name);
        }
    }

    @Override
    protected void runImpl()
    {
        L2PcInstance activeChar = getClient().getActiveChar();

        if (activeChar == null)
            return;

        SystemMessage sm;

        switch (_type)
        {
            case BLOCK:
            {
                if (_target == null)
                {
                    // Incorrect player name.
                    sm = new SystemMessage(SystemMessageId.FAILED_TO_REGISTER_TO_IGNORE_LIST);
                    activeChar.sendPacket(sm);
                    return;
                }

                if (_target.isGM())
                {
                    // Cannot block a GM character.
                    sm = new SystemMessage(SystemMessageId.YOU_MAY_NOT_IMPOSE_A_BLOCK_ON_GM);
                    activeChar.sendPacket(sm);
                    return;
                }

                BlockList.addToBlockList(activeChar, _target);

                break;
            }
            case UNBLOCK:
            {
                if (BlockList.isInBlockList(activeChar, _target))
                {
                    if (_target == null)
                    {
                        BlockList.removeFromBlockList(activeChar, _target);
                    }
                    else
                        BlockList.removeFromBlockList(activeChar, _target);
                }
                break;
            }
            case BLOCKLIST:
            {
                BlockList.sendListToOwner(activeChar);
                break;
            }
            case ALLBLOCK:
            {
                sm = new SystemMessage(SystemMessageId.MESSAGE_REFUSAL_MODE);
                activeChar.sendPacket(sm);
                BlockList.setBlockAll(activeChar, true);
                break;
            }
            case ALLUNBLOCK:
            {
                sm = new SystemMessage(SystemMessageId.MESSAGE_ACCEPTANCE_MODE);
                activeChar.sendPacket(sm);
                BlockList.setBlockAll(activeChar, false);
                break;
            }
            default:
                _log.info("Unknown 0x0a block type: " + _type);
        }
    }

    @Override
    public String getType()
    {
        return _C__A0_REQUESTBLOCK;
    }
}
