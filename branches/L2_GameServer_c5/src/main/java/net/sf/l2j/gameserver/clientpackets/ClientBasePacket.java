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
package net.sf.l2j.gameserver.clientpackets;

import java.nio.ByteBuffer;

import javolution.text.TextBuilder;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.TaskPriority;
import net.sf.l2j.gameserver.exception.L2JFunctionnalException;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.BasePacket;
import net.sf.l2j.gameserver.serverpackets.LeaveWorld;
import net.sf.l2j.gameserver.serverpackets.ServerBasePacket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class ...
 * 
 * @version $Revision: 1.5.4.9 $ $Date: 2005/03/27 15:29:30 $
 */
public abstract class ClientBasePacket extends BasePacket implements Runnable
{
	private final static Log _log = LogFactory.getLog(ClientBasePacket.class.getName());
	
	protected ClientBasePacket(ByteBuffer buf, ClientThread client)
	{
		super(client);
		// flood protection
		if(Config.FLOOD_PROTECTION)
		{
			if(!client.checkFloodProtection())
			{
		        try {
		            ClientThread.saveCharToDisk(client.getActiveChar());
		            client.getActiveChar().sendMessage("Kicked for flooding");
		            client.getActiveChar().sendPacket(new LeaveWorld());
		            client.getActiveChar().deleteMe();
		            client.getActiveChar().logout();
		            _log.warn("Warning : client " +client.getActiveChar().getName()+ " tryed to flood server !!!");
		            } catch (Throwable t)   {}
		 
		        try {
		        	client.getActiveChar().closeNetConnection();
		            } catch (Throwable t)   {} 
			}
		}
		// ends
		if (_log.isDebugEnabled()) _log.debug(getType()+" <<< "+client.getLoginName());
		_buf = buf;
		if (Config.ASSERT) assert _buf.position() == 1;
	}

	/** urgent messages are executed immediatly */
	public TaskPriority getPriority() { return TaskPriority.PR_NORMAL; }
	
	public final void run()
	{
		//assert _isValid;
		try
		{
            runImpl();
            if (!(this instanceof ValidatePosition || this instanceof Appearing || this instanceof EnterWorld || this instanceof RequestPledgeInfo || this instanceof RequestSkillList || this instanceof RequestQuestList || getClient().getActiveChar() == null)) getClient().getActiveChar().onActionRequest();
		}
		catch (Throwable e)
		{
			L2PcInstance player = getClient().getActiveChar();
			if (player != null)
			{
				_log.fatal("Character "+player.getName()+" of account "+player.getAccountName()+" caused the following error at packet-handling: "+getType(), e);			
			}
			else
			   _log.fatal("error handling client message "+getType(), e);
		}
		
	}
	
	/**
	 * This is only called once per packet instane ie: when you construct a packet and send it to many players,
	 * it will only run when the first packet is sent
	 */
	abstract void runImpl() throws L2JFunctionnalException;
	
	protected void sendPacket(ServerBasePacket msg)
	{
		getConnection().sendPacket(msg);
	}

	public final int readD()
	{
	    try {
		return _buf.getInt();
	    } catch (Exception e) {}
	    return 0;
	}

	public final int readC()
	{
	    try {
		return _buf.get() & 0xFF;
	    } catch (Exception e) {}
	    return 0;
	}

	public final int readH()
	{
	    try {
		return _buf.getShort() & 0xFFFF;
	    } catch (Exception e) {}
	    return 0;
	}

	public final double readF()
	{
	    try {
		return _buf.getDouble();
	    } catch (Exception e) {_log.error(e.getMessage(),e);}
	    return 0;
	}

    public final long readQ()
    {
        try {
            return _buf.getLong();
        } catch (Exception e) 
        {
            _log.error(e.getMessage(),e);
        }
        return 0;
    }

	public final String readS()
	{
        TextBuilder sb = new TextBuilder();
		char ch;
	    try {
		while ((ch = _buf.getChar()) != 0)
			sb.append(ch);
	    } catch (Exception e) {}
		return sb.toString();
	}
	
	public final byte[] readB(int length)
	{
		byte[] result = new byte[length];
	    try {
		_buf.get(result);
	    } catch (Exception e) {}
		return result;
	}
}
